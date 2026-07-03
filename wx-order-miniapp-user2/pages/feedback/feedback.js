const { requireLogin } = require('../../utils/auth')
const { updateOrder } = require('../../utils/store')

Page({
  data: {
    orderId: '',
    order: null,
    photo: '',
    content: '',
    feedbacks: []
  },

  onLoad(options) {
    this.setData({ orderId: options.orderId || '' })
  },

  onShow() {
    if (!requireLogin()) return
    this.refreshData()
  },

  refreshData() {
    const orders = wx.getStorageSync('orders') || []
    const order = orders.find(item => item.id === this.data.orderId) || null
    const feedbacks = wx.getStorageSync('feedbacks') || []
    this.setData({
      order,
      feedbacks
    })
  },

  choosePhoto() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: res => {
        const file = res.tempFiles && res.tempFiles[0]
        this.setData({ photo: file ? file.tempFilePath : '' })
      },
      fail: () => {
        wx.chooseImage({
          count: 1,
          sourceType: ['album', 'camera'],
          success: res => {
            this.setData({ photo: res.tempFilePaths[0] })
          }
        })
      }
    })
  },

  onContent(event) {
    this.setData({ content: event.detail.value })
  },

  submitFeedback() {
    if (!this.data.order) {
      wx.showToast({ title: '请从已完成订单进入反馈', icon: 'none' })
      return
    }
    if (this.data.order.status !== 'completed') {
      wx.showToast({ title: '订单完成后才可反馈', icon: 'none' })
      return
    }
    if (!this.data.photo) {
      wx.showToast({ title: '请上传反馈照片', icon: 'none' })
      return
    }
    if (!this.data.content.trim()) {
      wx.showToast({ title: '请填写问题描述', icon: 'none' })
      return
    }

    const feedbacks = wx.getStorageSync('feedbacks') || []
    const now = new Date()
    const pad = value => String(value).padStart(2, '0')
    const feedback = {
      id: 'F' + now.getTime(),
      orderId: this.data.order.id,
      photo: this.data.photo,
      content: this.data.content,
      time: `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}`
    }

    feedbacks.unshift(feedback)
    wx.setStorageSync('feedbacks', feedbacks)
    updateOrder(this.data.order.id, { feedback })

    this.setData({
      photo: '',
      content: '',
      feedbacks
    })
    wx.showToast({ title: '反馈成功', icon: 'success' })
    setTimeout(() => wx.switchTab({ url: '/pages/orders/orders' }), 700)
  }
})
