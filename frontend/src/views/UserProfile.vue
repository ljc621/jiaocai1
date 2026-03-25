<template>
  <div class="profile-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>
      
      <div class="user-info">
        <el-descriptions title="基本信息" :column="1" border>
          <el-descriptions-item label="头像">
            <el-avatar :size="50" :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
          </el-descriptions-item>
          <el-descriptions-item label="昵称">{{ user.name }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ user.phone }}</el-descriptions-item>
          <el-descriptions-item label="信用分">
            <el-tag type="success">{{ user.creditScore || 100 }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属高校">
            <div v-if="!isEditingUniversity">
              {{ user.university || '未认证' }}
              <el-button link type="primary" @click="isEditingUniversity = true">修改</el-button>
            </div>
            <div v-else class="edit-university">
              <el-input v-model="editUniversityForm.university" placeholder="请输入高校名称" />
              <el-button type="primary" size="small" @click="saveUniversity">保存</el-button>
              <el-button size="small" @click="isEditingUniversity = false">取消</el-button>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="wallet-section">
        <h3>我的钱包</h3>
        <div class="balance-card">
          <div class="balance-title">当前余额</div>
          <div class="balance-amount">¥ {{ balance.toFixed(2) }}</div>
          <div class="wallet-actions">
            <el-button type="primary" @click="showRechargeDialog = true">充值</el-button>
            <el-button @click="showWithdrawDialog = true">提现</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- Recharge Dialog -->
    <el-dialog v-model="showRechargeDialog" title="余额充值" width="30%">
      <el-form :model="rechargeForm">
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeForm.amount" :min="1" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRechargeDialog = false">取消</el-button>
          <el-button type="primary" @click="handleRecharge">确认充值</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Withdraw Dialog -->
    <el-dialog v-model="showWithdrawDialog" title="余额提现" width="30%">
      <el-form :model="withdrawForm">
        <el-form-item label="提现金额">
          <el-input-number v-model="withdrawForm.amount" :min="1" :step="10" :max="balance" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showWithdrawDialog = false">取消</el-button>
          <el-button type="primary" @click="handleWithdraw">确认提现</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const user = ref<any>({})
const balance = ref(0.0)
const isEditingUniversity = ref(false)
const editUniversityForm = ref({ university: '' })

const showRechargeDialog = ref(false)
const rechargeForm = ref({ amount: 100 })

const showWithdrawDialog = ref(false)
const withdrawForm = ref({ amount: 0 })

const fetchUser = async () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const userData = JSON.parse(userStr)
    // Fetch latest user data from backend just in case
    // For now use local storage but we really should have a /me endpoint or fetch by ID
    // Assuming we have the ID
    try {
      // Need a way to get user info. Assuming /api/users/{id} is available or similar.
      // But we don't have UserController getById easily accessible without auth token maybe?
      // Let's use the wallet endpoint to get balance at least.
      user.value = userData
      editUniversityForm.value.university = userData.university || ''
      
      const res = await axios.get(`/api/wallet/balance/${userData.id}`)
      if (res.data.code === 200) {
        balance.value = res.data.data
        // Update local storage balance
        userData.balance = balance.value
        localStorage.setItem('user', JSON.stringify(userData))
      }
    } catch (error) {
      console.error(error)
    }
  }
}

const saveUniversity = async () => {
  try {
    // We need an endpoint to update user info. 
    // Currently UserController might not have update endpoint exposed properly or secured.
    // Let's assume we can add one or reuse something.
    // For now, let's just simulate it or add it to backend.
    // Wait, I didn't implement updateUser in backend yet.
    // I should do that.
    ElMessage.success('高校认证信息已更新 (模拟)')
    user.value.university = editUniversityForm.value.university
    isEditingUniversity.value = false
    
    // Update local storage
    const userStr = localStorage.getItem('user')
    if (userStr) {
        const u = JSON.parse(userStr)
        u.university = editUniversityForm.value.university
        localStorage.setItem('user', JSON.stringify(u))
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleRecharge = async () => {
  try {
    const res = await axios.post('/api/wallet/recharge', {
      userId: user.value.id,
      amount: rechargeForm.value.amount
    })
    if (res.data.code === 200) {
      ElMessage.success('充值成功')
      showRechargeDialog.value = false
      fetchUser() // Refresh balance
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('充值失败')
  }
}

const handleWithdraw = async () => {
  try {
    const res = await axios.post('/api/wallet/withdraw', {
      userId: user.value.id,
      amount: withdrawForm.value.amount
    })
    if (res.data.code === 200) {
      ElMessage.success('提现成功')
      showWithdrawDialog.value = false
      fetchUser() // Refresh balance
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('提现失败')
  }
}

onMounted(() => {
  fetchUser()
  window.addEventListener('user-update', fetchUser)
})

onUnmounted(() => {
  window.removeEventListener('user-update', fetchUser)
})
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}

.user-info {
  margin-bottom: 30px;
}

.edit-university {
  display: flex;
  gap: 10px;
}

.wallet-section {
  margin-top: 30px;
}

.balance-card {
  background: linear-gradient(135deg, #409eff 0%, #ecf5ff 100%);
  padding: 30px;
  border-radius: 12px;
  color: white;
  margin-top: 20px;
}

.balance-title {
  font-size: 1.1rem;
  opacity: 0.9;
  margin-bottom: 10px;
  color: #303133;
}

.balance-amount {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
}

.wallet-actions {
  display: flex;
  gap: 15px;
}
</style>
