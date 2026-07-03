const { dishes } = require('../../utils/mock')
const { addToCart, getDiningType } = require('../../utils/store')
const { requireLogin } = require('../../utils/auth')

Page({
  data: {
    keyword: '',
    keywords: ['牛肉', '鸡腿', '小面', '饮品'],
    results: dishes
  },

  onShow() {
    requireLogin()
  },

  onInput(event) {
    this.setData({ keyword: event.detail.value }, () => this.doSearch())
  },

  selectKeyword(event) {
    this.setData({ keyword: event.currentTarget.dataset.keyword }, () => this.doSearch())
  },

  doSearch() {
    const keyword = this.data.keyword.trim()
    const results = keyword
      ? dishes.filter(item => `${item.name}${item.desc}${item.label}`.indexOf(keyword) > -1)
      : dishes
    this.setData({ results })
  },

  addCart(event) {
    if (!getDiningType()) {
      wx.showToast({ title: '请先选择堂食或外送', icon: 'none' })
      setTimeout(() => wx.switchTab({ url: '/pages/home/home' }), 500)
      return
    }
    addToCart(event.detail.id)
    wx.showToast({ title: '已加入购物栏', icon: 'success' })
  }
})
