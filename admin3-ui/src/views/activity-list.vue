<template>
  <div class="activity-wrap">
    <div class="handle-box">
      <el-input v-model="query.title" placeholder="活动标题" class="handle-input mr10" clearable style="width: 180px"></el-input>
      <el-select v-model="query.clubId" placeholder="所属社团" class="handle-select mr10" clearable style="width: 160px">
        <el-option v-for="club in clubOptions" :key="club.id" :label="club.name" :value="club.id"></el-option>
      </el-select>
      <el-select v-model="query.status" placeholder="状态" class="handle-select mr10" clearable style="width: 120px">
        <el-option v-for="item in ActivityStatusList" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch" plain>搜索</el-button>
      <el-button @click="handleReset" plain>重置</el-button>
      <el-button type="primary" @click="handleAdd" v-action:activity:create style="float: right">新建活动</el-button>
    </div>

    <el-table :data="tableData" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="title" label="活动标题" min-width="160"></el-table-column>
      <el-table-column prop="description" label="简介" min-width="180" show-overflow-tooltip></el-table-column>
      <el-table-column prop="location" label="地点" width="110"></el-table-column>
      <el-table-column prop="activityTime" label="活动时间" width="160"></el-table-column>
      <el-table-column label="所属社团" width="130">
        <template #default="{ row }">{{ row.club?.name || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :color="ActivityStatusColor(row.status)" style="color: white">{{ ActivityStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="maxParticipants" label="最大人数" width="90" align="center"></el-table-column>
      <el-table-column prop="currentParticipants" label="已报名" width="80" align="center"></el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button type="success" link size="small" @click="handleApprove(row)" v-action:activity:audit>审核通过</el-button>
            <el-button type="danger" link size="small" @click="handleReject(row)" v-action:activity:audit>审核不通过</el-button>
          </template>
          <template v-if="row.status === 'PUBLISHED'">
            <el-button type="primary" link size="small" @click="handleStart(row)" v-action:activity:update>开始活动</el-button>
            <el-button type="danger" link size="small" @click="handleCancel(row)" v-action:activity:update>取消</el-button>
          </template>
          <template v-if="row.status === 'ONGOING'">
            <el-button type="success" link size="small" @click="handleEnd(row)" v-action:activity:update>结束活动</el-button>
          </template>
          <el-button type="primary" link size="small" @click="handleEdit(row)" v-action:activity:update>编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)" v-action:activity:delete>删除</el-button>
        </template>
      </el-table-column>
    </el-table>
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

    <!-- 新增/编辑弹出框 -->
    <el-dialog :title="formTitle" v-model="dialogVisible" width="45%">
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动标题" required>
          <el-input v-model="form.title"></el-input>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3"></el-input>
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="form.location"></el-input>
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker
            v-model="form.activityTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="所属社团" required>
          <el-select v-model="form.clubId" style="width: 100%">
            <el-option v-for="club in clubOptions" :key="club.id" :label="club.name" :value="club.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="最大人数">
          <el-input-number v-model="form.maxParticipants" :min="1" :max="10000" style="width: 100%"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="saveForm">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  ActivityStatus,
  ActivityStatusList,
  ActivityStatusLabel,
  ActivityStatusColor,
  Activity,
  getActivityList,
  createActivity,
  updateActivity,
  deleteActivity,
  approveActivity,
  rejectActivity,
  startActivity,
  endActivity,
  cancelActivity
} from '../api/activity';
import { getClubList } from '../api/club';

const tableData = ref<Activity[]>([]);
const pageTotal = ref(0);
const clubOptions = ref<{ id: number; name: string }[]>([]);

const query = reactive<{ page: number; size: number; title: string; clubId: number | undefined; status: ActivityStatus | undefined }>({
  title: '',
  clubId: undefined,
  status: undefined,
  page: 1,
  size: 10
});

const dialogVisible = ref(false);
const isEdit = ref(false);
const form = reactive({
  id: 0,
  title: '',
  description: '',
  location: '',
  activityTime: '',
  coverImage: '',
  maxParticipants: 50,
  clubId: 0
});

const formTitle = computed(() => (isEdit.value ? '编辑活动' : '新建活动'));

const fetchActivities = async () => {
  try {
    const res = await getActivityList({
      page: query.page,
      size: query.size,
      title: query.title || undefined,
      clubId: query.clubId,
      status: query.status
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
  } catch (e) {
    console.error(e);
  }
};

const handleSearch = () => {
  query.page = 1;
  fetchActivities();
};

const handleReset = () => {
  query.title = '';
  query.clubId = undefined;
  query.status = undefined;
  query.page = 1;
  fetchActivities();
};

const handlePageChange = (page: number) => {
  query.page = page;
  fetchActivities();
};

const handleAdd = () => {
  isEdit.value = false;
  form.id = 0;
  form.title = '';
  form.description = '';
  form.location = '';
  form.activityTime = '';
  form.coverImage = '';
  form.maxParticipants = 50;
  form.clubId = clubOptions.value[0]?.id || 0;
  dialogVisible.value = true;
};

const handleEdit = (row: Activity) => {
  isEdit.value = true;
  form.id = row.id;
  form.title = row.title;
  form.description = row.description;
  form.location = row.location;
  form.activityTime = row.activityTime;
  form.maxParticipants = row.maxParticipants;
  form.clubId = row.club?.id || 0;
  dialogVisible.value = true;
};

const handleDelete = (row: Activity) => {
  ElMessageBox.confirm(`确定要删除活动"${row.title}"吗？`, '提示', { type: 'warning' })
    .then(() => {
      deleteActivity(row.id).then(() => {
        ElMessage.success('删除成功');
        fetchActivities();
      });
    })
    .catch(() => {});
};

const handleApprove = (row: Activity) => {
  approveActivity(row.id).then(() => {
    ElMessage.success('审核通过');
    fetchActivities();
  });
};

const handleReject = (row: Activity) => {
  ElMessageBox.prompt('请输入审核不通过的原因', '审核不通过', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: '不符合要求'
  })
    .then(({ value }) => {
      rejectActivity(row.id, value).then(() => {
        ElMessage.success('已标记为审核不通过');
        fetchActivities();
      });
    })
    .catch(() => {});
};

const handleStart = (row: Activity) => {
  startActivity(row.id).then(() => {
    ElMessage.success('活动已开始');
    fetchActivities();
  });
};

const handleEnd = (row: Activity) => {
  endActivity(row.id).then(() => {
    ElMessage.success('活动已结束');
    fetchActivities();
  });
};

const handleCancel = (row: Activity) => {
  ElMessageBox.prompt('请输入取消原因', '取消活动', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: '活动取消'
  })
    .then(({ value }) => {
      cancelActivity(row.id, value).then(() => {
        ElMessage.success('活动已取消');
        fetchActivities();
      });
    })
    .catch(() => {});
};

const saveForm = () => {
  if (!form.title.trim()) {
    ElMessage.warning('请填写活动标题');
    return;
  }
  if (!form.clubId) {
    ElMessage.warning('请选择所属社团');
    return;
  }
  const payload = {
    title: form.title,
    description: form.description,
    location: form.location,
    activityTime: form.activityTime,
    coverImage: form.coverImage,
    maxParticipants: form.maxParticipants,
    clubId: form.clubId
  };
  const req = isEdit.value ? updateActivity(form.id, payload) : createActivity(payload);
  req.then(() => {
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
    dialogVisible.value = false;
    fetchActivities();
  });
};

onMounted(() => {
  fetchActivities();
  fetchClubs();
});
</script>

<style scoped lang="scss">
.activity-wrap {
  position: relative;
  padding: 10px 20px;
  height: 100%;
}
</style>
