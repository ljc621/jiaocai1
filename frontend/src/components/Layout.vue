<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Reading, Search, ShoppingCart, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const searchQuery = ref('')
const user = ref<any>(null)
const unreadCount = ref(0)
let pollTimer: number | null = null

const refreshUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
    fetchUnreadCount()
  } else {
    user.value = null
    unreadCount.value = 0
  }
}

onMounted(() => {
  refreshUser()
  // Poll every 30 seconds
  pollTimer = window.setInterval(fetchUnreadCount, 30000)
  window.addEventListener('message-update', fetchUnreadCount)
  window.addEventListener('user-update', refreshUser)
})

import { onUnmounted } from 'vue'

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }
  window.removeEventListener('message-update', fetchUnreadCount)
  window.removeEventListener('user-update', refreshUser)
})

const fetchUnreadCount = async () => {
  if (!user.value) return
  try {
    const res = await axios.get('/api/messages/unread-count', {
      params: { userId: user.value.id }
    })
    if (res.data.code === 200) {
      unreadCount.value = res.data.data
    }
  } catch (e) {
    console.error('Failed to fetch unread count')
  }
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/textbooks',
      query: { keyword: searchQuery.value }
    })
  }
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('user')
    user.value = null
    router.push('/login')
    ElMessage.success('已退出登录')
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'messages') {
    router.push('/messages')
  } else if (command === 'admin') {
    router.push('/admin')
  }
}
</script>

<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <el-icon class="logo-icon"><Reading /></el-icon>
          <span>教材循环平台</span>
        </div>
        
        <div class="header-search">
          <el-input
            v-model="searchQuery"
            placeholder="搜索教材、作者、ISBN..."
            :prefix-icon="Search"
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="nav-menu">
          <router-link to="/textbooks" active-class="active">购买/租赁</router-link>
          <router-link to="/textbooks/create" active-class="active">出售/捐赠</router-link>
          <router-link to="/posts" active-class="active">社区</router-link>
        </div>

        <div class="user-actions">
          <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="message-badge">
            <el-button :icon="Message" circle class="action-btn" @click="$router.push('/messages')" />
          </el-badge>
          <el-button :icon="ShoppingCart" circle class="action-btn" @click="$router.push('/cart')" />
          
          <template v-if="user">
            <el-dropdown @command="handleCommand" trigger="click">
              <span class="el-dropdown-link user-profile">
                <el-avatar :size="32" :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                <span class="username">{{ user.name }}</span>
                <el-tag v-if="user.role === 'admin'" size="small" type="primary" effect="light" class="role-badge">管理员</el-tag>
              </span>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="messages">我的消息</el-dropdown-item>
                  <el-dropdown-item command="admin" v-if="user.role === 'admin'">后台管理</el-dropdown-item>
                  <el-dropdown-item command="logout" divided class="logout-item">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" link @click="$router.push('/login')">登录</el-button>
            <el-divider direction="vertical" />
            <el-button type="primary" link @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <router-view />
    </el-main>

    <el-footer class="footer">
      <div class="footer-content">
        <p>&copy; 2026 教材循环交易平台. All rights reserved.</p>
      </div>
    </el-footer>
  </el-container>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  padding: 0;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.logo-icon {
  font-size: 24px;
}

.header-search {
  flex: 1;
  max-width: 400px;
  margin: 0 40px;
}

.search-input :deep(.el-input__wrapper) {
  background-color: #f5f7fa;
  box-shadow: none;
  border-radius: 20px;
}

.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  background-color: white;
  box-shadow: 0 0 0 1px #409eff inset;
}

.nav-menu {
  display: flex;
  gap: 30px;
  margin-right: 40px;
}

.nav-menu a {
  text-decoration: none;
  color: #606266;
  font-size: 15px;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-menu a:hover, .nav-menu a.active {
  color: #409eff;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.action-btn {
  border: none;
  background: transparent;
  font-size: 20px;
  color: #606266;
}

.action-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-profile:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-badge {
  background-color: #ecf5ff;
  color: #409eff;
  border-color: #d9ecff;
}

.main-content {
  flex: 1;
  padding: 0;
  background-color: #fff;
  overflow-x: hidden;
}

.footer {
  background-color: #1a1e29;
  color: #909399;
  padding: 40px 0;
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  font-size: 14px;
}

.logout-item {
  color: #f56c6c;
}
</style>
