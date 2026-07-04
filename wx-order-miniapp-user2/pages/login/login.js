const { isLoggedIn, loginByCode, getToken } = require('../../utils/auth')

Page({
  data: {
    loading: false,
    savingProfile: false,
    loggedIn: false,
    needProfile: false,
    avatarUrl: '',
    nickName: '',
    tempAvatarPath: ''  // chooseAvatar 返回的临时路径
  },

  onLoad() {
    if (isLoggedIn()) {
      wx.switchTab({ url: '/pages/home/home' })
    }
  },

  handleLogin() {
    if (this.data.loading) return

    this.setData({ loading: true })
    wx.showLoading({ title: '登录中' })

    loginByCode()  // 纯静默登录
      .then((result) => {
        wx.hideLoading()
        const userInfo = wx.getStorageSync('userInfo') || result.userInfo || {}
        // 判断是否需要完善资料（无头像或无昵称）
        const needProfile = !userInfo.avatar && (!userInfo.nickName || userInfo.nickName === '微信用户')
        if (needProfile) {
          this.setData({ loggedIn: true, needProfile: true, loading: false })
        } else {
          wx.showToast({ title: '登录成功', icon: 'success' })
          setTimeout(() => wx.switchTab({ url: '/pages/home/home' }), 500)
        }
      })
      .catch(error => {
        wx.hideLoading()
        wx.showModal({
          title: '登录失败',
          content: error.message || '请检查后端服务地址、接口返回格式或网络配置',
          showCancel: false
        })
        this.setData({ loading: false })
      })
  },

  onChooseAvatar(e) {
    const { avatarUrl } = e.detail
    this.setData({ tempAvatarPath: avatarUrl, avatarUrl: avatarUrl })
  },

  onNickNameInput(e) {
    this.setData({ nickName: e.detail.value })
  },

  onNickNameBlur(e) {
    this.setData({ nickName: e.detail.value })
  },

  submitProfile() {
    if (this.data.savingProfile) return
    this.setData({ savingProfile: true })
    // 真机上延迟一下确保 blur 事件已触发
    setTimeout(() => {
      this._doSubmitProfile()
    }, 150)
  },

  _doSubmitProfile() {
    const { tempAvatarPath, nickName } = this.data
    if (!nickName.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' })
      this.setData({ savingProfile: false })
      return
    }

    this.setData({ savingProfile: true })
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')

    // 如果有新头像，先上传
    const doUpdate = () => {
      wx.request({
        url: `${baseUrl}/wx/user/info`,
        method: 'PUT',
        header: {
          'content-type': 'application/json',
          Authorization: `Bearer ${getToken()}`
        },
        data: { nickName, avatar: this.data.avatarUrl },
        success: (res) => {
          if (res.data?.code === 200) {
            // 更新本地缓存
            const ui = wx.getStorageSync('userInfo') || {}
            ui.nickName = nickName
            ui.avatar = this.data.avatarUrl
            wx.setStorageSync('userInfo', ui)
            wx.showToast({ title: '设置完成', icon: 'success' })
            setTimeout(() => wx.switchTab({ url: '/pages/home/home' }), 500)
          } else {
            wx.showToast({ title: '保存失败', icon: 'none' })
            this.setData({ savingProfile: false })
          }
        },
        fail: () => {
          wx.showToast({ title: '网络异常', icon: 'none' })
          this.setData({ savingProfile: false })
        }
      })
    }

    if (tempAvatarPath) {
      wx.uploadFile({
        url: `${baseUrl}/admin/upload/image`,
        filePath: tempAvatarPath,
        name: 'file',
        header: { Authorization: `Bearer ${getToken()}` },
        success: (res) => {
          const data = JSON.parse(res.data)
          if (data.code === 200 && data.data) {
            this.setData({ avatarUrl: data.data.url || data.data.path })
          }
          doUpdate()
        },
        fail: () => doUpdate()
      })
    } else {
      doUpdate()
    }
  }
})
