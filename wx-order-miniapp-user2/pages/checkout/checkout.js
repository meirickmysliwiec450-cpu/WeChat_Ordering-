const { getCart, getCartSummary, clearCart, createOrder, getDiningType, getDiningTypeText } = require('../../utils/store')
const { getToken, requireLogin } = require('../../utils/auth')

Page({
  data: {
    diningType: '',
    diningTypeText: '',
    cart: [],
    summary: { totalCount: 0, totalPrice: 0 },
    address: '',
    tableInfo: '',
    remark: '',
    submitting: false
  },

  onShow() {
    if (!requireLogin()) return

    const diningType = getDiningType()
    const cart = getCart()
    const profile = wx.getStorageSync('profile') || {}

    if (!diningType) {
      wx.showToast({ title: '请先选择堂食或外送', icon: 'none' })
      setTimeout(() => wx.switchTab({ url: '/pages/home/home' }), 500)
      return
    }

    if (!cart.length) {
      wx.showToast({ title: '购物栏为空', icon: 'none' })
      setTimeout(() => wx.switchTab({ url: '/pages/menu/menu' }), 500)
      return
    }

    this.setData({
      diningType,
      diningTypeText: getDiningTypeText(diningType),
      cart,
      summary: getCartSummary(cart),
      address: profile.address || this.data.address
    })
  },

  onAddress(event) {
    this.setData({ address: event.detail.value })
  },

  onTableInfo(event) {
    this.setData({ tableInfo: event.detail.value })
  },

  onRemark(event) {
    this.setData({ remark: event.detail.value })
  },

  chooseAddress() {
    wx.chooseAddress({
      success: res => {
        const address = `${res.provinceName}${res.cityName}${res.countyName}${res.detailInfo} ${res.userName} ${res.telNumber}`
        this.setData({ address })
      },
      fail: () => {
        wx.showToast({ title: '可手动填写地址', icon: 'none' })
      }
    })
  },

  backHome() {
    wx.switchTab({ url: '/pages/home/home' })
  },

  buildOrderPayload() {
    const data = this.data
    const payload = {
      diningType: data.diningType,
      diningTypeText: getDiningTypeText(data.diningType),
      remark: data.remark,
      address: data.diningType === 'takeout' ? data.address : '',
      tableInfo: data.diningType === 'dineIn' ? data.tableInfo : '',
      totalCount: data.summary.totalCount,
      totalPrice: data.summary.totalPrice,
      items: data.cart.map(item => ({
        dishId: item.id,
        name: item.name,
        price: item.price,
        count: item.count,
        amount: Number((item.price * item.count).toFixed(2))
      }))
    }
  
    if (data.diningType === 'takeout') {
      const selectAddress = wx.getStorageSync('selectAddress') || {}
      // 兜底：没有地址默认赋值空，后端会拦截
      payload.addressId = selectAddress.id ?? null
    } else {
      // 堂食固定传0
      payload.addressId = 0
    }
    return payload
  },

  submitOrder() {
    if (this.data.submitting) return

    if (this.data.diningType === 'takeout' && !this.data.address.trim()) {
      wx.showToast({ title: '请选择或填写外送地址', icon: 'none' })
      return
    }

    wx.showModal({
      title: '确认结算',
      content: `本单需支付 ￥${this.data.summary.totalPrice}，是否继续？`,
      success: res => {
        if (!res.confirm) return
        this.createOrderByApi()
      }
    })
  },

  createOrderByApi() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    const payload = this.buildOrderPayload()
    const fullUrl = `${baseUrl}/wx/orders`
    console.log('=== 完整请求地址 ===', fullUrl)
    console.log('请求参数payload', payload)
  
    this.setData({ submitting: true })
    wx.showLoading({ title: '结算中' })
  
    wx.request({
      url: fullUrl,
      method: 'POST',
      header: {
        'content-type': 'application/json',
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      data: payload,
      success: res => {
        console.log('请求成功响应', res)
        const ok = res.statusCode >= 200 && res.statusCode < 300
        if (!ok) {
          wx.hideLoading()
          this.setData({ submitting: false })
          wx.showToast({ title: '订单提交失败：' + (res.data?.msg || '服务异常'), icon: 'none' })
          return
        }
  
        clearCart()
        wx.hideLoading()
        this.setData({ submitting: false })
        wx.showToast({ title: '结算成功，订单已提交', icon: 'success' })
        setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 600)
      },
      fail: (err) => {
        console.error('请求失败err', err)
        wx.hideLoading()
        this.setData({ submitting: false })
        wx.showToast({ title: '无法连接后端，仅本地保存', icon: 'none' })
      }
    })
  },

  createLocalOrder(payload) {
    createOrder(Object.assign({}, payload, {
      status: 'paid',
      statusText: '已支付',
      feedback: null
    }))
    clearCart()
    wx.hideLoading()
    this.setData({ submitting: false })
    wx.showToast({ title: '已本地保存订单', icon: 'success' })
    setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 600)
  }
})
