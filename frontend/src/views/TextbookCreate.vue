<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { Plus, Search, Money } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const activeTab = ref('trade') // trade (sell/rent) or donate
const isRentMode = ref(false)

const isEditMode = computed(() => !!route.params.id)
const editId = computed(() => route.params.id as string)

const form = reactive<{
  title: string
  author: string
  publisher: string
  isbn: string
  price: number | undefined
  originalPrice: number | undefined
  dailyRent: number | undefined
  type: string
  condition: string
  category: string
  description: string
  cover: string
  status?: string
}>({
  title: '',
  author: '',
  publisher: '',
  isbn: '',
  price: undefined,
  originalPrice: undefined,
  dailyRent: undefined,
  type: 'sell',
  condition: '',
  category: '',
  description: '',
  cover: ''
})

const loading = ref(false)
const autoFilling = ref(false)

const conditions = ['全新', '良好', '一般', '较差']
const categories = ['计算机', '数学', '物理', '文学', '历史', '经济', '英语', '其他']

onMounted(async () => {
  if (isEditMode.value) {
    loading.value = true
    try {
      const res = await axios.get(`/api/textbooks/${editId.value}`)
      if (res.data.code === 200) {
        const data = res.data.data
        Object.assign(form, {
          title: data.title,
          author: data.author,
          publisher: data.publisher,
          isbn: data.isbn,
          price: data.price,
          originalPrice: data.originalPrice,
          dailyRent: data.dailyRent,
          type: data.type,
          condition: data.condition,
          category: data.category,
          description: data.description,
          cover: data.cover,
          status: data.status
        })

        if (data.type === 'donate') {
          activeTab.value = 'donate'
        } else {
          activeTab.value = 'trade'
          if (data.rentEnabled) {
            isRentMode.value = true
          }
        }
      }
    } catch (e) {
      ElMessage.error('获取详情失败')
    } finally {
      loading.value = false
    }
  }
})

const handleUploadSuccess = (response: any) => {
  if (response.code === 200) {
    form.cover = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败')
  }
}

const beforeUpload = (rawFile: any) => {
  if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

const handleAutoFill = async () => {
  if (!form.isbn || form.isbn.length !== 13) {
    ElMessage.warning('请输入正确的13位ISBN号码')
    return
  }
  
  autoFilling.value = true
  try {
    // Mock auto-fill for demo purposes as we don't have a real ISBN API connected
    // In a real app, call backend proxy to Douban or OpenLibrary
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Mock data based on ISBN (just for interaction feedback)
    if (form.isbn === '9787115545024') {
      form.title = '深度学习入门'
      form.author = '斋藤康毅'
      form.publisher = '人民邮电出版社'
      form.originalPrice = 59.00
      form.category = '计算机'
      ElMessage.success('识别成功')
    } else {
      ElMessage.info('未找到该ISBN信息，请手动填写')
    }
  } catch (e) {
    ElMessage.error('识别失败')
  } finally {
    autoFilling.value = false
  }
}

const generateRandomCover = () => {
  const seed = Math.floor(Math.random() * 10000)
  form.cover = `https://picsum.photos/seed/${seed}/300/400`
  ElMessage.success('已生成随机封面')
}

const handlePublish = async () => {
  // Validate
  if (!form.title) return ElMessage.warning('请输入教材名称')
  if (!isRentMode.value && !form.price && activeTab.value === 'trade') return ElMessage.warning('请输入出售价格')
  if (isRentMode.value && !form.dailyRent && activeTab.value === 'trade') return ElMessage.warning('请输入日租金')
  if (!form.condition) return ElMessage.warning('请选择成色')
  if (!form.category) return ElMessage.warning('请选择分类')

  // Auto-generate cover if missing
  if (!form.cover) {
    const seed = Math.floor(Math.random() * 10000)
    form.cover = `https://picsum.photos/seed/${seed}/300/400`
  }

  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  const user = JSON.parse(userStr)

  loading.value = true
  try {
    // Determine type based on mode
    let finalType = form.type
    if (activeTab.value === 'donate') {
      finalType = 'donate'
    } else if (isRentMode.value) {
      finalType = 'rent' // Mark as rent type primarily, but logic handles hybrid
    }

    const payload = {
      ...form,
      type: finalType,
      sellerId: user.id,
      status: isEditMode.value ? (form.status || 'available') : 'available',
      rentEnabled: isRentMode.value,
      dailyRent: isRentMode.value ? Number(form.dailyRent) : null,
      // Ensure numbers
      price: finalType === 'donate' ? 0 : Number(form.price),
      originalPrice: Number(form.originalPrice) || 0
    }
    
    if (isEditMode.value) {
      const updatePayload = { ...payload, id: editId.value }
      const res = await axios.put('/api/textbooks', updatePayload)
      if (res.data.code === 200) {
        ElMessage.success('修改成功')
        router.push('/profile/published')
      } else {
        ElMessage.error(res.data.message || '修改失败')
      }
    } else {
      const res = await axios.post('/api/textbooks', payload)
      if (res.data.code === 200) {
        ElMessage.success('发布成功')
        router.push('/textbooks')
      } else {
        ElMessage.error(res.data.message || '发布失败')
      }
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="publish-page">
    <div class="page-header">
      <h1>发布闲置教材</h1>
      <p>让闲置教材流动起来，帮助更多同学</p>
    </div>

    <div class="form-container">
      <div class="custom-tabs">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'trade' }"
          @click="activeTab = 'trade'"
        >
          出售 / 租赁
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'donate' }"
          @click="activeTab = 'donate'"
        >
          爱心捐赠
        </div>
      </div>

      <div class="isbn-section">
        <div class="isbn-title">
          <el-icon><Search /></el-icon>
          <span>ISBN 智能填表</span>
        </div>
        <div class="isbn-input-group">
          <el-input 
            v-model="form.isbn" 
            placeholder="请输入教材背面的13位ISBN号码（例如：9787115545024）" 
            class="isbn-input"
          />
          <el-button type="primary" :loading="autoFilling" @click="handleAutoFill">自动填充</el-button>
        </div>
        <p class="isbn-hint">输入ISBN后点击"自动填充"，可快速补全书名、作者、出版社等信息</p>
      </div>

      <el-form :model="form" label-position="top" class="main-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="教材名称" required>
              <el-input v-model="form.title" placeholder="例如：高等数学（第七版）" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" required>
              <el-input v-model="form.author" placeholder="例如：同济大学数学系" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出版社" required>
              <el-input v-model="form.publisher" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成色" required>
              <el-select v-model="form.condition" placeholder="选择成色" style="width: 100%">
                <el-option v-for="c in conditions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="分类" required>
          <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>

        <div class="price-section" v-if="activeTab === 'trade'">
          <div class="section-label" style="display: flex; justify-content: space-between; align-items: center;">
            <div><el-icon><Money /></el-icon> 价格设置</div>
            <el-checkbox v-model="isRentMode">开启租赁模式</el-checkbox>
          </div>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="出售价格 (¥)" :required="!isRentMode" :class="{ 'no-asterisk': isRentMode }">
                <el-input-number 
                  v-model="form.price" 
                  :precision="2" 
                  :step="0.1" 
                  :min="0" 
                  style="width: 100%" 
                  :controls="false"
                  class="price-input"
                  placeholder="0.00"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="原价 (¥)" required>
                <el-input-number 
                  v-model="form.originalPrice" 
                  :precision="2" 
                  :step="0.1" 
                  :min="0" 
                  style="width: 100%" 
                  :controls="false"
                  class="price-input"
                  placeholder="0.00"
                />
              </el-form-item>
            </el-col>
            <el-col :span="8" v-if="isRentMode">
              <el-form-item label="日租金 (¥/天)" required>
                <el-input-number 
                  v-model="form.dailyRent" 
                  :precision="2" 
                  :step="0.1" 
                  :min="0" 
                  style="width: 100%" 
                  :controls="false"
                  class="price-input"
                  placeholder="0.00"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div class="donate-info-section" v-if="activeTab === 'donate'">
           <div class="donate-reward-box">
             <h4>捐赠奖励</h4>
             <p>感谢您的爱心！捐赠此教材您将获得 <strong>100</strong> 环保积分。</p>
           </div>
        </div>

        <div class="upload-section">
          <div class="section-label">上传照片 (最多9张, 第一张为封面)</div>
          <el-upload
            action="/api/upload"
            list-type="picture-card"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            class="custom-uploader"
          >
            <img v-if="form.cover" :src="form.cover" class="uploaded-image" />
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <span>添加照片</span>
            </div>
          </el-upload>
        </div>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="handlePublish">
          立即发布
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.publish-page {
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 40px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 12px;
  font-weight: 600;
}

.page-header p {
  color: #606266;
  font-size: 16px;
}

.form-container {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.custom-tabs {
  display: flex;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24px;
  cursor: pointer;
  font-size: 16px;
  color: #606266;
  position: relative;
  transition: all 0.3s;
  background-color: #f5f7fa;
}

.tab-item:hover {
  color: #409eff;
  background-color: #ecf5ff;
}

.tab-item.active {
  color: #409eff;
  font-weight: 600;
  background-color: white;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #409eff;
}

.isbn-section {
  background: linear-gradient(to right, #ecf5ff, #fff);
  margin: 24px;
  padding: 24px;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.isbn-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  font-weight: 600;
  margin-bottom: 16px;
  font-size: 16px;
}

.isbn-input-group {
  display: flex;
  gap: 12px;
}

.isbn-input {
  flex: 1;
}

.isbn-hint {
  font-size: 13px;
  color: #909399;
  margin-top: 12px;
}

.main-form {
  padding: 20px 40px 40px;
}

.price-section {
  background-color: #f8f9fa;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 32px;
  border: 1px solid #ebeef5;
}

.section-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-section {
  margin-bottom: 40px;
}

.custom-uploader :deep(.el-upload--picture-card) {
  width: 140px;
  height: 140px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  transition: border-color 0.3s;
}

.custom-uploader :deep(.el-upload--picture-card:hover) {
  border-color: #409eff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  height: 100%;
}

.upload-placeholder .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
  color: #c0c4cc;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 18px;
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 1px;
}

.price-input :deep(.el-input__inner) {
  text-align: left;
}

.donate-info-section {
  background-color: #f0f9eb;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 32px;
  border: 1px solid #e1f3d8;
}

.donate-reward-box h4 {
  color: #67c23a;
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
}

.donate-reward-box p {
  color: #529b2e;
  margin: 0;
  font-size: 14px;
}

.no-asterisk :deep(.el-form-item__label::before) {
  display: none !important;
}
</style>