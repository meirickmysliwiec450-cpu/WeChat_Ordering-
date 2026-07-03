const { updateOrder, removeOrder } = require('../../utils/store')
const { getToken, requireLogin } = require('../../utils/auth')

const tabToApiNum = {
  pending: 3,
  paid: 1,
  completed: 2,
  cancelled: 0
}

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

  changeTab(event) {
    this.setData({ activeTab: event.currentTarget.dataset.key }, () => this.refreshOrders())
  },

  refreshOrders() {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    if (!baseUrl) {
      this.useLocalOrders()
      return
    }

    this.setData({ loading: true })
    wx.request({
      url: `${baseUrl}/wx/orders`,
      method: 'GET',
      // 核心修复：把字符串tab转数字传给后端
      data: this.data.activeTab === 'all' 
        ? {} 
        : { status: tabToApiNum[this.data.activeTab] },
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: res => {
        const ok = res.statusCode >= 200 && res.statusCode < 300
        if (!ok) {
          this.useLocalOrders()
          return
        }

        // 打印日志方便调试，可后续删除
        console.log('接口原始返回res.data', res.data)
        let orders = this.normalizeOrders(res.data)
        console.log('解析完成订单列表', orders)
        this.setData({ loading: false, orders: orders }, () => {
          // 批量查询每个已完成订单的评价状态
          this.batchCheckComment(orders)
        })
      },
      fail: () => {
        this.setData({ loading: false })
        this.useLocalOrders()
      }
    })
  },

  // 修复：正确提取接口里的 list 数组
  normalizeOrders(responseData) {
    let list = []
    // 当前接口标准结构：外层data.data.list
    if (responseData?.data?.list && Array.isArray(responseData.data.list)) {
      list = responseData.data.list
    }
    // 兼容纯数组返回
    else if (Array.isArray(responseData)) {
      list = responseData
    }
    // 兼容 records 分页格式
    else if (responseData?.data?.records && Array.isArray(responseData.data.records)) {
      list = responseData.data.records
    }
    // 兼容 rows 格式
    else if (responseData?.rows && Array.isArray(responseData.rows)) {
      list = responseData.rows
    }
    // 兼容 data 直接是数组的旧接口
    else if (Array.isArray(responseData?.data)) {
      list = responseData.data
    }

    return list.map(item => this.normalizeOrder(item))
  },

  // 批量查询已完成订单是否存在评价
  batchCheckComment(orderList) {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    if (!baseUrl) return
    const token = getToken()

    // 只筛选状态=2（已完成）的订单，其他状态无需查评价
    const completeOrders = orderList.filter(o => o.orderStatus === 2)
    if (completeOrders.length === 0) return

    // 循环请求每个订单评价接口
    completeOrders.forEach(order => {
      wx.request({
        url: `${baseUrl}/wx/comments/order/${order.id}`,
        method: 'GET',
        header: {
          Authorization: token ? `Bearer ${token}` : ''
        },
        success: commentRes => {
          if (commentRes.statusCode === 200 && commentRes.data?.data!="") {
            // 更新对应订单hasComment标记
            const updateOrders = this.data.orders.map(item => {
              if (item.id === order.id) {
                return { ...item, hasComment: true }
              }
              return item
            })
            this.setData({ orders: updateOrders })
          }
          else{
            const updateOrders = this.data.orders.map(item => {
              if (item.id === order.id) {
                return { ...item, hasComment: false }
              }
              return item
            })
            this.setData({ orders: updateOrders })
          }
        }
        // fail/无数据自动保持hasComment=false，无需处理
      })
    })
  },

  // 修复：数字orderStatus转对应字符串状态，匹配你最新状态规则
  normalizeOrder(item) {
    const rawItems = item.items || item.orderItems || item.dishes || []
    const items = Array.isArray(rawItems) ? rawItems.map(dish => ({
      id: String(dish.id || dish.dishId || ''),
      name: dish.name || dish.dishName || '菜品',
      price: Number(dish.price) || 0,
      count: Number(dish.count || dish.quantity || dish.number) || 1
    })) : []

    // 按你要求的数字状态映射
    let statusRaw = item.status || item.orderStatus
    let status
    if (typeof statusRaw === 'number') {
      const numStatusMap = {
        3: 'pending',    // 3 = 待支付
        1: 'paid',        // 1 = 已支付
        2: 'completed',   // 2 = 已完成
        0: 'cancelled'    // 0 = 已取消
      }
      status = numStatusMap[statusRaw] || 'pending'
    } else {
      status = statusRaw || 'pending'
    }

    return {
      id: String(item.id || ''),
      createTime: item.createTime || '',
      orderStatus:item.orderStatus,
      statusText: this.getStatusText(item.orderStatus),
      diningType: item.diningType || item.type || '',
      diningTypeText: item.diningTypeText || (item.diningType === 'takeout' ? '外送' : '堂食'),
      items,
      totalCount: Number(item.totalCount) || items.reduce((sum, dish) => sum + dish.count, 0),
      totalPrice: item.payAmount,
      orderNo: item.orderNo || '',
      address: item.address || '',
      remark: item.remark || '',
      hasComment: false
    }
  },

  getStatusText(status) {
    const statusMap = {
      3: '待支付',
      1: '已支付',
      2: '已完成',
      0: '已取消'
    }
    return statusMap[status] || status || '未知状态'
  },

  useLocalOrders() {
    const allOrders = wx.getStorageSync('orders') || []
    const orders = this.data.activeTab === 'all'
      ? allOrders
      : allOrders.filter(item => item.status === this.data.activeTab)
    this.setData({ orders })
  },

  goDetail(event) {
    wx.navigateTo({ url: `/pages/order_detail/order_detail?id=${event.currentTarget.dataset.id}` })
  },

  payOrder(event) {
    const id = event.currentTarget.dataset.id
    updateOrder(id, { status: 'paid', statusText: '已支付' })
    wx.showToast({ title: '支付成功', icon: 'success' })
    this.refreshOrders()
  },

  cancelOrder(event) {
    const id = event.currentTarget.dataset.id
    wx.showModal({
      title: '取消订单',
      content: '确定要取消该订单吗？',
      success: res => {
        if (!res.confirm) return
        this.cancelOrderByApi(id)
      }
    })
  },

  cancelOrderByApi(id) {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.showLoading({ title: '取消中' })

    if (!baseUrl) {
      updateOrder(id, { status: 'cancelled', statusText: '已取消' })
      wx.hideLoading()
      this.refreshOrders()
      return
    }

    wx.request({
      url: `${baseUrl}/wx/orders/${id}/cancel`,
      method: 'PUT',
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : ''
      },
      success: res => {
        wx.hideLoading()
        const ok = res.statusCode >= 200 && res.statusCode < 300
        if (!ok) {
          wx.showToast({ title: '取消失败', icon: 'none' })
          return
        }
        wx.showToast({ title: '已取消订单', icon: 'success' })
        this.refreshOrders()
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '取消失败，请稍后重试', icon: 'none' })
      }
    })
  },

  finishOrder(event) {
    const id = event.currentTarget.dataset.id
    wx.showModal({
      title: '确认收餐',
      content: '确认已收到餐品？',
      success: res => {
        if (!res.confirm) return
        this.finishOrderByApi(id)
      }
    })
  },

  // 新增：调用PUT /wx/orders/{id} 修改订单状态为2
  finishOrderByApi(id) {
    const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
    wx.showLoading({ title: '处理中' })

    // 无后端环境只更新本地缓存
    if (!baseUrl) {
      updateOrder(id, { status: 'completed', statusText: '已完成' })
      wx.hideLoading()
      wx.showToast({ title: '已确认收餐', icon: 'success' })
      this.refreshOrders()
      return
    }

    wx.request({
      url: `${baseUrl}/wx/orders/${id}`,
      method: 'PUT',
      header: {
        Authorization: getToken() ? `Bearer ${getToken()}` : '',
        'content-type': 'application/json'
      },
      // 仅传递orderStatus=2，后端接收后修改状态
      data: {
        orderStatus: 2
      },
      success: res => {
        wx.hideLoading()
        const ok = res.statusCode >= 200 && res.statusCode < 300
        if (!ok) {
          wx.showToast({ title: '操作失败', icon: 'none' })
          return
        }
        // 同步更新本地缓存
        updateOrder(id, { status: 'completed', statusText: '已完成' })
        wx.showToast({ title: '已确认收餐', icon: 'success' })
        this.refreshOrders()
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '请求失败，请重试', icon: 'none' })
      }
    })
  },

  goComment(event) {
    wx.navigateTo({ url: `/pages/order_comment/order_comment?orderId=${event.currentTarget.dataset.id}` })
  },

  lookComment(event) {
    const orderId = event.currentTarget.dataset.id
    wx.navigateTo({ 
      url: `/pages/comment_detail/comment_detail?orderId=${orderId}` 
    })
  },
  deleteOrder(event) {
    removeOrder(event.currentTarget.dataset.id)
    this.refreshOrders()
  }
})