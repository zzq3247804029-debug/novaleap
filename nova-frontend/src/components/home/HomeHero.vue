<template>
  <section class="hero-shell">
    <div class="hero-copy">
      <p class="hero-eyebrow">{{ eyebrow }}</p>

      <h1 class="hero-title">{{ title }}</h1>

      <p class="hero-subtitle">{{ subtitle }}</p>

      <div class="hero-meta">
        <span class="hero-chip">{{ greetingText }}</span>
        <span class="hero-chip">欢迎回来，{{ nickname }}</span>
      </div>

      <div class="hero-actions">
        <button
          type="button"
          class="btn-primary hero-btn-primary"
          @click="$emit('primary')"
        >
          开始我的成长路径
        </button>
        <button
          type="button"
          class="btn-ghost hero-btn-secondary"
          @click="$emit('secondary')"
        >
          请作者喝杯咖啡
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: 'NOVALEAP AI LEARNING PLATFORM',
  },
  title: {
    type: String,
    default: 'NovaLeap',
  },
  subtitle: {
    type: String,
    required: true,
  },
  greetingText: {
    type: String,
    required: true,
  },
  nickname: {
    type: String,
    default: '学习者',
  },
})

defineEmits(['primary', 'secondary'])
</script>

<style scoped>
.hero-shell {
  position: relative;
  min-height: calc(92vh - 132px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px 0 12px;
}

.hero-copy {
  max-width: 980px;
  text-align: center;
}

.hero-eyebrow,
.hero-title,
.hero-subtitle,
.hero-meta,
.hero-actions {
  opacity: 0;
  transform: translateY(18px);
  animation: heroIn 820ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.hero-title {
  animation-delay: 120ms;
}

.hero-subtitle {
  animation-delay: 240ms;
}

.hero-meta {
  animation-delay: 360ms;
}

.hero-actions {
  animation-delay: 480ms;
}

.hero-eyebrow {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.34em;
  color: var(--text-tertiary);
}

.hero-title {
  margin-top: 22px;
  margin-bottom: 10px;
  display: inline-block;
  font-size: clamp(72px, 15.5vw, 178px);
  line-height: 1.12;
  font-weight: 800;
  letter-spacing: -0.08em;
  color: transparent;
  background-image: linear-gradient(180deg, color-mix(in srgb, var(--text-primary) 92%, white), color-mix(in srgb, var(--primary) 42%, var(--text-primary)) 52%, var(--primary) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 0 0 28px rgba(var(--primary-rgb), 0.08);
  padding-bottom: 0.12em;
  padding-right: 0.05em;
  text-rendering: geometricPrecision;
}

.hero-subtitle {
  margin: 4px auto 0;
  max-width: 760px;
  font-size: clamp(18px, 2.2vw, 24px);
  line-height: 1.9;
  color: var(--text-secondary);
}

.hero-meta {
  margin-top: 24px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  border-radius: 999px;
  border: 1px solid var(--border-soft);
  background: var(--bg-soft);
  padding: 0 16px;
  font-size: 13px;
  color: var(--text-secondary);
  backdrop-filter: blur(18px);
}

.hero-actions {
  margin-top: 30px;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14px;
}

.hero-btn-primary,
.hero-btn-secondary {
  min-width: 210px;
  min-height: 52px;
  border-radius: 999px;
}

.hero-btn-primary {
  box-shadow: 0 18px 36px rgba(var(--primary-rgb), 0.24);
}

.hero-btn-secondary {
  background: var(--bg-soft);
}

@keyframes heroIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dark .hero-chip,
.dark .hero-btn-secondary {
  background: var(--bg-soft);
  border-color: var(--border-soft);
}

.dark .hero-title {
  background-image: linear-gradient(180deg, color-mix(in srgb, var(--text-primary) 92%, white), color-mix(in srgb, var(--primary) 58%, var(--text-primary)) 54%, var(--primary) 100%);
  text-shadow: 0 0 44px rgba(var(--primary-rgb), 0.16);
}

@media (max-width: 640px) {
  .hero-shell {
    min-height: auto;
    padding-top: 42px;
  }
}
</style>
