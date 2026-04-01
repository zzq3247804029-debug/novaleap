import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import {
  guestLoginApi,
  loginApi,
  logoutApi,
  registerApi,
  resetPasswordApi,
  sendEmailCodeApi,
} from '@/api/auth'
import { regenerateVisitorId } from '@/services/analytics'

const DEFAULT_AVATAR = '🚀'

function normalizeEmail(email) {
  return String(email || '').trim().toLowerCase()
}

function normalizeRegisterPayload(payloadOrUsername, legacyArgs) {
  if (payloadOrUsername && typeof payloadOrUsername === 'object' && !Array.isArray(payloadOrUsername)) {
    return { ...payloadOrUsername }
  }

  const [
    password = '',
    nickname = '',
    confirmPassword = '',
    consent = true,
    turnstileToken = '',
    emailCode = '',
  ] = legacyArgs

  return {
    username: payloadOrUsername,
    password,
    nickname,
    confirmPassword,
    consent,
    turnstileToken,
    emailCode,
  }
}

export function useAuth() {
  const authStore = useAuthStore()
  const router = useRouter()

  const normalizeCheckin = (checkin) => ({
    streakDays: Math.max(0, Number(checkin?.streakDays || 0)),
    signedToday: !!checkin?.signedToday,
  })

  const applyAuth = (res) => {
    if (res.code !== 200) {
      throw new Error(res.msg || '认证失败')
    }

    const normalizedCheckin = normalizeCheckin(res.data?.checkin)
    const mergedUser = {
      ...(res.data.user || {}),
      avatar: res.data.avatar || authStore.avatar || DEFAULT_AVATAR,
      checkin: normalizedCheckin,
    }

    authStore.setAuth(res.data.token, mergedUser)
    router.push('/')
    return res.data
  }

  async function login(username, passwordOrOptions, turnstileTokenArg = '') {
    const isObjectMode = passwordOrOptions && typeof passwordOrOptions === 'object' && !Array.isArray(passwordOrOptions)
    const loginType = isObjectMode
      ? (String(passwordOrOptions.loginType || 'password').toLowerCase() === 'code' ? 'code' : 'password')
      : 'password'
    const password = isObjectMode ? (passwordOrOptions.password || '') : (passwordOrOptions || '')
    const emailCode = isObjectMode ? String(passwordOrOptions.emailCode || '').trim() : ''
    const turnstileToken = isObjectMode ? (passwordOrOptions.turnstileToken || '') : turnstileTokenArg

    const res = await loginApi({
      username: String(username || '').trim(),
      password,
      emailCode,
      loginType,
      turnstileToken,
    })
    return applyAuth(res)
  }

  async function guestLogin() {
    regenerateVisitorId()
    const res = await guestLoginApi()
    return applyAuth(res)
  }

  async function register(payloadOrUsername, ...legacyArgs) {
    const payload = normalizeRegisterPayload(payloadOrUsername, legacyArgs)
    const res = await registerApi({
      username: normalizeEmail(payload.username),
      password: payload.password,
      confirmPassword: payload.confirmPassword,
      nickname: String(payload.nickname || '').trim(),
      consent: !!payload.consent,
      emailCode: String(payload.emailCode || '').trim(),
      turnstileToken: payload.turnstileToken || '',
    })

    if (res.code !== 200) {
      throw new Error(res.msg || '注册失败')
    }

    return res.data
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (_) {
      // ignore and force client logout
    }
    authStore.logout()
    router.push('/login')
  }

  async function sendEmailCode(email, type) {
    const res = await sendEmailCodeApi({
      email: normalizeEmail(email),
      type,
    })
    if (res.code !== 200) {
      throw new Error(res.msg || '发送验证码失败')
    }
    return res.data
  }

  async function resetPassword(username, newPassword, emailCode) {
    const res = await resetPasswordApi({
      username: normalizeEmail(username),
      newPassword,
      emailCode: String(emailCode || '').trim(),
    })
    if (res.code !== 200) {
      throw new Error(res.msg || '重置密码失败')
    }
    return true
  }

  return {
    login,
    guestLogin,
    register,
    logout,
    sendEmailCode,
    resetPassword,
    isLoggedIn: authStore.isLoggedIn,
    isGuest: authStore.isGuest,
    user: authStore.user,
  }
}
