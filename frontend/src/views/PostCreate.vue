<script setup lang="ts">
import { ref, reactive } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const submitting = ref(false)
const form = reactive({
  title: '',
  type: '',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const handlePublish = async () => {
  if (!form.title || !form.type || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  const userStr = localStorage.getItem('user')
  if (!userStr) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  const user = JSON.parse(userStr)

  submitting.value = true
  try {
    const res = await axios.post('/api/posts', {
      ...form,
      userId: user.id
    })
    if (res.data.code === 200) {
      ElMessage.success('发布成功')
      router.push('/posts')
    } else {
      ElMessage.error(res.data.message || '发布失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="post-create-container">
    <el-page-header @back="router.back()" content="发布帖子" class="mb-4" />
    
    <el-card>
      <el-form :model="form" :rules="rules" label-width="80px" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="提问" value="question" />
            <el-option label="交易" value="transaction" />
            <el-option label="测评" value="review" />
            <el-option label="讨论" value="discussion" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入内容" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handlePublish">发布</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.post-create-container {
  max-width: 800px;
  margin: 0 auto;
}
.mb-4 {
  margin-bottom: 20px;
}
</style>
