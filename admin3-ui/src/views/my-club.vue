<template>
  <div class="my-club">
    <div v-if="loading" class="loading-wrap">
      <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
    </div>

    <div v-else-if="!club" class="empty-wrap">
      <el-empty description="您暂未负责任何社团">
        <el-button type="primary" @click="$router.push('/clubs')">前往社团列表</el-button>
      </el-empty>
    </div>

    <div v-else class="content-wrap">
      <!-- 社团信息卡片 -->
      <el-card class="club-info-card">
        <template #header>
          <div class="card-header">
            <span>社团信息</span>
            <el-button type="primary" link size="small" @click="showEditDialog">
              <el-icon><Edit /></el-icon> 编辑社团信息
            </el-button>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="社团名称">{{ club.name }}</el-descriptions-item>
          <el-descriptions-item label="社团类别">
            {{ ClubCategoryLabel(club.category) }}
          </el-descriptions-item>
          <el-descriptions-item label="成立日期">{{ club.foundedDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="社团状态">
            <el-tag :type="club.state === 'ACTIVE' ? 'success' : 'danger'">
              {{ ClubStateLabel(club.state) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">{{ club.owner?.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="成员人数">{{ club.memberCount }} 人</el-descriptions-item>
        </el-descriptions>

        <div class="description-section">
          <h4>社团简介</h4>
          <p class="description-text">{{ club.description || '暂无简介' }}</p>
        </div>
      </el-card>

      <!-- 标签页：成员和活动 -->
      <el-card class="tabs-card">
        <el-tabs v-model="activeTab">
          <!-- 成员列表 -->
          <el-tab-pane label="成员列表" name="members">
            <div class="tab-header">
              <span>共 {{ members.length }} 名成员</span>
              <el-button type="primary" size="small" @click="showAddMemberDialog">
                <el-icon><Plus /></el-icon> 添加成员
              </el-button>
            </div>
            <el-table :data="members" stripe>
              <el-table-column prop="user.username" label="用户名" min-width="120" />
              <el-table-column label="角色" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.role === 'LEADER' ? 'warning' : 'info'" size="small">
                    {{ MemberRoleMap[row.role as MemberRole] }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" align="center">
                <template #default="{ row }">
                  <el-button
                    v-if="row.role === 'MEMBER'"
                    type="warning"
                    link
                    size="small"
                    @click="handleSetLeader(row)"
                  >
                    设为管理员
                  </el-button>
                  <el-button
                    v-else
                    type="info"
                    link
                    size="small"
                    @click="handleSetMember(row)"
                  >
                    设为普通成员
                  </el-button>
                  <el-button type="danger" link size="small" @click="handleRemoveMember(row)">
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 活动列表 -->
          <el-tab-pane label="我的活动" name="activities">
            <div class="tab-header">
              <span>共 {{ activities.length }} 个活动</span>
              <el-button type="primary" size="small" @click="$router.push('/activities')">
                <el-icon><Plus /></el-icon> 创建活动
              </el-button>
            </div>
            <el-table :data="activities" stripe>
              <el-table-column prop="title" label="活动标题" min-width="160">
                <template #default="{ row }">
                  <el-link type="primary" @click="$router.push(`/activities/detail/${row.id}`)">
                    {{ row.title }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="location" label="地点" width="120" />
              <el-table-column prop="activityTime" label="时间" width="160">
                <template #default="{ row }">{{ formatTime(row.activityTime) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="ActivityStatusType(row.status)" size="small">
                    {{ ActivityStatusLabel(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="报名" width="80" align="center">
                <template #default="{ row }">
                  {{ row.currentParticipants }}/{{ row.maxParticipants }}
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <!-- 编辑社团弹窗 -->
    <el-dialog title="编辑社团信息" v-model="editDialogVisible" width="40%">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="社团名称" required>
          <el-input v-model="editForm.name"></el-input>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="editForm.description" type="textarea" :rows="3"></el-input>
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="editForm.category" style="width: 100%">
            <el-option v-for="item in ClubCategoryList" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="成立日期">
          <el-date-picker v-model="editForm.foundedDate" type="date" value-format="YYYY-MM-DD" style="width: 100%"></el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加成员弹窗 -->
    <el-dialog title="添加成员" v-model="addMemberDialogVisible" width="40%">
      <el-form :model="addMemberForm" label-width="90px">
        <el-form-item label="选择用户" required>
          <el-select v-model="addMemberForm.userId" placeholder="请选择用户" filterable style="width: 100%">
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="addMemberForm.role" style="width: 100%">
            <el-option v-for="(label, key) in MemberRoleMap" :key="key" :label="label" :value="key"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleAddMember">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Loading, Edit, Plus } from '@element-plus/icons-vue';
import {
  Club,
  ClubCategoryList,
  ClubCategoryLabel,
  ClubStateLabel,
  MemberRole,
  MemberRoleMap,
  ClubMember,
  getClubList,
  updateClub,
  getMemberList,
  addMember,
  removeMember,
  updateMemberRole
} from '../api/club';
import { getUserList } from '../api/user';
import {
  Activity,
  ActivityStatusLabel,
  ActivityStatusType,
  getActivityList
} from '../api/activity';

const loading = ref(true);
const club = ref<Club>();
const members = ref<ClubMember[]>([]);
const activities = ref<Activity[]>([]);
const activeTab = ref('members');
const userOptions = ref<{ id: number; username: string }[]>([]);

// 编辑弹窗
const editDialogVisible = ref(false);
const editForm = reactive({
  name: '',
  description: '',
  category: 'OTHER' as string,
  foundedDate: ''
});

// 添加成员弹窗
const addMemberDialogVisible = ref(false);
const addMemberForm = reactive({
  userId: 0 as number,
  role: 'MEMBER' as MemberRole
});

// 可添加的用户（排除已在社团中的）
const availableUsers = computed(() => {
  const memberUserIds = members.value.map(m => m.user.id);
  return userOptions.value.filter(u => !memberUserIds.includes(u.id));
});

const formatTime = (time: string) => {
  if (!time) return '-';
  return time.replace('T', ' ').substring(0, 16);
};

const fetchMyClub = async () => {
  try {
    // 获取所有社团，找到当前用户负责的社团
    const res = await getClubList({ page: 1, size: 100 });
    const currentUserId = getCurrentUserId();
    const ownedClub = (res.data.list || []).find(
      (c: any) => c.owner?.id === currentUserId
    );
    club.value = ownedClub;
    if (ownedClub) {
      fetchMembers();
      fetchActivities();
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const fetchMembers = async () => {
  if (!club.value) return;
  try {
    const res = await getMemberList(club.value.id, { page: 1, size: 100 });
    members.value = res.data.list || [];
  } catch (e) {
    console.error(e);
  }
};

const fetchActivities = async () => {
  if (!club.value) return;
  try {
    const res = await getActivityList({ page: 1, size: 100, clubId: club.value.id });
    activities.value = res.data.list || [];
  } catch (e) {
    console.error(e);
  }
};

const fetchUsers = async () => {
  try {
    const res = await getUserList({ page: 1, size: 200 });
    userOptions.value = (res.data.list || []).map((u: any) => ({ id: u.id, username: u.username }));
  } catch (e) {
    console.error(e);
  }
};

const getCurrentUserId = (): number => {
  const token = localStorage.getItem('token');
  if (!token) return 0;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.userId || 0;
  } catch {
    return 0;
  }
};

const showEditDialog = () => {
  if (!club.value) return;
  editForm.name = club.value.name;
  editForm.description = club.value.description;
  editForm.category = club.value.category;
  editForm.foundedDate = club.value.foundedDate;
  editDialogVisible.value = true;
};

const handleSaveEdit = () => {
  if (!club.value) return;
  if (!editForm.name.trim()) {
    ElMessage.warning('请填写社团名称');
    return;
  }
  updateClub(club.value.id, {
    name: editForm.name,
    description: editForm.description,
    category: editForm.category as any,
    foundedDate: editForm.foundedDate,
    state: club.value.state,
    ownerId: club.value.owner?.id || 0
  }).then(() => {
    ElMessage.success('保存成功');
    editDialogVisible.value = false;
    fetchMyClub();
  }).catch((e: any) => {
    ElMessage.error(e.message || '保存失败');
  });
};

const showAddMemberDialog = () => {
  addMemberForm.userId = 0;
  addMemberForm.role = 'MEMBER';
  addMemberDialogVisible.value = true;
};

const handleAddMember = () => {
  if (!club.value || !addMemberForm.userId) {
    ElMessage.warning('请选择用户');
    return;
  }
  addMember(club.value.id, { userId: addMemberForm.userId, role: addMemberForm.role }).then(() => {
    ElMessage.success('添加成功');
    addMemberDialogVisible.value = false;
    fetchMembers();
  }).catch((e: any) => {
    ElMessage.error(e.message || '添加失败');
  });
};

const handleSetLeader = (row: ClubMember) => {
  if (!club.value) return;
  updateMemberRole(club.value.id, row.user.id, 'LEADER').then(() => {
    ElMessage.success('已设为管理员');
    fetchMembers();
  });
};

const handleSetMember = (row: ClubMember) => {
  if (!club.value) return;
  updateMemberRole(club.value.id, row.user.id, 'MEMBER').then(() => {
    ElMessage.success('已设为普通成员');
    fetchMembers();
  });
};

const handleRemoveMember = (row: ClubMember) => {
  if (!club.value) return;
  ElMessageBox.confirm(`确定要移除成员"${row.user.username}"吗？`, '提示', { type: 'warning' })
    .then(() => {
      removeMember(club.value!.id, row.user.id).then(() => {
        ElMessage.success('已移除');
        fetchMembers();
      });
    }).catch(() => {});
};

onMounted(() => {
  fetchMyClub();
  fetchUsers();
});
</script>

<style scoped lang="scss">
.my-club {
  padding: 20px;

  .loading-wrap, .empty-wrap {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
  }

  .content-wrap {
    .club-info-card {
      margin-bottom: 20px;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .description-section {
        margin-top: 20px;

        h4 {
          margin-bottom: 10px;
          color: #606266;
        }

        .description-text {
          color: #909399;
          line-height: 1.8;
          white-space: pre-wrap;
        }
      }
    }

    .tabs-card {
      .tab-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        padding-bottom: 10px;
        border-bottom: 1px solid #ebeef5;
      }
    }
  }
}
</style>
