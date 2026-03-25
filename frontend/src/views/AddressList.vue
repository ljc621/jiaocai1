<template>
  <div class="address-list-container">
    <div class="header">
      <h3>我的地址</h3>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增地址</el-button>
    </div>
    
    <div v-if="addresses.length === 0" class="empty-state">
      <el-empty description="暂无地址">
        <el-button type="primary" plain @click="openAdd">添加第一个地址</el-button>
      </el-empty>
    </div>
    
    <div v-else class="address-grid">
      <div 
        v-for="addr in addresses" 
        :key="addr.id" 
        class="address-card"
        :class="{ 'is-default': addr.isDefault }"
      >
        <div class="card-content">
          <div class="card-header-row">
            <span class="recipient">{{ addr.recipient }}</span>
            <el-tag v-if="addr.isDefault" size="small" type="success" effect="light">默认</el-tag>
          </div>
          <div class="phone">{{ addr.phone }}</div>
          <div class="detail">{{ addr.detail }}</div>
        </div>
        <div class="card-actions">
          <el-button type="primary" link :icon="Edit" @click="handleEdit(addr)">编辑</el-button>
          <el-divider direction="vertical" />
          <el-button type="danger" link :icon="Delete" @click="handleDelete(addr)">删除</el-button>
        </div>
      </div>
    </div>

    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑地址' : '添加地址'" 
      width="500px"
      destroy-on-close
      class="address-dialog"
    >
      <el-form :model="form" label-width="100px" size="large">
        <el-form-item label="收货人">
          <el-input v-model="form.recipient" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.detail" type="textarea" :rows="3" placeholder="请输入街道、楼牌号等详细地址" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import type { Address } from '../types'

const route = useRoute()
const router = useRouter()
const addresses = ref<Address[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref('')

const form = ref({
  recipient: '',
  phone: '',
  detail: '',
  isDefault: false
})

const fetchAddresses = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const user = JSON.parse(userStr)
  
  try {
    const res = await axios.get('/api/addresses', { params: { userId: user.id } })
    if (res.data.code === 200) {
      addresses.value = res.data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const openAdd = () => {
  isEdit.value = false
  currentId.value = ''
  form.value = { recipient: '', phone: '', detail: '', isDefault: false }
  dialogVisible.value = true
}

const handleEdit = (addr: Address) => {
  isEdit.value = true
  currentId.value = addr.id
  form.value = {
    recipient: addr.recipient,
    phone: addr.phone,
    detail: addr.detail,
    isDefault: !!addr.isDefault
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  const user = JSON.parse(userStr)
  
  try {
    const payload = { ...form.value, userId: user.id }
    
    let res
    if (isEdit.value) {
      res = await axios.put('/api/addresses', { ...payload, id: currentId.value })
    } else {
      res = await axios.post('/api/addresses', payload)
    }

    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      fetchAddresses()
      
      if (!isEdit.value && route.query.redirect) {
        router.push(route.query.redirect as string)
      }
    } else {
       ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (addr: Address) => {
  ElMessageBox.confirm('确认删除该地址吗？', '提示', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/addresses/${addr.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchAddresses()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(fetchAddresses)
</script>

<style scoped>
.address-list-container {
  padding: 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.address-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  background: white;
  transition: all 0.3s;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 160px;
}

.address-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border-color: #c0c4cc;
  transform: translateY(-2px);
}

.address-card.is-default {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.recipient {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.phone {
  color: #606266;
  margin-bottom: 8px;
  font-family: monospace;
}

.detail {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-top: 16px;
  border-top: 1px dashed #e4e7ed;
}

.empty-state {
  padding: 40px 0;
}
</style>
