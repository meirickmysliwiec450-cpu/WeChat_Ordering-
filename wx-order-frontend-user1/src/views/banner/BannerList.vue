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
        <el-table-column label="图片预览" width="120">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              style="width:80px;height:50px;border-radius:4px"
              fit="cover"
              :preview-src-list="[row.imageUrl]"
              preview-teleported
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="imageUrl" label="图片URL" min-width="200" show-overflow-tooltip />
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="isEdit ? '编辑轮播图' : '新增轮播图'" v-model="dialogVisible" width="700px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="请输入轮播图标题" /></el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.imageUrl" placeholder="选择下方图片或手动输入URL" />
        </el-form-item>
        <!-- 当前图片预览 -->
        <el-form-item label="预览" v-if="form.imageUrl">
          <el-image :src="form.imageUrl" style="width:200px;height:120px;border-radius:6px" fit="cover" />
        </el-form-item>
        <!-- 图片选择器（按子文件夹分组） -->
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
                  :class="{ selected: form.imageUrl === img.url }"
                  @click="form.imageUrl = img.url"
                >
                  <el-image :src="img.url" style="width:100%;height:80px" fit="cover" />
                  <span class="image-name">{{ img.shortName }}</span>
                </div>
              </div>
            </template>
          </div>
        </el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="form.linkUrl" placeholder="可选" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" /></el-form-item>
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
import { getBanners, addBanner, updateBanner, updateBannerStatus, deleteBanner } from '@/api/banner'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const banners = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ title: '', imageUrl: '', linkUrl: '', sort: 0, remark: '' })
const imageList = ref([])

// 按子文件夹分组
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

async function fetchData() {
  loading.value = true
  try {
    const res = await getBanners()
    banners.value = res.data || []
  } finally { loading.value = false }
}

async function fetchImages() {
  try {
    const token = localStorage.getItem('adminToken')
    const res = await axios.get('http://localhost:8080/api/admin/upload/images', {
      headers: { Authorization: `Bearer ${token}` }
    })
    imageList.value = res.data.data || []
  } catch (e) { /* 忽略 */ }
}

function handleAdd() {
  isEdit.value = false; editId.value = null
  form.value = { title: '', imageUrl: '', linkUrl: '', sort: 0, remark: '' }
  fetchImages()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.value = { title: row.title, imageUrl: row.imageUrl, linkUrl: row.linkUrl, sort: row.sort, remark: row.remark }
  fetchImages()
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

<style scoped>
.image-picker {
  width: 100%;
}
.folder-label {
  font-size: 13px;
  color: #409EFF;
  font-weight: bold;
  margin: 8px 0 4px;
  padding-bottom: 4px;
  border-bottom: 1px solid #e8e8e8;
}
.image-grid {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.image-item {
  width: 110px;
  cursor: pointer;
  border: 2px solid transparent;
  border-radius: 6px;
  overflow: hidden;
  transition: border-color 0.2s;
  text-align: center;
}
.image-item:hover {
  border-color: #409EFF;
}
.image-item.selected {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64,158,255,0.3);
}
.image-name {
  display: block;
  font-size: 11px;
  color: #606266;
  padding: 2px 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
