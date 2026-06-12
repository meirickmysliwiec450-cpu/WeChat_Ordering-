<template>
  <div>
    <h3>分类管理</h3>
    <el-card>
      <div style="margin-bottom:16px">
        <el-button type="primary" @click="handleAdd">新增分类</el-button>
      </div>
      <el-table :data="categories" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryName" label="分类名称" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="isEdit ? '编辑分类' : '新增分类'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCategories, addCategory, updateCategory, deleteCategory } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ categoryName: '', sort: 0 })
const editId = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } finally { loading.value = false }
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  form.value = { categoryName: '', sort: 0 }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.value = { categoryName: row.categoryName, sort: row.sort }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    if (isEdit.value) {
      await updateCategory(editId.value, form.value)
    } else {
      await addCategory(form.value)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) { /* 忽略 */ }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定要删除该分类吗？', '确认', { type: 'warning' })
  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) { /* 忽略 */ }
}

onMounted(fetchData)
</script>
