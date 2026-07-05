const { getToken, buildUrl } = require('../../utils/auth')
const { addToCart } = require('../../utils/store')

Page({
  data: {
    activeTab: 'recommend',
    // 今日推荐
    recDishes: [],
    recPeriod: '',
    // 偏好推荐
    prefText: '',
    prefLoading: false,
    chatDishes: [],
    lastQuery: '',
    // 营养助手
    messages: [],
    inputText: '',
    chatLoading: false,
    report: null,
    reportLoading: false,
    scrollTop: 0
  },

  onLoad() {
    this.loadQuickDishes()
    this.loadCachedOrFetch()
  },

  onShow() {
    // 切Tab不重新请求，秒显
  },

  // 缓存优先：5分钟内复用，超时才调AI
  loadCachedOrFetch() {
    const cache = wx.getStorageSync('ai_rec_cache')
    if (cache && cache.time && (Date.now() - cache.time < 300000)) {
      this.setData({ recDishes: cache.dishes, recPeriod: cache.period })
      return
    }
    this.fetchRecommend()
  },

  switchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab })
  },

  // ========== 图片下载（手机兼容） ==========
  downloadImages(list, key, callback) {
    if (!list || !list.length) { callback && callback(list); return }
    let done = 0
    list.forEach((item, i) => {
      const url = item[key || 'image']
      if (!url || url.startsWith('data:') || url.startsWith('wxfile:')) {
        done++; if (done === list.length && callback) callback(list); return
      }
      wx.downloadFile({
        url,
        success: res => {
          if (res.statusCode === 200) item[key || 'image'] = res.tempFilePath
        },
        complete: () => {
          done++
          if (done === list.length && callback) callback(list)
        }
      })
    })
  },

  // ========== 快捷提问 ==========
  quickAsk(e) {
    const text = e.currentTarget.dataset.text
    if (!text) return
    this.setData({ prefText: text })
  },

  quickNutrition(e) {
    this.setData({ activeTab: 'nutrition', inputText: e.currentTarget.dataset.text || '' })
  },

  // ========== 快速加载 ==========
  loadQuickDishes() {
    // 已有缓存直接显示，不用重新加载
    if (this.data.recDishes.length > 0) return
    const token = getToken()
    wx.request({
      url: buildUrl('/wx/dishes'),
      method: 'GET',
      header: { Authorization: `Bearer ${token}` },
      success: res => {
        if (res.data && res.data.code === 200) {
          const dishes = res.data.data || []
          const shuffled = dishes.sort(() => Math.random() - 0.5).slice(0, 6)
          const quick = shuffled.map(d => ({
            id: d.id, dishName: d.dishName, image: d.image,
            price: d.price, categoryName: d.categoryId, reason: '👀 猜你喜欢'
          }))
          this.setData({ recDishes: quick, recPeriod: '为你精选' })
        }
      }
    })
  },

  // ========== AI推荐（后台加载，替换快速结果） ==========
  fetchRecommend() {
    wx.request({
      url: buildUrl('/wx/recommend'),
      method: 'GET',
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data && res.data.code === 200) {
          const d = res.data.data
          if (d.dishes && d.dishes.length > 0) {
            this.setData({ recDishes: d.dishes, recPeriod: d.period || '' })
            // 缓存5分钟
            wx.setStorageSync('ai_rec_cache', { dishes: d.dishes, period: d.period, time: Date.now() })
          }
        }
      }
    })
  },

  // ========== 偏好推荐 ==========
  onPrefInput(e) { this.setData({ prefText: e.detail.value }) },

  sendPreference() {
    const text = this.data.prefText
    if (!text || this.data.prefLoading) return
    this.setData({ prefLoading: true, chatDishes: [], lastQuery: text, prefText: '' })
    wx.request({
      url: buildUrl('/wx/recommend/chat'),
      method: 'POST',
      header: { 'content-type': 'application/json', Authorization: `Bearer ${getToken()}` },
      data: { message: text },
      success: res => {
        if (res.data && res.data.code === 200) {
          this.setData({ chatDishes: res.data.data.dishes || [], prefLoading: false })
        } else {
          wx.showToast({ title: '暂无匹配菜品', icon: 'none' })
          this.setData({ prefLoading: false })
        }
      },
      fail: () => {
        wx.showToast({ title: '网络连接失败', icon: 'none' })
        this.setData({ prefLoading: false })
      }
    })
  },

  goDishDetail(e) {
    wx.navigateTo({ url: `/pages/dish_detail/dish_detail?id=${e.currentTarget.dataset.id}` })
  },

  addCartFromRec(e) {
    const id = e.currentTarget.dataset.id
    let dish = this.data.chatDishes.find(d => d.id == id)
    if (!dish) dish = this.data.recDishes.find(d => d.id == id)
    if (dish) {
      addToCart({ id: dish.id, name: dish.dishName, price: dish.price, image: dish.image })
      wx.showToast({ title: '已加入购物车', icon: 'success' })
    }
  },

  // ========== 营养助手 ==========
  onInput(e) { this.setData({ inputText: e.detail.value }) },

  sendMessage() {
    const text = this.data.inputText
    if (!text || this.data.chatLoading) return
    const msgs = this.data.messages.concat([{ role: 'user', content: text }])
    this.setData({ messages: msgs, inputText: '', chatLoading: true })
    wx.request({
      url: buildUrl('/wx/nutrition/chat'),
      method: 'POST',
      header: { 'content-type': 'application/json', Authorization: `Bearer ${getToken()}` },
      data: { message: text },
      success: res => {
        let reply = '抱歉，AI 助手暂时不可用。'
        if (res.data && res.data.code === 200) reply = res.data.data.reply || reply
        this.setData({ messages: msgs.concat([{ role: 'assistant', content: reply }]), chatLoading: false, scrollTop: 99999 })
      },
      fail: () => {
        this.setData({ messages: msgs.concat([{ role: 'assistant', content: '网络连接失败' }]), chatLoading: false, scrollTop: 99999 })
      }
    })
  },

  generateReport() {
    if (this.data.reportLoading) return
    this.setData({ reportLoading: true })
    wx.showLoading({ title: 'AI 分析中...', mask: true })
    wx.request({
      url: buildUrl('/wx/nutrition/report'),
      method: 'GET',
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        wx.hideLoading()
        if (res.data && res.data.code === 200) {
          this.setData({ report: res.data.data, reportLoading: false })
        } else {
          wx.showToast({ title: '暂无足够数据', icon: 'none' })
          this.setData({ reportLoading: false })
        }
      },
      fail: () => { wx.hideLoading(); wx.showToast({ title: '网络连接失败', icon: 'none' }); this.setData({ reportLoading: false }) }
    })
  }
})
