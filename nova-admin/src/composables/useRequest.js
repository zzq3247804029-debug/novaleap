/**
 * useRequest — 管理端统一 API 请求封装
 * 提供 JWT 鉴权头注入、统一错误处理、401 自动跳转
 */
const BASE_URL = ''  // 开发环境通过 Vite 代理

/**
 * 通用请求函数
 */
async function request(url, options = {}) {
  const token = localStorage.getItem('nova_admin_token')
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

  const response = await fetch(`${BASE_URL}${url}`, {
    ...options,
    headers,
    body: options.body
      ? (isFormData ? options.body : JSON.stringify(options.body))
      : undefined,
  })

  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      localStorage.removeItem('nova_admin_token')
      window.location.href = '/login'
    }
    const errorData = await response.json().catch(() => ({}))
    throw new Error(errorData.msg || `请求失败: HTTP ${response.status}`)
  }

  return response.json()
}

// 导出便捷方法
export const api = {
  get: (url) => request(url, { method: 'GET' }),
  post: (url, body) => request(url, { method: 'POST', body }),
  put: (url, body) => request(url, { method: 'PUT', body }),
  delete: (url) => request(url, { method: 'DELETE' }),
  upload: (url, formData) => request(url, { method: 'POST', body: formData }),
}
