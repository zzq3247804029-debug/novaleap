<template>
  <footer class="footer-landing">
    <section class="outro-stage relative rounded-[42px] px-6 pb-7 pt-7 sm:px-8 lg:px-10 lg:pb-8 lg:pt-9">
      <div class="outro-glow outro-glow-a"></div>
      <div class="outro-glow outro-glow-b"></div>

      <div class="relative z-[1] max-w-[760px]">
        <p class="outro-kicker text-[11px] font-semibold uppercase tracking-[0.28em]">
          NOVALEAP AI LEARNING PLATFORM
        </p>
        <h3 class="outro-title mt-4 text-[clamp(30px,4vw,52px)] font-semibold leading-[1.06] tracking-[-0.04em]">
          让成长更有节奏，也更有回响。
        </h3>
        <p class="outro-copy mt-4 max-w-[680px] text-[15px] leading-8 sm:text-[16px]">
          从扰光题库、灵感手记到简历工坊与知识陪练，NovaLeap 把训练、沉淀、表达、反馈与节奏切换，
          串成一条更稳定也更长期的学习路径。
        </p>
      </div>

      <div class="outro-wordmark-wrap relative z-[1] mt-8">
        <div class="outro-wordmark">NovaLeap</div>
      </div>
    </section>

    <section class="footer-band">
      <div class="mx-auto max-w-[1260px] px-6 py-6 sm:px-8 lg:px-10 lg:py-7">
        <div class="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
          <div class="space-y-4">
            <div class="brand-row">
              <span class="brand-title">NovaLeap</span>
              <span class="brand-slogan">让成长更有节奏，让学习更轻松</span>
            </div>
            <nav class="footer-links">
              <RouterLink
                v-for="item in footerLinks"
                :key="item.path"
                :to="item.path"
                class="footer-link"
              >
                {{ item.name }}
              </RouterLink>
            </nav>
          </div>

          <div class="public-card">
            <p class="public-title">关注公众号</p>
            <img
              :src="supportQr"
              alt="NovaLeap 公众号二维码"
              class="public-qr"
            />
            <p class="public-desc">获取更新、学习内容与活动通知</p>
          </div>
        </div>

        <div class="footer-copy">
          <p>© 2026 NovaLeap. All rights reserved.</p>
          <p>本网站为 NovaLeap 原创设计与内容，未经授权禁止复制或转载。</p>
          <p>AI Learning Platform · Train · Reflect · Express · Grow</p>
        </div>
      </div>
    </section>
  </footer>
</template>

<script setup>
import supportQr from '@/assets/support-qr.jpg'

const footerLinks = [
  { name: '扰光题库', path: '/questions' },
  { name: '灵感手记', path: '/notes' },
  { name: '简历工坊', path: '/resume' },
  { name: '知识陪练', path: '/coach' },
  { name: '星愿墙', path: '/wishes' },
  { name: '休闲时刻', path: '/game' },
]
</script>

<style scoped>
.footer-landing {
  position: relative;
}

.outro-stage {
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 28%, var(--module-glow-c), transparent 32%),
    radial-gradient(circle at 72% 78%, var(--module-glow-b), transparent 30%),
    radial-gradient(circle at 84% 26%, var(--module-glow-a), transparent 24%),
    var(--surface-panel);
}

.outro-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(86px);
  opacity: 0.72;
}

.outro-glow-a {
  left: -4%;
  top: 6%;
  width: 280px;
  height: 280px;
  background: var(--module-glow-a);
}

.outro-glow-b {
  right: 2%;
  top: 12%;
  width: 260px;
  height: 260px;
  background: var(--module-glow-c);
}

.outro-wordmark-wrap {
  padding-right: 0;
  padding-bottom: 12px;
}

.outro-kicker {
  color: var(--text-tertiary);
}

.outro-title {
  color: var(--text-primary);
}

.outro-copy {
  color: var(--text-secondary);
}

.outro-wordmark {
  font-size: clamp(64px, 12.4vw, 154px);
  font-weight: 700;
  line-height: 1.02;
  letter-spacing: -0.055em;
  color: var(--brand-wordmark);
  text-shadow: 0 14px 30px rgba(var(--primary-rgb), 0.12);
  white-space: nowrap;
}

.footer-band {
  margin-inline: calc(50% - 50vw);
  margin-top: -2px;
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--bg-page) 14%, #23232d) 0%,
    #20212a 100%
  );
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  color: rgba(248, 245, 255, 0.94);
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: #f6f3ff;
}

.brand-slogan {
  font-size: 14px;
  color: rgba(227, 220, 239, 0.7);
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 20px;
  color: rgba(227, 220, 239, 0.74);
  font-size: 14px;
}

.footer-link {
  transition: color 0.24s ease;
}

.footer-link:hover {
  color: #ffffff;
}

.public-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 182px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.04);
  padding: 10px 10px 8px;
}

.public-title {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  color: rgba(248, 245, 255, 0.92);
  letter-spacing: 0.08em;
}

.public-qr {
  width: 144px;
  height: 144px;
  margin-top: 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(255, 255, 255, 0.18);
  object-fit: cover;
}

.public-desc {
  margin: 8px 0 0;
  text-align: center;
  font-size: 12px;
  line-height: 1.45;
  color: rgba(221, 214, 235, 0.74);
}

.footer-copy {
  margin-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  padding-top: 14px;
  display: grid;
  gap: 5px;
  color: rgba(207, 200, 224, 0.72);
  font-size: 13px;
}

.dark .outro-stage {
  background:
    radial-gradient(circle at 18% 28%, var(--module-glow-c), transparent 40%),
    radial-gradient(circle at 72% 78%, var(--module-glow-a), transparent 36%),
    var(--surface-panel);
  border: 1px solid var(--border-soft);
}

.dark .outro-wordmark {
  color: var(--brand-wordmark);
  text-shadow: 0 0 32px rgba(var(--primary-rgb), 0.08);
}

.dark .public-card {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.12);
}

.dark .public-qr {
  opacity: 0.95;
  filter: brightness(0.96);
}

@media (max-width: 768px) {
  .outro-stage {
    border-radius: 34px;
    padding-bottom: 18px;
  }

  .outro-wordmark {
    font-size: clamp(52px, 18vw, 104px);
    letter-spacing: -0.045em;
  }

  .public-card {
    width: 170px;
  }

  .public-qr {
    width: 134px;
    height: 134px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .footer-link {
    transition: none;
  }
}
</style>
