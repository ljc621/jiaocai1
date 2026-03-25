<script setup lang="ts">
import { ArrowRight } from '@element-plus/icons-vue'
import type { Textbook } from '../types'

defineProps<{
  item: Textbook
}>()

defineEmits(['click'])

const getTypeLabel = (type?: string) => {
  switch (type) {
    case 'rent': return '租赁'
    case 'donate': return '捐赠'
    default: return '出售'
  }
}

const getTypeClass = (type?: string) => {
  switch (type) {
    case 'rent': return 'rent-type'
    case 'donate': return 'donate-type'
    default: return 'sell-type'
  }
}
</script>

<template>
  <div class="textbook-card" @click="$emit('click', item.id)">
    <div class="card-image-wrapper">
      <img :src="item.cover || 'https://placehold.co/200x260'" class="image" />
      <div class="card-overlay">
        <el-button type="primary" round size="small">查看详情</el-button>
      </div>
      <div class="condition-badge">{{ item.condition }}</div>
      <div class="status-badge" v-if="item.status === 'rented'">出租中</div>
      <div class="type-badge" :class="getTypeClass(item.type)">
        {{ getTypeLabel(item.type) }}
      </div>
    </div>
    
    <div class="card-content">
      <h3 class="title" :title="item.title">{{ item.title }}</h3>
      <p class="author">{{ item.author }}</p>
      
      <div class="card-footer">
        <div class="price-wrapper">
          <template v-if="item.type === 'donate'">
            <span class="price free">免费领取</span>
          </template>
          <template v-else>
            <span class="currency">¥</span>
            <span class="price">{{ item.type === 'rent' ? item.dailyRent : item.price }}</span>
            <span class="unit" v-if="item.type === 'rent'">/天</span>
          </template>
        </div>
        <div class="view-more">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.textbook-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #f0f0f0;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.textbook-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
  border-color: transparent;
}

.card-image-wrapper {
  position: relative;
  padding-top: 130%; /* Aspect ratio container */
  overflow: hidden;
  background: #f8f9fa;
}

.image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.textbook-card:hover .image {
  transform: scale(1.05);
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.textbook-card:hover .card-overlay {
  opacity: 1;
}

.condition-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.95);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  backdrop-filter: blur(4px);
}

.status-badge {
  position: absolute;
  top: 40px;
  right: 10px;
  background: rgba(245, 108, 108, 0.95);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  backdrop-filter: blur(4px);
}

.type-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.sell-type {
  background: linear-gradient(135deg, #409eff, #36cfc9);
}

.rent-type {
  background: linear-gradient(135deg, #e6a23c, #f56c6c);
}

.card-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 44px;
}

.author {
  font-size: 13px;
  color: #909399;
  margin: 0 0 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-footer {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f5f7fa;
}

.price-wrapper {
  color: #f56c6c;
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 14px;
  margin-right: 2px;
}

.price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
  line-height: 1;
}

.price.free {
  color: #67c23a;
  font-size: 16px;
}

.unit {
  font-size: 12px;
  color: #909399;
  margin-left: 2px;
}

.view-more {
  color: #c0c4cc;
  transition: color 0.3s;
}

.textbook-card:hover .view-more {
  color: #409eff;
}
</style>
