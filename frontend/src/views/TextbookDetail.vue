<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { 
  ShoppingCart, 
  Star, 
  StarFilled,
  ChatDotRound, 
  ArrowRight, 
  User, 
  Timer,
  Goods,
  Reading
} from '@element-plus/icons-vue'
import type { Textbook, User as UserType } from '../types'

interface Review {
    id: string;
    userId: string;
    userName: string;
    userAvatar: string;
    rating: number;
    content: string;
    createdAt: string;
}

const route = useRoute()
const router = useRouter()
const textbook = ref<Textbook | null>(null)
const seller = ref<UserType | null>(null)
const loading = ref(false)
const isFavorite = ref(false)

// Review related
const sellerReviews = ref<Review[]>([])
const activeTab = ref('detail')

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await axios.get(`/api/textbooks/${route.params.id}`)
    if (res.data.code === 200) {
      textbook.value = res.data.data
      checkFavorite()
      
      // Fetch seller info
      if (textbook.value?.sellerId) {
        const userRes = await axios.get(`/api/users/${textbook.value.sellerId}`)
        if (userRes.data.code === 200) {
          seller.value = userRes.data.data
        }
        // Fetch seller reviews
        const sellerReviewRes = await axios.get(`/api/reviews/seller/${textbook.value.sellerId}`)
        if (sellerReviewRes.data.code === 200) {
          sellerReviews.value = sellerReviewRes.data.data
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载详情失败')
  } finally {
    loading.value = false
  }
}

const handleContactSeller = () => {
  if (!seller.value) {
    ElMessage.warning('无法获取卖家信息')
    return
  }
  
  router.push({
    path: '/messages',
    query: { userId: seller.value.id }
  })
}

const checkFavorite = () => {
  if (!textbook.value) return
  const favStr = localStorage.getItem('favorites')
  if (favStr) {
    const favs = JSON.parse(favStr)
    isFavorite.value = favs.some((f: any) => f.id === textbook.value?.id)
  }
}

const handleToggleFavorite = () => {
  if (!textbook.value) return
  const favStr = localStorage.getItem('favorites')
  let favs = favStr ? JSON.parse(favStr) : []
  
  if (isFavorite.value) {
    favs = favs.filter((f: any) => f.id !== textbook.value?.id)
    isFavorite.value = false
    ElMessage.success('已取消收藏')
  } else {
    favs.push(textbook.value)
    isFavorite.value = true
    ElMessage.success('已收藏')
  }
  localStorage.setItem('favorites', JSON.stringify(favs))
  window.dispatchEvent(new Event('favorites-update'))
}

const handleAddToCart = () => {
  if (!textbook.value) return

  const userStr = localStorage.getItem('user')
  if (userStr) {
      const user = JSON.parse(userStr)
      if (user.id === textbook.value.sellerId) {
          ElMessage.warning('不能将自己发布的商品加入购物车')
          return
      }
  }

  const cartStr = localStorage.getItem('cart')
  let cart = cartStr ? JSON.parse(cartStr) : []
  
  // Check if already in cart
  if (cart.some((item: any) => item.id === textbook.value?.id)) {
    ElMessage.warning('该商品已在购物车中')
    return
  }
  
  cart.push(textbook.value)
  localStorage.setItem('cart', JSON.stringify(cart))
  ElMessage.success('已加入购物车')
}

const handleBuy = (type: string = 'sell') => {
  if (!textbook.value) return

  const userStr = localStorage.getItem('user')
  if (userStr) {
      const user = JSON.parse(userStr)
      if (user.id === textbook.value.sellerId) {
          const msg = type === 'rent' ? '不能租赁自己发布的商品' : '不能购买自己发布的商品'
          ElMessage.warning(msg)
          return
      }
  }

  router.push({
    path: '/orders/create',
    query: { textbookId: textbook.value.id, type }
  })
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <!-- Breadcrumb -->
    <div class="breadcrumb-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/textbooks' }">教材广场</el-breadcrumb-item>
        <el-breadcrumb-item v-if="textbook">{{ textbook.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="content-wrapper" v-if="textbook">
      <!-- Main Info Section -->
      <div class="product-card">
        <el-row :gutter="40">
          <!-- Left: Image -->
          <el-col :md="10" :sm="24">
            <div class="image-gallery">
              <img :src="textbook.cover || 'https://placehold.co/400x500'" class="main-image" />
              <div class="image-overlay" v-if="textbook.status !== 'available'">
                <div class="status-badge">
                   {{ textbook.status === 'sold' ? '已售出' : (textbook.status === 'rented' ? '已出租' : '商品已下架') }}
                </div>
              </div>
            </div>
          </el-col>

          <!-- Right: Info -->
          <el-col :md="14" :sm="24">
            <div class="product-info">
              <!-- Header -->
              <div class="info-header">
                <h1 class="product-title">{{ textbook.title }}</h1>
                <div class="header-actions">
                   <el-icon class="action-icon" @click="handleToggleFavorite" :class="{ active: isFavorite }">
                     <Star v-if="!isFavorite" />
                     <StarFilled v-else style="color: #f56c6c;" />
                   </el-icon>
                   <el-icon class="action-icon"><Share /></el-icon>
                </div>
              </div>
              
              <!-- Meta Info -->
              <div class="meta-info-list">
                <div class="meta-row">
                  <span class="label">作者：</span>
                  <span class="value">{{ textbook.author }}</span>
                </div>
                <div class="meta-row">
                  <span class="label">出版社：</span>
                  <span class="value">{{ textbook.publisher }}</span>
                </div>
                <div class="meta-row">
                  <span class="label">成色：</span>
                  <span class="value condition-highlight">{{ textbook.condition }}</span>
                </div>
              </div>

              <!-- Seller Card -->
              <div class="seller-card-new" v-if="seller">
                <div class="seller-left">
                  <el-avatar :size="48" :src="seller.avatar" class="seller-avatar" :style="{ background: '#409eff' }">
                    {{ seller.name?.charAt(0).toUpperCase() }}
                  </el-avatar>
                  <div class="seller-details">
                    <div class="seller-name">{{ seller.name }}</div>
                    <div class="seller-sub">
                      {{ seller.university || '未知大学' }} · 信用分 {{ seller.creditScore || 600 }}
                    </div>
                  </div>
                </div>
                <el-button round class="contact-btn" @click="handleContactSeller">
                  <el-icon><ChatDotRound /></el-icon> 联系卖家
                </el-button>
              </div>

              <!-- Price & Action Box -->
              <div class="purchase-box" :class="{ 'donate-theme': textbook.type === 'donate' }">
                <div class="price-display">
                   <div v-if="textbook.type === 'donate'" class="donate-simple-wrapper">
                     <div class="donate-left-col">
                        <span class="donate-label">爱心捐赠</span>
                        <div class="donate-price-row">
                          <span class="free-text">免费领取</span>
                          <span class="original-price" v-if="textbook.originalPrice">原价 ¥{{ textbook.originalPrice }}</span>
                        </div>
                     </div>
                     <div class="donate-right-col">
                        <div class="love-message">
                          <el-icon><Sugar /></el-icon>
                          <span>传递知识 · 分享爱心</span>
                        </div>
                     </div>
                   </div>
                   <div v-else>
                     <div v-if="textbook.price" class="price-item">
                       <span class="price-label">出售价格</span>
                       <span class="price-symbol">¥</span>
                       <span class="price-amount">{{ textbook.price }}</span>
                       <el-tag type="warning" size="small" effect="plain" round class="ml-2">
                         送 {{ Math.floor(textbook.price) }} 积分
                       </el-tag>
                     </div>
                     <div v-if="textbook.rentEnabled && textbook.dailyRent" class="price-item" :class="{ 'ml-4': textbook.price }">
                       <span class="price-label">日租金</span>
                       <span class="price-symbol">¥</span>
                       <span class="price-amount">{{ textbook.dailyRent }}</span>
                       <span class="price-unit">/天</span>
                     </div>
                   </div>
                </div>
                
                <div class="action-buttons-row" v-if="textbook.status === 'available' || (textbook.rentEnabled && textbook.status === 'rented')">
                  <template v-if="textbook.status === 'available'">
                    <el-button color="#67c23a" class="action-btn" @click="handleBuy('donate')" v-if="textbook.type === 'donate'">
                      <el-icon><Present /></el-icon> 立即领取
                    </el-button>
                    <template v-else>
                      <el-button color="#ff6b00" class="action-btn" @click="handleAddToCart" v-if="textbook.price">
                        <el-icon><Plus /></el-icon> 加入购物车
                      </el-button>
                      <el-button color="#f56c6c" class="action-btn" @click="handleBuy('sell')" v-if="textbook.price">
                        <el-icon><ShoppingCart /></el-icon> 立即购买
                      </el-button>
                      <el-button color="#409eff" class="action-btn" @click="handleBuy('rent')" v-if="textbook.rentEnabled">
                        <el-icon><Timer /></el-icon> 立即租赁
                      </el-button>
                    </template>
                  </template>
                  <template v-else-if="textbook.status === 'rented'">
                     <el-button type="info" class="action-btn" disabled>
                        <el-icon><Timer /></el-icon> 出租中
                     </el-button>
                  </template>
                </div>
                <div class="status-display" v-else>
                   <el-tag size="large" type="info">{{ textbook.status === 'sold' ? '已售出' : (textbook.status === 'rented' ? '出租中' : '商品已下架') }}</el-tag>
                </div>
              </div>

              <!-- Reviews Section (Inline) -->
              <div class="reviews-section-inline">
                 <div class="section-title">
                   <el-icon><ChatDotRound /></el-icon> 卖家评价 ({{ sellerReviews.length }})
                 </div>
                 <div class="reviews-list-inline">
                    <el-empty v-if="sellerReviews.length === 0" description="暂无评价" :image-size="60" />
                    <div v-for="review in sellerReviews.slice(0, 2)" :key="review.id" class="review-item-inline">
                        <el-avatar :size="32" :src="review.userAvatar" :style="{ background: '#e6a23c' }">
                            {{ review.userName?.charAt(0) }}
                        </el-avatar>
                        <div class="review-content-inline">
                            <div class="review-header-inline">
                                <span class="reviewer-name">{{ review.userName || '匿名用户' }}</span>
                                <span class="review-date">{{ new Date(review.createdAt).toLocaleDateString() }}</span>
                            </div>
                            <el-rate v-model="review.rating" disabled size="small" />
                            <div class="review-text-inline">{{ review.content }}</div>
                        </div>
                    </div>
                 </div>
              </div>

            </div>
          </el-col>
        </el-row>
      </div>

      <!-- Tabs Section -->
      <div class="details-tabs">
        <el-tabs v-model="activeTab" class="custom-tabs">
          <el-tab-pane name="detail">
             <template #label>
               <span class="tab-label"><el-icon><Reading /></el-icon> 商品详情</span>
             </template>
             <div class="tab-content description-content">
               <h3 class="content-title">内容简介</h3>
               <p class="description-text">{{ textbook.description || '暂无描述' }}</p>
             </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 80vh;
}

.ml-2 {
  margin-left: 8px;
}

.breadcrumb-container {
  margin-bottom: 20px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Product Card */
.product-card {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
}

.image-gallery {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  aspect-ratio: 3/4;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.image-gallery:hover .main-image {
  transform: scale(1.02);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(2px);
}

.status-badge {
  background: #303133;
  color: #fff;
  padding: 10px 24px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 30px;
}

/* Product Info */
.product-info {
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.product-title {
  font-size: 32px;
  font-weight: bold;
  color: #000;
  margin: 0;
  line-height: 1.2;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.action-icon {
  font-size: 24px;
  color: #909399;
  cursor: pointer;
  transition: color 0.2s;
}

.action-icon:hover {
  color: #409eff;
}

.action-icon.active {
  color: #e6a23c;
}

/* Meta Info */
.meta-info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 30px;
}

.meta-row {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.meta-row .label {
  color: #333;
  font-weight: bold;
  width: 70px;
}

.meta-row .value {
  color: #333;
}

.condition-highlight {
  color: #00c853;
  font-weight: bold;
}

/* Seller Card */
.seller-card-new {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.seller-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seller-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.seller-name {
  font-weight: bold;
  font-size: 16px;
  color: #303133;
}

.seller-sub {
  font-size: 12px;
  color: #909399;
}

.contact-btn {
  color: #409eff;
  border-color: #409eff;
  background: white;
}
.contact-btn:hover {
  background: #ecf5ff;
}

/* Purchase Box */
.purchase-box {
  background: #fbfbfb;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  transition: all 0.3s;
}

.purchase-box.donate-theme {
  background: linear-gradient(120deg, #e0f2f1 0%, #f1f8e9 100%);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.02);
  position: relative;
  overflow: hidden;
}

.donate-elegant-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
  width: 100%;
}

.donate-icon-box {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #4db6ac;
  box-shadow: 0 4px 12px rgba(77, 182, 172, 0.15);
}

.donate-info-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.donate-title {
  font-size: 18px;
  font-weight: bold;
  color: #00796b;
  letter-spacing: 0.5px;
}

.donate-slogan {
  font-size: 13px;
  color: #5d8b85;
}

.donate-price-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.free-text {
  font-size: 28px;
  font-weight: 800;
  color: #009688;
  letter-spacing: 1px;
}

.original-price {
  font-size: 14px;
  color: #a7b6b4;
  text-decoration: line-through;
}

.price-display {
  margin-bottom: 20px;
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 20px;
}

.price-item {
  display: flex;
  align-items: baseline;
}

.price-label {
  font-size: 14px;
  color: #606266;
  margin-right: 8px;
}

.price-symbol {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
  margin-right: 2px;
}

.price-amount {
  font-size: 36px;
  color: #f56c6c;
  font-weight: bold;
  line-height: 1;
}

.action-buttons-row {
  display: flex;
  gap: 16px;
}

.action-btn {
  flex: 1;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  border: none;
  color: white;
}

.cart-btn {
  background: #ff6b00;
}

.buy-btn {
  background: #f56c6c;
}

/* Reviews Inline */
.reviews-section-inline {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-item-inline {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.review-item-inline:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.review-content-inline {
  flex: 1;
}

.review-header-inline {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.reviewer-name {
  font-size: 14px;
  color: #303133;
}

.review-date {
  font-size: 12px;
  color: #c0c4cc;
}

.review-text-inline {
  font-size: 14px;
  color: #606266;
  margin-top: 4px;
  line-height: 1.4;
}

/* Tabs */
.details-tabs {
  background: #fff;
  border-radius: 16px;
  padding: 20px 30px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
}

.tab-content {
  padding: 20px 0;
}

.content-title {
  font-size: 18px;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}

.description-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
}

/* Reviews */
.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.rating-summary {
  display: flex;
  align-items: center;
  gap: 20px;
}

.rating-score {
  font-size: 48px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.rating-stars {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rating-count {
  font-size: 12px;
  color: #909399;
}

.review-item {
  display: flex;
  gap: 20px;
  padding: 24px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-main {
  flex: 1;
}

.review-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.username {
  font-weight: 600;
  color: #303133;
}

.time {
  margin-left: auto;
  font-size: 12px;
  color: #c0c4cc;
}

.review-text {
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .product-info {
    padding-left: 0;
    padding-top: 24px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .secondary-actions {
    grid-template-columns: 1fr;
  }
}
</style>
