<template>
  <div class="app-layout relative h-screen w-full overflow-hidden bg-bg-base">
    <div class="pointer-events-none absolute inset-0 z-0 overflow-hidden">
      <div class="layout-glow layout-glow-a"></div>
      <div class="layout-glow layout-glow-b"></div>
      <div class="layout-glow layout-glow-c"></div>
    </div>

    <header class="fixed left-1/2 top-4 z-40 w-[min(1240px,calc(100%-16px))] -translate-x-1/2 sm:top-5 sm:w-[min(1240px,calc(100%-30px))]">
      <div class="floating-nav">
        <button type="button" class="brand-pill" @click="navigateNav(navItems[0])">
          <img src="@/assets/logo.png" alt="logo" class="h-7 w-auto object-contain" />
          <div class="hidden min-w-0 text-left sm:block">
            <p class="text-sm font-semibold tracking-[-0.02em] text-text-primary">NovaLeap</p>
            <p class="text-[11px] text-text-muted">知跃</p>
          </div>
        </button>

        <nav class="nav-center hidden xl:flex items-center">
            <div
              v-for="item in navItems"
              :key="`desktop-${item.name}-${item.path}`"
              class="relative group h-full flex items-center"
            >
              <button
                type="button"
                class="nav-item-pill flex items-center gap-2"
                :class="isNavActive(item) ? 'nav-item-pill-active' : 'nav-item-pill-idle'"
                @click="navigateNav(item)"
              >
                <svg v-if="item.icon && NovaIcons[item.icon]" class="w-4 h-4 opacity-80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" v-html="NovaIcons[item.icon]"></svg>
                {{ item.name }}
              </button>
          </div>
        </nav>

        <div class="nav-actions hidden items-center gap-2 sm:flex">
          <button
            type="button"
            class="icon-btn"
            :title="uiStore.isDarkMode ? '切换到浅色模式' : '切换到深色模式'"
            @click="uiStore.toggleDarkMode"
          >
            <svg v-if="uiStore.isDarkMode" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="overflow: visible;">
              <circle cx="12" cy="12" r="5" />
              <path d="M12 1v2m0 18v2M4.22 4.22l1.42 1.42m12.72 12.72l1.42 1.42M1 12h2m18 0h2M4.22 19.78l1.42-1.42m12.72-12.72l1.42-1.42" />
            </svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="overflow: visible;">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
            </svg>
          </button>

          <button type="button" class="profile-pill" @click="goMe">
            <span v-if="isEmoji(displayAvatar)" class="profile-avatar">{{ displayAvatar }}</span>
            <img v-else :src="displayAvatar" alt="avatar" class="profile-avatar profile-avatar-img" />
            <div class="profile-copy">
              <span class="profile-label">{{ authStore.isLoggedIn ? '我的' : '登录' }}</span>
              <span class="profile-name hidden text-[11px] text-text-muted lg:block" :title="displayNicknameRaw">
                {{ displayNickname }}
              </span>
            </div>
          </button>

          <button
            v-if="authStore.isLoggedIn"
            type="button"
            class="icon-btn icon-btn-danger"
            title="退出登录"
            @click="handleLogout"
          >
            <svg class="h-[18px] w-[18px]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.9" d="M17 16l4-4m0 0l-4-4m4 4H8m5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
          </button>
        </div>

        <button type="button" class="icon-btn nav-mobile-trigger" @click="mobileMenuOpen = true">
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.9" d="M4 6h16M4 12h16m-7 6h7" />
          </svg>
        </button>
      </div>
    </header>

    <div v-if="mobileMenuOpen" class="fixed inset-0 z-40 bg-slate-900/18 backdrop-blur-sm sm:hidden" @click="mobileMenuOpen = false"></div>

    <aside class="mobile-drawer sm:hidden" :class="mobileMenuOpen ? 'translate-x-0' : 'translate-x-full'">
      <div class="flex items-center justify-between px-5 py-5">
        <div>
          <p class="text-lg font-semibold tracking-[-0.03em] text-text-primary">导航</p>
          <p class="mt-1 text-xs uppercase tracking-[0.2em] text-text-muted">NovaLeap</p>
        </div>
        <button type="button" class="icon-btn" @click="mobileMenuOpen = false">
          <svg class="h-[18px] w-[18px]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.9" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <nav class="flex-1 space-y-2 overflow-y-auto px-4 pb-5">
        <button
          v-for="item in navItems"
          :key="`mobile-${item.name}-${item.path}`"
          type="button"
          class="mobile-nav-item"
          :class="isNavActive(item) ? 'mobile-nav-item-active' : 'mobile-nav-item-idle'"
          @click="navigateNav(item)"
        >
          <span>{{ item.name }}</span>
          <svg class="h-4 w-4 opacity-55" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.9" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </nav>

      <div class="space-y-2 border-t border-border-subtle px-4 py-4">
        <button type="button" class="mobile-action-btn" @click="goMe">
          <span>进入我的</span>
          <span class="text-text-muted">{{ displayNickname }}</span>
        </button>
        <button type="button" class="mobile-action-btn" @click="uiStore.toggleDarkMode">
          <span>{{ uiStore.isDarkMode ? '浅色模式' : '深色模式' }}</span>
          <span class="text-text-muted">{{ uiStore.isDarkMode ? 'Light' : 'Dark' }}</span>
        </button>
        <button v-if="authStore.isLoggedIn" type="button" class="mobile-logout-btn" @click="handleMobileLogout">退出登录</button>
      </div>
    </aside>

    <div class="relative z-10 flex h-full flex-col">
      <div class="h-[78px] shrink-0 sm:h-[92px]"></div>
      <main class="min-h-0 flex-1 overflow-hidden">
        <router-view v-slot="{ Component }">
          <transition
            enter-active-class="transition-opacity duration-200"
            enter-from-class="opacity-0"
            enter-to-class="opacity-100"
            leave-active-class="transition-opacity duration-150"
            leave-from-class="opacity-100"
            leave-to-class="opacity-0"
            mode="out-in"
          >
            <keep-alive :include="['Home', 'QuestionBank', 'Me']">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { useAuth } from '@/composables/useAuth'
import { api } from '@/composables/useRequest'

// 极简 SVG 图标库实现：解决依赖缺失问题
const NovaIcons = {
  Home: '<path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>',
  Trophy: '<path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/>',
  Edit3: '<path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>',
  FileText: '<path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4"/><path d="M10 9H8"/><path d="M16 13H8"/><path d="M16 17H8"/>',
  UserCheck: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><polyline points="16 11 18 13 22 9"/>',
  Star: '<polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>',
  Gamepad2: '<line x1="6" x2="10" y1="11" y2="11"/><line x1="8" x2="8" y1="9" y2="13"/><rect width="20" height="12" x="2" y="6" rx="2"/><path d="M15 12h.01"/><path d="M18 10h.01"/>'
}

const route = useRoute()
const router = useRouter()
const uiStore = useUiStore()
const authStore = useAuthStore()
const { logout } = useAuth()
const mobileMenuOpen = ref(false)
const activeHomeSection = ref('hero')

const navItems = [
  { name: '首页', path: '/', icon: 'Home', scrollTarget: 'hero', activeSection: 'hero' },
  { name: '拾光题库', path: '/questions', icon: 'FileText' },
  { name: '灵感手记', path: '/notes', icon: 'Edit3' },
  { name: '简历工坊', path: '/resume', icon: 'FileText' },
  { name: '知跃陪练', path: '/coach', icon: 'UserCheck' },
  { name: '星愿墙', path: '/wishes', icon: 'Star' },
  { name: '休闲时刻', path: '/game', icon: 'Gamepad2' },
  { name: '排行榜', path: '/leaderboard', icon: 'Trophy' },
]

const isHomeRoute = computed(() => route.path === '/')

const guestIdPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
const isGuest = computed(() => {
  if (authStore.isGuest) return true
  const nickname = String(authStore.nickname || '')
  const username = String(authStore.user?.username || authStore.username || '')
  return nickname.startsWith('游客') || guestIdPattern.test(username)
})

const displayAvatar = computed(() => {
  const avatar = String(authStore.avatar || '').trim()
  if (!avatar) return '🥳'
  if (/^https?:\/\//i.test(avatar)) return avatar
  return avatar
})

const displayNicknameRaw = computed(() => {
  if (isGuest.value) return 'Nova 访客'
  return authStore.nickname || (authStore.isLoggedIn ? 'Nova 用户' : '立即登录')
})

const displayNickname = computed(() => {
  const raw = String(displayNicknameRaw.value || '').trim()
  return raw.length > 14 ? `${raw.slice(0, 12)}…` : raw
})

const isEmoji = (val) => typeof val === 'string' && !val.startsWith('http')

const emitHomeScroll = (sectionId) => {
  window.dispatchEvent(new CustomEvent('nova-home-scroll', { detail: { section: sectionId } }))
}

const emitHomeScrollWithRetry = (sectionId) => {
  const delays = [0, 80, 220, 420]
  delays.forEach((delay) => {
    window.setTimeout(() => emitHomeScroll(sectionId), delay)
  })
}

const navigateNav = async (item) => {
  mobileMenuOpen.value = false

  if (isHomeRoute.value && item.scrollTarget) {
    activeHomeSection.value = item.activeSection || item.scrollTarget
    emitHomeScrollWithRetry(item.scrollTarget)
    return
  }

  if (route.path !== item.path) {
    await router.push(item.path)
  }

  if (item.path === '/' && item.scrollTarget) {
    activeHomeSection.value = item.activeSection || item.scrollTarget
    emitHomeScrollWithRetry(item.scrollTarget)
  }
}

const isNavActive = (item) => {
  if (isHomeRoute.value && item.scrollTarget) {
    const activeKey = item.activeSection || item.scrollTarget
    return activeHomeSection.value === activeKey
  }
  if (item.path === '/') return route.path === '/'
  return route.path.startsWith(item.path)
}

const goMe = () => {
  mobileMenuOpen.value = false
  router.push('/me')
}

const handleLogout = async () => {
  if (window.confirm('确定要退出登录吗？')) {
    await logout()
  }
}

const handleMobileLogout = async () => {
  if (window.confirm('确定要退出登录吗？')) {
    mobileMenuOpen.value = false
    await logout()
  }
}

const handleHomeActive = (event) => {
  const section = event.detail?.section
  if (section) activeHomeSection.value = section
}

watch(() => route.fullPath, () => {
  mobileMenuOpen.value = false
})

onMounted(async () => {
  window.addEventListener('nova-home-active', handleHomeActive)
  try {
    const res = await api.get('/api/auth/profile')
    if (res.code === 200 && res.data?.avatar) {
      authStore.patchUser({
        avatar: res.data.avatar,
        nickname: res.data.nickname || authStore.nickname,
      })
    }
  } catch (_) {
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('nova-home-active', handleHomeActive)
})
</script>

<style scoped>
.app-layout :deep([class~='text-slate-400']),
.app-layout :deep([class~='text-slate-500']),
.app-layout :deep([class~='text-slate-300']) {
  color: var(--text-tertiary) !important;
}

.app-layout :deep([class~='text-slate-700']),
.app-layout :deep([class~='text-indigo-600']),
.app-layout :deep([class~='text-indigo-500/80']) {
  color: var(--primary) !important;
}

.app-layout :deep([class~='text-indigo-700']) {
  color: color-mix(in srgb, var(--primary) 84%, black 16%) !important;
}

.app-layout :deep([class~='text-amber-500']) {
  color: var(--rank-gold) !important;
}

.app-layout :deep([class~='border-slate-100']) {
  border-color: var(--border-soft) !important;
}

.app-layout :deep([class~='border-white/60']) {
  border-color: var(--border-soft) !important;
}

.app-layout :deep([class~='bg-indigo-50/50']) {
  background: var(--accent-soft) !important;
}

.app-layout :deep([class~='bg-white/95']) {
  background: var(--bg-elevated) !important;
}

.layout-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(100px);
}

.layout-glow-a {
  left: -64px;
  top: -84px;
  width: 280px;
  height: 280px;
  background: var(--module-glow-b);
}

.layout-glow-b {
  right: 0;
  top: 20px;
  width: 280px;
  height: 280px;
  background: var(--module-glow-c);
}

.layout-glow-c {
  left: 33%;
  bottom: 0;
  width: 320px;
  height: 320px;
  background: var(--module-glow-a);
}

.floating-nav {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  border-radius: 999px;
  border: 1px solid var(--border-soft);
  background: var(--surface-panel-soft);
  backdrop-filter: blur(20px);
  box-shadow: var(--shadow-soft);
  padding: 8px 10px;
  animation: navEnter 820ms cubic-bezier(0.22, 1, 0.36, 1);
}

.brand-pill {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  border-radius: 999px;
  border: 1px solid var(--border-soft);
  background: var(--bg-elevated);
  padding: 8px 12px;
  white-space: nowrap;
}

.nav-center {
  min-width: 0;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.nav-item-pill {
  border-radius: 999px;
  padding: 9px 14px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  transition: color 0.2s ease, background-color 0.2s ease, transform 0.2s ease;
}

.nav-item-pill-idle {
  color: var(--text-secondary);
}

.nav-item-pill-idle:hover {
  transform: translateY(-1px);
  background: var(--primary-soft);
  color: var(--text-primary);
}

.nav-item-pill-active {
  background: var(--accent-soft);
  color: var(--primary);
}

.nav-actions {
  min-width: 0;
  justify-self: end;
}

.icon-btn {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 999px;
  border: 1px solid var(--border-soft);
  background: var(--bg-soft);
  color: var(--text-secondary);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: visible; /* 确保图标阴影或微调不会被裁切 */
}

.icon-btn:hover {
  transform: translateY(-1px);
  color: var(--text-primary);
}

.icon-btn-danger {
  color: var(--danger);
}

.profile-pill {
  min-height: 40px;
  max-width: min(320px, 26vw);
  min-width: 0;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  border-radius: 999px;
  border: 1px solid rgba(34, 27, 41, 0.1);
  background: var(--bg-elevated);
  padding: 0 14px 0 10px;
  color: var(--primary);
}

.profile-copy {
  display: flex;
  min-width: 0;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.15;
}

.profile-label {
  font-size: 13px;
  font-weight: 600;
}

.profile-name {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-avatar {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: var(--accent-soft);
  overflow: hidden;
}

.profile-avatar-img {
  object-fit: cover;
}

.nav-mobile-trigger {
  display: none;
}

.mobile-drawer {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 50;
  width: min(84vw, 320px);
  display: flex;
  flex-direction: column;
  border-left: 1px solid var(--border-soft);
  background: var(--surface-panel);
  backdrop-filter: blur(18px);
  box-shadow: -18px 0 44px rgba(34, 27, 41, 0.12);
  transition: transform 0.26s ease;
}

.mobile-nav-item,
.mobile-action-btn {
  width: 100%;
  min-height: 48px;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 14px;
  border: 1px solid var(--border-soft);
  background: var(--bg-soft);
  padding: 0 12px;
  font-size: 13px;
}

.mobile-nav-item-idle {
  color: var(--text-secondary);
}

.mobile-nav-item-active {
  color: var(--primary);
  background: var(--accent-soft);
}

.mobile-logout-btn {
  width: 100%;
  border-radius: 14px;
  border: 1px solid var(--danger-border);
  background: var(--danger-soft);
  color: var(--danger);
  padding: 11px 12px;
  font-size: 13px;
  font-weight: 600;
}

@keyframes navEnter {
  0% {
    opacity: 0;
    transform: translateY(-12px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1439px) {
  .profile-pill {
    max-width: 220px;
  }
}

@media (max-width: 1279px) {
  .floating-nav {
    grid-template-columns: auto 1fr;
  }

  .nav-actions {
    margin-left: auto;
  }
}

@media (max-width: 639px) {
  .floating-nav {
    grid-template-columns: auto 1fr auto;
  }

  .nav-mobile-trigger {
    display: inline-flex;
    justify-self: end;
  }
}

.dark .floating-nav,
.dark .brand-pill,
.dark .icon-btn,
.dark .profile-pill,
.dark .mobile-drawer,
.dark .mobile-nav-item,
.dark .mobile-action-btn {
  background: var(--surface-panel-soft);
  border-color: rgba(255, 255, 255, 0.08);
}

.dark .mobile-nav-item-active {
  background: var(--accent-soft);
  border-color: var(--accent-border);
}
</style>
