const { isLoggedIn, loginByCode } = require('../../utils/auth')

Page({
  data: {
    loading: false
  },

  onLoad() {
    if (isLoggedIn()) {
      wx.switchTab({ url: '/pages/home/home' })
    }
  },

  handleLogin() {
    if (this.data.loading) return

    this.setData({ loading: true })
    wx.showLoading({ title: '登录中' })

    loginByCode()
      .then(() => {
        wx.hideLoading()
        wx.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          wx.switchTab({ url: '/pages/home/home' })
        }, 500)
      })
      .catch(error => {
        wx.hideLoading()
        wx.showModal({
          title: '登录失败',
          content: error.message || '请检查后端服务地址、接口返回格式或网络配置',
          showCancel: false
        })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  }
})
