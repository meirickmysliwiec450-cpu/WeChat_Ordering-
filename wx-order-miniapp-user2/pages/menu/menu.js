const { categories: mockCategories, dishes: mockDishes } = require('../../utils/mock')
const { addToCart, updateCartItem, getCart, getCartSummary, clearCart, getDiningType, getDiningTypeText } = require('../../utils/store')
const { getToken, requireLogin } = require('../../utils/auth')

const DEFAULT_BANNER = {
  id: 'default-banner',
  title: '精选菜品推荐',
  description: '新鲜现做，欢迎点餐',
  imageUrl: '/images/default_banner.png'
}

Page({
  data: {
    categories: [],
    activeCategory: '',
    currentDishes: [],
    banners: [DEFAULT_BANNER],
    cart: [],
    summary: { totalCount: 0, totalPrice: 0 },
    diningType: '',
    diningTypeText: '未选择'
  },

  onLoad() {
    this.loadCategories()
    this.loadBanners()
  },

  onShow() {
    if (!requireLogin()) return
    this.refreshDiningType()
    this.refreshCart()
  },

  refreshDiningType() {
    const diningType = getDiningType()
    if (!diningType) {
      wx.showToast({ title: '请先选择堂食或外送', icon: 'none' })
      setTimeout(() => wx.switchTab({ url: '/pages/home/home' }), 500)
      return
    }
    this.setData({
      diningType,
      diningTypeText: getDiningTypeText(diningType)
    })
  },

  // ========== 获取分类 ==========

  loadCategories() {
    // 强制使用正确的地址，避免缓存问题
    const baseUrl = (getApp().globalData.baseUrl || 'http://localhost:8080/api').replace(/\/$/, '')
    
    wx.request({
      url: `${baseUrl}/wx/categories`,
      method: 'GET',
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: res => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          this.useFallbackCategories()
          return
        }
        const categories = this.normalizeCategories(res.data)
        if (!categories.length) {
          this.useFallbackCategories()
          return
        }
        this.setData({
          categories,
          activeCategory: categories[0].id
        }, () => this.loadDishes())
      },
      fail: () => {
        this.useFallbackCategories()
      }
    })
  },

  normalizeCategories(responseData) {
    const list = Array.isArray(responseData)
      ? responseData
      : Array.isArray(responseData && responseData.data)
        ? responseData.data
        : Array.isArray(responseData && responseData.data && responseData.data.records)
          ? responseData.data.records
          : Array.isArray(responseData && responseData.rows)
            ? responseData.rows
            : []

    return list.map((item, index) => ({
      id: String(item.id || item.categoryId || item.catId || `cat-${index}`),
      name: item.name || item.categoryName || item.title || `分类${index + 1}`
    }))
  },

  useFallbackCategories() {
    this.setData({
      categories: mockCategories,
      activeCategory: mockCategories[0].id
    }, () => this.loadDishes())
  },

  // ========== 获取菜品 ==========

  selectCategory(event) {
    this.setData({
      activeCategory: event.currentTarget.dataset.id
    }, () => this.loadDishes())
  },

  loadDishes() {
    const categoryId = this.data.activeCategory
    if (!categoryId) return

    // 强制使用正确的地址，避免缓存问题
    const baseUrl = (getApp().globalData.baseUrl || 'http://localhost:8080/api').replace(/\/$/, '')
    
    wx.showLoading({ title: '加载中' })

    wx.request({
      url: `${baseUrl}/wx/dishes`,
      method: 'GET',
      data: { categoryId },
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: res => {
        wx.hideLoading()
        if (res.statusCode < 200 || res.statusCode >= 300) {
          this.useFallbackDishes()
          return
        }
        const dishes = this.normalizeDishes(res.data)
        if (!dishes.length) {
          this.useFallbackDishes()
          return
        }
        this.setData({ currentDishes: dishes })
      },
      fail: () => {
        wx.hideLoading()
        this.useFallbackDishes()
      }
    })
  },

  normalizeDishes(responseData) {
    const list = Array.isArray(responseData)
      ? responseData
      : Array.isArray(responseData && responseData.data)
        ? responseData.data
        : Array.isArray(responseData && responseData.data && responseData.data.records)
          ? responseData.data.records
          : Array.isArray(responseData && responseData.rows)
            ? responseData.rows
            : []

    return list.map((item, index) => ({
      id: String(item.id || item.dishId || `dish-${index}`),
      categoryId: String(item.categoryId || item.categoryId || this.data.activeCategory),
      name: item.name || item.dishName || '未命名菜品',
      desc: item.desc || item.description || item.remark || '',
      price: Number(item.price) || 0,
      sales: Number(item.sales) || Number(item.saleCount) || 0,
      stock: Number(item.stock) || 0,
      imageColor: item.imageColor || item.color || '#c01818',
      label: item.label || item.tag || '',
      imageUrl: item.imageUrl || item.imgUrl || item.picUrl || item.image || ''
    }))
  },

  useFallbackDishes() {
    this.setData({
      currentDishes: mockDishes.filter(item => item.categoryId === this.data.activeCategory)
    })
  },

  // ========== 轮播图 ==========

  loadBanners() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    if (!baseUrl) {
      this.useDefaultBanner()
      return
    }

    wx.request({
      url: `${baseUrl}/wx/banners`,
      method: 'GET',
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: res => {
        if (res.statusCode < 200 || res.statusCode >= 300) {
          this.useDefaultBanner()
          return
        }
        const banners = this.normalizeBanners(res.data)
        if (!banners.length) {
          this.useDefaultBanner()
          return
        }
        this.setData({ banners })
      },
      fail: () => {
        this.useDefaultBanner()
      }
    })
  },

  normalizeBanners(responseData) {
    const list = Array.isArray(responseData)
      ? responseData
      : Array.isArray(responseData && responseData.data)
        ? responseData.data
        : Array.isArray(responseData && responseData.data && responseData.data.records)
          ? responseData.data.records
          : Array.isArray(responseData && responseData.rows)
            ? responseData.rows
            : []

    return list
      .map((item, index) => {
        const imageUrl = item.imageUrl || item.imgUrl || item.picUrl || item.url || item.image || ''
        if (!imageUrl) return null
        return {
          id: item.id || item.bannerId || `banner-${index}`,
          title: item.title || item.name || '精选推荐',
          description: item.description || item.remark || item.subTitle || '',
          imageUrl
        }
      })
      .filter(Boolean)
  },

  useDefaultBanner() {
    this.setData({ banners: [DEFAULT_BANNER] })
  },

  onBannerError(event) {
    const index = event.currentTarget.dataset.index
    this.setData({
      [`banners[${index}].imageUrl`]: DEFAULT_BANNER.imageUrl,
      [`banners[${index}].title`]: this.data.banners[index].title || DEFAULT_BANNER.title,
      [`banners[${index}].description`]: this.data.banners[index].description || DEFAULT_BANNER.description
    })
  },

  // ========== 购物栏 ==========

  refreshCart() {
    const cart = getCart()
    this.setData({
      cart,
      summary: getCartSummary(cart)
    })
  },

  changeCount(event) {
    updateCartItem(event.currentTarget.dataset.id, Number(event.currentTarget.dataset.count))
    this.refreshCart()
  },

  clearCart() {
    wx.showModal({
      title: '清空购物栏',
      content: '确定要清空已选菜品吗？',
      success: res => {
        if (!res.confirm) return
        clearCart()
        this.refreshCart()
      }
    })
  },

  // ========== 跳转 ==========

  goSearch() {
    wx.navigateTo({ url: '/pages/search/search' })
  },

  goCheckout() {
    if (!this.data.cart.length) {
      wx.showToast({ title: '请先添加菜品', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/checkout/checkout' })
  },

  // ========== 菜品操作 ==========

  addCart(event) {
    console.log('页面接收加购事件event', event)
    const dishId = event.detail.id
    const fullDish = event.detail.dish // 拿到完整菜品对象
    console.log('拿到菜品id', dishId, '菜品完整数据：', fullDish)
    
    // 直接传完整菜品，不再只传id
    addToCart(fullDish)
  
    const cartList = getCart()
    console.log('本地storage读取购物车', cartList)
    this.refreshCart()
    console.log('更新后页面data.cart', this.data.cart)
    wx.showToast({ title: '已加入购物栏', icon: 'success' })
  },

  viewDishDetail(event) {
    const dishId = event.detail.id
    wx.navigateTo({ url: `/pages/dish_detail/dish_detail?id=${dishId}` })
  }
})
