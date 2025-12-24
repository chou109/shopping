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
  
  // 为每个banner添加对应的商品ID
  banners.value = [
    { 
      id: 1, 
      imageUrl: banner1,
      title: '小米(MI) L65M4-AQ电视',
      description: '全高清屏，纤薄金属',
      productId: 48,
      link: '/product/detail?id=48'
    },
    { 
      id: 2, 
      imageUrl: banner2,
      title: '培罗蒙(BAROMON) 西服套装',
      description: '柔软舒适，质感档次',
      productId: 103,
      link: '/product/detail?id=103'
    },
    { 
      id: 3, 
      imageUrl: banner3,
      title: '苹果(Apple) MacBook',
      description: '纤薄，轻巧，强劲',
      productId: 41,
      link: '/product/detail?id=41'
    },
    { 
      id: 4, 
      imageUrl: banner4,
      title: '马克华菲(Mark Fairwhale) 韩版衬衫',
      description: '光泽软和，时尚修身',
      productId: 118,
      link: '/product/detail?id=118'
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

// 修改：处理轮播图点击，跳转到商品详情页
const handleBannerClick = function(banner) {
  if (banner.link) {
    router.push(banner.link)
  } else if (banner.productId) {
    // 如果link不存在但有productId，也跳转到商品详情
    router.push(`/product/detail?id=${banner.productId}`)
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
  --primary-color: #ff5500;
  --hover-bg: rgba(255, 85, 0, 0.08);
  --active-bg: rgba(255, 85, 0, 0.15);
  --category-bg: #f8f8f8;
  --category-hover: #fff5f0;
  --category-border: #e0e0e0;
  --text-primary: #ff5500;
  --text-secondary: #666;
}

.banner-container {
  width: 1400px;
  margin: 0 auto 20px auto;
  position: relative;
  min-height: 417px;
  display: flex;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.banner-wrapper {
  border-radius: 0 12px 12px 0;
  overflow: hidden;
  position: relative;
  width: 1000px;
  height: 400px;
  margin: 0;
  flex: 1;
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

/* 商品分类导航样式 - 淘宝样式优化 */
.home-category {
  width: 200px;
  height: 400px;
  background-color: var(--category-bg);
  position: relative;
  z-index: 3;
  border-right: 1px solid var(--category-border);
}

.menu {
  list-style: none !important;
  padding: 0;
  margin: 0;
  height: 100%;
}

.menu li {
  position: relative;
  height: 40px;
  line-height: 40px;
  transition: all 0.3s ease;
  cursor: pointer;
  color: var(--text-primary);
  border-bottom: 1px solid var(--category-border);
  list-style: none !important;
  padding: 0 20px;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.menu li:last-child {
  border-bottom: none;
}

.menu li:hover,
.menu li.active {
  background-color: var(--category-hover);
  color: var(--primary-color);
  font-weight: 500;
}

/* 淘宝样式箭头 */
.menu li:hover::after,
.menu li.active::after {
  content: "›";
  position: absolute;
  right: 15px;
  color: var(--primary-color);
  font-size: 16px;
  font-weight: bold;
}

.category-main {
  font-size: 14px;
  font-weight: 400;
  transition: all 0.3s ease;
  width: 100%;
}

/* 子分类层样式优化 */
.layer {
  width: 800px;
  height: 400px;
  background-color: white;
  position: absolute;
  left: 200px;
  top: 0;
  z-index: 4;
  border: 1px solid var(--category-border);
  border-left: none;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  border-radius: 0 12px 12px 0;
  padding: 20px;
  box-sizing: border-box;
}

.layer ul {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  padding: 0;
  margin: 0;
  list-style: none !important;
  height: 100%;
  align-content: flex-start;
}

.layer li {
  width: 25%; /* 每行显示4个子分类 */
  height: 36px;
  line-height: 36px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-secondary);
  padding: 0 15px;
  box-sizing: border-box;
  border: none;
  text-align: left;
}

.layer li:hover {
  background-color: transparent;
  color: var(--primary-color);
  font-weight: 500;
}

.sub-category-item {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
}

.name {
  font-size: 13px;
  margin: 0;
  transition: color 0.3s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.layer li:hover .name {
  color: var(--primary-color);
}

.ellipsis-2 {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
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

/* 响应式调整 */
@media (max-width: 1240px) {
  .banner-container {
    width: 95%;
    max-width: 1500px;
  }
  
  .banner-wrapper {
    width: calc(100% - 200px);
  }
  
  .layer {
    width: calc(100% - 200px);
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
    width: calc(100% - 180px);
    left: 180px;
    height: 320px;
  }
  
  .banner-wrapper {
    width: calc(100% - 180px);
    height: 320px;
  }
  
  .menu li {
    height: 35px;
    line-height: 35px;
    padding: 0 15px;
    font-size: 13px;
  }
  
  .layer li {
    width: 33.333%; /* 中屏每行显示3个 */
    height: 32px;
    line-height: 32px;
    padding: 0 12px;
  }
  
  .name {
    font-size: 12px;
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
    flex-direction: column;
    height: auto;
  }
  
  .home-category {
    width: 100%;
    height: auto;
    border-right: none;
    border-bottom: 1px solid var(--category-border);
  }
  
  .menu {
    display: flex;
    flex-wrap: wrap;
    height: auto;
  }
  
  .menu li {
    width: 33.333%;
    height: 40px;
    border-bottom: 1px solid var(--category-border);
    border-right: 1px solid var(--category-border);
    box-sizing: border-box;
  }
  
  .menu li:nth-child(3n) {
    border-right: none;
  }
  
  .menu li:nth-last-child(-n+3) {
    border-bottom: none;
  }
  
  .layer {
    display: none; /* 移动端隐藏子分类层 */
  }
  
  .banner-wrapper {
    width: 100%;
    margin-left: 0;
    height: 250px;
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
    padding: 0;
    border-radius: 0;
  }
  
  .home-category {
    display: none; /* 超小屏隐藏分类导航 */
  }
  
  .banner-wrapper {
    width: 100%;
    height: 180px;
    border-radius: 0;
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