import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import vue from '@vitejs/plugin-vue'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
// import { tr } from 'element-plus/es/locale'
// https://vitejs.dev/config/
export default defineConfig({
  server:{
    hmr:true,
    proxy: {
      '/api': { //这里的test可以理解为后端接口里面的前缀，就是所有的地址前面都是http://aaa.com/test/xxx/xxx
        // target: 'http://localhost:8081/api',//这个不多说，请求的目标地址
        target: 'http://localhost:8000',//这个不多说，请求的目标地址
        changeOrigin: true,//是否允许跨域
        //rewrite: path => path.replace(/^\/api/, '')
        pathRewrite: {
          '^/api': '/' // 重写路径（表示请求的地址没有test前缀的时候）
        }
      }
    }
    
  },
  headers: {
    'X-Requested-With': 'XMLHttpRequest',
    'Content-Type': 'multipart/form-data',
    'Access-Control-Allow-Origin': '*',
    // 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
 
  },
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },


})
