import request from './index'

export function getFeedbacks(params) {
  return request({ url: '/admin/feedbacks', method: 'get', params })
}

export function getFeedbackById(id) {
  return request({ url: `/admin/feedbacks/${id}`, method: 'get' })
}

export function replyFeedback(id, replyContent) {
  return request({ url: `/admin/feedbacks/${id}/reply`, method: 'post', data: { replyContent } })
}

export function updateFeedbackStatus(id, status) {
  return request({ url: `/admin/feedbacks/${id}/status`, method: 'put', data: { status } })
}
