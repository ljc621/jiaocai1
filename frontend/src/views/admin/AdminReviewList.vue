<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Delete, Star, StarFilled } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const reviews = ref([])

const fetchReviews = async () => {
  try {
    const res = await axios.get('/api/reviews/admin/list')
    if (res.data.code === 200) {
      reviews.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取评价列表失败')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该评价吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/reviews/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchReviews()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(fetchReviews)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">评价管理</h2>
      <div class="header-actions">
        <span class="admin-info">管理员: admin</span>
        <div class="status-dot"></div>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="reviews" style="width: 100%">
        <el-table-column prop="id" label="评价ID" width="120" show-overflow-tooltip />
        <el-table-column label="评分" width="150">
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
        <el-table-column label="用户">
            <template #default="{ row }">
                <div class="user-info">
                    <el-avatar :size="24" :src="row.userAvatar">{{ row.userName?.charAt(0) }}</el-avatar>
                    <span>{{ row.userName }}</span>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间">
             <template #default="{ row }">
                 {{ new Date(row.createdAt).toLocaleString() }}
             </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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

.user-info {
    display: flex;
    align-items: center;
    gap: 8px;
}
</style>
