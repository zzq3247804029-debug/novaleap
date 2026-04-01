<template>
  <div class="space-y-6">
    <div class="bg-admin-card rounded-lg p-6 shadow-sm border border-gray-100">
      <h2 class="text-lg font-medium mb-4 flex items-center">
        <Server class="w-5 h-5 mr-2 text-admin-muted" /> JVM 虚拟资源监控
      </h2>
      <div v-if="loading" class="animate-pulse flex space-x-4">
        <div class="flex-1 space-y-4 py-1">
          <div class="h-2 bg-slate-200 rounded"></div>
          <div class="space-y-3">
            <div class="grid grid-cols-3 gap-4">
              <div class="h-2 bg-slate-200 rounded col-span-2"></div>
              <div class="h-2 bg-slate-200 rounded col-span-1"></div>
            </div>
          </div>
        </div>
      </div>
      <div v-else-if="error" class="text-admin-danger bg-red-50 p-4 rounded-md text-sm border border-red-100 flex items-start">
        <AlertTriangle class="w-5 h-5 mr-2 shrink-0" />
        <div>
          <p class="font-medium">未能连接至后端监控探测口 /api/admin/system-monitor</p>
          <p class="mt-1 opacity-80">{{ error }}</p>
        </div>
      </div>
      <div v-else class="space-y-6">
        <div class="flex items-center justify-between text-sm">
          <span class="text-admin-muted font-medium">应用堆栈内存利用率</span>
          <span class="font-bold text-admin-text">{{ memoryPercentage }}%</span>
        </div>
        <div class="w-full bg-gray-100 rounded-full h-4 overflow-hidden shadow-inner">
          <div class="h-4 transition-all duration-500 ease-out" 
               :class="memoryPercentage > 85 ? 'bg-admin-danger' : memoryPercentage > 60 ? 'bg-amber-500' : 'bg-admin-success'" 
               :style="{ width: `${memoryPercentage}%` }"></div>
        </div>
        <div class="grid grid-cols-3 gap-4 border-t border-gray-100 pt-4 mt-4">
           <div class="text-center">
             <p class="text-xs text-admin-muted mb-1">当前占用刻度</p>
             <p class="text-lg font-bold">{{ monitorData.jvmUsedMemoryMB || 0 }} <span class="text-xs font-normal text-admin-muted">MB</span></p>
           </div>
           <div class="text-center border-l border-r border-gray-100">
             <p class="text-xs text-admin-muted mb-1">分配阀值大限</p>
             <p class="text-lg font-bold">{{ monitorData.jvmMaxMemoryMB || 0 }} <span class="text-xs font-normal text-admin-muted">MB</span></p>
           </div>
           <div class="text-center">
             <p class="text-xs text-admin-muted mb-1">活跃并行任务数</p>
             <p class="text-lg font-bold">{{ monitorData.currentActiveTasks || 0 }} <span class="text-xs font-normal text-admin-muted"></span></p>
           </div>
        </div>
      </div>
    </div>
    
    <div class="bg-admin-card rounded-lg p-6 shadow-sm border border-gray-100 h-64 flex items-center justify-center">
      <div class="text-center text-admin-muted">
        <Activity class="w-12 h-12 mx-auto mb-3 opacity-20" />
        <p>Redis 挂载节点压力与底层网络吞吐情况雷达探测 (预装)</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Server, Activity, AlertTriangle } from 'lucide-vue-next'
import { api } from '@/composables/useRequest'

const loading = ref(true)
const error = ref(null)
const monitorData = ref({})
let timer = null

const memoryPercentage = computed(() => {
  if (!monitorData.value.jvmMaxMemoryMB) return 0
  return Math.round((monitorData.value.jvmUsedMemoryMB / monitorData.value.jvmMaxMemoryMB) * 100)
})

const fetchMonitorData = async () => {
  try {
    const json = await api.get('/api/admin/system-monitor')
    if (json && json.code === 200) {
      monitorData.value = json.data
      error.value = null
    } else {
      throw new Error(json?.message || '抓取底层指标出错')
    }
  } catch (err) {
    error.value = err.message || '网络或授权异常'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchMonitorData()
  timer = setInterval(fetchMonitorData, 5000) // 5s 心跳拉取
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>
