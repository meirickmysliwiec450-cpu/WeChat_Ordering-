<template>
  <div>
    <h3>支付记录</h3>
    <el-card>
      <div class="toolbar">
        <el-input v-model="filterOrderId" placeholder="按订单号筛选" clearable @clear="fetchData" style="width:220px" />
        <el-button type="primary" @click="fetchData">查询</el-button>
      </div>
      <el-table :data="payments" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderId" label="订单ID" width="80" />
        <el-table-column prop="payNo" label="支付流水号" min-width="200" show-overflow-tooltip />
        <el-table-column prop="payAmount" label="支付金额" width="120">
          <template #default="{ row }">¥{{ row.payAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.payMethod === 'wechat'" type="success">微信支付</el-tag>
            <el-tag v-else-if="row.payMethod === 'alipay'" type="primary">支付宝</el-tag>
            <el-tag v-else type="info">{{ row.payMethod || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="170" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page" :page-size="pageSize" :total="total"
        layout="total, prev, pager, next" @current-change="fetchData"
        style="margin-top:20px; justify-content:flex-end"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog title="支付记录详情" v-model="detailVisible" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ detail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="支付流水号">{{ detail.payNo }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">¥{{ detail.payAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detail.payMethod }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detail.payTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPayments, getPaymentById } from '@/api/payment'

const payments = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterOrderId = ref('')
const detailVisible = ref(false)
const detail = ref({})

async function fetchData() {
  loading.value = true
  try {
    const res = await getPayments({
      page: page.value,
      pageSize: pageSize.value,
      orderId: filterOrderId.value || undefined
    })
    payments.value = res.data.list
    total.value = res.data.total
  } finally { loading.value = false }
}

async function showDetail(row) {
  const res = await getPaymentById(row.id)
  detail.value = res.data
  detailVisible.value = true
}

onMounted(fetchData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
