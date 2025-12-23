<template>
  <div class="user-center">
    <!-- 顶部导航栏 -->
    <header class="user-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="brand-name">ESHOP</span>
        </div>
        <div class="user-welcome">
          欢迎回来，{{ userInfo.name || '用户' }}
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="user-main">
      <!-- 左侧导航 -->
      <div class="user-sidebar">
        <nav class="sidebar-nav">
          <ul>
            <li 
              v-for="item in navItems" 
              :key="item.key"
              :class="{ active: activeNav === item.key }"
              @click="switchNav(item)"
            >
              <el-icon class="nav-icon">
                <component :is="item.icon" />
              </el-icon>
              <span class="nav-text">{{ item.text }}</span>
              <!-- 数量徽章 -->
              <span v-if="getBadgeCount(item.key)" class="sidebar-badge">
                {{ formatCount(getBadgeCount(item.key)) }}
              </span>
            </li>
          </ul>
        </nav>
      </div>

      <!-- 右侧内容区域 -->
      <div class="user-content">
        <div class="page-header">
          <h2>{{ pageTitle }}</h2>
          <div class="page-subtitle">{{ pageSubtitle }}</div>
        </div>
        <component 
          :is="currentComponent" 
          :key="activeNav"
          v-bind="componentProps"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { 
  User, 
  List, 
  ShoppingCart, 
  Clock, 
  Shop, 
  Star, 
  ChatDotRound 
} from '@element-plus/icons-vue'

// 导入各个功能组件
import UserProfile from '@/views/user/UserProfile.vue'
import UserOrders from '@/views/user/UserOrders.vue'
import ShoppingCartPage from '@/views/user/ShoppingCart.vue'
import UserFootprint from '@/views/user/UserFootprint.vue'
import FavoriteShops from '@/views/user/FavoriteShops.vue'
import FavoriteProducts from '@/views/user/FavoriteProducts.vue'
import UserReviews from '@/views/user/UserReviews.vue'

// 定义 props
const props = defineProps({
  page: {
    type: String,
    default: 'profile'
  },
  query: {
    type: Object,
    default: () => ({})
  }
})

const store = useStore()
const route = useRoute()
const router = useRouter()

// 用户信息
const userInfo = computed(() => {
  if (store.state.user && store.state.user.name) {
    return {
      name: store.state.user.name
    }
  } else {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      try {
        const user = JSON.parse(userInfoStr)
        return {
          name: user.name || user.userName || user.username || ''
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
        return {}
      }
    }
  }
  return {}
})

// 页面标题配置
const pageConfig = {
  profile: { title: '个人中心', subtitle: '管理您的个人信息和账户设置' },
  orders: { title: '我的订单', subtitle: '查看和管理您的所有订单' },
  cart: { title: '购物车', subtitle: '查看和管理购物车中的商品' },
  footprint: { title: '我的足迹', subtitle: '查看您浏览过的商品记录' },
  'favorite/shop': { title: '收藏店铺', subtitle: '查看您收藏的店铺' },
  'favorite/product': { title: '收藏商品', subtitle: '查看您收藏的商品' },
  review: { title: '我的评价', subtitle: '查看和管理您的评价' }
}

// 导航菜单项
const navItems = [
  { 
    key: 'profile', 
    text: '个人主页', 
    icon: User, 
    route: '/user/profile',
    badgeKey: null
  },
  { 
    key: 'orders', 
    text: '我的订单', 
    icon: List, 
    route: '/user/orders',
    badgeKey: 'orderCount'
  },
  { 
    key: 'cart', 
    text: '购物车', 
    icon: ShoppingCart, 
    route: '/user/cart',
    badgeKey: 'cartCount'
  },
  { 
    key: 'footprint', 
    text: '我的足迹', 
    icon: Clock, 
    route: '/user/footprint',
    badgeKey: null
  },
  { 
    key: 'favorite/shop', 
    text: '收藏店铺', 
    icon: Shop, 
    route: '/user/favorite/shop',
    badgeKey: 'shopFavoriteCount'
  },
  { 
    key: 'favorite/product', 
    text: '收藏商品', 
    icon: Star, 
    route: '/user/favorite/product',
    badgeKey: 'productFavoriteCount'
  },
  { 
    key: 'review', 
    text: '我的评价', 
    icon: ChatDotRound, 
    route: '/user/review',
    badgeKey: 'reviewCount'
  }
]

// 响应式数据
const activeNav = ref(props.page)
const badgeCounts = ref({
  cartCount: 0,
  orderCount: 0,
  productFavoriteCount: 0,
  shopFavoriteCount: 0,
  reviewCount: 0
})

// 计算属性
const pageTitle = computed(() => {
  return pageConfig[activeNav.value]?.title || '用户中心'
})

const pageSubtitle = computed(() => {
  return pageConfig[activeNav.value]?.subtitle || '管理您的账户和购物信息'
})

const currentComponent = computed(() => {
  const componentMap = {
    'profile': UserProfile,
    'orders': UserOrders,
    'cart': ShoppingCartPage,
    'footprint': UserFootprint,
    'favorite/shop': FavoriteShops,
    'favorite/product': FavoriteProducts,
    'review': UserReviews
  }
  return componentMap[activeNav.value] || UserProfile
})

const componentProps = computed(() => {
  // 可以根据不同的组件传递不同的 props
  const propsMap = {
    'orders': { query: props.query },
    'cart': { query: props.query },
    // 其他组件的 props
  }
  return propsMap[activeNav.value] || {}
})

// 方法
const switchNav = (navItem) => {
  if (activeNav.value === navItem.key) return
  
  activeNav.value = navItem.key
  
  // 更新 URL 但不触发路由跳转（因为已经是 /user/:page 路由）
  router.push({ 
    path: navItem.route,
    query: {} // 清空查询参数
  })
}

const formatCount = (count) => {
  if (count > 99) return '99+'
  return count.toString()
}

const getBadgeCount = (navKey) => {
  const navItem = navItems.find(item => item.key === navKey)
  if (navItem?.badgeKey) {
    return badgeCounts.value[navItem.badgeKey] || 0
  }
  return 0
}

// 加载徽章数量
const loadBadgeCounts = async () => {
  try {
    // 模拟 API 调用
    badgeCounts.value = {
      cartCount: 3,
      orderCount: 5,
      productFavoriteCount: 12,
      shopFavoriteCount: 3,
      reviewCount: 7
    }
  } catch (error) {
    console.error('加载徽章数量失败:', error)
  }
}

// 生命周期
onMounted(() => {
  // 检查用户登录状态
  if (!userInfo.value.name) {
    router.push('/login')
    return
  }
  
  // 加载徽章数据
  loadBadgeCounts()
  
  console.log('用户中心初始化，页面参数:', props.page)
})

// 监听路由参数变化
watch(
  () => props.page,
  (newPage) => {
    console.log('页面参数变化:', newPage)
    if (newPage && newPage !== activeNav.value) {
      activeNav.value = newPage
    }
  },
  { immediate: true }
)

// 监听 route.query 变化（用于处理从外部带参数跳转）
watch(
  () => route.query,
  (newQuery) => {
    console.log('查询参数变化:', newQuery)
    // 可以根据查询参数执行特定操作
  },
  { immediate: true }
)
</script>

<style scoped>
.user-center {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.user-header {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  padding: 0 20px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 10px;
}

.brand-name {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(135deg, #ff5500 0%, #ff9900 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-welcome {
  font-size: 16px;
  color: #666;
  font-weight: 500;
}

.user-main {
  max-width: 1200px;
  margin: 20px auto;
  display: flex;
  min-height: calc(100vh - 110px);
}

.user-sidebar {
  width: 250px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-right: 20px;
  height: fit-content;
}

.sidebar-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.sidebar-nav li {
  position: relative;
  padding: 15px 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.sidebar-nav li:hover {
  background-color: #f8f9fa;
  color: #ff5500;
}

.sidebar-nav li.active {
  background-color: #fff5f0;
  color: #ff5500;
  border-left-color: #ff5500;
}

.nav-icon {
  margin-right: 10px;
  font-size: 18px;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  flex: 1;
}

/* 侧边栏徽章 */
.sidebar-badge {
  background: #ff5500;
  color: white;
  border-radius: 10px;
  min-width: 20px;
  height: 20px;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  font-weight: bold;
}

.user-content {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 30px;
}

.page-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #333;
}

.page-subtitle {
  font-size: 14px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-main {
    flex-direction: column;
  }
  
  .user-sidebar {
    width: 100%;
    margin-right: 0;
    margin-bottom: 20px;
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
    border-bottom-color: #ff5500;
  }
  
  .user-content {
    padding: 20px;
  }
}

/* 动画效果 */
.sidebar-nav li {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-nav li:hover {
  transform: translateX(5px);
}

.sidebar-nav li.active:hover {
  transform: translateX(0);
}
</style>