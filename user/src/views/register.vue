<template>
  <div class="register-container">
    <div class="header">
      <img src="@/assets/logo/logo.png" class="logo" />
      <div class="title">网购商城 - 用户注册</div>
    </div>

    <div class="register-box">
      <img src="@/assets/images/login-sub-bg.webp" class="sub-bg" />

      <form class="register-form" @submit.prevent="handleRegister">
        <h3 class="title">用户注册</h3>

        <!-- 用户名 -->
        <div class="form-item">
          <div class="input-wrapper">
            <el-icon class="input-icon">
              <User />
            </el-icon>
            <input ref="usernameInput" v-model="registerForm.username" type="text" placeholder="请输入用户名（2-20位字符）"
              :disabled="loading" @input="onUsernameInput" autocomplete="username" class="custom-input" />
          </div>
          <div v-if="errors.username" class="error-message">{{ errors.username }}</div>
        </div>

        <!-- 密码 -->
        <div class="form-item">
          <div class="input-wrapper">
            <el-icon class="input-icon">
              <Lock />
            </el-icon>
            <input ref="passwordInput" v-model="registerForm.password" :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码（5-20位字符）" :disabled="loading" @input="onPasswordInput" autocomplete="new-password"
              class="custom-input" />
            <el-icon class="password-toggle" @click="showPassword = !showPassword">
              <Lock v-if="showPassword" />
              <Lock v-else />
            </el-icon>
          </div>
          <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
        </div>

        <!-- 确认密码 -->
        <div class="form-item">
          <div class="input-wrapper">
            <el-icon class="input-icon">
              <Lock />
            </el-icon>
            <input ref="confirmPasswordInput" v-model="registerForm.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'" placeholder="请再次输入密码" :disabled="loading"
              @input="onConfirmPasswordInput" autocomplete="new-password" class="custom-input" />
            <el-icon class="password-toggle" @click="showConfirmPassword = !showConfirmPassword">
              <Lock v-if="showConfirmPassword" />
              <Lock v-else />
            </el-icon>
          </div>
          <div v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</div>
        </div>

        <!-- 验证码 -->
        <div class="form-item">
          <div class="code-container">
            <div class="input-wrapper" style="width: 65%;">
              <el-icon class="input-icon">
                <Key />
              </el-icon>
              <input ref="codeInput" v-model="registerForm.code" type="text" placeholder="请输入验证码" :disabled="loading"
                @input="onCodeInput" autocomplete="off" class="custom-input" />
            </div>
            <div class="register-code">
              <button type="button" class="register-code-button" aria-label="刷新验证码"
                :disabled="loading" :title="loading ? '正在加载...' : '点击刷新验证码'" @click="getCode">
                <img :src="codeUrl" alt="验证码" class="register-code-img" />
              </button>
            </div>
          </div>
          <div v-if="errors.code" class="error-message">{{ errors.code }}</div>
        </div>

        <!-- 协议 -->
        <div class="form-item">
          <div class="agreement-checkbox">
            <label class="agreement-control">
              <input type="checkbox" v-model="registerForm.agreed" :disabled="loading" class="checkbox-input" />
              <span class="checkbox-custom"></span>
              我已阅读并同意
            </label>
            <button type="button" class="agreement-link" @click="showAgreement">《用户注册协议》</button>
          </div>
        </div>

        <!-- 注册按钮 -->
        <div class="form-item" style="width:100%;">
          <button type="submit" :disabled="loading || !registerForm.agreed" class="register-button"
            :class="{ 'loading': loading }">
            <span v-if="!loading">立即注册</span>
            <span v-else class="loading-text">
              <span class="loading-spinner"></span>注册中...
            </span>
          </button>
        </div>

        <!-- 登录链接 -->
        <div class="login-link">
          已有账号？
          <router-link to="/login">立即登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { getCodeImg, register } from '@/api/login'

const router = useRouter()

// 使用简单的响应式对象
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  code: '',
  uuid: '',
  agreed: false
})

const errors = ref({
  username: '',
  password: '',
  confirmPassword: '',
  code: ''
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const codeUrl = ref('')
const loading = ref(false)

// 输入处理函数
const onUsernameInput = (e) => {
  const value = e.target.value
  registerForm.value.username = value
  validateUsername()
}

const onPasswordInput = (e) => {
  const value = e.target.value
  registerForm.value.password = value
  validatePassword()
}

const onConfirmPasswordInput = (e) => {
  const value = e.target.value
  registerForm.value.confirmPassword = value
  validateConfirmPassword()
}

const onCodeInput = (e) => {
  const value = e.target.value
  registerForm.value.code = value
  validateCode()
}

// 验证函数
const validateUsername = () => {
  const value = registerForm.value.username
  if (!value || value.trim() === '') {
    errors.value.username = '请输入用户名'
    return false
  } else if (value.length < 2 || value.length > 20) {
    errors.value.username = '用户名长度在 2 到 20 个字符'
    return false
  } else if (!/^[a-zA-Z0-9_]{2,20}$/.test(value)) {
    errors.value.username = '用户名只能包含字母、数字和下划线'
    return false
  }
  errors.value.username = ''
  return true
}

const validatePassword = () => {
  const value = registerForm.value.password
  if (!value) {
    errors.value.password = '请输入密码'
    return false
  } else if (value.length < 5 || value.length > 20) {
    errors.value.password = '密码长度在 5 到 20 个字符'
    return false
  }
  errors.value.password = ''
  return true
}

const validateConfirmPassword = () => {
  const value = registerForm.value.confirmPassword
  if (!value) {
    errors.value.confirmPassword = '请再次输入密码'
    return false
  } else if (value !== registerForm.value.password) {
    errors.value.confirmPassword = '两次输入密码不一致'
    return false
  }
  errors.value.confirmPassword = ''
  return true
}

const validateCode = () => {
  const value = registerForm.value.code
  if (!value || value.trim() === '') {
    errors.value.code = '请输入验证码'
    return false
  }
  errors.value.code = ''
  return true
}

// 获取验证码
async function getCode() {
  if (loading.value) return

  try {
    loading.value = true
    const res = await getCodeImg()
    codeUrl.value = "data:image/gif;base64," + res.img
    registerForm.value.uuid = res.uuid
    registerForm.value.code = ''
    errors.value.code = ''
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请重试')
  } finally {
    loading.value = false
  }
}

// 显示用户协议
function showAgreement() {
  ElMessageBox.alert(
    `用户注册协议\n\n
    1. 用户注册成功后，即成为本站的正式用户\n
    2. 用户应妥善保管账号和密码\n
    3. 用户不得利用本站从事任何非法活动\n
    4. 本站有权根据实际情况修改本协议\n`,
    '用户注册协议',
    {
      confirmButtonText: '我知道了',
      customClass: 'agreement-dialog',
      draggable: true
    }
  )
}

// 处理注册
async function handleRegister() {
  // 验证所有字段
  const isValid = [
    validateUsername(),
    validatePassword(),
    validateConfirmPassword(),
    validateCode()
  ].every(v => v)

  if (!isValid) {
    ElMessage.warning('请检查表单输入')
    return
  }

  if (!registerForm.value.agreed) {
    ElMessage.warning('请先同意用户注册协议')
    return
  }

  try {
    loading.value = true

    const res = await register({
      username: registerForm.value.username,
      password: registerForm.value.password,
      code: registerForm.value.code,
      uuid: registerForm.value.uuid
    })

    if (res.code === 200) {
      ElMessage.success('注册成功，正在跳转到登录页')
      router.replace('/login')
    } else {
      ElMessage.error(res.msg || '注册失败')
      getCode()
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败，请重试')
    getCode()
  } finally {
    loading.value = false
  }
}

// 初始化验证码
onMounted(() => {
  getCode()

  // 添加全局样式，防止输入被阻止
  const style = document.createElement('style')
  style.textContent = `
    /* 防止输入被阻止 */
    .custom-input {
      caret-color: #409EFF !important;
      user-select: text !important;
      -webkit-user-select: text !important;
      -moz-user-select: text !important;
      -ms-user-select: text !important;
    }
    
    /* 移除可能干扰的样式 */
    .register-form input {
      all: unset;
    }
    
    /* 确保输入框可输入 */
    .register-form input[type="text"],
    .register-form input[type="password"] {
      -webkit-user-modify: read-write-plaintext-only !important;
      user-modify: read-write !important;
    }
  `
  document.head.appendChild(style)
})
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.header {
  width: 100%;
  height: 60px;
  display: flex;
  align-items: center;
  background: white;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, .1);
}

.header .logo {
  width: 36px;
  height: 36px;
  margin-left: 30px;
  margin-right: 10px;
}

.header .title {
  font-size: 24px;
  font-weight: bold;
  color: #222222;
}

.register-box {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
  background-image: url("@/assets/images/login-bg.png");
  background-size: cover;
  position: relative;
}

.sub-bg {
  width: 30%;
  position: absolute;
  left: 20%;
  top: 20%;
}

.register-form {
  margin-left: 500px;
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, .1);
}

.register-form .title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
  font-size: 20px;
  font-weight: 500;
}

.form-item {
  margin-bottom: 22px;
  position: relative;
}

.input-wrapper {
  position: relative;
  height: 40px;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  display: flex;
  align-items: center;
  padding: 0 15px;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #409EFF;
  box-shadow: 0 0 0 1px #409EFF;
}

.input-icon {
  margin-right: 10px;
  color: #c0c4cc;
}

.custom-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  font-size: 14px;
  color: #606266;
  background: transparent;
}

.custom-input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.custom-input::placeholder {
  color: #c0c4cc;
}

.password-toggle {
  position: absolute;
  right: 15px;
  cursor: pointer;
  color: #c0c4cc;
  transition: color 0.3s ease;
}

.password-toggle:hover {
  color: #409EFF;
}

.code-container {
  display: flex;
  gap: 5%;
  align-items: center;
}

.register-code {
  width: 30%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-code-img {
  height: 100%;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

.register-code-button {
  height: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.register-code-button:focus-visible {
  outline: 3px solid #409eff;
  outline-offset: 2px;
}

.register-code-img:hover {
  border-color: #409EFF;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
  margin-left: 15px;
}

.agreement-checkbox {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.agreement-control {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-input {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.checkbox-custom {
  width: 16px;
  height: 16px;
  border: 1px solid #dcdfe6;
  border-radius: 3px;
  margin-right: 8px;
  position: relative;
  transition: all 0.3s ease;
}

.checkbox-input:checked+.checkbox-custom {
  background-color: #409EFF;
  border-color: #409EFF;
}

.checkbox-input:checked+.checkbox-custom::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 1px;
  width: 4px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-input:focus-visible + .checkbox-custom {
  outline: 2px solid #409EFF;
  outline-offset: 2px;
}

.agreement-link {
  color: #409EFF;
  margin-left: 4px;
  padding: 0;
  border: 0;
  background: transparent;
  font: inherit;
  cursor: pointer;
}

.agreement-link:focus-visible {
  outline: 2px solid #409EFF;
  outline-offset: 2px;
}

.agreement-link:hover {
  text-decoration: underline;
}

.register-button {
  width: 100%;
  height: 40px;
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.register-button:hover:not(:disabled) {
  background: #66b1ff;
  transform: translateY(-1px);
}

.register-button:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

.register-button.loading {
  opacity: 0.8;
}

.loading-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.login-link {
  text-align: center;
  margin-top: 25px;
  color: #666;
  font-size: 14px;
}

.login-link a {
  color: #409EFF;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 500;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
