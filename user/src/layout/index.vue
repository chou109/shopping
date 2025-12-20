<template>
  <div class="home-container">
    <div class="banner-section">
      <IndexBanner />
    </div>
    
    <div class="content-wrapper">
      <!-- 侧边栏（在移动端放在主要内容之后） -->
      <div class="main-content">
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
        
        <!-- <div class="appliance-section">
          <CategoryProducts :category-id="3" category-name="家用电器" />
        </div>
        
        <div class="clothing-section">
          <CategoryProducts :category-id="4" category-name="服装服饰" />
        </div>
        
        <div class="beauty-section">
          <CategoryProducts :category-id="5" category-name="美妆护肤" />
        </div> -->
      </div>
      
      <div class="sidebar-section">
        <IndexCate />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import IndexBanner from '@/views/index/IndexBanner.vue'
import ProductHot from '@/views/index/ProductHot.vue'
import IndexCate from '@/views/index/IndexCate.vue'
import CategoryProducts from '@/views/index/CategoryProducts.vue'

onMounted(() => {
  console.log('首页加载完成')
})
</script>

<style scoped>
/* 保持原有样式不变 */
.home-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.banner-section {
  margin-bottom: 30px;
}

.content-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 40px;
}

.main-content {
  flex: 1;
  min-width: 300px;
}

.sidebar-section {
  flex: 0 0 250px;
}

.hot-section,
.phone-section,
.office-section,
.appliance-section,
.clothing-section,
.beauty-section {
  margin-bottom: 40px;
}

/* 平板设备 */
@media (max-width: 992px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .sidebar-section {
    flex: 0 0 auto;
    width: 100%;
    order: -1;
    margin-bottom: 20px;
  }
}

/* 手机设备 */
@media (max-width: 768px) {
  .home-container {
    padding: 15px;
  }
  
  .content-wrapper {
    gap: 15px;
  }
  
  .banner-section {
    margin-bottom: 20px;
  }
  
  .hot-section,
  .phone-section,
  .office-section,
  .appliance-section,
  .clothing-section,
  .beauty-section {
    margin-bottom: 30px;
  }
  
  .sidebar-section {
    width: 100%;
  }
}

/* 小屏手机 */
@media (max-width: 480px) {
  .home-container {
    padding: 12px;
  }
  
  .content-wrapper {
    gap: 12px;
  }
  
  .banner-section {
    margin-bottom: 15px;
  }
  
  .hot-section,
  .phone-section,
  .office-section,
  .appliance-section,
  .clothing-section,
  .beauty-section {
    margin-bottom: 25px;
  }
}
</style>