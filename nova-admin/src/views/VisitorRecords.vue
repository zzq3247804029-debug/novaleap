<template>
  <div class="space-y-4">
    <div class="bg-admin-card rounded-lg p-4 border border-gray-100 shadow-sm">
      <div class="grid grid-cols-1 md:grid-cols-[1fr_180px_120px] gap-3">
        <input
          v-model.trim="keyword"
          @keyup.enter="reload"
          type="text"
          placeholder="搜索游客名 / 用户名 / IP / 地区"
          class="w-full rounded-md border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-admin-primary/20 focus:border-admin-primary"
        />
        <select
          v-model="actorType"
          class="rounded-md border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-admin-primary/20 focus:border-admin-primary"
        >
          <option value="">全部身份</option>
          <option value="guest">游客</option>
          <option value="user">注册用户</option>
        </select>
        <button
          @click="reload"
          class="rounded-md bg-admin-primary text-white text-sm font-medium hover:bg-admin-primary/90 transition-colors"
        >
          查询
        </button>
      </div>
      <div class="mt-3 text-xs text-admin-muted">
        规则：按“人”去重建档，游客命名为 <code>游客+UUID</code>，同一身份不会重复建档。
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
      <div class="bg-admin-card rounded-lg p-4 border border-gray-100 shadow-sm">
        <div class="text-xs text-admin-muted">当前筛选总人数</div>
        <div class="mt-1 text-2xl font-semibold text-admin-text">{{ total }}</div>
      </div>
      <div class="bg-admin-card rounded-lg p-4 border border-gray-100 shadow-sm">
        <div class="text-xs text-admin-muted">游客人数（当前页）</div>
        <div class="mt-1 text-2xl font-semibold text-admin-text">{{ pageGuestCount }}</div>
      </div>
      <div class="bg-admin-card rounded-lg p-4 border border-gray-100 shadow-sm">
        <div class="text-xs text-admin-muted">注册人数（当前页）</div>
        <div class="mt-1 text-2xl font-semibold text-admin-text">{{ pageUserCount }}</div>
      </div>
    </div>

    <div class="bg-admin-card rounded-lg border border-gray-100 shadow-sm overflow-hidden">
      <div v-if="loading" class="p-10 text-center text-sm text-admin-muted">加载中...</div>
      <div v-else-if="!records.length" class="p-10 text-center text-sm text-admin-muted">暂无访客记录</div>
      <div v-else class="overflow-auto">
        <table class="min-w-full text-sm">
          <thead class="bg-gray-50 text-admin-muted">
            <tr>
              <th class="px-4 py-3 text-left font-medium">访客名</th>
              <th class="px-4 py-3 text-left font-medium">身份</th>
              <th class="px-4 py-3 text-left font-medium">IP</th>
              <th class="px-4 py-3 text-left font-medium">地区</th>
              <th class="px-4 py-3 text-left font-medium">访问次数</th>
              <th class="px-4 py-3 text-left font-medium">首次访问</th>
              <th class="px-4 py-3 text-left font-medium">最近访问</th>
              <th class="px-4 py-3 text-left font-medium">最近路径</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in records" :key="row.identity" class="border-t border-gray-100 hover:bg-gray-50/50">
              <td class="px-4 py-3 text-admin-text">
                <div class="font-medium">{{ row.displayName || '-' }}</div>
                <div class="text-[11px] text-admin-muted mt-0.5">{{ row.username || row.identity }}</div>
              </td>
              <td class="px-4 py-3">
                <span
                  class="inline-flex px-2 py-0.5 rounded-full text-xs"
                  :class="row.actorType === 'user' ? 'bg-emerald-50 text-emerald-600' : 'bg-sky-50 text-sky-600'"
                >
                  {{ row.actorLabel }}
                </span>
              </td>
              <td class="px-4 py-3 text-admin-text">{{ row.ip || '-' }}</td>
              <td class="px-4 py-3 text-admin-text">{{ row.region || '-' }} / {{ row.city || '-' }}</td>
              <td class="px-4 py-3 text-admin-text">{{ row.visitCount || 0 }}</td>
              <td class="px-4 py-3 text-admin-muted">{{ row.firstSeenAt || '-' }}</td>
              <td class="px-4 py-3 text-admin-muted">{{ row.lastSeenAt || '-' }}</td>
              <td class="px-4 py-3 text-admin-muted">{{ row.lastPath || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="px-4 py-3 border-t border-gray-100 flex items-center justify-between text-sm">
        <div class="text-admin-muted">第 {{ page }} / {{ totalPages }} 页，共 {{ total }} 条</div>
        <div class="flex items-center gap-2">
          <button
            @click="prevPage"
            :disabled="page <= 1 || loading"
            class="px-3 py-1.5 rounded border border-gray-200 text-admin-text disabled:opacity-40 hover:bg-gray-50"
          >
            上一页
          </button>
          <button
            @click="nextPage"
            :disabled="page >= totalPages || loading"
            class="px-3 py-1.5 rounded border border-gray-200 text-admin-text disabled:opacity-40 hover:bg-gray-50"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getVisitorRecords } from '@/api/admin'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const keyword = ref('')
const actorType = ref('')

const totalPages = computed(() => Math.max(1, Math.ceil((Number(total.value) || 0) / size.value)))
const pageGuestCount = computed(() => records.value.filter((row) => row.actorType === 'guest').length)
const pageUserCount = computed(() => records.value.filter((row) => row.actorType === 'user').length)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getVisitorRecords(page.value, size.value, keyword.value, actorType.value)
    if (res.code !== 200) return
    const data = res.data || {}
    records.value = Array.isArray(data.records) ? data.records : []
    total.value = Number(data.total || 0)
    page.value = Number(data.page || page.value)
    size.value = Number(data.size || size.value)
  } catch (e) {
    console.error('Load visitor records failed:', e)
  } finally {
    loading.value = false
  }
}

const reload = () => {
  page.value = 1
  loadData()
}

const prevPage = () => {
  if (page.value <= 1) return
  page.value -= 1
  loadData()
}

const nextPage = () => {
  if (page.value >= totalPages.value) return
  page.value += 1
  loadData()
}

onMounted(loadData)
</script>
