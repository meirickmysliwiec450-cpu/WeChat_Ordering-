
const { getToken, requireLogin } = require('../../utils/auth')

Page({
  data: {
    orderId: '',
    order: null,
    loading: true,
    comment:false
  },

  onLoad(options) {
    this.setData({ orderId: options.id || '' })
    this.loadOrderDetail(options.id || '')
  },

  loadOrderDetail(orderId) {
    if (!requireLogin()) return

    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    if (!baseUrl) {
      this.useLocalOrder(orderId)
      return
    }

    wx.request({
      url: `${baseUrl}/wx/orders/${orderId}`,
      method: 'GET',
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: res => {
        const ok = res.statusCode >= 200 && res.statusCode < 300
        if (!ok || res.data.code !== 200) {
          this.useLocalOrder(orderId)
          return
        }

        console.log('完整返回', res.data);
        // res.data.data = { details:[], order:{} }
        const rootData = res.data.data
        const parseResult = this.normalizeOrder(rootData)
        this.setData({ order: parseResult, loading: false }, () => {
          // 订单加载完成，立刻查询评价接口
          this.loadComment(orderId)
        })
        console.log('解析后订单', parseResult)
      },
      fail: () => {
        this.useLocalOrder(orderId)
      }
    })
  },

  // rootData = { details:[], order:{} }
  normalizeOrder(rootData) {
    if (!rootData || typeof rootData !== 'object') return null
    const item = rootData.order // 主订单对象
    const rawItems = rootData.details // 菜品明细数组

    if (!item) return null

    // 组装菜品列表
    const items = Array.isArray(rawItems) ? rawItems.map(dish => ({
      id: String(dish.id),
      name: dish.dishName,
      price: Number(dish.price) || 0,
      count: Number(dish.quantity || 1)
    })) : []

    // 数字状态映射
    let statusRaw = item.orderStatus
    let status
    const numStatusMap = {
      3: 'pending',    // 待支付
      1: 'paid',       // 已支付
      2: 'completed',  // 已完成
      0: 'cancelled'  // 已取消
    }
    status = numStatusMap[statusRaw] || 'pending'

    // 计算总价总件数
    let totalCount = 0
    let totalPrice = 0
    items.forEach(d => {
      totalCount += d.count
      totalPrice += d.price * d.count
    })

    return {
      id: String(item.id),
      orderNo: item.orderNo,
      createTime: item.createTime,
      status,
      statusText: this.getStatusText(status),
      diningType: item.diningType || '',
      diningTypeText: item.diningType === 'takeout' ? '外送' : '堂食',
      items,
      totalCount,
      totalPrice,
      address: item.address || '',
      tableInfo: item.tableInfo || '',
      remark: item.remark || ''
    }
  },

  getStatusText(status) {
    const statusMap = {
      pending: '待支付',
      paid: '已支付',
      completed: '已完成',
      cancelled: '已取消'
    }
    return statusMap[status] || status || '未知状态'
  },

  useLocalOrder(orderId) {
    const orders = wx.getStorageSync('orders') || []
    const order = orders.find(item => String(item.id) === String(orderId)) || null
    this.setData({ order, loading: false })
  },

  loadComment(orderId) {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    if (!baseUrl) {
      this.loadCommentLocal(orderId)
      return
    }

    wx.request({
      url: `${baseUrl}/wx/comments/order/${orderId}`,
      method: 'GET',
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: res => {
        const ok = res.statusCode >= 200 && res.statusCode < 300
        if (!ok) {
          this.loadCommentLocal(orderId)
          return
        }
        console.log(res.data.data.length)
        if(res.data.data.length!==0){
          console.log(res.data.data.length)
          this.setData({ comment:true })
        }
      },
      fail: () => {
        this.loadCommentLocal(orderId)
      }
    })
  },


  loadCommentLocal(orderId) {
    const feedbacks = wx.getStorageSync('feedbacks') || []
    const fb = feedbacks.find(item => String(item.orderId) === String(orderId))
    this.setData({
      comment: fb ? {
        id: fb.id,
        content: fb.content,
        photoUrl: fb.photo,
        createTime: fb.time
      } : null
    })
  },

  goCommentDetail() {
    if (!this.data.order) return
    wx.navigateTo({ url: `/pages/comment_detail/comment_detail?orderId=${this.data.order.id}` })
  },
  goComment() {
    if (!this.data.order) return
    wx.navigateTo({ url: `/pages/order_comment/order_comment?orderId=${this.data.order.id}` })
  }
})