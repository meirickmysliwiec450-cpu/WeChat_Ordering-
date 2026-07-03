const { requireLogin, logout } = require('../../utils/auth')

Page({
  data: {
    profile: {},
    servicePhone: ''
  },

  onShow() {
    if (!requireLogin()) return

    this.setData({
      profile: wx.getStorageSync('profile') || {},
      servicePhone: getApp().globalData.servicePhone
    })
  },

  onInput(event) {
    const key = event.currentTarget.dataset.key
    this.setData({
      [`profile.${key}`]: event.detail.value
    })
  },

  saveProfile() {
    const profile = Object.assign({}, this.data.profile)
    profile.avatarText = (profile.nickname || '顾').slice(0, 1)
    wx.setStorageSync('profile', profile)
    this.setData({ profile })
    wx.showToast({ title: '保存成功', icon: 'success' })
  },

  goOrders() {
    wx.switchTab({ url: '/pages/orders/orders' })
  },

  goFeedback() {
    wx.navigateTo({ url: '/pages/feedback/feedback' })
  },

  logout() {
    logout()
  }
})
