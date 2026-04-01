<template>
  <!-- 背景遮罩 -->
  <transition
    enter-active-class="transition-opacity duration-300 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition-opacity duration-200 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div 
      v-if="visible" 
      class="fixed inset-0 z-40 bg-black/20 backdrop-blur-sm"
      @click="handleClose"
    ></div>
  </transition>

  <!-- 抽屉本体 -->
  <transition
    enter-active-class="transition-transform duration-300 cubic-bezier(0.16, 1, 0.3, 1)"
    enter-from-class="translate-x-full shadow-none"
    enter-to-class="translate-x-0 shadow-[-20px_0_40px_rgba(0,0,0,0.1)]"
    leave-active-class="transition-transform duration-200 ease-in"
    leave-from-class="translate-x-0 shadow-[-20px_0_40px_rgba(0,0,0,0.1)]"
    leave-to-class="translate-x-full shadow-none"
  >
    <div 
      v-if="visible"
      class="fixed top-0 right-0 z-50 w-full max-w-md h-full bg-white/90 backdrop-blur-xl border-l border-white/40 flex flex-col pt-safe px-6 pb-safe"
      :class="customClass"
    >
      <!-- 头部：标题与关闭按钮 -->
      <div class="flex items-center justify-between py-5 border-b border-black/5 shrink-0">
        <h3 class="text-xl font-display font-semibold text-text-primary flex items-center gap-2">
          <!-- 渐变 AI 动效微图标 -->
          <svg class="h-6 w-6 text-ai-from animate-pulse" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
          {{ title }}
        </h3>
        <button 
          @click="handleClose"
          class="p-2 -mr-2 text-text-tertiary hover:text-text-primary hover:bg-black/5 rounded-full transition-colors"
        >
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- 内容区滚动区域 -->
      <div class="flex-1 overflow-y-auto py-6 pr-2 -mr-2 custom-scrollbar">
        <slot></slot>
      </div>

      <!-- 底部固定区域（如果有内容的话） -->
      <div v-if="$slots.footer" class="shrink-0 py-4 border-t border-black/5">
        <slot name="footer"></slot>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: 'AI 洞察' },
  customClass: { type: String, default: '' },
})

const emit = defineEmits(['update:visible', 'close'])

const handleClose = () => {
  emit('update:visible', false)
  emit('close')
}

// 阻止底层滚动
watch(() => props.visible, (val) => {
  if (val) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

onUnmounted(() => {
  document.body.style.overflow = ''
})
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.1);
  border-radius: 4px;
}
.pt-safe { padding-top: max(env(safe-area-inset-top), 1rem); }
.pb-safe { padding-bottom: max(env(safe-area-inset-bottom), 1rem); }
</style>
