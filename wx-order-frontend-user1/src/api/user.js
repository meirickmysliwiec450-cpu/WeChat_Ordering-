import request from './index'

export function getUsers(params) {
  return request({ url: '/admin/users', method: 'get', params })
}

export function getUserById(id) {
  return request({ url: `/admin/users/${id}`, method: 'get' })
}

export function updateUser(id, data) {
  return request({ url: `/admin/users/${id}`, method: 'put', data })
}

export function updateUserStatus(id, status) {
  return request({ url: `/admin/users/${id}/status`, method: 'put', data: { status } })
}
