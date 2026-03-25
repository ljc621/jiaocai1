<template>
  <div class="home-container">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">让教材在校园里<br>更有价值地流转</h1>
        <p class="hero-subtitle">二手买卖 · 信用租赁 · 爱心捐赠 · 环保置换</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round class="action-btn" @click="$router.push('/textbooks')">立即找书</el-button>
          <el-button ghost size="large" round class="action-btn outline-btn" @click="$router.push('/textbooks/create')">发布闲置</el-button>
        </div>
      </div>
      <div class="hero-image">
        <el-icon :size="200" color="rgba(255,255,255,0.8)"><Reading /></el-icon>
      </div>
    </div>

    <!-- Features Section -->
    <div class="section-container">
      <h2 class="section-title">多元化的流转模式</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Goods /></el-icon>
          </div>
          <h3>二手交易</h3>
          <p>平台担保交易，资金安全有保障，支持议价与一口价。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Refresh /></el-icon>
          </div>
          <h3>信用租赁</h3>
          <p>按天/月租赁，租金低廉，到期自动提醒，支持续租。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Present /></el-icon>
          </div>
          <h3>爱心捐赠</h3>
          <p>捐赠闲置教材获取积分，助力环保，获得捐赠证书。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><RefreshRight /></el-icon>
          </div>
          <h3>环保置换</h3>
          <p>以书换书，物尽其用，发挥教材最大价值。</p>
        </div>
      </div>
    </div>

    <!-- Hot Textbooks Section -->
    <div class="section-container">
      <div class="section-header">
        <h2 class="section-title">热门教材推荐</h2>
        <el-button link type="primary" @click="$router.push('/textbooks')">查看更多 <el-icon><ArrowRight /></el-icon></el-button>
      </div>
      
      <div class="textbook-grid" v-loading="loading">
        <div v-for="book in textbooks" :key="book.id">
           <TextbookCard :item="book" @click="router.push(`/textbooks/${book.id}`)" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Reading, Goods, Refresh, Present, RefreshRight, ArrowRight } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { Textbook } from '../types'
import TextbookCard from '../components/TextbookCard.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const textbooks = ref<Textbook[]>([])

const fetchHotTextbooks = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/textbooks')
    if (res.data.code === 200) {
      // Take first 3 items as "hot" recommendations
      textbooks.value = res.data.data.slice(0, 3)
    }
  } catch (error) {
    console.error('Failed to fetch textbooks', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHotTextbooks()
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.hero-banner {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  padding: 100px 10%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 500px;
  position: relative;
  overflow: hidden;
}

.hero-banner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgdmlld0JveD0iMCAwIDQwIDQwIj48ZyBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxwYXRoIGQ9IkQwIDQwTDQwIDBIODBWODBIMHoiIGZpbGw9IiNmZmYiIGZpbGwtb3BhY2l0eT0iMC4wNSIvPjwvZz48L3N2Zz4=');
  opacity: 0.1;
}

.hero-content {
  max-width: 600px;
  z-index: 1;
}

.hero-title {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 24px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.2);
}

.hero-subtitle {
  font-size: 1.4rem;
  opacity: 0.95;
  margin-bottom: 48px;
  font-weight: 300;
  letter-spacing: 1px;
}

.hero-actions {
  display: flex;
  gap: 24px;
}

.action-btn {
  font-weight: 600;
  padding: 24px 48px;
  font-size: 1.1rem;
  transition: transform 0.3s;
}

.action-btn:hover {
  transform: translateY(-2px);
}

.outline-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.outline-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border-color: white;
}

.hero-image {
  z-index: 1;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
  100% { transform: translateY(0px); }
}

.section-container {
  max-width: 1200px;
  margin: 80px auto;
  padding: 0 24px;
}

.section-title {
  font-size: 2rem;
  color: #303133;
  text-align: center;
  margin-bottom: 60px;
  font-weight: 700;
  position: relative;
}

.section-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  background: #409eff;
  margin: 16px auto 0;
  border-radius: 2px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.section-header .section-title {
  margin-bottom: 0;
  text-align: left;
}

.section-header .section-title::after {
  margin: 16px 0 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 48px 32px;
  text-align: center;
  transition: all 0.4s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f2f5;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  border-color: #ecf5ff;
}

.feature-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 24px;
  background: #ecf5ff;
  width: 96px;
  height: 96px;
  line-height: 96px;
  border-radius: 50%;
  margin: 0 auto 24px;
  transition: all 0.4s ease;
}

.feature-card:hover .feature-icon {
  background: #409eff;
  color: white;
  transform: scale(1.1);
}

.feature-card h3 {
  font-size: 1.4rem;
  color: #303133;
  margin-bottom: 16px;
  font-weight: 600;
}

.feature-card p {
  font-size: 1rem;
  color: #606266;
  line-height: 1.8;
}

.textbook-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 32px;
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: repeat(1, 1fr); /* Single column on mobile */
  }
  
  .hero-banner {
    flex-direction: column;
    text-align: center;
    padding: 60px 24px;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-actions {
    justify-content: center;
    flex-direction: column;
  }
  
  .hero-image {
    display: none;
  }
}
</style>