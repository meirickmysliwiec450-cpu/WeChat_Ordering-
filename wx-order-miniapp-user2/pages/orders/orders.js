const { getToken, requireLogin } = require('../../utils/auth')

const TAB_MAP = { pending: 3, paid: 1, completed: 2, cancelled: 0 }
const STATUS_MAP = { 3: 'pending', 1: 'paid', 2: 'completed', 0: 'cancelled' }
const STATUS_TEXT = { 3: '待支付', 1: '已支付', 2: '已完成', 0: '已取消' }

Page({
  data: {
    tabs: [
      { key: 'all', name: '全部' },
      { key: 'pending', name: '待支付' },
      { key: 'paid', name: '已支付' },
      { key: 'completed', name: '已完成' },
      { key: 'cancelled', name: '已取消' }
    ],
    activeTab: 'all',
    orders: [],
    loading: false
  },

  onShow() {
    if (!requireLogin()) return
    this.refreshOrders()
  },

  changeTab(e) {
    const tab = e.currentTarget.dataset.key
    this.setData({ activeTab: tab }, () => this.refreshOrders())
  },

  refreshOrders() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    if (!baseUrl) { this.useLocal(); return }

    this.setData({ loading: true })
    const apiStatus = TAB_MAP[this.data.activeTab]
    const reqData = apiStatus != null ? { status: apiStatus } : {}

    wx.request({
      url: `${baseUrl}/wx/orders`,
      method: 'GET',
      data: reqData,
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.statusCode >= 200 && res.statusCode < 300 && res.data?.code === 200) {
          const apiList = (res.data.data?.list || []).map(o => this.formatOrder(o))
          const localList = this.getLocalOrders()
          let merged = this.mergeLists(apiList, localList)
          // 前端二次过滤确保准确
          if (this.data.activeTab !== 'all') {
            merged = merged.filter(o => o.status === this.data.activeTab)
          }
          this.setData({ orders: merged, loading: false })
        } else {
          this.useLocal()
        }
      },
      fail: () => this.useLocal()
    })
  },

  formatOrder(o) {
    const items = (o.items || []).map(d => ({
      id: String(d.id || d.dishId || ''),
      name: d.name || d.dishName || '菜品',
      price: Number(d.price) || 0,
      count: Number(d.count || d.quantity || 1)
    }))
    const os = o.orderStatus
    return {
      id: String(o.id || ''),
      orderNo: o.orderNo || '',
      createTime: o.createTime || '',
      orderStatus: os,
      status: STATUS_MAP[os] || 'pending',
      statusText: STATUS_TEXT[os] || '未知',
      diningType: o.receiver ? 'takeout' : 'dineIn',
      diningTypeText: o.receiver ? '外送' : '堂食',
      items,
      totalCount: items.reduce((s, d) => s + d.count, 0),
      totalPrice: o.payAmount || o.totalAmount || 0,
      address: o.receiver || '',
      remark: o.remark || ''
    }
  },

  getLocalOrders() {
    return (wx.getStorageSync('orders') || []).map(o => ({
      ...o,
      orderStatus: o.orderStatus != null ? o.orderStatus : (TAB_MAP[o.status] || 3),
      status: o.status || 'pending',
      statusText: o.statusText || '未知',
      totalCount: o.totalCount || (o.items || []).reduce((s, d) => s + (d.count || 1), 0)
    }))
  },

  mergeLists(api, local) {
    const ids = new Set(api.map(o => o.id))
    const result = [...api]
    for (const o of local) {
      if (!ids.has(o.id)) result.push(o)
    }
    result.sort((a, b) => (b.createTime || '').localeCompare(a.createTime || ''))
    return result
  },

  useLocal() {
    const all = this.getLocalOrders()
    const filtered = this.data.activeTab === 'all'
      ? all
      : all.filter(o => o.status === this.data.activeTab)
    this.setData({ orders: filtered, loading: false })
  },

  goReview(e) {
    const { id, orderno } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/order_review/order_review?orderId=${id}&orderNo=${orderno || ''}` })
  },

  goDetail(e) {
    wx.navigateTo({ url: `/pages/order_detail/order_detail?id=${e.currentTarget.dataset.id}` })
  },

  payOrder(e) {
    wx.showToast({ title: '请稍后再试', icon: 'none' })
  },

  cancelOrder(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '取消订单',
      content: '确定要取消该订单吗？',
      success: res => {
        if (!res.confirm) return
        const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
        wx.request({
          url: `${baseUrl}/wx/orders/${id}/cancel`,
          method: 'PUT',
          header: { Authorization: `Bearer ${getToken()}` },
          success: () => { wx.showToast({ title: '已取消', icon: 'success' }); this.refreshOrders() },
          fail: () => wx.showToast({ title: '取消失败', icon: 'none' })
        })
      }
    })
  },

  finishOrder(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认收餐',
      content: '确认已收到餐品？',
      success: res => {
        if (!res.confirm) return
        const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
        wx.request({
          url: `${baseUrl}/wx/orders/${id}`,
          method: 'PUT',
          header: { 'content-type': 'application/json', Authorization: `Bearer ${getToken()}` },
          data: { orderStatus: 2 },
          success: () => { wx.showToast({ title: '已确认', icon: 'success' }); this.refreshOrders() },
          fail: () => wx.showToast({ title: '操作失败', icon: 'none' })
        })
      }
    })
  }
})
