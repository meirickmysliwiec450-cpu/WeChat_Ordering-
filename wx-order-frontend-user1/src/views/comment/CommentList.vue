<template>
  <div>
    <h3>评价管理</h3>
    <el-card>
      <div class="toolbar">
        <el-input v-model="filterOrderId" placeholder="按订单号筛选" clearable @clear="fetchData" style="width:220px" />
        <el-button type="primary" @click="fetchData">查询</el-button>
      </div>
      <el-table :data="comments" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderId" label="订单ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="score" label="评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.score" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="photo" label="图片" width="80">
          <template #default="{ row }">
            <el-button v-if="row.photo" type="primary" size="small" link @click="previewImage(row.photo)">查看</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-popconfirm title="确定删除该评价吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" size="small" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="fetchData"
        style="margin-top:20px; justify-content:flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getComments, deleteComment } from '@/api/orderComment'
import { ElMessage } from 'element-plus'

const comments = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterOrderId = ref('')

async function fetchData() {
  loading.value = true
  try {
    const res = await getComments({
      page: page.value,
      pageSize: pageSize.value,
      orderId: filterOrderId.value || undefined
    })
    comments.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

function previewImage(url) {
  window.open(url, '_blank')
}

async function handleDelete(id) {
  await deleteComment(id)
  ElMessage.success('评价已删除')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
