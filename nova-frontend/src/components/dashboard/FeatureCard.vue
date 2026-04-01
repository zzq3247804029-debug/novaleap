<template>
  <RouterLink :to="item.path" class="feature-card group">
    <div class="flex items-start justify-between gap-4">
      <div :class="['feature-icon', item.iconBg]">
        <AppIcon :name="item.icon" :size="22" class-name="text-slate-700" />
      </div>
      <span class="feature-action">
        进入
        <svg class="h-4 w-4 transition-transform duration-200 group-hover:translate-x-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" d="M9 5l7 7-7 7" />
        </svg>
      </span>
    </div>

    <div class="mt-9">
      <h3 class="text-[22px] font-semibold tracking-tight text-text-primary">{{ item.name }}</h3>
      <p class="mt-3 text-sm leading-7 text-text-secondary">{{ item.desc }}</p>
    </div>

    <div class="mt-auto pt-8">
      <div class="feature-foot">
        <span>开始使用</span>
        <span class="feature-foot-dot"></span>
      </div>
    </div>
  </RouterLink>
</template>

<script setup>
import AppIcon from '@/components/common/AppIcon.vue'

defineProps({
  item: {
    type: Object,
    required: true,
  },
})
</script>

<style scoped>
.feature-card {
  min-height: 244px;
  display: flex;
  flex-direction: column;
  padding: 28px;
  border-radius: 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-soft);
  box-shadow: var(--shadow-soft);
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.feature-card:hover {
  transform: translateY(-6px);
  border-color: rgba(109, 140, 255, 0.22);
  box-shadow: 0 18px 46px rgba(31, 41, 55, 0.1);
}

.feature-icon {
  width: 58px;
  height: 58px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.feature-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: 999px;
  background: rgba(109, 140, 255, 0.08);
  color: var(--primary);
  padding: 8px 12px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.feature-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(246, 247, 251, 0.96), rgba(241, 244, 250, 0.96));
  border: 1px solid rgba(232, 236, 244, 0.92);
  padding: 14px 16px;
  font-size: 13px;
  color: var(--text-secondary);
}

.feature-foot-dot {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: rgba(109, 140, 255, 0.5);
  box-shadow: 0 0 0 6px rgba(109, 140, 255, 0.08);
}

.icon-glow-cyan { background: linear-gradient(145deg, rgba(160, 220, 247, 0.5), rgba(224, 241, 255, 0.95)); }
.icon-glow-violet { background: linear-gradient(145deg, rgba(207, 214, 255, 0.6), rgba(239, 241, 255, 0.95)); }
.icon-glow-slate { background: linear-gradient(145deg, rgba(225, 230, 239, 0.96), rgba(247, 249, 252, 0.96)); }
.icon-glow-blue { background: linear-gradient(145deg, rgba(195, 214, 255, 0.62), rgba(232, 240, 255, 0.95)); }
.icon-glow-amber { background: linear-gradient(145deg, rgba(248, 226, 192, 0.65), rgba(253, 245, 227, 0.95)); }
.icon-glow-rose { background: linear-gradient(145deg, rgba(249, 212, 223, 0.64), rgba(254, 240, 245, 0.95)); }

.dark .feature-card {
  background: rgba(17, 24, 39, 0.88);
  border-color: rgba(71, 85, 105, 0.5);
}

.dark .feature-foot {
  background: rgba(15, 23, 42, 0.78);
  border-color: rgba(51, 65, 85, 0.72);
}
</style>
