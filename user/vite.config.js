import { defineConfig, loadEnv } from 'vite'
import path from 'path'
import createVitePlugins from './vite/plugins'

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd())
  const { VITE_APP_ENV } = env
  return {
    // 部署生产环境和开发环境下的URL。
    // 默认情况下，vite 会假设你的应用是被部署在一个域名的根路径上
    // 例如 https://www.ruoyi.vip/。如果应用被部署在一个子路径上，你就需要用这个选项指定这个子路径。例如，如果你的应用被部署在 https://www.ruoyi.vip/admin/，则设置 baseUrl 为 /admin/。
    base: VITE_APP_ENV === 'production' ? '/' : '/',
    plugins: createVitePlugins(env, command === 'build'),
    resolve: {
      // https://cn.vitejs.dev/config/#resolve-alias
      alias: {
        // 设置路径
        '~': path.resolve(__dirname, './'),
        // 设置别名
        '@': path.resolve(__dirname, './src')
      },
      // https://cn.vitejs.dev/config/#resolve-extensions
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    // vite 相关配置
    server: {
      port: 18183,
      host: true,
      open: true,
      cors: true,
      proxy: {
        // https://cn.vitejs.dev/config/#server-proxy
        '/dev-api': {
          target: 'http://127.0.0.1:9091',
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api/, ''),
          secure: false
        },
        // WebSocket 代理配置
        '/ai/chat': {
          target: 'ws://localhost:9091',
          ws: true, // 启用 WebSocket 代理
          changeOrigin: true,
           secure: false,
            // 重要：添加重试和超时配置
          configure: (proxy, options) => {
            proxy.on('error', (err, req, res) => {
              console.log('WebSocket 代理错误:', err.message)
            })
            proxy.on('proxyReq', (proxyReq, req, res) => {
              console.log('代理请求:', req.method, req.url)
            })
          }
          // 这里不需要 rewrite，因为要保持路径 /ai/chat 不变
        }
      },
      '/ws': {
          target: 'ws://localhost:9091',
          ws: true,
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path.replace(/^\/ws/, '/ai/chat')
        },
           hmr: {
        protocol: 'ws',
        host: 'localhost',
        port: 18183,
        // 禁用 HMR 覆盖层
        overlay: false
      },
      watch: {
        usePolling: false
      }
    },
     // 构建配置
    build: {
      sourcemap: false,
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          manualChunks: {
            vue: ['vue', 'vue-router', 'pinia'],
            element: ['element-plus', '@element-plus/icons-vue']
          }
        }
      }
    },
    
    css: {
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule) => {
                if (atRule.name === 'charset') {
                  atRule.remove();
                }
              }
            }
          }
        ]
      }
    },
    //fix:error:stdin>:7356:1: warning: "@charset" must be the first rule in the file

  }
})
