const { requireLogin, getToken } = require('../../utils/auth')
const { updateOrder } = require('../../utils/store')

Page({
  data: {
    content: '',
    feedbacks: []
  },

  onShow() {
    if (!requireLogin()) return
    this.getFeedbacksFromApi()
  },

  getFeedbacksFromApi() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    if (!baseUrl) {
      // 无后端地址，读取本地缓存
      this.refreshLocalFeedbacks()
      return
    }
    wx.request({
      url: `${baseUrl}/wx/feedbacks`,
      method: 'GET',
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: res => {
        if (res.statusCode >= 200 && res.statusCode < 300 && res.data.code === 200) {
          // 格式化后端返回列表，统一字段
          const list = Array.isArray(res.data.data) ? res.data.data.map(item => ({
            id: item.id,
            content: item.content,
            createTime: item.createTime
          })) : []
          this.setData({ feedbacks: list })
        } else {
          // 接口异常降级本地缓存
          this.refreshLocalFeedbacks()
        }
      },
      fail: () => {
        // 网络失败读本地缓存
        this.refreshLocalFeedbacks()
      }
    })
  },
  // 监听输入框内容
  onContent(event) {
    this.setData({ content: event.detail.value })
  },

  // 提交反馈（线上接口优先，失败走本地缓存）
  submitFeedback() {
    const { content } = this.data
    // 非空校验
    if (!content.trim()) {
      wx.showToast({ title: '请填写问题描述', icon: 'none' })
      return
    }

    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    // 有后端地址，调用线上接口
    if (baseUrl) {
      wx.showLoading({ title: '提交中...' })
      wx.request({
        url: `${baseUrl}/wx/feedbacks`,
        method: 'POST',
        header: {
          'Content-Type': 'application/json',
          ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {})
        },
        data: {
          content: content.trim()
        },
        success: res => {
          wx.hideLoading()
          if (res.statusCode >= 200 && res.statusCode < 300 && res.data.code === 200) {
            wx.showToast({ title: '反馈成功', icon: 'success' })
            this.setData({ content: '' })
            setTimeout(() => wx.switchTab({ url: '/pages/profile/profile' }), 700)
          } else {
            wx.showToast({ title: res.data.message || '提交失败，已保存本地', icon: 'none' })
            this.saveLocalFeedback()
          }
        },
        fail: () => {
          wx.hideLoading()
          wx.showToast({ title: '网络异常，已保存本地', icon: 'none' })
          this.saveLocalFeedback()
        }
      })
    } else {
      // 无后端地址，直接本地存储
      this.saveLocalFeedback()
    }
  },

  // 本地缓存保存反馈（不需要order、photo、orderId）
  saveLocalFeedback() {
    const feedbacks = wx.getStorageSync('feedbacks') || []
    const now = new Date()
    const pad = value => String(value).padStart(2, '0')
    const feedback = {
      id: 'F' + now.getTime(),
      content: this.data.content.trim(),
      time: `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}`
    }
    feedbacks.unshift(feedback)
    wx.setStorageSync('feedbacks', feedbacks)

    this.setData({
      content: '',
      feedbacks
    })
    wx.showToast({ title: '反馈成功', icon: 'success' })
    setTimeout(() => wx.switchTab({ url: '/pages/profile/profile' }), 700)
  }
})