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
    this.loadProfile()
    this.setData({
      servicePhone: getApp().globalData.servicePhone
    })
    this.loadStats()
  },

  loadProfile() {
    // 优先从后端获取最新用户信息
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.request({
      url: `${baseUrl}/wx/user/info`,
      method: 'GET',
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200 && res.data.data) {
          const user = res.data.data
          const p = {
            nickName: user.nickName || '微信用户',
            avatar: user.avatar || '',
            avatarText: (user.nickName || '微')[0],
            phone: user.phone || ''
          }
          // 同步到本地存储
          wx.setStorageSync('userInfo', p)
          this.setData({ profile: p })
        } else {
          this.useLocalProfile()
        }
      },
      fail: () => this.useLocalProfile()
    })
  },

  useLocalProfile() {
    const userInfo = wx.getStorageSync('userInfo') || {}
    const oldProfile = wx.getStorageSync('profile') || {}
    const p = {
      nickName: userInfo.nickName || oldProfile.nickname || '微信用户',
      avatar: userInfo.avatar || oldProfile.avatar || '',
      avatarText: (userInfo.nickName || oldProfile.nickname || '微')[0],
      phone: userInfo.phone || oldProfile.phone || ''
    }
    this.setData({ profile: p })
  },

  loadStats() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    // 查订单数（只统计已支付和已完成）
    wx.request({
      url: `${baseUrl}/wx/orders?pageSize=999`,
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200) {
          const allOrders = res.data.data?.list || []
          const paidCount = allOrders.filter(o => o.orderStatus === 1 || o.orderStatus === 2).length
          this.setData({ orderCount: paidCount })
        }
      }
    })
    // 查支付总额 = 积分（只统计已支付订单产生的支付记录）
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
