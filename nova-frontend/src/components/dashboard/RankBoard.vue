<template>
  <section class="rank-shell">
    <div class="flex flex-col gap-3 border-b border-border-subtle pb-6 md:flex-row md:items-end md:justify-between">
      <div>
        <p class="text-xs font-semibold uppercase tracking-[0.24em] text-text-muted">Growth Board</p>
        <h2 class="mt-3 text-[30px] font-semibold tracking-[-0.04em] text-text-primary">成长排行榜</h2>
        <p class="mt-2 text-sm leading-7 text-text-secondary">把原来的表格感收成更像产品模块的榜单视图，数据逻辑保持不变。</p>
      </div>
      <span class="rank-badge">实时刷新</span>
    </div>

    <div class="mt-6 grid gap-5 xl:grid-cols-2">
      <article v-for="board in boards" :key="board.key" class="board-card">
        <header class="board-head">
          <div>
            <h3 class="text-lg font-semibold tracking-tight text-text-primary">{{ board.title }}</h3>
            <p class="mt-1 text-xs text-text-muted">{{ board.description }}</p>
          </div>
          <span class="board-pill">Top 10</span>
        </header>

        <div class="mt-4 space-y-3">
          <div v-if="!board.list.length" class="empty-rank">
            {{ board.emptyText }}
          </div>

          <div
            v-for="item in board.list"
            :key="`${board.key}-${item.userId}-${item.rank}`"
            class="rank-row"
            :class="{ 'rank-row-top3': item.rank <= 3 }"
          >
            <div class="rank-index" :class="rankIndexClass(item.rank)">
              {{ item.rank }}
            </div>

            <div class="rank-avatar">
              <span v-if="isEmojiAvatar(item.avatar)">{{ item.avatar }}</span>
              <img v-else :src="item.avatar" alt="avatar" />
            </div>

            <div class="min-w-0">
              <div class="truncate text-sm font-semibold text-text-primary">{{ item.displayName }}</div>
              <div class="mt-1 text-xs text-text-muted">{{ board.metricLabel }}</div>
            </div>

            <div class="score-wrap">
              <span class="score-value">{{ item[board.scoreKey] }}</span>
              <span class="score-unit">{{ board.unit }}</span>
            </div>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  questionTopTen: {
    type: Array,
    default: () => [],
  },
  scoreTopTen: {
    type: Array,
    default: () => [],
  },
  isEmojiAvatar: {
    type: Function,
    required: true,
  },
})

const boards = computed(() => ([
  {
    key: 'question',
    title: '做题榜',
    description: '按累计做题数排序',
    metricLabel: '今日刷题节奏',
    unit: '题',
    scoreKey: 'questionDone',
    emptyText: '暂无做题数据',
    list: props.questionTopTen,
  },
  {
    key: 'score',
    title: '跑分榜',
    description: '按游戏最高分排序',
    metricLabel: '休息时刻分数',
    unit: '分',
    scoreKey: 'gameBestScore',
    emptyText: '暂无游戏数据',
    list: props.scoreTopTen,
  },
]))

const rankIndexClass = (rank) => {
  if (rank === 1) return 'rank-index-first'
  if (rank === 2) return 'rank-index-second'
  if (rank === 3) return 'rank-index-third'
  return ''
}
</script>

<style scoped>
.rank-shell {
  border-radius: 32px;
  border: 1px solid var(--border-soft);
  background: var(--bg-card);
  box-shadow: var(--shadow-soft);
  padding: 28px;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  border: 1px solid rgba(109, 140, 255, 0.18);
  background: rgba(109, 140, 255, 0.08);
  padding: 9px 14px;
  font-size: 12px;
  font-weight: 600;
  color: var(--primary);
}

.board-card {
  border-radius: 26px;
  border: 1px solid rgba(232, 236, 244, 0.92);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(249, 250, 253, 0.94));
  padding: 22px;
}

.board-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.board-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  background: rgba(246, 247, 251, 0.95);
  border: 1px solid rgba(232, 236, 244, 0.92);
  padding: 8px 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.rank-row {
  display: grid;
  grid-template-columns: 42px 44px minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  border-radius: 20px;
  background: rgba(250, 251, 255, 0.9);
  border: 1px solid rgba(232, 236, 244, 0.86);
  padding: 16px 18px;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.rank-row:hover {
  transform: translateY(-1px);
  border-color: rgba(109, 140, 255, 0.2);
}

.rank-row-top3 {
  background: linear-gradient(90deg, rgba(237, 242, 255, 0.96), rgba(249, 250, 255, 0.96));
}

.rank-index {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: rgba(246, 247, 251, 0.95);
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 700;
}

.rank-index-first,
.rank-index-second,
.rank-index-third {
  color: var(--primary);
  background: rgba(109, 140, 255, 0.12);
}

.rank-avatar {
  width: 44px;
  height: 44px;
  overflow: hidden;
  display: grid;
  place-items: center;
  border-radius: 14px;
  border: 1px solid rgba(232, 236, 244, 0.9);
  background: white;
}

.rank-avatar span {
  font-size: 24px;
}

.rank-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.score-wrap {
  text-align: right;
  display: flex;
  align-items: baseline;
  justify-content: flex-end;
  gap: 4px;
  min-width: 88px;
}

.score-value {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.04em;
  color: var(--text-primary);
}

.score-unit {
  font-size: 12px;
  color: var(--text-muted);
}

.empty-rank {
  border-radius: 20px;
  border: 1px dashed rgba(203, 213, 225, 0.9);
  padding: 22px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 14px;
}

@media (max-width: 640px) {
  .rank-shell {
    padding: 22px;
  }

  .rank-row {
    grid-template-columns: 38px 42px minmax(0, 1fr) auto;
    gap: 12px;
    padding: 14px;
  }

  .score-wrap {
    min-width: 68px;
  }

  .score-value {
    font-size: 24px;
  }
}

.dark .rank-shell,
.dark .board-card,
.dark .rank-row,
.dark .rank-avatar {
  background: rgba(17, 24, 39, 0.84);
  border-color: rgba(51, 65, 85, 0.7);
}

.dark .rank-row-top3 {
  background: rgba(30, 41, 59, 0.92);
}

.dark .board-pill,
.dark .rank-index {
  background: rgba(15, 23, 42, 0.86);
}
</style>
