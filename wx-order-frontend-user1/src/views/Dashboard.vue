<template>
  <div class="dashboard">
    <h3>数据概览</h3>
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card><div class="stat-card"><el-icon :size="32" color="#409EFF"><User /></el-icon><div><p>用户总数</p><h2>{{ stats.totalUsers }}</h2></div></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat-card"><el-icon :size="32" color="#67C23A"><Tickets /></el-icon><div><p>订单总数</p><h2>{{ stats.totalOrders }}</h2></div></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat-card"><el-icon :size="32" color="#E6A23C"><Coin /></el-icon><div><p>总营业额</p><h2>¥{{ stats.totalRevenue }}</h2></div></div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card><div class="stat-card"><el-icon :size="32" color="#F56C6C"><KnifeFork /></el-icon><div><p>菜品数量</p><h2>{{ stats.totalDishes }}</h2></div></div></el-card>
      </el-col>
    </el-row>

    <!-- 订单状态 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card>
          <template #header>订单状态分布</template>
          <div class="order-stats">
            <el-tag type="warning" size="large">待处理: {{ stats.pendingOrders }}</el-tag>
            <el-tag type="primary" size="large">已接单: {{ stats.acceptedOrders }}</el-tag>
            <el-tag type="success" size="large">已完成: {{ stats.completedOrders }}</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>今日速览</template>
          <p>今日订单数: <b>{{ stats.todayOrders }}</b></p>
          <p>待处理反馈: <b>{{ stats.pendingFeedbacks }}</b></p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStats } from '@/api/dashboard'

const stats = ref({
  totalUsers: 0, totalOrders: 0, totalDishes: 0,
  totalRevenue: 0, todayOrders: 0, pendingFeedbacks: 0,
  pendingOrders: 0, acceptedOrders: 0, completedOrders: 0
})

onMounted(async () => {
  try {
    const res = await getStats()
    if (res.data) stats.value = res.data
  } catch (e) { /* 忽略 */ }
})
</script>

<style scoped>
.dashboard h3 { margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; gap: 16px; }
.stat-card p { color: #909399; margin: 0; font-size: 14px; }
.stat-card h2 { margin: 4px 0 0; font-size: 24px; }
.order-stats { display: flex; gap: 20px; }
</style>
