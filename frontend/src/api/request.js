import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  timeout: 60000,
})

// 每个请求自动带上 Sa-Token（后端 token-name 配置为 satoken）
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('satoken')
  if (token) {
    config.headers['satoken'] = token
  }
  return config
})

// 后端统一返回 Result<T>：{ code, message, data }
// 这里直接把 data 拆出来，业务层拿到的就是纯数据
request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code !== 0) {
        ElMessage.error(body.message || '请求失败')
        return Promise.reject(new Error(body.message || '请求失败'))
      }
      return body.data
    }
    return body
  },
  (error) => {
    let msg
    if (error.response) {
      msg =
        error.response.status === 413
          ? '文件超过大小限制（10MB）'
          : `请求失败（HTTP ${error.response.status}）`
    } else if (error.code === 'ECONNABORTED') {
      msg = '请求超时，模型响应较慢，请重试'
    } else {
      msg = '无法连接后端服务，请确认 Spring Boot 已启动'
    }
    // skipErrorHandler：静默请求（如自动登录）不弹提示
    if (!error.config?.skipErrorHandler) {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  },
)

export default request
