<template>
  <div>
    <h3>订单详情</h3>
    <el-card v-loading="loading">
      <template #header>
        <el-button @click="$router.back()">返回</el-button>
        <span style="margin-left:16px;font-weight:bold">订单号: {{ order?.orderNo }}</span>
      </template>
      <el-descriptions v-if="order" :column="2" border>
        <el-descriptions-item label="订单状态">
          <el-tag v-if="order.orderStatus === 1" type="warning">待处理</el-tag>
          <el-tag v-else-if="order.orderStatus === 2" type="primary">已接单</el-tag>
          <el-tag v-else-if="order.orderStatus === 3" type="success">已完成</el-tag>
          <el-tag v-else type="danger">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="order.payStatus === 1 ? 'success' : 'info'">
            {{ order.payStatus === 1 ? '已支付' : '未支付' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ order.receiver }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ order.payAmount || order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 订单明细 -->
      <h4 style="margin-top:24px">订单明细</h4>
      <el-table :data="details" stripe>
        <el-table-column prop="dishName" label="菜品名称" />
        <el-table-column prop="price" label="单价">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="amount" label="小计">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail } from '@/api/order'

const route = useRoute()
const order = ref(null)
const details = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res.data.order
    details.value = res.data.details
  } finally { loading.value = false }
})
</script>
