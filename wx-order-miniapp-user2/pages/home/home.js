const { addToCart, getDiningType, setDiningType, getDiningTypeText } = require('../../utils/store')
const { requireLogin, getToken, buildUrl } = require('../../utils/auth')

Page({
  data: {
    shopName: '校园风味点餐',
    hotDishes: [],
    diningType: '',
    // 今日推荐
    recDishes: [],
    recPeriod: '',
    diningTypeText: '未选择',
    dishList: [],
  },

  onLoad() {
    this.setData({ shopName: getApp().globalData.shopName })
    this.loadCachedOrFetch()
    this.loadQuickDishes()
    this.fetchBanners()
    this.fetchDishes()
  },

  onShow() {
    this.refreshDiningType()
    setTimeout(() => requireLogin(), 300)
    setTimeout(()=>{
      console.log("recDishes数组：", this.data.recDishes, "长度", this.data.recDishes.length)
    }, 1000)
  },

  loadCachedOrFetch() {
    const cache = wx.getStorageSync('ai_rec_cache')
    if (cache && cache.time && (Date.now() - cache.time < 300000)) {
      this.setData({ recDishes: cache.dishes, recPeriod: cache.period })
      return
    }
    this.fetchRecommend()
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
  // 轮播图：从 t_banner 读取，匹配对应菜品实现点击跳转
  fetchBanners() {
    const token = getToken()
    if (!token) return
    wx.request({
      url: buildUrl('/wx/banners'),
      method: 'GET',
      header: { Authorization: `Bearer ${token}` },
      success: res => {
        if (res.data && res.data.code === 200) {
          const banners = res.data.data || []
          // 同时加载菜品列表用于匹配
          wx.request({
            url: buildUrl('/wx/dishes'),
            method: 'GET',
            header: { Authorization: `Bearer ${token}` },
            success: dishRes => {
              const dishes = (dishRes.data?.data) || []
              const list = banners.map(b => {
                // 按图片URL匹配菜品
                const matched = dishes.find(d => d.image && b.imageUrl && d.image.includes(b.imageUrl.split('/').pop()))
                return {
                  id: matched ? matched.id : b.id,
                  dishName: b.title,
                  image: b.imageUrl,
                  price: matched ? matched.price : ''
                }
              })
              this.setData({ dishList: list })
            }
          })
        }
      }
    })
  },

  // 热销菜品：按销量排序
  fetchDishes() {
    const token = getToken()
    if (!token) return
    wx.request({
      url: buildUrl('/wx/dishes'),
      method: 'GET',
      header: { Authorization: `Bearer ${token}` },
      success: res => {
        if (res.data && res.data.code === 200) {
          const all = res.data.data || []
          const sorted = [...all].sort((a, b) => (b.sales || 0) - (a.sales || 0))
          this.setData({ hotDishes: sorted.slice(0, 6) })
        }
      }
    })
  },

  refreshDiningType() {
    const diningType = getDiningType()
    this.setData({ diningType, diningTypeText: diningType ? getDiningTypeText(diningType) : '未选择' })
  },

  selectDiningType(e) {
    setDiningType(e.currentTarget.dataset.type)
    this.refreshDiningType()
    wx.showToast({ title: `已选择${getDiningTypeText(e.currentTarget.dataset.type)}`, icon: 'success' })
  },

  startOrder() {
    if (!this.data.diningType) {
      wx.showToast({ title: '请先选择用餐方式', icon: 'none' })
      return
    }
    wx.switchTab({ url: '/pages/menu/menu' })
  },

  goDishDetail(e) {
    wx.navigateTo({ url: `/pages/dish_detail/dish_detail?id=${e.currentTarget.dataset.id}` })
  },

  addCart(event) {
    if (!this.data.diningType) {
      wx.showToast({ title: '请先选择用餐方式', icon: 'none' })
      return
    }
    addToCart(event.detail.dish || event.detail)
    wx.showToast({ title: '已加入购物栏', icon: 'success' })
  }
})
