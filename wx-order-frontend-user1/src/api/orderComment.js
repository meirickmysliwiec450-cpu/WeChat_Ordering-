import request from './index'

export function getComments(params) {
  return request({ url: '/admin/comments', method: 'get', params })
}

export function getCommentById(id) {
  return request({ url: `/admin/comments/${id}`, method: 'get' })
}

export function deleteComment(id) {
  return request({ url: `/admin/comments/${id}`, method: 'delete' })
}
