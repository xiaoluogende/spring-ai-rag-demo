import request from './request'

/** 普通大模型对话 */
export function askQuestion(question) {
  return request.get('/chat/ask', { params: { question } })
}

/** 知识库检索增强问答 */
export function askRag(question) {
  return request.get('/chat/rag', { params: { question } })
}

/** 上传文档入库，字段名必须是 file */
export function uploadDocument(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/doc/upload', form)
}
