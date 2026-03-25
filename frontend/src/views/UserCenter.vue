<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  User, 
  List, 
  Reading, 
  Star, 
  Wallet, 
  Medal,
  Location, 
  Setting 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const user = ref<any>({})

const activeMenu = ref(route.path)

const refreshUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
  }
}

onMounted(() => {
  refreshUser()
  window.addEventListener('user-update', refreshUser)
})

import { onUnmounted } from 'vue'
onUnmounted(() => {
  window.removeEventListener('user-update', refreshUser)
})
</script>

<template>
  <div class="user-center-layout">
    <div class="sidebar">
      <div class="user-brief">
        <el-avatar :size="64" :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        <h3 class="username">{{ user.name }}</h3>
        <p class="role-tag">{{ user.role === 'admin' ? '系统管理员' : '普通用户' }}</p>
        <el-tag type="success" size="small" round v-if="user.verified">已实名认证</el-tag>
        <el-button link type="primary" size="small" class="edit-link" @click="router.push('/profile/info')">编辑资料</el-button>
      </div>
      
      <el-menu
        :default-active="route.path"
        class="side-menu"
        router
      >
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人资料</span> <!-- Using "Personal Info" as label for Dashboard as per screenshot top item? Or maybe "Overview"? Screenshot says "Personal Info" selected. -->
        </el-menu-item>
        <el-menu-item index="/profile/orders">
          <el-icon><List /></el-icon>
          <span>我的订单</span>
        </el-menu-item>
        <el-menu-item index="/profile/published">
          <el-icon><Reading /></el-icon>
          <span>我的发布</span>
        </el-menu-item>
        <el-menu-item index="/profile/favorites">
          <el-icon><Star /></el-icon>
          <span>我的收藏</span>
        </el-menu-item>
        <el-menu-item index="/profile/wallet">
          <el-icon><Wallet /></el-icon>
          <span>我的账单</span>
        </el-menu-item>
        <el-menu-item index="/profile/points">
          <el-icon><Medal /></el-icon>
          <span>我的积分</span>
        </el-menu-item>
        <el-menu-item index="/profile/addresses">
          <el-icon><Location /></el-icon>
          <span>收货地址</span>
        </el-menu-item>
        <el-menu-item index="/profile/settings">
          <el-icon><Setting /></el-icon>
          <span>账号设置</span>
        </el-menu-item>
      </el-menu>
    </div>
    
    <div class="main-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<style scoped>
.user-center-layout {
  display: flex;
  max-width: 1200px;
  margin: 30px auto;
  padding: 0 20px;
  gap: 20px;
}

.sidebar {
  width: 240px;
  background: white;
  border-radius: 12px;
  padding: 24px 0;
  height: fit-content;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
}

.main-content {
  flex: 1;
  min-width: 0; /* Prevent overflow */
}

.user-brief {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.username {
  margin: 10px 0 5px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.role-tag {
  color: #909399;
  font-size: 12px;
  margin: 0 0 8px;
}

.edit-link {
  margin-top: 8px;
}

.side-menu {
  border-right: none;
}

.side-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 0;
  border-radius: 0;
  transition: all 0.3s;
  border-right: 3px solid transparent;
}

.side-menu :deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
  font-weight: 500;
}

.side-menu :deep(.el-menu-item:hover) {
  background-color: #f0f7ff;
  color: #409eff;
}

.main-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  padding: 30px;
  min-height: 600px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}
</style>
