const { addToCart, getDiningType, setDiningType, getDiningTypeText } = require('../../utils/store')
const { requireLogin, getToken, buildUrl } = require('../../utils/auth')

Page({
  data: {
    shopName: '校园风味点餐',
    hotDishes: [],
    diningType: '',
    diningTypeText: '未选择',
    dishList: []
  },

  onLoad() {
    this.setData({ shopName: getApp().globalData.shopName })
  },

  onShow() {
    requireLogin()
    this.refreshDiningType()
    this.fetchBanners()
    this.fetchDishes()
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
