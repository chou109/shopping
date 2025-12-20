<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import banner1 from '@/assets/banner1.jpg';
import banner2 from '@/assets/banner2.jpg';

const router = useRouter()
const banners = ref([])
const loading = ref(false)
const carouselHeight = ref('400px')
const windowWidth = ref(window.innerWidth) // 添加windowWidth变量

// 根据屏幕宽度调整轮播图高度
const updateCarouselHeight = () => {
  const width = window.innerWidth
  windowWidth.value = width // 更新窗口宽度
  
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
    }
  ]
  
  loading.value = false
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

onMounted(() => {
  loadBanners()
  updateCarouselHeight()
  window.addEventListener('resize', updateCarouselHeight)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateCarouselHeight)
})
</script>

<template>
  <div class="banner-container" v-loading="loading">
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
.banner-container {
  width: 100%;
  margin-bottom: 20px;
}

.banner-wrapper {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
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

/* 响应式调整 */
@media (max-width: 992px) {
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
  .banner-content {
    padding: 8px 12px;
  }
  
  .banner-content h3 {
    font-size: 16px;
  }
  
  .banner-content p {
    font-size: 11px;
    display: none; /* 在非常小的屏幕上隐藏描述文字 */
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