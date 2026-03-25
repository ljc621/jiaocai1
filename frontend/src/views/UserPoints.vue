<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Medal, TopRight, BottomLeft, Delete } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

interface PointTransaction {
  id: string
  points: number
  type: string
  description: string
  balance?: number
  createdAt: string
}

const transactions = ref<PointTransaction[]>([])
const loading = ref(false)

const getTransactionType = (points: number) => {
  return points > 0 ? 'income' : 'expense'
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString()
}

const handleDelete = async (id: string) => {
  try {
    const res = await axios.delete(`/api/points/transactions/${id}`)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      fetchTransactions()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除请求失败')
  }
}

const fetchTransactions = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const user = JSON.parse(userStr)
  
  loading.value = true
  try {
    const res = await axios.get(`/api/points/transactions/${user.id}`)
    if (res.data.code === 200) {
      transactions.value = res.data.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTransactions()
})
</script>

<template>
  <div class="wallet-container" v-loading="loading">
    <div class="header">
      <h3><el-icon><Medal /></el-icon> 我的积分账单</h3>
    </div>

    <div class="transactions-list">
      <el-empty v-if="transactions.length === 0" description="暂无积分记录" />
      <div v-else v-for="item in transactions" :key="item.id" class="transaction-item">
        <div class="icon-wrapper" :class="getTransactionType(item.points)">
          <el-icon>
            <TopRight v-if="getTransactionType(item.points) === 'expense'" />
            <BottomLeft v-else />
          </el-icon>
        </div>
        <div class="info">
          <div class="title">{{ item.description }}</div>
          <div class="time">{{ formatTime(item.createdAt) }}</div>
        </div>
        <div class="amount-info">
          <div class="amount" :class="getTransactionType(item.points)">
            {{ item.points > 0 ? '+' : '' }}{{ item.points }} 积分
          </div>
          <div class="balance" v-if="item.balance !== undefined && item.balance !== null">
            余额: {{ item.balance }} 积分
          </div>
        </div>
        <div class="actions">
          <el-popconfirm title="确定删除这条记录吗？" @confirm="handleDelete(item.id)">
            <template #reference>
              <el-button type="danger" :icon="Delete" circle plain size="small" class="delete-btn" />
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wallet-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  min-height: 500px;
}
.header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  color: #333;
}
.transactions-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.transaction-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  transition: all 0.3s;
}
.transaction-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 20px;
}
.icon-wrapper.expense {
  background-color: #fef0f0;
  color: #f56c6c;
}
.icon-wrapper.income {
  background-color: #f0f9eb;
  color: #67c23a;
}
.info {
  flex: 1;
}
.title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}
.time {
  font-size: 12px;
  color: #909399;
}
.amount-info {
  text-align: right;
}
.amount {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}
.amount.expense {
  color: #f56c6c;
}
.amount.income {
  color: #67c23a;
}
.balance {
  font-size: 12px;
  color: #909399;
}
.actions {
  margin-left: 20px;
  opacity: 0;
  transition: opacity 0.3s;
}
.transaction-item:hover .actions {
  opacity: 1;
}
</style>
