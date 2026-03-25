<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const textbooks = ref([])
const editDialogVisible = ref(false)
const editForm = ref({
  id: '',
  title: '',
  author: '',
  price: 0,
  dailyRent: 0,
  rentEnabled: false,
  status: '',
  category: '',
  type: ''
})

const handleRentChange = (val: any) => {
  if (val) {
    editForm.value.type = 'rent'
  } else {
    editForm.value.type = 'sell'
  }
}

const fetchTextbooks = async () => {
  try {
    const res = await axios.get('/api/textbooks')
    if (res.data.code === 200) {
      textbooks.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取教材列表失败')
  }
}

const handleEdit = (row: any) => {
  editForm.value = { ...row }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    const res = await axios.put('/api/textbooks', editForm.value)
    if (res.data.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      fetchTextbooks()
    } else {
      ElMessage.error(res.data.msg || '更新失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该教材吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await axios.delete(`/api/textbooks/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchTextbooks()
      } else {
        ElMessage.error(res.data.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const handleAddTextbook = () => {
  ElMessage.info('功能开发中')
}

onMounted(fetchTextbooks)
</script>

<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">教材管理</h2>
      <div class="header-actions">
        <span class="admin-info">管理员: admin</span>
        <div class="status-dot"></div>
      </div>
    </div>

    <div class="table-container">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAddTextbook">新增教材</el-button>
      </div>

      <el-table :data="textbooks" style="width: 100%">
        <el-table-column prop="title" label="教材名称" />
        <el-table-column prop="author" label="作者" />
        <el-table-column label="价格">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类型" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'available' ? 'success' : (row.status === 'rented' ? 'warning' : 'info')" size="small" effect="light">
              {{ row.status === 'available' ? '在售' : (row.status === 'sold' ? '已售' : (row.status === 'rented' ? '出租中' : row.status)) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)" />
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)" />
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑教材" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="教材名称">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="editForm.author" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="editForm.category" />
        </el-form-item>
        <el-form-item label="租赁模式">
          <el-checkbox v-model="editForm.rentEnabled" @change="handleRentChange">开启租赁</el-checkbox>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="editForm.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="日租金" v-if="editForm.rentEnabled">
          <el-input-number v-model="editForm.dailyRent" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status">
            <el-option label="在售" value="available" />
            <el-option label="已售" value="sold" />
            <el-option label="出租中" value="rented" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">确定</el-button>
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