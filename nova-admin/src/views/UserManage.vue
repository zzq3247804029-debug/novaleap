<template>
  <div class="space-y-4">
    <div class="bg-admin-card rounded-lg p-4 shadow-sm border border-gray-100 flex flex-wrap items-center gap-3">
      <div class="relative flex-1 min-w-[200px] max-w-[320px]">
        <Search class="w-4 h-4 text-admin-muted absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          v-model="keyword"
          @keyup.enter="loadData(1)"
          type="text"
          placeholder="搜索用户名 / 昵称"
          class="w-full pl-9 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-admin-primary/20 focus:border-admin-primary outline-none transition-all"
        />
      </div>
      <select
        v-model="roleFilter"
        @change="loadData(1)"
        class="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-admin-primary/20 outline-none"
      >
        <option value="">全部角色</option>
        <option value="ADMIN">管理员</option>
        <option value="USER">普通用户</option>
        <option value="GUEST">游客</option>
      </select>

      <button
        @click="openCreate"
        class="ml-auto inline-flex items-center gap-1.5 px-3 py-2 bg-admin-primary text-white rounded-lg text-sm hover:bg-admin-primary/90 transition-colors"
      >
        <Plus class="w-4 h-4" />
        新增用户
      </button>
    </div>

    <div class="bg-admin-card rounded-lg shadow-sm border border-gray-100 overflow-hidden">
      <div v-if="loading" class="p-8 text-center text-admin-muted">
        <div class="w-6 h-6 border-2 border-admin-primary/30 border-t-admin-primary rounded-full animate-spin mx-auto mb-2"></div>
        加载中...
      </div>
      <div v-else-if="error" class="p-8 text-center text-admin-danger">
        <AlertTriangle class="w-8 h-8 mx-auto mb-2 opacity-60" />
        {{ error }}
      </div>
      <table v-else class="w-full text-sm">
        <thead class="bg-gray-50 text-admin-muted">
          <tr>
            <th class="text-left px-4 py-3 font-medium">ID</th>
            <th class="text-left px-4 py-3 font-medium">用户名</th>
            <th class="text-left px-4 py-3 font-medium">昵称</th>
            <th class="text-left px-4 py-3 font-medium">角色</th>
            <th class="text-left px-4 py-3 font-medium">注册时间</th>
            <th class="text-left px-4 py-3 font-medium">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="user in users" :key="user.id" class="hover:bg-gray-50/50 transition-colors">
            <td class="px-4 py-3 text-admin-muted">{{ user.id }}</td>
            <td class="px-4 py-3 font-medium text-admin-text">{{ user.username }}</td>
            <td class="px-4 py-3 text-admin-text">{{ user.nickname }}</td>
            <td class="px-4 py-3">
              <span
                class="px-2 py-1 rounded-full text-xs font-medium"
                :class="{
                  'bg-purple-100 text-purple-700': user.role === 'ADMIN',
                  'bg-blue-100 text-blue-700': user.role === 'USER',
                  'bg-gray-100 text-gray-600': user.role === 'GUEST',
                }"
              >
                {{ roleText(user.role) }}
              </span>
            </td>
            <td class="px-4 py-3 text-admin-muted">{{ formatDate(user.createdAt) }}</td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <button
                  @click="openEdit(user)"
                  class="inline-flex items-center gap-1 px-2 py-1 text-xs rounded border border-gray-200 hover:bg-gray-50 text-admin-text"
                >
                  <Pencil class="w-3.5 h-3.5" />
                  编辑
                </button>
                <button
                  @click="handleDelete(user)"
                  class="inline-flex items-center gap-1 px-2 py-1 text-xs rounded border border-red-200 text-admin-danger hover:bg-red-50"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                  删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!loading && !error" class="flex items-center justify-between px-4 py-3 border-t border-gray-100 text-sm text-admin-muted">
        <span>共 {{ total }} 条</span>
        <div class="flex gap-1">
          <button @click="loadData(currentPage - 1)" :disabled="currentPage <= 1" class="px-3 py-1 border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-40 transition-colors">上一页</button>
          <span class="px-3 py-1 bg-admin-primary/10 text-admin-primary rounded font-medium">{{ currentPage }}</span>
          <button @click="loadData(currentPage + 1)" :disabled="currentPage >= totalPages" class="px-3 py-1 border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-40 transition-colors">下一页</button>
        </div>
      </div>
    </div>

    <div v-if="dialogVisible" class="fixed inset-0 z-40 bg-black/30 flex items-center justify-center p-4">
      <div class="w-full max-w-lg bg-white rounded-xl shadow-xl border border-gray-100">
        <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 class="text-base font-semibold text-admin-text">{{ dialogMode === 'create' ? '新增用户' : '编辑用户' }}</h3>
          <button @click="closeDialog" class="p-1.5 rounded hover:bg-gray-100 text-admin-muted">
            <X class="w-4 h-4" />
          </button>
        </div>
        <div class="p-5 space-y-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-xs text-admin-muted mb-1">用户名</label>
              <input v-model="form.username" type="text" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">昵称</label>
              <input v-model="form.nickname" type="text" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">角色</label>
              <select v-model="form.role" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20">
                <option value="ADMIN">管理员</option>
                <option value="USER">普通用户</option>
                <option value="GUEST">游客</option>
              </select>
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">{{ dialogMode === 'create' ? '密码' : '新密码(可留空)' }}</label>
              <input v-model="form.password" type="password" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
          </div>
        </div>
        <div class="px-5 py-4 border-t border-gray-100 flex justify-end gap-2">
          <button @click="closeDialog" class="px-3 py-2 rounded-lg border border-gray-200 text-sm hover:bg-gray-50">取消</button>
          <button @click="submitDialog" :disabled="submitting" class="px-3 py-2 rounded-lg bg-admin-primary text-white text-sm hover:bg-admin-primary/90 disabled:opacity-60">
            {{ submitting ? '提交中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { Search, AlertTriangle, Plus, Pencil, Trash2, X } from 'lucide-vue-next'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/admin'

const loading = ref(true)
const error = ref(null)
const users = ref([])
const keyword = ref('')
const roleFilter = ref('')
const currentPage = ref(1)
const total = ref(0)
const totalPages = ref(1)

const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const submitting = ref(false)
const form = reactive({
  username: '',
  nickname: '',
  role: 'USER',
  password: '',
})

const roleText = (role) => ({ ADMIN: '管理员', USER: '用户', GUEST: '游客' }[role] || role)

const formatDate = (dt) => {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('zh-CN', { hour12: false })
}

const resetForm = () => {
  form.username = ''
  form.nickname = ''
  form.role = 'USER'
  form.password = ''
}

const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEdit = (user) => {
  dialogMode.value = 'edit'
  editingId.value = user.id
  form.username = user.username || ''
  form.nickname = user.nickname || ''
  form.role = user.role || 'USER'
  form.password = ''
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  submitting.value = false
}

const submitDialog = async () => {
  if (!form.username.trim()) {
    alert('用户名不能为空')
    return
  }
  if (dialogMode.value === 'create' && !form.password) {
    alert('新增用户必须填写密码')
    return
  }

  submitting.value = true
  try {
    const payload = {
      username: form.username.trim(),
      nickname: form.nickname.trim(),
      role: form.role,
    }
    if (form.password) payload.password = form.password

    const res = dialogMode.value === 'create'
      ? await createUser(payload)
      : await updateUser(editingId.value, payload)

    if (res.code === 200) {
      closeDialog()
      await loadData(dialogMode.value === 'create' ? 1 : currentPage.value)
    } else {
      alert(res.msg || '保存失败')
    }
  } catch (e) {
    alert(e.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (user) => {
  if (!confirm(`确定删除用户「${user.username}」吗？`)) return
  try {
    const res = await deleteUser(user.id)
    if (res.code === 200) {
      const nextPage = users.value.length === 1 && currentPage.value > 1 ? currentPage.value - 1 : currentPage.value
      await loadData(nextPage)
    } else {
      alert(res.msg || '删除失败')
    }
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

const loadData = async (page = 1) => {
  if (page < 1) return
  loading.value = true
  error.value = null
  try {
    const res = await getUserList(page, 10, keyword.value, roleFilter.value)
    if (res.code === 200) {
      users.value = res.data.records || []
      total.value = res.data.total || 0
      totalPages.value = res.data.pages || 1
      currentPage.value = page
    } else {
      error.value = res.msg || '加载失败'
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData(1))
</script>
