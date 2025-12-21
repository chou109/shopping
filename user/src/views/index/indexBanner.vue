<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import banner1 from '@/assets/banner1.jpg';
import banner2 from '@/assets/banner2.jpg';
import banner3 from '@/assets/banner3.jpg';
import banner4 from '@/assets/banner4.jpg';
import { getFrontCategoryTree } from '@/api/system/category';

const router = useRouter()
const banners = ref([])
const categories = ref([])  // 存储分类数据
const loading = ref(false)
const carouselHeight = ref('400px')
const windowWidth = ref(window.innerWidth)
const hoveredCategory = ref(null)  // 当前悬停的分类ID
const isMouseOverMenu = ref(false)  // 鼠标是否在菜单区域内

// 根据屏幕宽度调整轮播图高度
const updateCarouselHeight = () => {
  const width = window.innerWidth
  windowWidth.value = width
  
  if (width < 480) {
    carouselHeight.value = '180px'
  } else if (width < 768) {
    carouselHeight.value = '250px'
  } else if (width < 992) {
    carouselHeight.value = '320px'
  } else {
    carouselHeight.value = '400px'
  }
}

const loadBanners = function() {
  loading.value = true
  
  // 添加4张轮播图
  banners.value = [
    { 
      id: 1, 
      imageUrl: banner1,
      title: '薄无止境',
      description: '时尚集成平板灯，芯片升级，纤薄SCM，品质提升',
      link: '/products?category=1'
    },
    { 
      id: 2, 
      imageUrl: banner2,
      title: '欧式纯铜台灯',
      description: '欧式经典复刻，岁月的痕迹',
      link: '/products?category=2'
    },
    { 
      id: 3, 
      imageUrl: banner3,
      title: '新品上市',
      description: '最新科技产品，引领时尚潮流',
      link: '/products?category=3'
    },
    { 
      id: 4, 
      imageUrl: banner4,
      title: '限时特惠',
      description: '精选商品，限时折扣，不容错过',
      link: '/products?category=4'
    }
  ]
  
  loading.value = false
}

// 加载分类数据
const loadCategories = async function() {
  try {
    const response = await getFrontCategoryTree()
    console.log('分类数据响应:', response)
    
    if (response.code === 200) {
      // 过滤出顶级分类（parentId=0）作为大类
      const topLevelCategories = response.data || []
      console.log('顶级分类:', topLevelCategories)
      
      // 只显示前6个大类
      categories.value = topLevelCategories.slice(0, 6)
      
      // 如果没有数据，使用默认数据
      if (categories.value.length === 0) {
        categories.value = getDefaultCategories()
      }
    } else {
      // API失败时使用默认数据
      console.error('获取分类树失败:', response.msg)
      categories.value = getDefaultCategories()
    }
  } catch (error) {
    console.error('获取分类失败:', error)
    // 使用默认数据
    categories.value = getDefaultCategories()
  }
}

// 获取默认分类数据
const getDefaultCategories = () => {
  return [
    { 
      id: 1, 
      name: '手机数码',
      children: [
        { id: 7, name: '手机通讯' },
        { id: 8, name: '手机配件' },
        { id: 9, name: '摄影摄像' },
        { id: 10, name: '数码配件' },
        { id: 11, name: '智能设备' },
        { id: 12, name: '时尚影音' },
        { id: 13, name: '电子教育' },
        { id: 14, name: '苹果配件' }
      ]
    },
    { 
      id: 2, 
      name: '电脑办公',
      children: [
        { id: 15, name: '笔记本' },
        { id: 16, name: '台式机' },
        { id: 17, name: '平板电脑' },
        { id: 18, name: '办公设备' }
      ]
    },
    { 
      id: 3, 
      name: '家用电器',
      children: [
        { id: 19, name: '厨房电器' },
        { id: 20, name: '生活电器' },
        { id: 21, name: '大家电' },
        { id: 22, name: '个护健康' }
      ]
    },
    { 
      id: 4, 
      name: '服装服饰',
      children: [
        { id: 23, name: '女装' },
        { id: 24, name: '男装' },
        { id: 25, name: '童装' },
        { id: 26, name: '内衣' }
      ]
    },
    { 
      id: 5, 
      name: '美妆护肤',
      children: [
        { id: 27, name: '面部护肤' },
        { id: 28, name: '彩妆' },
        { id: 29, name: '香水' },
        { id: 30, name: '美妆工具' }
      ]
    },
    { 
      id: 6, 
      name: '黄金首饰',
      children: [
        { id: 31, name: '黄金' },
        { id: 32, name: '钻石' },
        { id: 33, name: '银饰' },
        { id: 34, name: '玉石' }
      ]
    }
  ]
}

// 处理子分类点击
const handleSubCategoryClick = (subCategoryId) => {
  console.log('点击子分类:', subCategoryId)
  router.push({ 
    path: '/products', 
    query: { 
      categoryId: subCategoryId,
      searchType: 'category'
    }
  })
}

const handleBannerClick = function(banner) {
  if (banner.link) {
    router.push(banner.link)
  }
}

const handleImageError = function(event) {
  console.error('图片加载失败:', event.target.src)
  event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwMCIgaGVpZ2h0PSI0MDAiIHZpZXdCb3g9IjAgMCAxMjAwIDQwMCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTIwMCIgaGVpZ2h0PSI0MDAiIGZpbGw9IiNlZWVlZWUiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjI0IiBmaWxsPSIjYWFhIj5CYW5uZXIgSW1hZ2U8L3RleHQ+PC9zdmc+'
}

// 鼠标进入菜单区域
const handleMouseEnterMenu = () => {
  isMouseOverMenu.value = true
}

// 鼠标离开菜单区域
const handleMouseLeaveMenu = () => {
  isMouseOverMenu.value = false
  // 延迟清除悬停状态，避免快速切换时闪烁
  setTimeout(() => {
    if (!isMouseOverMenu.value) {
      hoveredCategory.value = null
    }
  }, 200)
}

onMounted(() => {
  loadBanners()
  loadCategories()
  updateCarouselHeight()
  window.addEventListener('resize', updateCarouselHeight)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateCarouselHeight)
})
</script>

<template>
  <div class="banner-container" v-loading="loading">
    <!-- 商品分类导航 - 独立于轮播图，固定在轮播图上方 -->
    <div class="home-category" v-if="categories.length > 0"
         @mouseenter="handleMouseEnterMenu"
         @mouseleave="handleMouseLeaveMenu">
      <ul class="menu">
        <li v-for="item in categories" :key="item.id" 
            :class="{ 'active': hoveredCategory === item.id }"
            @mouseenter="hoveredCategory = item.id">
          <div class="category-main">{{ item.name }}</div>
        </li>
      </ul>
      
      <!-- 统一的子目录层 - 所有子分类都显示在这个固定位置 -->
      <div class="layer" v-if="hoveredCategory && categories.find(c => c.id === hoveredCategory)">
        <ul>
          <li v-for="child in categories.find(c => c.id === hoveredCategory)?.children" 
              :key="child.id" 
              @click="handleSubCategoryClick(child.id)">
            <div class="sub-category-item">
              <p class="name ellipsis-2">
                {{ child.name }}
              </p>
            </div>
          </li>
        </ul>
      </div>
    </div>
    
    <!-- 轮播图 - 分类导航不随轮播图轮播 -->
    <div class="banner-wrapper">
      <el-carousel 
        :interval="5000" 
        :height="carouselHeight" 
        indicator-position="outside"
        :arrow="windowWidth > 768 ? 'always' : 'never'"
      >
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <div class="banner-item" @click="handleBannerClick(banner)">
            <img 
              :src="banner.imageUrl" 
              :alt="banner.title || 'Banner'" 
              class="banner-image"
              @error="handleImageError"
            />
            <div class="banner-content">
              <h3>{{ banner.title }}</h3>
              <p>{{ banner.description }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
  </div>
</template>

<style scoped>
/* 页面主题色 */
:root {
  --primary-color: #409eff; /* Element Plus 主题蓝色 */
  --hover-bg: rgba(64, 158, 255, 0.1);
  --active-bg: rgba(64, 158, 255, 0.2);
}

.banner-container {
  width: 1200px;
  margin: 0 auto 20px auto;
  position: relative;
  min-height: 417px;
}

.banner-wrapper {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
  width: 1000px;
  margin-left: 200px;
  height: 400px;
}

.banner-item {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: pointer;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.banner-item:hover .banner-image {
  transform: scale(1.05);
}

.banner-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: white;
  padding: 15px 25px;
  text-align: center;
  z-index: 1;
}

.banner-content h3 {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.banner-content p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
  line-height: 1.4;
}

/* 商品分类导航样式 - 固定在轮播图上方，不随轮播图轮播 */
.home-category {
  width: 200px;
  height: 417px;
  background-color: #f5f5f5; /* 浅灰色背景 */
  position: absolute;
  left: 0;
  top: 0;
  z-index: 3;
  margin-bottom: 0;
  border-radius: 12px 0 0 12px;
  border: 1px solid #e0e0e0; /* 添加边框 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.menu {
  list-style: none !important; /* 强制移除列表标记 */
  padding: 0;
  margin: 0;
}

.menu li {
  position: relative;
  padding-left: 40px;
  height: 55px;
  line-height: 55px;
  transition: all 0.3s ease;
  cursor: pointer;
  color: #333; /* 黑色文字 */
  border-bottom: 1px solid #e8e8e8;
  /* 彻底移除列表标记 */
  list-style-type: none !important;
  list-style: none !important;
}

.menu li:last-child {
  border-bottom: none;
}

.menu li:hover,
.menu li.active {
  background-color: var(--primary-color); /* 主题色背景 */
  color: rgb(91, 70, 193); /* 悬停时的文字颜色 */
}

.menu li:hover .category-main,
.menu li.active .category-main {
  color: rgb(91, 70, 193); /* 确保悬停时文字的颜色 */
}

.category-main {
  margin-left: -30px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  /* 移除列表标记的伪元素 */
  &::before {
    content: none !important;
    display: none !important;
  }
}

.layer {
  width: 200px;
  height: 417px;
  background-color: #f8f8f8; /* 浅灰色背景 */
  position: absolute;
  left: 200px;
  top: 0;
  border-radius: 0 12px 12px 0;
  z-index: 4;
  border: 1px solid #e0e0e0;
  border-left: none;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
}

.layer ul {
  display: flex;
  flex-wrap: wrap;
  padding: 0;
  margin: 0;
  list-style: none !important; /* 强制移除列表标记 */
}

.layer li {
  width: 200px;
  height: 52px;
  line-height: 52px;
  padding-left: 40px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #333; /* 黑色文字 */
  border-bottom: 1px solid #e8e8e8;
  /* 彻底移除列表标记 */
  list-style-type: none !important;
  list-style: none !important;
}

.layer li:last-child {
  border-bottom: none;
}

.layer li:hover {
  background-color: var(--primary-color); /* 主题色背景 */
  color: rgb(91, 70, 193); /* 悬停时的文字颜色 */
}

.sub-category-item {
  margin-left: -30px;
}

.name {
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  transition: color 0.3s ease;
}

.layer li:hover .name {
  color: rgb(91, 70, 193); /* 确保悬停时文字的颜色 */
}

.ellipsis-2 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2; /* 补充标准属性 */
}

/* 彻底移除所有列表标记 */
.home-category ul,
.home-category li,
.layer ul,
.layer li {
  list-style-type: none !important;
  list-style: none !important;
}

.home-category li::marker,
.layer li::marker {
  display: none !important;
  content: none !important;
}

/* 移除列表标记 */
ul li::marker {
  display: none;
}

/* 响应式调整 - 在较小屏幕上恢复自适应 */
@media (max-width: 1240px) {
  .banner-container {
    width: 95%;
    max-width: 1200px;
  }
  
  .banner-wrapper {
    width: calc(100% - 200px);
    margin-left: 200px;
  }
}

@media (max-width: 992px) {
  .banner-container {
    width: 95%;
    margin: 0 auto 20px auto;
  }
  
  .home-category {
    width: 180px;
    height: 320px;
  }
  
  .layer {
    width: 180px;
    left: 180px;
    height: 320px;
  }
  
  .banner-wrapper {
    width: calc(100% - 180px);
    margin-left: 180px;
    height: 320px;
  }
  
  .menu li {
    padding-left: 30px;
    height: 50px;
    line-height: 50px;
  }
  
  .category-main {
    margin-left: -20px;
    font-size: 15px;
  }
  
  .layer li {
    width: 180px;
    height: 48px;
    line-height: 48px;
    padding-left: 30px;
  }
  
  .banner-content {
    padding: 12px 20px;
  }
  
  .banner-content h3 {
    font-size: 20px;
  }
  
  .banner-content p {
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .banner-container {
    width: 95%;
    margin: 0 auto 20px auto;
  }
  
  .home-category {
    width: 160px;
    height: 250px;
  }
  
  .layer {
    width: 160px;
    left: 160px;
    height: 250px;
  }
  
  .banner-wrapper {
    width: calc(100% - 160px);
    margin-left: 160px;
    height: 250px;
  }
  
  .menu li {
    padding-left: 25px;
    height: 45px;
    line-height: 45px;
  }
  
  .category-main {
    margin-left: -15px;
    font-size: 14px;
  }
  
  .layer li {
    width: 160px;
    height: 44px;
    line-height: 44px;
    padding-left: 25px;
  }
  
  .banner-content {
    padding: 10px 15px;
  }
  
  .banner-content h3 {
    font-size: 18px;
    margin-bottom: 5px;
  }
  
  .banner-content p {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .banner-container {
    width: 100%;
    margin: 0 auto 20px auto;
    padding: 0 10px;
  }
  
  .home-category {
    display: none;
  }
  
  .banner-wrapper {
    margin-left: 0;
    width: 100%;
    height: 180px;
  }
  
  .banner-content {
    padding: 8px 12px;
  }
  
  .banner-content h3 {
    font-size: 16px;
  }
  
  .banner-content p {
    font-size: 11px;
    display: none;
  }
}

/* 轮播图指示器样式 */
:deep(.el-carousel__indicator) {
  padding: 6px 3px;
}

:deep(.el-carousel__button) {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
}

:deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background-color: #fff;
}

/* 轮播图箭头样式 */
:deep(.el-carousel__arrow) {
  background-color: rgba(255, 255, 255, 0.8);
  color: #333;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

:deep(.el-carousel__arrow:hover) {
  background-color: rgba(255, 255, 255, 0.95);
}

:deep(.el-carousel__arrow i) {
  font-size: 16px;
  font-weight: bold;
}

/* 在移动端隐藏箭头 */
@media (max-width: 768px) {
  :deep(.el-carousel__arrow) {
    display: none;
  }
  
  :deep(.el-carousel__indicator) {
    padding: 4px 2px;
  }
  
  :deep(.el-carousel__button) {
    width: 5px;
    height: 5px;
  }
}
</style>