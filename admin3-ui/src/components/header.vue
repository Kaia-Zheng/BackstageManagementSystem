<template>
  <div class="header">
    <!-- 折叠按钮 -->
    <div class="collapse-btn" @click="collapseChage">
      <el-icon v-if="sidebar.collapse">
        <Expand/>
      </el-icon>
      <el-icon v-else>
        <Fold/>
      </el-icon>
    </div>
    <div class="logo">天津仁爱学院社团管理系统</div>
    <div class="header-right">
      <div class="header-user-con">
        <!-- 通知铃铛 -->
        <el-popover
          placement="bottom"
          :width="360"
          trigger="click"
          v-model:visible="popoverVisible"
          @show="fetchNotifications"
        >
          <template #reference>
            <div class="btn-bell">
              <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
                <el-icon :size="20" style="color: #fff;"><Bell /></el-icon>
              </el-badge>
            </div>
          </template>
          <div class="notification-panel">
            <div class="notification-header">
              <span class="notification-title">通知</span>
              <el-button type="primary" link size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">全部标记为已读</el-button>
            </div>
            <div class="notification-list" v-if="notifications.length > 0">
              <div
                v-for="item in notifications"
                :key="item.id"
                class="notification-item"
                :class="{ unread: !item.read }"
              >
                <div class="notification-item-title">{{ item.title }}</div>
                <div class="notification-item-content">{{ item.content }}</div>
                <div class="notification-item-time">{{ item.createdTime }}</div>
              </div>
            </div>
            <el-empty v-else description="暂无通知" :image-size="60" />
          </div>
        </el-popover>
        <!-- 用户头像 -->
        <el-avatar class="user-avator" :size="30" :src="avatar"/>
        <!-- 用户名下拉菜单 -->
        <el-dropdown class="user-name" trigger="click" @command="handleCommand">
					<span class="el-dropdown-link">
						{{ username }}
						<el-icon class="el-icon--right">
							<arrow-down/>
						</el-icon>
					</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="user">个人中心</el-dropdown-item>
              <el-dropdown-item divided command="loginout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { Bell } from '@element-plus/icons-vue';
import { useSidebarStore } from '../store/sidebar';
import { useRouter } from 'vue-router';
import { logout } from "../api/login";
import { useBasicStore } from "../store/basic";
import { getMyNotifications, getUnreadCount, markAllAsRead, type Notification } from '../api/notification';
import { ElMessage } from 'element-plus';

const userinfoStore = useBasicStore();
const userinfo = userinfoStore.userinfo;
const username: string | null = userinfo.username;
const avatar: string | null = userinfo.avatar;

const sidebar = useSidebarStore();
// 侧边栏折叠
const collapseChage = () => {
  sidebar.handleCollapse();
};

// 通知相关
const notifications = ref<Notification[]>([]);
const unreadCount = ref(0);
const popoverVisible = ref(false);
let timer: ReturnType<typeof setInterval> | null = null;

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount();
    unreadCount.value = res.data || 0;
  } catch (e) {
    console.error(e);
  }
};

const fetchNotifications = async () => {
  try {
    const res = await getMyNotifications({ page: 1, size: 10 });
    notifications.value = res.data?.list || [];
  } catch (e) {
    console.error(e);
  }
};

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead();
    ElMessage.success('已全部标记为已读');
    unreadCount.value = 0;
    notifications.value = notifications.value.map(n => ({ ...n, read: true }));
  } catch (e) {
    console.error(e);
  }
};

onMounted(() => {
  if (document.body.clientWidth < 1500) {
    collapseChage();
  }
  fetchUnreadCount();
  timer = setInterval(fetchUnreadCount, 30000);
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
});

// 用户名下拉菜单选择事件
const router = useRouter();
const handleCommand = (command: string) => {
  if (command == 'loginout') {
    logout().then(() => {
      localStorage.removeItem('token');
      router.push('/login');
    })
  } else if (command == 'user') {
    router.push('/user');
  }
};
</script>
<style scoped>
.header {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  height: 70px;
  font-size: 22px;
  color: #fff;
}

.collapse-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  float: left;
  padding: 0 21px;
  cursor: pointer;
}

.header .logo {
  float: left;
  width: 250px;
  line-height: 70px;
}

.header-right {
  float: right;
  padding-right: 50px;
}

.header-user-con {
  display: flex;
  height: 70px;
  align-items: center;
}

.btn-fullscreen {
  transform: rotate(45deg);
  margin-right: 5px;
  font-size: 24px;
}

.btn-bell,
.btn-fullscreen {
  position: relative;
  width: 30px;
  height: 30px;
  text-align: center;
  border-radius: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.user-name {
  margin-left: 10px;
}

.user-avator {
  margin-left: 20px;
}

.el-dropdown-link {
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.el-dropdown-menu__item {
  text-align: center;
}

.notification-panel {
  max-height: 400px;
  overflow-y: auto;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 10px;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
}

.notification-list {
  max-height: 320px;
  overflow-y: auto;
}

.notification-item {
  padding: 10px 0;
  border-bottom: 1px solid #f2f3f5;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item.unread {
  background-color: #f0f6ff;
  margin: 0 -12px;
  padding: 10px 12px;
  border-radius: 4px;
}

.notification-item-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.notification-item-content {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.5;
}

.notification-item-time {
  font-size: 11px;
  color: #909399;
}
</style>
