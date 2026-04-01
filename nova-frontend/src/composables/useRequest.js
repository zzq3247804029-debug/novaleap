/**
 * useRequest — 统一 API 请求封装（组合式函数）
 * 封装 loading / error / data 状态管理
 */
import { ref } from 'vue'

const BASE_URL = ''  // 开发环境通过 Vite 代理，无需前缀

function normalizeUrl(url) {
  const raw = String(url || '').trim()
  if (!raw) return '/'
  if (/^https?:\/\//i.test(raw)) {
    return raw
  }
  return raw.startsWith('/') ? raw : `/${raw}`
}

/**
 * 通用请求函数
 */
async function request(url, options = {}) {
  const token = localStorage.getItem('nova_token')
  const isFormData = options.body instanceof FormData

  const headers = {
    ...options.headers,
  }
  if (!isFormData) {
    headers['Content-Type'] = headers['Content-Type'] || 'application/json'
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(`${BASE_URL}${normalizeUrl(url)}`, {
    ...options,
    headers,
    body: options.body
      ? (isFormData ? options.body : JSON.stringify(options.body))
      : undefined,
  })

  if (!response.ok) {
    // 401 未授权 → 清除 token
    if (response.status === 401) {
      localStorage.removeItem('nova_token')
      localStorage.removeItem('nova_user')
      window.location.href = '/login'
    }
    const errorData = await response.json().catch(() => ({}))
    throw new Error(errorData.msg || `请求失败: HTTP ${response.status}`)
  }

  return response.json()
}

/**
 * useRequest — 组合式函数
 * @param {Function} apiFn - 返回 Promise 的 API 调用函数
 * @returns {{ data, loading, error, execute }}
 */
export function useRequest(apiFn) {
  const data = ref(null)
  const loading = ref(false)
  const error = ref(null)

  async function execute(...args) {
    loading.value = true
    error.value = null
    try {
      const result = await apiFn(...args)
      // 统一解包 Result<T> 响应
      if (result.code === 200) {
        data.value = result.data
      } else {
        throw new Error(result.msg || '未知错误')
      }
      return data.value
    } catch (e) {
      error.value = e.message
      throw e
    } finally {
      loading.value = false
    }
  }

  return { data, loading, error, execute }
}

// 导出便捷方法
export const api = {
  get: (url, options = {}) => request(url, { method: 'GET', ...options }),
  post: (url, body) => request(url, { method: 'POST', body }),
  put: (url, body) => request(url, { method: 'PUT', body }),
  delete: (url) => request(url, { method: 'DELETE' }),
  upload: (url, formData) => request(url, { method: 'POST', body: formData }),
}
