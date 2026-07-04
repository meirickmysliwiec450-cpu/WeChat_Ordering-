const { getToken, requireLogin } = require('../../utils/auth')

Page({
  data: {
    orderId: '',
    hasReviewed: false,
    reviewScore: 0,
    reviewStars: [],
    reviewContent: '',
    reviewTime: '',
    order: null,
    comment: null,
    loading: true
  },

  onLoad(options) {
    this.setData({ orderId: options.id || '' })
    this.loadOrderDetail(options.id || '')
    this.checkReviewStatus(options.id || '')
    this.loadComment(options.id || '')
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
        this.setData({ order: parseResult, loading: false })
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
      remark: item.remark || '',
      comment: item.comment || null
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
        const comment = this.normalizeComment(res.data)
        this.setData({ comment })
      },
      fail: () => {
        this.loadCommentLocal(orderId)
      }
    })
  },

  normalizeComment(responseData) {
    const raw = responseData && responseData.data ? responseData.data : responseData
    if (!raw || typeof raw !== 'object') return null

    const photoUrl = raw.photoUrl || raw.imageUrl || raw.imgUrl || raw.picUrl || raw.fileUrl || raw.url || raw.photo || ''
    return {
      id: String(raw.id || raw.commentId || ''),
      content: raw.content || raw.text || raw.comment || raw.description || '',
      photoUrl,
      createTime: raw.createTime || raw.createdTime || raw.time || ''
    }
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

  checkReviewStatus(orderId) {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.request({
      url: `${baseUrl}/wx/comments/order/${orderId}`,
      method: 'GET',
      header: { Authorization: `Bearer ${getToken()}` },
      success: res => {
        if (res.data?.code === 200 && res.data.data?.length > 0) {
          const reviewData = res.data.data[0]
          const score = reviewData.score || 0
          this.setData({
            hasReviewed: true,
            reviewScore: score,
            reviewStars: Array.from({ length: score }, (_, i) => i),
            reviewContent: reviewData.content || '',
            reviewTime: reviewData.createTime || ''
          })
        }
      }
    })
  },

  goReview() {
    if (!this.data.order) return
    wx.navigateTo({ url: `/pages/order_review/order_review?orderId=${this.data.order.id}&orderNo=${this.data.order.orderNo || ''}` })
  },

  goComment() {
    if (!this.data.order) return
    wx.navigateTo({ url: `/pages/feedback/feedback?orderId=${this.data.order.id}` })
  },

  payOrder() {
    if (!this.data.order) return
    wx.showModal({
      title: '确认支付',
      content: '确认支付该订单？',
      success: res => {
        if (!res.confirm) return
        const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
        wx.request({
          url: `${baseUrl}/wx/orders/${this.data.order.id}`,
          method: 'PUT',
          header: { 'content-type': 'application/json', Authorization: `Bearer ${getToken()}` },
          data: { orderStatus: 1 },
          success: apiRes => {
            if (apiRes.data?.code === 200) {
              wx.showToast({ title: '支付成功', icon: 'success' })
              this.loadOrderDetail(this.data.orderId)
              this.setData({ hasReviewed: false })
            } else {
              wx.showToast({ title: apiRes.data?.message || '支付失败', icon: 'none' })
            }
          },
          fail: () => wx.showToast({ title: '网络异常', icon: 'none' })
        })
      }
    })
  },

  cancelOrder() {
    if (!this.data.order) return
    wx.showModal({
      title: '取消订单',
      content: '确定取消该订单？',
      success: res => {
        if (!res.confirm) return
        const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
        wx.request({
          url: `${baseUrl}/wx/orders/${this.data.order.id}/cancel`,
          method: 'PUT',
          header: { Authorization: `Bearer ${getToken()}` },
          success: () => {
            wx.showToast({ title: '已取消', icon: 'success' })
            this.loadOrderDetail(this.data.orderId)
          },
          fail: () => wx.showToast({ title: '操作失败', icon: 'none' })
        })
      }
    })
  },

  finishOrder() {
    if (!this.data.order) return
    wx.showModal({
      title: '确认收餐',
      content: '确认已收到餐品？',
      success: res => {
        if (!res.confirm) return
        const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
        wx.request({
          url: `${baseUrl}/wx/orders/${this.data.order.id}`,
          method: 'PUT',
          header: { 'content-type': 'application/json', Authorization: `Bearer ${getToken()}` },
          data: { orderStatus: 2 },
          success: apiRes => {
            if (apiRes.data?.code === 200) {
              wx.showToast({ title: '已确认', icon: 'success' })
              this.loadOrderDetail(this.data.orderId)
              this.checkReviewStatus(this.data.orderId)
            } else {
              wx.showToast({ title: apiRes.data?.message || '操作失败', icon: 'none' })
            }
          },
          fail: () => wx.showToast({ title: '网络异常', icon: 'none' })
        })
      }
    })
  }
})