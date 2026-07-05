const { getCart, getCartSummary, clearCart, createOrder, getDiningType,getDiningTypeText, setDiningType } = require('../../utils/store')
const { getToken, requireLogin } = require('../../utils/auth')

Page({
  data: {
    diningType: '',
    diningTypeText: '',
    cart: [],
    summary: { totalCount: 0, totalPrice: 0 },
    address: '',
    tableInfo: '',
    phone: '',
    remark: '',
    submitting: false,
    addressList: [],
    selectedAddr: null,
    showModePicker: false
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
      address: profile.address || this.data.address,
      phone: profile.phone || ''
    })
    if (diningType === 'takeout') this.loadAddresses()
  },

  loadAddresses() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.request({
      url: `${baseUrl}/wx/address`,
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200) {
          const list = res.data.data || []
          const def = list.find(a => a.isDefault) || list[0] || null
          this.setData({ addressList: list, selectedAddr: def })
        }
      }
    })
  },

  selectAddr(e) {
    const id = e.currentTarget.dataset.id
    const addr = this.data.addressList.find(a => a.id == id)
    this.setData({ selectedAddr: addr })
  },

  goAddAddress() {
    wx.navigateTo({ url: '/pages/address/address' })
  },

  onAddress(event) {
    this.setData({ address: event.detail.value })
  },

  onTableInfo(event) { this.setData({ tableInfo: event.detail.value }) },
  onPhone(event) { this.setData({ phone: event.detail.value }) },
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

  toggleMode() { this.setData({ showModePicker: !this.data.showModePicker }) },

  switchMode(e) {
    const type = e.currentTarget.dataset.type
    setDiningType(type)
    this.setData({
      diningType: type,
      diningTypeText: getDiningTypeText(type),
      showModePicker: false,
      selectedAddr: null
    })
    if (type === 'takeout') this.loadAddresses()
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
      payload.addressId = this.data.selectedAddr ? this.data.selectedAddr.id : null
    } else {
      payload.addressId = 0  // 堂食/自取不需要地址
    }
    return payload
  },

  submitOrder() {
    /* 生成订单信息 */
    if (this.data.submitting) return

    if (this.data.diningType === 'takeout' && !this.data.selectedAddr) {
      wx.showToast({ title: '请先选择收货地址', icon: 'none' })
      return
    }
    if (!this.data.phone.trim() || this.data.phone.trim().length < 11) {
      wx.showToast({ title: '请输入正确的11位手机号', icon: 'none' })
      return
    }
    this.createOrderByApi()
  },
  createOrderByApi() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    const payload = this.buildOrderPayload()
    const fullUrl = `${baseUrl}/wx/orders`
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
        const httpOk = res.statusCode >= 200 && res.statusCode < 300
        const bizOk = res.data && res.data.code === 200
        if (!httpOk || !bizOk) {
          wx.hideLoading()
          this.setData({ submitting: false })
          wx.showToast({ title: res.data?.message || '订单提交失败', icon: 'none', duration: 2000 })
          return
        }
        const orderId = res.data.data.orderId
        clearCart()
        wx.hideLoading()
        this.payOrder(orderId)
      },
      fail: (err) => {
        wx.hideLoading()
        this.setData({ submitting: false })
        
        let errorMsg = '无法连接后端'
        if (err.errMsg) {
          errorMsg += ': ' + err.errMsg
        }
        wx.showToast({ title: errorMsg, icon: 'none', duration: 3000 })
      }
    })
  },
  /* 生成支付信息 */
  payOrder(param) {
    console.log(param)
    let orderId = ''
    // 判断传入的是点击事件还是直接订单ID
    if (param && param.currentTarget) {
      // WXML按钮点击触发，从dataset拿id
      orderId = String(param.currentTarget.dataset.id || '').replace(/\D/g, '')
    } else {
      // 接口创建订单后主动调用，直接传id
      orderId = String(param).replace(/\D/g, '')
    }
  
    if (!orderId) {
      wx.showToast({ title: '订单ID缺失，无法支付', icon: 'none' })
      return
    }
  
    wx.showModal({
      title: '确认支付',
      content: '确认支付该订单？',
      success: res => {
        if (!res.confirm) {
          this.setData({ submitting: false })
          return
        }
        const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
        wx.request({
          url: `${baseUrl}/wx/orders/${orderId}`,
          method: 'PUT',
          header: { 'content-type': 'application/json', Authorization: `Bearer ${getToken()}` },
          data: { orderStatus: 1 },
          success: (apiRes) => {
            this.setData({ submitting: false })
            if (apiRes.data?.code === 200) {
              wx.showToast({ title: '支付成功', icon: 'success' })
              setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 600)
            } else {
              wx.showToast({ title: apiRes.data?.message || '支付失败', icon: 'none' })
            }
          },
          fail: () => wx.showToast({ title: '网络异常', icon: 'none' })
        })
      }
    })
  },
})
