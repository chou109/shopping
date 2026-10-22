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
                <img :src="avatarUrl" alt="头像" class="user-avatar-img" />
                <button type="button" class="avatar-upload" aria-label="更换头像" @click="openAvatarDialog">
                  <el-icon>
                    <Camera />
                  </el-icon>
                  <span>更换</span>
                </button>
              </div>
              <h3 class="user-name">{{ userInfo.nickName || userInfo.userName || '用户' }}</h3>
              <p class="user-role">{{ formatUserType(userInfo.type) }}</p>
              <p class="user-id">ID: {{ userInfo.userId || userInfo.id }}</p>
            </div>

            <div class="stats-section">
              <router-link class="stat-item" to="/user/orders" aria-label="查看我的订单">
                <div class="stat-value">{{ userStats.orderCount ?? '—' }}</div>
                <div class="stat-label">订单</div>
              </router-link>
              <router-link class="stat-item" to="/user/favoriteProduct" aria-label="查看收藏商品">
                <div class="stat-value">{{ userStats.favoriteCount ?? '—' }}</div>
                <div class="stat-label">收藏</div>
              </router-link>
              <router-link class="stat-item" to="/user/footprint" aria-label="查看浏览足迹">
                <div class="stat-value">{{ userStats.footprintCount ?? '—' }}</div>
                <div class="stat-label">足迹</div>
              </router-link>
            </div>

            <div class="balance-section">
              <div class="balance-label">账户余额</div>
              <div class="balance-amount">¥{{ formatPrice(userInfo.balance || 0) }}</div>
              <el-button type="danger" round size="small" disabled class="recharge-btn">
                充值暂未开通
              </el-button>
            </div>

            <div class="quick-actions">
              <el-button type="danger" plain round @click="$router.push('/user/orders')">
                <el-icon>
                  <List />
                </el-icon>
                我的订单
              </el-button>
              <br>
              <el-button style="margin-top: -10px;" type="warning" plain round @click="$router.push('/user/cart')">
                <el-icon>
                  <ShoppingCart />
                </el-icon>
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
                <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="100px"
                  class="basic-form">
                  <el-form-item label="用户昵称" prop="nickName">
                    <el-input v-model="basicForm.nickName" placeholder="请输入用户昵称" maxlength="30" show-word-limit />
                  </el-form-item>

                  <el-form-item label="用户名" prop="userName">
                    <el-input v-model="basicForm.userName" placeholder="请输入用户名" disabled />
                    <span class="form-tip">用户名不可修改</span>
                  </el-form-item>

                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="basicForm.email" placeholder="请输入邮箱地址" type="email" />
                  </el-form-item>

                  <el-form-item label="手机号" prop="phonenumber">
                    <el-input v-model="basicForm.phonenumber" placeholder="请输入手机号码" maxlength="11" />
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
                </div>
              </el-tab-pane>

              <!-- 地址管理 -->
              <el-tab-pane label="地址管理" name="address">
                <div class="address-section" v-loading="addressListLoading">
                  <div class="address-header">
                    <h3>我的收货地址</h3>
                    <el-button type="primary" @click="openAddAddressDialog">
                      <el-icon>
                        <Plus />
                      </el-icon>
                      新增地址
                    </el-button>
                  </div>

                  <el-alert v-if="addressListError" :title="addressListError" type="error" :closable="false" show-icon>
                    <template #default>
                      <el-button link type="primary" :disabled="addressListLoading" @click="loadAddressList">重试加载地址</el-button>
                    </template>
                  </el-alert>

                  <div class="address-list" v-if="addressList.length > 0">
                    <div v-for="address in addressList" :key="address.id" class="address-item"
                      :class="{ 'default-address': address.defaultFlag === 1 }">
                      <div class="address-info">
                        <div class="address-header-info">
                          <span class="receive-name">{{ address.receiveName }}</span>
                          <span class="receive-phone">{{ address.receivePhone }}</span>
                          <el-tag v-if="address.defaultFlag === 1" type="success" size="small">默认</el-tag>
                          <el-tag type="info" size="small">{{ getAddressLabelName(address.addressLabel) }}</el-tag>
                        </div>
                        <div class="full-address">{{ formatFullAddress(address) }}</div>
                      </div>
                      <div class="address-actions">
                        <el-button v-if="address.defaultFlag !== 1" type="text"
                          :loading="addressMutations.has(address.id)"
                          :disabled="addressMutations.has(address.id)"
                          @click="handleSetDefaultAddress(address.id)">
                          设为默认
                        </el-button>
                        <el-button type="text" @click="openEditAddressDialog(address)">编辑</el-button>
                        <el-button type="text" @click="handleDeleteAddress(address.id)"
                          :loading="addressMutations.has(address.id)"
                          :disabled="address.defaultFlag === 1 || addressMutations.has(address.id)">
                          删除
                        </el-button>
                      </div>
                    </div>
                  </div>

                  <div v-else-if="!addressListLoading && !addressListError" class="empty-address">
                    <el-empty description="暂无收货地址">
                      <el-button type="primary" @click="openAddAddressDialog">添加地址</el-button>
                    </el-empty>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog title="更换头像" v-model="avatarDialogVisible" width="400px" :before-close="handleAvatarClose" @close="clearAvatarPreview">
      <div class="avatar-dialog">
        <div class="avatar-preview" v-if="avatarPreviewUrl">
          <img :src="avatarPreviewUrl" alt="头像预览" />
        </div>
        <el-upload ref="avatarUploadRef" class="avatar-uploader" :action="uploadAction" name="avatarfile"
          :auto-upload="false" :show-file-list="false"
          :before-upload="beforeAvatarUpload" :on-success="handleAvatarSuccess" :on-error="handleAvatarError"
          :headers="uploadHeaders">
          <el-icon v-if="!avatarPreviewUrl" class="avatar-uploader-icon">
            <Plus />
          </el-icon>
          <div v-else class="upload-again">
            <el-icon>
              <Refresh />
            </el-icon>
            <span>重新选择</span>
          </div>
        </el-upload>
        <div class="upload-tips">
          <p>支持 JPG、PNG 格式，大小不超过 2MB</p>
          <p>建议尺寸：200×200 像素</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="handleAvatarCancel">取消</el-button>
        <el-button type="primary" @click="submitAvatar" :loading="avatarLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" v-model="passwordDialogVisible" width="400px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit" :loading="passwordLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 地址编辑对话框 -->
    <el-dialog :title="isEditAddress ? '编辑地址' : '新增地址'" v-model="addressDialogVisible" width="600px"
      :close-on-click-modal="!addressLoading" :close-on-press-escape="!addressLoading"
      :show-close="!addressLoading" @close="handleAddressDialogClose">
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收货人" prop="receiveName">
              <el-input v-model="addressForm.receiveName" placeholder="请输入收货人姓名" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="receivePhone">
              <el-input v-model="addressForm.receivePhone" placeholder="请输入手机号码" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="省级" prop="provinceCode">
              <el-select v-model="addressForm.provinceCode" placeholder="请选择省级" filterable clearable
                :loading="provinceLoading" @change="handleProvinceChange">
                <el-option v-for="province in provinces" :key="province.code" :label="province.name"
                  :value="province.code" />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 非直辖市显示地级选择 -->
          <el-col :span="8" v-if="!isMunicipality(addressForm.provinceCode)">
            <el-form-item label="地级" prop="cityCode">
              <el-select v-model="addressForm.cityCode" placeholder="请选择地级" filterable clearable :loading="cityLoading"
                :disabled="!addressForm.provinceCode" @change="handleCityChange">
                <el-option v-for="city in cities" :key="city.code" :label="city.name" :value="city.code" />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 直辖市时显示提示 -->
          <el-col :span="8" v-else-if="addressForm.provinceCode">
            <el-form-item label="地级">
              <el-tag type="info" style="width: 100%; text-align: center;">
                直辖市，无需选择地级
              </el-tag>
            </el-form-item>
          </el-col>

          <!-- 区域选择器 -->
          <el-col :span="8">
            <el-form-item label="县级" prop="areaCode">
              <el-select v-model="addressForm.areaCode" placeholder="请选择县级" filterable clearable :loading="areaLoading"
                :disabled="!addressForm.provinceCode || (!isMunicipality(addressForm.provinceCode) && !addressForm.cityCode)"
                @change="handleAreaChange">
                <el-option v-for="area in areas" :key="area.code" :label="area.name" :value="area.code" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-alert v-if="regionLoadError" :title="regionLoadError" type="error" :closable="false" show-icon>
        </el-alert>
        <el-button v-if="regionLoadError" link type="primary" @click="retryRegionLoad">重试加载地区</el-button>

        <el-form-item label="详细地址" prop="street">
          <el-input v-model="addressForm.street" type="textarea" :rows="3" placeholder="请输入详细地址（街道、门牌号等）"
            maxlength="200" show-word-limit />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="地址标签">
              <el-select v-model="addressForm.addressLabel" placeholder="请选择标签">
                <el-option v-for="option in addressLabelOptions" :key="option.value" :label="option.label"
                  :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮编">
              <el-input v-model="addressForm.postalCode" placeholder="请输入邮编" maxlength="10" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="默认地址">
          <el-checkbox v-model="addressForm.defaultFlag" :true-label="1" :false-label="0">
            设为默认收货地址
          </el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button :disabled="addressLoading" @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddressSubmit" :loading="addressLoading" :disabled="addressLoading">
          {{ isEditAddress ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera, Plus, Refresh, List, ShoppingCart } from '@element-plus/icons-vue'
import { getUserProfile, updateUserProfile, updateUserPwd } from '@/api/system/user'
import { getToken } from '@/utils/auth'
import { DEFAULT_AVATAR_IMAGE, getProductImageUrl } from '@/utils/image'
import { getFavoriteStats } from '@/api/system/favorite'
import { getFootprintCount, getOrderCount } from '@/api/system/stats'
import { useProfileAddresses } from '@/composables/useProfileAddresses.mjs'
import { isMunicipality } from '@/utils/profile.mjs'

const store = useStore()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('basic')
const avatarDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const avatarLoading = ref(false)
const basicLoading = ref(false)
const passwordLoading = ref(false)
const currentUserId = ref('')

// 用户信息
const userInfo = ref({})
const userStats = ref({
  orderCount: null,
  favoriteCount: null,
  footprintCount: null
})

// 头像URL计算属性
const avatarUrl = computed(() => {
  if (userInfo.value.avatar) {
    return getProductImageUrl(userInfo.value.avatar)
  }
  return DEFAULT_AVATAR_IMAGE
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
const avatarUploadRef = ref()
let avatarObjectUrl = ''

const clearAvatarPreview = () => {
  if (avatarObjectUrl) {
    URL.revokeObjectURL(avatarObjectUrl)
    avatarObjectUrl = ''
  }
  avatarPreviewUrl.value = ''
}

// 上传配置
const uploadAction = computed(() => `${import.meta.env.VITE_APP_BASE_API}/system/user/profile/avatar`)
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken()
}))

const formatUserType = (type) => type || '用户'
const formatPrice = (price) => {
  const amount = Number(price)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}
const formatDate = (value) => {
  if (!value) return '未知'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '未知' : date.toLocaleString('zh-CN')
}

const loadUserInfo = async () => {
  loading.value = true
  try {
    const response = await getUserProfile()
    const profile = response.data || response.user || {}
    userInfo.value = profile
    currentUserId.value = profile.userId || store.state.user.userId || ''
    Object.assign(basicForm, {
      userId: currentUserId.value,
      nickName: profile.nickName || '',
      userName: profile.userName || '',
      email: profile.email || '',
      phonenumber: profile.phonenumber || '',
      sex: String(profile.sex ?? '2')
    })
    if (currentUserId.value) {
      store.commit('SET_USERID', currentUserId.value)
    }
    await Promise.all([
      loadProfileStats(),
      loadAddressList()
    ])
  } catch (error) {
    console.error('加载个人资料失败:', error)
    ElMessage.error('加载个人资料失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const loadProfileStats = async () => {
  userStats.value = { orderCount: null, favoriteCount: null, footprintCount: null }
  const requests = [getOrderCount(), getFavoriteStats(), getFootprintCount()]
  const results = await Promise.allSettled(requests)
  const countFrom = (result) => {
    if (result.status !== 'fulfilled') return null
    const value = result.value?.data
    const count = typeof value === 'number' || typeof value === 'string'
      ? value
      : value?.count ?? value?.total ?? value?.totalCount
    if (count === null || count === undefined || count === '') return null
    const parsedCount = Number(count)
    return Number.isFinite(parsedCount) && parsedCount >= 0 ? parsedCount : null
  }
  userStats.value = {
    orderCount: countFrom(results[0]),
    favoriteCount: countFrom(results[1]),
    footprintCount: countFrom(results[2])
  }
}

const handleBasicSubmit = async () => {
  if (!basicFormRef.value || basicLoading.value) return
  basicLoading.value = true
  try {
    await basicFormRef.value.validate()
    const response = await updateUserProfile({ ...basicForm })
    if (response.code !== 200) throw new Error(response.msg || '资料保存失败')
    userInfo.value = { ...userInfo.value, ...basicForm }
    store.commit('SET_REALNAME', basicForm.nickName)
    ElMessage.success('个人资料已保存')
  } catch (error) {
    if (error?.message) {
      console.error('保存个人资料失败:', error)
      ElMessage.error(error.message || '个人资料保存失败')
    }
  } finally {
    basicLoading.value = false
  }
}

const resetBasicForm = () => {
  Object.assign(basicForm, {
    userId: currentUserId.value,
    nickName: userInfo.value.nickName || '',
    userName: userInfo.value.userName || '',
    email: userInfo.value.email || '',
    phonenumber: userInfo.value.phonenumber || '',
    sex: String(userInfo.value.sex ?? '2')
  })
  basicFormRef.value?.clearValidate()
}

const openPasswordDialog = () => {
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  passwordDialogVisible.value = true
}

const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value || passwordLoading.value) return
  passwordLoading.value = true
  try {
    await passwordFormRef.value.validate()
    const response = await updateUserPwd(passwordForm.oldPassword, passwordForm.newPassword)
    if (response.code !== 200) throw new Error(response.msg || '密码修改失败')
    passwordDialogVisible.value = false
    Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
    ElMessage.success('密码修改成功')
  } catch (error) {
    if (error?.message) {
      console.error('修改密码失败:', error)
      ElMessage.error(error.message || '密码修改失败')
    }
  } finally {
    passwordLoading.value = false
  }
}

const openAvatarDialog = () => {
  clearAvatarPreview()
  uploadFile.value = null
  avatarDialogVisible.value = true
}

const handleAvatarClose = (done) => {
  if (avatarLoading.value) return
  clearAvatarPreview()
  uploadFile.value = null
  done()
}

const handleAvatarCancel = () => {
  if (avatarLoading.value) return
  clearAvatarPreview()
  uploadFile.value = null
  avatarDialogVisible.value = false
}

const beforeAvatarUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png'].includes(file.type)
  const isSmallEnough = file.size / 1024 / 1024 <= 2
  if (!isImage) ElMessage.error('头像只支持 JPG 或 PNG 格式')
  if (!isSmallEnough) ElMessage.error('头像大小不能超过 2MB')
  if (isImage && isSmallEnough) {
    uploadFile.value = file
    clearAvatarPreview()
    avatarObjectUrl = URL.createObjectURL(file)
    avatarPreviewUrl.value = avatarObjectUrl
  }
  return isImage && isSmallEnough
}

const handleAvatarSuccess = (response) => {
  avatarLoading.value = false
  if (response?.code !== 200 || !response.imgUrl) {
    handleAvatarError(new Error(response?.msg || '头像上传失败'))
    return
  }
  userInfo.value = { ...userInfo.value, avatar: response.imgUrl }
  store.commit('SET_AVATAR', getProductImageUrl(response.imgUrl))
  avatarDialogVisible.value = false
  clearAvatarPreview()
  uploadFile.value = null
  ElMessage.success('头像已更新')
}

const handleAvatarError = (error) => {
  avatarLoading.value = false
  console.error('上传头像失败:', error)
  ElMessage.error('头像上传失败，请稍后重试')
}

const submitAvatar = async () => {
  if (!uploadFile.value) {
    ElMessage.warning('请先选择头像图片')
    return
  }
  avatarLoading.value = true
  avatarUploadRef.value?.submit()
}

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


const {
  addressDialogVisible,
  addressLoading,
  addressMutations,
  addressListLoading,
  addressListError,
  isEditAddress,
  currentAddressId,
  addressList,
  addressForm,
  addressRules,
  addressLabelOptions,
  addressFormRef,
  provinceLoading,
  cityLoading,
  areaLoading,
  regionLoadError,
  provinces,
  cities,
  areas,
  loadAddressList,
  openAddAddressDialog,
  openEditAddressDialog,
  handleProvinceChange,
  handleCityChange,
  handleAreaChange,
  retryRegionLoad,
  handleAddressSubmit,
  handleDeleteAddress,
  handleSetDefaultAddress,
  getAddressLabelName,
  formatFullAddress,
  handleAddressDialogClose
} = useProfileAddresses()

onMounted(() => {
  loadUserInfo()
})

onBeforeUnmount(clearAvatarPreview)
</script>

<style scoped lang="scss">
@use "./UserProfile.scss";
</style>
