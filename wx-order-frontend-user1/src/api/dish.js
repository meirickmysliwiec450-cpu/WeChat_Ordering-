import request from './index'

export function getDishes(params) {
  return request({ url: '/admin/dishes', method: 'get', params })
}

export function getDishById(id) {
  return request({ url: `/admin/dishes/${id}`, method: 'get' })
}

export function addDish(data) {
  return request({ url: '/admin/dishes', method: 'post', data })
}

export function updateDish(id, data) {
  return request({ url: `/admin/dishes/${id}`, method: 'put', data })
}

export function updateDishStatus(id, status) {
  return request({ url: `/admin/dishes/${id}/status`, method: 'put', data: { status } })
}

export function deleteDish(id) {
  return request({ url: `/admin/dishes/${id}`, method: 'delete' })
}
