<template>
  <div>
    <h3>订单管理</h3>
    <el-card>
      <div class="toolbar">
        <el-select v-model="filterStatus" placeholder="按状态筛选" clearable @change="fetchData">
          <el-option label="待处理" :value="1" />
          <el-option label="已接单" :value="2" />
          <el-option label="已完成" :value="3" />
        </el-select>
      </div>
      <el-table :data="orders" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="totalAmount" label="订单金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="receiver" label="收货人" width="100" />
        <el-table-column prop="receiverPhone" label="联系电话" width="130" />
        <el-table-column prop="orderStatus" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.orderStatus === 1" type="warning">待处理</el-tag>
            <el-tag v-else-if="row.orderStatus === 2" type="primary">已接单</el-tag>
            <el-tag v-else-if="row.orderStatus === 3" type="success">已完成</el-tag>
            <el-tag v-else type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payStatus" label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.payStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="$router.push(`/orders/${row.id}`)">详情</el-button>
            <el-button v-if="row.orderStatus === 1" type="success" size="small" @click="handleStatus(row.id, 2)">接单</el-button>
            <el-button v-if="row.orderStatus === 2" type="warning" size="small" @click="handleStatus(row.id, 3)">完成</el-button>
            <el-button v-if="row.orderStatus === 1" type="danger" size="small" @click="handleStatus(row.id, 0)">拒单</el-button>
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
import { getOrders, updateOrderStatus } from '@/api/order'
import { ElMessage } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getOrders({ page: page.value, pageSize: pageSize.value, orderStatus: filterStatus.value })
    orders.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

async function handleStatus(id, status) {
  try {
    await updateOrderStatus(id, status)
    ElMessage.success('状态更新成功')
    fetchData()
  } catch (e) { /* 忽略 */ }
}

onMounted(fetchData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
