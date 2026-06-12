import request from './index'

export function login(username, password) {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data: { username, password }
  })
}

export function getInfo(adminId) {
  return request({
    url: '/admin/auth/info',
    method: 'get',
    params: { adminId }
  })
}
