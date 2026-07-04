const { addToCart, getDiningType } = require('../../utils/store')
const { requireLogin, getToken, buildUrl } = require('../../utils/auth')

Page({
  data: {
    keyword: '',
    keywords: [],
    results: [],
    allDishes: []
  },

  onShow() {
    requireLogin()
    this.loadDishes()
  },

  loadDishes() {
    wx.request({
      url: buildUrl('/wx/dishes'),
      method: 'GET',
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200) {
          const dishes = res.data.data || []
          // 按销量排序取前6个作为热门搜索关键词
          const sorted = [...dishes].sort((a, b) => (b.sales || 0) - (a.sales || 0))
          const hotKeywords = sorted.slice(0, 6).map(d => d.dishName)
          this.setData({
            allDishes: dishes,
            results: dishes,
            keywords: hotKeywords.length > 0 ? hotKeywords : ['暂无菜品']
          })
        }
      }
    })
  },

  onInput(event) {
    this.setData({ keyword: event.detail.value }, () => this.doSearch())
  },

  selectKeyword(event) {
    const word = event.currentTarget.dataset.keyword
    this.setData({ keyword: word }, () => this.doSearch())
  },

  doSearch() {
    const keyword = this.data.keyword.trim().toLowerCase()
    const results = keyword
      ? this.data.allDishes.filter(d => {
          const name = (d.dishName || '').toLowerCase()
          const desc = (d.description || '').toLowerCase()
          const cat = (d.categoryName || '').toLowerCase()
          return name.includes(keyword) || desc.includes(keyword) || cat.includes(keyword)
        })
      : this.data.allDishes
    this.setData({ results })
  },

  addCart(e) {
    if (!getDiningType()) {
      wx.showToast({ title: '请先选择堂食或外送', icon: 'none' })
      setTimeout(() => wx.switchTab({ url: '/pages/home/home' }), 500)
      return
    }
    const dish = e.detail
    addToCart({ id: dish.id, name: dish.dishName, price: dish.price, image: dish.image })
    wx.showToast({ title: '已加入购物车', icon: 'success' })
  }
})
