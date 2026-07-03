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
            <el-tag type="warning" size="large">待支付: {{ stats.pendingOrders }}</el-tag>
            <el-tag type="primary" size="large">已支付: {{ stats.acceptedOrders }}</el-tag>
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
            <el-image v-if="dish.image" :src="dish.image" style="width:100%;height:140px;border-radius:6px" fit="cover" />
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

    <!-- 营养助手模拟 + 周报 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card style="height:380px">
          <template #header>🥗 AI 营养助手（模拟对话）</template>
          <div class="chat-box">
            <div v-for="(msg, i) in chatHistory" :key="i" :class="msg.role === 'user' ? 'chat-user' : 'chat-ai'">
              <b>{{ msg.role === 'user' ? '用户' : 'AI助手' }}：</b>{{ msg.content }}
            </div>
            <p v-if="chatHistory.length === 0" style="color:#909399;text-align:center;padding:40px 0">输入营养相关问题，模拟小程序端对话</p>
          </div>
          <div class="chat-input">
            <el-input v-model="chatMsg" placeholder="如：我今天点的菜营养怎么样？" @keyup.enter="sendChat" />
            <el-button type="primary" style="margin-left:8px" @click="sendChat" :loading="chatLoading">发送</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card style="height:380px">
          <template #header>
            <span>📊 饮食周报预览</span>
            <el-button size="small" style="float:right" @click="fetchReport" :loading="reportLoading">生成周报</el-button>
          </template>
          <div v-if="report" class="report-box">
            <p><b>订单数：</b>{{ report.orderCount }} 笔</p>
            <p><b>总热量：</b>{{ report.stats?.totalCalories }}千卡 | <b>蛋白质：</b>{{ report.stats?.totalProtein }}g | <b>脂肪：</b>{{ report.stats?.totalFat }}g | <b>碳水：</b>{{ report.stats?.totalCarbs }}g</p>
            <p style="margin-top:12px;white-space:pre-wrap;background:#f5f7fa;padding:12px;border-radius:6px;font-size:13px">{{ report.report }}</p>
          </div>
          <p v-else style="color:#909399;text-align:center;padding:60px 0">点击"生成周报"查看AI饮食分析</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStats } from '@/api/dashboard'
import axios from 'axios'

const stats = ref({ totalUsers: 0, totalOrders: 0, totalDishes: 0, totalRevenue: 0, todayOrders: 0, pendingFeedbacks: 0, pendingOrders: 0, acceptedOrders: 0, completedOrders: 0 })
const recommend = ref({ period: '', dishes: [] })
const recLoading = ref(false)

const chatMsg = ref('')
const chatHistory = ref([])
const chatLoading = ref(false)
const report = ref(null)
const reportLoading = ref(false)

async function fetchRecommend() {
  recLoading.value = true
  try {
    const token = localStorage.getItem('adminToken')
    const res = await axios.get('http://localhost:8080/api/admin/recommend/preview', { headers: { Authorization: `Bearer ${token}` } })
    recommend.value = res.data.data
  } catch (e) { /* */ }
  finally { recLoading.value = false }
}

async function sendChat() {
  if (!chatMsg.value.trim()) return
  chatLoading.value = true
  chatHistory.value.push({ role: 'user', content: chatMsg.value })
  try {
    const token = localStorage.getItem('adminToken')
    // 先用admin token登录一个测试用户获取wx token
    const loginRes = await axios.post('http://localhost:8080/api/wx/user/login', { openId: 'admin_preview' })
    const wxToken = loginRes.data.data.token
    const res = await axios.post('http://localhost:8080/api/wx/nutrition/chat', { message: chatMsg.value }, { headers: { Authorization: `Bearer ${wxToken}` } })
    chatHistory.value.push({ role: 'assistant', content: res.data.data.reply })
  } catch (e) {
    chatHistory.value.push({ role: 'assistant', content: 'AI助手暂不可用，请稍后再试' })
  }
  chatMsg.value = ''
  chatLoading.value = false
}

async function fetchReport() {
  reportLoading.value = true
  try {
    const loginRes = await axios.post('http://localhost:8080/api/wx/user/login', { openId: 'admin_preview' })
    const wxToken = loginRes.data.data.token
    const res = await axios.get('http://localhost:8080/api/wx/nutrition/report', { headers: { Authorization: `Bearer ${wxToken}` } })
    report.value = res.data.data
  } catch (e) { /* */ }
  finally { reportLoading.value = false }
}

onMounted(async () => {
  try {
    const res = await getStats()
    if (res.data) stats.value = res.data
  } catch (e) { /* */ }
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
.chat-box { height: 220px; overflow-y: auto; margin-bottom: 12px; padding: 8px; background: #fafafa; border-radius: 6px; }
.chat-user { margin-bottom: 8px; color: #409EFF; }
.chat-ai { margin-bottom: 8px; color: #67C23A; }
.chat-input { display: flex; }
.report-box p { margin: 4px 0; }
</style>
