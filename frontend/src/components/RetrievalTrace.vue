<script setup>
import { computed } from 'vue'
import { ThoughtChain } from 'vue-element-plus-x'

const props = defineProps({
  references: { type: Array, default: () => [] },
  costMs: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
  mode: { type: String, default: 'chat' },
})

/**
 * 把 RAG 的内部流程摊开成两个节点：
 * 检索 → 生成。请求中显示 loading，返回后变 success。
 */
const items = computed(() => {
  if (props.mode !== 'rag') return []

  const status = props.loading ? 'loading' : 'success'
  const count = props.references.length

  return [
    {
      id: 'retrieve',
      title: props.loading ? '正在检索知识库…' : `检索到 ${count} 个片段`,
      status,
      isCanExpand: count > 0,
      isDefaultExpand: false,
      thinkContent: props.references
        .map((r) => `${r.docName}　相似度 ${Number(r.score ?? 0).toFixed(3)}`)
        .join('\n'),
    },
    {
      id: 'generate',
      title: props.loading ? '等待模型生成答案…' : '调用 deepseek-chat 生成答案',
      status,
      isCanExpand: false,
      isDefaultExpand: false,
      thinkContent: '',
    },
  ]
})
</script>

<template>
  <ThoughtChain v-if="items.length" :thinking-items="items" class="trace" />
</template>

<style scoped>
.trace {
  max-width: 100%;
  margin-bottom: 4px;
}
</style>
