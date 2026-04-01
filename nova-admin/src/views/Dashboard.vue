<template>
  <div class="space-y-5">
    <section class="grid grid-cols-2 lg:grid-cols-4 2xl:grid-cols-6 gap-3">
      <div
        v-for="stat in stats"
        :key="stat.label"
        class="bg-admin-card rounded-lg p-4 shadow-sm border border-gray-100 min-h-[108px]"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <p class="text-[11px] uppercase tracking-wider text-admin-muted truncate">{{ stat.label }}</p>
            <p class="mt-1.5 text-2xl font-semibold text-admin-text">{{ stat.value }}</p>
            <p v-if="stat.subText" class="mt-1 text-[11px] text-admin-muted">{{ stat.subText }}</p>
          </div>
          <div :class="`p-2 rounded-lg ${stat.iconBg} ${stat.iconColor}`">
            <component :is="stat.icon" class="w-4 h-4" />
          </div>
        </div>
      </div>
    </section>

    <section class="grid grid-cols-1 xl:grid-cols-3 gap-4">
      <div class="xl:col-span-2 bg-admin-card rounded-lg p-4 shadow-sm border border-gray-100">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-sm font-semibold text-admin-text flex items-center gap-2">
            <MapPinned class="w-4 h-4 text-indigo-500" />
            地区访问热度（按 IP 记录）
          </h3>
          <span class="text-xs text-admin-muted">实时累计</span>
        </div>

        <div v-if="!geo.topCities.length" class="text-sm text-admin-muted py-6 text-center">
          暂无地区访问数据
        </div>

        <div v-else class="space-y-2.5">
          <div
            v-for="item in geo.topCities"
            :key="`city-${item.name}`"
            class="grid grid-cols-[1fr_auto] items-center gap-2"
          >
            <div class="min-w-0">
              <div class="flex items-center justify-between mb-1">
                <span class="text-xs text-admin-text truncate">{{ item.name }}</span>
                <span class="text-xs text-admin-muted">{{ item.count }}</span>
              </div>
              <div class="h-1.5 rounded-full bg-gray-100 overflow-hidden">
                <div
                  class="h-full rounded-full bg-gradient-to-r from-indigo-500 to-cyan-500"
                  :style="{ width: `${barWidth(item.count, maxCityCount)}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <div class="mt-5">
          <h4 class="text-xs font-semibold text-admin-muted mb-2">高频省区</h4>
          <div class="flex flex-wrap gap-2">
            <span
              v-for="item in geo.topRegions"
              :key="`region-${item.name}`"
              class="text-xs px-2.5 py-1 rounded-full bg-indigo-50 text-indigo-700"
            >
              {{ item.name }} · {{ item.count }}
            </span>
            <span v-if="!geo.topRegions.length" class="text-xs text-admin-muted">暂无</span>
          </div>
        </div>
      </div>

      <div class="bg-admin-card rounded-lg p-4 shadow-sm border border-gray-100">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-sm font-semibold text-admin-text flex items-center gap-2">
            <Clock3 class="w-4 h-4 text-emerald-500" />
            最近访问
          </h3>
          <span class="text-xs text-admin-muted">脱敏 IP</span>
        </div>

        <div v-if="!geo.recentGeoVisits.length" class="text-sm text-admin-muted py-6 text-center">
          暂无访问记录
        </div>

        <div v-else class="space-y-2 max-h-[330px] overflow-auto pr-1">
          <div
            v-for="(row, idx) in geo.recentGeoVisits"
            :key="`${row.time}-${idx}`"
            class="rounded-md border border-gray-100 p-2.5"
          >
            <div class="flex items-center justify-between gap-2">
              <span class="text-[11px] text-admin-text truncate">{{ row.region }} / {{ row.city }}</span>
              <span class="text-[11px] text-admin-muted">{{ row.actorType === 'user' ? '注册' : '游客' }}</span>
            </div>
            <div class="mt-1 text-[11px] text-admin-muted truncate">{{ row.ip }} · {{ row.path }}</div>
            <div class="mt-0.5 text-[10px] text-admin-muted">{{ row.time }}</div>
          </div>
        </div>
      </div>
    </section>

    <section class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <router-link
        to="/users"
        class="bg-admin-card rounded-lg p-5 shadow-sm border border-gray-100 hover:shadow-md transition-shadow group cursor-pointer"
      >
        <div class="flex items-center gap-4">
          <div class="p-2.5 rounded-lg bg-blue-50 text-blue-500 group-hover:bg-blue-100 transition-colors">
            <Users class="w-5 h-5" />
          </div>
          <div>
            <h3 class="font-medium text-admin-text">用户管理</h3>
            <p class="text-sm text-admin-muted mt-0.5">维护注册用户与权限</p>
          </div>
        </div>
      </router-link>

      <router-link
        to="/questions"
        class="bg-admin-card rounded-lg p-5 shadow-sm border border-gray-100 hover:shadow-md transition-shadow group cursor-pointer"
      >
        <div class="flex items-center gap-4">
          <div class="p-2.5 rounded-lg bg-indigo-50 text-indigo-500 group-hover:bg-indigo-100 transition-colors">
            <BookOpen class="w-5 h-5" />
          </div>
          <div>
            <h3 class="font-medium text-admin-text">题库管理</h3>
            <p class="text-sm text-admin-muted mt-0.5">管理题目内容和分类</p>
          </div>
        </div>
      </router-link>

      <router-link
        to="/wishes"
        class="bg-admin-card rounded-lg p-5 shadow-sm border border-gray-100 hover:shadow-md transition-shadow group cursor-pointer"
      >
        <div class="flex items-center gap-4">
          <div class="p-2.5 rounded-lg bg-amber-50 text-amber-500 group-hover:bg-amber-100 transition-colors">
            <MessageSquare class="w-5 h-5" />
          </div>
          <div>
            <h3 class="font-medium text-admin-text">星愿审核</h3>
            <p class="text-sm text-admin-muted mt-0.5">审核用户提交内容</p>
          </div>
        </div>
      </router-link>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  Users,
  BookOpen,
  Sparkles,
  MessageSquare,
  UserRoundCheck,
  UserRoundX,
  Eye,
  Activity,
  MousePointerClick,
  MapPinned,
  Clock3,
} from 'lucide-vue-next'
import { getDashboard } from '@/api/admin'

const stats = ref([
  { label: '注册用户', value: '-', icon: Users, iconBg: 'bg-blue-50', iconColor: 'text-blue-500' },
  { label: '游客 PV', value: '-', icon: Eye, iconBg: 'bg-cyan-50', iconColor: 'text-cyan-500' },
  { label: '游客 UV', value: '-', icon: UserRoundX, iconBg: 'bg-sky-50', iconColor: 'text-sky-500' },
  { label: '注册 PV', value: '-', icon: MousePointerClick, iconBg: 'bg-emerald-50', iconColor: 'text-emerald-500' },
  { label: '注册 UV', value: '-', icon: UserRoundCheck, iconBg: 'bg-green-50', iconColor: 'text-green-500' },
  { label: '今日游客 PV', value: '-', icon: Activity, iconBg: 'bg-teal-50', iconColor: 'text-teal-500', subText: '当日统计' },
  { label: '今日注册 PV', value: '-', icon: Activity, iconBg: 'bg-lime-50', iconColor: 'text-lime-600', subText: '当日统计' },
  { label: '题库总量', value: '-', icon: BookOpen, iconBg: 'bg-indigo-50', iconColor: 'text-indigo-500' },
  { label: '手记总量', value: '-', icon: BookOpen, iconBg: 'bg-violet-50', iconColor: 'text-violet-500' },
  { label: 'AI 调用', value: '-', icon: Sparkles, iconBg: 'bg-purple-50', iconColor: 'text-purple-500' },
  { label: '星愿总量', value: '-', icon: MessageSquare, iconBg: 'bg-amber-50', iconColor: 'text-amber-500' },
])

const geo = ref({
  topCities: [],
  topRegions: [],
  recentGeoVisits: [],
})

const toDisplay = (val) => String(Number(val || 0))

const maxCityCount = computed(() => {
  const list = geo.value.topCities || []
  if (!list.length) return 1
  return Math.max(...list.map((item) => Number(item.count || 0)), 1)
})

const barWidth = (count, max) => {
  const value = Number(count || 0)
  const upper = Number(max || 1)
  return Math.max(6, Math.round((value / upper) * 100))
}

onMounted(async () => {
  try {
    const res = await getDashboard()
    if (res.code !== 200) return

    const d = res.data || {}
    stats.value[0].value = toDisplay(d.registeredUserCount ?? d.userCount)
    stats.value[1].value = toDisplay(d.guestPv)
    stats.value[2].value = toDisplay(d.guestUv)
    stats.value[3].value = toDisplay(d.userPv)
    stats.value[4].value = toDisplay(d.userUv)
    stats.value[5].value = toDisplay(d.guestPvToday)
    stats.value[6].value = toDisplay(d.userPvToday)
    stats.value[7].value = toDisplay(d.questionCount)
    stats.value[8].value = toDisplay(d.noteCount)
    stats.value[9].value = toDisplay(d.aiCallCount)
    stats.value[10].value = toDisplay(d.wishCount)

    geo.value.topCities = Array.isArray(d.topCities) ? d.topCities : []
    geo.value.topRegions = Array.isArray(d.topRegions) ? d.topRegions : []
    geo.value.recentGeoVisits = Array.isArray(d.recentGeoVisits) ? d.recentGeoVisits : []
  } catch (e) {
    console.error('Dashboard load failed:', e)
  }
})
</script>
