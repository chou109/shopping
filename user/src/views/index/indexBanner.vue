<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import banner1 from '@/assets/banner1.webp';
import banner2 from '@/assets/banner2.jpg';
import banner3 from '@/assets/banner3.webp';
import banner4 from '@/assets/banner4.webp';
import BlindBox from '@/assets/BlindBox.png';
import { getFrontCategoryTree } from '@/api/system/category';

const router = useRouter()
const banners = ref([])
const categories = ref([])  // 存储分类数据
const loading = ref(false)
const carouselHeight = ref('400px')
const windowWidth = ref(window.innerWidth)
const hoveredCategory = ref(null)  // 当前悬停的分类ID
const isMouseOverMenu = ref(false)  // 鼠标是否在菜单区域内
let hoverResetTimer = null

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

const loadBanners = function () {
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
const loadCategories = async function () {
  try {
    const response = await getFrontCategoryTree()

    if (response.code === 200) {
      // 过滤出顶级分类（parentId=0）作为大类
      const topLevelCategories = response.data || []

      // 只显示前6个大类
      categories.value = topLevelCategories.slice(0, 6)

    } else {
      console.error('获取分类树失败:', response.msg)
      categories.value = []
    }
  } catch (error) {
    console.error('获取分类失败:', error)
    categories.value = []
  }
}

// 处理子分类点击
const handleSubCategoryClick = (subCategoryId) => {
  router.push({
    path: '/products',
    query: {
      categoryId: subCategoryId,
      searchType: 'category'
    }
  })
}

const handleCategoryActivation = (category) => {
  if (category.children?.length) {
    hoveredCategory.value = category.id
  } else {
    handleSubCategoryClick(category.id)
  }
}

const handleImageError = function (event) {
  console.error('图片加载失败:', event.target.src)
  event.target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwMCIgaGVpZ2h0PSI0MDAiIHZpZXdCb3g9IjAgMCAxMjAwIDQwMCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTIwMCIgaGVpZ2h0PSI0MDAiIGZpbGw9IiNlZWVlZWUiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjI0IiBmaWxsPSIjYWFhIj5CYW5uZXIgSW1hZ2U8L3RleHQ+PC9zdmc+'
}

// 鼠标进入菜单区域
const handleMouseEnterMenu = () => {
  isMouseOverMenu.value = true
  if (hoverResetTimer) {
    window.clearTimeout(hoverResetTimer)
    hoverResetTimer = null
  }
}

// 鼠标离开菜单区域
const handleMouseLeaveMenu = () => {
  isMouseOverMenu.value = false
  // 延迟清除悬停状态，避免快速切换时闪烁
  if (hoverResetTimer) window.clearTimeout(hoverResetTimer)
  hoverResetTimer = window.setTimeout(() => {
    hoverResetTimer = null
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
  if (hoverResetTimer) {
    window.clearTimeout(hoverResetTimer)
    hoverResetTimer = null
  }
})
</script>

<template>
  <div class="banner-container" v-loading="loading">
    <!-- 商品分类导航 - 独立于轮播图，固定在轮播图上方 -->
    <div class="home-category" v-if="categories.length > 0" @mouseenter="handleMouseEnterMenu"
      @mouseleave="handleMouseLeaveMenu">
      <ul class="menu">
        <li v-for="item in categories" :key="item.id" :class="{ 'active': hoveredCategory === item.id }"
          role="button" tabindex="0" :aria-expanded="hoveredCategory === item.id"
          :aria-label="`分类：${item.name}`" @mouseenter="hoveredCategory = item.id" @focus="hoveredCategory = item.id"
          @click="handleCategoryActivation(item)" @keydown.enter.prevent="handleCategoryActivation(item)"
          @keydown.space.prevent="handleCategoryActivation(item)">
          <div class="category-main">{{ item.name }}</div>
        </li>
      </ul>

      <!-- 统一的子目录层 - 所有子分类都显示在这个固定位置 -->
      <div class="layer" v-if="hoveredCategory && categories.find(c => c.id === hoveredCategory)">
        <ul>
          <li v-for="child in categories.find(c => c.id === hoveredCategory)?.children" :key="child.id"
            role="button" tabindex="0" :aria-label="`查看分类：${child.name}`"
            @click="handleSubCategoryClick(child.id)" @keydown.enter.prevent="handleSubCategoryClick(child.id)"
            @keydown.space.prevent="handleSubCategoryClick(child.id)">
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
      <el-carousel :interval="5000" :height="carouselHeight" indicator-position="outside"
        :arrow="windowWidth > 768 ? 'always' : 'never'">
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <router-link :to="banner.link" class="banner-item" :aria-label="`查看商品：${banner.title}`">
            <img :src="banner.imageUrl" :alt="banner.title || 'Banner'" class="banner-image"
              @error="handleImageError" />
            <div class="banner-content">
              <h3>{{ banner.title }}</h3>
              <p>{{ banner.description }}</p>
            </div>
          </router-link>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 右侧跳转模块 -->
    <router-link to="/product/blind-box" class="jump-module" aria-label="浏览随机商品盲盒">
      <div class="jump-content">
        <div class="jump-text">
          <div class="jump-text-large">随机商品盲盒<br>好货低价易购</div>
          <div class="jump-text-small">官方正品 省钱省心</div>
        </div>
        <div class="jump-image">
          <img :src="BlindBox" alt="促销图片">
        </div>
      </div>
    </router-link>
  </div>
</template>

<style scoped>
/* 页面主题色 */
:root {
  --primary-color: #ff0036;
  --hover-bg: rgba(255, 0, 54, 0.08);
  --active-bg: rgba(255, 0, 54, 0.15);
  --category-bg: #fff;
  --category-hover: #fff0f3;
  --category-border: #e0e0e0;
  --text-primary: #ff0036;
  --text-secondary: #333;
}

.banner-container {
  width: 1450px;
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
  border-radius: 0 0 0 0;
  overflow: hidden;
  position: relative;
  flex: 1;
  height: 400px;
  margin: 0;
  min-width: 0;
  /* 确保flex item可以收缩 */
}

/* 右侧跳转模块样式 */
.jump-module {
  width: 250px;
  height: 400px;
  background-color: #ff6034;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
  color: inherit;
  text-decoration: none;
}

.jump-content {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  color: white;
  justify-content: space-between;
  align-items: center;
}

.jump-text {
  flex: 0.6;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  width: 100%;
  padding-top: 20px;
}

.jump-text-large {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.3;
  margin-bottom: 8px;
  font-family: 'Microsoft YaHei', 'SimHei', sans-serif;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  letter-spacing: 1px;
}

.jump-text-small {
  font-size: 14px;
  opacity: 0.9;
  font-family: 'Microsoft YaHei', 'SimHei', sans-serif;
  letter-spacing: 0.5px;
}

.jump-image {
  flex: 0.8;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  width: 100%;
  padding-bottom: 20px;
}

.jump-image img {
  height: 180px;
  width: auto;
  max-width: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.jump-image:hover img {
  transform: scale(1.05);
}

.banner-item {
  position: relative;
  width: 100%;
  height: 100%;
  display: block;
  color: inherit;
  text-decoration: none;
  overflow: hidden;
}

.banner-item:focus-visible,
.jump-module:focus-visible {
  outline: 3px solid #ff0036;
  outline-offset: -4px;
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
  width: 250px;
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
  height: 66.67px;
  line-height: 66.67px;
  transition: all 0.3s ease;
  cursor: pointer;
  color: #000000;
  border-bottom: 1px solid var(--category-border);
  list-style: none !important;
  padding: 0 20px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu li:last-child {
  border-bottom: none;
}

.menu li:hover,
.menu li.active {
  background-color: var(--category-hover);
  color: #ff6034;
  font-weight: 500;
}

.menu li:focus-visible,
.layer li:focus-visible {
  outline: 3px solid var(--primary-color);
  outline-offset: -3px;
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
  font-size: 16px;
  font-weight: 400;
  transition: all 0.3s ease;
  width: 100%;
}

/* 子分类层样式优化 */
.layer {
  width: 300px;
  height: 400px;
  background-color: white;
  position: absolute;
  left: 250px;
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
  flex-direction: row;
  flex-wrap: wrap;
  gap: 0;
  padding: 0;
  margin: 0;
  list-style: none !important;
  height: 100%;
}

.layer li {
  width: 50%;
  /* 每行显示2个子分类 */
  height: 25%;
  /* 每列显示4个子分类 */
  line-height: 100px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: var(--text-secondary);
  padding: 0 15px;
  box-sizing: border-box;
  border: none;
  text-align: center;
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
    flex: 1;
    min-width: 0;
  }

  .layer {
    width: 300px;
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
    width: 200px;
    left: 180px;
    height: 320px;
  }

  .banner-wrapper {
    flex: 1;
    min-width: 0;
    height: 320px;
  }

  .jump-module {
    width: 250px;
    height: 320px;
  }

  .menu li {
    height: 35px;
    line-height: 35px;
    padding: 0 15px;
    font-size: 13px;
  }

  .layer li {
    width: 33.333%;
    /* 中屏每行显示3个 */
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
    display: block;
    position: static;
    width: 100%;
    height: auto;
    padding: 8px 12px;
    border: 0;
    border-radius: 0;
    box-shadow: none;
  }

  .layer ul {
    gap: 4px;
  }

  .layer li {
    width: 50%;
    height: auto;
    line-height: 1.4;
    padding: 8px;
  }

  .banner-wrapper {
    width: 100%;
    margin-left: 0;
    height: 250px;
  }

  .jump-module {
    width: 100%;
    height: auto;
    margin-top: 10px;
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
