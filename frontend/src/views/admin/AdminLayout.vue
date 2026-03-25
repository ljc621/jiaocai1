<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  DataAnalysis, 
  User, 
  Reading, 
  List, 
  ChatDotRound, 
  Setting,
  Search,
  Bell,
  ShoppingCart,
  Avatar,
  ArrowDown,
  Star
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const user = ref<any>(null)

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
  }
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm(
      '确定要退出登录吗?',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).then(() => {
      localStorage.removeItem('user')
      router.push('/login')
      ElMessage.success('已退出登录')
    })
  } else if (command === 'home') {
    router.push('/')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

const menuItems = [
  { index: '/admin', icon: DataAnalysis, title: '数据概览' },
  { index: '/admin/users', icon: User, title: '用户管理' },
  { index: '/admin/textbooks', icon: Reading, title: '教材管理' },
  { index: '/admin/orders', icon: List, title: '订单管理' },
  { index: '/admin/community', icon: ChatDotRound, title: '社区管理' },
  { index: '/admin/reviews', icon: Star, title: '评价管理' },
]

const activeMenu = ref(route.path)
</script>

<template>
  <div class="admin-layout">
    <div class="sidebar">
      <div class="sidebar-header">
        <el-icon><Setting /></el-icon>
        <span>后台管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        background-color="#1a2332"
        text-color="#fff"
        active-text-color="#fff"
        router
      >
        <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-footer">
        <div class="logo-circle">N</div>
      </div>
    </div>

    <div class="main-content">
      <div class="header">
        <div class="brand" @click="router.push('/')">
          <el-icon class="book-icon"><Reading /></el-icon>
          <span>教材循环平台</span>
        </div>
        
        <div class="search-bar">
          <el-input
            placeholder="搜索教材、作者、ISBN..."
            prefix-icon="Search"
          />
        </div>

        <div class="header-right">
          <span class="nav-link" @click="router.push('/textbooks')">购买/租赁</span>
          <span class="nav-link" @click="router.push('/textbooks/create')">出售/捐赠</span>
          <span class="nav-link" @click="router.push('/posts')">社区</span>
          
          <div class="icon-btn" @click="router.push('/posts')">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="icon-btn" @click="router.push('/cart')">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-profile">
              <el-avatar :size="32" :src="user?.avatar" :icon="User" class="avatar" />
              <span>{{ user?.name || '系统管理员' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">返回前台</el-dropdown-item>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="content-wrapper">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 240px;
  background-color: #1a2332;
  color: white;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 18px;
  font-weight: bold;
  gap: 10px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 8px 10px;
  border-radius: 4px;
}

:deep(.el-menu-item.is-active) {
  background-color: #2563eb !important;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255,255,255,0.1) !important;
}

.sidebar-footer {
  padding: 20px;
}
.logo-circle {
  width: 30px;
  height: 30px;
  background-color: #000;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.header {
  height: 60px;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2563eb;
  font-weight: bold;
  font-size: 16px;
}

.search-bar {
  width: 400px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nav-link {
  font-size: 14px;
  color: #606266;
  cursor: pointer;
}
.nav-link:hover {
  color: #409eff;
}

.icon-btn {
  font-size: 20px;
  color: #606266;
  cursor: pointer;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #f5f7fa;
  padding: 4px 12px;
  border-radius: 20px;
  cursor: pointer;
}
.avatar {
  background-color: #e6e8eb;
  color: #909399;
}

.content-wrapper {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
}
</style>