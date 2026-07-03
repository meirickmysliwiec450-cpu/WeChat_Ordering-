const { dishes } = require('../../utils/mock')
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
    this.setData({
      shopName: getApp().globalData.shopName,
      hotDishes: dishes.filter(item => item.categoryId === 'hot')
    })
  },

  onShow() {
    requireLogin()
    this.refreshDiningType()
    this.fetchDishes()
  },

  fetchDishes() {
    const token = getToken()
    if (!token) return
    wx.request({
      url: buildUrl('/wx/dishes'),
      method: 'GET',
      header: { Authorization: `Bearer ${token}` },
      success: res => {
        if (res.data && res.data.code === 200) {
          this.setData({ dishList: res.data.data || [] })
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
      wx.showToast({ title: '请先选择堂食或外送', icon: 'none' })
      return
    }
    wx.switchTab({ url: '/pages/menu/menu' })
  },

  goSearch() { wx.navigateTo({ url: '/pages/search/search' }) },
  goMenu() { this.startOrder() },
  goOrders() { wx.switchTab({ url: '/pages/orders/orders' }) },

  goDishDetail(e) {
    wx.navigateTo({ url: `/pages/dish_detail/dish_detail?id=${e.currentTarget.dataset.id}` })
  },

  addCart(event) {
    if (!this.data.diningType) {
      wx.showToast({ title: '请先选择堂食或外送', icon: 'none' })
      return
    }
    addToCart(event.detail.id)
    wx.showToast({ title: '已加入购物栏', icon: 'success' })
  }
})
