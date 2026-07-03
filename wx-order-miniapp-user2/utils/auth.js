const LOGIN_PAGE = '/pages/login/login'

function getToken() {
  return wx.getStorageSync('token') || ''
}

function isLoggedIn() {
  return !!getToken()
}

function getCurrentRoute() {
  const pages = getCurrentPages()
  if (!pages.length) return ''
  return '/' + pages[pages.length - 1].route
}

function requireLogin() {
  if (isLoggedIn()) return true

  const currentRoute = getCurrentRoute()
  if (currentRoute !== LOGIN_PAGE) {
    wx.navigateTo({ url: LOGIN_PAGE })
  }
  return false
}

function buildUrl(path) {
  const baseUrl = (getApp().globalData.baseUrl || '').replace(/\/$/, '')
  return `${baseUrl}${path}`
}

function normalizeLoginResult(responseData) {
  const data = responseData || {}
  const payload = data.data || data.result || data
  return {
    token: payload.token || payload.accessToken || payload.jwt || '',
    userInfo: payload.userInfo || payload.user || payload
  }
}

function loginByCode() {
  return new Promise((resolve, reject) => {
    wx.login({
      success(loginRes) {
        console.log("wx.login获取的code：", loginRes.code)
        if (!loginRes.code) {
          reject(new Error('获取微信登录 code 失败'))
          return
        }

        wx.request({
          url: buildUrl('/wx/user/login'),
          method: 'POST',
          header: {
            'content-type': 'application/json'
          },
          data: {
            code: loginRes.code // 仅传code，后端用code+appid+secret调微信api拿openid
          },
          success(res) {
            console.log("登录接口完整返回：", res)
            // 后端业务异常（500、提示openId为空）
            if (res.data.code !== 200) {
              reject(new Error(res.data.message || "登录失败"))
              return
            }

            const result = normalizeLoginResult(res.data)
            if (!result.token) {
              reject(new Error('登录接口未返回 token'))
              return
            }

            wx.setStorageSync('token', result.token)
            wx.setStorageSync('userInfo', result.userInfo || {})
            resolve(result)
          },
          fail(error) {
            reject(error)
          }
        })
      },
      fail(error) {
        reject(error)
      }
    })
  })
}

function logout() {
  wx.removeStorageSync('token')
  wx.removeStorageSync('userInfo')
  wx.navigateTo({ url: LOGIN_PAGE })
}

module.exports = {
  getToken,
  isLoggedIn,
  requireLogin,
  loginByCode,
  logout
}
