const { getToken } = require('../../utils/auth')

Page({
  data: {
    list: [], loading: true,
    showEdit: false, editId: null,
    form: { receiver: '', phone: '', addressDetail: '' }
  },

  onShow() { this.loadList() },

  loadList() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.request({
      url: `${baseUrl}/wx/address`,
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200) {
          this.setData({ list: res.data.data || [], loading: false })
        } else {
          this.setData({ loading: false })
        }
      },
      fail: () => this.setData({ loading: false })
    })
  },

  showForm() { this.setData({ showEdit: true, editId: null, form: { receiver: '', phone: '', addressDetail: '' } }) },
  hideForm() { this.setData({ showEdit: false }) },

  editAddr(e) {
    const item = e.currentTarget.dataset.item
    this.setData({
      showEdit: true, editId: item.id,
      form: { receiver: item.receiver || '', phone: item.phone || '', addressDetail: item.addressDetail || '' }
    })
  },

  onField(e) {
    const key = e.currentTarget.dataset.key
    this.setData({ [`form.${key}`]: e.detail.value })
  },

  saveAddr() {
    const { receiver, phone, addressDetail } = this.data.form
    if (!receiver || !phone || !addressDetail) return
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')

    const data = { receiver: receiver.trim(), phone: phone.trim(), addressDetail: addressDetail.trim() }
    const method = this.data.editId ? 'PUT' : 'POST'
    const url = this.data.editId ? `${baseUrl}/wx/address/${this.data.editId}` : `${baseUrl}/wx/address`

    wx.request({
      url, method,
      header: { 'content-type': 'application/json', Authorization: `Bearer ${getToken()}` },
      data,
      success: res => {
        if (res.data?.code === 200) {
          wx.showToast({ title: '保存成功', icon: 'success' })
          this.hideForm()
          this.loadList()
        } else {
          wx.showToast({ title: res.data?.message || '保存失败', icon: 'none' })
        }
      },
      fail: () => wx.showToast({ title: '网络异常', icon: 'none' })
    })
  },

  delAddr(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个地址吗？',
      success: res => {
        if (!res.confirm) return
        const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
        wx.request({
          url: `${baseUrl}/wx/address/${id}`,
          method: 'DELETE',
          header: { Authorization: `Bearer ${getToken()}` },
          success: res => {
            if (res.data?.code === 200) {
              wx.showToast({ title: '已删除', icon: 'success' })
              this.loadList()
            }
          }
        })
      }
    })
  },

  setDefault(e) {
    const id = e.currentTarget.dataset.id
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.request({
      url: `${baseUrl}/wx/address/${id}/default`,
      method: 'PUT',
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200) {
          wx.showToast({ title: '已设为默认', icon: 'success' })
          this.loadList()
        }
      }
    })
  }
})
