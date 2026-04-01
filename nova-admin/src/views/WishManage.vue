<template>
  <div class="space-y-4">
    <div class="bg-admin-card rounded-lg p-4 shadow-sm border border-gray-100 flex flex-wrap items-center gap-3">
      <select
        v-model="statusFilter"
        @change="loadData(1)"
        class="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-admin-primary/20 outline-none"
      >
        <option value="">全部状态</option>
        <option value="0">待审核</option>
        <option value="1">已通过</option>
        <option value="2">已拒绝</option>
      </select>
      <span class="text-xs text-admin-muted ml-auto">提示：审核通过的星愿展示在 C 端星愿墙</span>
      <button
        @click="openCreate"
        class="inline-flex items-center gap-1.5 px-3 py-2 bg-admin-primary text-white rounded-lg text-sm hover:bg-admin-primary/90 transition-colors"
      >
        <Plus class="w-4 h-4" />
        新增星愿
      </button>
    </div>

    <div class="grid grid-cols-2 xl:grid-cols-4 gap-3">
      <div class="bg-admin-card rounded-lg border border-gray-100 p-4">
        <div class="text-xs text-admin-muted mb-1">待消费队列</div>
        <div class="text-2xl font-semibold text-admin-text">{{ queueStats.pendingQueueSize }}</div>
      </div>
      <div class="bg-admin-card rounded-lg border border-gray-100 p-4">
        <div class="text-xs text-admin-muted mb-1">处理中队列</div>
        <div class="text-2xl font-semibold text-admin-text">{{ queueStats.processingQueueSize }}</div>
      </div>
      <div class="bg-admin-card rounded-lg border border-gray-100 p-4">
        <div class="text-xs text-admin-muted mb-1">Dead Letter</div>
        <div class="text-2xl font-semibold text-admin-danger">{{ queueStats.deadLetterSize }}</div>
      </div>
      <div class="bg-admin-card rounded-lg border border-gray-100 p-4">
        <div class="text-xs text-admin-muted mb-1">数据库待审核</div>
        <div class="text-2xl font-semibold text-admin-text">{{ queueStats.pendingDbCount }}</div>
      </div>
    </div>

    <div class="bg-admin-card rounded-lg border border-gray-100 p-4 space-y-3">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h3 class="text-sm font-semibold text-admin-text">异步补偿面板</h3>
          <p class="text-xs text-admin-muted">查看 dead-letter 愿望并手动重试审核</p>
        </div>
        <button
          @click="loadQueueState"
          :disabled="queueLoading"
          class="inline-flex items-center gap-1.5 px-3 py-2 text-xs rounded border border-gray-200 hover:bg-gray-50 text-admin-text disabled:opacity-50"
        >
          <RefreshCw class="w-3.5 h-3.5" :class="{ 'animate-spin': queueLoading }" />
          刷新队列
        </button>
      </div>

      <div v-if="queueLoading" class="text-sm text-admin-muted">队列状态加载中...</div>
      <div v-else-if="deadLetters.length === 0" class="text-sm text-admin-muted">当前没有 dead-letter 愿望</div>
      <div v-else class="space-y-3">
        <div
          v-for="wish in deadLetters"
          :key="wish.id"
          class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 rounded-lg border border-red-100 bg-red-50/40 p-3"
        >
          <div class="min-w-0">
            <div class="text-xs text-admin-muted mb-1">#{{ wish.id }} · {{ wish.city || '未设置城市' }}</div>
            <p class="text-sm text-admin-text leading-relaxed break-words">{{ wish.content }}</p>
          </div>
          <button
            @click="handleRetry(wish.id)"
            :disabled="retrying[wish.id]"
            class="inline-flex items-center justify-center gap-1.5 px-3 py-2 text-xs rounded border border-amber-200 bg-white text-amber-700 hover:bg-amber-50 disabled:opacity-50"
          >
            <RefreshCw class="w-3.5 h-3.5" :class="{ 'animate-spin': retrying[wish.id] }" />
            重试审核
          </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="p-8 text-center text-admin-muted">
      <div class="w-6 h-6 border-2 border-admin-primary/30 border-t-admin-primary rounded-full animate-spin mx-auto mb-2"></div>
      加载中...
    </div>
    <div v-else-if="error" class="p-8 text-center text-admin-danger bg-admin-card rounded-lg border border-gray-100">{{ error }}</div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <div v-for="wish in wishes" :key="wish.id" class="bg-admin-card rounded-xl p-5 shadow-sm border border-gray-100 flex flex-col justify-between hover:shadow-md transition-shadow">
        <div>
          <div class="flex items-center justify-between mb-3">
            <span class="text-xs text-admin-muted">#{{ wish.id }} · {{ wish.city || '未知坐标' }}</span>
            <span
              class="px-2 py-0.5 rounded-full text-[11px] font-medium"
              :class="{
                'bg-amber-100 text-amber-700': wish.status === 0,
                'bg-green-100 text-green-700': wish.status === 1,
                'bg-red-100 text-red-700': wish.status === 2,
              }"
            >
              {{ statusText(wish.status) }}
            </span>
          </div>

          <p class="text-admin-text text-sm leading-relaxed mb-4">{{ wish.content }}</p>

          <div class="flex items-center gap-2 mb-4" v-if="wish.emotion">
            <span class="px-2 py-1 text-[10px] rounded-full font-medium" :style="{ backgroundColor: wish.color || '#f5f5f5' }">
              {{ emotionMap[wish.emotion] || wish.emotion }}
            </span>
          </div>
        </div>

        <div class="space-y-2 pt-3 border-t border-gray-100 mt-auto">
          <div v-if="wish.status === 0" class="flex gap-2">
            <button
              @click="handleAudit(wish.id, 1)"
              :disabled="auditing[wish.id]"
              class="flex-1 px-3 py-2 bg-admin-success/10 text-admin-success hover:bg-admin-success/20 rounded-lg text-xs font-medium transition-colors disabled:opacity-50"
            >
              通过
            </button>
            <button
              @click="handleAudit(wish.id, 2)"
              :disabled="auditing[wish.id]"
              class="flex-1 px-3 py-2 bg-admin-danger/10 text-admin-danger hover:bg-admin-danger/20 rounded-lg text-xs font-medium transition-colors disabled:opacity-50"
            >
              拒绝
            </button>
          </div>
          <div class="flex gap-2">
            <button
              @click="openEdit(wish)"
              class="flex-1 inline-flex items-center justify-center gap-1 px-3 py-2 text-xs rounded border border-gray-200 hover:bg-gray-50 text-admin-text"
            >
              <Pencil class="w-3.5 h-3.5" />
              编辑
            </button>
            <button
              @click="handleDelete(wish)"
              class="flex-1 inline-flex items-center justify-center gap-1 px-3 py-2 text-xs rounded border border-red-200 text-admin-danger hover:bg-red-50"
            >
              <Trash2 class="w-3.5 h-3.5" />
              删除
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!loading && !error && wishes.length === 0" class="p-12 text-center text-admin-muted bg-admin-card rounded-lg border border-gray-100">
      <Star class="w-10 h-10 mx-auto mb-3 opacity-20" />
      <p>暂无星愿数据</p>
    </div>

    <div v-if="!loading && !error && wishes.length > 0" class="flex items-center justify-between px-2 py-3 text-sm text-admin-muted">
      <span>共 {{ total }} 条</span>
      <div class="flex gap-1">
        <button @click="loadData(currentPage - 1)" :disabled="currentPage <= 1" class="px-3 py-1 border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-40 transition-colors bg-white">上一页</button>
        <span class="px-3 py-1 bg-admin-primary/10 text-admin-primary rounded font-medium">{{ currentPage }}</span>
        <button @click="loadData(currentPage + 1)" :disabled="currentPage >= totalPages" class="px-3 py-1 border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-40 transition-colors bg-white">下一页</button>
      </div>
    </div>

    <div v-if="dialogVisible" class="fixed inset-0 z-40 bg-black/30 flex items-center justify-center p-4">
      <div class="w-full max-w-2xl bg-white rounded-xl shadow-xl border border-gray-100">
        <div class="px-5 py-4 border-b border-gray-100 flex items-center justify-between">
          <h3 class="text-base font-semibold text-admin-text">{{ dialogMode === 'create' ? '新增星愿' : '编辑星愿' }}</h3>
          <button @click="closeDialog" class="p-1.5 rounded hover:bg-gray-100 text-admin-muted">
            <X class="w-4 h-4" />
          </button>
        </div>
        <div class="p-5 space-y-4">
          <div>
            <label class="block text-xs text-admin-muted mb-1">星愿内容</label>
            <textarea v-model="form.content" rows="4" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20 resize-y"></textarea>
          </div>
          <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
            <div>
              <label class="block text-xs text-admin-muted mb-1">城市</label>
              <input v-model="form.city" type="text" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">情绪</label>
              <input v-model="form.emotion" type="text" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">颜色</label>
              <input v-model="form.color" type="text" placeholder="#D4E0D0" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">状态</label>
              <select v-model.number="form.status" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20">
                <option :value="0">待审核</option>
                <option :value="1">已通过</option>
                <option :value="2">已拒绝</option>
              </select>
            </div>
          </div>
          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="block text-xs text-admin-muted mb-1">位置X</label>
              <input v-model.number="form.posX" type="number" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">位置Y</label>
              <input v-model.number="form.posY" type="number" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
            </div>
            <div>
              <label class="block text-xs text-admin-muted mb-1">浮动速度</label>
              <input v-model.number="form.floatSpeed" type="number" step="0.1" class="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:ring-2 focus:ring-admin-primary/20" />
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
import { Star, Plus, Pencil, Trash2, X, RefreshCw } from 'lucide-vue-next'
import { getWishList, updateWishStatus, createWish, updateWish, deleteWish, getWishQueueStats, getDeadLetterWishes, retryWishReview } from '@/api/admin'

const loading = ref(true)
const error = ref(null)
const wishes = ref([])
const queueLoading = ref(false)
const deadLetters = ref([])
const statusFilter = ref('')
const currentPage = ref(1)
const total = ref(0)
const totalPages = ref(1)
const auditing = reactive({})
const retrying = reactive({})
const queueStats = reactive({
  pendingQueueSize: 0,
  processingQueueSize: 0,
  deadLetterSize: 0,
  pendingDbCount: 0,
})

const dialogVisible = ref(false)
const dialogMode = ref('create')
const editingId = ref(null)
const submitting = ref(false)
const form = reactive({
  content: '',
  emotion: '',
  color: '',
  city: '',
  posX: null,
  posY: null,
  floatSpeed: null,
  status: 0,
})

const emotionMap = {
  happy: '充满希望',
  hopeful: '积极期待',
  confused: '有些迷茫',
  anxious: '焦虑不安',
  determined: '意志坚定',
  confident: '自信笃定',
}

const statusText = (status) => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[status] || '未知')

const resetForm = () => {
  form.content = ''
  form.emotion = ''
  form.color = ''
  form.city = ''
  form.posX = null
  form.posY = null
  form.floatSpeed = null
  form.status = 0
}

const openCreate = () => {
  dialogMode.value = 'create'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEdit = (wish) => {
  dialogMode.value = 'edit'
  editingId.value = wish.id
  form.content = wish.content || ''
  form.emotion = wish.emotion || ''
  form.color = wish.color || ''
  form.city = wish.city || ''
  form.posX = wish.posX
  form.posY = wish.posY
  form.floatSpeed = wish.floatSpeed
  form.status = wish.status ?? 0
  dialogVisible.value = true
}

const closeDialog = () => {
  dialogVisible.value = false
  submitting.value = false
}

const submitDialog = async () => {
  if (!form.content.trim()) {
    alert('星愿内容不能为空')
    return
  }

  submitting.value = true
  try {
    const payload = {
      content: form.content.trim(),
      emotion: form.emotion || '',
      color: form.color || '',
      city: form.city || '',
      posX: form.posX,
      posY: form.posY,
      floatSpeed: form.floatSpeed,
      status: form.status,
    }

    const res = dialogMode.value === 'create'
      ? await createWish(payload)
      : await updateWish(editingId.value, payload)

    if (res.code === 200) {
      closeDialog()
      await loadData(dialogMode.value === 'create' ? 1 : currentPage.value)
      await loadQueueState()
    } else {
      alert(res.msg || '保存失败')
    }
  } catch (e) {
    alert(e.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (wish) => {
  if (!confirm(`确定删除星愿 #${wish.id} 吗？`)) return
  try {
    const res = await deleteWish(wish.id)
    if (res.code === 200) {
      const nextPage = wishes.value.length === 1 && currentPage.value > 1 ? currentPage.value - 1 : currentPage.value
      await loadData(nextPage)
      await loadQueueState()
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
    const res = await getWishList(page, 12, statusFilter.value)
    if (res.code === 200) {
      wishes.value = res.data.records || []
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

const loadQueueState = async () => {
  queueLoading.value = true
  try {
    const [statsRes, deadRes] = await Promise.all([
      getWishQueueStats(),
      getDeadLetterWishes(20),
    ])
    if (statsRes.code === 200 && statsRes.data) {
      queueStats.pendingQueueSize = statsRes.data.pendingQueueSize || 0
      queueStats.processingQueueSize = statsRes.data.processingQueueSize || 0
      queueStats.deadLetterSize = statsRes.data.deadLetterSize || 0
      queueStats.pendingDbCount = statsRes.data.pendingDbCount || 0
    }
    if (deadRes.code === 200) {
      deadLetters.value = deadRes.data || []
    }
  } catch (e) {
    console.error('load wish queue state failed', e)
  } finally {
    queueLoading.value = false
  }
}

const handleAudit = async (id, status) => {
  auditing[id] = true
  try {
    const res = await updateWishStatus(id, status)
    if (res.code === 200) {
      const wish = wishes.value.find((item) => item.id === id)
      if (wish) wish.status = status
      await loadQueueState()
    } else {
      alert(res.msg || '审核失败')
    }
  } catch (e) {
    alert(`审核失败: ${e.message}`)
  } finally {
    auditing[id] = false
  }
}

const handleRetry = async (id) => {
  retrying[id] = true
  try {
    const res = await retryWishReview(id)
    if (res.code === 200) {
      await loadQueueState()
      await loadData(currentPage.value)
    } else {
      alert(res.msg || '重试失败')
    }
  } catch (e) {
    alert(e.message || '重试失败')
  } finally {
    retrying[id] = false
  }
}

onMounted(async () => {
  await Promise.all([loadData(1), loadQueueState()])
})
</script>
