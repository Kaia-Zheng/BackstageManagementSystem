<template>
  <div class="club-wrap">
    <div class="handle-box">
      <el-input v-model="query.name" placeholder="社团名称" class="handle-input mr10" clearable style="width: 180px"></el-input>
      <el-select v-model="query.category" placeholder="类别" class="handle-select mr10" clearable style="width: 130px">
        <el-option v-for="item in ClubCategoryList" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
      <el-select v-model="query.state" placeholder="状态" class="handle-select mr10" clearable style="width: 120px">
        <el-option v-for="item in ClubStateList" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
      <el-button type="primary" @click="handleSearch" plain>搜索</el-button>
      <el-button @click="handleReset" plain>重置</el-button>
      <el-button type="primary" @click="handleAdd" v-action:club:create style="float: right">新建社团</el-button>
    </div>

    <el-table :data="tableData" border class="table" header-cell-class-name="table-header">
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="name" label="社团名称" min-width="140"></el-table-column>
      <el-table-column prop="description" label="简介" min-width="200" show-overflow-tooltip></el-table-column>
      <el-table-column label="类别" width="100" align="center">
        <template #default="{ row }">{{ ClubCategoryLabel(row.category) }}</template>
      </el-table-column>
      <el-table-column label="成立日期" width="120">
        <template #default="{ row }">{{ row.foundedDate || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.state === 'ACTIVE' ? 'success' : 'danger'">{{ ClubStateLabel(row.state) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="负责人" width="110">
        <template #default="{ row }">{{ row.owner?.username || '-' }}</template>
      </el-table-column>
      <el-table-column prop="memberCount" label="成员数" width="80" align="center"></el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)" v-action:club:update>编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)" v-action:club:delete>删除</el-button>
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
    <el-dialog :title="formTitle" v-model="dialogVisible" width="40%">
      <el-form :model="form" label-width="90px">
        <el-form-item label="社团名称" required>
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3"></el-input>
        </el-form-item>
        <el-form-item label="类别" required>
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="item in ClubCategoryList" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="成立日期">
          <el-date-picker v-model="form.foundedDate" type="date" value-format="YYYY-MM-DD" style="width: 100%"></el-date-picker>
        </el-form-item>
        <el-form-item label="状态" required>
          <el-select v-model="form.state" style="width: 100%">
            <el-option v-for="item in ClubStateList" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select
            v-model="form.ownerId"
            placeholder="请选择用户"
            filterable
            style="width: 100%"
          >
            <el-option v-for="user in userOptions" :key="user.id" :label="user.username" :value="user.id"></el-option>
          </el-select>
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
  ClubCategory,
  ClubCategoryList,
  ClubCategoryLabel,
  ClubState,
  ClubStateList,
  ClubStateLabel,
  Club,
  getClubList,
  createClub,
  updateClub,
  deleteClub
} from '../api/club';
import { getUserList } from '../api/user';

const tableData = ref<Club[]>([]);
const pageTotal = ref(0);

const query = reactive<{ page: number; size: number; name: string; category: ClubCategory | undefined; state: ClubState | undefined }>({
  name: '',
  category: undefined,
  state: undefined,
  page: 1,
  size: 10
});

const userOptions = ref<{ id: number; username: string }[]>([]);

const dialogVisible = ref(false);
const isEdit = ref(false);
const form = reactive<{
  id: number;
  name: string;
  description: string;
  category: ClubCategory;
  foundedDate: string;
  state: ClubState;
  avatar: string;
  ownerId: number;
}>({
  id: 0,
  name: '',
  description: '',
  category: 'OTHER',
  foundedDate: '',
  state: 'ACTIVE',
  avatar: '',
  ownerId: 0
});

const formTitle = computed(() => (isEdit.value ? '编辑社团' : '新建社团'));

const fetchClubs = async () => {
  try {
    const res = await getClubList({
      page: query.page,
      size: query.size,
      name: query.name || undefined,
      category: query.category,
      state: query.state
    });
    tableData.value = res.data.list || [];
    pageTotal.value = res.data.total || 0;
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
  fetchClubs();
};

const handleReset = () => {
  query.name = '';
  query.category = undefined;
  query.state = undefined;
  query.page = 1;
  fetchClubs();
};

const handlePageChange = (page: number) => {
  query.page = page;
  fetchClubs();
};

const handleAdd = () => {
  isEdit.value = false;
  form.id = 0;
  form.name = '';
  form.description = '';
  form.category = 'OTHER';
  form.foundedDate = '';
  form.state = 'ACTIVE';
  form.ownerId = 0;
  dialogVisible.value = true;
};

const handleEdit = (row: Club) => {
  isEdit.value = true;
  form.id = row.id;
  form.name = row.name;
  form.description = row.description;
  form.category = row.category;
  form.foundedDate = row.foundedDate;
  form.state = row.state;
  form.ownerId = row.owner?.id || 0;
  dialogVisible.value = true;
};

const handleDelete = (row: Club) => {
  ElMessageBox.confirm(`确定要删除社团"${row.name}"吗？`, '提示', {
    type: 'warning'
  })
    .then(() => {
      deleteClub(row.id).then(() => {
        ElMessage.success('删除成功');
        fetchClubs();
      });
    })
    .catch(() => {});
};

const saveForm = () => {
  if (!form.name.trim()) {
    ElMessage.warning('请填写社团名称');
    return;
  }
  const payload = {
    name: form.name,
    description: form.description,
    category: form.category,
    foundedDate: form.foundedDate,
    state: form.state,
    avatar: form.avatar,
    ownerId: form.ownerId
  };
  const req = isEdit.value ? updateClub(form.id, payload) : createClub(payload);
  req.then(() => {
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功');
    dialogVisible.value = false;
    fetchClubs();
  });
};

onMounted(() => {
  fetchClubs();
  fetchUsers();
});
</script>

<style scoped lang="scss">
.club-wrap {
  position: relative;
  padding: 10px 20px;
  height: 100%;
}
</style>
