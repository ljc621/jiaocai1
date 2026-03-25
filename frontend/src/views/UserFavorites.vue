<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { StarFilled, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const favorites = ref<any[]>([])

const fetchFavorites = () => {
    const favStr = localStorage.getItem('favorites')
    if (favStr) {
        try {
            favorites.value = JSON.parse(favStr)
        } catch (e) {
            favorites.value = []
        }
    } else {
        favorites.value = []
    }
}

const removeFavorite = (item: any) => {
    ElMessageBox.confirm(
        '确定要取消收藏该教材吗？',
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(() => {
        favorites.value = favorites.value.filter(f => f.id !== item.id)
        localStorage.setItem('favorites', JSON.stringify(favorites.value))
        ElMessage.success('已取消收藏')
        window.dispatchEvent(new Event('favorites-update'))
    })
}

const goToDetail = (id: string) => {
    router.push(`/textbooks/${id}`)
}

onMounted(() => {
    fetchFavorites()
    window.addEventListener('favorites-update', fetchFavorites)
})

onUnmounted(() => {
    window.removeEventListener('favorites-update', fetchFavorites)
})
</script>

<template>
  <div class="favorites-container">
    <div class="header">
      <h3><el-icon><StarFilled /></el-icon> 我的收藏</h3>
    </div>
    
    <div v-if="favorites.length === 0" class="empty-state">
      <div class="empty-icon">
        <el-icon><StarFilled /></el-icon>
      </div>
      <h4>暂无收藏的教材</h4>
      <el-button link type="primary" @click="router.push('/textbooks')">去逛逛</el-button>
    </div>

    <div v-else class="favorites-list">
        <el-card v-for="item in favorites" :key="item.id" class="favorite-item" shadow="hover">
            <div class="item-content" @click="goToDetail(item.id)">
                <img :src="item.cover || 'https://placehold.co/100'" class="item-img" />
                <div class="item-info">
                    <h3 class="item-title">{{ item.title }}</h3>
                    <div class="item-meta">
                        <span class="price">¥{{ item.price }}</span>
                        <el-tag size="small" class="ml-2">{{ item.condition }}</el-tag>
                    </div>
                    <p class="author">{{ item.author }}</p>
                </div>
            </div>
            <div class="item-actions">
                <el-button type="danger" :icon="Delete" circle @click.stop="removeFavorite(item)" />
            </div>
        </el-card>
    </div>
  </div>
</template>

<style scoped>
.favorites-container {
    padding: 20px;
    max-width: 800px;
    margin: 0 auto;
}

.header {
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #EBEEF5;
}

.header h3 {
    margin: 0;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    color: #303133;
}

.header .el-icon {
    color: #F56C6C;
}

.empty-state {
    background: white;
    border-radius: 8px;
    padding: 60px 0;
    text-align: center;
    border: 1px solid #EBEEF5;
    min-height: 300px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
}

.empty-icon {
    font-size: 48px;
    color: #DCDFE6;
    margin-bottom: 16px;
}

.empty-state h4 {
    margin: 0 0 8px 0;
    color: #909399;
    font-weight: normal;
}

.favorites-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.favorite-item {
    cursor: pointer;
    transition: all 0.3s;
}

.favorite-item:hover {
    transform: translateY(-2px);
}

.item-content {
    display: flex;
    gap: 15px;
    flex: 1;
}

.favorite-item :deep(.el-card__body) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
}

.item-img {
    width: 80px;
    height: 100px;
    object-fit: cover;
    border-radius: 4px;
}

.item-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
}

.item-title {
    margin: 0;
    font-size: 16px;
    color: #303133;
}

.item-meta {
    display: flex;
    align-items: center;
}

.price {
    color: #F56C6C;
    font-weight: bold;
    font-size: 16px;
}

.ml-2 {
    margin-left: 8px;
}

.author {
    margin: 0;
    color: #909399;
    font-size: 13px;
}

.item-actions {
    margin-left: 15px;
}
</style>
