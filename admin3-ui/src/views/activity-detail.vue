<template>
  <div class="activity-detail">
    <el-page-header @back="goBack" content="活动详情" />

    <div v-if="loading" class="loading-wrap">
      <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
    </div>

    <div v-else-if="activity" class="content-wrap">
      <!-- 活动信息卡片 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span class="title">{{ activity.title }}</span>
            <el-tag :type="ActivityStatusType(activity.status)" effect="dark" size="large" round>
              {{ ActivityStatusLabel(activity.status) }}
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="活动地点">{{ activity.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="活动时间">{{ formatTime(activity.activityTime) }}</el-descriptions-item>
          <el-descriptions-item label="所属社团">{{ activity.club?.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报名人数">
            <span>{{ activity.currentParticipants }} / {{ activity.maxParticipants }}</span>
            <el-progress
              :percentage="getPercentage(activity.currentParticipants, activity.maxParticipants)"
              :stroke-width="8"
              style="width: 150px; display: inline-block; margin-left: 10px;"
            />
          </el-descriptions-item>
        </el-descriptions>

        <div class="description-section">
          <h4>活动简介</h4>
          <p class="description-text">{{ activity.description || '暂无简介' }}</p>
        </div>
      </el-card>

      <!-- 操作区域 -->
      <el-card class="action-card">
        <template #header>
          <span>操作</span>
        </template>

        <div class="action-buttons">
          <!-- 已发布状态 - 显示报名按钮 -->
          <template v-if="activity.status === 'PUBLISHED'">
            <el-button v-if="!myRegistration" type="primary" size="large" @click="handleRegister" :disabled="!canRegister">
              立即报名
            </el-button>
            <el-button v-else type="warning" size="large" @click="handleCancelRegister">
              取消报名
            </el-button>
          </template>

          <!-- 进行中状态 - 显示签到按钮（仅报名用户） -->
          <template v-if="activity.status === 'ONGOING' && myRegistration && myRegistration.status === 'REGISTERED'">
            <el-button type="success" size="large" @click="handleSelfCheckIn">
              我要签到
            </el-button>
          </template>

          <!-- 报名状态提示 -->
          <div v-if="myRegistration" class="my-status">
            <el-alert
              :title="`您已${myRegistration.status === 'CHECKED_IN' ? '签到' : '报名'}此活动`"
              :type="myRegistration.status === 'CHECKED_IN' ? 'success' : 'info'"
              :closable="false"
              show-icon
            />
          </div>
        </div>
      </el-card>

      <!-- 报名列表区域 -->
      <el-card class="registration-card">
        <template #header>
          <div class="card-header">
            <span>报名列表</span>
            <el-button type="primary" link size="small" @click="fetchRegistrations">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </template>

        <el-table :data="registrations" stripe>
          <el-table-column prop="user.username" label="报名人" min-width="120" />
          <el-table-column prop="registerTime" label="报名时间" width="170">
            <template #default="{ row }">{{ formatTime(row.registerTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :color="RegistrationStatusColor(row.status)" style="color: white" size="small">
                {{ RegistrationStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="registrations.length === 0" description="暂无报名记录" />
      </el-card>
    </div>

    <div v-else class="empty-wrap">
      <el-empty description="活动不存在或已被删除" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Loading, Refresh } from '@element-plus/icons-vue';
import {
  Activity,
  ActivityStatusLabel,
  ActivityStatusType,
  Registration,
  RegistrationStatusLabel,
  RegistrationStatusColor,
  getActivity,
  getMyRegistration,
  registerActivity,
  cancelRegistration,
  getActivityRegistrations
} from '../api/activity';

const route = useRoute();
const router = useRouter();

const activity = ref<Activity>();
const myRegistration = ref<Registration | null>(null);
const registrations = ref<Registration[]>([]);
const loading = ref(true);

const goBack = () => {
  router.back();
};

const formatTime = (time: string) => {
  if (!time) return '-';
  return time.replace('T', ' ').substring(0, 16);
};

const getPercentage = (current: number, max: number) => {
  if (max === 0) return 0;
  return Math.round((current / max) * 100);
};

const canRegister = computed(() => {
  if (!activity.value) return false;
  return activity.value.currentParticipants < activity.value.maxParticipants;
});

const fetchActivity = async () => {
  const id = Number(route.params.id);
  try {
    activity.value = await getActivity(id).then(res => res.data);
    // 获取当前用户的报名状态
    try {
      myRegistration.value = await getMyRegistration(id).then(res => res.data);
    } catch {
      myRegistration.value = null;
    }
  } catch (e) {
    console.error(e);
    activity.value = undefined;
  } finally {
    loading.value = false;
  }
};

const fetchRegistrations = async () => {
  if (!activity.value) return;
  try {
    const res = await getActivityRegistrations(activity.value.id, { page: 1, size: 100 });
    registrations.value = res.data.list || [];
  } catch (e) {
    console.error(e);
  }
};

const handleRegister = () => {
  if (!activity.value) return;
  registerActivity(activity.value.id).then(() => {
    ElMessage.success('报名成功');
    fetchActivity();
    fetchRegistrations();
  }).catch((e: any) => {
    ElMessage.error(e.message || '报名失败');
  });
};

const handleCancelRegister = () => {
  if (!activity.value) return;
  cancelRegistration(activity.value.id).then(() => {
    ElMessage.success('已取消报名');
    fetchActivity();
    fetchRegistrations();
  }).catch((e: any) => {
    ElMessage.error(e.message || '取消失败');
  });
};

const handleSelfCheckIn = () => {
  // 这里是用户自己签到，需要调用签到接口
  // 但目前的签到接口是管理员操作的，所以这里提示用户联系签到员
  ElMessage.info('请出示报名凭证，由活动工作人员为您签到');
};

onMounted(() => {
  fetchActivity();
  fetchRegistrations();
});
</script>

<style scoped lang="scss">
.activity-detail {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;

  .loading-wrap, .empty-wrap {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
  }

  .content-wrap {
    margin-top: 20px;

    .info-card {
      margin-bottom: 20px;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .title {
          font-size: 18px;
          font-weight: 600;
        }
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

    .action-card {
      margin-bottom: 20px;

      .action-buttons {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 15px;

        .my-status {
          margin-top: 10px;
        }
      }
    }

    .registration-card {
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }
}
</style>
