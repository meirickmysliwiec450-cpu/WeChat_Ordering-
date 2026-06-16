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

    <!-- AI推荐预览 -->
    <el-card style="margin-top:20px">
      <template #header>
        <span>🤖 AI 推荐预览 — 当前时段：<b>{{ recommend.period }}</b></span>
        <el-button size="small" style="float:right" @click="fetchRecommend" :loading="recLoading">刷新</el-button>
      </template>
      <el-row :gutter="16" v-if="recommend.dishes.length > 0">
        <el-col :span="8" v-for="dish in recommend.dishes" :key="dish.id">
          <el-card shadow="hover" class="recommend-card">
            <el-image
              v-if="dish.image"
              :src="dish.image"
              style="width:100%;height:140px;border-radius:6px"
              fit="cover"
            />
            <div style="padding:8px 0">
              <span style="font-size:15px;font-weight:bold">{{ dish.dishName }}</span>
              <el-tag size="small" style="margin-left:8px">{{ dish.categoryName }}</el-tag>
              <span style="float:right;color:#E6A23C;font-weight:bold">¥{{ dish.price }}</span>
            </div>
            <p style="color:#909399;font-size:12px;margin:0">💡 {{ dish.reason }}</p>
          </el-card>
        </el-col>
      </el-row>
      <p v-else style="color:#909399">暂无推荐数据，请先上架菜品并让用户下单</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStats } from '@/api/dashboard'
import axios from 'axios'

const stats = ref({
  totalUsers: 0, totalOrders: 0, totalDishes: 0,
  totalRevenue: 0, todayOrders: 0, pendingFeedbacks: 0,
  pendingOrders: 0, acceptedOrders: 0, completedOrders: 0
})
const recommend = ref({ period: '', dishes: [] })
const recLoading = ref(false)

async function fetchRecommend() {
  recLoading.value = true
  try {
    const token = localStorage.getItem('adminToken')
    const res = await axios.get('http://localhost:8080/api/admin/recommend/preview', {
      headers: { Authorization: `Bearer ${token}` }
    })
    recommend.value = res.data.data
  } catch (e) { /* 忽略 */ }
  finally { recLoading.value = false }
}

onMounted(async () => {
  try {
    const res = await getStats()
    if (res.data) stats.value = res.data
  } catch (e) { /* 忽略 */ }
  fetchRecommend()
})
</script>

<style scoped>
.dashboard h3 { margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; gap: 16px; }
.stat-card p { color: #909399; margin: 0; font-size: 14px; }
.stat-card h2 { margin: 4px 0 0; font-size: 24px; }
.order-stats { display: flex; gap: 20px; }
.recommend-card { border: 1px solid #e8e8e8; transition: transform 0.2s; }
.recommend-card:hover { transform: translateY(-2px); }
</style>
