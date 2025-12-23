<template>
  <div class="lazy-product-list-container">
    <!-- 排序选项（预留，后期可添加） -->
    <div class="sort-options" v-if="false">
      <div class="sort-container">
        <span class="sort-label">排序：</span>
        <el-radio-group v-model="sortType" @change="handleSortChange">
          <el-radio label="random">随机推荐</el-radio>
          <el-radio label="new">最新上架</el-radio>
          <el-radio label="hot">热门商品</el-radio>
          <el-radio label="price_asc">价格从低到高</el-radio>
          <el-radio label="price_desc">价格从高到低</el-radio>
        </el-radio-group>
      </div>
    </div>

    <!-- 商品列表 - 网格布局 -->
    <div class="products-container">
      <div class="products-grid">
        <div v-for="product in products" :key="product.id" class="product-item" @click="handleProductClick(product.id)">
          <div class="product-card">
            <div class="product-image-wrapper">
              <img :src="getProductImageUrl(product.mainImage)" :alt="product.name" class="product-image"
                @error="handleImageError" />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-subtitle">{{ product.subTitle }}</div>
              <div class="product-price-section">
                <span class="current-price">¥{{ formatPrice(product.price) }}</span>
                <span class="original-price" v-if="product.originalPrice">
                  ¥{{ formatPrice(product.originalPrice) }}
                </span>
              </div>
              <div class="product-meta">
                <span class="product-category" v-if="product.categoryName">
                  {{ product.categoryName }}
                </span>
                <span class="product-sales" v-if="product.salesCount !== undefined">
                  已售 {{ formatSalesCount(product.salesCount) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner">
          <el-icon class="loading-icon" :size="24">
            <Loading />
          </el-icon>
          <span class="loading-text">正在加载更多商品...</span>
        </div>
      </div>

      <!-- 没有更多数据 -->
      <div v-if="!loading && hasMore === false && products.length > 0" class="no-more-container">
        <div class="no-more-content">
          <el-icon class="no-more-icon">
            <CircleCheck />
          </el-icon>
          <span class="no-more-text">已经到底了，没有更多商品了</span>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && products.length === 0" class="empty-container">
        <el-empty description="暂无商品数据">
          <template #image>
            <el-icon :size="80">
              <Goods />
            </el-icon>
          </template>
          <el-button type="primary" @click="resetAndLoadProducts">重新加载</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Loading,
  CircleCheck,
  Goods
} from '@element-plus/icons-vue'
import { getProductImageUrl, handleImageError } from '@/utils/image'
import api from "@/utils/request.js"

const router = useRouter()

// 商品数据
const products = ref([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(20)

// 排序和筛选相关（预留接口）
const sortType = ref('random') // random, new, hot, price_asc, price_desc

// 网格列数计算
const gridColumnCount = computed(() => {
  const width = window.innerWidth
  if (width < 640) return 2      // 手机：2列
  if (width < 768) return 2      // 小平板：2列
  if (width < 1024) return 3     // 大平板：3列
  if (width < 1280) return 4     // 小电脑：4列
  return 5                       // 大电脑：5列
})

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 格式化销量
const formatSalesCount = (salesCount) => {
  if (salesCount >= 10000) {
    return (salesCount / 10000).toFixed(1) + '万件'
  } else if (salesCount >= 1000) {
    return (salesCount / 1000).toFixed(1) + '千件'
  }
  return salesCount + '件'
}

// 加载商品数据
const loadProducts = async (reset = false) => {
  if (loading.value) return

  if (reset) {
    currentPage.value = 1
    products.value = []
    hasMore.value = true
  }

  if (!hasMore.value) return

  loading.value = true

  try {
    const response = await api({
      url: '/system/product/list/random',
      method: 'get',
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        sortType: sortType.value,
        random: sortType.value === 'random' ? 1 : 0,
        needSalesCount: 1 // 预留参数，告诉后端需要返回销量数据
      }
    })

    if (response.code === 200) {
      const newProducts = response.rows || response.data || []

      // 为商品添加销量数据
      // 后期后端接口完善后，这里应该直接使用后端返回的salesCount字段
      const enhancedProducts = newProducts.map(product => ({
        ...product,
        // 这里模拟一些销量数据，实际应该从后端获取
        // 如果后端返回了salesCount，就用后端的；如果没有，可以显示默认值或不显示
        salesCount: product.salesCount || Math.floor(Math.random() * 1000)
      }))

      if (reset) {
        products.value = enhancedProducts
      } else {
        products.value = [...products.value, ...enhancedProducts]
      }

      // 判断是否还有更多数据
      const total = response.total || 0
      if (products.value.length >= total || newProducts.length < pageSize.value) {
        hasMore.value = false
      } else {
        currentPage.value++
        hasMore.value = true
      }
    } else {
      ElMessage.error(response.msg || '加载商品失败')
      hasMore.value = false
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败，请稍后重试')
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

// 监听滚动，实现懒加载
let scrollListener = null
const setupScrollListener = () => {
  scrollListener = () => {
    if (loading.value || !hasMore.value) return

    const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
    const windowHeight = window.innerHeight
    const scrollHeight = document.documentElement.scrollHeight

    // 当滚动到底部 200px 内时加载更多
    if (scrollTop + windowHeight >= scrollHeight - 200) {
      loadProducts()
    }
  }

  window.addEventListener('scroll', scrollListener)
}

// 移除滚动监听
const removeScrollListener = () => {
  if (scrollListener) {
    window.removeEventListener('scroll', scrollListener)
  }
}

// 商品点击事件
const handleProductClick = (productId) => {
  router.push({
    path: '/product/detail',
    query: { id: productId }
  })
}

// 排序改变事件（预留）
const handleSortChange = () => {
  resetAndLoadProducts()
}

// 重置并重新加载商品
const resetAndLoadProducts = () => {
  loadProducts(true)
}

// 初始加载
onMounted(() => {
  loadProducts(true)
  setupScrollListener()
})

onUnmounted(() => {
  removeScrollListener()
})

// 暴露给父组件的方法
defineExpose({
  resetAndLoadProducts
})
</script>

<style scoped>
.lazy-product-list-container {
  width: 100%;
}

/* 排序选项样式 */
.sort-options {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.sort-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

.sort-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

/* 商品列表容器 */
.products-container {
  position: relative;
  min-height: 200px;
}

/* 网格布局 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

/* 商品项样式 */
.product-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-item:hover {
  transform: translateY(-5px);
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-item:hover .product-card {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
  border-color: #e0e0e0;
}

.product-image-wrapper {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-item:hover .product-image {
  transform: scale(1.05);
}

.product-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-subtitle {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
  line-height: 1.4;
  height: 34px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  flex-shrink: 0;
}

.product-price-section {
  display: flex;
  align-items: baseline;
  gap: 5px;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.current-price {
  color: #ff0036;
  font-size: 18px;
  font-weight: bold;
}

.original-price {
  color: #999;
  font-size: 12px;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.product-category,
.product-sales {
  font-size: 11px;
  padding: 3px 6px;
  border-radius: 3px;
  background: #f5f5f5;
}

.product-category {
  color: #409eff;
}

.product-sales {
  color: #db780e;
}

/* 加载状态样式 */
.loading-container {
  padding: 30px 0;
  text-align: center;
}

.loading-spinner {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 15px 25px;
  background: #f8f9fa;
  border-radius: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  color: #666;
  font-size: 14px;
}

/* 没有更多数据样式 */
.no-more-container {
  padding: 30px 0;
  text-align: center;
}

.no-more-content {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 15px 25px;
  background: #f0f9ff;
  border-radius: 25px;
  color: #409eff;
  font-size: 14px;
}

.no-more-icon {
  color: #67c23a;
}

/* 空状态样式 */
.empty-container {
  padding: 40px 0;
  text-align: center;
}

.empty-container .el-empty {
  padding: 30px;
  background: #f9f9f9;
  border-radius: 12px;
}

/* 响应式调整 - 5列布局适配 */
@media (max-width: 1600px) {
  .products-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 18px;
  }

  .product-image-wrapper {
    height: 160px;
  }
}

@media (max-width: 1400px) {
  .products-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
  }

  .product-image-wrapper {
    height: 150px;
  }

  .product-info {
    padding: 12px;
  }

  .product-name {
    font-size: 13px;
    height: 36px;
  }

  .current-price {
    font-size: 16px;
  }
}

@media (max-width: 1280px) {
  .products-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
  }

  .product-image-wrapper {
    height: 180px;
  }
}

@media (max-width: 1024px) {
  .products-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 18px;
  }

  .product-image-wrapper {
    height: 200px;
  }
}

@media (max-width: 768px) {
  .sort-container {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .sort-label {
    font-size: 13px;
  }

  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .product-image-wrapper {
    height: 180px;
  }

  .product-info {
    padding: 12px;
  }

  .product-name {
    font-size: 13px;
    height: 36px;
  }

  .current-price {
    font-size: 16px;
  }
}

@media (max-width: 640px) {
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .product-image-wrapper {
    height: 160px;
  }

  .product-info {
    padding: 10px;
  }

  .product-name {
    font-size: 12px;
    height: 34px;
  }

  .product-subtitle {
    font-size: 11px;
    height: 32px;
  }

  .current-price {
    font-size: 15px;
  }

  .product-meta {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .sort-options {
    padding: 10px;
  }

  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .product-image-wrapper {
    height: 150px;
  }

  .product-name {
    font-size: 12px;
    height: 32px;
  }

  .product-subtitle {
    font-size: 10px;
    height: 30px;
  }

  .current-price {
    font-size: 14px;
  }

  .product-meta {
    font-size: 10px;
  }
}

@media (max-width: 360px) {
  .products-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .product-image-wrapper {
    height: 200px;
  }
}
</style>