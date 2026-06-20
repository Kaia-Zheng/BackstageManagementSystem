<template>
  <div class="profile-container">
    <!-- 左侧用户信息卡片 -->
    <div class="profile-left">
      <el-card shadow="hover" class="user-card">
        <div class="user-avatar">
          <el-avatar :size="90" :src="profile.avatar || defaultAvatar" />
        </div>
        <div class="user-name">{{ profile.realName || profile.username }}</div>
        <div class="user-username">@{{ profile.username }}</div>
        <div class="user-roles">
          <el-tag v-for="role in profile.roles" :key="role" size="small" type="primary" style="margin: 2px 4px;">
            {{ role }}
          </el-tag>
        </div>
        <el-divider />
        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-value">{{ profile.clubCount }}</div>
            <div class="stat-label">社团</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ profile.activityCount }}</div>
            <div class="stat-label">活动</div>
          </div>
        </div>
        <el-divider />
        <div class="user-info-list">
          <div class="info-row">
            <el-icon><Phone /></el-icon>
            <span>{{ profile.phone || '未填写' }}</span>
          </div>
          <div class="info-row">
            <el-icon><Message /></el-icon>
            <span>{{ profile.email || '未填写' }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 右侧内容区域 -->
    <div class="profile-right">
      <el-card shadow="hover">
        <el-tabs v-model="activeTab">
          <!-- 我的概览 -->
          <el-tab-pane label="我的概览" name="overview">
            <el-row :gutter="16" class="stat-cards">
              <el-col :span="6">
                <el-card shadow="hover" class="stat-card stat-club">
                  <div class="stat-card-value">{{ profile.clubCount }}</div>
                  <div class="stat-card-label">加入社团</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover" class="stat-card stat-activity">
                  <div class="stat-card-value">{{ profile.activityCount }}</div>
                  <div class="stat-card-label">报名活动</div>
                </el-card>
              </el-col>
              <el-col :span="6" v-if="hasAuditPermission">
                <el-card shadow="hover" class="stat-card stat-pending">
                  <div class="stat-card-value">{{ profile.pendingAuditCount }}</div>
                  <div class="stat-card-label">待审核活动</div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 最近参与的活动 -->
            <div class="section-title">最近参与的活动</div>
            <el-table :data="profile.recentActivities" stripe v-if="profile.recentActivities?.length" style="width: 100%;">
              <el-table-column prop="title" label="活动标题" min-width="150" />
              <el-table-column prop="activityTime" label="活动时间" width="170">
                <template #default="{ row }">
                  {{ formatTime(row.activityTime) }}
                </template>
              </el-table-column>
              <el-table-column label="活动状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="activityStatusType(row.activityStatus)" size="small">
                    {{ activityStatusLabel(row.activityStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="报名状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="registerStatusType(row.registerStatus)" size="small">
                    {{ registerStatusLabel(row.registerStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无参与的活动" :image-size="80" />

            <!-- 我管理的社团 -->
            <template v-if="profile.managedClubs?.length">
              <div class="section-title" style="margin-top: 24px;">我管理的社团</div>
              <el-row :gutter="16">
                <el-col :span="8" v-for="club in profile.managedClubs" :key="club.clubId">
                  <el-card shadow="hover" class="managed-club-card" @click="$router.push('/members?clubId=' + club.clubId)">
                    <div class="managed-club-name">{{ club.clubName }}</div>
                    <div class="managed-club-info">
                      <el-tag size="small" type="info">{{ categoryLabel(club.category) }}</el-tag>
                      <span class="managed-club-count">{{ club.memberCount }}人</span>
                    </div>
                    <el-button type="primary" size="small" text>管理社团 &rarr;</el-button>
                  </el-card>
                </el-col>
              </el-row>
            </template>
          </el-tab-pane>

          <!-- 我的社团 -->
          <el-tab-pane label="我的社团" name="clubs">
            <el-row :gutter="16" v-if="profile.clubs?.length">
              <el-col :xs="24" :sm="12" :md="8" v-for="club in profile.clubs" :key="club.clubId">
                <el-card shadow="hover" class="club-card">
                  <div class="club-card-header">
                    <div class="club-card-name">{{ club.clubName }}</div>
                    <el-tag :type="club.role === 'OWNER' || club.role === 'VICE' ? 'warning' : 'info'" size="small">
                      {{ roleLabel(club.role) }}
                    </el-tag>
                  </div>
                  <div class="club-card-category">
                    <el-tag size="small" type="info">{{ categoryLabel(club.category) }}</el-tag>
                  </div>
                  <div class="club-card-members">
                    <el-icon><User /></el-icon>
                    <span>{{ club.memberCount }} 人</span>
                  </div>
                  <div class="club-card-actions" v-if="club.role === 'OWNER' || club.role === 'VICE'">
                    <el-button type="primary" size="small" @click="$router.push('/members?clubId=' + club.clubId)">
                      管理社团
                    </el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <el-empty v-else description="暂未加入任何社团" :image-size="100" />
          </el-tab-pane>

          <!-- 我的活动 -->
          <el-tab-pane label="我的活动" name="activities">
            <el-table :data="profile.activities" stripe v-if="profile.activities?.length" style="width: 100%;">
              <el-table-column prop="title" label="活动标题" min-width="150" />
              <el-table-column prop="clubName" label="所属社团" width="120" />
              <el-table-column label="活动时间" width="170">
                <template #default="{ row }">
                  {{ formatTime(row.activityTime) }}
                </template>
              </el-table-column>
              <el-table-column label="报名状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="registerStatusType(row.registerStatus)" size="small">
                    {{ registerStatusLabel(row.registerStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="活动状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="activityStatusType(row.activityStatus)" size="small">
                    {{ activityStatusLabel(row.activityStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160" align="center">
                <template #default="{ row }">
                  <el-button v-if="row.activityStatus === 'PUBLISHED' && row.registerStatus === 'REGISTERED'"
                    type="danger" size="small" text @click="handleCancelRegistration(row)">
                    取消报名
                  </el-button>
                  <el-button v-if="row.activityStatus === 'ENDED'"
                    type="primary" size="small" text @click="$router.push('/activities/detail/' + row.activityId)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无报名的活动" :image-size="100" />
          </el-tab-pane>

          <!-- 个人设置 -->
          <el-tab-pane label="个人设置" name="settings">
            <el-form :model="settingsForm" :rules="settingsRules" ref="settingsFormRef" label-width="100px" style="max-width: 500px;">
              <el-form-item label="头像">
                <div class="avatar-upload">
                  <el-avatar :size="64" :src="settingsForm.avatar || defaultAvatar" />
                  <el-upload
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="handleAvatarUpload"
                    accept="image/*"
                  >
                    <el-button type="primary" size="small" style="margin-left: 12px;">更换头像</el-button>
                  </el-upload>
                </div>
              </el-form-item>
              <el-form-item label="用户名">
                <el-input :model-value="profile.username" disabled />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input :model-value="profile.realName || '未填写'" disabled />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="settingsForm.phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="settingsForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveSettings" :loading="saving">保存修改</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus';
import { Phone, Message, User } from '@element-plus/icons-vue';
import { getProfile, updateProfile, type ProfileData } from '../api/profile';
import { cancelRegistration } from '../api/activity';

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
const activeTab = ref('overview');
const loading = ref(false);
const saving = ref(false);
const settingsFormRef = ref<FormInstance>();

const profile = ref<ProfileData>({
  userId: 0, username: '', realName: '', avatar: '', phone: '', email: '',
  gender: '', state: '', roles: [], permissions: [],
  clubCount: 0, activityCount: 0, pendingAuditCount: 0,
  clubs: [], activities: [], recentActivities: [], managedClubs: []
});

const settingsForm = reactive({
  avatar: '',
  phone: '',
  email: ''
});

const hasAuditPermission = computed(() => profile.value.permissions?.includes('activity:audit'));

const settingsRules: FormRules = {
  phone: [
    { pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { pattern: /^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '邮箱格式不正确', trigger: 'blur' }
  ]
};

const categoryMap: Record<string, string> = {
  ACADEMIC: '学术科技', CULTURAL: '文化体育', SPORTS: '运动竞技', VOLUNTEER: '公益志愿', OTHER: '其他'
};
const activityStatusMap: Record<string, { label: string; type: string }> = {
  PENDING: { label: '待审核', type: 'warning' },
  PUBLISHED: { label: '已发布', type: 'success' },
  ONGOING: { label: '进行中', type: 'primary' },
  ENDED: { label: '已结束', type: 'info' },
  CANCELLED: { label: '已取消', type: 'danger' }
};
const registerStatusMap: Record<string, { label: string; type: string }> = {
  REGISTERED: { label: '已报名', type: 'success' },
  CHECKED_IN: { label: '已签到', type: 'primary' },
  CANCELLED: { label: '已取消', type: 'info' }
};
const roleMap: Record<string, string> = { OWNER: '负责人', VICE: '副社长', MEMBER: '成员' };

const categoryLabel = (c: string) => categoryMap[c] || c;
const activityStatusLabel = (s: string) => activityStatusMap[s]?.label || s;
const activityStatusType = (s: string) => activityStatusMap[s]?.type || 'info';
const registerStatusLabel = (s: string) => registerStatusMap[s]?.label || s;
const registerStatusType = (s: string) => registerStatusMap[s]?.type || 'info';
const roleLabel = (r: string) => roleMap[r] || r;

const formatTime = (t: string) => {
  if (!t) return '';
  return t.replace('T', ' ').substring(0, 16);
};

const fetchProfile = async () => {
  loading.value = true;
  try {
    const res = await getProfile();
    profile.value = res.data;
    settingsForm.avatar = res.data.avatar || '';
    settingsForm.phone = res.data.phone || '';
    settingsForm.email = res.data.email || '';
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const handleCancelRegistration = async (row: any) => {
  try {
    await cancelRegistration(row.activityId);
    ElMessage.success('取消报名成功');
    fetchProfile();
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '取消报名失败');
  }
};

const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片文件');
  }
  return isImage;
};

const handleAvatarUpload = (options: UploadRequestOptions) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    settingsForm.avatar = e.target?.result as string;
  };
  reader.readAsDataURL(options.file);
};

const saveSettings = () => {
  if (!settingsFormRef.value) return;
  settingsFormRef.value.validate(async (valid) => {
    if (!valid) return;
    saving.value = true;
    try {
      await updateProfile({
        avatar: settingsForm.avatar,
        phone: settingsForm.phone,
        email: settingsForm.email
      });
      ElMessage.success('保存成功');
      fetchProfile();
    } catch (e: any) {
      ElMessage.error(e?.response?.data?.message || '保存失败');
    } finally {
      saving.value = false;
    }
  });
};

onMounted(() => {
  fetchProfile();
});
</script>

<style scoped>
.profile-container {
  display: flex;
  gap: 20px;
  padding: 0;
}

.profile-left {
  width: 280px;
  flex-shrink: 0;
}

.profile-right {
  flex: 1;
  min-width: 0;
}

.user-card {
  text-align: center;
}

.user-avatar {
  padding: 10px 0;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.user-username {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.user-roles {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
}

.user-stats {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.user-info-list {
  text-align: left;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 13px;
  color: #606266;
}

.stat-cards {
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
  padding: 10px 0;
}

.stat-card-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-card-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.stat-club .stat-card-value { color: #409eff; }
.stat-activity .stat-card-value { color: #67c23a; }
.stat-pending .stat-card-value { color: #e6a23c; }

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.managed-club-card {
  cursor: pointer;
  transition: box-shadow 0.3s;
  margin-bottom: 12px;
}

.managed-club-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.managed-club-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.managed-club-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.managed-club-count {
  font-size: 12px;
  color: #909399;
}

.club-card {
  margin-bottom: 16px;
}

.club-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.club-card-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.club-card-category {
  margin-bottom: 8px;
}

.club-card-members {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
}

.club-card-actions {
  text-align: right;
}

.avatar-upload {
  display: flex;
  align-items: center;
}
</style>
