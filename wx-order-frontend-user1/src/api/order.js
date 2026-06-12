import request from './index'

export function getOrders(params) {
  return request({ url: '/admin/orders', method: 'get', params })
}

export function getOrderDetail(id) {
  return request({ url: `/admin/orders/${id}`, method: 'get' })
}

export function updateOrderStatus(id, orderStatus) {
  return request({ url: `/admin/orders/${id}/status`, method: 'put', data: { orderStatus } })
}
