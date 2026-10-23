<script setup>
import { computed, reactive, ref } from 'vue'
import { BubbleList, XSender } from 'vue-element-plus-x'
import { askQuestion, askRag } from '../api/chat'
import AnswerContent from './AnswerContent.vue'
import ReferenceList from './ReferenceList.vue'
import RetrievalTrace from './RetrievalTrace.vue'

const props = defineProps({
  mode: { type: String, default: 'rag' },
})

const messages = ref([])
const loading = ref(false)
const senderRef = ref(null)

const placeholder = computed(() =>
  props.mode === 'rag' ? '基于知识库提问…' : '直接向大模型提问…',
)

const examples = computed(() =>
  props.mode === 'rag'
    ? ['Spring AI 有哪些核心抽象', 'RAG 是怎么减少幻觉的', '分块为什么要按 token 算']
    : ['解释一下 SpringBoot 自动配置', '什么是 IoC 容器', 'JDK 动态代理有什么用'],
)

async function send(question) {
  if (!question || loading.value) return

  const stamp = Date.now()
  const userMsg = { id: `u-${stamp}`, placement: 'end', content: question }
  // 必须用 reactive 包装：
  // push 进 ref 数组后，数组里存的是代理对象；直接改原始对象不经过 Proxy，视图不会更新
  const aiMsg = reactive({
    id: `a-${stamp}`,
    placement: 'start',
    content: '',
    loading: true,
    mode: props.mode,
  })
  messages.value.push(userMsg, aiMsg)
  loading.value = true

  try {
    const data = props.mode === 'rag' ? await askRag(question) : await askQuestion(question)
    Object.assign(aiMsg, {
      content: data.answer,
      references: data.references || [],
      costMs: data.costMs,
      loading: false,
    })
  } catch {
    Object.assign(aiMsg, {
      content: '请求失败，请稍后重试。',
      loading: false,
      error: true,
    })
  } finally {
    loading.value = false
  }
}

function onSubmit() {
  const raw = senderRef.value?.getModelValue?.()
  const question = String(raw?.text ?? '').trim()
  if (!question) return
  senderRef.value?.clear?.()
  send(question)
}
</script>

<template>
  <main class="chat">
    <div v-if="!messages.length" class="empty">
      <h2>有什么可以帮你？</h2>
      <p>{{ mode === 'rag' ? '回答基于已上传的知识库文档' : '直接调用大模型，不检索知识库' }}</p>
      <div class="examples">
        <el-button v-for="q in examples" :key="q" round @click="send(q)">{{ q }}</el-button>
      </div>
    </div>

    <div v-else class="messages">
      <BubbleList :list="messages" max-height="100%">
        <template #content="{ item }">
          <AnswerContent :item="item" />
        </template>
        <template #footer="{ item }">
          <div v-if="item.placement === 'start'" class="meta">
            <RetrievalTrace
              v-if="item.mode === 'rag'"
              :references="item.references || []"
              :loading="item.loading"
              :cost-ms="item.costMs"
              :mode="item.mode"
            />
            <ReferenceList
              v-if="item.mode === 'rag'"
              :references="item.references || []"
              :loading="item.loading"
              :cost-ms="item.costMs"
              :mode="item.mode"
            />
            <div v-if="item.costMs" class="cost">
              <el-tag size="small" type="info" round>耗时 {{ item.costMs }} ms</el-tag>
            </div>
          </div>
        </template>
      </BubbleList>
    </div>

    <div class="composer">
      <XSender
        ref="senderRef"
        :loading="loading"
        :placeholder="placeholder"
        submit-type="enter"
        clearable
        @submit="onSubmit"
      />
    </div>
  </main>
</template>

<style scoped>
.chat {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px 20px;
}

.empty h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.empty p {
  margin: 0;
  color: #9aa4b2;
  font-size: 13px;
}

.examples {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
}

.messages {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding: 20px;
}

.meta {
  margin-top: 8px;
}

.cost {
  margin-top: 8px;
}

.composer {
  flex: none;
  padding: 12px 20px 20px;
  border-top: 1px solid #e3e7ec;
}
</style>
