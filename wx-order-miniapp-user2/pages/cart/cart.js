const { getCart, updateCartItem, getCartSummary, createOrder, clearCart } = require('../../utils/store')
const { requireLogin } = require('../../utils/auth')

Page({
  data: {
    cart: [],
    summary: { totalCount: 0, totalPrice: 0 },
    address: '',
    remark: ''
  },

  onShow() {
    if (!requireLogin()) return

    const profile = wx.getStorageSync('profile') || {}
    this.setData({
      address: profile.address || this.data.address
    })
    this.refreshCart()
  },

  refreshCart() {
    const cart = getCart()
    this.setData({
      cart,
      summary: getCartSummary(cart)
    })
  },

  changeCount(event) {
    updateCartItem(event.currentTarget.dataset.id, Number(event.currentTarget.dataset.count))
    this.refreshCart()
  },

  onAddress(event) {
    this.setData({ address: event.detail.value })
  },

  onRemark(event) {
    this.setData({ remark: event.detail.value })
  },

  goMenu() {
    wx.switchTab({ url: '/pages/menu/menu' })
  },

  submitOrder() {
    if (!this.data.address.trim()) {
      wx.showToast({ title: '请填写取餐信息', icon: 'none' })
      return
    }

    wx.showModal({
      title: '确认支付',
      content: `需支付 ￥${this.data.summary.totalPrice}，是否继续？`,
      success: res => {
        if (!res.confirm) return
        wx.showLoading({ title: '支付中' })
        setTimeout(() => {
          wx.hideLoading()
          const order = createOrder({
            items: this.data.cart,
            totalCount: this.data.summary.totalCount,
            totalPrice: this.data.summary.totalPrice,
            address: this.data.address,
            remark: this.data.remark
          })
          clearCart()
          this.setData({ remark: '' })
          this.refreshCart()
          wx.showToast({ title: '支付成功', icon: 'success' })
          setTimeout(() => {
            wx.switchTab({ url: '/pages/orders/orders' })
          }, 600)
        }, 700)
      }
    })
  }
})
