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
                <el-icon class="dropdown-icon">
                  <ArrowDown />
                </el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/user/profile')">
                    <el-icon>
                      <User />
                    </el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/user/orders')">
                    <el-icon>
                      <List />
                    </el-icon>
                    我的订单
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/user/footprint')">
                    <el-icon>
                      <Star />
                    </el-icon>
                    我的足迹
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="logout">
                    <el-icon>
                      <SwitchButton />
                    </el-icon>
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
              </router-link>

              <router-link :to="{ path: '/user/orders' }" class="nav-link">
                <span class="link-text">我的订单</span>
              </router-link>

              <router-link :to="{ path: '/user/favoriteShop' }" class="nav-link">
                <span class="link-text">收藏店铺</span>
              </router-link>

              <router-link :to="{ path: '/user/favoriteProduct' }" class="nav-link">
                <span class="link-text">收藏商品</span>
              </router-link>

              <router-link :to="{ path: '/user/review' }" class="nav-link">
                <span class="link-text">我的评价</span>
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
              <el-input v-model="searchKeyword" placeholder="搜索商品..." @keyup.enter="handleSearch" size="large"
                class="search-input">
                <template #append>
                  <el-button @click="handleSearch" class="search-btn">
                    <el-icon>
                      <Search />
                    </el-icon>
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

      <!-- 首页精选商品区 -->
      <FeaturedProducts />

      <!-- 商品懒加载列表区域 -->
      <div class="lazy-products-section">
        <LazyProductList />
      </div>
    </main>

    <!-- 侧边栏固定按钮 -->
    <FixedSidebar />

    <!-- 页脚 -->
    <footer class="main-footer">
      <div class="footer-content">
        <div class="service-section">
          <div class="service-item">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
                  fill="currentColor" />
              </svg>
            </div>
            <div class="service-text">
              <h3>商品信息</h3>
              <p>商品信息由店铺发布</p>
            </div>
          </div>

          <div class="service-item">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M18 18.5c.83 0 1.5-.67 1.5-1.5s-.67-1.5-1.5-1.5-1.5.67-1.5 1.5.67 1.5 1.5 1.5zM3 14h14v-3H3v3zm15.5-6c.83 0 1.5-.67 1.5-1.5S19.33 5 18.5 5 17 5.67 17 6.5 17.67 8 18.5 8zM3 5h9v3H3V5zm1 13h5v-2H4v2zm11-2v2h2v-2h-2zM9 5h5v3H9V5z"
                  fill="currentColor" />
              </svg>
            </div>
            <div class="service-text">
              <h3>订单进度</h3>
              <p>在订单页查看发货状态</p>
            </div>
          </div>

          <div class="service-item">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 14H4V8l8 5 8-5v10zm-8-7L4 6h16l-8 5z"
                  fill="currentColor" />
              </svg>
            </div>
            <div class="service-text">
              <h3>售后服务</h3>
              <p>可在订单页申请售后</p>
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
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
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
import FeaturedProducts from '@/views/index/FeaturedProducts.vue'
import LazyProductList from '@/views/index/LazyProductList.vue'
import FixedSidebar from '@/views/index/FixedSidebar.vue'

const router = useRouter()
const store = useStore()
const searchKeyword = ref('')

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

const showBackTop = ref(false)

// 页面加载时获取数据
onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 跳转到人工客服页面
const goToCustomerService = () => {
  if (!userInfo.value.name) {
    router.push('/login')
    return
  }
  router.push('/customer-service')
}

// 处理滚动事件
const handleScroll = () => {
  showBackTop.value = window.scrollY > 300
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
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
  background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  width: 100%;
  height: 80px;
  display: flex;
  align-items: center;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 85, 0, 0.1);
}

.header-container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
}

/* Logo区域 */
.logo-area {
  flex: 0 0 auto;
  margin-right: 40px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 12px;
  transition: all 0.3s ease;
  padding: 8px 12px;
  border-radius: 12px;
}

.logo:hover {
  background: rgba(255, 85, 0, 0.05);
  transform: translateY(-2px);
}

.logo img {
  height: 45px;
  width: auto;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.brand-name {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #ff5500 0%, #ff9900 50%, #ffcc00 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(255, 85, 0, 0.1);
}

/* 功能链接区域 */
.nav-links-area {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  margin-right: 40px;
  min-width: 0;
}

/* 用户登录后的链接样式 */
.user-links {
  display: flex;
  align-items: center;
  gap: 30px;
}

/* 用户信息下拉菜单 */
.user-dropdown {
  cursor: pointer;
}

.user-info-link {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s ease;
  background: rgba(255, 85, 0, 0.05);
  border: 2px solid transparent;
}

.user-info-link:hover {
  background: rgba(255, 85, 0, 0.1);
  border-color: rgba(255, 85, 0, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.15);
}

.user-name {
  font-size: 15px;
  color: #ff5500;
  font-weight: 600;
  margin-right: 6px;
}

.dropdown-icon {
  color: #ff5500;
  font-size: 14px;
  transition: transform 0.3s ease;
}

.user-dropdown:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 功能链接 */
.function-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 导航链接样式 */
.nav-link {
  position: relative;
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #666;
  font-size: 14px;
  padding: 10px 16px;
  border-radius: 20px;
  transition: all 0.3s ease;
  white-space: nowrap;
  font-weight: 500;
  background: transparent;
  border: 2px solid transparent;
}

.nav-link:hover {
  color: #ff5500;
  background: rgba(255, 85, 0, 0.08);
  border-color: rgba(255, 85, 0, 0.15);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.1);
}

.nav-link.router-link-active {
  color: #fff;
  background: linear-gradient(135deg, #ff5500 0%, #ff7700 100%);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

.link-text {
  display: inline-block;
}

/* 统一数量徽章样式 */
.count-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: linear-gradient(135deg, #ff3c00 0%, #ff0040 100%);
  color: white;
  border-radius: 12px;
  min-width: 20px;
  height: 20px;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  font-weight: 700;
  z-index: 1;
  box-shadow: 0 2px 8px rgba(255, 0, 64, 0.4);
  animation: pulse 2s infinite;
  border: 2px solid white;
}

/* 红点脉冲动画 */
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 60, 0, 0.8);
  }

  70% {
    box-shadow: 0 0 0 8px rgba(255, 60, 0, 0);
  }

  100% {
    box-shadow: 0 0 0 0 rgba(255, 60, 0, 0);
  }
}

/* 用户未登录样式 */
.login-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 10px 24px;
  border-radius: 24px;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #ff5500 0%, #ff7700 100%);
  box-shadow: 0 4px 12px rgba(255, 85, 0, 0.3);
}

.login-link:hover {
  background: linear-gradient(135deg, #ff7700 0%, #ff9900 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 85, 0, 0.4);
}

.login-text {
  color: white;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

/* 搜索框区域 */
.search-area {
  flex: 0 0 450px;
  min-width: 350px;
}

.search-container {
  width: 100%;
}

.search-box {
  width: 100%;
}

.search-input {
  border-radius: 28px;
  overflow: hidden;
  border: 2px solid #ff5500;
  box-shadow: 0 4px 16px rgba(255, 85, 0, 0.15);
  transition: all 0.3s ease;
}

.search-input:hover {
  box-shadow: 0 6px 20px rgba(255, 85, 0, 0.25);
  transform: translateY(-2px);
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 28px 0 0 28px;
  border: none;
  padding-left: 24px;
  box-shadow: none;
  background: white;
  font-size: 14px;
}

.search-input :deep(.el-input__wrapper:focus-within) {
  box-shadow: none;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: #bbb;
}

.search-btn {
  background: linear-gradient(135deg, #ff5500 0%, #ff7700 100%);
  color: white;
  border: none;
  border-radius: 0 28px 28px 0;
  width: 70px;
  height: 100%;
  transition: all 0.3s ease;
  font-size: 18px;
}

.search-btn:hover {
  background: linear-gradient(135deg, #ff7700 0%, #ff9900 100%);
  transform: scale(1.05);
}

/* Element Plus 下拉菜单样式覆盖 */
:deep(.el-dropdown-menu) {
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  padding: 12px 0;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

:deep(.el-dropdown-menu__item) {
  padding: 12px 20px;
  font-size: 14px;
  color: #333;
  border-radius: 8px;
  margin: 4px 8px;
  transition: all 0.3s ease;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 10px;
  font-size: 16px;
  color: #666;
}

:deep(.el-dropdown-menu__item:hover) {
  color: #ff5500;
  background: linear-gradient(135deg, rgba(255, 85, 0, 0.08) 0%, rgba(255, 119, 0, 0.08) 100%);
  transform: translateX(4px);
}

:deep(.el-dropdown-menu__item:hover .el-icon) {
  color: #ff5500;
}

:deep(.el-dropdown-menu__item--divided) {
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
  padding-top: 16px;
}

/* 主体内容 - 添加顶部内边距以避开固定导航栏 */
.main-content {
  flex: 1;
  width: 100%;
  padding-top: 95px;
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
  }

  .nav-link .link-text {
    margin-bottom: 2px;
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
  z-index: 1001;
  /* 确保下拉菜单在导航栏上方 */
}
</style>
