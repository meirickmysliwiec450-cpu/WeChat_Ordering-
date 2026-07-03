App({
  globalData: {
    shopName: '校园风味点餐',
    servicePhone: '400-888-0000',
    // 请根据你的后端服务地址修改，例如：http://localhost:8080 或 https://api.example.com
    baseUrl: 'http://localhost:8080/api'
  },

  onLaunch() {
    this.initStorage()
  },

  initStorage() {
    if (!wx.getStorageSync('profile')) {
      wx.setStorageSync('profile', {
        nickname: '微信用户',
        phone: '',
        address: '',
        avatarText: '顾'
      })
    }
    if (!wx.getStorageSync('cart')) {
      wx.setStorageSync('cart', [])
    }
    if (!wx.getStorageSync('orders')) {
      wx.setStorageSync('orders', [])
    }
    if (!wx.getStorageSync('feedbacks')) {
      wx.setStorageSync('feedbacks', [])
    }
  }
})
