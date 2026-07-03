const { getToken, requireLogin } = require('../../utils/auth')

Page({
  data: {
    orderId: '',
    comment: null,
    loading: true,
    scoreList: [1, 2, 3, 4, 5],
  },

  onLoad(options) {
    const orderId = options.orderId || ''
    this.setData({ orderId })
    this.loadComment(orderId)
  },

  onShow() {
    requireLogin()
  },

  loadComment(orderId) {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    if (!baseUrl) {
      this.useLocalComment(orderId)
      return
    }

    wx.request({
      url: `${baseUrl}/wx/comments/order/${orderId}`,
      method: 'GET',
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: res => {
        const ok = res.statusCode >= 200 && res.statusCode < 300
        if (!ok) {
          this.useLocalComment(orderId)
          return
        }
        const comment = this.normalizeComment(res.data)
        if (!comment) {
          this.useLocalComment(orderId)
          return
        }
        this.setData({ comment, loading: false })
      },
      fail: () => {
        this.useLocalComment(orderId)
      }
    })
  },

  normalizeComment(responseData) {
    const raw = responseData && responseData.data ? responseData.data : responseData
    if (!raw || typeof raw !== 'object') return null

    console.log(raw[0].content)
    return {
      id: String(raw[0].id || ''),
      orderId: String(raw[0].orderId || ''),
      content: raw[0].content || '',
      photo:raw[0].photo,
      createTime: raw[0].createTime || '',
      score:raw[0].score
    }
  },

  useLocalComment(orderId) {
    const feedbacks = wx.getStorageSync('feedbacks') || []
    const fb = feedbacks.find(item => String(item.orderId) === String(orderId))
    if (fb) {
      this.setData({
        comment: {
          id: fb.id,
          orderId: fb.orderId,
          content: fb.content,
          photoUrl: fb.photo,
          createTime: fb.time
        },
        loading: false
      })
    } else {
      this.setData({ comment: null, loading: false })
    }
  },

  onPhotoError() {
    // 图片加载失败，保持占位
  },

  goBack() {
    wx.navigateBack()
  }
})
