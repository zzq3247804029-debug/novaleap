<template>
  <canvas ref="canvasRef" class="absolute inset-0 w-full h-full pointer-events-none z-[2] opacity-[0.92]"></canvas>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'

const canvasRef = ref(null)

const SCENES = ['张志琪', 'NovaLeap']
const FPS_DESKTOP = 30
const FPS_MOBILE = 24
const MAX_DOTS_DESKTOP = 1100
const MAX_DOTS_MOBILE = 600

const HOLD_TEXT_MS = 420
const HOLD_EXPLODE_MS = 420
const MOVE_SPEED_TO_TEXT = 0.16
const MOVE_SPEED_EXPLODE = 0.28
const MOVE_SPEED_SHRINK = 0.20
const ARRIVE_EPS = 1.2
const MOUSE_INFLUENCE_RADIUS = 300
const MOUSE_PULL_STRENGTH = 0.08

const rand = (min, max) => min + Math.random() * (max - min)

let ctx = null
let rafId = 0
let dots = []
let sceneIndex = 0
let phase = 'toText'
let phaseStartAt = 0
let lastFrameAt = 0
let frameInterval = 1000 / FPS_DESKTOP
let mouseX = -1000
let mouseY = -1000

const fitCanvas = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const dpr = 1
  const width = Math.max(1, Math.floor(canvas.clientWidth || window.innerWidth))
  const height = Math.max(1, Math.floor(canvas.clientHeight || window.innerHeight))
  canvas.width = Math.floor(width * dpr)
  canvas.height = Math.floor(height * dpr)
  ctx?.setTransform(dpr, 0, 0, dpr, 0, 0)
  frameInterval = 1000 / (width < 768 ? FPS_MOBILE : FPS_DESKTOP)
}

const drawTextToImageData = (text) => {
  const canvas = canvasRef.value
  if (!canvas || !ctx) return null
  const width = canvas.width
  const height = canvas.height
  const isMobile = width < 768

  ctx.clearRect(0, 0, width, height)

  const chars = text.split('')
  const spacing = text === 'NovaLeap'
    ? Math.max(24, Math.min(54, width / (chars.length + 2)))
    : Math.max(42, Math.min(92, width / (chars.length + 1.8)))

  for (let i = 0; i < chars.length; i += 1) {
    const fontSize = text === 'NovaLeap'
      ? (isMobile ? rand(86, 104) : rand(112, 136))
      : (isMobile ? rand(92, 118) : rand(126, 162))

    ctx.save()
    ctx.font = text === 'NovaLeap'
      ? `700 ${Math.floor(fontSize)}px "DM Sans","Segoe UI","Microsoft YaHei",sans-serif`
      : `700 ${Math.floor(fontSize)}px "Ma Shan Zheng","Microsoft YaHei","KaiTi",serif`
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'

    const hue = text === 'NovaLeap' ? rand(205, 235) : rand(350, 12)
    const sat = text === 'NovaLeap' ? rand(48, 62) : rand(50, 66)
    const lig = text === 'NovaLeap' ? rand(56, 68) : rand(48, 60)
    ctx.fillStyle = `hsl(${hue} ${sat}% ${lig}%)`

    const x = width * 0.5 - ((chars.length / 2 - i) * spacing)
    const y = height * 0.5
    ctx.fillText(chars[i], x, y)
    ctx.restore()
  }

  const imageData = ctx.getImageData(0, 0, width, height)
  ctx.clearRect(0, 0, width, height)
  return imageData
}

const collectPoints = (data, width, height, step) => {
  const points = []
  for (let x = 0; x < width; x += step) {
    for (let y = 0; y < height; y += step) {
      const i = (Math.floor(y) * width + Math.floor(x)) * 4
      if (data[i + 3] < 126) continue
      points.push({
        x,
        y,
        color: { r: data[i], g: data[i + 1], b: data[i + 2] },
      })
    }
  }
  return points
}

const sampleTextDots = (text) => {
  const canvas = canvasRef.value
  const imageData = drawTextToImageData(text)
  if (!canvas || !imageData) return []
  const width = imageData.width
  const height = imageData.height
  const isMobile = width < 768
  const maxDots = isMobile ? MAX_DOTS_MOBILE : MAX_DOTS_DESKTOP
  const step = text === 'NovaLeap' ? (isMobile ? 5 : 4) : (isMobile ? 4 : 3.8)
  const data = imageData.data
  let points = collectPoints(data, width, height, step)
  if (points.length < 260) {
    points = collectPoints(data, width, height, Math.max(2.6, step * 0.75))
  }

  if (points.length <= maxDots) return points
  const stride = Math.ceil(points.length / maxDots)
  const sampled = []
  for (let i = 0; i < points.length; i += stride) sampled.push(points[i])
  return sampled
}

const setTargetsToText = (text, keepPosition = true) => {
  const canvas = canvasRef.value
  if (!canvas) return
  const width = canvas.width
  const height = canvas.height
  const points = sampleTextDots(text)
  if (points.length === 0) {
    dots = []
    return
  }

  const cx = width * 0.5
  const cy = height * 0.5

  if (!keepPosition || dots.length !== points.length) {
    dots = points.map((p) => ({
      x: cx + rand(-40, 40),
      y: cy + rand(-40, 40),
      tx: p.x,
      ty: p.y,
      ox: p.x, // 原始文本坐标
      oy: p.y,
      size: rand(4.0, 7.2), // 稍微大一点
      color: p.color,
      vx: 0,
      vy: 0,
    }))
    return
  }

  for (let i = 0; i < points.length; i += 1) {
    dots[i].tx = points[i].x
    dots[i].ty = points[i].y
    dots[i].ox = points[i].x
    dots[i].oy = points[i].y
    dots[i].color = points[i].color
  }
}

const setTargetsToExplosion = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const width = canvas.width
  const height = canvas.height
  const cx = width * 0.5
  const cy = height * 0.5
  const spread = Math.max(width, height) * 0.55

  for (const d of dots) {
    const vx = d.x - cx
    const vy = d.y - cy
    const len = Math.hypot(vx, vy) || 1
    const nx = vx / len
    const ny = vy / len
    const power = rand(spread * 0.72, spread * 1.25)
    d.tx = d.x + nx * power + rand(-46, 46)
    d.ty = d.y + ny * power + rand(-46, 46)
  }
}

const setTargetsToShrink = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const cx = canvas.width * 0.5
  const cy = canvas.height * 0.5
  for (const d of dots) {
    const ang = rand(0, Math.PI * 2)
    const r = rand(6, 28)
    d.tx = cx + Math.cos(ang) * r
    d.ty = cy + Math.sin(ang) * r
  }
}

const moveDots = (speed) => {
  let arrived = 0
  for (const d of dots) {
    // 基础移动
    const dx = d.tx - d.x
    const dy = d.ty - d.y
    
    // 鼠标干扰逻辑 (仅在非爆炸状态下更明显，或全局作用)
    const mdx = mouseX - d.x
    const mdy = mouseY - d.y
    const dist = Math.sqrt(mdx * mdx + mdy * mdy)
    
    let targetX = d.tx
    let targetY = d.ty
    
    if (dist < MOUSE_INFLUENCE_RADIUS) {
      const power = (MOUSE_INFLUENCE_RADIUS - dist) / MOUSE_INFLUENCE_RADIUS
      targetX += mdx * power * MOUSE_PULL_STRENGTH * 15 // 稍微放大偏移
      targetY += mdy * power * MOUSE_PULL_STRENGTH * 15
    }

    const finalDx = targetX - d.x
    const finalDy = targetY - d.y

    if (Math.abs(finalDx) < ARRIVE_EPS && Math.abs(finalDy) < ARRIVE_EPS) {
      d.x = targetX
      d.y = targetY
      arrived += 1
      continue
    }
    d.x += finalDx * speed
    d.y += finalDy * speed
  }
  return arrived === dots.length
}

const draw = () => {
  const canvas = canvasRef.value
  if (!canvas || !ctx) return
  const width = canvas.width
  const height = canvas.height
  ctx.clearRect(0, 0, width, height)
  
  // 绘制荧光效果
  for (const d of dots) {
    ctx.shadowBlur = 10
    ctx.shadowColor = `rgb(${d.color.r},${d.color.g},${d.color.b})`
    ctx.fillStyle = `rgba(${d.color.r},${d.color.g},${d.color.b},0.82)`
    ctx.fillRect(d.x, d.y, d.size, d.size)
  }
  ctx.shadowBlur = 0 // 重置
}

const enterPhase = (nextPhase, now) => {
  phase = nextPhase
  phaseStartAt = now
}

const bootstrapScene = () => {
  setTargetsToText(SCENES[sceneIndex], false)
  phase = 'toText'
  phaseStartAt = performance.now()
}

const tick = (now) => {
  if (now - lastFrameAt < frameInterval) {
    rafId = requestAnimationFrame(tick)
    return
  }
  lastFrameAt = now

  if (phase === 'toText') {
    const done = moveDots(MOVE_SPEED_TO_TEXT)
    if (done) enterPhase('holdText', now)
  } else if (phase === 'holdText') {
    if (now - phaseStartAt >= HOLD_TEXT_MS) {
      setTargetsToExplosion()
      enterPhase('explode', now)
    }
  } else if (phase === 'explode') {
    const done = moveDots(MOVE_SPEED_EXPLODE)
    if (done) enterPhase('holdExplode', now)
  } else if (phase === 'holdExplode') {
    if (now - phaseStartAt >= HOLD_EXPLODE_MS) {
      setTargetsToShrink()
      enterPhase('shrink', now)
    }
  } else if (phase === 'shrink') {
    const done = moveDots(MOVE_SPEED_SHRINK)
    if (done) {
      sceneIndex = (sceneIndex + 1) % SCENES.length
      setTargetsToText(SCENES[sceneIndex], true)
      enterPhase('toText', now)
    }
  }

  draw()
  rafId = requestAnimationFrame(tick)
}

const handleResize = () => {
  fitCanvas()
  bootstrapScene()
}

const handleMouseMove = (e) => {
  mouseX = e.clientX
  mouseY = e.clientY
}

onMounted(() => {
  ctx = canvasRef.value?.getContext('2d', { alpha: true })
  if (!ctx) return
  fitCanvas()
  bootstrapScene()
  lastFrameAt = 0
  rafId = requestAnimationFrame(tick)
  window.addEventListener('resize', handleResize)
  window.addEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  cancelAnimationFrame(rafId)
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('mousemove', handleMouseMove)
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Ma+Shan+Zheng&display=swap');
</style>
