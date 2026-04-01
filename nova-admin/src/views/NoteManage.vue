<template>
  <div class="space-y-4">
    <div class="bg-admin-card rounded-lg p-4 shadow-sm border border-gray-100 flex flex-wrap items-center gap-3">
      <div class="relative flex-1 min-w-[220px] max-w-[340px]">
        <Search class="w-4 h-4 text-admin-muted absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          v-model="keyword"
          @keyup.enter="loadData(1)"
          type="text"
          placeholder="搜索手记标题"
          class="w-full pl-9 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-admin-primary/20 focus:border-admin-primary outline-none transition-all"
        />
      </div>

      <input
        v-model="categoryFilter"
        @keyup.enter="loadData(1)"
        type="text"
        placeholder="分类（如：技术手记）"
        class="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-admin-primary/20 outline-none"
      />

      <select
        v-model="statusFilter"
        @change="loadData(1)"
        class="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-admin-primary/20 outline-none"
      >
        <option value="">全部状态</option>
        <option value="0">待审核</option>
        <option value="1">已通过</option>
        <option value="2">已驳回</option>
      </select>

      <button
        @click="openCreate"
        class="ml-auto inline-flex items-center gap-1.5 px-3 py-2 bg-admin-primary text-white rounded-lg text-sm hover:bg-admin-primary/90 transition-colors"
      >
        <Plus class="w-4 h-4" />
        新增手记
      </button>
    </div>

    <div class="bg-admin-card rounded-lg shadow-sm border border-gray-100 overflow-hidden">
      <div v-if="loading" class="p-8 text-center text-admin-muted">
        <div class="w-6 h-6 border-2 border-admin-primary/30 border-t-admin-primary rounded-full animate-spin mx-auto mb-2"></div>
        加载中...
      </div>

      <div v-else-if="error" class="p-8 text-center text-admin-danger">{{ error }}</div>

      <table v-else class="w-full text-sm">
        <thead class="bg-gray-50 text-admin-muted">
          <tr>
            <th class="text-left px-4 py-3 font-medium">ID</th>
            <th class="text-left px-4 py-3 font-medium">标题</th>
            <th class="text-left px-4 py-3 font-medium">分类</th>
            <th class="text-left px-4 py-3 font-medium">作者</th>
            <th class="text-left px-4 py-3 font-medium">浏览量</th>
            <th class="text-left px-4 py-3 font-medium">状态</th>
            <th class="text-left px-4 py-3 font-medium">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="note in notes" :key="note.id" class="hover:bg-gray-50/50 transition-colors">
            <td class="px-4 py-3 text-admin-muted">{{ note.id }}</td>
            <td class="px-4 py-3 font-medium text-admin-text max-w-[380px]">
              <div class="truncate">
                <span class="mr-1.5">{{ note.emoji || '📘' }}</span>
                {{ note.title }}
              </div>
              <p v-if="note.status === 2 && note.rejectReason" class="mt-1 text-xs text-rose-600 line-clamp-2">
                失败原因：{{ note.rejectReason }}
              </p>
            </td>
            <td class="px-4 py-3 text-admin-muted">{{ note.category || '-' }}</td>
            <td class="px-4 py-3 text-admin-muted">{{ note.author || '-' }}</td>
            <td class="px-4 py-3 text-admin-muted">{{ note.viewCount || 0 }}</td>
            <td class="px-4 py-3">
              <span class="px-2 py-1 rounded-full text-xs font-medium" :class="statusClass(note.status)">
                {{ statusText(note.status) }}
              </span>
            </td>
            <td class="px-4 py-3">
              <div class="flex items-center flex-wrap gap-2">
                <button
                  v-if="note.status === 0"
                  @click="handleAudit(note, 1)"
                  class="inline-flex items-center gap-1 px-2 py-1 text-xs rounded border border-emerald-200 text-emerald-700 hover:bg-emerald-50"
                >
                  <Check class="w-3.5 h-3.5" />
                  通过
                </button>
                <button
                  v-if="note.status === 0"
                  @click="handleAudit(note, 2)"
                  class="inline-flex items-center gap-1 px-2 py-1 text-xs rounded border border-rose-200 text-rose-700 hover:bg-rose-50"
                >
                  <XCircle class="w-3.5 h-3.5" />
                  驳回
                </button>
                <button
                  @click="openEdit(note)"
                  class="inline-flex items-center gap-1 px-2 py-1 text-xs rounded border border-gray-200 hover:bg-gray-50 text-admin-text"
                >
                  <Pencil class="w-3.5 h-3.5" />
                  编辑
                </button>
                <button
                  @click="handleDelete(note)"
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
      <div class="w-full max-w-3xl bg-white rounded-xl shadow-xl border border-gray-100">
        <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 class="text-base font-semibold text-admin-text">{{ dialogMode === 'create' ? '新增手记' : '编辑手记' }}</h3>
          <button @click="closeDialog" class="p-1.5 rounded hover:bg-gray-100 text-admin-muted">
            <X class="w-4 h-4" />
          </button>
        </div>
        <div class="p-5 space-y-4">
          <div>
            <label class="block text-xs text-admin-muted mb-1">标题</label>
            <input v-model="form.title" type="text" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-5 gap-3">
            <div>
              <label class="block text-xs text-admin-muted mb-1">分类</label>
              <input v-model="form.category" type="text" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">作者</label>
              <input v-model="form.author" type="text" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">封面表情</label>
              <input v-model="form.emoji" type="text" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">浏览量</label>
              <input v-model.number="form.viewCount" type="number" min="0" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">状态</label>
              <select v-model.number="form.status" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20">
                <option :value="0">待审核</option>
                <option :value="1">已通过</option>
                <option :value="2">已驳回</option>
              </select>
            </div>
          </div>

          <div v-if="form.status === 2">
            <label class="block text-xs text-admin-muted mb-1">审核失败原因（展示给投稿用户）</label>
            <input v-model="form.rejectReason" type="text" maxlength="240" class="w-full px-3 py-2 border border-rose-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-rose-200" placeholder="请输入审核失败原因" />
          </div>

          <div>
            <label class="block text-xs text-admin-muted mb-1">正文（支持 Markdown）</label>
            <textarea v-model="form.content" rows="10" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20 resize-y"></textarea>
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
import { onMounted, reactive, ref } from 'vue'
import { Search, Plus, Pencil, Trash2, X, Check, XCircle } from 'lucide-vue-next'
import { auditNoteStatus, createNote, deleteNote, getNoteDetail, getNoteList, updateNote } from '@/api/admin'

const loading = ref(true)
const error = ref(null)
const notes = ref([])
const keyword = ref('')
const categoryFilter = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const total = ref(0)
const totalPages = ref(1)

const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const submitting = ref(false)

const form = reactive({
  title: '',
  content: '',
  category: '技术手记',
  emoji: '📘',
  author: 'Nova 学员',
  viewCount: 0,
  status: 1,
  rejectReason: '',
})

const statusText = (status) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '未知'
}

const statusClass = (status) => {
  if (status === 0) return 'bg-amber-100 text-amber-700'
  if (status === 1) return 'bg-emerald-100 text-emerald-700'
  if (status === 2) return 'bg-rose-100 text-rose-700'
  return 'bg-gray-100 text-gray-600'
}

const resetForm = () => {
  form.title = ''
  form.content = ''
  form.category = '技术手记'
  form.emoji = '📘'
  form.author = 'Nova 学员'
  form.viewCount = 0
  form.status = 1
  form.rejectReason = ''
}

const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEdit = async (note) => {
  let editableNote = note
  if (!editableNote?.content) {
    try {
      const res = await getNoteDetail(note.id)
      if (res.code === 200 && res.data) {
        editableNote = res.data
      }
    } catch (_) {
      // noop
    }
  }
  dialogMode.value = 'edit'
  editingId.value = editableNote.id
  form.title = editableNote.title || ''
  form.content = editableNote.content || ''
  form.category = editableNote.category || '技术手记'
  form.emoji = editableNote.emoji || '📘'
  form.author = editableNote.author || 'Nova 学员'
  form.viewCount = editableNote.viewCount ?? 0
  form.status = editableNote.status ?? 0
  form.rejectReason = editableNote.rejectReason || ''
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  submitting.value = false
}

const submitDialog = async () => {
  if (!form.title.trim()) {
    alert('标题不能为空')
    return
  }
  if (!form.content.trim()) {
    alert('正文不能为空')
    return
  }
  if ((form.status ?? 1) === 2 && !form.rejectReason.trim()) {
    alert('请输入审核失败原因')
    return
  }

  submitting.value = true
  try {
    const payload = {
      title: form.title.trim(),
      content: form.content,
      category: form.category || '技术手记',
      emoji: form.emoji || '📘',
      author: form.author || 'Nova 学员',
      viewCount: Number.isFinite(form.viewCount) ? Math.max(0, form.viewCount) : 0,
      status: form.status ?? 1,
      rejectReason: (form.status ?? 1) === 2 ? form.rejectReason.trim() : '',
    }

    const res = dialogMode.value === 'create'
      ? await createNote(payload)
      : await updateNote(editingId.value, payload)

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

const handleAudit = async (note, targetStatus) => {
  const actionText = targetStatus === 1 ? '通过' : '驳回'
  let rejectReason = ''

  if (targetStatus === 2) {
    const input = window.prompt(`请输入“${note.title}”的审核失败原因（会展示给用户）`, note.rejectReason || '')
    if (input === null) return
    rejectReason = input.trim()
    if (!rejectReason) {
      alert('审核失败原因不能为空')
      return
    }
  }

  if (!confirm(`确认${actionText}《${note.title}》吗？`)) return

  try {
    const res = await auditNoteStatus(note.id, targetStatus, rejectReason)
    if (res.code === 200) {
      await loadData(currentPage.value)
    } else {
      alert(res.msg || '审核失败')
    }
  } catch (e) {
    alert(e.message || '审核失败')
  }
}

const handleDelete = async (note) => {
  if (!confirm(`确定删除手记《${note.title}》吗？`)) return
  try {
    const res = await deleteNote(note.id)
    if (res.code === 200) {
      const nextPage = notes.value.length === 1 && currentPage.value > 1 ? currentPage.value - 1 : currentPage.value
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
    const res = await getNoteList(page, 10, keyword.value, categoryFilter.value, statusFilter.value)
    if (res.code === 200) {
      notes.value = res.data.records || []
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
