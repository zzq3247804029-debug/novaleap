<template>
  <div class="h-screen w-screen bg-admin-bg flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-admin-card rounded-xl shadow-lg border border-gray-100 p-8">
      <div class="text-center mb-8">
        <div class="w-12 h-12 rounded-xl bg-admin-primary/10 text-admin-primary flex items-center justify-center mx-auto mb-4">
          <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-admin-text">Nova Admin</h1>
        <p class="text-sm text-admin-muted mt-1">管理端系统看板</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-5">
        <div>
          <label class="block text-sm font-medium text-admin-text mb-1">管理员账号</label>
          <input 
            type="text"
            v-model="form.username" 
            required 
            placeholder="请输入邮箱/账号"
            class="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-admin-primary transition-colors text-sm"
          />
        </div>
        
        <div>
          <label class="block text-sm font-medium text-admin-text mb-1">安全密码</label>
          <input 
            type="password" 
            v-model="form.password" 
            required 
            placeholder="••••••••"
            class="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-admin-primary transition-colors text-sm"
          />
        </div>

        <button 
          type="submit" 
          :disabled="loading"
          class="w-full bg-admin-primary hover:bg-admin-primary/90 text-white font-medium py-2 rounded-lg transition-colors flex justify-center items-center text-sm disabled:opacity-75"
        >
          <span v-if="loading" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2"></span>
          {{ loading ? '验证中...' : '登 录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

// 这里的 api 引用路径根据实际项目微调，通常在 @/composables/useRequest.js 里
import { api } from '@/composables/useRequest'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const loading = ref(false)

const handleLogin = async () => {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const res = await api.post('/api/auth/login', form)
    if (res.code === 200) {
      if (res.data.user.role !== 'ADMIN') {
        alert('该账号无管理端访问权限')
        return
      }
      localStorage.setItem('nova_admin_token', res.data.token)
      router.push('/dashboard')
    } else {
      alert(res.msg || '登录失败')
    }
  } catch (e) {
    alert(e.message || '网络或接口故障')
  } finally {
    loading.value = false
  }
}
</script>
