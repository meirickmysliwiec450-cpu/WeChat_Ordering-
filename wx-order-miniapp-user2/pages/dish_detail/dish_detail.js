const { dishes: mockDishes } = require('../../utils/mock')
const { addToCart, getDiningType } = require('../../utils/store')
const { getToken, requireLogin } = require('../../utils/auth')

Page({
  data: {
    dishId: '',
    dish: null,
    loading: true
  },

  onLoad(options) {
    const dishId = options.id || ''
    this.setData({ dishId })
    this.loadDishDetail(dishId)
  },

  loadDishDetail(dishId) {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')

    if (!baseUrl) {
      this.useFallbackDetail(dishId)
      return
    }

    // 调用单品详情接口 /wx/dishes/{id}，不是列表接口
    wx.request({
      url: `${baseUrl}/wx/dishes/${dishId}`,
      method: 'GET',
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: res => {
        if (res.statusCode < 200 || res.statusCode >= 300 || !res.data || res.data.code !== 200) {
          this.useFallbackDetail(dishId)
          return
        }
        const raw = res.data.data
        if (!raw) {
          this.useFallbackDetail(dishId)
          return
        }
        const detail = {
          id: String(raw.id || ''),
          categoryId: String(raw.categoryId || ''),
          name: raw.dishName || '未命名菜品',
          desc: raw.description || '',
          price: Number(raw.price) || 0,
          sales: Number(raw.sales) || 0,
          stock: Number(raw.stock) || 0,
          imageUrl: raw.image || ''
        }
        this.setData({ dish: detail, loading: false })
      },
      fail: () => {
        this.useFallbackDetail(dishId)
      }
    })
  },

  normalizeDetail(responseData) {
    // 先尝试从数组中取第一条，再尝试直接取对象
    const raw = Array.isArray(responseData)
      ? responseData[0]
      : responseData && responseData.data
        ? Array.isArray(responseData.data) ? responseData.data[0] : responseData.data
        : responseData

    if (!raw || typeof raw !== 'object') return null

    return {
      id: String(raw.id || raw.dishId || ''),
      categoryId: String(raw.categoryId || ''),
      name: raw.name || raw.dishName || '未命名菜品',
      desc: raw.desc || raw.description || raw.remark || '',
      price: Number(raw.price) || 0,
      sales: Number(raw.sales) || Number(raw.saleCount) || 0,
      stock: Number(raw.stock) || 0,
      imageColor: raw.imageColor || raw.color || '#c01818',
      label: raw.label || raw.tag || '',
      imageUrl: raw.imageUrl || raw.imgUrl || raw.picUrl || raw.image || ''
    }
  },

  useFallbackDetail(dishId) {
    const dish = mockDishes.find(item => item.id === dishId)
    this.setData({
      dish: dish || null,
      loading: false
    })
  },

  addCart() {
    if (!getDiningType()) {
      wx.showToast({ title: '请先选择堂食或外送', icon: 'none' })
      return
    }
    const dish = this.data.dish
    if (!dish) return
    addToCart({ id: dish.id, name: dish.name, price: dish.price, image: dish.imageUrl })
    wx.showToast({ title: '已加入购物栏', icon: 'success' })
  },

  onImgError() {
    this.setData({ 'dish.imageUrl': '' })
  },

  goBack() {
    wx.navigateBack()
  }
})
