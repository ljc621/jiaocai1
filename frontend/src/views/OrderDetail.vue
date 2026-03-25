<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, Delete } from '@element-plus/icons-vue'
import type { Order, Textbook, Address, User } from '../types'

const route = useRoute()
const router = useRouter()
const order = ref<Order | null>(null)
const textbook = ref<Textbook | null>(null)
const address = ref<Address | null>(null)
const seller = ref<User | null>(null)
const loading = ref(false)
const now = ref(Date.now())
let timerInterval: any = null

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

const getStepActive = (status: string) => {
  const s = status?.toLowerCase().trim() || ''
  switch (s) {
    case 'pending': return 1
    case 'paid': return 2
    case 'shipped': return 3
    case 'renting': return 3 // 租赁中，相当于已发货/已收货，等待归还完成
    case 'returning': return 3 // 归还中，等待完成
    case 'completed': return 4
    default: return 0
  }
}

const orderId = route.params.id as string

const fetchAddress = async (addressId: string) => {
  try {
    const res = await axios.get(`/api/addresses/${addressId}`)
    if (res.data.code === 200) {
      address.value = res.data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchOrder = async () => {
  loading.value = true
  try {
    const res = await axios.get(`/api/orders/${orderId}`)
    if (res.data.code === 200) {
      order.value = res.data.data
      
      if (order.value) {
        if (order.value.addressId) {
          fetchAddress(order.value.addressId)
        }
        
        const [tbRes, sellerRes] = await Promise.all([
          axios.get(`/api/textbooks/${order.value.textbookId}`),
          axios.get(`/api/users/${order.value.sellerId}`)
        ])
        
        if (tbRes.data.code === 200) textbook.value = tbRes.data.data
        if (sellerRes.data.code === 200) seller.value = sellerRes.data.data
      }
    } else {
      ElMessage.error('订单不存在')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrder()
  timerInterval = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})

const handlePay = async () => {
  if (!order.value) return
  try {
    const res = await axios.post(`/api/orders/${order.value.id}/pay`)
    if (res.data.code === 200) {
      ElMessage.success('支付成功')
      fetchOrder()
    }
  } catch (e) {
    ElMessage.error('支付失败')
  }
}

const handleDelete = async () => {
  if (!order.value) return
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？删除后不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await axios.delete(`/api/orders/${order.value.id}`)
    if (res.data.code === 200) {
      ElMessage.success('订单删除成功')
      router.push('/orders')
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除操作失败')
    }
  }
}

const getStatusType = (status: string) => {
  if (!status) return 'info'
  const s = status.toLowerCase().trim()
  const map: Record<string, string> = {
    pending: 'warning',
    paid: 'success',
    shipped: 'primary',
    completed: 'success',
    cancelled: 'info',
    refunded: 'info'
  }
  return map[s] || 'info'
}

const getStatusText = (status: string) => {
  if (!status) return ''
  const s = status.toLowerCase().trim()
  const map: Record<string, string> = {
    pending: '待支付',
    paid: '已支付',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '已退款'
  }
  return map[s] || status
}
</script>

<template>
  <div class="order-detail-container" v-loading="loading">
    <div class="header">
      <el-page-header @back="router.back()">
        <template #content>
          <span class="text-large font-600 mr-3"> 订单详情 </span>
        </template>
      </el-page-header>
    </div>

    <div v-if="order" class="content">
      <el-steps :active="['pending', 'paid', 'shipped', 'completed'].indexOf(order.status.toLowerCase().trim())" finish-status="success" align-center class="steps">
        <el-step title="已下单" :description="order.createdAt" />
        <el-step title="已支付" />
        <el-step title="已发货" />
        <el-step title="已完成" />
      </el-steps>

      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <span>订单信息</span>
            <div class="status-wrapper">
                <span v-if="order.status === 'pending'" class="countdown-timer">
                    <el-icon><Timer /></el-icon>
                    剩余支付时间 {{ getRemainingTime(order.createdAt) }}
                </span>
                <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
            </div>
          </div>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ order.id }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ order.amount.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ order.createdAt }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="actions" v-if="order.status === 'pending'">
          <el-button type="danger" @click="handlePay">立即支付</el-button>
        </div>
        <div class="actions" v-if="order.status === 'cancelled' || order.status === 'completed'">
            <el-button type="danger" :icon="Delete" @click="handleDelete">删除订单</el-button>
        </div>
      </el-card>

      <el-card class="section-card" v-if="textbook">
        <template #header>
          <div class="card-header">
            <span>商品信息</span>
          </div>
        </template>
        <div class="product-info">
          <img :src="textbook.cover || 'https://placehold.co/100'" class="cover" />
          <div class="details">
            <h3>{{ textbook.title }}</h3>
            <p>作者: {{ textbook.author }}</p>
            <p>卖家: {{ seller?.name || '未知' }}</p>
          </div>
        </div>
      </el-card>

      <el-card class="section-card" v-if="address">
        <template #header>
          <div class="card-header">
            <span>收货地址</span>
          </div>
        </template>
        <el-descriptions :column="1">
          <el-descriptions-item label="收件人">{{ address.recipient }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ address.phone }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ address.detail }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.order-detail-container {
  max-width: 800px;
  margin: 0 auto;
}
.header {
  margin-bottom: 20px;
}
.steps {
  margin-bottom: 30px;
}
.section-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.status-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
}
.countdown-timer {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #f56c6c;
  font-weight: 500;
}
.product-info {
  display: flex;
  gap: 20px;
}
.cover {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}
.actions {
  margin-top: 20px;
  text-align: right;
}
</style>
