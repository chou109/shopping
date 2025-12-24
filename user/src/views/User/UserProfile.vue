<template>
  <div class="user-profile">
    <!-- 主要内容区域 -->
    <div class="profile-content" v-loading="loading">
      <el-row :gutter="20">
        <!-- 左侧：个人信息卡片 -->
        <el-col :xs="24" :sm="8" :md="8" :lg="8">
          <div class="profile-card">
            <div class="avatar-section">
              <div class="avatar-wrapper">
                
                <div class="avatar-upload" @click="openAvatarDialog">
                  <el-icon><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </div>
              <h3 class="user-name">{{ userInfo.nickName || userInfo.userName || '用户' }}</h3>
              <p class="user-role">{{ formatUserType(userInfo.type) }}</p>
              <p class="user-id">ID: {{ userInfo.userId || userInfo.id }}</p>
            </div>

            <div class="stats-section">
              <div class="stat-item" @click="$router.push('/user/orders')">
                <div class="stat-value">{{ userStats.orderCount || 0 }}</div>
                <div class="stat-label">总订单</div>
              </div>
              <div class="stat-item" @click="$router.push('/user/favorite/product')">
                <div class="stat-value">{{ userStats.favoriteCount || 0 }}</div>
                <div class="stat-label">收藏</div>
              </div>
              <div class="stat-item" @click="$router.push('/user/footprint')">
                <div class="stat-value">{{ userStats.footprintCount || 0 }}</div>
                <div class="stat-label">足迹</div>
              </div>
            </div>

            <div class="balance-section">
              <div class="balance-label">账户余额</div>
              <div class="balance-amount">¥{{ formatPrice(userInfo.balance || 0) }}</div>
              <el-button type="primary" size="small" @click="handleRecharge" class="recharge-btn">
                立即充值
              </el-button>
            </div>

            <div class="quick-actions">
              <el-button type="primary" plain @click="$router.push('/user/orders')">
                <el-icon><List /></el-icon>
                我的订单
              </el-button>
              <el-button type="success" plain @click="$router.push('/user/cart')">
                <el-icon><ShoppingCart /></el-icon>
                购物车
              </el-button>
            </div>
          </div>
        </el-col>

        <!-- 右侧：信息编辑区域 -->
        <el-col :xs="24" :sm="16" :md="16" :lg="16">
          <div class="info-edit-section">
            <el-tabs v-model="activeTab" class="profile-tabs">
              <!-- 基本资料 -->
              <el-tab-pane label="基本资料" name="basic">
                <el-form
                  ref="basicFormRef"
                  :model="basicForm"
                  :rules="basicRules"
                  label-width="100px"
                  class="basic-form"
                >
                  <el-form-item label="用户昵称" prop="nickName">
                    <el-input
                      v-model="basicForm.nickName"
                      placeholder="请输入用户昵称"
                      maxlength="30"
                      show-word-limit
                    />
                  </el-form-item>

                  <el-form-item label="用户名" prop="userName">
                    <el-input
                      v-model="basicForm.userName"
                      placeholder="请输入用户名"
                      disabled
                    />
                    <span class="form-tip">用户名不可修改</span>
                  </el-form-item>

                  <el-form-item label="邮箱" prop="email">
                    <el-input
                      v-model="basicForm.email"
                      placeholder="请输入邮箱地址"
                      type="email"
                    />
                  </el-form-item>

                  <el-form-item label="手机号" prop="phonenumber">
                    <el-input
                      v-model="basicForm.phonenumber"
                      placeholder="请输入手机号码"
                      maxlength="11"
                    />
                  </el-form-item>

                  <el-form-item label="性别" prop="sex">
                    <el-radio-group v-model="basicForm.sex">
                      <el-radio label="0">男</el-radio>
                      <el-radio label="1">女</el-radio>
                      <el-radio label="2">未知</el-radio>
                    </el-radio-group>
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" @click="handleBasicSubmit" :loading="basicLoading">
                      保存修改
                    </el-button>
                    <el-button @click="resetBasicForm">重置</el-button>
                  </el-form-item>
                </el-form>
              </el-tab-pane>

              <!-- 安全设置 -->
              <el-tab-pane label="安全设置" name="security">
                <div class="security-section">
                  <div class="security-item">
                    <div class="security-info">
                      <div class="security-title">登录密码</div>
                      <div class="security-desc">定期更改密码可以提高账户安全性</div>
                    </div>
                    <el-button type="text" @click="openPasswordDialog">修改密码</el-button>
                  </div>

                  <div class="security-item">
                    <div class="security-info">
                      <div class="security-title">手机验证</div>
                      <div class="security-desc" v-if="userInfo.phonenumber">
                        已绑定手机：{{ userInfo.phonenumber }}
                      </div>
                      <div class="security-desc" v-else>未绑定手机号</div>
                    </div>
                    <el-button type="text" @click="handlePhoneBind">
                      {{ userInfo.phonenumber ? '更换手机' : '绑定手机' }}
                    </el-button>
                  </div>

                  <div class="security-item">
                    <div class="security-info">
                      <div class="security-title">邮箱验证</div>
                      <div class="security-desc" v-if="userInfo.email">
                        已绑定邮箱：{{ userInfo.email }}
                      </div>
                      <div class="security-desc" v-else>未绑定邮箱</div>
                    </div>
                    <el-button type="text" @click="handleEmailBind">
                      {{ userInfo.email ? '更换邮箱' : '绑定邮箱' }}
                    </el-button>
                  </div>
                </div>
              </el-tab-pane>

              <!-- 账户信息 -->
              <el-tab-pane label="账户信息" name="account">
                <div class="account-info">
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="用户ID">{{ userInfo.userId || userInfo.id }}</el-descriptions-item>
                    <el-descriptions-item label="用户名">{{ userInfo.userName }}</el-descriptions-item>
                    <el-descriptions-item label="用户类型">{{ formatUserType(userInfo.type) }}</el-descriptions-item>
                    <el-descriptions-item label="注册时间">{{ formatDate(userInfo.createTime) }}</el-descriptions-item>
                    <el-descriptions-item label="最后登录时间">{{ formatDate(userInfo.loginDate) }}</el-descriptions-item>
                    <el-descriptions-item label="最后登录IP">{{ userInfo.loginIp || '未知' }}</el-descriptions-item>
                    <el-descriptions-item label="账户状态">
                      <el-tag :type="userInfo.status === '0' ? 'success' : 'danger'">
                        {{ userInfo.status === '0' ? '正常' : '停用' }}
                      </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="账户余额">
                      <span class="balance-highlight">¥{{ formatPrice(userInfo.balance || 0) }}</span>
                    </el-descriptions-item>
                  </el-descriptions>
                  
                  <div class="account-actions">
                    <el-button type="primary" @click="handleRecharge">充值</el-button>
                    <el-button @click="handleWithdraw">提现</el-button>
                    <el-button @click="handleTransactionHistory">交易记录</el-button>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog
      title="更换头像"
      v-model="avatarDialogVisible"
      width="400px"
      :before-close="handleAvatarClose"
    >
      <div class="avatar-dialog">
        <div class="avatar-preview" v-if="avatarPreviewUrl">
          
        </div>
        <el-upload
          class="avatar-uploader"
          :action="uploadAction"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :on-success="handleAvatarSuccess"
          :on-error="handleAvatarError"
          :headers="uploadHeaders"
        >
          <el-icon v-if="!avatarPreviewUrl" class="avatar-uploader-icon"><Plus /></el-icon>
          <div v-else class="upload-again">
            <el-icon><Refresh /></el-icon>
            <span>重新选择</span>
          </div>
        </el-upload>
        <div class="upload-tips">
          <p>支持 JPG、PNG 格式，大小不超过 2MB</p>
          <p>建议尺寸：200×200 像素</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="avatarDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAvatar" :loading="avatarLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      title="修改密码"
      v-model="passwordDialogVisible"
      width="400px"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="80px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit" :loading="passwordLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Plus, Refresh, List, ShoppingCart } from '@element-plus/icons-vue'
import { getCurrentUser, updateUser, updateUserPwd, updateAvatar, getUserStats } from '@/api/system/customer'
import { getUserOrderStats, getUserFavoriteStats, getUserFootprintStats, getUserCartStats } from '@/api/system/stats'
import { getToken } from '@/utils/auth'

const store = useStore()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('basic')
const avatarDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const avatarLoading = ref(false)
const basicLoading = ref(false)
const passwordLoading = ref(false)

// 用户信息
const userInfo = ref({})
const userStats = ref({
  orderCount: 0,
  favoriteCount: 0,
  footprintCount: 0,
  cartCount: 0
})

// 表单引用
const basicFormRef = ref()
const passwordFormRef = ref()

// 表单数据
const basicForm = reactive({
  userId: '',
  nickName: '',
  userName: '',
  email: '',
  phonenumber: '',
  sex: '2'
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 头像预览
const avatarPreviewUrl = ref('')
const uploadFile = ref(null)

// 上传配置
const uploadAction = computed(() => `${import.meta.env.VITE_APP_BASE_API}/system/user/profile/avatar`)
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken()
}))

// 表单验证规则
const basicRules = {
  nickName: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' },
    { min: 2, max: 30, message: '昵称长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phonenumber: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const currentUserId = computed(() => {
  return store.state.user?.userId || localStorage.getItem('userId') || userInfo.value.userId
})

// 方法
const getAvatarUrl = (avatar) => {
  if (!avatar) {
    return '/src/assets/images/default-avatar.png'
  }
  if (avatar.startsWith('http') || avatar.startsWith('//')) {
    return avatar
  }
  return `${import.meta.env.VITE_APP_BASE_API}${avatar}`
}

const handleAvatarError = (event) => {
  event.target.src = '/src/assets/images/default-avatar.png'
}

const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

const formatDate = (dateString) => {
  if (!dateString) return '未知'
  try {
    return new Date(dateString).toLocaleString('zh-CN')
  } catch (error) {
    return dateString
  }
}

const formatUserType = (type) => {
  const typeMap = {
    '00': '系统用户',
    '01': '普通用户',
    '02': 'VIP用户',
    '03': '商家用户'
  }
  return typeMap[type] || '普通用户'
}

// 加载用户信息
const loadUserInfo = async () => {
  loading.value = true
  try {
    const response = await getCurrentUser()
    if (response.code === 200) {
      userInfo.value = response.data || response
      
      // 同步更新store中的用户信息
      if (store.state.user) {
        store.commit('SET_USER', userInfo.value)
      }
      
      // 填充表单数据
      Object.keys(basicForm).forEach(key => {
        if (userInfo.value[key] !== undefined) {
          basicForm[key] = userInfo.value[key]
        }
      })
      
      // 加载用户统计信息
      await loadUserStats()
    } else {
      ElMessage.error(response.msg || '获取用户信息失败')
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

// 加载用户统计信息
const loadUserStats = async () => {
  try {
    const userId = currentUserId.value
    if (!userId) return

    // 并行加载所有统计信息
    const [orderStats, favoriteStats, footprintStats, cartStats] = await Promise.all([
      getUserOrderStats(userId),
      getUserFavoriteStats(userId),
      getUserFootprintStats(userId),
      getUserCartStats(userId)
    ])

    userStats.value = {
      orderCount: orderStats.code === 200 ? (orderStats.data || 0) : 0,
      favoriteCount: favoriteStats.code === 200 ? (favoriteStats.data || 0) : 0,
      footprintCount: footprintStats.code === 200 ? (footprintStats.data || 0) : 0,
      cartCount: cartStats.code === 200 ? (cartStats.data || 0) : 0
    }
  } catch (error) {
    console.error('加载用户统计信息失败:', error)
    // 如果接口不存在，使用模拟数据
    userStats.value = {
      orderCount: 0,
      favoriteCount: 0,
      footprintCount: 0,
      cartCount: 0
    }
  }
}

// 处理基本资料提交
const handleBasicSubmit = async () => {
  if (!basicFormRef.value) return
  
  try {
    await basicFormRef.value.validate()
    basicLoading.value = true
    
    const updateData = {
      ...basicForm,
      userId: currentUserId.value
    }
    
    const response = await updateUser(updateData)
    if (response.code === 200) {
      ElMessage.success('资料更新成功')
      // 重新加载用户信息
      await loadUserInfo()
    } else {
      ElMessage.error(response.msg || '资料更新失败')
    }
  } catch (error) {
    console.error('更新资料失败:', error)
    ElMessage.error('更新资料失败')
  } finally {
    basicLoading.value = false
  }
}

// 重置基本资料表单
const resetBasicForm = () => {
  Object.keys(basicForm).forEach(key => {
    if (userInfo.value[key] !== undefined) {
      basicForm[key] = userInfo.value[key]
    }
  })
}

// 打开头像上传对话框
const openAvatarDialog = () => {
  avatarPreviewUrl.value = userInfo.value.avatar ? getAvatarUrl(userInfo.value.avatar) : ''
  avatarDialogVisible.value = true
}

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  
  uploadFile.value = file
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreviewUrl.value = e.target.result
  }
  reader.readAsDataURL(file)
  
  return false // 手动上传
}

// 提交头像
const submitAvatar = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请选择要上传的头像')
    return
  }
  
  try {
    avatarLoading.value = true
    const formData = new FormData()
    formData.append('avatar', uploadFile.value)
    
    const response = await updateAvatar(formData)
    if (response.code === 200) {
      ElMessage.success('头像更新成功')
      avatarDialogVisible.value = false
      uploadFile.value = null
      await loadUserInfo()
    } else {
      ElMessage.error(response.msg || '头像更新失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    avatarLoading.value = false
  }
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success('头像上传成功')
    avatarDialogVisible.value = false
    loadUserInfo()
  } else {
    ElMessage.error(response.msg || '头像上传失败')
  }
}

// 关闭头像对话框
const handleAvatarClose = (done) => {
  avatarPreviewUrl.value = ''
  uploadFile.value = null
  done()
}

// 打开修改密码对话框
const openPasswordDialog = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

// 处理密码修改提交
const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    
    const response = await updateUserPwd(passwordForm.oldPassword, passwordForm.newPassword)
    if (response.code === 200) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
      // 清空表单
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.error(response.msg || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败')
  } finally {
    passwordLoading.value = false
  }
}

// 处理手机绑定
const handlePhoneBind = () => {
  ElMessage.info('手机绑定功能开发中')
}

// 处理邮箱绑定
const handleEmailBind = () => {
  ElMessage.info('邮箱绑定功能开发中')
}

// 处理充值
const handleRecharge = () => {
  router.push('/recharge')
}

// 处理提现
const handleWithdraw = () => {
  ElMessage.info('提现功能开发中')
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.user-profile {
  padding: 0;
}

.profile-content {
  min-height: 500px;
}

/* 个人信息卡片样式 */
.profile-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 30px 20px;
  text-align: center;
  height: fit-content;
  position: sticky;
  top: 20px;
}

.avatar-section {
  margin-bottom: 25px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 15px;
}

.avatar-image {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 40px;
  font-weight: bold;
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #409eff;
  color: white;
  border-radius: 20px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.avatar-upload:hover {
  background: #66b1ff;
  transform: translateY(-2px);
}

.user-name {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.user-role {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #666;
}

.user-id {
  margin: 0;
  font-size: 12px;
  color: #999;
}

/* 统计信息样式 */
.stats-section {
  display: flex;
  justify-content: space-around;
  margin: 25px 0;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 5px;
  border-radius: 6px;
}

.stat-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

/* 余额样式 */
.balance-section {
  margin: 25px 0;
  padding: 20px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 8px;
  color: white;
}

.balance-label {
  font-size: 14px;
  margin-bottom: 8px;
  opacity: 0.9;
}

.balance-amount {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 15px;
}

.recharge-btn {
  width: 100%;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  transition: all 0.3s;
}

.recharge-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

/* 快捷操作样式 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 20px;
}

.quick-actions .el-button {
  justify-content: center;
}

/* 信息编辑区域样式 */
.info-edit-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
  min-height: 500px;
}

.profile-tabs {
  min-height: 400px;
}

:deep(.profile-tabs .el-tabs__header) {
  margin-bottom: 30px;
}

:deep(.profile-tabs .el-tabs__nav-wrap::after) {
  background-color: #f0f0f0;
}

:deep(.profile-tabs .el-tabs__active-bar) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.basic-form {
  max-width: 500px;
  margin-top: 10px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

/* 安全设置样式 */
.security-section {
  padding: 10px 0;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.security-item:hover {
  background-color: #fafafa;
  padding-left: 10px;
  padding-right: 10px;
  border-radius: 6px;
}

.security-item:last-child {
  border-bottom: none;
}

.security-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.security-desc {
  font-size: 14px;
  color: #666;
}

/* 账户信息样式 */
.account-info {
  padding: 10px 0;
}

.balance-highlight {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.account-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 头像上传对话框样式 */
.avatar-dialog {
  text-align: center;
}

.avatar-preview {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  margin: 0 auto 20px;
  border: 3px solid #f0f0f0;
}

.avatar-uploader {
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  width: 150px;
  height: 150px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.upload-again {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-tips {
  text-align: center;
  color: #999;
  font-size: 12px;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-card {
    position: static;
    margin-bottom: 20px;
  }
  
  .stats-section {
    padding: 15px 0;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .balance-amount {
    font-size: 24px;
  }
  
  .info-edit-section {
    padding: 15px;
  }
  
  .basic-form {
    max-width: 100%;
  }
  
  :deep(.basic-form .el-form-item__label) {
    width: 80px !important;
  }
  
  :deep(.basic-form .el-form-item__content) {
    margin-left: 80px !important;
  }
  
  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .account-actions {
    justify-content: center;
  }
  
  .account-actions .el-button {
    flex: 1;
    min-width: 100px;
  }
}

@media (max-width: 480px) {
  .profile-card {
    padding: 20px 15px;
  }
  
  .avatar-image {
    width: 80px;
    height: 80px;
    font-size: 32px;
  }
  
  .avatar-upload {
    padding: 3px 6px;
    font-size: 11px;
  }
  
  .user-name {
    font-size: 16px;
  }
  
  .stats-section {
    margin: 15px 0;
  }
  
  .stat-value {
    font-size: 18px;
  }
  
  .balance-section {
    margin: 15px 0;
    padding: 15px;
  }
  
  .balance-amount {
    font-size: 22px;
  }
  
  .quick-actions {
    gap: 8px;
  }
  
  .info-edit-section {
    padding: 10px;
  }
  
  :deep(.profile-tabs .el-tabs__item) {
    padding: 0 12px;
    font-size: 14px;
  }
}

/* 动画效果 */
.profile-card {
  animation: fadeInUp 0.6s ease-out;
}

.info-edit-section {
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 加载状态样式 */
:deep(.el-loading-mask) {
  border-radius: 8px;
}

/* 表单元素样式优化 */
:deep(.el-input .el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.el-input .el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

:deep(.el-input .el-input__wrapper.is-focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

:deep(.el-radio-group) {
  display: flex;
  gap: 20px;
}

:deep(.el-radio) {
  margin-right: 0;
}

/* 按钮样式优化 */
:deep(.el-button) {
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
}

:deep(.el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

/* 标签样式优化 */
:deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
}

/* 描述列表样式优化 */
:deep(.el-descriptions) {
  margin-top: 10px;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  color: #666;
}

:deep(.el-descriptions__content) {
  color: #333;
}

/* 对话框样式优化 */
:deep(.el-dialog) {
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
  border-bottom: 1px solid #f0f0f0;
  margin-right: 0;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #333;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
  border-top: 1px solid #f0f0f0;
}

/* 上传组件样式优化 */
:deep(.el-upload) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 滚动条样式优化 */
:deep(::-webkit-scrollbar) {
  width: 6px;
  height: 6px;
}

:deep(::-webkit-scrollbar-track) {
  background: #f1f1f1;
  border-radius: 3px;
}

:deep(::-webkit-scrollbar-thumb) {
  background: #c1c1c1;
  border-radius: 3px;
}

:deep(::-webkit-scrollbar-thumb:hover) {
  background: #a8a8a8;
}

/* 响应式工具类 */
.mobile-only {
  display: none;
}

.desktop-only {
  display: block;
}

@media (max-width: 768px) {
  .mobile-only {
    display: block;
  }
  
  .desktop-only {
    display: none;
  }
}

/* 打印样式 */
@media print {
  .profile-card,
  .info-edit-section {
    box-shadow: none;
    border: 1px solid #ddd;
  }
  
  .quick-actions,
  .account-actions {
    display: none;
  }
}

/* 高对比度模式支持 */
@media (prefers-contrast: high) {
  .profile-card {
    border: 2px solid #000;
  }
  
  .stat-value {
    color: #000;
  }
  
  .balance-section {
    background: #000;
    color: #fff;
  }
}

/* 减少动画模式支持 */
@media (prefers-reduced-motion: reduce) {
  .profile-card,
  .info-edit-section {
    animation: none;
  }
  
  :deep(.el-button:hover) {
    transform: none;
  }
  
  .stat-item:hover {
    transform: none;
  }
}
</style>