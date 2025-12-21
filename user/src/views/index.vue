<template>
  <div class="home-container">
    <!-- 头部导航栏 -->
    <header class="main-header">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/')">
          <img src="@/assets/eshop.png" alt="ESHOP">
          <span class="brand-name">ESHOP</span>
        </div>
        
        <!-- 搜索框 -->
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
        
        <!-- 用户信息 -->
        <div class="user-info">
          <el-dropdown v-if="userInfo.name">
            <span class="user-name">{{ userInfo.name }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/user/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <span v-else @click="$router.push('/login')" class="login-link">请登录</span>
        </div>
      </div>
    </header>
    
    <!-- 主体内容 -->
    <main class="main-content">
      <div class="banner-section">
        <IndexBanner />
      </div>
      
      <div class="content-wrapper">
        <!-- 侧边栏（在移动端放在主要内容之后） -->
        <div class="main-content-area">
          <!-- 热门商品 -->
          <div class="hot-section">
            <ProductHot />
          </div>
          
          <!-- 使用静态分类数据 -->
          <div class="phone-section">
            <CategoryProducts :category-id="24" category-name="精品男装" />
          </div>
          
          <div class="office-section">
            <CategoryProducts :category-id="31" category-name="黄金首饰" />
          </div>
        </div>
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
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { Search } from '@element-plus/icons-vue'
import IndexBanner from '@/views/index/indexBanner.vue'
import ProductHot from '@/views/index/productHot.vue'
import IndexCate from '@/views/index/indexCate.vue'
import CategoryProducts from '@/views/index/CategoryProducts.vue'

const router = useRouter()
const store = useStore()
const searchKeyword = ref('')

// 使用 Vuex 的用户信息
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
      // 强制清除本地存储并跳转到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    })
  } else {
    // 如果 store 没有 dispatch 方法，直接清除
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }
}

// 页面加载时检查用户登录状态
onMounted(() => {
  // 如果 Vuex 中没有用户信息，尝试从 localStorage 恢复
  if (!userInfo.value.name) {
    const token = localStorage.getItem('token')
    if (token) {
      // 如果有 token，可以尝试获取用户信息
      // 这里可以根据你的实际 API 进行调整
      console.log('检测到 token，但用户信息未加载')
    }
  }
})
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 头部样式 */
.main-header {
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  padding: 15px 0;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 10px;
}

.logo img {
  height: 40px;
}

.brand-name {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 搜索框样式 */
.search-container {
  flex: 1;
  max-width: 600px;
  margin: 0 40px;
}

.search-box {
  width: 100%;
}

.search-input {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px 0 0 20px;
  border: none;
  padding-left: 20px;
}

.search-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 0 20px 20px 0;
  width: 60px;
}

/* 用户信息样式 */
.user-info {
  color: #666;
  cursor: pointer;
  min-width: 80px;
  text-align: right;
}

.user-name {
  color: #333;
  font-weight: 500;
}

.user-name:hover {
  color: #409eff;
}

.login-link {
  color: #409eff;
  font-weight: 500;
}

.login-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 主体内容 */
.main-content {
  flex: 1;
  width: 100%;
}

.banner-section {
  margin-bottom: 30px;
}

.content-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

.main-content-area {
  flex: 1;
  min-width: 300px;
}

.hot-section,
.phone-section,
.office-section {
  margin-bottom: 40px;
}

/* 页脚样式 */
.main-footer {
  background: #f8f8f8;
  border-top: 1px solid #eee;
  padding: 40px 0 20px;
}

.footer-content {
  max-width: 1200px;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

/* 响应式调整 */
@media (max-width: 992px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .service-section {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-wrap: wrap;
  }
  
  .search-container {
    order: 3;
    flex: 1 0 100%;
    margin: 15px 0 0 0;
  }
  
  .user-info {
    order: 2;
    min-width: auto;
  }
  
  .service-section {
    grid-template-columns: 1fr;
  }
}
</style>