const { requireLogin, logout, getToken, buildUrl } = require('../../utils/auth')

Page({
  data: {
    profile: {},
    servicePhone: '',
    orderCount: 0,
    points: 0
  },

  onShow() {
    if (!requireLogin()) return
    this.setData({
      profile: wx.getStorageSync('profile') || {},
      servicePhone: getApp().globalData.servicePhone
    })
    this.loadStats()
  },

  loadStats() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    // 查订单数
    wx.request({
      url: `${baseUrl}/wx/orders?pageSize=1`,
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200) {
          this.setData({ orderCount: res.data.data?.total || 0 })
        }
      }
    })
    // 查支付总额 = 积分
    wx.request({
      url: `${baseUrl}/wx/payments`,
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200) {
          const total = (res.data.data || []).reduce((s, p) => s + (p.payAmount || 0), 0)
          this.setData({ points: Math.floor(total) })
        }
      }
    })
  },

  goEditProfile() { wx.navigateTo({ url: '/pages/edit_profile/edit_profile' }) },
  goAddress() { wx.navigateTo({ url: '/pages/address/address' }) },
  goPayments() { wx.navigateTo({ url: '/pages/payments/payments' }) },
  goOrders() { wx.switchTab({ url: '/pages/orders/orders' }) },
  goReviews() { wx.navigateTo({ url: '/pages/my_reviews/my_reviews' }) },
  goFeedback() { wx.navigateTo({ url: '/pages/feedback/feedback' }) },
  logout() { logout() }
})
