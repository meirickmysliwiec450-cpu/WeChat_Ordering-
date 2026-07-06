<template>
  <div class="dashboard">
    <div class="page-header">
      <h3>📊 商家仪表盘</h3>
      <span class="header-sub">微信点餐系统 · 数据概览</span>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card stat-blue">
          <div class="stat-icon"><el-icon :size="28"><User /></el-icon></div>
          <div class="stat-body">
            <p class="stat-label">用户总数</p>
            <h2 class="stat-num">{{ stats.totalUsers }}</h2>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-green">
          <div class="stat-icon"><el-icon :size="28"><Tickets /></el-icon></div>
          <div class="stat-body">
            <p class="stat-label">订单总数</p>
            <h2 class="stat-num">{{ stats.totalOrders }}</h2>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-orange">
          <div class="stat-icon"><el-icon :size="28"><Coin /></el-icon></div>
          <div class="stat-body">
            <p class="stat-label">总营业额</p>
            <h2 class="stat-num">¥{{ stats.totalRevenue }}</h2>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-red">
          <div class="stat-icon"><el-icon :size="28"><KnifeFork /></el-icon></div>
          <div class="stat-body">
            <p class="stat-label">菜品数量</p>
            <h2 class="stat-num">{{ stats.totalDishes }}</h2>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 订单状态 + 今日速览 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="14">
        <el-card shadow="never" class="section-card">
          <template #header><span class="card-title">📦 订单状态分布</span></template>
          <div class="order-status-bar">
            <div class="os-item os-warning">
              <span class="os-num">{{ stats.pendingOrders }}</span>
              <span class="os-text">待支付</span>
            </div>
            <div class="os-divider"></div>
            <div class="os-item os-primary">
              <span class="os-num">{{ stats.acceptedOrders }}</span>
              <span class="os-text">已支付</span>
            </div>
            <div class="os-divider"></div>
            <div class="os-item os-success">
              <span class="os-num">{{ stats.completedOrders }}</span>
              <span class="os-text">已完成</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="section-card">
          <template #header><span class="card-title">📅 今日速览</span></template>
          <div class="today-grid">
            <div class="today-item">
              <span class="today-num">{{ stats.todayOrders }}</span>
              <span class="today-label">今日订单</span>
            </div>
            <div class="today-item">
              <span class="today-num">{{ stats.pendingFeedbacks }}</span>
              <span class="today-label">待处理反馈</span>
            </div>
            <div class="today-item">
              <span class="today-num">¥{{ stats.totalRevenue }}</span>
              <span class="today-label">累计营收</span>
            </div>
            <div class="today-item">
              <span class="today-num">{{ stats.totalDishes }}</span>
              <span class="today-label">在售菜品</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStats } from '@/api/dashboard'

const stats = ref({ totalUsers: 0, totalOrders: 0, totalDishes: 0, totalRevenue: 0, todayOrders: 0, pendingFeedbacks: 0, pendingOrders: 0, acceptedOrders: 0, completedOrders: 0 })

onMounted(async () => {
  try {
    const res = await getStats()
    if (res.data) stats.value = res.data
  } catch (e) { /* */ }
})
</script>

<style scoped>
.dashboard { padding: 0; }
.page-header { margin-bottom: 24px; }
.page-header h3 { margin: 0 0 4px; font-size: 24px; }
.header-sub { color: #909399; font-size: 13px; }

/* 统计卡片 */
.stat-card {
  display: flex; align-items: center; gap: 18px;
  padding: 24px 20px; border-radius: 12px;
  color: #fff; cursor: default;
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.12); }
.stat-blue { background: linear-gradient(135deg, #409EFF, #337ecc); }
.stat-green { background: linear-gradient(135deg, #67C23A, #529b2e); }
.stat-orange { background: linear-gradient(135deg, #E6A23C, #c98e2e); }
.stat-red { background: linear-gradient(135deg, #F56C6C, #d9534f); }
.stat-icon { width: 52px; height: 52px; border-radius: 12px; background: rgba(255,255,255,0.2); display: flex; align-items: center; justify-content: center; }
.stat-body { flex: 1; }
.stat-label { font-size: 13px; opacity: 0.9; margin: 0; }
.stat-num { font-size: 28px; font-weight: 700; margin: 4px 0 0; }

/* 卡片 */
.section-card { border: none; border-radius: 12px; }
.card-title { font-size: 16px; font-weight: 600; }

/* 订单状态 */
.order-status-bar { display: flex; align-items: center; justify-content: space-around; padding: 10px 0; }
.os-item { text-align: center; }
.os-num { display: block; font-size: 32px; font-weight: 700; }
.os-text { display: block; font-size: 13px; color: #909399; margin-top: 4px; }
.os-warning .os-num { color: #E6A23C; }
.os-primary .os-num { color: #409EFF; }
.os-success .os-num { color: #67C23A; }
.os-divider { width: 1px; height: 48px; background: #ebeef5; }

/* 今日速览 */
.today-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.today-item { text-align: center; padding: 12px 0; }
.today-num { display: block; font-size: 26px; font-weight: 700; color: #303133; }
.today-label { display: block; font-size: 12px; color: #909399; margin-top: 4px; }
</style>
