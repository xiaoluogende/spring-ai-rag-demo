import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },

  server: {
    // Windows 上默认监听 ::1 可能被拒绝，强制走 IPv4
    host: '127.0.0.1',
    // 不用 5173：它落在 Windows 保留端口段 5141-5240 内（Docker/WSL2 保留），绑定会报 EACCES
    port: 3000,
    proxy: {
      // 开发时把接口请求转发到 Spring Boot，避免跨域
      // 注意：新增后端接口路径时，这里要同步加一条
      '/chat': { target: 'http://localhost:8080', changeOrigin: true },
      '/doc': { target: 'http://localhost:8080', changeOrigin: true },
      '/auth': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },

  build: {
    // 构建产物直接输出到后端的 static 目录，这样 mvn package 后单 jar 就能跑
    outDir: fileURLToPath(new URL('../demo/src/main/resources/static', import.meta.url)),
    // outDir 在项目根之外时 Vite 默认不清空，必须显式开启
    emptyOutDir: true,
  },
})
