<script setup>
import { computed } from 'vue'
import { Document } from '@element-plus/icons-vue'

const props = defineProps({
  references: { type: Array, default: () => [] },
  costMs: { type: Number, default: 0 },
  mode: { type: String, default: 'chat' },
  loading: { type: Boolean, default: false },
})

const toPct = (score) => Math.round(Number(score ?? 0) * 100)

// 分数分级：>=80 绿（高度相关）、>=60 蓝（相关）、其余灰
const scoreColor = (pct) => (pct >= 80 ? '#67c23a' : pct >= 60 ? '#409eff' : '#909399')

const missed = computed(
  () => props.mode === 'rag' && !props.loading && props.references.length === 0,
)
</script>

<template>
  <div class="refs">
    <!-- 未命中：这是防幻觉的直接证据，演示时要能看见 -->
    <el-alert
      v-if="missed"
      type="warning"
      :closable="false"
      show-icon
      title="知识库未命中"
      description="没有检索到相关片段，已拒绝编造答案"
    />

    <template v-else-if="references.length">
      <div class="refs-title">
        <el-icon><Document /></el-icon>
        <span>参考依据 · {{ references.length }} 条</span>
      </div>

      <el-card v-for="(r, i) in references" :key="i" shadow="never" class="ref-card">
        <div class="ref-head">
          <el-tag size="small" type="primary" effect="light">{{ r.docName }}</el-tag>
          <div class="score">
            <span class="score-num" :style="{ color: scoreColor(toPct(r.score)) }">
              {{ toPct(r.score) }}%
            </span>
            <el-progress
              class="score-bar"
              :percentage="toPct(r.score)"
              :stroke-width="4"
              :show-text="false"
              :color="scoreColor(toPct(r.score))"
            />
          </div>
        </div>
        <p class="snippet">{{ r.snippet }}</p>
      </el-card>
    </template>
  </div>
</template>

<style scoped>
.refs {
  margin-top: 12px;
}

.refs-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #8a94a6;
}

.ref-card {
  margin-bottom: 8px;
  border-radius: 8px;
  background: #fafbfc;
}

.ref-card :deep(.el-card__body) {
  padding: 10px 12px;
}

.ref-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.score {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-num {
  font-size: 12px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.score-bar {
  width: 48px;
}

.snippet {
  margin: 8px 0 0;
  font-size: 12.5px;
  line-height: 1.6;
  color: #57606a;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
