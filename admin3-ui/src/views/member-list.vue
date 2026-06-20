<template>
  <div class="member-wrap">
    <div class="handle-box">
      <el-select v-model="selectedClubId" placeholder="选择社团" class="handle-select mr10" clearable style="width: 200px">
        <el-option v-for="club in clubOptions" :key="club.id" :label="club.name" :value="club.id"></el-option>
      </el-select>
    </div>

    <el-tabs v-model="activeTab" v-if="selectedClubId">
      <!-- 成员管理 Tab -->
      <el-tab-pane label="成员管理" name="members">
        <div class="handle-box" style="margin-bottom: 12px;">
          <el-select v-model="query.role" placeholder="成员角色" class="handle-select mr10" clearable style="width: 140px">
            <el-option v-for="(label, key) in MemberRoleMap" :key="key" :label="label" :value="key"></el-option>
          </el-select>
          <el-button type="primary" @click="handleSearch" plain>搜索</el-button>
          <el-button @click="handleAdd" type="primary" v-action:club:manage style="float: right">添加成员</el-button>
        </div>

        <el-table v-if="tableData.length > 0" :data="tableData" border class="table" header-cell-class-name="table-header">
          <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
          <el-table-column label="用户名" min-width="120">
            <template #default="{ row }">{{ row.user?.username || '-' }}</template>
          </el-table-column>
          <el-table-column label="所属社团" min-width="140">
            <template #default="{ row }">{{ row.club?.name || '-' }}</template>
          </el-table-column>
          <el-table-column label="角色" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.role === 'OWNER' ? 'danger' : row.role === 'VICE' ? 'warning' : 'info'">{{ MemberRoleMap[row.role as MemberRole] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleToggleRole(row)" v-action:club:manage>
                {{ row.role === 'MEMBER' ? '设为副社长' : '设为普通成员' }}
              </el-button>
              <el-button type="danger" link size="small" @click="handleRemove(row)" v-action:club:manage>移除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="tableData.length === 0" description="暂无成员数据" />
        <div class="pagination">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :current-page="query.page"
            :page-size="query.size"
            :total="pageTotal"
            @current-change="handlePageChange"
          ></el-pagination>
        </div>

        <!-- 添加成员弹出框 -->
        <el-dialog title="添加成员" v-model="addDialogVisible" width="40%">
          <el-form :model="addForm" label-width="90px">
            <el-form-item label="选择用户" required>
              <el-select v-model="addForm.userId" placeholder="请选择用户" filterable style="width: 100%">
                <el-option v-for="user in userOptions" :key="user.id" :label="user.username" :value="user.id"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="addForm.role" style="width: 100%">
                <el-option v-for="(label, key) in MemberRoleMap" :key="key" :label="label" :value="key"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="addDialogVisible = false">取 消</el-button>
              <el-button type="primary" @click="saveAddMember">确 定</el-button>
            </span>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- 入社申请 Tab -->
      <el-tab-pane label="入社申请" name="applications">
        <el-table :data="applications" border v-if="applications.length" class="table" header-cell-class-name="table-header">
          <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
          <el-table-column label="申请人" min-width="120">
            <template #default="{ row }">{{ row.realName || row.username || '-' }}</template>
          </el-table-column>
          <el-table-column label="申请时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PENDING' ? 'warning' : row.status === 'APPROVED' ? 'success' : 'danger'" size="small">
                {{ row.status === 'PENDING' ? '待审核' : row.status === 'APPROVED' ? '已通过' : '已拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
                <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
              </template>
              <span v-else style="color: #909399; font-size: 12px;">已处理</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无入社申请" />
      </el-tab-pane>
    </el-tabs>

    <el-empty v-if="!selectedClubId" description="请先选择社团" />

    <!-- 拒绝申请弹窗 -->
    <el-dialog title="拒绝申请" v-model="rejectDialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="请输入拒绝原因（可选）"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  MemberRole,
  MemberRoleMap,
  ClubMember,
  getMemberList,
  addMember,
  removeMember,
  updateMemberRole
} from '../api/club';
import { getClubList } from '../api/club';
import { getUserList } from '../api/user';
import { getClubApplications, approveApplication, rejectApplication, type JoinApplication } from '../api/join-application';

const tableData = ref<ClubMember[]>([]);
const pageTotal = ref(0);
const clubOptions = ref<{ id: number; name: string }[]>([]);
const userOptions = ref<{ id: number; username: string }[]>([]);
const selectedClubId = ref<number | undefined>(undefined);

const query = reactive<{ page: number; size: number; role: MemberRole | undefined }>({
  role: undefined,
  page: 1,
  size: 10
});

const addDialogVisible = ref(false);
const addForm = reactive<{ userId: number; role: MemberRole }>({
  userId: 0,
  role: 'MEMBER'
});
const activeTab = ref('members');
const applications = ref<JoinApplication[]>([]);
const rejectDialogVisible = ref(false);
const rejectForm = reactive({ id: 0, reason: '' });

const fetchMembers = async () => {
  if (!selectedClubId.value) {
    tableData.value = [];
    pageTotal.value = 0;
    return;
  }
  try {
    const res = await getMemberList(selectedClubId.value, {
      page: query.page,
      size: query.size,
      role: query.role
    });
    tableData.value = res.data.list || [];
    pageTotal.value = res.data.total || 0;
  } catch (e) {
    console.error(e);
  }
};

const fetchClubs = async () => {
  try {
    const res = await getClubList({ page: 1, size: 100 });
    clubOptions.value = (res.data.list || []).map((c: any) => ({ id: c.id, name: c.name }));
    if (clubOptions.value.length > 0) {
      selectedClubId.value = clubOptions.value[0].id;
    }
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

const handleSearch = () => {
  query.page = 1;
  fetchMembers();
};

const handlePageChange = (page: number) => {
  query.page = page;
  fetchMembers();
};

const handleAdd = () => {
  addForm.userId = 0;
  addForm.role = 'MEMBER';
  addDialogVisible.value = true;
};

const saveAddMember = () => {
  if (!addForm.userId) {
    ElMessage.warning('请选择用户');
    return;
  }
  if (!selectedClubId.value) {
    ElMessage.warning('请先选择社团');
    return;
  }
  addMember(selectedClubId.value, { userId: addForm.userId, role: addForm.role }).then(() => {
    ElMessage.success('添加成功');
    addDialogVisible.value = false;
    fetchMembers();
  });
};

const handleRemove = (row: ClubMember) => {
  ElMessageBox.confirm(`确定要移除成员"${row.user?.username}"吗？`, '提示', { type: 'warning' })
    .then(() => {
      if (selectedClubId.value) {
        removeMember(selectedClubId.value, row.user.id).then(() => {
          ElMessage.success('移除成功');
          fetchMembers();
        });
      }
    })
    .catch(() => {});
};

const handleToggleRole = (row: ClubMember) => {
  if (!selectedClubId.value) return;
  const newRole: MemberRole = row.role === 'MEMBER' ? 'VICE' : 'MEMBER';
  updateMemberRole(selectedClubId.value, row.user.id, newRole).then(() => {
    ElMessage.success('角色已更新');
    fetchMembers();
  });
};

const fetchApplications = async () => {
  if (!selectedClubId.value) return;
  try {
    const res = await getClubApplications(selectedClubId.value);
    applications.value = res.data.list || [];
  } catch (e) {
    console.error(e);
  }
};

const formatTime = (t: string) => {
  if (!t) return '';
  return t.replace('T', ' ').substring(0, 16);
};

const handleApprove = async (row: JoinApplication) => {
  try {
    await approveApplication(row.id);
    ElMessage.success('已通过申请');
    fetchApplications();
    fetchMembers();
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败');
  }
};

const handleReject = (row: JoinApplication) => {
  rejectForm.id = row.id;
  rejectForm.reason = '';
  rejectDialogVisible.value = true;
};

const confirmReject = async () => {
  try {
    await rejectApplication(rejectForm.id, rejectForm.reason || undefined);
    ElMessage.success('已拒绝申请');
    rejectDialogVisible.value = false;
    fetchApplications();
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败');
  }
};

watch(selectedClubId, () => {
  query.page = 1;
  fetchMembers();
  fetchApplications();
});

onMounted(() => {
  fetchClubs();
  fetchUsers();
});
</script>

<style scoped lang="scss">
.member-wrap {
  position: relative;
  padding: 10px 20px;
  height: 100%;
}
</style>
