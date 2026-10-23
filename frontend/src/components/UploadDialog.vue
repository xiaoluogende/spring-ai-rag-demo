<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadDocument } from '../api/chat'

defineProps({
  modelValue: { type: Boolean, default: false },
})
const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)
const fileList = ref([])

async function handleUpload(options) {
  uploading.value = true
  try {
    // 后端返回文案形如「已入库 11 个片段：test-doc.md」
    const msg = await uploadDocument(options.file)
    ElMessage.success(msg || '上传成功')
    options.onSuccess?.(msg)
  } catch (e) {
    ElMessage.error('上传失败，请重试')
    options.onError?.(e)
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="上传文档到知识库"
    width="520px"
    :close-on-click-modal="!uploading"
    @update:model-value="emit('update:modelValue', $event)"
    @closed="fileList = []"
  >
    <el-upload
      v-model:file-list="fileList"
      drag
      :limit="1"
      :http-request="handleUpload"
      accept=".md,.txt"
      :disabled="uploading"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">把文件拖到这里，或 <em>点击选择</em></div>
      <template #tip>
        <div class="el-upload__tip">
          支持 .md / .txt，单文件不超过 10MB。上传后会自动切分、向量化并写入 Chroma。
        </div>
      </template>
    </el-upload>
  </el-dialog>
</template>
