<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Timer } from '@element-plus/icons-vue'

const router = useRouter()
const user = ref<any>({})
const balance = ref(0.0)
const recentOrders = ref<any[]>([])
const myTextbooks = ref<any[]>([])
const now = ref(Date.now())
let timerInterval: ReturnType<typeof setInterval> | null = null

// Dialog visibility
const rechargeVisible = ref(false)
const withdrawVisible = ref(false)
const exchangeVisible = ref(false)

// Form data
const rechargeAmount = ref(10)
const withdrawAmount = ref(10)
const exchangePoints = ref(100)

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

const getStatusType = (status: string) => {
  if (!status) return 'info'
  const s = status.toLowerCase().trim()
  const map: Record<string, string> = {
    'pending': 'warning',
    'paid': 'success',
    'shipped': 'primary',
    'completed': 'success',
    'cancelled': 'info',
    'refunded': 'info'
  }
  return map[s] || 'info'
}

const getStatusText = (status: string) => {
  if (!status) return ''
  const s = status.toLowerCase().trim()
  const map: Record<string, string> = {
    'pending': '待付款',
    'paid': '已付款',
    'shipped': '已发货',
    'completed': '已完成',
    'cancelled': '已取消',
    'refunded': '已退款'
  }
  return map[s] || status
}

const fetchUser = async () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const userData = JSON.parse(userStr)
    user.value = userData
    
    try {
      // Fetch balance
      const res = await axios.get(`/api/wallet/balance/${userData.id}`)
      if (res.data.code === 200) {
        balance.value = res.data.data || 0
      }

      // Fetch updated user info (points, etc)
      const userRes = await axios.get(`/api/users/${userData.id}`)
      if (userRes.data.code === 200) {
        user.value = userRes.data.data
        localStorage.setItem('user', JSON.stringify(user.value))
      }

      // Fetch recent orders
      const ordersRes = await axios.get('/api/orders', { params: { buyerId: userData.id, limit: 2 } }) // Assuming backend supports limit or we slice
      if (ordersRes.data.code === 200) {
        // Mock data enhancement for demo as per screenshot
        const rawOrders = ordersRes.data.data.slice(0, 2)
         // Enhance orders with textbook info (mock or real)
         recentOrders.value = await Promise.all(rawOrders.map(async (order: any) => {
             try {
                 const tbRes = await axios.get(`/api/textbooks/${order.textbookId}`)
                 const tb = tbRes.data.data
                 if (tb) {
                    return { ...order, textbookTitle: tb.title, cover: tb.cover || (tb.images && tb.images.length > 0 ? tb.images[0] : '') || '' }
                 }
                 return { ...order, textbookTitle: '未知教材', cover: '' }
             } catch {
                 return { ...order, textbookTitle: '未知教材', cover: '' }
             }
         }))
      }

      // Fetch published textbooks
      const tbRes = await axios.get('/api/textbooks', { params: { sellerId: userData.id } })
      if (tbRes.data.code === 200) {
          myTextbooks.value = tbRes.data.data.slice(0, 4)
      }

    } catch (error) {
      console.error(error)
    }
  }
}

const handleRecharge = async () => {
  if (rechargeAmount.value <= 0) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }
  try {
    const res = await axios.post('/api/wallet/recharge', {
      userId: user.value.id,
      amount: rechargeAmount.value
    })
    if (res.data.code === 200) {
      ElMessage.success('充值成功')
      rechargeVisible.value = false
      fetchUser() // Refresh balance
    } else {
      ElMessage.error(res.data.msg || '充值失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  }
}

const handleWithdraw = async () => {
  if (withdrawAmount.value <= 0) {
    ElMessage.warning('请输入有效的提现金额')
    return
  }
  if (withdrawAmount.value > balance.value) {
    ElMessage.warning('余额不足')
    return
  }
  try {
    const res = await axios.post('/api/wallet/withdraw', {
      userId: user.value.id,
      amount: withdrawAmount.value
    })
    if (res.data.code === 200) {
      ElMessage.success('提现成功')
      withdrawVisible.value = false
      fetchUser() // Refresh balance
    } else {
      ElMessage.error(res.data.msg || '提现失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  }
}

const handleOpenExchange = () => {
  if ((user.value.points || 0) < 100) {
    ElMessage.error('积分不足 100，无法兑换')
    return
  }
  exchangeVisible.value = true
}

const handleExchange = async () => {
  if (exchangePoints.value < 100) {
    ElMessage.warning('最低兑换100积分')
    return
  }
  if (exchangePoints.value > (user.value.points || 0)) {
    ElMessage.warning('积分不足')
    return
  }
  try {
    const res = await axios.post('/api/wallet/exchange', {
      userId: user.value.id,
      points: exchangePoints.value
    })
    if (res.data.code === 200) {
      ElMessage.success('兑换成功')
      exchangeVisible.value = false
      fetchUser() // Refresh balance and points
    } else {
      ElMessage.error(res.data.msg || '兑换失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  }
}

onMounted(() => {
  fetchUser()
  timerInterval = setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>

<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <div class="user-avatar-section">
         <!-- Avatar is in sidebar, here just welcome text -->
      </div>
      <div>
          <h2 class="welcome-title">欢迎回来，{{ user.name }} 👋</h2>
          <p class="welcome-subtitle">这是您的个人中心概览</p>
      </div>
    </div>

    <div class="stats-cards">
      <el-card class="stat-card" shadow="hover">
        <div class="stat-header">信用分</div>
        <div class="stat-value credit-score">
            {{ user.creditScore || 605 }} <span class="tag">良好</span>
        </div>
        <div class="stat-action">
            <el-tag type="success" size="small" effect="light">已实名认证</el-tag>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-header">
            钱包余额
            <div class="actions">
                <el-button type="success" plain size="small" @click="withdrawVisible = true">提现</el-button>
                <el-button type="success" size="small" @click="rechargeVisible = true">充值</el-button>
            </div>
        </div>
        <div class="stat-value money">
            ¥{{ balance.toFixed(2) }} <span class="sub-text">可提现</span>
        </div>
      </el-card>

      <el-card class="stat-card" shadow="hover">
        <div class="stat-header">
            环保积分
            <el-button plain size="small" :type="(user.points || 0) < 100 ? 'info' : ''" @click="handleOpenExchange">兑换</el-button>
        </div>
        <div class="stat-value points">
            {{ user.points || 0 }} <span class="sub-text">可兑换</span>
        </div>
      </el-card>
    </div>

    <!-- Dialogs -->
    <el-dialog v-model="rechargeVisible" title="钱包充值" width="30%">
      <el-form>
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeAmount" :min="1" :precision="2" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rechargeVisible = false">取消</el-button>
          <el-button type="primary" @click="handleRecharge">确认充值</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="withdrawVisible" title="余额提现" width="30%">
      <el-form>
        <el-form-item label="提现金额">
          <el-input-number v-model="withdrawAmount" :min="1" :max="balance" :precision="2" :step="10" />
        </el-form-item>
        <div class="dialog-tip">当前可提现余额: ¥{{ balance.toFixed(2) }}</div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="withdrawVisible = false">取消</el-button>
          <el-button type="primary" @click="handleWithdraw">确认提现</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="exchangeVisible" title="积分兑换" width="30%">
      <el-form>
        <el-form-item label="兑换积分">
          <el-input-number v-model="exchangePoints" :min="0" :max="user.points || 0" :step="100" />
        </el-form-item>
        <div class="dialog-tip">
          当前积分: {{ user.points || 0 }} <br>
          兑换比例: 100积分 = 1元 <br>
          <span style="color: #f56c6c; font-size: 12px;">(最低兑换100积分)</span>
        </div>
        <div class="exchange-preview" v-if="exchangePoints >= 100">
          预计获得: ¥{{ (exchangePoints / 100).toFixed(2) }}
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="exchangeVisible = false">取消</el-button>
          <el-button type="primary" @click="handleExchange">确认兑换</el-button>
        </span>
      </template>
    </el-dialog>

    <div class="section-container">
        <div class="section-header">
            <h3>最近订单</h3>
            <el-button link type="primary" @click="router.push('/profile/orders')">查看全部</el-button>
        </div>
        <el-card shadow="never" class="list-card">
            <div v-if="recentOrders.length === 0" class="empty-list">暂无订单</div>
            <div v-else class="order-list">
                <div v-for="order in recentOrders" :key="order.id" class="order-item">
                    <img :src="order.cover || 'https://placehold.co/60'" class="item-img" />
                    <div class="item-info">
                        <div class="item-title">
                            {{ order.textbookTitle }}
                            <el-tag size="small" :type="getStatusType(order.status)" effect="plain" class="status-tag">
                                {{ getStatusText(order.status) }}
                            </el-tag>
                        </div>
                        <div class="item-meta">
                            <span>购买 • {{ order.createdAt }}</span>
                            <span v-if="order.status === 'pending'" class="countdown">
                                <el-icon><Timer /></el-icon>
                                剩余 {{ getRemainingTime(order.createdAt) }}
                            </span>
                        </div>
                    </div>
                    <div class="item-price">¥{{ order.amount }}</div>
                    <el-button size="small" plain @click="router.push(`/orders/${order.id}`)">详情</el-button>
                </div>
            </div>
        </el-card>
    </div>

    <div class="section-container">
        <div class="section-header">
            <h3>我发布的教材</h3>
            <el-button link type="primary" @click="router.push('/profile/published')">去发布</el-button>
        </div>
        <div class="textbook-grid">
             <div v-if="myTextbooks.length === 0" class="empty-list">暂无发布</div>
             <el-card v-for="book in myTextbooks" :key="book.id" :body-style="{ padding: '0px' }" class="book-card" shadow="hover">
                <img :src="book.cover || 'https://placehold.co/150'" class="book-image" />
                <div class="book-info">
                    <span class="book-title">{{ book.title }}</span>
                </div>
             </el-card>
        </div>
    </div>

  </div>
</template>

<style scoped>
.dashboard-container {
    padding-bottom: 40px;
}
.welcome-section {
    margin-bottom: 24px;
}
.welcome-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px 0;
    color: #303133;
}
.welcome-subtitle {
    color: #909399;
    margin: 0;
    font-size: 14px;
}

.stats-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-bottom: 30px;
}

.stat-card {
    border-radius: 8px;
}

.stat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #606266;
    font-size: 14px;
    margin-bottom: 16px;
}

.stat-value {
    font-size: 32px;
    font-weight: bold;
    color: #303133;
    display: flex;
    align-items: baseline;
    gap: 8px;
}

.credit-score {
    color: #409EFF;
}

.money {
    color: #67C23A;
}

.points {
    color: #E6A23C;
}

.tag {
    font-size: 14px;
    font-weight: normal;
    background: #ecf5ff;
    color: #409eff;
    padding: 2px 6px;
    border-radius: 4px;
}

.sub-text {
    font-size: 12px;
    font-weight: normal;
    color: #909399;
}

.section-container {
    margin-bottom: 30px;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.section-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
}

.order-list {
    display: flex;
    flex-direction: column;
}

.order-item {
    display: flex;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #EBEEF5;
}

.order-item:last-child {
    border-bottom: none;
}

.item-img {
    width: 48px;
    height: 48px;
    border-radius: 4px;
    object-fit: cover;
    margin-right: 16px;
}

.item-info {
    flex: 1;
}

.item-title {
    font-weight: 500;
    margin-bottom: 4px;
}

.item-meta {
    font-size: 12px;
    color: #909399;
}

.item-price {
    font-weight: bold;
    color: #409EFF;
    margin-right: 20px;
}

.textbook-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
}

.book-card {
    cursor: pointer;
}

.book-image {
    width: 100%;
    height: 120px;
    object-fit: cover;
}

.book-info {
    padding: 10px;
}

.book-title {
    font-size: 14px;
    font-weight: 500;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.status-tag {
    margin-left: 8px;
    vertical-align: text-bottom;
}

.countdown {
    margin-left: 12px;
    color: #f56c6c;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
}
</style>
