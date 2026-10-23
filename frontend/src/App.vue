<script setup>
import { ref, watch } from 'vue'
import { Upload } from '@element-plus/icons-vue'
import ChatPanel from './components/ChatPanel.vue'
import UploadDialog from './components/UploadDialog.vue'

const mode = ref(localStorage.getItem('chat-mode') || 'rag')
const uploadVisible = ref(false)

watch(mode, (v) => localStorage.setItem('chat-mode', v))

const modeOptions = [
  { label: '普通对话', value: 'chat' },
  { label: '知识库问答', value: 'rag' },
]
</script>

<template>
  <div class="app">
    <header class="appbar">
      <div class="brand">
        <span class="dot" />
        <span class="title">Simple AI</span>
        <span class="subtitle">Spring AI · Chroma · RAG</span>
      </div>

      <div class="actions">
        <el-segmented v-model="mode" :options="modeOptions" />
        <el-button :icon="Upload" @click="uploadVisible = true">上传文档</el-button>
      </div>
    </header>

    <ChatPanel :mode="mode" />

    <UploadDialog v-model="uploadVisible" />
  </div>
</template>

<style scoped>
.app {
  display: flex;
  flex-direction: column;
  height: 100%;
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-left: 1px solid #e3e7ec;
  border-right: 1px solid #e3e7ec;
}

.appbar {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: 60px;
  padding: 0 20px;
  border-bottom: 1px solid #e3e7ec;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1f6feb;
}

.title {
  font-size: 15px;
  font-weight: 600;
}

.subtitle {
  font-size: 12px;
  color: #9aa4b2;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

@media (max-width: 640px) {
  .subtitle {
    display: none;
  }
  .appbar {
    padding: 0 12px;
  }
}
</style>
