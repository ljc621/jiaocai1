<template>
  <div class="login-wrapper">
    <div class="login-left">
      <div class="brand-intro">
        <h1>教材循环平台</h1>
        <p>让每一本教材都能找到新的主人</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Refresh /></el-icon>
            <span>环保置换</span>
          </div>
          <div class="feature-item">
            <el-icon><Money /></el-icon>
            <span>高价回收</span>
          </div>
          <div class="feature-item">
            <el-icon><Reading /></el-icon>
            <span>低价淘书</span>
          </div>
        </div>
      </div>
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
    </div>
    
    <div class="login-right">
      <div class="login-form-container">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p class="subtitle">请登录您的账号以继续</p>
        </div>
        
        <el-form :model="form" size="large" class="login-form">
          <el-form-item>
            <el-input 
              v-model="form.phone" 
              placeholder="手机号码" 
              :prefix-icon="User" 
            />
          </el-form-item>
          <el-form-item>
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="密码" 
              :prefix-icon="Lock" 
              show-password 
            />
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox>记住我</el-checkbox>
            <el-button link type="primary">忘记密码?</el-button>
          </div>

          <el-form-item>
            <el-button type="primary" :loading="loading" class="submit-btn" @click="handleLogin">
              立即登录
            </el-button>
          </el-form-item>
          
          <div class="form-footer">
            <span>还没有账号?</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
            <el-divider direction="vertical" />
            <router-link to="/home" class="guest-link">随便看看</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Refresh, Money, Reading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const form = reactive({
  phone: '',
  password: ''
})

const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await axios.post('/api/auth/login', form)
    if (res.data.code === 200) {
      ElMessage.success('登录成功')
      localStorage.setItem('user', JSON.stringify(res.data.data))
      router.push('/textbooks')
    } else {
      ElMessage.error(res.data.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  min-height: 100vh;
  background-color: #fff;
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 80px;
  color: white;
  overflow: hidden;
}

.brand-intro {
  z-index: 2;
  position: relative;
}

.brand-intro h1 {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 24px;
  letter-spacing: 2px;
}

.brand-intro p {
  font-size: 20px;
  opacity: 0.9;
  margin-bottom: 60px;
  font-weight: 300;
}

.features {
  display: flex;
  gap: 40px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.feature-item .el-icon {
  font-size: 32px;
  background: rgba(255, 255, 255, 0.2);
  padding: 16px;
  border-radius: 50%;
  backdrop-filter: blur(10px);
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 600px;
  height: 600px;
  bottom: -200px;
  left: -200px;
}

.login-right {
  width: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  padding: 40px;
}

.login-form-container {
  width: 100%;
  max-width: 360px;
}

.form-header {
  margin-bottom: 40px;
  text-align: left;
}

.form-header h2 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 12px;
  font-weight: 700;
}

.subtitle {
  color: #909399;
  font-size: 16px;
}

.login-form :deep(.el-input__wrapper) {
  padding: 12px 16px;
  border-radius: 8px;
  background-color: #f5f7fa;
  box-shadow: none;
  border: 1px solid transparent;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background-color: white;
  border-color: #409eff;
  box-shadow: 0 0 0 1px #409eff inset;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 1px;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  color: #606266;
  font-size: 14px;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 600;
  margin-left: 8px;
}

.guest-link {
  color: #909399;
  text-decoration: none;
  margin-left: 8px;
}

.guest-link:hover {
  color: #409eff;
}

@media (max-width: 900px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
  }
}
</style>
