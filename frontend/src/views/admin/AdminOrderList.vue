<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { Delete, Timer } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const now = ref(Date.now())
let timerInterval: any = null

const fetchOrders = async () => {
  try {
    const res = await axios.get('/api/orders')
    if (res.data.code === 200) {
      orders.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  }
}

const getRemainingTime = (createdAt: string) => {
  if (!createdAt) return '00:00'
  const created = new Date(createdAt).getTime()
  if (isNaN(created)) return '00:00'
  
  const expireTime = created + 30 * 60 * 1000 // 30 minutes
  const diff = expireTime - now.value

  if (diff <= 0) return '00:00'

  const minutes = Math.floor(diff / 60000)
  const seconds = Math.floor((diff % 60000) / 1000)
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该订单吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/orders/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchOrders()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const getStatusType = (status: string) => {
  if (!status) return 'info'
  const s = status.toLowerCase().trim()
  const map: Record<string, string> = {
    pending: 'warning',
    completed: 'success',
    cancelled: 'info',
    shipped: 'primary',
    paid: 'success',
    refunded: 'info'
  }
  return map[s] || 'info'
}

const getStatusText = (status: string) => {
  if (!status) return ''
  const s = status.toLowerCase().trim()
  const map: Record<string, string> = {
    pending: '待支付',
    completed: '已完成',
    cancelled: '已取消',
    shipped: '已发货',
    paid: '已支付',
    refunded: '已退款'
  }
  return map[s] || status
}

onMounted(() => {
  fetchOrders()
  timerInterval = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">订单管理</h2>
      <div class="header-actions">
        <div class="admin-badge">
          <span class="role">管理员</span>
          <span class="name">admin</span>
        </div>
      </div>
    </div>

    <div class="table-container">
      <el-table 
        :data="orders" 
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="id" label="订单号" width="180" show-overflow-tooltip />
        <el-table-column prop="textbookId" label="教材ID" width="100" show-overflow-tooltip />
        <el-table-column prop="buyerId" label="买家ID" width="100" show-overflow-tooltip />
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="160">
          <template #default="{ row }">
            <div class="status-cell">
              <el-tag :type="getStatusType(row.status)" effect="light" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
              <div v-if="row.status === 'pending'" class="countdown">
                <el-icon class="timer-icon"><Timer /></el-icon>
                <span>{{ getRemainingTime(row.createdAt) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="删除订单" placement="top">
              <el-button link type="danger" :icon="Delete" @click="handleDelete(row)" />
            </el-tooltip>
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
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  position: relative;
  padding-left: 16px;
}

.title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: #409eff;
  border-radius: 2px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.admin-badge {
  display: flex;
  align-items: center;
  background: white;
  padding: 6px 16px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #ebeef5;
}

.role {
  font-size: 12px;
  color: #909399;
  margin-right: 8px;
  padding-right: 8px;
  border-right: 1px solid #dcdfe6;
}

.name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.table-container {
  background: white;
  border-radius: 12px;
  padding: 24px;
  flex: 1;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  overflow: hidden;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f56c6c;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
  background: #fef0f0;
  padding: 2px 8px;
  border-radius: 4px;
}

.timer-icon {
  font-size: 14px;
}

/* Element Plus overrides */
:deep(.el-table) {
  --el-table-header-bg-color: #f5f7fa;
}

:deep(.el-table__row) {
  transition: all 0.2s;
}

:deep(.el-table__row:hover) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  z-index: 1;
  position: relative;
}
</style>