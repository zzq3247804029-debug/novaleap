import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

const DEFAULT_AVATAR = '🥳'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('nova_token') || '')
  const user = ref(JSON.parse(localStorage.getItem('nova_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isGuest = computed(() => user.value?.role === 'GUEST')
  const nickname = computed(() => user.value?.nickname || '未登录')
  const avatar = computed(() => user.value?.avatar || DEFAULT_AVATAR)

  function setAuth(tokenValue, userInfo) {
    token.value = tokenValue
    user.value = userInfo
    localStorage.setItem('nova_token', tokenValue)
    localStorage.setItem('nova_user', JSON.stringify(userInfo))
  }

  function patchUser(partial) {
    const next = { ...(user.value || {}), ...(partial || {}) }
    user.value = next
    localStorage.setItem('nova_user', JSON.stringify(next))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('nova_token')
    localStorage.removeItem('nova_user')
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    isGuest,
    nickname,
    avatar,
    setAuth,
    patchUser,
    logout,
  }
})
