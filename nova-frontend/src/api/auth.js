import { api } from '@/composables/useRequest'

export const loginApi = (data) => api.post('/api/auth/login', data)

export const guestLoginApi = () => api.post('/api/auth/guest')

export const registerApi = (data) => api.post('/api/auth/register', data)

export const resetPasswordApi = (data) => api.post('/api/auth/password/reset', data)

export const sendEmailCodeApi = (data) => api.post('/api/auth/email/send-code', data)

export const logoutApi = () => api.post('/api/auth/logout')

export const getMeApi = () => api.get('/api/auth/profile')
