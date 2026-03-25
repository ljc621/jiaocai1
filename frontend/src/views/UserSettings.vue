<script setup lang="ts">
import { reactive, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadProps } from 'element-plus'
import { ArrowLeft, User, SwitchButton, Plus, CircleCheckFilled, WarningFilled } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const currentUser = reactive({
  id: '',
  role: ''
})

const verificationStatus = ref<any>(null)

const form = reactive({
  avatar: '',
  nickname: '',
  school: '',
  newPassword: '',
  realName: '',
  idCard: '',
  verified: false
})

const verifyDialogVisible = reactive({
  visible: false,
  name: '',
  id: '',
  materialUrl: ''
})

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    currentUser.id = user.id
    currentUser.role = user.role
    
    form.avatar = user.avatar
    form.nickname = user.name
    form.school = user.university || '' // Mapped to university in DB
    form.verified = user.verified || false
    
    fetchVerificationStatus()
  }
})

const fetchVerificationStatus = async () => {
  try {
    const res = await axios.get(`/api/verification/status?userId=${currentUser.id}`)
    if (res.data.code === 200) {
      verificationStatus.value = res.data.data
    }
  } catch (error) {
    console.error('Failed to fetch verification status')
  }
}

const goBack = () => {
  router.back()
}

const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    localStorage.removeItem('user')
    ElMessage.success('已退出登录')
    router.push('/login')
  })
}

const handleSave = async () => {
  try {
    const updateData: any = {
      id: currentUser.id,
      name: form.nickname,
      university: form.school,
      avatar: form.avatar
    }
    
    if (form.newPassword) {
      updateData.password = form.newPassword
    }

    const res = await axios.put('/api/users', updateData)
    
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      // Update local storage
      const userStr = localStorage.getItem('user')
      if (userStr) {
        const user = JSON.parse(userStr)
        const updatedUser = { ...user, ...updateData }
        // Don't save password to local storage
        delete updatedUser.password
        localStorage.setItem('user', JSON.stringify(updatedUser))
        
        // Notify other components
        window.dispatchEvent(new Event('user-update'))
      }
    } else {
      ElMessage.error(res.data.msg || '保存失败')
    }
  } catch (e) {
    ElMessage.error('网络错误，保存失败')
  }
}

const handleAvatarSuccess = (res: any) => {
  if (res.code === 200) {
    form.avatar = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('头像上传失败')
  }
}

const beforeAvatarUpload = (rawFile: any) => {
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('Avatar picture size can not exceed 2MB!')
    return false
  }
  return true
}

const handleVerify = async () => {
  if (!verifyDialogVisible.name || !verifyDialogVisible.id) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    const payload = {
      userId: currentUser.id,
      realName: verifyDialogVisible.name,
      studentId: verifyDialogVisible.id,
      materialUrl: verifyDialogVisible.materialUrl
    }
    
    const res = await axios.post('/api/verification/apply', payload)
    
    if (res.data.code === 200) {
      ElMessage.success('提交成功，等待审核')
      verifyDialogVisible.visible = false
      fetchVerificationStatus()
    } else {
      ElMessage.error(res.data.msg || '提交失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  }
}

const handleUploadSuccess: UploadProps['onSuccess'] = (response, uploadFile) => {
  if (response.code === 200) {
    verifyDialogVisible.materialUrl = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('Avatar picture must be JPG format!')
    return false
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('Avatar picture size can not exceed 2MB!')
    return false
  }
  return true
}
</script>

<template>
  <div class="settings-container">
    <div class="header">
      <div class="left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <h3>账号设置</h3>
      </div>
      <div class="right" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        <span>退出登录</span>
      </div>
    </div>

    <div class="content">
      <div class="auth-status" :class="{ verified: form.verified }">
        <div class="status-icon">
          <el-icon v-if="form.verified"><CircleCheckFilled /></el-icon>
          <el-icon v-else><WarningFilled /></el-icon>
        </div>
        <div class="status-info">
          <div class="status-title">{{ form.verified ? '已完成实名认证' : '未完成实名认证' }}</div>
          <div class="status-desc">
            <template v-if="form.verified">
              您已通过实名认证，享有完整权益
            </template>
            <template v-else-if="verificationStatus && verificationStatus.status === 'pending'">
              您的认证申请正在审核中，请耐心等待
            </template>
            <template v-else-if="verificationStatus && verificationStatus.status === 'rejected'">
              认证被拒绝，请重新提交
            </template>
            <template v-else>
              为了保障交易安全，建议您完成实名认证
            </template>
          </div>
        </div>
        <el-button 
          v-if="!form.verified && (!verificationStatus || verificationStatus.status !== 'pending')" 
          type="primary" 
          size="small"
          @click="verifyDialogVisible.visible = true"
        >
          去认证
        </el-button>
      </div>

      <div class="avatar-section">
        <div class="label">头像</div>
        <div class="avatar-wrapper">
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img :src="form.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="avatar" />
            <div class="upload-overlay">
              <el-icon><Plus /></el-icon>
            </div>
          </el-upload>
        </div>
      </div>

      <el-form label-position="top" class="settings-form">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        
        <el-form-item label="学校">
          <el-input v-model="form.school" />
        </el-form-item>

        <div class="section-title">安全设置</div>
        
        <el-form-item label="新密码">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="如果不修改密码请留空" />
        </el-form-item>

        <el-form-item class="submit-item">
          <el-button type="primary" class="save-btn" @click="handleSave">保存更改</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-dialog v-model="verifyDialogVisible.visible" title="实名认证" width="400px">
      <el-form label-position="top">
        <el-form-item label="真实姓名">
          <el-input v-model="verifyDialogVisible.name" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="verifyDialogVisible.id" placeholder="请输入18位身份证号" />
        </el-form-item>
        <el-form-item label="证明材料" required>
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="verifyDialogVisible.materialUrl" :src="verifyDialogVisible.materialUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">请上传学生证/工作证等证明材料</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="verifyDialogVisible.visible = false">取消</el-button>
          <el-button type="primary" @click="handleVerify">提交认证</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.settings-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.right {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #666;
}

.content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.auth-status {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 30px;
  gap: 15px;
}

.auth-status.verified {
  background: #f0f9eb;
}

.status-icon {
  font-size: 24px;
}

.status-icon .el-icon {
  color: #e6a23c;
}

.verified .status-icon .el-icon {
  color: #67c23a;
}

.status-info {
  flex: 1;
}

.status-title {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 4px;
}

.status-desc {
  font-size: 14px;
  color: #909399;
}

.avatar-section {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 30px;
  border-bottom: 1px solid #eee;
}

.label {
  width: 100px;
  font-weight: bold;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.avatar-wrapper:hover .edit-mask {
  opacity: 1;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.edit-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.form-section {
  max-width: 500px;
}

.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
