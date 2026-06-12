import request from './index'

export function getStats() {
  return request({ url: '/admin/dashboard/stats', method: 'get' })
}
