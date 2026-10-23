import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

const md = new MarkdownIt({
  // 禁止原始 HTML：从源头减少 XSS 面，模型输出里即使有 <script> 也不会被当标签解析
  html: false,
  // 自动识别裸链接
  linkify: true,
  // 单个换行也渲染成换行，符合聊天场景的阅读习惯
  breaks: true,
})

/**
 * 把 Markdown 文本渲染成安全的 HTML。
 * markdown-it 负责语法，DOMPurify 负责兜底清洗（防 onerror 之类的事件属性）。
 */
export function renderMd(text) {
  if (!text) return ''
  return DOMPurify.sanitize(md.render(text))
}
