const { getToken } = require('../../utils/auth')

Page({
  data: {
    profile: { nickName: '', phone: '', avatar: '', avatarText: '微' },
    tempAvatarPath: '',
    saving: false
  },

  onShow() {
    // 优先从后端获取最新数据
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.request({
      url: `${baseUrl}/wx/user/info`,
      method: 'GET',
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200 && res.data.data) {
          const user = res.data.data
          this.setData({
            profile: {
              nickName: user.nickName || '',
              phone: user.phone || '',
              avatar: user.avatar || '',
              avatarText: (user.nickName || '微')[0]
            }
          })
        } else { this.loadLocal() }
      },
      fail: () => this.loadLocal()
    })
  },

  loadLocal() {
    const userInfo = wx.getStorageSync('userInfo') || {}
    const oldProfile = wx.getStorageSync('profile') || {}
    this.setData({
      profile: {
        nickName: userInfo.nickName || oldProfile.nickname || '',
        phone: userInfo.phone || oldProfile.phone || '',
        avatar: userInfo.avatar || oldProfile.avatar || '',
        avatarText: (userInfo.nickName || oldProfile.nickname || '微')[0]
      }
    })
  },

  onChooseAvatar(e) {
    const { avatarUrl } = e.detail
    this.setData({ tempAvatarPath: avatarUrl })
    // 先更新预览
    this.setData({ 'profile.avatar': avatarUrl })
  },

  onNickNameInput(e) {
    this.setData({ 'profile.nickName': e.detail.value })
  },

  onField(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`profile.${key}`]: e.detail.value })
  },

  saveProfile() {
    const p = this.data.profile
    if (!p.nickName.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' }); return
    }
    if (this.data.saving) return
    this.setData({ saving: true })

    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    const doSave = () => {
      wx.request({
        url: `${baseUrl}/wx/user/info`,
        method: 'PUT',
        header: { 'content-type': 'application/json', Authorization: `Bearer ${getToken()}` },
        data: {
          nickName: p.nickName.trim(),
          phone: p.phone.trim(),
          avatar: p.avatar
        },
        success: () => {
          // 同步到本地存储
          const userInfo = {
            nickName: p.nickName.trim(),
            phone: p.phone.trim(),
            avatar: p.avatar,
            avatarText: p.nickName.trim()[0]
          }
          wx.setStorageSync('userInfo', userInfo)
          wx.showToast({ title: '保存成功', icon: 'success' })
          setTimeout(() => wx.navigateBack(), 800)
        },
        fail: () => {
          wx.showToast({ title: '已本地保存', icon: 'success' })
          setTimeout(() => wx.navigateBack(), 800)
        }
      })
    }

    // 如果有新头像，先上传
    if (this.data.tempAvatarPath) {
      wx.uploadFile({
        url: `${baseUrl}/admin/upload/image`,
        filePath: this.data.tempAvatarPath,
        name: 'file',
        header: { Authorization: `Bearer ${getToken()}` },
        success: (res) => {
          const data = JSON.parse(res.data)
          if (data.code === 200 && data.data) {
            this.setData({ 'profile.avatar': data.data.url || data.data.path })
          }
        },
        complete: () => doSave()
      })
    } else {
      doSave()
    }
  }
})
