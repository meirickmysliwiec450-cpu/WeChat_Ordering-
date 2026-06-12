<template>
  <div>
    <h3>轮播图管理</h3>
    <el-card>
      <div style="margin-bottom:16px">
        <el-button type="primary" @click="handleAdd">新增轮播图</el-button>
      </div>
      <el-table :data="banners" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="imageUrl" label="图片URL" width="300" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="isEdit ? '编辑轮播图' : '新增轮播图'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="form.imageUrl" /></el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="form.linkUrl" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
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
import { getBanners, addBanner, updateBanner, updateBannerStatus, deleteBanner } from '@/api/banner'
import { ElMessage, ElMessageBox } from 'element-plus'

const banners = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ title: '', imageUrl: '', linkUrl: '', sort: 0, remark: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getBanners()
    banners.value = res.data || []
  } finally { loading.value = false }
}

function handleAdd() {
  isEdit.value = false; editId.value = null
  form.value = { title: '', imageUrl: '', linkUrl: '', sort: 0, remark: '' }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.value = { title: row.title, imageUrl: row.imageUrl, linkUrl: row.linkUrl, sort: row.sort, remark: row.remark }
  dialogVisible.value = true
}

async function handleSave() {
  isEdit.value ? await updateBanner(editId.value, form.value) : await addBanner(form.value)
  ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
  dialogVisible.value = false
  fetchData()
}

async function handleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await updateBannerStatus(row.id, newStatus)
  ElMessage.success('状态更新')
  row.status = newStatus
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该轮播图吗？', '确认', { type: 'warning' })
  await deleteBanner(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(fetchData)
</script>
