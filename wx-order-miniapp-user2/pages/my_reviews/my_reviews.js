const { getToken } = require('../../utils/auth')

Page({
  data: {
    reviews: [],
    loading: true
  },

  onShow() {
    this.loadReviews()
  },

  loadReviews() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    this.setData({ loading: true })

    wx.request({
      url: `${baseUrl}/wx/comments/my`,
      method: 'GET',
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.statusCode === 200 && res.data?.code === 200) {
          const list = (res.data.data || []).map(item => ({
            id: item.id,
            orderId: item.orderId,
            score: item.score || 5,
            content: item.content || '',
            createTime: (item.createTime || '').substring(0, 10)
          }))
          this.setData({ reviews: list, loading: false })
        } else {
          this.setData({ loading: false })
        }
      },
      fail: () => {
        this.setData({ loading: false })
      }
    })
  }
})
