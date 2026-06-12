import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getInfo } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('adminToken') || '')
  const adminInfo = ref(JSON.parse(localStorage.getItem('adminInfo') || 'null'))

  async function login(username, password) {
    const res = await loginApi(username, password)
    token.value = res.data.token
    adminInfo.value = res.data.adminInfo
    localStorage.setItem('adminToken', res.data.token)
    localStorage.setItem('adminInfo', JSON.stringify(res.data.adminInfo))
    return res
  }

  function logout() {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem('adminToken')
    localStorage.removeItem('adminInfo')
  }

  return { token, adminInfo, login, logout }
})
