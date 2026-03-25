<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, ArrowRight, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const cartItems = ref<any[]>([])
const checkAll = ref(false)
const isIndeterminate = ref(false)

onMounted(() => {
  const cartStr = localStorage.getItem('cart')
  if (cartStr) {
    try {
      cartItems.value = JSON.parse(cartStr).map((item: any) => ({ ...item, checked: false }))
    } catch (e) {
      cartItems.value = []
    }
  }
})

const handleCheckAllChange = (val: boolean) => {
  cartItems.value.forEach(item => item.checked = val)
  isIndeterminate.value = false
}

const handleItemCheckChange = () => {
  const checkedCount = cartItems.value.filter(item => item.checked).length
  checkAll.value = checkedCount === cartItems.value.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < cartItems.value.length
}

const selectedItems = computed(() => cartItems.value.filter(item => item.checked))
const totalPrice = computed(() => selectedItems.value.reduce((sum, item) => sum + (item.price || 0), 0))

const removeFromCart = (index: number) => {
  cartItems.value.splice(index, 1)
  localStorage.setItem('cart', JSON.stringify(cartItems.value))
  ElMessage.success('已移除')
  handleItemCheckChange()
}

const checkout = (item: any) => {
    const userStr = localStorage.getItem('user')
    if (userStr) {
         const user = JSON.parse(userStr)
         if (user.id === item.sellerId) {
             ElMessage.warning('不能购买自己发布的商品')
             return
         }
     }
    router.push({
    path: '/orders/create',
    query: { textbookId: item.id }
  })
}

const batchCheckout = () => {
    if (selectedItems.value.length === 0) return ElMessage.warning('请选择要结算的商品')
    
    const userStr = localStorage.getItem('user')
    if (userStr) {
         const user = JSON.parse(userStr)
         const hasSelfItem = selectedItems.value.some(item => item.sellerId === user.id)
         if (hasSelfItem) {
             ElMessage.warning('不能购买自己发布的商品')
             return
         }
     }

    localStorage.setItem('checkoutItems', JSON.stringify(selectedItems.value))
    router.push({
        path: '/orders/create',
        query: { mode: 'batch' }
    })
}
</script>

<template>
  <div class="cart-container">
    <h2>购物车</h2>
    <div v-if="cartItems.length === 0" class="empty-cart">
      <div class="empty-icon-wrapper">
        <el-icon class="empty-icon"><ShoppingCart /></el-icon>
      </div>
      <h3>购物车还是空的</h3>
      <p>看起来你还没有添加任何教材到购物车</p>
      <el-button type="primary" size="large" class="go-shopping-btn" @click="router.push('/textbooks')">
        去逛逛 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
      </el-button>
    </div>
    <div v-else class="cart-list">
      <div class="cart-header-row">
        <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange">全选 ({{ cartItems.length }})</el-checkbox>
      </div>
      
      <div class="cart-items-container">
        <el-card v-for="(item, index) in cartItems" :key="item.id" class="cart-item" shadow="hover">
            <div class="item-content-wrapper">
                <el-checkbox v-model="item.checked" @change="handleItemCheckChange" class="item-checkbox"></el-checkbox>
                <div class="item-main">
                    <img :src="item.cover || 'https://placehold.co/100'" class="item-img" />
                    <div class="item-info">
                        <div class="item-header">
                            <h3 class="item-title" @click="router.push(`/textbooks/${item.id}`)">{{ item.title }}</h3>
                            <el-button type="danger" link :icon="Delete" @click="removeFromCart(index)" class="delete-btn">删除</el-button>
                        </div>
                        <div class="item-tags">
                             <el-tag size="small" effect="plain">{{ item.condition }}</el-tag>
                             <el-tag size="small" :type="item.type === 'rent' ? 'warning' : 'success'" effect="light" class="ml-2">
                                {{ item.type === 'rent' ? '租赁' : '出售' }}
                             </el-tag>
                        </div>
                        <div class="item-footer">
                            <span class="price">¥{{ item.price }}</span>
                            <el-button v-if="!selectedItems.length" type="primary" size="small" plain @click="checkout(item)">立即购买</el-button>
                        </div>
                    </div>
                </div>
            </div>
        </el-card>
      </div>

      <div class="cart-footer-bar" v-if="cartItems.length > 0">
        <div class="footer-left">
            <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" @change="handleCheckAllChange">全选</el-checkbox>
            <span class="selected-count ml-4">已选 {{ selectedItems.length }} 件</span>
        </div>
        <div class="footer-right">
            <div class="total-info">
                <span>合计：</span>
                <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <el-button type="primary" size="large" class="checkout-btn" :disabled="selectedItems.length === 0" @click="batchCheckout">
                结算
            </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
  min-height: 60vh;
}
.cart-container h2 {
    font-size: 24px;
    margin-bottom: 25px;
    font-weight: 600;
}

.empty-cart {
    text-align: center;
    padding: 60px 0;
    background: #fff;
    border-radius: 8px;
}
.empty-icon-wrapper {
    width: 120px;
    height: 120px;
    background: #f5f7fa;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20px;
}
.empty-icon {
    font-size: 60px;
    color: #909399;
}
.empty-cart h3 {
    font-size: 20px;
    color: #303133;
    margin-bottom: 10px;
}
.empty-cart p {
    color: #909399;
    margin-bottom: 30px;
}
.go-shopping-btn {
    padding: 12px 30px;
    font-size: 16px;
}

.cart-header-row {
    margin-bottom: 15px;
    padding: 0 10px;
}

.cart-items-container {
    margin-bottom: 80px; /* Space for fixed footer */
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.cart-item {
    border: none;
    border-radius: 8px;
    transition: all 0.3s;
}
.cart-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.item-content-wrapper {
    display: flex;
    align-items: center;
}
.item-checkbox {
    margin-right: 20px;
}
.item-main {
    flex: 1;
    display: flex;
    gap: 20px;
}
.item-img {
    width: 120px;
    height: 120px;
    object-fit: cover;
    border-radius: 6px;
    border: 1px solid #eee;
}
.item-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}
.item-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
}
.item-title {
    margin: 0;
    font-size: 16px;
    color: #303133;
    cursor: pointer;
    line-height: 1.4;
    max-width: 80%;
}
.item-title:hover {
    color: #409eff;
}
.delete-btn {
    padding: 0;
}
.item-tags {
    margin: 8px 0;
}
.item-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.price {
    font-size: 20px;
    font-weight: bold;
    color: #f56c6c;
}

.cart-footer-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    padding: 15px 40px;
    box-shadow: 0 -2px 12px rgba(0,0,0,0.05);
    display: flex;
    justify-content: space-between;
    align-items: center;
    z-index: 100;
}
.footer-left {
    display: flex;
    align-items: center;
}
.selected-count {
    color: #606266;
    font-size: 14px;
}
.footer-right {
    display: flex;
    align-items: center;
    gap: 20px;
}
.total-info {
    display: flex;
    align-items: baseline;
    gap: 5px;
}
.total-price {
    font-size: 24px;
    font-weight: bold;
    color: #f56c6c;
}
.checkout-btn {
    width: 120px;
    font-size: 16px;
}

.ml-2 { margin-left: 8px; }
.ml-4 { margin-left: 16px; }

@media (max-width: 768px) {
    .cart-footer-bar {
        padding: 10px 20px;
    }
    .item-img {
        width: 80px;
        height: 80px;
    }
}
</style>
