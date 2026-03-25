<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import axios from 'axios'
import { Search, Filter, Reading, ArrowRight } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import TextbookCard from '../components/TextbookCard.vue'

const router = useRouter()
const route = useRoute()
const textbooks = ref<any[]>([])
const loading = ref(false)
const searchForm = reactive({
  keyword: '',
  category: '',
  type: ''
})

const categories = [
  '计算机', '数学', '物理', '文学', '历史', '经济', '英语', '其他'
]

const fetchTextbooks = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/textbooks', { params: searchForm })
    if (res.data.code === 200) {
      textbooks.value = res.data.data
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const goToDetail = (id: string) => {
  router.push(`/textbooks/${id}`)
}

watch(() => route.query.keyword, (newKeyword) => {
  if (newKeyword) {
    searchForm.keyword = newKeyword as string
    fetchTextbooks()
  } else if (route.path === '/textbooks' && newKeyword === undefined) {
    // If keyword is removed from query but we are still on textbooks page
    // (though usually handleSearch pushes a keyword)
    // We might want to clear it or keep it depending on UX.
    // For now, let's update if it's explicitly different.
  }
})

onMounted(() => {
  if (route.query.keyword) {
    searchForm.keyword = route.query.keyword as string
  }
  fetchTextbooks()
})
</script>

<template>
  <div class="page-container">
    <!-- Header / Search Section -->
    <div class="filter-section">
      <div class="filter-header">
        <h2 class="section-title">
          <el-icon><Reading /></el-icon> 教材广场
        </h2>
        <span class="section-subtitle">发现优质二手教材，开启智慧学习之旅</span>
      </div>
      
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索书名、作者、ISBN..."
          class="search-input"
          :prefix-icon="Search"
          size="large"
          clearable
          @keyup.enter="fetchTextbooks"
          @clear="fetchTextbooks"
        />
        
        <el-select 
          v-model="searchForm.type" 
          placeholder="交易类型" 
          clearable 
          size="large"
          class="filter-select"
          @change="fetchTextbooks"
        >
          <template #prefix><el-icon><Filter /></el-icon></template>
          <el-option label="全部类型" value="" />
          <el-option label="出售" value="sell" />
          <el-option label="出租" value="rent" />
          <el-option label="爱心捐赠" value="donate" />
        </el-select>
        
        <el-select 
          v-model="searchForm.category" 
          placeholder="全部分类" 
          clearable 
          size="large"
          class="filter-select"
          @change="fetchTextbooks"
        >
           <template #prefix><el-icon><Filter /></el-icon></template>
          <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
        </el-select>
        
        <el-button type="primary" size="large" @click="fetchTextbooks" class="search-btn">
          搜索
        </el-button>
      </div>
    </div>

    <!-- Results Section -->
    <div class="results-section" v-loading="loading">
      <el-empty v-if="!loading && textbooks.length === 0" description="暂无符合条件的教材" />
      
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in textbooks" :key="item.id" class="mb-4">
          <TextbookCard :item="item" @click="goToDetail(item.id)" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* Filter Section */
.filter-section {
  background: #fff;
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.filter-header {
  margin-bottom: 24px;
  text-align: center;
}

.section-title {
  margin: 0 0 8px;
  font-size: 28px;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.section-subtitle {
  color: #909399;
  font-size: 14px;
}

.search-bar {
  display: flex;
  gap: 16px;
  max-width: 900px;
  margin: 0 auto;
  flex-wrap: wrap;
}

.search-input {
  flex: 2;
  min-width: 200px;
}

.filter-select {
  flex: 1;
  min-width: 140px;
}

.search-btn {
  padding: 0 30px;
  font-weight: 600;
}

/* Card Styling removed - using component */

.mb-4 {
  margin-bottom: 24px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    gap: 12px;
  }
  
  .search-input, .filter-select, .search-btn {
    width: 100%;
  }
  
  .filter-section {
    padding: 20px;
  }
  
  .section-title {
    font-size: 24px;
  }
}
</style>
