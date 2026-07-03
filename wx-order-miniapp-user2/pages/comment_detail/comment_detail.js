const { getToken, requireLogin } = require('../../utils/auth')

Page({
  data: {
    orderId: '',
    comment: null,
    loading: true
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

    const photoUrl = raw.photoUrl || raw.imageUrl || raw.imgUrl || raw.picUrl || raw.fileUrl || raw.url || raw.photo || ''
    return {
      id: String(raw.id || raw.commentId || ''),
      orderId: String(raw.orderId || ''),
      content: raw.content || raw.text || raw.comment || raw.description || '',
      photoUrl,
      createTime: raw.createTime || raw.createdTime || raw.time || ''
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
