<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { Plus, View, ChatDotSquare, Trophy, Filter, ArrowDown } from '@element-plus/icons-vue'
import type { Post } from '../types'

const router = useRouter()
const posts = ref<Post[]>([])
const loading = ref(false)
const activeType = ref('')
const sortOrder = ref('newest')
const sortLabel = computed(() => {
  const map: Record<string, string> = {
    newest: '最新发布',
    replies: '最多回复',
    views: '最多浏览'
  }
  return map[sortOrder.value]
})

const fetchPosts = async () => {
  loading.value = true
  try {
    const params: any = {}
    if (activeType.value) {
      params.type = activeType.value
    }
    params.sort = sortOrder.value
    const res = await axios.get('/api/posts', { params })
    posts.value = res.data.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSortChange = (command: string) => {
  sortOrder.value = command
  fetchPosts()
}

onMounted(fetchPosts)

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    question: '提问',
    transaction: '交易',
    review: '测评',
    discussion: '讨论'
  }
  return map[type] || type
}

const getTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    question: 'warning',
    transaction: 'success',
    review: 'danger',
    discussion: ''
  }
  return map[type] || ''
}
</script>

<template>
  <div class="community-container">
    <div class="page-header">
      <div>
        <h2>社区讨论</h2>
        <p class="subtitle">分享学习心得，交流教材使用体验</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="router.push('/posts/create')">发布帖子</el-button>
    </div>

    <div class="content-layout">
      <div class="main-content">
        <div class="filter-bar">
          <div class="tabs">
            <el-button 
              :type="activeType === '' ? 'primary' : 'default'" 
              round 
              @click="activeType = ''; fetchPosts()"
            >全部</el-button>
            <el-button 
              :type="activeType === 'discussion' ? 'primary' : 'default'" 
              round 
              @click="activeType = 'discussion'; fetchPosts()"
            >讨论</el-button>
            <el-button 
              :type="activeType === 'question' ? 'primary' : 'default'" 
              round 
              @click="activeType = 'question'; fetchPosts()"
            >提问</el-button>
            <el-button 
              :type="activeType === 'transaction' ? 'primary' : 'default'" 
              round 
              @click="activeType = 'transaction'; fetchPosts()"
            >交易</el-button>
            <el-button 
              :type="activeType === 'review' ? 'primary' : 'default'" 
              round 
              @click="activeType = 'review'; fetchPosts()"
            >测评</el-button>
          </div>
        </div>

        <div v-loading="loading" class="post-list">
          <div v-if="posts.length === 0" class="empty-state">
            <el-empty description="暂无帖子" />
          </div>
          <el-card v-else v-for="post in posts" :key="post.id" class="post-card" shadow="hover" @click="router.push(`/posts/${post.id}`)">
            <div class="post-content-wrapper">
              <div class="post-main">
                <div class="post-header-line">
                  <el-tag :type="getTypeTagType(post.type)" size="small" effect="light" class="type-tag">
                    {{ getTypeName(post.type) }}
                  </el-tag>
                  <h3 class="post-title">{{ post.title }}</h3>
                </div>
                <p class="post-excerpt">{{ post.content.substring(0, 100) }}...</p>
                <div class="post-footer">
                  <div class="user-info">
                    <el-avatar :size="24" :src="post.user?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                    <span class="username">{{ post.user?.name || '匿名用户' }}</span>
                    <span class="separator">•</span>
                    <span class="time">{{ post.createdAt }}</span>
                  </div>
                </div>
              </div>
              
              <div class="post-stats">
                <div class="stat-item">
                  <el-icon><Trophy /></el-icon>
                  <span>{{ post.likes }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><ChatDotSquare /></el-icon>
                  <span>{{ post.commentsCount || 0 }}</span>
                </div>
                <div class="stat-item">
                  <el-icon><View /></el-icon>
                  <span>{{ post.views }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <div class="side-content">
        <div class="sort-filter">
           <el-dropdown trigger="click" @command="handleSortChange">
            <span class="el-dropdown-link">
              <el-icon><Filter /></el-icon> {{ sortLabel }} <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="newest">最新发布</el-dropdown-item>
                <el-dropdown-item command="replies">最多回复</el-dropdown-item>
                <el-dropdown-item command="views">最多浏览</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.community-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.subtitle {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

.content-layout {
  display: flex;
  gap: 20px;
}

.main-content {
  flex: 1;
}

.side-content {
  width: 200px;
}

.filter-bar {
  margin-bottom: 20px;
}

.tabs {
  display: flex;
  gap: 10px;
}

.post-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.post-card:hover {
  transform: translateY(-2px);
}

.post-content-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.post-main {
  flex: 1;
  padding-right: 20px;
}

.post-header-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.type-tag {
  font-weight: normal;
}

.post-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.post-excerpt {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 8px 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
}

.separator {
  margin: 0 4px;
}

.post-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-left: 20px;
  border-left: 1px solid #EBEEF5;
  min-width: 60px;
  justify-content: center;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
}

.sort-filter {
  display: flex;
  justify-content: flex-end;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #606266;
}
</style>
