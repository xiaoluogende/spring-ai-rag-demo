import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import 'element-plus/dist/index.css'
import 'github-markdown-css/github-markdown-light.css'
import './style.css'

import App from './App.vue'
import { login } from './api/auth'

async function bootstrap() {
  // 演示用：没有 token 时自动以 admin 身份登录，避免每次手动登录
  if (!localStorage.getItem('satoken')) {
    try {
      const token = await login('admin')
      localStorage.setItem('satoken', token)
    } catch (e) {
      console.warn('[auth] 自动登录失败，接口将返回 401', e)
    }
  }

  const app = createApp(App)
  app.use(ElementPlus, { locale: zhCn })
  app.mount('#app')
}

bootstrap()
