<template>
  <div class="user-center">
    <!-- 顶部用户信息卡片 -->
    <div class="user-header-card">
      <div class="header-card-content">
        <div class="user-info-section">
          <div class="avatar-box" role="button" tabindex="0" aria-label="打开个人资料"
            @click="goToProfile" @keydown.enter.prevent="goToProfile" @keydown.space.prevent="goToProfile">
            <img :src="avatarUrl" alt="头像" class="header-avatar" />
            <div class="avatar-overlay">
              <span>个人中心</span>
            </div>
          </div>
          <div class="user-text">
            <div class="user-nickname">{{ userInfo.name || '用户' }}</div>
            <div class="user-id">ID: {{ userInfo.userId || '--' }}</div>
          </div>
        </div>
        <div class="header-actions">
          <button type="button" class="action-btn" @click="$router.push('/')">
            <el-icon>
              <HomeFilled />
            </el-icon>
            <span>返回首页</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="user-main" ref="mainRef">
      <!-- 左侧导航 -->
      <div class="user-sidebar" ref="sidebarRef">
        <nav class="sidebar-nav">
          <ul>
            <li v-for="item in navItems" :key="item.key" :class="{ active: activeNav === item.key }"
              role="button" tabindex="0" :aria-current="activeNav === item.key ? 'page' : undefined"
              @click="switchNav(item)" @keydown.enter.prevent="switchNav(item)"
              @keydown.space.prevent="switchNav(item)">
              <el-icon class="nav-icon">
                <component :is="item.icon" />
              </el-icon>
              <span class="nav-text">{{ item.text }}</span>
              <span v-if="getBadgeCount(item.key)" class="sidebar-badge">
                {{ formatCount(getBadgeCount(item.key)) }}
              </span>
            </li>
          </ul>
        </nav>
      </div>

      <!-- 右侧内容区域 - 滚动容器 -->
      <div class="user-content" style="margin-top: -50px;" ref="contentRef" @scroll="handleContentScroll">
        <div class="page-header">
          <h2>{{ pageTitle }}</h2>
          <div class="page-subtitle">{{ pageSubtitle }}</div>
        </div>
        <component :is="currentComponent" :key="activeNav" v-bind="componentProps" />
      </div>

      <!-- 回到顶部按钮 -->
      <transition name="fade">
        <button v-show="showBackTop" type="button" class="back-to-top" aria-label="回到顶部"
          @click="scrollToTop">
          <el-icon>
            <Top />
          </el-icon>
        </button>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, provide } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
  User, List, ShoppingCart, Clock, Shop, Star, ChatDotRound, HomeFilled, Top, Message
} from '@element-plus/icons-vue'
import { getUsers } from '@/api/system/user'
import { DEFAULT_AVATAR_IMAGE, getProductImageUrl } from '@/utils/image'

// 导入各个功能组件
import UserProfile from '@/views/User/UserProfile.vue'
import UserOrders from '@/views/User/UserOrders.vue'
import ShoppingCartPage from '@/views/User/ShoppingCart.vue'
import UserFootprint from '@/views/User/UserFootprint.vue'
import FavoriteShops from '@/views/User/FavoriteShops.vue'
import FavoriteProducts from '@/views/User/FavoriteProducts.vue'
import UserReviews from '@/views/User/UserReviews.vue'
import UserMessage from '@/views/User/UserMessage.vue'
const props = defineProps({
  page: { type: String, default: 'profile' },
  query: { type: Object, default: () => ({}) }
})

const store = useStore()
const route = useRoute()
const router = useRouter()

// 用户信息
const userInfo = ref({
  userId: null, userName: '', name: '', nickName: '', avatar: '', email: '', phonenumber: '', sex: ''
})

// 头像点击跳转到个人中心
const goToProfile = () => {
  if (activeNav.value !== 'profile') {
    activeNav.value = 'profile'
    router.push({ path: '/user/profile' })
  }
}

const avatarUrl = computed(() => {
  if (userInfo.value.avatar) {
    return getProductImageUrl(userInfo.value.avatar)
  }
  return DEFAULT_AVATAR_IMAGE
})

const loadUserInfo = async () => {
  try {
    const response = await getUsers()
    if (response.code === 200) {
      const userData = response.data
      userInfo.value = {
        userId: userData.userId,
        userName: userData.userName,
        name: userData.nickName || userData.userName,
        nickName: userData.nickName,
        avatar: userData.avatar,
        email: userData.email,
        phonenumber: userData.phonenumber,
        sex: userData.sex
      }
      store.commit('SET_NAME', userData.userName)
      store.commit('SET_AVATAR', userData.avatar)
      store.commit('SET_USERID', userData.userId)
      store.commit('SET_REALNAME', userData.nickName)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}


// 页面配置
const pageConfig = {
  profile: { title: '个人中心', subtitle: '管理您的个人信息和账户设置' },
  orders: { title: '我的订单', subtitle: '查看和管理您的所有订单' },
  cart: { title: '购物车', subtitle: '查看和管理购物车中的商品' },
  footprint: { title: '我的足迹', subtitle: '查看您浏览过的商品记录' },
  favoriteShop: { title: '收藏店铺', subtitle: '查看您收藏的店铺' },
  favoriteProduct: { title: '收藏商品', subtitle: '查看您收藏的商品' },
  review: { title: '我的评价', subtitle: '查看和管理您的评价' },
  message: { title: '我的消息', subtitle: '查看您与客服的对话记录' }
}

const navItems = [
  { key: 'profile', text: '个人主页', icon: User, route: '/user/profile', badgeKey: null },
  { key: 'orders', text: '我的订单', icon: List, route: '/user/orders', badgeKey: 'orderCount' },
  { key: 'cart', text: '购物车', icon: ShoppingCart, route: '/user/cart', badgeKey: 'cartCount' },
  { key: 'message', text: '我的消息', icon: Message, route: '/user/message', badgeKey: 'messageCount' },
  { key: 'footprint', text: '我的足迹', icon: Clock, route: '/user/footprint', badgeKey: null },
  { key: 'favoriteShop', text: '收藏店铺', icon: Shop, route: '/user/favoriteShop', badgeKey: 'shopFavoriteCount' },
  { key: 'favoriteProduct', text: '收藏商品', icon: Star, route: '/user/favoriteProduct', badgeKey: 'productFavoriteCount' },
  { key: 'review', text: '我的评价', icon: ChatDotRound, route: '/user/review', badgeKey: 'reviewCount' }
]

const activeNav = ref(props.page)
const badgeCounts = ref({ cartCount: 0, orderCount: 0, productFavoriteCount: 0, shopFavoriteCount: 0, reviewCount: 0 })

const pageTitle = computed(() => pageConfig[activeNav.value]?.title || '用户中心')
const pageSubtitle = computed(() => pageConfig[activeNav.value]?.subtitle || '')

const currentComponent = computed(() => {
  const map = {
    'profile': UserProfile, 'orders': UserOrders, 'cart': ShoppingCartPage,
    'footprint': UserFootprint, 'favoriteShop': FavoriteShops,
    'favoriteProduct': FavoriteProducts, 'review': UserReviews, 'message': UserMessage
  }
  return map[activeNav.value] || UserProfile
})

const componentProps = computed(() => {
  const map = { 'orders': { query: props.query }, 'cart': { query: props.query } }
  return map[activeNav.value] || {}
})

// 内容区域滚动容器引用
const contentRef = ref(null)

// 提供滚动容器给子组件使用（用于懒加载）
provide('scrollContainer', contentRef)

// 回到顶部相关
const showBackTop = ref(false)

const handleContentScroll = () => {
  if (contentRef.value) {
    showBackTop.value = contentRef.value.scrollTop > 300
  }
}

const scrollToTop = () => {
  if (contentRef.value) {
    contentRef.value.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}

const switchNav = (navItem) => {
  if (activeNav.value === navItem.key) return
  activeNav.value = navItem.key
  router.push({ path: navItem.route, query: {} })
}

const formatCount = (count) => count > 99 ? '99+' : count.toString()
const getBadgeCount = (navKey) => {
  const item = navItems.find(i => i.key === navKey)
  return item?.badgeKey ? (badgeCounts.value[item.badgeKey] || 0) : 0
}

// 侧边栏引用
const sidebarRef = ref(null)
const mainRef = ref(null)

onMounted(async () => {
  // 禁用 body 滚动
  document.body.style.overflow = 'hidden'
  document.documentElement.style.overflow = 'hidden'

  await loadUserInfo()
  if (!userInfo.value.name) { router.push('/login'); return }
})

onUnmounted(() => {
  // 恢复 body 滚动
  document.body.style.overflow = ''
  document.documentElement.style.overflow = ''
})

watch(() => props.page, (newPage) => {
  if (newPage && newPage !== activeNav.value) activeNav.value = newPage
}, { immediate: true })
</script>

<style scoped>
.user-center {
  height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 顶部用户卡片 */
.user-header-card {
  background: linear-gradient(135deg, #ff6034 0%, #ee0a24 100%);
  padding: 30px 20px;
  flex-shrink: 0;
}

.header-card-content {
  max-width: 1300px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-box {
  position: relative;
  width: 70px;
  height: 70px;
  cursor: pointer;
  flex-shrink: 0;
}

.header-avatar {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.9);
  object-fit: cover;
  display: block;
}

.avatar-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 24px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 0 0 35px 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-box:hover .avatar-overlay {
  opacity: 1;
}

.user-text {
  color: white;
}

.user-nickname {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 5px;
}

.user-id {
  font-size: 13px;
  opacity: 0.85;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  color: white;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.3s;
  border: 0;
  cursor: pointer;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.action-btn:focus-visible,
.avatar-box:focus-visible,
.sidebar-nav li:focus-visible,
.back-to-top:focus-visible {
  outline: 3px solid #1f2937;
  outline-offset: 3px;
}

/* 主体内容 */
.user-main {
  flex: 1;
  max-width: 1300px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  gap: 20px;
  overflow: hidden;
  box-sizing: border-box;
}

.user-sidebar {
  width: 220px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: fit-content;
  flex-shrink: 0;
  height: fit-content;
  max-height: 100%;
  overflow-y: auto;
}

.sidebar-nav ul {
  list-style: none;
  padding: 10px 0;
  margin: 0;
}

.sidebar-nav li {
  padding: 14px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.2s;
  border-left: 3px solid transparent;
  color: #666;
}

.sidebar-nav li:hover {
  background-color: #fff5f5;
  color: #ee0a24;
}

.sidebar-nav li.active {
  background-color: #fff1f0;
  color: #ee0a24;
  border-left-color: #ee0a24;
  font-weight: 500;
}

.nav-icon {
  margin-right: 10px;
  font-size: 18px;
}

.nav-text {
  flex: 1;
  font-size: 14px;
}

.sidebar-badge {
  background: #ee0a24;
  color: white;
  border-radius: 10px;
  min-width: 20px;
  height: 20px;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
}

.user-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 25px;
  min-width: 0;
  max-width: calc(100% - 240px);
  overflow-y: auto;
  overflow-x: hidden;
}

.page-header {
  margin-bottom: 25px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  position: sticky;
  top: 0;
  background: white;
  z-index: 20;
}

.page-header h2 {
  margin: 0 0 6px 0;
  font-size: 20px;
  color: #333;
}

.page-subtitle {
  font-size: 13px;
  color: #999;
}

/* 响应式 - 大屏幕 */
@media (min-width: 1600px) {

  .header-card-content,
  .user-main {
    max-width: 1600px;
  }
}

/* 响应式 - 中等屏幕 */
@media (max-width: 1200px) {

  .header-card-content,
  .user-main {
    max-width: 100%;
    padding: 15px;
  }

  .user-sidebar {
    width: 180px;
  }

  .user-content {
    max-width: calc(100% - 200px);
  }
}

/* 响应式 - 小屏幕 */
@media (max-width: 768px) {
  .user-center {
    height: auto;
    min-height: 100vh;
    overflow: auto;
  }

  .header-card-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .user-info-section {
    flex-direction: column;
  }

  .avatar-box,
  .header-avatar {
    width: 60px;
    height: 60px;
  }

  .user-nickname {
    font-size: 18px;
  }

  .user-main {
    flex-direction: column;
    padding: 10px;
    overflow: visible;
  }

  .user-sidebar {
    width: 100%;
    max-height: none;
    overflow: visible;
  }

  .sidebar-nav ul {
    display: flex;
    overflow-x: auto;
    padding: 10px;
  }

  .sidebar-nav li {
    flex: 0 0 auto;
    white-space: nowrap;
    border-left: none;
    border-bottom: 3px solid transparent;
    padding: 10px 15px;
  }

  .sidebar-nav li.active {
    border-left: none;
    border-bottom-color: #ee0a24;
  }

  .user-content {
    max-width: 100%;
    padding: 15px;
    overflow: visible;
  }
}

/* 回到顶部按钮 */
.back-to-top {
  position: absolute;
  right: 30px;
  bottom: 30px;
  width: 44px;
  height: 44px;
  background: #ee0a24;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(238, 10, 36, 0.4);
  transition: all 0.3s;
  z-index: 100;
  border: 0;
}

.back-to-top:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(238, 10, 36, 0.5);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
