<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, Delete, VideoPause, VideoPlay, View } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const activeTab = ref('all')
const loading = ref(false)
const textbooks = ref<any[]>([])

const fetchMyTextbooks = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return

  const user = JSON.parse(userStr)
  loading.value = true
  try {
    const res = await axios.get('/api/textbooks', {
        params: { sellerId: user.id }
    })
    if (res.data.code === 200) {
      textbooks.value = res.data.data
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取发布列表失败')
  } finally {
    loading.value = false
  }
}

const filteredTextbooks = computed(() => {
    if (activeTab.value === 'all') return textbooks.value
    if (activeTab.value === 'selling') return textbooks.value.filter((t: any) => t.status === 'available')
    if (activeTab.value === 'sold') return textbooks.value.filter((t: any) => t.status === 'sold' || t.status === 'rented')
    if (activeTab.value === 'off_shelf') return textbooks.value.filter((t: any) => t.status === 'off_shelf')
    return textbooks.value
})

const handleEdit = (id: string) => {
  router.push(`/textbooks/edit/${id}`)
}

const handleView = (id: string) => {
  router.push(`/textbooks/${id}`)
}

const handleToggleStatus = async (item: any) => {
  const newStatus = item.status === 'available' ? 'off_shelf' : 'available'
  const actionText = newStatus === 'available' ? '上架' : '下架'
  
  try {
    const res = await axios.put('/api/textbooks', {
        id: item.id,
        status: newStatus
    })
    
    if (res.data.code === 200) {
        ElMessage.success(`${actionText}成功`)
        item.status = newStatus
    } else {
        ElMessage.error(`${actionText}失败`)
    }
  } catch (e) {
    ElMessage.error(`${actionText}失败`)
  }
}

const handleDelete = (id: string) => {
  ElMessageBox.confirm(
    '确定要删除该教材吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
        const res = await axios.delete(`/api/textbooks/${id}`)
        if (res.data.code === 200) {
            ElMessage.success('删除成功')
            textbooks.value = textbooks.value.filter(t => t.id !== id)
        }
    } catch (e) {
        ElMessage.error('删除失败')
    }
  })
}

onMounted(fetchMyTextbooks)
</script>

<template>
  <div class="published-container">
    <div class="header">
      <h3>我的发布</h3>
      <el-button type="primary" @click="router.push('/textbooks/create')">发布新教材</el-button>
    </div>

    <el-tabs v-model="activeTab" class="status-tabs">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="出售中" name="selling" />
      <el-tab-pane label="已下架" name="off_shelf" />
      <el-tab-pane label="已售出" name="sold" />
    </el-tabs>

    <div v-loading="loading" class="list-container">
      <el-empty v-if="filteredTextbooks.length === 0" description="暂无相关记录" />
      
      <div v-else class="textbook-item" v-for="item in filteredTextbooks" :key="item.id">
        <div class="item-image-wrapper">
           <img :src="(item.images && item.images.length > 0 ? item.images[0] : item.cover) || 'https://placehold.co/150'" class="item-img" />
        </div>
        
        <div class="item-content">
          <div class="item-main">
            <h4 class="item-title" @click="handleView(item.id)">{{ item.title }}</h4>
            <div class="item-meta">
              <span>{{ item.author }}</span>
              <span class="separator">·</span>
              <span>{{ item.condition }}</span>
            </div>
            <div class="item-price-row">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ item.price }}</span>
              <el-tag 
                size="small" 
                :type="item.status === 'available' ? 'success' : (item.status === 'off_shelf' ? 'info' : 'warning')"
                class="status-tag"
              >
                {{ item.status === 'available' ? '在售' : (item.status === 'sold' ? '已售出' : (item.status === 'rented' ? '已出租' : '已下架')) }}
              </el-tag>
            </div>
          </div>
          
          <div class="item-actions-wrapper">
             <div class="publish-time">发布于 {{ new Date(item.createdAt).toLocaleDateString() }}</div>
             <div class="action-buttons">
                <el-button size="small" :icon="View" @click="handleView(item.id)">查看</el-button>
                <el-button 
                  v-if="item.status === 'available' || item.status === 'off_shelf'"
                  size="small" 
                  :type="item.status === 'available' ? 'warning' : 'success'" 
                  plain
                  :icon="item.status === 'available' ? VideoPause : VideoPlay" 
                  @click="handleToggleStatus(item)"
                >
                  {{ item.status === 'available' ? '下架' : '上架' }}
                </el-button>
                <el-button size="small" type="primary" plain :icon="Edit" @click="handleEdit(item.id)">编辑</el-button>
                <el-button size="small" type="danger" plain :icon="Delete" @click="handleDelete(item.id)">删除</el-button>
             </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.published-container {
  padding: 24px;
  background-color: #fff;
  border-radius: 12px;
  min-height: 500px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.status-tabs {
  margin-bottom: 24px;
}

.list-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.textbook-item {
  display: flex;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background-color: #fff;
  transition: all 0.3s;
}

.textbook-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-color: #dcdfe6;
}

.item-image-wrapper {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  margin-right: 20px;
  border: 1px solid #f0f0f0;
}

.item-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
}

.item-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  cursor: pointer;
  line-height: 1.4;
}

.item-title:hover {
  color: #409eff;
}

.item-meta {
  font-size: 14px;
  color: #909399;
  display: flex;
  align-items: center;
}

.separator {
  margin: 0 8px;
}

.item-price-row {
  margin-top: 4px;
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 14px;
  color: #f56c6c;
  font-weight: 600;
}

.price-value {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
  margin-right: 12px;
}

.item-actions-wrapper {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
}

.publish-time {
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 12px;
}
</style>