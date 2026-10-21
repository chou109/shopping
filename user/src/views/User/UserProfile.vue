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
                <div class="address-section">
                  <div class="address-header">
                    <h3>我的收货地址</h3>
                    <el-button type="primary" @click="openAddAddressDialog">
                      <el-icon>
                        <Plus />
                      </el-icon>
                      新增地址
                    </el-button>
                  </div>

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
                          @click="handleSetDefaultAddress(address.id)">
                          设为默认
                        </el-button>
                        <el-button type="text" @click="openEditAddressDialog(address)">编辑</el-button>
                        <el-button type="text" @click="handleDeleteAddress(address.id)"
                          :disabled="address.defaultFlag === 1">
                          删除
                        </el-button>
                      </div>
                    </div>
                  </div>

                  <div v-else class="empty-address">
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
    <el-dialog title="更换头像" v-model="avatarDialogVisible" width="400px" :before-close="handleAvatarClose">
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
        <el-button @click="avatarDialogVisible = false">取消</el-button>
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
      @close="handleAddressDialogClose">
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
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddressSubmit" :loading="addressLoading">
          {{ isEditAddress ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, watch, nextTick } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, Plus, Refresh, List, ShoppingCart } from '@element-plus/icons-vue'
import { getUserProfile, updateUserProfile, updateUserPwd } from '@/api/system/user'
import { getToken } from '@/utils/auth'
import { DEFAULT_AVATAR_IMAGE, getProductImageUrl } from '@/utils/image'
import { getFavoriteStats } from '@/api/system/favorite'
import { getFootprintCount, getOrderCount } from '@/api/system/stats'
import { AreaDataService } from '@/utils/areaService.mjs'
// 导入地址管理相关的API
import {
  getMyAddressList,
  addAddress,
  updateAddress,
  delAddress,
  setDefaultAddress
} from '@/api/system/address'

const store = useStore()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('basic')
const avatarDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const avatarLoading = ref(false)
const basicLoading = ref(false)
const passwordLoading = ref(false)
const isLoadingEditData = ref(false) // 标记是否正在加载编辑数据
const currentUserId = ref('')
const provinceLoading = ref(false)
const cityLoading = ref(false)
const areaLoading = ref(false)
const regionLoadError = ref('')
const provinces = ref([])
const cities = ref([])
const areas = ref([])

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
const addressFormRef = ref()

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
  try {
    await basicFormRef.value.validate()
    basicLoading.value = true
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
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
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

// 地址管理相关变量
const addressDialogVisible = ref(false)
const addressLoading = ref(false)
const isEditAddress = ref(false)
const currentAddressId = ref(null)

// 地址列表数据
const addressList = ref([])
const defaultAddress = ref(null)

// 地址表单数据
const addressForm = reactive({
  addressId: null,
  receiveName: '',
  receivePhone: '',
  province: '',
  provinceCode: '',
  city: '',
  cityCode: '',
  area: '',
  areaCode: '',
  street: '',
  postalCode: '',
  addressLabel: 1,
  defaultFlag: 0,
  userId: null
})

// 地址验证规则
const addressRules = {
  receiveName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 30, message: '姓名长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  receivePhone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  provinceCode: [
    { required: true, message: '请选择省级', trigger: 'change' }
  ],
  cityCode: [
    {
      validator: (rule, value, callback) => {
        // 直辖市不需要选择地级
        if (isMunicipality(addressForm.provinceCode)) {
          callback()
        } else if (!value) {
          callback(new Error('请选择地级'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  areaCode: [
    { required: true, message: '请选择县级', trigger: 'change' }
  ],
  street: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 200, message: '详细地址长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 地址标签选项
const addressLabelOptions = [
  { value: 1, label: '家' },
  { value: 2, label: '公司' },
  { value: 3, label: '学校' },
  { value: 4, label: '其他' }
]

const areaService = new AreaDataService()
let provinceRequestId = 0
let cityRequestId = 0

// 加载省份数据
const loadProvinces = async () => {
  provinceLoading.value = true
  try {
    provinces.value = await areaService.getProvinces()

  } catch (error) {
    console.error('加载省份数据失败:', error)
    ElMessage.error('加载省份数据失败')
  } finally {
    provinceLoading.value = false
  }
}

// 直辖市代码列表
const municipalityCodes = ['110000', '120000', '310000', '500000']; // 北京、天津、上海、重庆

// 判断是否为直辖市
const isMunicipality = (provinceCode) => {
  return municipalityCodes.includes(provinceCode);
};

// 省份改变时加载城市
const handleProvinceChange = async (provinceCode) => {
  regionLoadError.value = ''
  const requestId = ++provinceRequestId
  // 省份变化后，之前按旧省份发出的城市/区县请求不能再写入表单。
  cityRequestId++

  // 确保 provinceCode 是字符串而不是数组
  if (Array.isArray(provinceCode)) {
    provinceCode = provinceCode[0];
  }

  // 清空下级数据
  addressForm.cityCode = '';
  addressForm.areaCode = '';
  addressForm.city = '';
  addressForm.area = '';
  cities.value = [];
  areas.value = [];

  if (!provinceCode) return;

  // 设置省份名称
  const province = provinces.value.find(p => p.code === provinceCode);
  if (province) {
    addressForm.province = province.name;
    addressForm.provinceCode = provinceCode;
  }

  // 如果是直辖市，直接加载区县数据，跳过地级市
  if (isMunicipality(provinceCode)) {

    // 直辖市没有地级市，直接设置city为直辖市名称
    addressForm.city = province.name;
    addressForm.cityCode = provinceCode; // 使用省份代码作为城市代码

    // 直接加载区县数据
    areaLoading.value = true;
    try {
      // 直辖市的数据通常挂在省级代码下
      areas.value = await areaService.getAreas(provinceCode);
      if (requestId !== provinceRequestId || addressForm.provinceCode !== provinceCode) return
    } catch (error) {
      if (requestId !== provinceRequestId || addressForm.provinceCode !== provinceCode) return
      console.error('加载直辖市区县数据失败:', error);
      // 尝试备用方案：使用直辖市的标准区县代码
      areas.value = await loadMunicipalityAreas(provinceCode);
      if (areas.value.length === 0) {
        regionLoadError.value = '地区数据服务暂不可用，请检查网络后重试。'
      }
    } finally {
      if (requestId === provinceRequestId) areaLoading.value = false;
    }
  } else {
    // 非直辖市，正常加载城市数据
    cityLoading.value = true;
    try {
      cities.value = await areaService.getCities(provinceCode);
      if (requestId !== provinceRequestId || addressForm.provinceCode !== provinceCode) return
    } catch (error) {
      if (requestId !== provinceRequestId || addressForm.provinceCode !== provinceCode) return
      console.error('加载城市失败:', error);
      regionLoadError.value = '城市数据加载失败，请检查网络后重试。'
    } finally {
      if (requestId === provinceRequestId) cityLoading.value = false;
    }
  }
};

// 城市改变时加载区县
const handleCityChange = async (cityCode) => {
  regionLoadError.value = ''
  const requestId = ++cityRequestId

  // 确保 cityCode 是字符串而不是数组
  if (Array.isArray(cityCode)) {
    cityCode = cityCode[0];
  }

  // 清空区域数据
  addressForm.areaCode = '';
  addressForm.area = '';
  areas.value = [];

  if (!cityCode) return;

  // 设置城市名称
  const city = cities.value.find(c => c.code === cityCode);
  if (city) {
    addressForm.city = city.name;
    addressForm.cityCode = cityCode;
  }

  // 如果是直辖市，已经在上一步处理了区县数据，这里不需要重复加载
  const currentProvinceCode = addressForm.provinceCode;
  if (currentProvinceCode && isMunicipality(currentProvinceCode)) {
    return;
  }

  // 非直辖市，正常加载区域
  areaLoading.value = true;
  try {
    areas.value = await areaService.getAreas(cityCode);
    if (requestId !== cityRequestId || addressForm.cityCode !== cityCode) return
  } catch (error) {
    if (requestId !== cityRequestId || addressForm.cityCode !== cityCode) return
    console.error('加载区域失败:', error);
    regionLoadError.value = '区县数据加载失败，请检查网络后重试。'
  } finally {
    if (requestId === cityRequestId) areaLoading.value = false;
  }
};

const retryRegionLoad = async () => {
  if (!addressForm.provinceCode) return
  if (cityLoading.value || areaLoading.value) return
  regionLoadError.value = ''
  if (isEditAddress.value) {
    await loadEditAddressData({ ...addressForm })
  } else {
    await handleProvinceChange(addressForm.provinceCode)
  }
}

// 添加直辖市区县数据备用加载函数
const loadMunicipalityAreas = async (provinceCode) => {
  // 直辖市的标准区县数据
  const municipalityAreas = {
    '110000': [ // 北京
      { code: '110101', name: '东城区' }, { code: '110102', name: '西城区' },
      { code: '110105', name: '朝阳区' }, { code: '110106', name: '丰台区' },
      { code: '110107', name: '石景山区' }, { code: '110108', name: '海淀区' },
      { code: '110109', name: '门头沟区' }, { code: '110111', name: '房山区' },
      { code: '110112', name: '通州区' }, { code: '110113', name: '顺义区' },
      { code: '110114', name: '昌平区' }, { code: '110115', name: '大兴区' },
      { code: '110116', name: '怀柔区' }, { code: '110117', name: '平谷区' },
      { code: '110118', name: '密云区' }, { code: '110119', name: '延庆区' }
    ],
    '120000': [ // 天津
      { code: '120101', name: '和平区' }, { code: '120102', name: '河东区' },
      { code: '120103', name: '河西区' }, { code: '120104', name: '南开区' },
      { code: '120105', name: '河北区' }, { code: '120106', name: '红桥区' },
      { code: '120110', name: '东丽区' }, { code: '120111', name: '西青区' },
      { code: '120112', name: '津南区' }, { code: '120113', name: '北辰区' },
      { code: '120114', name: '武清区' }, { code: '120115', name: '宝坻区' },
      { code: '120116', name: '滨海新区' }, { code: '120117', name: '宁河区' },
      { code: '120118', name: '静海区' }, { code: '120119', name: '蓟州区' }
    ],
    '310000': [ // 上海
      { code: '310101', name: '黄浦区' }, { code: '310104', name: '徐汇区' },
      { code: '310105', name: '长宁区' }, { code: '310106', name: '静安区' },
      { code: '310107', name: '普陀区' }, { code: '310109', name: '虹口区' },
      { code: '310110', name: '杨浦区' }, { code: '310112', name: '闵行区' },
      { code: '310113', name: '宝山区' }, { code: '310114', name: '嘉定区' },
      { code: '310115', name: '浦东新区' }, { code: '310116', name: '金山区' },
      { code: '310117', name: '松江区' }, { code: '310118', name: '青浦区' },
      { code: '310120', name: '奉贤区' }, { code: '310151', name: '崇明区' }
    ],
    '500000': [ // 重庆
      { code: '500101', name: '万州区' }, { code: '500102', name: '涪陵区' },
      { code: '500103', name: '渝中区' }, { code: '500104', name: '大渡口区' },
      { code: '500105', name: '江北区' }, { code: '500106', name: '沙坪坝区' },
      { code: '500107', name: '九龙坡区' }, { code: '500108', name: '南岸区' },
      { code: '500109', name: '北碚区' }, { code: '500110', name: '綦江区' },
      { code: '500111', name: '大足区' }, { code: '500112', name: '渝北区' },
      { code: '500113', name: '巴南区' }, { code: '500114', name: '黔江区' },
      { code: '500115', name: '长寿区' }, { code: '500116', name: '江津区' },
      { code: '500117', name: '合川区' }, { code: '500118', name: '永川区' },
      { code: '500119', name: '南川区' }, { code: '500120', name: '璧山区' },
      { code: '500151', name: '铜梁区' }, { code: '500152', name: '潼南区' },
      { code: '500153', name: '荣昌区' }, { code: '500154', name: '开州区' },
      { code: '500155', name: '梁平区' }, { code: '500156', name: '武隆区' }
    ]
  };

  return municipalityAreas[provinceCode] || [];
};

// 区县改变时更新数据
const handleAreaChange = (areaCode) => {

  // 确保 areaCode 是字符串而不是数组
  if (Array.isArray(areaCode)) {
    areaCode = areaCode[0];
  }

  if (!areaCode) {
    addressForm.area = '';
    addressForm.areaCode = '';
    return;
  }

  // 设置区域名称
  const area = areas.value.find(a => a.code === areaCode);
  if (area) {
    addressForm.area = area.name;
    addressForm.areaCode = areaCode;
  }
};

// 加载地址列表
const loadAddressList = async () => {
  try {
    const response = await getMyAddressList()
    if (response.code === 200) {
      addressList.value = response.data || []
      // 查找默认地址
      defaultAddress.value = addressList.value.find(addr => addr.defaultFlag === 1) || null
    } else {
      ElMessage.error(response.msg || '获取地址列表失败')
    }
  } catch (error) {
    console.error('加载地址列表失败:', error)
    ElMessage.error('加载地址列表失败')
  }
}

// 打开新增地址对话框
const openAddAddressDialog = () => {
  isEditAddress.value = false
  currentAddressId.value = null

  // 重置表单
  Object.keys(addressForm).forEach(key => {
    if (key === 'addressLabel') {
      addressForm[key] = 1
    } else if (key === 'defaultFlag') {
      addressForm[key] = 0
    } else if (key === 'addressId') {
      // 生成一个临时的 addressId
      addressForm[key] = Date.now()
    } else {
      addressForm[key] = ''
    }
  })

  // 设置 userId
  addressForm.userId = currentUserId.value

  addressDialogVisible.value = true

  // 重置省市区数据
  cities.value = []
  areas.value = []

  // 加载省份数据
  if (provinces.value.length === 0) {
    loadProvinces()
  }
}

// 打开编辑地址对话框
const openEditAddressDialog = async (address) => {
  isEditAddress.value = true
  currentAddressId.value = address.id

  // 重置表单（不清空省市区编码，等待数据加载后设置）
  Object.keys(addressForm).forEach(key => {
    if (key === 'addressLabel') {
      addressForm[key] = 1
    } else if (key === 'defaultFlag') {
      addressForm[key] = 0
    } else if (key === 'addressId') {
      addressForm[key] = address[key] || Date.now()
    } else {
      addressForm[key] = ''
    }
  })

  // 设置基本字段
  addressForm.id = address.id
  addressForm.userId = currentUserId.value
  addressForm.receiveName = address.receiveName || ''
  addressForm.receivePhone = address.receivePhone || ''
  addressForm.street = address.street || ''
  addressForm.postalCode = address.postalCode || ''
  addressForm.addressLabel = address.addressLabel || 1
  addressForm.defaultFlag = address.defaultFlag || 0

  // 确保省份数据已加载
  if (provinces.value.length === 0) {
    await loadProvinces()
  }

  // 先打开对话框
  addressDialogVisible.value = true

  // 等待对话框DOM渲染完成
  await nextTick()

  // 加载并设置省市区数据
  await loadEditAddressData(address)

}

// 监听省份编码变化，自动加载城市（仅在新增时触发）
watch(() => addressForm.provinceCode, async (newVal, oldVal) => {
  // 编辑模式下不触发自动加载
  if (isEditAddress.value || isLoadingEditData.value) return
  if (!newVal || newVal === oldVal) return

  // 确保是字符串
  if (Array.isArray(newVal)) {
    newVal = newVal[0]
    addressForm.provinceCode = newVal
  }

  // 设置省份名称
  const province = provinces.value.find(p => p.code === newVal)
  if (province && addressForm.province !== province.name) {
    addressForm.province = province.name
  }

  // 清空下级数据
  addressForm.cityCode = ''
  addressForm.areaCode = ''
  addressForm.city = ''
  addressForm.area = ''
  cities.value = []
  areas.value = []
})

// 监听城市编码变化，自动加载区域（仅在新增时触发）
watch(() => addressForm.cityCode, async (newVal, oldVal) => {
  // 编辑模式下不触发自动加载
  if (isEditAddress.value || isLoadingEditData.value) return
  if (!newVal || newVal === oldVal) return

  // 确保是字符串
  if (Array.isArray(newVal)) {
    newVal = newVal[0]
    addressForm.cityCode = newVal
  }

  // 设置城市名称
  const city = cities.value.find(c => c.code === newVal)
  if (city && addressForm.city !== city.name) {
    addressForm.city = city.name
  }

  // 清空区域数据
  addressForm.areaCode = ''
  addressForm.area = ''
  areas.value = []
})

// 监听区域编码变化
watch(() => addressForm.areaCode, (newVal) => {
  if (!newVal) return

  // 确保是字符串
  if (Array.isArray(newVal)) {
    newVal = newVal[0]
    addressForm.areaCode = newVal
  }

  // 设置区域名称
  const area = areas.value.find(a => a.code === newVal)
  if (area && addressForm.area !== area.name) {
    addressForm.area = area.name
  }
})

// 专门处理编辑时的省市区数据加载
const loadEditAddressData = async (address) => {
  // 统一转换为字符串
  const provinceCode = String(address.provinceCode || '')
  const cityCode = String(address.cityCode || '')
  const areaCode = String(address.areaCode || '')

  if (!provinceCode) return

  cities.value = []
  areas.value = []

  try {
    const province = provinces.value.find(p => p.code === provinceCode)
    if (!province) return

    const isMunicipalityArea = isMunicipality(provinceCode)

    if (isMunicipalityArea) {
      areaLoading.value = true
      try {
        areas.value = await areaService.getAreas(provinceCode)
        if (areas.value.length === 0) {
          areas.value = await loadMunicipalityAreas(provinceCode)
        }
      } finally {
        areaLoading.value = false
      }

      await nextTick()
      await nextTick()

      addressForm.provinceCode = provinceCode
      addressForm.province = province.name
      addressForm.cityCode = provinceCode
      addressForm.city = province.name

      if (areaCode) {
        const area = areas.value.find(a => a.code === areaCode)
        if (area) {
          addressForm.areaCode = areaCode
          addressForm.area = area.name
        }
      }
    } else {
      cityLoading.value = true
      try {
        cities.value = await areaService.getCities(provinceCode)
      } finally {
        cityLoading.value = false
      }

      await nextTick()
      await nextTick()

      addressForm.provinceCode = provinceCode
      addressForm.province = province.name

      if (cityCode && cities.value.length > 0) {
        const city = cities.value.find(c => c.code === cityCode)
        if (!city) return

        areaLoading.value = true
        try {
          areas.value = await areaService.getAreas(cityCode)
        } finally {
          areaLoading.value = false
        }

        await nextTick()
        await nextTick()

        addressForm.cityCode = cityCode
        addressForm.city = city.name

        if (areaCode && areas.value.length > 0) {
          const area = areas.value.find(a => a.code === areaCode)
          if (area) {
            addressForm.areaCode = areaCode
            addressForm.area = area.name
          }
        }
      }
    }
  } catch (error) {
    console.error('加载省市区数据失败:', error)
    regionLoadError.value = '地区数据加载失败，请检查网络后重试。'
  }
}

// 处理地址提交
const handleAddressSubmit = async () => {
  if (!addressFormRef.value) return

  try {
    await addressFormRef.value.validate()
    addressLoading.value = true

    // 修复数据：确保编码是字符串而不是数组
    const submitData = {
      ...addressForm,
      provinceCode: Array.isArray(addressForm.provinceCode) ? addressForm.provinceCode[0] : addressForm.provinceCode,
      cityCode: Array.isArray(addressForm.cityCode) ? addressForm.cityCode[0] : addressForm.cityCode,
      areaCode: Array.isArray(addressForm.areaCode) ? addressForm.areaCode[0] : addressForm.areaCode,
    }

    // 确保名称正确
    if (!submitData.province && submitData.provinceCode) {
      const province = provinces.value.find(p => p.code === submitData.provinceCode)
      if (province) submitData.province = province.name
    }

    if (!submitData.city && submitData.cityCode) {
      const city = cities.value.find(c => c.code === submitData.cityCode)
      if (city) submitData.city = city.name
    }

    if (!submitData.area && submitData.areaCode) {
      const area = areas.value.find(a => a.code === submitData.areaCode)
      if (area) submitData.area = area.name
    }

    let response
    if (isEditAddress.value) {
      response = await updateAddress(submitData)
    } else {
      response = await addAddress(submitData)
    }

    if (response.code === 200) {
      ElMessage.success(isEditAddress.value ? '地址更新成功' : '地址添加成功')
      addressDialogVisible.value = false
      await loadAddressList()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    console.error('操作地址失败:', error)
    ElMessage.error('操作失败')
  } finally {
    addressLoading.value = false
  }
}

// 根据编码获取区域名称的辅助函数
const getAreaNameByCode = (areaList, code) => {
  if (!code || !areaList || areaList.length === 0) return ''
  const area = areaList.find(item => item.code === code)
  return area ? area.name : ''
}

// 删除地址
const handleDeleteAddress = async (addressId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 直接调用删除接口，不需要先查询
    const response = await delAddress(addressId)
    if (response.code === 200) {
      ElMessage.success('地址删除成功')
      await loadAddressList()
    } else {
      ElMessage.error(response.msg || '地址删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
      ElMessage.error('地址删除失败')
    }
  }
}

// 设置默认地址
const handleSetDefaultAddress = async (addressId) => {
  try {
    const response = await setDefaultAddress({
      addressId: addressId,
      userId: currentUserId.value
    })

    if (response.code === 200) {
      ElMessage.success('默认地址设置成功')
      await loadAddressList()
    } else {
      ElMessage.error(response.msg || '设置默认地址失败')
    }
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置默认地址失败')
  }
}

// 获取地址标签名称
const getAddressLabelName = (label) => {
  const option = addressLabelOptions.find(opt => opt.value === label)
  return option ? option.label : '其他'
}

// 格式化完整地址
const formatFullAddress = (address) => {
  const { province, city, area, street } = address
  return `${province}${city}${area}${street}`
}

// 关闭地址对话框时的处理
const handleAddressDialogClose = () => {
  // 重置表单
  if (addressFormRef.value) {
    addressFormRef.value.resetFields()
  }
  // 清空省市区数据
  cities.value = []
  areas.value = []
}

onMounted(() => {
  loadUserInfo()
})

onBeforeUnmount(clearAvatarPreview)
</script>

<style scoped>
.user-profile {
  padding: 0;
}

.profile-content {
  min-height: 500px;
}

/* 个人信息卡片样式 - 淘宝红风格 */
.profile-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 25px 20px;
  text-align: center;
  height: fit-content;
  position: sticky;
  top: 20px;
}

.avatar-section {
  margin-bottom: 20px;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 15px;
  width: 90px;
  height: 90px;
}

.user-avatar-img {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  display: block;
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  right: -5px;
  background: linear-gradient(135deg, #ff6034 0%, #ee0a24 100%);
  color: white;
  border-radius: 15px;
  padding: 4px 10px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 3px;
  white-space: nowrap;
  border: 0;
  font-family: inherit;
}

.avatar-upload:focus-visible,
.stat-item:focus-visible {
  outline: 3px solid #ee0a24;
  outline-offset: 3px;
}

.avatar-upload:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(238, 10, 36, 0.4);
}

.user-name {
  margin: 0 0 6px 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.user-role {
  margin: 0 0 4px 0;
  font-size: 13px;
  color: #ff6034;
  font-weight: 500;
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
  margin: 20px 0;
  padding: 18px 0;
  border-top: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
}

.stat-item {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 12px;
  border-radius: 8px;
  color: inherit;
  text-decoration: none;
}

.stat-item:hover {
  background-color: #fff5f5;
  transform: translateY(-2px);
}

.stat-value {
  font-size: 22px;
  font-weight: bold;
  color: #ff6034;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

/* 余额样式 */
.balance-section {
  margin: 20px 0;
  padding: 20px;
  background: linear-gradient(135deg, #ff6034 0%, #ee0a24 100%);
  border-radius: 12px;
  color: white;
}

.balance-label {
  font-size: 13px;
  margin-bottom: 8px;
  opacity: 0.9;
}

.balance-amount {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 15px;
}

.balance-section .recharge-btn {
  width: 100%;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: white;
}

.balance-section .recharge-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 快捷操作样式 */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 15px;
}

.quick-actions .el-button {
  justify-content: center;
}

/* 信息编辑区域样式 */
.info-edit-section {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 25px;
  min-height: 500px;
}

.profile-tabs {
  min-height: 400px;
}

:deep(.profile-tabs .el-tabs__header) {
  margin-bottom: 25px;
}

:deep(.profile-tabs .el-tabs__nav-wrap::after) {
  background-color: #f0f0f0;
}

:deep(.profile-tabs .el-tabs__item) {
  font-size: 14px;
}

:deep(.profile-tabs .el-tabs__item.is-active) {
  color: #ff6034;
  font-weight: 600;
}

:deep(.profile-tabs .el-tabs__item:hover) {
  color: #ff6034;
}

:deep(.profile-tabs .el-tabs__active-bar) {
  background: linear-gradient(135deg, #ff6034 0%, #ee0a24 100%);
  height: 3px;
  border-radius: 2px;
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

/* 地址管理样式 */
.address-section {
  padding: 10px 0;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.address-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

/* 地址项样式 */
.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  transition: all 0.3s;
  background: #fff;
}

.address-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.address-item.default-address {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
}

.address-info {
  flex: 1;
}

.address-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.receive-name {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.receive-phone {
  color: #666;
}

.full-address {
  color: #666;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 10px;
}

.empty-address {
  text-align: center;
  padding: 40px 0;
}

/* 地址表单样式 */
.address-form {
  max-width: 100%;
}

:deep(.address-form .el-form-item) {
  margin-bottom: 20px;
}

:deep(.address-form .el-input .el-input__wrapper) {
  border-radius: 6px;
}

:deep(.address-form .el-textarea .el-textarea__inner) {
  border-radius: 6px;
  resize: vertical;
}

/* 省市区选择器样式 */
.area-selectors {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.area-selector {
  flex: 1;
}

:deep(.area-selector .el-select) {
  width: 100%;
}

/* 头像上传对话框样式 */
.avatar-dialog {
  text-align: center;
}

.avatar-preview {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 20px;
  border: 3px solid #f0f0f0;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-again {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  color: #409eff;
}

.upload-tips {
  text-align: center;
  color: #999;
  font-size: 12px;
  line-height: 1.5;
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

.security-info {
  flex: 1;
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
    font-size: 18px;
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

  .address-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .address-actions {
    align-self: flex-end;
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

  .address-header-info {
    flex-wrap: wrap;
    gap: 5px;
  }

  .address-actions {
    flex-direction: column;
    gap: 5px;
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

/* 按钮样式优化 - 淘宝红 */
:deep(.el-button) {
  border-radius: 20px;
  transition: all 0.3s;
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #ff6034 0%, #ee0a24 100%);
  border: none;
}

:deep(.el-button--danger:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(238, 10, 36, 0.3);
}

:deep(.el-button--danger.is-plain) {
  background: transparent;
  border: 1px solid #ff6034;
  color: #ff6034;
}

:deep(.el-button--danger.is-plain:hover) {
  background: #fff5f5;
}

:deep(.el-button--warning.is-plain) {
  border: 1px solid #ff9500;
  color: #ff9500;
}

:deep(.el-button--warning.is-plain:hover) {
  background: #fffaf0;
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

  .address-item:hover {
    transform: none;
  }
}

/* 自定义滚动条 */
.custom-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 表单提示样式 */
.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 加载动画 */
.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

/* 错误状态样式 */
.error-state {
  text-align: center;
  padding: 40px 20px;
  color: #f56c6c;
}

.error-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 成功状态样式 */
.success-state {
  text-align: center;
  padding: 40px 20px;
  color: #67c23a;
}

.success-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 卡片悬停效果 */
.card-hover {
  transition: all 0.3s ease;
}

.card-hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 渐变背景 */
.gradient-bg {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.gradient-bg-primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.gradient-bg-success {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.gradient-bg-warning {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.gradient-bg-danger {
  background: linear-gradient(135deg, #f56c6c 0%, #f89898 100%);
}

/* 文字渐变 */
.gradient-text {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 阴影效果 */
.shadow-sm {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.shadow-md {
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.shadow-lg {
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
}

.shadow-xl {
  box-shadow: 0 20px 25px rgba(0, 0, 0, 0.1);
}

/* 边框圆角 */
.rounded-sm {
  border-radius: 4px;
}

.rounded-md {
  border-radius: 8px;
}

.rounded-lg {
  border-radius: 12px;
}

.rounded-full {
  border-radius: 50%;
}

/* 间距工具类 */
.mt-1 {
  margin-top: 4px;
}

.mt-2 {
  margin-top: 8px;
}

.mt-3 {
  margin-top: 12px;
}

.mt-4 {
  margin-top: 16px;
}

.mt-5 {
  margin-top: 20px;
}

.mb-1 {
  margin-bottom: 4px;
}

.mb-2 {
  margin-bottom: 8px;
}

.mb-3 {
  margin-bottom: 12px;
}

.mb-4 {
  margin-bottom: 16px;
}

.mb-5 {
  margin-bottom: 20px;
}

.p-1 {
  padding: 4px;
}

.p-2 {
  padding: 8px;
}

.p-3 {
  padding: 12px;
}

.p-4 {
  padding: 16px;
}

.p-5 {
  padding: 20px;
}

/* 文本工具类 */
.text-xs {
  font-size: 12px;
}

.text-sm {
  font-size: 14px;
}

.text-base {
  font-size: 16px;
}

.text-lg {
  font-size: 18px;
}

.text-xl {
  font-size: 20px;
}

.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

.text-primary {
  color: #409eff;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

.text-info {
  color: #909399;
}

/* 弹性布局工具类 */
.flex {
  display: flex;
}

.flex-col {
  flex-direction: column;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

.justify-between {
  justify-content: space-between;
}

.flex-1 {
  flex: 1;
}

/* 隐藏工具类 */
.hidden {
  display: none;
}

@media (max-width: 768px) {
  .hidden-mobile {
    display: none;
  }
}

@media (min-width: 769px) {
  .hidden-desktop {
    display: none;
  }
}

/* 充值弹窗样式 */
.recharge-tips {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.quick-amount-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-amount-btns .el-button {
  min-width: 70px;
}

.payment-radio {
  margin-right: 20px;
  margin-bottom: 10px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.payment-icon {
  display: inline-block;
  width: 24px;
  height: 24px;
  border-radius: 4px;
}

.payment-icon.alipay {
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
}

.payment-icon.wechat {
  background: linear-gradient(135deg, #07c160 0%, #05a14a 100%);
}

.payment-icon.bank {
  background: linear-gradient(135deg, #ff9500 0%, #ff6b00 100%);
}

.current-balance {
  font-size: 16px;
  color: #999;
}

.after-balance {
  font-size: 20px;
  font-weight: bold;
  color: #ff6034;
}
</style>
