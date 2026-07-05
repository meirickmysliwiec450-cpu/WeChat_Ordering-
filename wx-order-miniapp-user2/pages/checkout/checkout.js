const { getCart, getCartSummary, clearCart, createOrder, getDiningType, getDiningTypeText, setDiningType } = require('../../utils/store')
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
    if (this.data.submitting) return

    if (this.data.diningType === 'takeout' && !this.data.selectedAddr) {
      wx.showToast({ title: '请先选择收货地址', icon: 'none' })
      return
    }
    if (!this.data.phone.trim() || this.data.phone.trim().length < 11) {
      wx.showToast({ title: '请输入正确的11位手机号', icon: 'none' })
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
  },

  createOrderByApi() {
    console.log('========== 开始提交订单 ==========')
    
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    const payload = this.buildOrderPayload()
    const fullUrl = `${baseUrl}/wx/orders`
    
    console.log('完整请求地址:', fullUrl)
    console.log('请求参数:', payload)
    console.log('Token:', getToken())
    
    if (!baseUrl) {
      console.error('❌ baseUrl 未配置！')
      wx.showToast({ title: '请先配置后端地址', icon: 'none' })
      return
    }
    
    if (!payload.items || payload.items.length === 0) {
      console.error('❌ 购物车为空！')
      wx.showToast({ title: '购物车为空', icon: 'none' })
      return
    }

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
        console.log('✅ 请求成功响应:', res)
        console.log('响应状态码:', res.statusCode)
        console.log('响应数据:', res.data)
        
        const httpOk = res.statusCode >= 200 && res.statusCode < 300
        const bizOk = res.data && res.data.code === 200
        if (!httpOk || !bizOk) {
          console.error('❌ 订单提交失败，HTTP=' + res.statusCode + ', code=' + (res.data?.code))
          wx.hideLoading()
          this.setData({ submitting: false })
          // 显示具体错误信息
          wx.showToast({ title: res.data?.message || '订单提交失败', icon: 'none', duration: 2000 })
          // 仍然保存到本地，数据不丢失
          this.createLocalOrder(payload)
          setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 800)
          return
        }

        clearCart()
        wx.hideLoading()
        this.setData({ submitting: false })
        wx.showToast({ title: '结算成功，订单已提交', icon: 'success' })
        setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 600)
      },
      fail: (err) => {
        console.error('❌ 请求失败:', err)
        console.error('错误详情:', JSON.stringify(err))
        
        wx.hideLoading()
        this.setData({ submitting: false })
        
        let errorMsg = '无法连接后端'
        if (err.errMsg) {
          errorMsg += ': ' + err.errMsg
        }
        wx.showToast({ title: errorMsg, icon: 'none', duration: 3000 })
        
        // 尝试使用本地订单（临时方案）
        console.log('使用本地保存订单...')
        this.createLocalOrder(payload)
      }
    })
  }
})
