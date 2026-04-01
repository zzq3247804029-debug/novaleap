<template>
  <svg
    :class="svgClass"
    :style="svgStyle"
    viewBox="0 0 24 24"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <defs>
      <linearGradient :id="gradientId" x1="2" y1="2" x2="22" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" :stop-color="gradientColors.from" />
        <stop offset="100%" :stop-color="gradientColors.to" />
      </linearGradient>
    </defs>
    <rect x="2" y="2" width="20" height="20" rx="6" :fill="`url(#${gradientId})`" opacity="0.14" />
    <path
      v-for="(d, idx) in iconPaths"
      :key="idx"
      :d="d"
      stroke="currentColor"
      stroke-width="1.85"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
  </svg>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, default: 'home' },
  size: { type: Number, default: 20 },
  className: { type: String, default: '' },
})

const ICON_PATHS = {
  home: ['M3 10.5L12 3l9 7.5', 'M5 9.5V20h14V9.5', 'M9.5 20v-5h5V20'],
  questions: ['M5 4.5h9a3 3 0 013 3V19.5H8a3 3 0 00-3 3V4.5z', 'M17 7h2v15h-2', 'M8.5 9h5', 'M8.5 12h5', 'M8.5 15h4'],
  notes: ['M7 3.5h7l4 4V20.5H7z', 'M14 3.5V8h4', 'M9 12h6', 'M9 15h6', 'M9 18h4'],
  resume: ['M7 3.5h10v17H7z', 'M10 8h4', 'M9 12h6', 'M9 15h6', 'M9 18h5'],
  coach: ['M12 4.2l8.2 4.4-8.2 4.4L3.8 8.6 12 4.2z', 'M6.2 10.4V15c0 1.3 2.6 2.8 5.8 2.8s5.8-1.5 5.8-2.8v-4.6', 'M20.2 8.6V14'],
  wishes: ['M12 3.5v3', 'M12 17.5v3', 'M4.5 12h3', 'M16.5 12h3', 'M7.2 7.2l2.2 2.2', 'M14.6 14.6l2.2 2.2', 'M16.8 7.2l-2.2 2.2', 'M9.4 14.6l-2.2 2.2'],
  game: ['M8 6.5h8', 'M6.5 10h11', 'M8 13.5h8', 'M9 17h6', 'M4.5 21h15', 'M7 3.8h10'],
  me: ['M12 12a4 4 0 100-8 4 4 0 000 8z', 'M4 20.5a8 8 0 0116 0'],
}

const ICON_GRADIENTS = {
  home: { from: '#38bdf8', to: '#6366f1' },
  questions: { from: '#4f8cff', to: '#6ec5ff' },
  notes: { from: '#fb7185', to: '#a78bfa' },
  resume: { from: '#94a3b8', to: '#cbd5e1' },
  coach: { from: '#60a5fa', to: '#c084fc' },
  wishes: { from: '#f59e0b', to: '#fb7185' },
  game: { from: '#f43f5e', to: '#8b5cf6' },
  me: { from: '#22c55e', to: '#3b82f6' },
}

const iconPaths = computed(() => ICON_PATHS[props.name] || ICON_PATHS.home)
const gradientColors = computed(() => ICON_GRADIENTS[props.name] || ICON_GRADIENTS.home)
const gradientId = `app-icon-grad-${Math.random().toString(36).slice(2, 10)}`
const svgClass = computed(() => props.className)
const svgStyle = computed(() => ({ width: `${props.size}px`, height: `${props.size}px` }))
</script>
