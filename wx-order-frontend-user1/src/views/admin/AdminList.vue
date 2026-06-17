<template>
  <div>
    <h3>管理员管理</h3>
    <el-card>
      <div style="margin-bottom:16px">
        <el-button type="primary" @click="handleAdd">新增管理员</el-button>
      </div>
      <el-table :data="admins" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'super_admin' ? 'danger' : 'primary'" size="small">
              {{ row.role === 'super_admin' ? '超级管理员' : '管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="row.role !== 'super_admin'" size="small" @click="handleToggle(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="新增管理员" v-model="dialogVisible" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" placeholder="登录账号" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" placeholder="登录密码" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" placeholder="真实姓名" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="手机号" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const admins = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({ username: '', password: '', realName: '', phone: '' })

async function fetchData() {
  loading.value = true
  try {
    const token = localStorage.getItem('adminToken')
    const res = await axios.get('http://localhost:8080/api/admin/super/admins', {
      headers: { Authorization: `Bearer ${token}` }
    })
    admins.value = res.data.data || []
  } catch (e) {
    ElMessage.error('获取管理员列表失败，可能权限不足')
  }
  finally { loading.value = false }
}

function handleAdd() {
  form.value = { username: '', password: '', realName: '', phone: '' }
  dialogVisible.value = true
}

async function handleSave() {
  const token = localStorage.getItem('adminToken')
  try {
    await axios.post('http://localhost:8080/api/admin/super/register', form.value, {
      headers: { Authorization: `Bearer ${token}` }
    })
    ElMessage.success('管理员注册成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '注册失败')
  }
}

async function handleToggle(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const token = localStorage.getItem('adminToken')
  await axios.put(`http://localhost:8080/api/admin/super/toggle-status/${row.id}`,
    { status: newStatus },
    { headers: { Authorization: `Bearer ${token}` } }
  )
  ElMessage.success('状态已更新')
  row.status = newStatus
}

onMounted(fetchData)
</script>
