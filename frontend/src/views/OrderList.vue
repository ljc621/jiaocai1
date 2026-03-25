<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { Search, Delete, Timer } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Order } from '../types'

const router = useRouter()
const orders = ref<any[]>([])
const loading = ref(false)
const orderType = ref('buy') // 'buy' or 'sell'
const activeStatus = ref('') // '' for all
const searchQuery = ref('')
const isBatchMode = ref(false)

const reviewDialogVisible = ref(false)
const reviewForm = ref({
  content: '',
  rating: 5
})
const currentOrder = ref<Order | null>(null)

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

const fetchOrders = async () => {
  loading.value = true
  const userStr = localStorage.getItem('user')
  const user = userStr ? JSON.parse(userStr) : null
  
  if (!user) {
    loading.value = false
    return
  }

  try {
    const params: any = {}
    if (orderType.value === 'buy') {
      params.buyerId = user.id
    } else {
      params.sellerId = user.id
    }
    
    // Backend filtering by status not fully implemented in mock, so we filter client side or assume backend support
    // For now, let's fetch all and filter client side for status to ensure UI works as expected
    const res = await axios.get('/api/orders', { params })
    
    if (res.data.code === 200) {
      const rawOrders = res.data.data
      // Enhance orders
      const ordersWithDetails = await Promise.all(rawOrders.map(async (order: any) => {
        try {
          const tbRes = await axios.get(`/api/textbooks/${order.textbookId}`)
          const tb = tbRes.data.data
          return {
            ...order,
            textbookTitle: tb ? tb.title : '未知教材',
            cover: tb ? tb.cover : 'https://placehold.co/80',
            sellerName: 'User ' + order.sellerId, // Mock seller name
            buyerName: 'User ' + order.buyerId, // Mock buyer name
            type: order.type || 'buy',
            checked: false
          }
        } catch (e) {
          return {
            ...order,
            textbookTitle: '未知教材',
            cover: 'https://placehold.co/80',
            sellerName: 'Unknown',
            type: order.type || 'buy',
            checked: false
          }
        }
      }))
      orders.value = ordersWithDetails
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const filteredOrders = computed(() => {
    let result = orders.value
    if (activeStatus.value) {
        result = result.filter(o => o.status === activeStatus.value)
    }
    if (searchQuery.value) {
        const q = searchQuery.value.toLowerCase()
        result = result.filter(o => o.textbookTitle.toLowerCase().includes(q))
    }
    return result
})

const selectedOrders = computed(() => filteredOrders.value.filter(o => o.checked))
const batchTotal = computed(() => selectedOrders.value.reduce((sum, o) => sum + (o.amount || 0), 0))

const handleBatchPay = async () => {
    if (selectedOrders.value.length === 0) return
    
    try {
        const orderIds = selectedOrders.value.map(o => o.id)
        const res = await axios.post('/api/orders/batch/pay', orderIds)
        
        if (res.data.code === 200) {
            ElMessage.success('批量支付成功')
            fetchOrders()
            isBatchMode.value = false
        } else {
            ElMessage.error(res.data.message || '批量支付失败')
        }
    } catch (e) {
        ElMessage.error('支付请求失败')
    }
}

const toggleBatchMode = () => {
    isBatchMode.value = !isBatchMode.value
    if (!isBatchMode.value) {
        orders.value.forEach(o => o.checked = false)
    }
}

const handlePay = async (order: Order) => {
  try {
    const res = await axios.post(`/api/orders/${order.id}/pay`)
    if (res.data.code === 200) {
      ElMessage.success('支付成功')
      fetchOrders()
    } else {
      ElMessage.error(res.data.message || '支付失败')
    }
  } catch (e) {
    ElMessage.error('支付出错')
  }
}

const handleCancel = async (order: Order) => {
  try {
    const res = await axios.post(`/api/orders/${order.id}/cancel`)
    if (res.data.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrders()
    } else {
      ElMessage.error(res.data.message || '取消失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}

const handleConfirm = async (order: Order) => {
  try {
    const res = await axios.post(`/api/orders/${order.id}/complete`)
    if (res.data.code === 200) {
      ElMessage.success(order.type === 'rent' ? '已确认收货，租赁开始' : '订单已完成')
      fetchOrders()
    } else {
      ElMessage.error(res.data.message || '确认收货失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}

const handleReturn = async (order: Order) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入归还快递单号', '归还教材', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '快递单号不能为空'
    })
    
    if (value) {
      const res = await axios.post(`/api/orders/${order.id}/return`, null, {
        params: { trackingNumber: value }
      })
      if (res.data.code === 200) {
        ElMessage.success('归还申请已提交')
        fetchOrders()
      } else {
        ElMessage.error(res.data.message || '提交失败')
      }
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleConfirmReturn = async (order: Order) => {
  try {
    await ElMessageBox.confirm('确定已收到归还的教材？确认后订单将完成。', '确认收货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await axios.post(`/api/orders/${order.id}/confirm-return`)
    if (res.data.code === 200) {
      ElMessage.success('订单已完成')
      fetchOrders()
    } else {
      ElMessage.error(res.data.message || '确认失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleShip = async (order: Order) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入快递单号', '发货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '快递单号不能为空'
    })
    
    if (value) {
      const res = await axios.post(`/api/orders/${order.id}/ship`, null, {
        params: { trackingNumber: value }
      })
      if (res.data.code === 200) {
        ElMessage.success('发货成功')
        fetchOrders()
      } else {
        ElMessage.error(res.data.message || '发货失败')
      }
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('发货操作失败')
    }
  }
}

const handleDelete = async (order: Order) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？删除后不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await axios.delete(`/api/orders/${order.id}`)
    if (res.data.code === 200) {
      ElMessage.success('订单删除成功')
      fetchOrders()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除操作失败')
    }
  }
}

const handleReview = (order: Order) => {
  currentOrder.value = order
  reviewForm.value = { content: '', rating: 5 }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!currentOrder.value) return
  
  if (!reviewForm.value.content) {
    ElMessage.warning('请输入评价内容')
    return
  }
  
  try {
    const res = await axios.post(`/api/orders/${currentOrder.value.id}/review`, reviewForm.value)
    if (res.data.code === 200) {
      ElMessage.success('评价提交成功')
      reviewDialogVisible.value = false
      fetchOrders()
    } else {
      ElMessage.error(res.data.message || '评价失败')
    }
  } catch (e) {
    ElMessage.error('提交评价失败')
  }
}

const getStatusType = (status: string) => {
  if (!status) return 'info'
  const s = status.toLowerCase().trim()
  const map: Record<string, string> = {
    pending: 'warning', // 待付款
    paid: 'primary', // 待发货
    shipped: 'info', // 待收货
    renting: 'success', // 租赁中
    returning: 'warning', // 归还中
    completed: 'success', // 已完成
    cancelled: 'info', // 已取消
    refunded: 'info'
  }
  return map[s] || 'info'
}

const getStatusText = (status: string, type?: string) => {
  if (!status) return ''
  const s = status.toLowerCase().trim()
  
  if (s === 'pending' && type === 'donate') return '待领取'

  const map: Record<string, string> = {
    pending: '待付款',
    paid: '待发货',
    shipped: '待收货',
    renting: '租赁中',
    returning: '归还中',
    completed: '已完成',
    cancelled: '已取消',
    refunded: '已退款'
  }
  return map[s] || status
}

const getStatusTagEffect = (status: string) => {
    const s = (status || '').toLowerCase().trim()
    return s === 'completed' ? 'light' : 'dark'
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
  <div class="order-list-container">
    <div class="header-section">
        <div class="header-title-row">
            <h2>我的订单</h2>
            <div class="header-controls">
                <el-radio-group v-model="orderType" @change="fetchOrders" size="default">
                    <el-radio-button label="buy">我买的</el-radio-button>
                    <el-radio-button label="sell">我卖的</el-radio-button>
                </el-radio-group>
            </div>
        </div>
        
        <div class="filter-section">
            <div class="status-tabs">
                <el-radio-group v-model="activeStatus" class="status-radio-group">
                    <el-radio-button label="">全部</el-radio-button>
                    <el-radio-button label="pending">待付款</el-radio-button>
                    <el-radio-button label="paid">待发货</el-radio-button>
                    <el-radio-button label="shipped">待收货</el-radio-button>
                    <el-radio-button label="renting">租赁中</el-radio-button>
                    <el-radio-button label="returning">归还中</el-radio-button>
                    <el-radio-button label="completed">已完成</el-radio-button>
                    <el-radio-button label="cancelled">已取消</el-radio-button>
                </el-radio-group>
            </div>
            <div class="filter-actions">
                 <el-input v-model="searchQuery" placeholder="搜索商品名称/订单号" :prefix-icon="Search" class="search-input" clearable />
                 <el-button v-if="orderType === 'buy' && activeStatus !== 'completed' && activeStatus !== 'cancelled'" 
                    :type="isBatchMode ? 'danger' : 'primary'" 
                    plain
                    @click="toggleBatchMode">
                    {{ isBatchMode ? '取消批量' : '批量支付' }}
                 </el-button>
            </div>
        </div>
    </div>

    <div v-loading="loading" class="order-items-container">
      <el-empty v-if="filteredOrders.length === 0" description="暂无相关订单" :image-size="200" />
      
      <div v-else class="orders-list">
          <el-card v-for="order in filteredOrders" :key="order.id" class="order-card" shadow="hover" :body-style="{ padding: '0px' }">
            <div class="card-header">
                <div class="header-left">
                    <el-checkbox v-if="isBatchMode && order.status === 'pending'" v-model="order.checked" class="batch-checkbox" />
                    <span class="order-date">{{ order.createdAt }}</span>
                    <span class="order-id">订单号：{{ order.id }}</span>
                    <span class="order-party">{{ orderType === 'buy' ? '卖家' : '买家' }}：{{ orderType === 'buy' ? (order as any).sellerName : (order as any).buyerName }}</span>
                </div>
                <div class="header-right">
                    <span v-if="order.status === 'pending'" class="countdown-timer">
                        <el-icon><Timer /></el-icon>
                        剩余支付时间 {{ getRemainingTime(order.createdAt) }}
                    </span>
                    <el-tag :type="getStatusType(order.status)" effect="dark" size="default" class="status-tag">
                        {{ getStatusText(order.status) }}
                    </el-tag>
                </div>
            </div>
            
            <div class="card-body">
              <div class="product-info">
                  <img :src="order.cover" class="product-img" />
                  <div class="product-details">
                    <h3 class="product-title" @click="router.push(`/textbooks/${order.textbookId}`)">{{ order.textbookTitle }}</h3>
                    <div class="product-meta">
                        <el-tag size="small" :type="order.type === 'rent' ? 'warning' : 'success'" effect="plain">
                            {{ order.type === 'rent' ? '租赁' : '购买' }}
                        </el-tag>
                    </div>
                  </div>
              </div>
              
              <div class="order-amount">
                  <span class="amount-label">订单金额</span>
                  <span class="amount-value">¥{{ order.amount.toFixed(2) }}</span>
              </div>

              <div class="order-actions">
                 <el-button link type="primary" @click="router.push(`/orders/${order.id}`)">订单详情</el-button>
                 
                 <!-- Pending Actions -->
                 <template v-if="orderType === 'buy' && order.status === 'pending'">
                     <el-button type="info" link @click="handleCancel(order)">取消订单</el-button>
                     <el-button v-if="order.type === 'donate'" type="success" size="small" @click="handlePay(order)">确认领取</el-button>
                     <el-button v-else type="danger" size="small" @click="handlePay(order)">立即支付</el-button>
                 </template>
                 
                 <!-- Shipped Actions -->
                 <el-button v-if="orderType === 'buy' && order.status === 'shipped'" type="primary" size="small" @click="handleConfirm(order)">确认收货</el-button>
                 
                 <!-- Renting Actions (Buyer) -->
                 <el-button v-if="orderType === 'buy' && order.status === 'renting'" type="warning" size="small" @click="handleReturn(order)">归还教材</el-button>

                 <!-- Returning Actions (Seller) -->
                 <el-button v-if="orderType === 'sell' && order.status === 'returning'" type="success" size="small" @click="handleConfirmReturn(order)">确认归还</el-button>

                 <!-- Paid Actions (Seller) -->
                 <el-button v-if="orderType === 'sell' && order.status === 'paid'" type="primary" size="small" @click="handleShip(order)">去发货</el-button>
                 
                 <!-- Completed Actions -->
                 <template v-if="order.status === 'completed'">
                     <el-button v-if="orderType === 'buy'" type="warning" link @click="handleReview(order)">评价</el-button>
                     <el-button type="danger" link :icon="Delete" @click="handleDelete(order)">删除</el-button>
                 </template>

                 <!-- Cancelled Actions -->
                 <template v-if="order.status === 'cancelled'">
                     <el-button type="danger" link :icon="Delete" @click="handleDelete(order)">删除</el-button>
                 </template>
              </div>
            </div>
          </el-card>
      </div>
    </div>

    <el-dialog v-model="reviewDialogVisible" title="评价订单" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="请输入您的评价..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview">提交评价</el-button>
        </span>
      </template>
    </el-dialog>

    <transition name="slide-up">
        <div v-if="isBatchMode" class="batch-footer-bar">
            <div class="batch-left">
                 <el-checkbox :checked="selectedOrders.length > 0 && selectedOrders.length === filteredOrders.filter(o => o.status === 'pending').length" 
                    @change="(val: any) => { filteredOrders.filter(o => o.status === 'pending').forEach(o => o.checked = val) }">
                    全选本页待付款
                 </el-checkbox>
            </div>
            <div class="batch-right">
                <div class="batch-summary">
                    已选 <span class="highlight-count">{{ selectedOrders.length }}</span> 个订单，
                    合计：<span class="highlight-price">¥{{ batchTotal.toFixed(2) }}</span>
                </div>
                <el-button type="danger" size="large" :disabled="selectedOrders.length === 0" @click="handleBatchPay">合并支付</el-button>
            </div>
        </div>
    </transition>
  </div>
</template>

<style scoped>
.order-list-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
  min-height: 80vh;
  padding-bottom: 80px; /* Space for footer */
}

.header-section {
    margin-bottom: 20px;
}
.header-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.header-title-row h2 {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
    color: #303133;
}

.filter-section {
    background: #fff;
    padding: 15px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
    display: flex;
    flex-direction: column;
    gap: 15px;
}
.status-tabs {
    overflow-x: auto;
    white-space: nowrap;
    padding-bottom: 5px;
}
.filter-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.search-input {
    width: 300px;
}

.orders-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.order-card {
    border: none;
    border-radius: 8px;
    transition: all 0.3s;
    overflow: hidden;
}
.order-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.card-header {
    background: #f5f7fa;
    padding: 12px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #ebeef5;
}
.header-left {
    display: flex;
    align-items: center;
    gap: 20px;
    color: #606266;
    font-size: 13px;
}
.batch-checkbox {
    margin-right: 0;
}
.order-id {
    font-family: monospace;
}
.order-party {
    color: #909399;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 10px;
}
.countdown-timer {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #f56c6c;
    font-weight: 500;
}

.card-body {
    padding: 20px;
    display: flex;
    align-items: center;
}
.product-info {
    flex: 2;
    display: flex;
    gap: 15px;
}
.product-img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 4px;
    border: 1px solid #eee;
}
.product-details {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}
.product-title {
    margin: 0;
    font-size: 16px;
    color: #303133;
    cursor: pointer;
    line-height: 1.4;
}
.product-title:hover {
    color: #409eff;
}

.order-amount {
    flex: 1;
    text-align: center;
    border-left: 1px solid #ebeef5;
    border-right: 1px solid #ebeef5;
    padding: 0 20px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 5px;
}
.amount-label {
    font-size: 12px;
    color: #909399;
}
.amount-value {
    font-size: 18px;
    font-weight: bold;
    color: #303133;
}

.order-actions {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 10px;
    padding-left: 20px;
}

.batch-footer-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    padding: 15px 40px;
    box-shadow: 0 -4px 12px rgba(0,0,0,0.1);
    display: flex;
    justify-content: space-between;
    align-items: center;
    z-index: 200;
    animation: slideUp 0.3s ease-out;
}
.batch-right {
    display: flex;
    align-items: center;
    gap: 20px;
}
.batch-summary {
    font-size: 14px;
    color: #606266;
}
.highlight-count {
    color: #f56c6c;
    font-weight: bold;
    font-size: 16px;
    margin: 0 2px;
}
.highlight-price {
    color: #f56c6c;
    font-weight: bold;
    font-size: 20px;
    margin-left: 5px;
}

@keyframes slideUp {
    from { transform: translateY(100%); }
    to { transform: translateY(0); }
}

@media (max-width: 768px) {
    .card-body {
        flex-direction: column;
        align-items: flex-start;
        gap: 15px;
    }
    .order-amount {
        border: none;
        padding: 0;
        text-align: left;
        flex-direction: row;
        align-items: baseline;
        width: 100%;
        justify-content: space-between;
    }
    .order-actions {
        width: 100%;
        flex-direction: row;
        justify-content: flex-end;
        padding-left: 0;
        border-top: 1px solid #ebeef5;
        padding-top: 15px;
    }
    .batch-footer-bar {
        padding: 10px 20px;
    }
}
</style>
