<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Edit, Delete, Check, Close } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const pendingVerifications = ref([])
const verificationDialogVisible = ref(false)

const editDialogVisible = ref(false)
const editForm = ref({
  id: '',
  phone: '',
  name: '',
  university: '',
  creditScore: 0,
  password: '',
  role: '',
  verified: false
})

const addDialogVisible = ref(false)
const addForm = ref({
  phone: '',
  password: '',
  name: '',
  university: '',
  role: 'user',
  verified: false
})

const fetchUsers = async () => {
  try {
    const res = await axios.get('/api/users')
    if (res.data.code === 200) {
      users.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  }
}

const fetchPendingVerifications = async () => {
  try {
    const res = await axios.get('/api/verification/pending')
    if (res.data.code === 200) {
      pendingVerifications.value = res.data.data
    }
  } catch (error) {
    console.error('Fetch verifications failed')
  }
}

const handleOpenVerifications = () => {
  fetchPendingVerifications()
  verificationDialogVisible.value = true
}

const handleApproveVerification = async (id: string) => {
  try {
    const res = await axios.post(`/api/verification/${id}/approve`)
    if (res.data.code === 200) {
      ElMessage.success('已通过')
      fetchPendingVerifications()
      fetchUsers() // Refresh user list to show verified status
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  }
}

const handleRejectVerification = async (id: string) => {
  try {
    const res = await axios.post(`/api/verification/${id}/reject`)
    if (res.data.code === 200) {
      ElMessage.success('已拒绝')
      fetchPendingVerifications()
    } else {
      ElMessage.error(res.data.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  }
}

const handleAddUser = () => {
  addForm.value = {
    phone: '',
    password: '',
    name: '',
    university: '',
    role: 'user',
    verified: false
  }
  addDialogVisible.value = true
}

const submitAdd = async () => {
  try {
    const res = await axios.post('/api/users', addForm.value)
    if (res.data.code === 200) {
      ElMessage.success('用户创建成功')
      addDialogVisible.value = false
      fetchUsers()
    } else {
      ElMessage.error(res.data.msg || '创建失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  }
}

const handleEdit = (row: any) => {
  editForm.value = { ...row, password: '' }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    const res = await axios.put('/api/users', editForm.value)
    if (res.data.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      fetchUsers()
    } else {
      ElMessage.error(res.data.msg || '更新失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该用户吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/users/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchUsers()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleVerify = async (row: any) => {
  try {
    const updatedUser = { ...row, verified: true }
    const res = await axios.put('/api/users', updatedUser)
    if (res.data.code === 200) {
      ElMessage.success('认证成功')
      fetchUsers()
    } else {
      ElMessage.error(res.data.msg || '认证失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  }
}

const getCreditLevelTag = (score: number) => {
  if (score >= 700) return 'success'
  if (score >= 600) return 'primary'
  if (score >= 500) return 'warning'
  return 'danger'
}

const getCreditLevelText = (score: number) => {
  if (score >= 700) return '极好'
  if (score >= 600) return '良好'
  if (score >= 500) return '一般'
  return '较差'
}

onMounted(() => {
  fetchUsers()
  fetchPendingVerifications()
})
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">用户管理</h2>
      <div class="header-actions">
        <span class="admin-info">管理员: admin</span>
        <div class="status-dot"></div>
      </div>
    </div>

    <div class="table-container">
      <div class="toolbar">
        <el-button type="warning" @click="handleOpenVerifications">
          待审核认证 ({{ pendingVerifications.length }})
        </el-button>
        <el-button type="primary" :icon="Plus" @click="handleAddUser">新增用户</el-button>
      </div>

      <el-table :data="users" style="width: 100%">
        <el-table-column prop="id" label="用户ID" width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="昵称" />
        <el-table-column prop="university" label="学校" />
        <el-table-column label="信用分" width="180">
          <template #default="{ row }">
            <div class="credit-score-cell">
              <span class="score-value">{{ row.creditScore || 605 }}</span>
              <el-tag :type="getCreditLevelTag(row.creditScore || 605)" size="small" effect="plain">
                {{ getCreditLevelText(row.creditScore || 605) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'warning' : 'info'" size="small" effect="light">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.verified ? 'success' : 'info'" size="small" effect="light">
              {{ row.verified ? '已认证' : '未认证' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button 
              v-if="!row.verified"
              link 
              type="success" 
              size="small" 
              @click="handleVerify(row)"
            >
              认证
            </el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)" />
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="verificationDialogVisible" title="待审核认证请求" width="800px">
      <el-table :data="pendingVerifications" style="width: 100%">
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column prop="studentId" label="学号/工号" />
        <el-table-column label="证明材料">
          <template #default="{ row }">
            <el-image 
              style="width: 100px; height: 100px"
              :src="row.materialUrl" 
              :preview-src-list="[row.materialUrl]"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="success" size="small" :icon="Check" circle @click="handleApproveVerification(row.id)" />
            <el-button type="danger" size="small" :icon="Close" circle @click="handleRejectVerification(row.id)" />
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="addDialogVisible" title="新增用户" width="500px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="手机号">
          <el-input v-model="addForm.phone" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="addForm.password" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="addForm.name" />
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="addForm.university" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="addForm.role">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="addForm.verified">
            <el-option label="已认证" :value="true" />
            <el-option label="未认证" :value="false" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAdd">添加</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑用户" width="500px">
      <el-form :model="editForm" label-width="110px">
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="editForm.university" />
        </el-form-item>
        <el-form-item label="信用分">
          <el-input v-model="editForm.creditScore" />
        </el-form-item>
        <el-form-item label="重置密码 (选填)">
          <el-input v-model="editForm.password" placeholder="不修改请留空" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证状态">
          <el-select v-model="editForm.verified">
            <el-option label="已认证" :value="true" />
            <el-option label="未认证" :value="false" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.credit-score-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-value {
  font-weight: bold;
  font-family: monospace;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-info {
  font-size: 14px;
  color: #606266;
}

.status-dot {
  width: 20px;
  height: 20px;
  background-color: #2563eb;
  border-radius: 50%;
}

.table-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  flex: 1;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}
</style>