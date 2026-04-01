<template>
  <div class="relative overflow-hidden rounded-2xl border border-sky-200/70 bg-white/88 p-5 group transition-all duration-300 hover:shadow-lg hover:border-sky-300/80">
    <!-- 左侧光晕装饰条 -->
    <div class="absolute left-0 top-0 bottom-0 w-1 bg-gradient-to-b from-sky-400 to-emerald-400"></div>
    
    <!-- 背景光效圆斑 -->
    <div class="absolute -right-10 -top-10 w-36 h-36 bg-sky-300 rounded-full blur-[65px] opacity-18 group-hover:opacity-28 transition-opacity duration-500"></div>
    <div class="absolute -left-8 -bottom-12 w-40 h-40 bg-emerald-200 rounded-full blur-[70px] opacity-20 group-hover:opacity-30 transition-opacity duration-500"></div>

    <div class="relative z-10">
      <div class="flex items-center gap-2 mb-3">
        <svg class="w-5 h-5 text-sky-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
        </svg>
        <span class="text-xs font-semibold uppercase tracking-wider text-sky-600">AI 摘要</span>
      </div>
      <div class="text-slate-700 text-sm leading-relaxed whitespace-pre-wrap">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
// AI 摘要高光块组件
</script>
