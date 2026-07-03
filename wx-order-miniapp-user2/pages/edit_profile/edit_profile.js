const { getToken } = require('../../utils/auth')

Page({
  data: {
    profile: { nickname: '', phone: '', avatarText: '顾' }
  },

  onShow() {
    const p = wx.getStorageSync('profile') || {}
    this.setData({ profile: { nickname: p.nickname || '', phone: p.phone || '', avatarText: p.avatarText || '顾' } })
  },

  onField(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`profile.${key}`]: e.detail.value })
  },

  saveProfile() {
    const p = this.data.profile
    if (!p.nickname.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' }); return
    }
    // 先存本地
    wx.setStorageSync('profile', { ...wx.getStorageSync('profile'), nickname: p.nickname.trim(), phone: p.phone.trim() })
    // 同步到后端数据库
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    if (baseUrl) {
      wx.request({
        url: `${baseUrl}/wx/user/info`,
        method: 'PUT',
        header: { 'content-type': 'application/json', Authorization: `Bearer ${getToken()}` },
        data: { nickName: p.nickname.trim(), phone: p.phone.trim() },
        success: () => wx.showToast({ title: '保存成功', icon: 'success' }),
        fail: () => wx.showToast({ title: '已本地保存', icon: 'success' })
      })
    } else {
      wx.showToast({ title: '保存成功', icon: 'success' })
    }
    setTimeout(() => wx.navigateBack(), 800)
  }
})
