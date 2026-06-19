<template>
  <el-dialog title="报名列表" v-model="visible" width="65%" @close="handleClose">
    <div v-if="loading" style="text-align: center; padding: 40px;">
      <el-icon class="is-loading"><Loading /></el-icon>
    </div>
    <div v-else>
      <div style="margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="margin-right: 20px;">总报名人数：<strong>{{ registrations.length }}</strong></span>
        </div>
        <el-tag v-if="activity" type="info">活动：{{ activity.title }}</el-tag>
      </div>

      <el-table :data="registrations" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="user.username" label="报名人" min-width="120">
          <template #default="{ row }">{{ row.user?.username || '-' }}</template>
        </el-table-column>
        <el-table-column prop="registerTime" label="报名时间" width="170">
          <template #default="{ row }">{{ formatTime(row.registerTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :color="RegistrationStatusColor(row.status)" style="color: white">
              {{ RegistrationStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'REGISTERED'"
              type="success"
              link
              size="small"
              @click="handleCheckIn(row)"
            >
              签到
            </el-button>
            <span v-else-if="row.status === 'CHECKED_IN'" style="color: #67c23a;">已签到</span>
            <span v-else style="color: #909399;">已取消</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="registrations.length === 0" description="暂无报名记录" />
    </div>

    <template #footer>
      <el-button @click="handleClose">关 闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import {
  Registration,
  RegistrationStatusLabel,
  RegistrationStatusColor,
  getActivityRegistrations,
  checkInRegistration
} from '../api/activity';
import { Activity } from '../api/activity';

const props = defineProps<{
  modelValue: boolean;
  activityId?: number;
  activity?: Activity;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
}>();

const visible = ref(false);
const loading = ref(false);
const registrations = ref<Registration[]>([]);

watch(() => props.modelValue, (val) => {
  visible.value = val;
  if (val && props.activityId) {
    fetchRegistrations();
  }
}, { immediate: true });

const fetchRegistrations = async () => {
  if (!props.activityId) return;
  loading.value = true;
  try {
    const res = await getActivityRegistrations(props.activityId, { page: 1, size: 100 });
    registrations.value = res.data.list || [];
  } catch (e) {
    console.error(e);
    ElMessage.error('获取报名列表失败');
  } finally {
    loading.value = false;
  }
};

const handleCheckIn = (row: Registration) => {
  if (!props.activityId) return;
  checkInRegistration(props.activityId, row.user.id).then(() => {
    ElMessage.success(`已为 ${row.user.username} 签到`);
    fetchRegistrations();
  }).catch((e: any) => {
    ElMessage.error(e.message || '签到失败');
  });
};

const formatTime = (time: string) => {
  if (!time) return '-';
  return time.replace('T', ' ').substring(0, 19);
};

const handleClose = () => {
  emit('update:modelValue', false);
};
</script>
