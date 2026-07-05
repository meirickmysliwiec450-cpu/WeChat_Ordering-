const { categories: mockCats, dishes: mockDishes } = require('../../utils/mock')
const { addToCart, updateCartItem, getCart, getCartSummary, clearCart, getDiningType, getDiningTypeText } = require('../../utils/store')
const { getToken, requireLogin } = require('../../utils/auth')

Page({
  data: {
    categories: [],
    activeCategory: '',
    activeCateName: '',
    currentDishes: [],
    cart: [],
    summary: { totalCount: 0, totalPrice: 0 },
    diningType: '', diningTypeText: '',
    showCart: false
  },

  onLoad() { this.loadData() },

  onShow() {
    if (!requireLogin()) return
    this.refreshCart()
    this.setData({ diningType: getDiningType(), diningTypeText: getDiningTypeText() })
  },

  loadData() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    // 加载分类
    wx.request({
      url: `${baseUrl}/wx/categories`,
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200 && res.data.data?.length) {
          const cats = res.data.data
          this.setData({ categories: cats, activeCategory: cats[0].id, activeCateName: cats[0].categoryName || cats[0].name })
          this.loadDishes(cats[0].id)
        } else {
          this.setData({ categories: mockCats, activeCategory: mockCats[0]?.id })
          this.loadDishes(mockCats[0]?.id)
        }
      },
      fail: () => { this.setData({ categories: mockCats, activeCategory: mockCats[0]?.id }); this.loadDishes(mockCats[0]?.id) }
    })
  },

  loadDishes(cateId) {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.request({
      url: `${baseUrl}/wx/dishes`,
      data: { categoryId: cateId },
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200) {
          this.setData({ currentDishes: res.data.data || [] })
        }
      }
    })
  },

  selectCategory(e) {
    const id = e.currentTarget.dataset.id
    const cat = this.data.categories.find(c => c.id == id)
    this.setData({ activeCategory: id, activeCateName: cat?.categoryName || cat?.name || '' })
    this.loadDishes(id)
  },

  addCart(e) {
    const item = e.currentTarget.dataset.item
    if (!getDiningType()) { wx.showToast({ title: '请先在首页选用餐方式', icon: 'none' }); return }
    addToCart(item)
    this.refreshCart()
    wx.showToast({ title: '已加入', icon: 'success' })
  },

  changeCount(e) {
    const { id, count } = e.currentTarget.dataset
    if (count <= 0) {
      updateCartItem(id, 0)
    } else {
      updateCartItem(id, count)
    }
    this.refreshCart()
  },

  refreshCart() {
    const cart = getCart()
    this.setData({ cart, summary: getCartSummary(cart) })
  },

  clearCart() {
    clearCart()
    this.refreshCart()
    this.setData({ showCart: false })
  },

  toggleCart() { this.setData({ showCart: !this.data.showCart }) },

  goSearch() { wx.navigateTo({ url: '/pages/search/search' }) },

  goCheckout() {
    if (!this.data.cart.length) return
    wx.navigateTo({ url: '/pages/checkout/checkout' })
  },

  viewDishDetail(e) {
    wx.navigateTo({ url: `/pages/dish_detail/dish_detail?id=${e.currentTarget.dataset.id}` })
  }
})
