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
  const baseUrl = (getApp().globalData.baseUrl || 'http://172.20.10.10:8080/api').replace(/\/$/, '')
  return `${baseUrl}${path}`
}

/** 小程序图片显示：加 ?base64=true 绕过微信HTTP限制 */
function imageUrl(url) {
  if (!url) return ''
  // 相对路径 → 完整URL
  if (!url.startsWith('http')) {
    const base = (getApp().globalData.baseUrl || 'http://172.20.10.10:8080/api').replace(/\/$/, '')
    url = base + (url.startsWith('/') ? '' : '/') + url
  }
  // 已是 base64 不重复加
  if (url.includes('?base64')) return url
  return url + '?base64=true'
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
    // ========== 配置：可切换正式/测试模式 ==========
    const USE_TEST_MODE = true; // true=测试模式(直接传openId)，false=正式微信登录
    
    if (USE_TEST_MODE) {
      // 测试模式：直接传openId，不用调用微信接口
      console.log("========== 开始测试模式登录 ==========")
      const testOpenId = 'test-openid-' + Date.now()
      const testNickName = '答辩演示用户'
      
      console.log("测试openId：", testOpenId)
      
      wx.request({
        url: buildUrl('/wx/user/login'),
        method: 'POST',
        header: {
          'content-type': 'application/json'
        },
        data: {
          openId: testOpenId,
          nickName: testNickName,
          avatar: ''
        },
        success(res) {
          console.log("========== 登录接口响应 ==========")
          console.log("完整响应：", res)
          console.log("响应数据：", res.data)
          
          if (res.data.code !== 200) {
            reject(new Error(res.data.message || "登录失败"))
            return
          }
          
          const result = normalizeLoginResult(res.data)
          console.log("解析结果：", result)
          
          wx.setStorageSync('token', result.token)
          wx.setStorageSync('userInfo', result.userInfo || {})
          
          console.log("已保存token到Storage：", result.token)
          console.log("已保存userInfo到Storage：", result.userInfo)
          
          resolve(result)
        },
        fail(error) {
          console.log("========== 登录接口失败 ==========")
          console.log("错误：", error)
          reject(error)
        }
      })
    } else {
      // 正式微信登录
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
              code: loginRes.code
            },
            success(res) {
              console.log("登录接口完整返回：", res)
              if (res.data.code !== 200) {
                reject(new Error(res.data.message || "登录失败"))
                return
              }
              const result = normalizeLoginResult(res.data)
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
    }
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
  logout,
  buildUrl,
  imageUrl
}
