App({
  globalData: {
    shopName: '校园风味点餐',
    servicePhone: '400-888-0000',
    // 请根据你的后端服务地址修改，例如：http://localhost:8080 或 https://api.example.com
    // 本机测试用 localhost，真机调试用局域网IP
    // 模拟器用 localhost，真机扫码预览用你的局域网IP
    baseUrl: 'http://10.200.52.12:8080/api'
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
