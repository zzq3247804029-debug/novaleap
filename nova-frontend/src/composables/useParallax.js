/**
 * useParallax — 鼠标 3D 视差组合式函数
 * 根据鼠标位置计算偏移量数组
 */
import { ref, onMounted, onUnmounted } from 'vue'

export function useParallax(elements) {
  // 存储所有元素的计算后的 X/Y 偏移量
  const offsets = ref(elements.value.map(() => ({ x: 0, y: 0 })))

  const handleMouseMove = (e) => {
    // 获取视口中心点
    const cx = window.innerWidth / 2
    const cy = window.innerHeight / 2
    
    // 鼠标相对于中心的偏移像素
    const dx = e.clientX - cx
    const dy = e.clientY - cy

    elements.value.forEach((el, index) => {
      // parallaxX/Y 为各自元素的差速系数（例如 0.03 等）
      // 80 为放大乘数，保证位移明显但不过度
      offsets.value[index] = {
        x: dx * (el.parallaxX || 0) * 80,
        y: dy * (el.parallaxY || 0) * 80,
      }
    })
  }

  onMounted(() => {
    window.addEventListener('mousemove', handleMouseMove)
  })

  onUnmounted(() => {
    window.removeEventListener('mousemove', handleMouseMove)
  })

  return { offsets }
}
