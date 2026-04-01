<template>
  <section class="hero-shell">
    <div class="pointer-events-none absolute inset-0">
      <div class="hero-glow hero-glow-left"></div>
      <div class="hero-glow hero-glow-right"></div>
    </div>

    <div class="relative grid gap-8 xl:grid-cols-[minmax(0,1fr)_280px] xl:items-end">
      <div>
        <div class="flex flex-wrap items-center gap-3">
          <span class="hero-tag">NovaLeap Learning Hub</span>
          <span class="hero-user">{{ nickname }}</span>
          <span v-if="showStreak" class="hero-streak" :class="{ 'hero-streak-muted': !signedToday }">
            <span>连续签到</span>
            <strong>{{ streakDays }}</strong>
            <span>天</span>
            <span>{{ signedToday ? '今天已完成' : '今天待完成' }}</span>
          </span>
        </div>

        <h1 class="mt-6 max-w-3xl text-[38px] font-semibold leading-[1.12] tracking-[-0.04em] text-text-primary sm:text-[46px] lg:text-[58px]">
          {{ greetingText }}
          <span class="block text-[0.9em] font-medium text-slate-500">让今天的成长继续发生。</span>
        </h1>

        <p class="mt-5 max-w-2xl text-base leading-8 text-text-secondary sm:text-[17px]">
          {{ greetingDesc }} 从下方入口选择一个模块，继续你的做题、记录、打磨与复盘节奏。
        </p>
      </div>

      <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-1">
        <article class="hero-panel">
          <p class="hero-panel-label">学习入口</p>
          <div class="hero-panel-value">{{ moduleCount }}</div>
          <p class="hero-panel-desc">完整保留现有功能模块，只重构首页呈现方式。</p>
        </article>
        <article class="hero-panel">
          <p class="hero-panel-label">在榜学员</p>
          <div class="hero-panel-value">{{ learnerCount }}</div>
          <p class="hero-panel-desc">{{ signedToday ? '今天也在保持节奏。' : '先签到，再开始今天的学习。' }}</p>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  greetingText: {
    type: String,
    required: true,
  },
  greetingDesc: {
    type: String,
    required: true,
  },
  nickname: {
    type: String,
    required: true,
  },
  showStreak: {
    type: Boolean,
    default: false,
  },
  streakDays: {
    type: Number,
    default: 0,
  },
  signedToday: {
    type: Boolean,
    default: false,
  },
  moduleCount: {
    type: Number,
    default: 0,
  },
  learnerCount: {
    type: Number,
    default: 0,
  },
})
</script>

<style scoped>
.hero-shell {
  position: relative;
  overflow: hidden;
  border-radius: 32px;
  border: 1px solid var(--border-soft);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.97), rgba(255, 255, 255, 0.92)),
    var(--bg-card);
  box-shadow: var(--shadow-soft);
  padding: 32px;
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  border: 1px solid rgba(109, 140, 255, 0.18);
  background: rgba(109, 140, 255, 0.08);
  color: var(--primary);
  padding: 8px 14px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero-user {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  border: 1px solid var(--border-soft);
  background: rgba(255, 255, 255, 0.84);
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.hero-streak {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: 999px;
  border: 1px solid rgba(109, 140, 255, 0.16);
  background: rgba(236, 241, 255, 0.9);
  padding: 8px 14px;
  font-size: 12px;
  color: var(--text-secondary);
}

.hero-streak strong {
  font-size: 20px;
  line-height: 1;
  color: var(--text-primary);
}

.hero-streak-muted {
  background: rgba(246, 247, 251, 0.92);
  border-color: rgba(203, 213, 225, 0.64);
}

.hero-panel {
  border-radius: 24px;
  border: 1px solid rgba(232, 236, 244, 0.9);
  background: rgba(250, 251, 255, 0.82);
  padding: 22px;
  backdrop-filter: blur(10px);
}

.hero-panel-label {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-muted);
}

.hero-panel-value {
  margin-top: 10px;
  font-size: 34px;
  font-weight: 700;
  line-height: 1;
  letter-spacing: -0.04em;
  color: var(--text-primary);
}

.hero-panel-desc {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.hero-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(64px);
  opacity: 0.9;
}

.hero-glow-left {
  top: -60px;
  left: -30px;
  width: 240px;
  height: 240px;
  background: rgba(109, 140, 255, 0.12);
}

.hero-glow-right {
  right: 0;
  bottom: -70px;
  width: 220px;
  height: 220px;
  background: rgba(196, 206, 255, 0.34);
}

@media (max-width: 640px) {
  .hero-shell {
    padding: 24px;
    border-radius: 28px;
  }

  .hero-streak {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }
}

.dark .hero-shell {
  background: rgba(15, 23, 42, 0.88);
  border-color: rgba(51, 65, 85, 0.8);
}

.dark .hero-user,
.dark .hero-panel {
  background: rgba(17, 24, 39, 0.8);
  border-color: rgba(51, 65, 85, 0.8);
}

.dark .hero-streak {
  background: rgba(30, 41, 59, 0.82);
}
</style>
