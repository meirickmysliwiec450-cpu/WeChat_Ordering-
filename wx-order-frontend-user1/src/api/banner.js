import request from './index'

export function getBanners() {
  return request({ url: '/admin/banners', method: 'get' })
}

export function addBanner(data) {
  return request({ url: '/admin/banners', method: 'post', data })
}

export function updateBanner(id, data) {
  return request({ url: `/admin/banners/${id}`, method: 'put', data })
}

export function updateBannerStatus(id, status) {
  return request({ url: `/admin/banners/${id}/status`, method: 'put', data: { status } })
}

export function deleteBanner(id) {
  return request({ url: `/admin/banners/${id}`, method: 'delete' })
}
