<template>
  <div class="create-order-container">
    <div class="page-header">
      <h2>确认订单</h2>
      <el-steps :active="1" finish-status="success" align-center class="steps">
        <el-step title="确认订单" />
        <el-step title="在线支付" />
        <el-step title="支付成功" />
      </el-steps>
    </div>

    <div class="main-content" v-loading="loading">
      <!-- Address Section -->
      <div class="section-block">
        <div class="section-title">
          <el-icon><Location /></el-icon> 收货地址
        </div>
        
        <div v-if="addresses.length === 0" class="no-address-state">
          <el-empty description="暂无收货地址" :image-size="100">
            <el-button type="primary" @click="router.push(`/addresses?redirect=${encodeURIComponent(route.fullPath)}`)">添加新地址</el-button>
          </el-empty>
        </div>
        
        <div v-else class="address-grid">
          <div 
            v-for="addr in addresses" 
            :key="addr.id" 
            class="address-option"
            :class="{ active: selectedAddressId === addr.id }"
            @click="selectedAddressId = addr.id"
          >
            <div class="addr-header">
              <span class="recipient">{{ addr.recipient }}</span>
              <span class="phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.isDefault" size="small" type="info">默认</el-tag>
            </div>
            <div class="addr-detail">{{ addr.detail }}</div>
            <div class="check-mark" v-if="selectedAddressId === addr.id">
              <el-icon><Check /></el-icon>
            </div>
          </div>
          <div class="add-address-btn" @click="router.push(`/addresses?redirect=${encodeURIComponent(route.fullPath)}`)">
            <el-icon><Plus /></el-icon>
            <span>管理地址</span>
          </div>
        </div>
      </div>

      <!-- Order Items Section -->
      <div class="section-block">
        <div class="section-title">
          <el-icon><Goods /></el-icon> 商品信息
        </div>
        
        <div class="order-items">
          <div v-if="isBatch" class="batch-list">
            <div v-for="item in batchItems" :key="item.id" class="order-item-card">
              <img :src="item.cover || 'https://placehold.co/100'" class="item-img" />
              <div class="item-content">
                <h3 class="item-title">{{ item.title }}</h3>
                <div class="item-meta">
                  <span class="author">{{ item.author }}</span>
                  <span class="publisher">{{ item.publisher }}</span>
                </div>
                <div class="item-price-row">
                  <span class="price">¥{{ item.price }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else-if="textbook" class="order-item-card">
            <img :src="textbook.cover || 'https://placehold.co/100'" class="item-img" />
            <div class="item-content">
              <h3 class="item-title">{{ textbook.title }}</h3>
              <div class="item-meta">
                <span class="author">{{ textbook.author }}</span>
                <span class="publisher">{{ textbook.publisher }}</span>
              </div>
              <div class="item-price-row">
                <span class="price" v-if="orderType === 'donate'">免费</span>
                <span class="price" v-else>¥{{ orderAmount }}</span>
                <span v-if="orderType === 'rent'" class="rent-tag">/{{ rentDuration }}天</span>
              </div>
              <div v-if="orderType === 'rent'" class="rent-duration-select">
                <span>租赁天数：</span>
                <el-input-number v-model="rentDuration" :min="1" :max="365" size="small" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Sticky Footer -->
    <div class="order-footer">
      <div class="footer-content">
        <div class="footer-left">
          <span class="item-count">共 {{ isBatch ? batchItems.length : 1 }} 件商品</span>
        </div>
        <div class="footer-right">
          <div class="total-wrapper">
            <span>实付款：</span>
            <span class="final-price" v-if="orderType === 'donate'">免费</span>
            <span class="final-price" v-else>¥{{ orderAmount.toFixed(2) }}</span>
          </div>
          <el-button 
            type="primary" 
            size="large" 
            class="submit-btn"
            :loading="submitting" 
            @click="handleCreateOrder"
            :disabled="!selectedAddressId"
          >
            提交订单
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Location, Goods, Check, Plus } from '@element-plus/icons-vue'
import type { Textbook, Address } from '../types'

const route = useRoute()
const router = useRouter()
const textbook = ref<Textbook | null>(null)
const loading = ref(false)
const submitting = ref(false)
const addresses = ref<Address[]>([])
const selectedAddressId = ref('')

const isBatch = computed(() => route.query.mode === 'batch')
const batchItems = ref<any[]>([])
const textbookId = computed(() => route.query.textbookId as string)
const orderType = computed(() => (route.query.type as string) || 'buy')
const rentDuration = ref(1)

const orderAmount = computed(() => {
  if (isBatch.value) {
    return batchItems.value.reduce((sum, item) => sum + (item.price || 0), 0)
  }
  if (!textbook.value) return 0
  if (orderType.value === 'donate') return 0
  if (orderType.value === 'rent') {
    return (textbook.value.dailyRent || 0) * rentDuration.value
  }
  return textbook.value.price || 0
})

const fetchTextbook = async () => {
  if (isBatch.value) {
    const itemsStr = localStorage.getItem('checkoutItems')
    if (itemsStr) {
      batchItems.value = JSON.parse(itemsStr)
    }
    return
  }

  if (!textbookId.value) return
  try {
    const res = await axios.get(`/api/textbooks/${textbookId.value}`)
    textbook.value = res.data.data

    const userStr = localStorage.getItem('user')
    if (userStr && textbook.value) {
        const currentUser = JSON.parse(userStr)
        if (currentUser.id === textbook.value.sellerId) {
             let msg = '不能购买自己发布的商品'
             if (orderType.value === 'rent') msg = '不能租赁自己发布的商品'
             if (orderType.value === 'donate') msg = '不能领取自己发布的商品'
             ElMessage.error(msg)
             router.replace('/textbooks')
             return
        }
    }
  } catch (error) {
    console.error(error)
  }
}

// Fetch addresses from API
const fetchAddresses = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const currentUser = JSON.parse(userStr)

  try {
    const res = await axios.get('/api/addresses', { params: { userId: currentUser.id } })
    if (res.data.code === 200) {
      addresses.value = res.data.data
      if (addresses.value.length > 0) {
        const def = addresses.value.find(a => a.isDefault)
        selectedAddressId.value = def ? def.id : addresses.value[0].id
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const handleCreateOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  if (!isBatch.value && !textbook.value) {
    ElMessage.warning('商品信息未加载')
    return
  }
  
  if (isBatch.value && batchItems.value.length === 0) {
    ElMessage.warning('没有要结算的商品')
    return
  }

  const userStr = localStorage.getItem('user')
  const currentUser = userStr ? JSON.parse(userStr) : null

  if (!currentUser) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }

  submitting.value = true
  try {
    if (isBatch.value) {
        const orders = batchItems.value.map(item => ({
            textbookId: item.id,
            buyerId: currentUser.id,
            sellerId: item.sellerId,
            addressId: selectedAddressId.value,
            amount: item.price,
            type: item.type || 'buy'
        }))
        
        const res = await axios.post('/api/orders/batch', orders)
        if (res.data.code === 200) {
            ElMessage.success('批量下单成功')
            // Remove checkout items
            localStorage.removeItem('checkoutItems')
            // Optionally clear cart or remove bought items from cart
            // Since we don't have direct access to cart storage key 'cart' here easily without potentially overwriting other tabs, 
            // but we can try to remove them if they exist in 'cart'.
            const cartStr = localStorage.getItem('cart')
            if (cartStr) {
                const cartItems = JSON.parse(cartStr)
                const boughtIds = new Set(orders.map((o: any) => o.textbookId))
                const newCart = cartItems.filter((item: any) => !boughtIds.has(item.id))
                localStorage.setItem('cart', JSON.stringify(newCart))
            }
            router.push('/orders')
        } else {
            ElMessage.error(res.data.message || '批量下单失败')
        }
    } else {
        const payload = {
            textbookId: textbook.value!.id,
            buyerId: currentUser.id,
            sellerId: textbook.value!.sellerId,
            addressId: selectedAddressId.value,
            amount: orderAmount.value,
            type: orderType.value,
            rentDuration: orderType.value === 'rent' ? rentDuration.value : undefined
        }
        
        const res = await axios.post('/api/orders', payload)
        if (res.data.code === 200) {
            ElMessage.success('订单创建成功')
            router.push('/orders')
        } else {
            ElMessage.error(res.data.message || '创建订单失败')
        }
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchTextbook()
  fetchAddresses()
})
</script>

<style scoped>
.create-order-container {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 100px;
}

.page-header {
  padding: 40px 0 20px;
  text-align: center;
}

.page-header h2 {
  font-size: 24px;
  margin-bottom: 30px;
  color: #303133;
}

.steps {
  max-width: 600px;
  margin: 0 auto;
}

.main-content {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.section-block {
  margin-bottom: 40px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 12px;
}

.section-title .el-icon {
  color: #409eff;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.address-option {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.address-option:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.address-option.active {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.addr-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.recipient {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.phone {
  color: #606266;
  font-family: monospace;
}

.addr-detail {
  color: #606266;
  font-size: 14px;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.check-mark {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 0 24px 24px;
  border-color: transparent transparent #409eff transparent;
}

.check-mark .el-icon {
  position: absolute;
  bottom: -24px;
  right: 0;
  color: white;
  font-size: 14px;
  padding: 1px;
}

.add-address-btn {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  color: #909399;
  cursor: pointer;
  min-height: 100px;
  transition: all 0.2s;
}

.add-address-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.add-address-btn .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-item-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #f9fafc;
  border-radius: 8px;
}

.item-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 6px;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.item-meta {
  color: #909399;
  font-size: 14px;
  display: flex;
  gap: 15px;
}

.item-price-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.rent-tag {
  color: #909399;
  font-size: 12px;
}

.order-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 100;
  padding: 15px 0;
}

.footer-content {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.total-wrapper {
  display: flex;
  align-items: baseline;
  font-size: 14px;
  color: #606266;
}

.final-price {
  color: #f56c6c;
  font-size: 28px;
  font-weight: bold;
  margin-left: 5px;
}

.submit-btn {
  padding: 0 40px;
  font-size: 16px;
  font-weight: 600;
}

.rent-duration-select {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
  font-size: 14px;
}
</style>
