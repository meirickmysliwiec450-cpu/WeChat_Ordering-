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
        <el-form-item label="营养信息">
          <div style="margin-bottom:8px">
            <el-button size="small" type="success" @click="autoFillNutrition" :loading="nutriLoading" :disabled="!form.dishName">
              🤖 AI智能填充营养数据
            </el-button>
            <span style="color:#909399;font-size:12px;margin-left:8px">根据菜品名称自动查询营养信息</span>
          </div>
          <el-row :gutter="12">
            <el-col :span="12"><el-input-number v-model="form.calories" :min="0" placeholder="热量(千卡/100g)" controls-position="right" style="width:100%" /></el-col>
            <el-col :span="12"><el-input-number v-model="form.protein" :precision="1" :min="0" placeholder="蛋白质(g/100g)" controls-position="right" style="width:100%" /></el-col>
          </el-row>
          <el-row :gutter="12" style="margin-top:8px">
            <el-col :span="12"><el-input-number v-model="form.fat" :precision="1" :min="0" placeholder="脂肪(g/100g)" controls-position="right" style="width:100%" /></el-col>
            <el-col :span="12"><el-input-number v-model="form.carbs" :precision="1" :min="0" placeholder="碳水(g/100g)" controls-position="right" style="width:100%" /></el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.image" placeholder="选择下方图片或手动输入URL" />
        </el-form-item>
        <el-form-item label="预览" v-if="form.image">
          <el-image :src="form.image" style="width:200px;height:120px;border-radius:6px" fit="cover" />
        </el-form-item>
        <el-form-item label="选择图片">
          <div class="image-picker">
            <div v-if="imageList.length === 0" style="color:#909399;font-size:13px;margin-bottom:8px">
              暂无图片，请将图片放入 public/images 文件夹的子目录中
            </div>
            <template v-for="(group, folder) in imageGroups" :key="folder">
              <div class="folder-label">{{ folder }}</div>
              <div class="image-grid">
                <div
                  v-for="img in group"
                  :key="img.name"
                  class="image-item"
                  :class="{ selected: form.image === img.url }"
                  @click="form.image = img.url"
                >
                  <el-image :src="img.url" style="width:100%;height:80px" fit="cover" />
                  <span class="image-name">{{ img.shortName }}</span>
                </div>
              </div>
            </template>
          </div>
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
import { ref, computed, onMounted } from 'vue'
import { getDishes, addDish, updateDish, updateDishStatus, deleteDish } from '@/api/dish'
import { getCategories } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

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
const imageList = ref([])

const emptyForm = () => ({ categoryId: null, dishName: '', price: 0, stock: 0, discount: '', image: '', description: '', calories: null, protein: null, fat: null, carbs: null })
const form = ref(emptyForm())

const imageGroups = computed(() => {
  const groups = {}
  for (const img of imageList.value) {
    const slashIdx = img.name.lastIndexOf('/')
    const folder = slashIdx > 0 ? img.name.substring(0, slashIdx) : '默认'
    const shortName = slashIdx > 0 ? img.name.substring(slashIdx + 1) : img.name
    if (!groups[folder]) groups[folder] = []
    groups[folder].push({ ...img, shortName })
  }
  return groups
})

async function fetchImages() {
  try {
    const token = localStorage.getItem('adminToken')
    const res = await axios.get('http://localhost:8080/api/admin/upload/images', {
      headers: { Authorization: `Bearer ${token}` }
    })
    imageList.value = res.data.data || []
  } catch (e) { /* 忽略 */ }
}

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

const nutriLoading = ref(false)

async function autoFillNutrition() {
  if (!form.value.dishName) return
  nutriLoading.value = true
  try {
    const token = localStorage.getItem('adminToken')
    const res = await axios.post('http://localhost:8080/api/admin/dishes/nutrition-lookup',
      { dishName: form.value.dishName },
      { headers: { Authorization: `Bearer ${token}` } }
    )
    const d = res.data.data
    form.value.calories = d.calories
    form.value.protein = d.protein
    form.value.fat = d.fat
    form.value.carbs = d.carbs
    ElMessage.success('营养数据已自动填充（来自AI查询）')
  } catch (e) {
    ElMessage.warning('AI查询失败，请手动填写')
  }
  finally { nutriLoading.value = false }
}

function handleAdd() {
  isEdit.value = false; editId.value = null
  form.value = emptyForm()
  fetchImages()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.value = { ...row }
  fetchImages()
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
.image-picker { width: 100%; }
.folder-label { font-size: 13px; color: #409EFF; font-weight: bold; margin: 8px 0 4px; padding-bottom: 4px; border-bottom: 1px solid #e8e8e8; }
.image-grid { display: flex; gap: 10px; flex-wrap: wrap; }
.image-item { width: 110px; cursor: pointer; border: 2px solid transparent; border-radius: 6px; overflow: hidden; transition: border-color 0.2s; text-align: center; }
.image-item:hover { border-color: #409EFF; }
.image-item.selected { border-color: #409EFF; box-shadow: 0 0 0 2px rgba(64,158,255,0.3); }
.image-name { display: block; font-size: 11px; color: #606266; padding: 2px 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
