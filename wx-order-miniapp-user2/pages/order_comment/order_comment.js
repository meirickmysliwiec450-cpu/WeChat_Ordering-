const { requireLogin, getToken } = require('../../utils/auth')
const { updateOrder } = require('../../utils/store')

Page({
  data: {
    orderId: '',
    order: null,
    photo: '', // 本地临时图片路径
    imgUrl: '', // 上传后后端返回的线上图片地址
    content: '',
    score: 5,
    scoreList: [1, 2, 3, 4, 5],
    submitting: false
  },

  onLoad(options) {
    const orderId = options.orderId || ''
    this.setData({ orderId })
    if (orderId) {
      this.refreshData()
    }
  },

  onShow() {
    if (!requireLogin()) return
    if (this.data.orderId) {
      this.refreshData()
    }
  },

  refreshData() {
    const orders = wx.getStorageSync('orders') || []
    const order = orders.find(item => item.id === this.data.orderId) || null
    this.setData({ order })
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

  selectScore(event) {
    this.setData({ score: Number(event.currentTarget.dataset.score) })
  },
  submitFeedback() {
    const { orderId, photo, content,score, submitting } = this.data
    // 完整校验逻辑恢复
    if (!content.trim()) {
      wx.showToast({ title: '请填写问题描述', icon: 'none' })
      return
    }
    if (submitting) return
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    if (!baseUrl) {
      this.saveFeedbackLocal()
      return
    }

    this.setData({ submitting: true })
    wx.showLoading({ title: '图片上传中...' })

    // 第一步：单独上传图片接口（后端需要提供 /upload 接口，只接收文件返回url）
    wx.uploadFile({
      url: `${baseUrl}/common/upload/image`,
      filePath: photo,
      name: 'file',
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: uploadRes => {
        const uploadResult = JSON.parse(uploadRes.data)
        if (uploadResult.code !== 200 || !uploadResult.data) {
          wx.hideLoading()
          this.setData({ submitting: false })
          wx.showToast({ title: '图片上传失败', icon: 'none' })
          return
        }
        // 保存线上图片地址
        this.setData({ imgUrl: uploadResult.data })
        wx.showLoading({ title: '提交评价...' })
        wx.request({
          url: `${baseUrl}/wx/comments`,
          method: "POST",
          header: {
            'Content-Type': 'application/json', // JSON格式关键头
            Authorization: getToken() ? `Bearer ${getToken()}` : ''
          },
          data: {
            id: orderId,
            content: content,
            score:score,
            photo: this.data.imgUrl // JSON体携带图片线上地址
          },
          success: reqRes => {
            wx.hideLoading()
            this.setData({ submitting: false })
            if (reqRes.statusCode >= 200 && reqRes.statusCode < 300 && reqRes.data.code === 200) {
              wx.showToast({ title: '反馈成功', icon: 'success' })
              // 清空表单
              this.setData({ photo: '', imgUrl: '', content: '' })
              setTimeout(() => {
                const pages = getCurrentPages()
                const prev = pages[pages.length - 2]
                if (prev && prev.loadComment) prev.loadComment(orderId)
                if (prev && prev.refreshOrders) prev.refreshOrders()
                wx.navigateBack()
              }, 700)
            } else {
              wx.showToast({ title: '提交失败', icon: 'none' })
            }
          },
          fail: () => {
            wx.hideLoading()
            this.setData({ submitting: false })
            wx.showToast({ title: '提交请求异常', icon: 'none' })
          }
        })
      },
      fail: () => {
        wx.hideLoading()
        this.setData({ submitting: false })
        wx.showToast({ title: '图片上传失败，请重试', icon: 'none' })
      }
    })
  },

  saveFeedbackLocal() {
    const feedbacks = wx.getStorageSync('feedbacks') || []
    const now = new Date()
    const pad = value => String(value).padStart(2, '0')
    const feedback = {
      id: 'F' + now.getTime(),
      orderId: this.data.orderId, // 修复原代码bug：不用order.id防止空报错
      photo: this.data.photo,
      content: this.data.content,
      time: `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}`
    }
    feedbacks.unshift(feedback)
    wx.setStorageSync('feedbacks', feedbacks)
    updateOrder(this.data.orderId, { feedback })
    wx.showToast({ title: '反馈成功', icon: 'success' })
    this.setData({ photo: '', content: '' })
    setTimeout(() => wx.navigateBack(), 700)
  }
})