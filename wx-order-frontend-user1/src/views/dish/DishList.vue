<template>
  <div>
    <h3>菜品管理</h3>
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增菜品</el-button>
        <el-select v-model="filterCategoryId" placeholder="按分类筛选" clearable style="width:180px" @change="fetchData">
          <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索菜品名" clearable style="width:200px" @input="fetchData" />
      </div>
      <el-table :data="dishes" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="dishName" label="菜品名称" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="fetchData"
        style="margin-top:20px; justify-content:flex-end"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="isEdit ? '编辑菜品' : '新增菜品'" v-model="dialogVisible" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="菜品名称">
          <el-input v-model="form.dishName" />
        </el-form-item>
        <el-form-item label="所属分类">
          <el-select v-model="form.categoryId" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.categoryName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="折扣">
          <el-input v-model="form.discount" placeholder="如: 8折" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.image" placeholder="菜品图片地址" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { getDishes, addDish, updateDish, updateDishStatus, deleteDish } from '@/api/dish'
import { getCategories } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

const dishes = ref([])
const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterCategoryId = ref(null)
const keyword = ref('')

const form = ref({ categoryId: null, dishName: '', price: 0, stock: 0, discount: '', image: '', description: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getDishes({ page: page.value, pageSize: pageSize.value, categoryId: filterCategoryId.value, keyword: keyword.value })
    dishes.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

async function fetchCategories() {
  const res = await getCategories()
  categories.value = res.data || []
}

function handleAdd() {
  isEdit.value = false; editId.value = null
  form.value = { categoryId: null, dishName: '', price: 0, stock: 0, discount: '', image: '', description: '' }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    isEdit.value ? await updateDish(editId.value, form.value) : await addDish(form.value)
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) { /* 忽略 */ }
}

async function handleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateDishStatus(row.id, newStatus)
  ElMessage.success('状态更新成功')
  row.status = newStatus
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该菜品吗？', '确认', { type: 'warning' })
  await deleteDish(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => { fetchData(); fetchCategories() })
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; align-items: center; }
</style>
