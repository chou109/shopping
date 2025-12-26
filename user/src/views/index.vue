<template>
  <div class="home-container">
    <!-- 头部导航栏 -->
    <header class="main-header">
      <div class="header-container">
        <!-- Logo区域 -->
        <div class="logo-area">
          <div class="logo" @click="$router.push('/')">
            <img src="@/assets/eshop.png" alt="ESHOP">
            <span class="brand-name">ESHOP</span>
          </div>
        </div>
        
        <!-- 功能链接区域 -->
        <div class="nav-links-area">
          <!-- 功能链接 - 用户登录后显示 -->
          <div v-if="userInfo.name" class="user-links">
            <!-- 用户信息下拉菜单 -->
            <el-dropdown class="user-dropdown">
              <div class="user-info-link">
                <span class="user-name">{{ userInfo.name }}</span>
                <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/user/profile')">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/user/orders')">
                    <el-icon><List /></el-icon>
                    我的订单
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/user/footprint')">
                    <el-icon><Star /></el-icon>
                    我的足迹
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            
            <!-- 其他功能链接 -->
            <!-- 在首页的 template 部分，修改导航链接 -->
<!-- 在 index.vue 的功能链接部分，确保使用正确的路由 -->
<div class="function-links">
  <router-link :to="{ path: '/user/cart' }" class="nav-link">
    <span class="link-text">购物车</span>
    <span v-if="cartCount > 0" class="count-badge">{{ formatCount(cartCount) }}</span>
  </router-link>
  
  <router-link :to="{ path: '/user/orders' }" class="nav-link">
    <span class="link-text">我的订单</span>
    <span v-if="orderCount > 0" class="count-badge">{{ formatCount(orderCount) }}</span>
  </router-link>
  
  <router-link :to="{ path: '/user/favoriteShop' }" class="nav-link">
    <span class="link-text">收藏店铺</span>
    <span v-if="shopFavoriteCount > 0" class="count-badge">{{ formatCount(shopFavoriteCount) }}</span>
  </router-link>
  
  <router-link :to="{ path: '/user/favoriteProduct' }" class="nav-link">
    <span class="link-text">收藏商品</span>
    <span v-if="productFavoriteCount > 0" class="count-badge">{{ formatCount(productFavoriteCount) }}</span>
  </router-link>
  
  <router-link :to="{ path: '/user/review' }" class="nav-link">
    <span class="link-text">我的评价</span>
    <span v-if="reviewCount > 0" class="count-badge">{{ formatCount(reviewCount) }}</span>
  </router-link>
</div>
          </div>
          
          <!-- 用户未登录时显示的登录链接 -->
          <div v-else class="login-link" @click="$router.push('/login')">
            <span class="login-text">请登录</span>
          </div>
        </div>
        
        <!-- 搜索框区域 -->
        <div class="search-area">
          <div class="search-container">
            <div class="search-box">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索商品..."
                @keyup.enter="handleSearch"
                size="large"
                class="search-input"
              >
                <template #append>
                  <el-button @click="handleSearch" class="search-btn">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </div>
    </header>
    
    <!-- 主体内容 -->
    <main class="main-content">
      <!-- 轮播图部分 -->
      <div class="banner-section">
        <IndexBanner />
      </div>
      
      <!-- 商品懒加载列表区域 -->
      <div class="lazy-products-section">
        <LazyProductList ref="lazyProductListRef" />
      </div>
    </main>
    
    <!-- 页脚 -->
    <footer class="main-footer">
      <div class="footer-content">
        <div class="service-section">
          <div class="service-item">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z" fill="currentColor"/>
              </svg>
            </div>
            <div class="service-text">
              <h3>正品保障</h3>
              <p>官方授权，品质保证</p>
            </div>
          </div>
          
          <div class="service-item">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M18 18.5c.83 0 1.5-.67 1.5-1.5s-.67-1.5-1.5-1.5-1.5.67-1.5 1.5.67 1.5 1.5 1.5zM3 14h14v-3H3v3zm15.5-6c.83 0 1.5-.67 1.5-1.5S19.33 5 18.5 5 17 5.67 17 6.5 17.67 8 18.5 8zM3 5h9v3H3V5zm1 13h5v-2H4v2zm11-2v2h2v-2h-2zM9 5h5v3H9V5z" fill="currentColor"/>
              </svg>
            </div>
            <div class="service-text">
              <h3>极速物流</h3>
              <p>当日下单，次日送达</p>
            </div>
          </div>
          
          <div class="service-item">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 14H4V8l8 5 8-5v10zm-8-7L4 6h16l-8 5z" fill="currentColor"/>
              </svg>
            </div>
            <div class="service-text">
              <h3>7天退换</h3>
              <p>无忧购物，售后无忧</p>
            </div>
          </div>
        </div>
        
        <div class="copyright">
          <p>© 2025 ESHOP 版权所有</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { 
  Search, 
  ArrowDown, 
  User, 
  List, 
  Star, 
  SwitchButton 
} from '@element-plus/icons-vue'
import IndexBanner from '@/views/index/indexBanner.vue'
import LazyProductList from '@/views/index/LazyProductList.vue'

const router = useRouter()
const route = useRoute()
const store = useStore()
const searchKeyword = ref('')
const lazyProductListRef = ref(null)

// 用户信息
const userInfo = computed(() => {
  if (store.state.user && store.state.user.name) {
    return {
      name: store.state.user.name
    }
  } else {
    // 从 localStorage 尝试获取用户信息
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

// 购物车、订单、收藏和评价数量
const cartCount = ref(0)
const orderCount = ref(0)
const productFavoriteCount = ref(0)
const shopFavoriteCount = ref(0)
const reviewCount = ref(0)

// 页面加载时获取数据
onMounted(() => {
  if (userInfo.value.name) {
    loadNavData()
  }
})

// 格式化数量显示
const formatCount = (count) => {
  if (count > 99) return '99+'
  return count.toString()
}

// 加载导航栏数据
const loadNavData = async () => {
  await Promise.all([
    getCartCount(),
    getOrderCount(),
    getFavoriteStats(),
    getReviewCount()
  ])
}

// 获取购物车数量
const getCartCount = async () => {
  try {
    // 这里调用购物车数量接口
    // const response = await api.get('/system/cart/count')
    // if (response.code === 200) {
    //   cartCount.value = response.data.count || 0
    // }
    
    // 模拟数据
    cartCount.value = 3
  } catch (error) {
    console.error('获取购物车数量失败:', error)
  }
}

// 获取订单数量
const getOrderCount = async () => {
  try {
    // 这里调用订单数量接口
    // const response = await api.get('/system/orderDetail/count')
    // if (response.code === 200) {
    //   orderCount.value = response.data.count || 0
    // }
    
    // 模拟数据
    orderCount.value = 5
  } catch (error) {
    console.error('获取订单数量失败:', error)
  }
}

// 获取收藏统计
const getFavoriteStats = async () => {
  try {
    // 这里调用收藏统计接口
    // const response = await api.get('/system/favorite/my/stats')
    // if (response.code === 200) {
    //   productFavoriteCount.value = response.data.productCount || 0
    //   shopFavoriteCount.value = response.data.shopCount || 0
    // }
    
    // 模拟数据
    productFavoriteCount.value = 5
    shopFavoriteCount.value = 2
  } catch (error) {
    console.error('获取收藏统计失败:', error)
  }
}

// 获取评价数量
const getReviewCount = async () => {
  try {
    // 这里需要新增评价相关的接口
    // const response = await api.get('/system/review/my/count')
    // if (response.code === 200) {
    //   reviewCount.value = response.data.count || 0
    // }
    
    // 模拟数据
    reviewCount.value = 7
  } catch (error) {
    console.error('获取评价数量失败:', error)
  }
}

// 获取订单列表
const getOrderList = async () => {
  try {
    // 这里调用订单列表接口
    // const response = await api.get('/system/orderDetail/list')
  } catch (error) {
    console.error('获取订单列表失败:', error)
  }
}

// 获取店铺收藏
const getShopFavorites = async () => {
  try {
    // 这里调用店铺收藏接口
    // const response = await api.get('/system/favorite/my/shops')
  } catch (error) {
    console.error('获取店铺收藏失败:', error)
  }
}

// 获取商品收藏
const getProductFavorites = async () => {
  try {
    // 这里调用商品收藏接口
    // const response = await api.get('/system/favorite/my/products')
  } catch (error) {
    console.error('获取商品收藏失败:', error)
  }
}

// 获取我的评价
const getMyReviews = async () => {
  try {
    // 这里需要新增评价相关的接口
    // const response = await api.get('/system/review/my')
  } catch (error) {
    console.error('获取我的评价失败:', error)
  }
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/products',
      query: { 
        keyword: searchKeyword.value.trim(),
        searchType: 'keyword'
      }
    })
    searchKeyword.value = ''
  }
}

const logout = () => {
  if (store.dispatch && typeof store.dispatch === 'function') {
    store.dispatch('LogOut').then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }).catch(error => {
      console.error('退出登录失败:', error)
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    })
  } else {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }
}
</script>

<style scoped>
.main-header {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  width: 100%;
  height: 70px;
  display: flex;
  align-items: center;
}

.header-container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
}

/* Logo区域 */
.logo-area {
  flex: 0 0 auto;
  margin-right: 30px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 10px;
}
.logo img {
  height: 40px;
  width: auto;
}

.brand-name {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(135deg, #ff5500 0%, #ff9900 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 功能链接区域 */
.nav-links-area {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  margin-right: 30px;
  min-width: 0; /* 防止flex元素溢出 */
}

/* 用户登录后的链接样式 */
.user-links {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 用户信息下拉菜单 */
.user-dropdown {
  cursor: pointer;
}

.user-info-link {
  display: flex;
  align-items: center;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s;
  
  &:hover {
    background-color: #f5f5f5;
  }
}

.user-name {
  font-size: 14px;
  color: #ff5500;
  font-weight: 500;
  margin-right: 5px;
}

.dropdown-icon {
  color: #999;
  font-size: 12px;
  transition: transform 0.3s;
}

.user-dropdown:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 功能链接 */
.function-links {
  display: flex;
  align-items: center;
  gap: 15px;
}

/* 导航链接样式 */
.nav-link {
  position: relative;
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #666;
  font-size: 13px;
  padding: 5px 8px;
  border-radius: 4px;
  transition: all 0.3s;
  white-space: nowrap;
  
  &:hover {
    color: #ff5500;
    background-color: #fff5f0;
  }
  
  &.router-link-active {
    color: #ff5500;
    font-weight: 500;
  }
}

.link-text {
  display: inline-block;
}

/* 统一数量徽章样式 */
.count-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ff5500;
  color: white;
  border-radius: 10px;
  min-width: 18px;
  height: 18px;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  font-weight: bold;
  z-index: 1;
  box-shadow: 0 2px 4px rgba(255, 85, 0, 0.3);
  animation: pulse 2s infinite;
}

/* 红点脉冲动画 */
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 85, 0, 0.7);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(255, 85, 0, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 85, 0, 0);
  }
}

/* 用户未登录样式 */
.login-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s;
  
  &:hover {
    background-color: #f5f5f5;
  }
}

.login-text {
  color: #ff5500;
  font-size: 14px;
  font-weight: 500;
  
  &:hover {
    text-decoration: underline;
  }
}

/* 搜索框区域 */
.search-area {
  flex: 0 0 400px;
  min-width: 300px;
}

.search-container {
  width: 100%;
}

.search-box {
  width: 100%;
}

.search-input {
  border-radius: 20px;
  overflow: hidden;
  border: 2px solid #ff5500;
  box-shadow: 0 2px 8px rgba(255, 85, 0, 0.1);
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px 0 0 20px;
  border: none;
  padding-left: 20px;
  box-shadow: none;
}

.search-btn {
  background: #ff5500;
  color: white;
  border: none;
  border-radius: 0 20px 20px 0;
  width: 60px;
  transition: all 0.3s;
  
  &:hover {
    background: #ff7700;
  }
}

/* Element Plus 下拉菜单样式覆盖 */
:deep(.el-dropdown-menu) {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  padding: 8px 0;
  
  .el-dropdown-menu__item {
    padding: 8px 16px;
    font-size: 13px;
    color: #333;
    
    .el-icon {
      margin-right: 8px;
      font-size: 14px;
      color: #666;
    }
    
    &:hover {
      color: #ff5500;
      background-color: #fff5f0;
      
      .el-icon {
        color: #ff5500;
      }
    }
  }
  
  .el-dropdown-menu__item--divided {
    border-top: 1px solid #f0f0f0;
    margin-top: 4px;
    padding-top: 12px;
  }
}

/* 主体内容 - 添加顶部内边距以避开固定导航栏 */
.main-content {
  flex: 1;
  width: 100%;
  padding-top: 85px; /* 导航栏高度 + 一些额外空间 */
}

.banner-section {
  margin-bottom: 30px;
}

/* 商品懒加载列表区域 */
.lazy-products-section {
  max-width: 1400px;
  margin: 0 auto 40px;
  padding: 0 20px;
}

/* 页脚样式 */
.main-footer {
  background: #f8f8f8;
  border-top: 1px solid #eee;
  padding: 40px 0 20px;
}

.footer-content {
  max-width: 1500px;
  margin: 0 auto;
  padding: 0 20px;
}

.service-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  padding-bottom: 30px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.service-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #ff5500 0%, #ff9900 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.service-icon svg {
  width: 24px;
  height: 24px;
}

.service-text h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #333;
}

.service-text p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.copyright {
  text-align: center;
  color: #999;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 1500px) {
  .header-container {
    padding: 0 15px;
  }
  
  .search-area {
    flex: 0 0 350px;
  }
  
  .function-links {
    gap: 10px;
  }
}

@media (max-width: 992px) {
  .header-container {
    flex-wrap: wrap;
    height: auto;
    min-height: 70px;
    padding: 10px 15px;
  }
  
  .logo-area {
    flex: 0 0 auto;
    margin-right: 20px;
  }
  
  .nav-links-area {
    order: 3;
    flex: 1 0 100%;
    margin: 10px 0 0 0;
    justify-content: center;
  }
  
  .search-area {
    order: 2;
    flex: 1;
    min-width: auto;
  }
  
  .user-links {
    justify-content: center;
    width: 100%;
  }
  
  .function-links {
    justify-content: center;
    flex-wrap: wrap;
    gap: 15px 20px;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 8px 12px;
  }
  
  .brand-name {
    font-size: 20px;
  }
  
  .user-links {
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }
  
  .function-links {
    gap: 10px 15px;
  }
  
  .nav-link {
    padding: 4px 6px;
    font-size: 12px;
  }
  
  .search-area {
    flex: 0 0 300px;
  }
  
  .search-input {
    border-width: 1px;
  }
}

@media (max-width: 640px) {
  .header-container {
    flex-direction: column;
    gap: 10px;
  }
  
  .logo-area {
    width: 100%;
    text-align: center;
    margin-right: 0;
  }
  
  .nav-links-area {
    order: 2;
    margin: 0;
  }
  
  .search-area {
    order: 3;
    width: 100%;
    flex: 0 0 auto;
  }
  
  .user-links {
    width: 100%;
  }
  
  .function-links {
    justify-content: space-between;
    width: 100%;
  }
  
  .nav-link {
    flex-direction: column;
    padding: 5px 3px;
    font-size: 11px;
    
    .link-text {
      margin-bottom: 2px;
    }
  }
  
  .count-badge {
    top: -3px;
    right: -3px;
    min-width: 16px;
    height: 16px;
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .function-links {
    gap: 5px 8px;
  }
  
  .nav-link {
    padding: 4px 2px;
    font-size: 10px;
  }
  
  .search-area {
    min-width: 100%;
  }
}

/* 添加滚动时导航栏的动画效果 */
.main-header {
  transition: box-shadow 0.3s ease;
}

.main-header.scrolled {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 确保Element Plus组件在固定导航栏下正常工作 */
:deep(.el-dropdown-menu) {
  z-index: 1001; /* 确保下拉菜单在导航栏上方 */
}
</style>