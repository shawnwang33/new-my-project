import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // --- 新增 server 配置解决跨域 ---
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 你的 Spring Boot 后端地址
        changeOrigin: true, // 允许跨域
        // 如果你的后端接口没有 /api 前缀，需要打开下面这行进行路径重写
        //rewrite: (path) => path.replace(/^\/api/, '') 
      }
    }
  }
})