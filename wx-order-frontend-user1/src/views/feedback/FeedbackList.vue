<template>
  <div>
    <h3>反馈管理</h3>
    <el-card>
      <div class="toolbar">
        <el-select v-model="filterStatus" placeholder="按状态筛选" clearable @change="fetchData">
          <el-option label="未处理" :value="0" />
          <el-option label="已回复" :value="1" />
          <el-option label="已解决" :value="2" />
        </el-select>
      </div>
      <el-table :data="feedbacks" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="content" label="反馈内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="replyContent" label="回复内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.replyContent || '暂未回复' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">未处理</el-tag>
            <el-tag v-else-if="row.status === 1" type="primary">已回复</el-tag>
            <el-tag v-else type="success">已解决</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="反馈时间" width="170" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" size="small" @click="handleReply(row)">回复</el-button>
            <el-button v-if="row.status === 1" type="success" size="small" @click="handleResolve(row)">标记解决</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="fetchData"
        style="margin-top:20px; justify-content:flex-end"
      />
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog title="回复反馈" v-model="replyVisible" width="500px">
      <el-input v-model="replyContent" type="textarea" :rows="4" placeholder="请输入回复内容" />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReply">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getFeedbacks, replyFeedback, updateFeedbackStatus } from '@/api/feedback'
import { ElMessage } from 'element-plus'

const feedbacks = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref(null)
const replyVisible = ref(false)
const replyContent = ref('')
const currentId = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getFeedbacks({ page: page.value, pageSize: pageSize.value, status: filterStatus.value })
    feedbacks.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

function handleReply(row) {
  currentId.value = row.id
  replyContent.value = ''
  replyVisible.value = true
}

async function handleSubmitReply() {
  await replyFeedback(currentId.value, replyContent.value)
  ElMessage.success('回复成功')
  replyVisible.value = false
  fetchData()
}

async function handleResolve(row) {
  await updateFeedbackStatus(row.id, 2)
  ElMessage.success('已标记为解决')
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
