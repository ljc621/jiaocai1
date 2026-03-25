<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Star, ChatDotSquare, View, Trophy } from '@element-plus/icons-vue'
import type { Post, Comment, User } from '../types'

const route = useRoute()
const router = useRouter()
const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const loading = ref(false)
const newComment = ref('')
const submitting = ref(false)
const author = ref<User | null>(null)
const isLiked = ref(false)

const postId = route.params.id as string

const fetchPost = async () => {
  loading.value = true
  try {
    const res = await axios.get(`/api/posts/${postId}`)
    if (res.data.code === 200) {
      post.value = res.data.data
      if (post.value) {
        // Fetch author info
        try {
            const userRes = await axios.get(`/api/users/${post.value.userId}`)
            if (userRes.data.code === 200) author.value = userRes.data.data
        } catch(e) {}
        fetchComments()
      }
    } else {
      ElMessage.error('帖子不存在')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await axios.get('/api/comments', { params: { postId } })
    if (res.data.code === 200) {
      comments.value = res.data.data
      // We could also fetch user info for each comment, but let's skip for now or do it if needed
      // Ideally backend should return user info joined
    }
  } catch (error) {
    console.error(error)
  }
}

const handleComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  const user = JSON.parse(userStr)

  submitting.value = true
  try {
    const res = await axios.post('/api/comments', {
      postId: Number(postId),
      userId: user.id,
      content: newComment.value
    })
    if (res.data.code === 200) {
      ElMessage.success('评论成功')
      newComment.value = ''
      fetchComments()
      // Update local comment count
      if (post.value) {
        post.value.commentsCount = (post.value.commentsCount || 0) + 1
      }
    } else {
      ElMessage.error('评论失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}

const handleLike = async () => {
  if (!post.value) return
  
  try {
    if (isLiked.value) {
      const res = await axios.post(`/api/posts/${post.value.id}/unlike`)
      if (res.data.code === 200) {
        post.value.likes = (post.value.likes || 0) - 1
        isLiked.value = false
        ElMessage.success('已取消点赞')
      }
    } else {
      const res = await axios.post(`/api/posts/${post.value.id}/like`)
      if (res.data.code === 200) {
        post.value.likes = (post.value.likes || 0) + 1
        isLiked.value = true
        ElMessage.success('点赞成功')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(fetchPost)

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    question: '提问',
    transaction: '交易',
    review: '测评',
    discussion: '讨论'
  }
  return map[type] || type
}
</script>

<template>
  <div class="post-detail-container" v-loading="loading">
    <el-page-header @back="router.back()" content="帖子详情" class="mb-4" />

    <div v-if="post">
      <el-card class="post-card">
        <div class="post-header">
          <h1>{{ post.title }}</h1>
          <div class="meta">
             <el-tag size="small">{{ getTypeName(post.type) }}</el-tag>
             <span class="author" v-if="author">作者: {{ author.name }}</span>
             <span class="time">{{ post.createdAt }}</span>
             <span class="views">浏览: {{ post.views }}</span>
          </div>
        </div>
        <div class="post-content">
          {{ post.content }}
        </div>
        
        <div class="post-actions">
           <el-button 
             :type="isLiked ? 'primary' : 'default'" 
             :icon="Trophy" 
             circle 
             size="large"
             @click="handleLike"
           />
           <span class="likes-count">{{ post.likes || 0 }} 人点赞</span>
        </div>
      </el-card>

      <div class="comments-section">
        <h3>评论 ({{ comments.length }})</h3>
        <div class="comment-input">
          <el-input v-model="newComment" type="textarea" :rows="3" placeholder="写下你的评论..." />
          <el-button type="primary" class="mt-2" :loading="submitting" @click="handleComment">发表评论</el-button>
        </div>
        
        <div class="comment-list">
          <el-card v-for="comment in comments" :key="comment.id" class="comment-card" shadow="never">
            <div class="comment-meta">
              <span class="user-id">用户ID: {{ comment.userId }}</span>
              <span class="time">{{ comment.createdAt }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.post-detail-container {
  max-width: 800px;
  margin: 0 auto;
}
.mb-4 {
  margin-bottom: 20px;
}
.post-card {
  margin-bottom: 30px;
}
.post-header h1 {
  margin-top: 0;
}
.meta {
  display: flex;
  gap: 15px;
  color: #999;
  font-size: 14px;
  align-items: center;
  margin-bottom: 20px;
}
.post-content {
  font-size: 16px;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: 30px;
}
.post-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}
.likes-count {
  font-size: 14px;
  color: #606266;
}
.comments-section {
  margin-top: 30px;
}
.comment-input {
  margin-bottom: 30px;
}
.mt-2 {
  margin-top: 10px;
}
.comment-card {
  margin-bottom: 15px;
}
.comment-meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 12px;
  margin-bottom: 5px;
}
</style>
