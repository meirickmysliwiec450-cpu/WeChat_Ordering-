import request from './index'

export function getPayments(params) {
  return request({ url: '/admin/payments', method: 'get', params })
}

export function getPaymentById(id) {
  return request({ url: `/admin/payments/${id}`, method: 'get' })
}
