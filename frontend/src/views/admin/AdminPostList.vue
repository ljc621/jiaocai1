<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const posts = ref([])

const fetchPosts = async () => {
  try {
    const res = await axios.get('/api/posts')
    if (res.data.code === 200) {
      posts.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    '确定要删除这条帖子吗?',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await axios.delete(`/api/posts/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchPosts()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

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

onMounted(fetchPosts)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">社区管理</h2>
      <div class="header-actions">
        <span class="admin-info">管理员: admin</span>
        <div class="status-dot"></div>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="posts" style="width: 100%">
        <el-table-column label="标题" min-width="200">
          <template #default="{ row }">
            <div class="post-title">{{ row.title }}</div>
            <div class="post-content">{{ row.content }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="作者ID" width="150">
          <template #default="{ row }">
            <div class="author-name">{{ row.userId }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            <el-tag effect="plain" :type="getTypeTagType(row.type)" size="small">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="互动" width="120">
          <template #default="{ row }">
            <div class="interaction-item">点赞: {{ row.likes || 0 }}</div>
            <div class="interaction-item">浏览: {{ row.views || 0 }}</div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)" />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-info {
  font-size: 14px;
  color: #606266;
}

.status-dot {
  width: 20px;
  height: 20px;
  background-color: #2563eb;
  border-radius: 50%;
}

.table-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  flex: 1;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.post-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.post-content {
  font-size: 12px;
  color: #909399;
}

.author-name {
  font-size: 14px;
  color: #303133;
}

.author-school {
  font-size: 12px;
  color: #909399;
}

.interaction-item {
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
}
</style>