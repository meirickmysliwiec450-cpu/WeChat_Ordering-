const { getToken } = require('../../utils/auth')

Page({
  data: {
    orderId: '',
    orderNo: '',
    score: 0,
    content: '',
    submitting: false,
    scoreHint: '',
    isUpdate: false
  },

  onLoad(options) {
    this.setData({
      orderId: options.orderId || '',
      orderNo: options.orderNo || ''
    })
    this.loadExistingReview()
  },

  loadExistingReview() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.request({
      url: `${baseUrl}/wx/comments/order/${this.data.orderId}`,
      method: 'GET',
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200 && res.data.data?.length > 0) {
          const r = res.data.data[0]
          this.setData({
            score: r.score || 0,
            content: r.content || '',
            isUpdate: true,
            scoreHint: ['', '非常差', '较差', '一般', '不错', '非常好'][r.score || 0]
          })
        }
      }
    })
  },

  setScore(e) {
    const score = e.currentTarget.dataset.score
    const hints = ['', '非常差', '较差', '一般', '不错', '非常好']
    this.setData({ score, scoreHint: hints[score] })
  },

  onContent(e) {
    this.setData({ content: e.detail.value })
  },

  submitReview() {
    if (this.data.submitting) return
    if (this.data.score === 0) {
      wx.showToast({ title: '请先评分', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')

    wx.request({
      url: `${baseUrl}/wx/comments`,
      method: 'POST',
      header: {
        'content-type': 'application/json',
        Authorization: `Bearer ${getToken()}`
      },
      data: {
        id: this.data.orderId,
        score: this.data.score,
        content: this.data.content
      },
      success: res => {
        if (res.statusCode >= 200 && res.statusCode < 300 && res.data.code === 200) {
          wx.showToast({ title: '评价成功', icon: 'success' })
          setTimeout(() => wx.navigateBack(), 1000)
        } else {
          wx.showToast({ title: res.data?.message || '评价失败', icon: 'none' })
          this.setData({ submitting: false })
        }
      },
      fail: () => {
        wx.showToast({ title: '网络异常', icon: 'none' })
        this.setData({ submitting: false })
      }
    })
  }
})
