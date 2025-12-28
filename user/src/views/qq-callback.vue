<template>
  <div class="qq-callback">
    <div class="loading-container" v-if="loading">
      <el-icon class="loading-icon" :size="48">
        <Loading />
      </el-icon>
      <p>正在登录中...</p>
    </div>
    <div class="error-container" v-else-if="error">
      <el-icon :size="48" color="#F56C6C">
        <CircleClose />
      </el-icon>
      <p>{{ error }}</p>
      <el-button type="primary" @click="goToLogin">返回登录</el-button>
    </div>
  </div>
</template>

<script>
import { setToken } from '@/utils/auth'
import { Loading, CircleClose } from '@element-plus/icons-vue'

export default {
  name: 'QQCallback',
  components: { Loading, CircleClose },
  data() {
    return {
      loading: true,
      error: null
    }
  },
  created() {
    this.handleCallback()
  },
  methods: {
    async handleCallback() {
      try {
        // 从URL获取参数
        const urlParams = new URLSearchParams(window.location.search)
        const token = urlParams.get('token')
        const error = urlParams.get('error')

        if (error) {
          this.loading = false
          this.error = decodeURIComponent(error)
          return
        }

        if (!token) {
          this.loading = false
          this.error = '登录失败：未获取到登录凭证'
          return
        }

        // 保存token
        setToken(token)

        // 获取用户信息
        await this.$store.dispatch('GetInfo')

        // 跳转到首页
        this.$router.push({ path: '/index' })

      } catch (e) {
        console.error('QQ登录回调处理失败:', e)
        this.loading = false
        this.error = '登录处理失败: ' + (e.message || '未知错误')
      }
    },
    goToLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.qq-callback {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.loading-container,
.error-container {
  text-align: center;
  background: white;
  padding: 60px 80px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.loading-container p,
.error-container p {
  margin-top: 20px;
  font-size: 18px;
  color: #333;
}

.loading-icon {
  animation: spin 1s linear infinite;
  color: #12B7F5;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.error-container .el-button {
  margin-top: 20px;
}
</style>
