<script setup>
import { computed } from 'vue'
import { renderMd } from '../utils/markdown'

const props = defineProps({
  item: { type: Object, required: true },
})

const isUser = computed(() => props.item.placement === 'end')

// 只有 AI 消息走 Markdown 渲染；用户消息用插值，天然转义
const html = computed(() => (isUser.value ? '' : renderMd(props.item.content)))
</script>

<template>
  <span v-if="isUser" class="plain">{{ item.content }}</span>
  <div v-else class="markdown-body" v-html="html" />
</template>

<style scoped>
.plain {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
