<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <nav class="nav-header">
      <div class="container">
        <div class="logo">
          <img src="@/assets/eshop.png" @click="toIndex()" alt="ESHOP" />
        </div>
        <div class="header-search">
          <div class="wrapper">
            <input type="text" name="keyword" placeholder="搜索商品..." v-model="searchKeyword" @keyup.enter="handleSearch">
            <a href="javascript:;" @click="handleSearch">搜索</a>
          </div>
        </div>
      </div>
    </nav>

    <!-- 主体内容 -->
    <div class="content-wrapper">
      <slot></slot>
    </div>

    <!-- 页脚 -->
    <footer class="home-footer">
      <div class="footer-content">
        <!-- 服务承诺部分 -->
        <div class="service-promise">
          <div class="service-item">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M14 7L8 12V20H16V12L10 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 4V2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M16 4V2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M8 4V2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <span>商品由店铺发布</span>
          </div>
          
          <div class="service-item">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M3 9L12 2L21 9V20C21 20.5304 20.7893 21.0391 20.4142 21.4142C20.0391 21.7893 19.5304 22 19 22H5C4.46957 22 3.96086 21.7893 3.58579 21.4142C3.21071 21.0391 3 20.5304 3 20V9Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M9 22V12H15V22" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span>订单状态可在订单页查看</span>
          </div>
          
          <div class="service-item">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span>售后按订单状态办理</span>
          </div>
          
          <div class="service-item">
            <div class="service-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M16 3H1V16H16V3Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 8H20L23 11V16H16V8Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M5.5 21C6.88071 21 8 19.8807 8 18.5C8 17.1193 6.88071 16 5.5 16C4.11929 16 3 17.1193 3 18.5C3 19.8807 4.11929 21 5.5 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M18.5 21C19.8807 21 21 19.8807 21 18.5C21 17.1193 19.8807 16 18.5 16C17.1193 16 16 17.1193 16 18.5C16 19.8807 17.1193 21 18.5 21Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span>可通过客服咨询店铺</span>
          </div>
        </div>
        
        <!-- 品牌和链接部分 -->
        <div class="footer-info">
          <div class="brand-section">
            <h3 class="brand-title">ESHOP</h3>
            <p class="brand-slogan">品质生活，触手可及</p>
          </div>
          
          <div class="link-section">
            <div class="footer-links">
              <router-link to="/index" class="footer-link">商城首页</router-link>
              <span class="link-divider">|</span>
              <router-link to="/products" class="footer-link">全部商品</router-link>
              <span class="link-divider">|</span>
              <router-link to="/customer-service" class="footer-link">在线客服</router-link>
              <span class="link-divider">|</span>
              <router-link to="/user/orders" class="footer-link">我的订单</router-link>
            </div>
            <p class="copyright">Copyright ©2023 eshop.com All Rights Reserved.</p>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchKeyword = ref('')

const toIndex = () => {
  router.push('/')
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/products',
      query: { 
        keyword: searchKeyword.value.trim(),
        searchType: 'keyword' // 添加搜索类型，便于后端处理
      }
    })
    // 清空搜索框
    searchKeyword.value = ''
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
}

/* 导航栏样式 */
.nav-header {
  width: 100%;
  background: linear-gradient(135deg, #ff5500 0%, #ff9900 100%);
  padding: 10px 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.nav-header .container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 0 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.logo img {
  height: 40px;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.logo img:hover {
  transform: scale(1.05);
}

.header-search {
  flex: 0 0 400px;
}

.header-search .wrapper {
  height: 40px;
  border: 2px solid #409eff;
  border-radius: 20px;
  display: flex;
  align-items: center;
  background: white;
  overflow: hidden;
  transition: all 0.3s ease;
}

.header-search .wrapper:hover {
  border-color: #66b1ff;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
}

.header-search input {
  border: none;
  box-sizing: border-box;
  border-right: 1px solid #e0e0e0;
  flex: 1;
  height: 100%;
  padding: 0 15px;
  font-size: 14px;
  outline: none;
}

.header-search input::placeholder {
  color: #999;
}

.header-search a {
  width: 80px;
  height: 100%;
  background: #409eff;
  color: white;
  text-decoration: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.header-search a:hover {
  background: #66b1ff;
}

/* 主体内容区域 */
.content-wrapper {
  flex: 1;
  width: 100%;
  max-width: 1500px;
  margin: 0 auto;
  padding: 20px 15px;
  box-sizing: border-box;
}

/* 页脚样式 - 修改为与导航栏相同风格 */
.home-footer {
  width: 100%;
  background: linear-gradient(135deg, #ff5500 0%, #ff9900 100%);
  color: #fff;
  padding: 40px 0 20px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.footer-content {
  max-width: 1500px;
  margin: 0 auto;
  padding: 0 15px;
}

/* 服务承诺部分 */
.service-promise {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
  transition: transform 0.3s ease;
}

.service-item:hover {
  transform: translateY(-5px);
}

.service-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}

.service-icon svg {
  width: 24px;
  height: 24px;
}

.service-item span {
  font-size: 15px;
  font-weight: 500;
  color: #ecf0f1;
}

/* 品牌和链接部分 */
.footer-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 40px;
}

.brand-section {
  flex: 1;
  min-width: 200px;
}

.brand-title {
  font-size: 32px;
  font-weight: bold;
  color: #fff;
  margin: 0 0 10px 0;
  letter-spacing: 1px;
}

.brand-slogan {
  font-size: 14px;
  color: #bdc3c7;
  margin: 0;
}

.link-section {
  flex: 2;
  min-width: 300px;
}

.footer-links {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.footer-link {
  color: #bdc3c7;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
  padding: 4px 0;
}

.footer-link:hover {
  color: #409eff;
}

.link-divider {
  color: rgba(255, 255, 255, 0.2);
}

.copyright {
  font-size: 13px;
  color: #95a5a6;
  margin: 0;
  line-height: 1.5;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .nav-header .container {
    height: 50px;
  }
  
  .logo img {
    height: 35px;
  }
  
  .header-search {
    flex: 0 0 300px;
  }
  
  .service-promise {
    grid-template-columns: repeat(2, 1fr);
    gap: 30px;
  }
  
  .footer-info {
    flex-direction: column;
    gap: 30px;
  }
  
  .brand-section, .link-section {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .nav-header .container {
    flex-direction: column;
    height: auto;
    padding: 15px;
    gap: 15px;
  }
  
  .logo img {
    height: 30px;
  }
  
  .header-search {
    width: 100%;
    flex: 0 0 auto;
  }
  
  .header-search .wrapper {
    max-width: 500px;
    margin: 0 auto;
  }
  
  .content-wrapper {
    padding: 15px;
  }
  
  .home-footer {
    padding: 30px 0 15px;
  }
  
  .service-promise {
    grid-template-columns: 1fr;
    gap: 25px;
    margin-bottom: 30px;
    padding-bottom: 25px;
  }
  
  .service-item {
    flex-direction: row;
    justify-content: flex-start;
    text-align: left;
    gap: 15px;
  }
  
  .service-icon {
    flex-shrink: 0;
  }
  
  .brand-title {
    font-size: 28px;
  }
  
  .footer-links {
    justify-content: center;
  }
  
  .copyright {
    text-align: center;
  }
}

@media (max-width: 480px) {
  .nav-header .container {
    padding: 10px;
  }
  
  .header-search {
    width: 100%;
  }
  
  .header-search input {
    width: calc(100% - 80px);
  }
  
  .home-footer {
    padding: 25px 0 12px;
  }
  
  .service-promise {
    gap: 20px;
    margin-bottom: 25px;
    padding-bottom: 20px;
  }
  
  .brand-title {
    font-size: 24px;
  }
  
  .footer-links {
    gap: 10px;
  }
  
  .footer-link {
    font-size: 13px;
  }
  
  .copyright {
    font-size: 12px;
  }
}

@media (max-width: 360px) {
  .service-item {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
  
  .footer-links {
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }
  
  .link-divider {
    display: none;
  }
}
</style>
