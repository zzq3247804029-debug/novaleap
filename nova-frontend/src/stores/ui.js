import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)

  // 黑夜模式状态
  const isDarkMode = ref(localStorage.getItem('nova_theme') === 'dark')

  // 全局 loading
  const globalLoading = ref(false)

  // 通知消息队列
  const notifications = ref([])

  // 切换侧边栏
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 切换黑夜模式
  function toggleDarkMode() {
    isDarkMode.value = !isDarkMode.value
    applyTheme()
  }

  // 应用主题到 DOM
  function applyTheme() {
    if (isDarkMode.value) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('nova_theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('nova_theme', 'light')
    }
  }

  // 初始化应用主题
  applyTheme()

  // 添加通知
  function addNotification(message, type = 'info', duration = 3000) {
    const id = Date.now()
    notifications.value.push({ id, message, type })
    if (duration > 0) {
      setTimeout(() => removeNotification(id), duration)
    }
  }

  // 移除通知
  function removeNotification(id) {
    notifications.value = notifications.value.filter(n => n.id !== id)
  }

  return {
    sidebarCollapsed,
    isDarkMode,
    globalLoading,
    notifications,
    toggleSidebar,
    toggleDarkMode,
    addNotification,
    removeNotification,
  }
})
