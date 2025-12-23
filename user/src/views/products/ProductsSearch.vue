<template>
  <div class="search-container">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-nav">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="keyword">搜索"{{ keyword }}"</el-breadcrumb-item>
        <el-breadcrumb-item v-else-if="categoryName">分类: {{ categoryName }}</el-breadcrumb-item>
        <el-breadcrumb-item v-else>商品列表</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 搜索头部 -->
    <div class="search-header">
      <h2>搜索结果</h2>
      <p>{{ pageTitle }}</p>
    </div>
    
    <!-- 分类筛选选项卡 -->
    <div class="filter-tabs">
      <el-radio-group v-model="activeFilter" @change="handleFilterChange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="product">商品</el-radio-button>
        <el-radio-button label="shop">店铺</el-radio-button>
      </el-radio-group>
      
      <!-- 统计信息 -->
      <div class="filter-stats">
        <span class="stat-item">共 {{ totalCount }} 条结果</span>
        <span class="stat-item" v-if="activeFilter === 'product'">商品: {{ filteredProducts.length }} 个</span>
        <span class="stat-item" v-if="activeFilter === 'shop'">店铺: {{ filteredShops.length }} 个</span>
      </div>
    </div>
    
    <!-- 搜索结果 -->
    <div v-loading="loading" class="search-results">
      <!-- 全部或商品筛选 -->
      <div v-if="activeFilter === 'all' || activeFilter === 'product'" class="product-section">
        <div v-if="filteredProducts.length > 0" class="product-list">
          <div class="search-stats">
            <span>找到 {{ filteredProducts.length }} 个相关商品</span>
          </div>
          
          <div class="product-grid">
            <div class="product-item" v-for="item in filteredProducts" :key="item.id" 
                 @click="handleProductClick(item.id)">
              <div class="product-card">
                <div class="product-image">
                  <img :src="getProductImageUrl(item.mainImage)" :alt="item.name" @error="handleImageError" />
                  <div class="product-badge">商品</div>
                </div>
                <div class="product-info">
                  <h3 class="product-name">{{ item.name }}</h3>
                  <div class="product-price">¥{{ formatPrice(item.price) }}</div>
                  <div class="product-desc">{{ item.subTitle }}</div>
                  <div class="product-category">分类: {{ item.categoryName || item.category?.name || '未分类' }}</div>
                  <div class="product-meta">
                    <span class="product-sales" v-if="item.salesCount">已售 {{ formatSalesCount(item.salesCount) }}</span>
                    <span class="product-stock" v-if="item.stock">库存 {{ item.stock }} 件</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="activeFilter === 'product' && !loading" class="empty-section">
          <el-empty description="暂无相关商品">
            <el-button type="primary" @click="resetSearch">重新搜索</el-button>
          </el-empty>
        </div>
      </div>
      
      <!-- 全部或店铺筛选 -->
      <div v-if="activeFilter === 'all' || activeFilter === 'shop'" class="shop-section">
        <div v-if="filteredShops.length > 0" class="shop-list">
          <div class="search-stats">
            <span>找到 {{ filteredShops.length }} 个相关店铺</span>
          </div>
          
          <div class="shop-grid">
            <div class="shop-item" v-for="shop in filteredShops" :key="shop.id" 
                 @click="handleShopClick(shop.id)">
              <div class="shop-card">
                <div class="shop-image">
                  <!-- {{ item.shopLogo }} -->
                  <img :src="getProductImageUrl(shop.shopLogo)" :alt="shop.shopName" @error="handleImageError" />
                  <div class="shop-badge">店铺</div>
                </div>
                <div class="shop-info">
                  <h3 class="shop-name">{{ shop.shopName }}</h3>
                  <div class="shop-rating">
                    <el-rate
                      v-model="shop.shopScore"
                      disabled
                      show-score
                      text-color="#ff9900"
                      :score-template="formatShopScore(shop.shopScore)"
                    />
                  </div>
                  <div class="shop-desc">{{ shop.description || '暂无描述' }}</div>
                  <div class="shop-meta">
                    <span class="shop-sales">销量: {{ shop.salesCount || 0 }}</span>
                    <span class="shop-products">商品: {{ shop.productCount || 0 }}</span>
                    <span class="shop-followers">粉丝: {{ shop.followerCount || 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-else-if="activeFilter === 'shop' && !loading" class="empty-section">
          <el-empty description="暂无相关店铺">
            <el-button type="primary" @click="resetSearch">重新搜索</el-button>
          </el-empty>
        </div>
      </div>
      
      <!-- 全部筛选下的空状态 -->
      <div v-if="activeFilter === 'all' && filteredProducts.length === 0 && filteredShops.length === 0 && !loading" class="empty-state">
        <el-empty :description="emptyDescription">
          <div class="empty-suggestions">
            <p>建议：</p>
            <ul>
              <li>检查输入的关键词是否正确</li>
              <li>尝试其他相关关键词</li>
              <li>浏览分类目录</li>
            </ul>
          </div>
          <el-button type="primary" @click="resetSearch">重新搜索</el-button>
        </el-empty>
      </div>
      
      <!-- 分页组件 -->
      <el-pagination
        v-if="(activeFilter === 'all' && totalCount > pageSize) || 
              (activeFilter === 'product' && filteredProducts.length > pageSize) ||
              (activeFilter === 'shop' && filteredShops.length > pageSize)"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalCount"
        layout="prev, pager, next, jumper"
        @current-change="handlePageChange"
        class="search-pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from "@/utils/request.js"
import { getCategory } from '@/api/system/category'
import { getProductImageUrl, handleImageError } from '@/utils/image'
import { searchShop, listHotShop } from '@/api/system/shop'

const route = useRoute()
const keyword = ref('')
const categoryId = ref('')
const categoryName = ref('')
const products = ref([])
const shops = ref([]) // 新增店铺数据
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const activeFilter = ref('all') // 筛选类型：all, product, shop

// 获取分类名称
const loadCategoryName = async () => {
  if (!categoryId.value) {
    categoryName.value = ''
    return
  }
  
  try {
    const response = await getCategory(categoryId.value)
    console.log('分类详情响应:', response)
    
    if (response.code === 200 && response.data) {
      categoryName.value = response.data.name || `ID: ${categoryId.value}`
    } else {
      categoryName.value = `ID: ${categoryId.value}`
    }
  } catch (error) {
    console.error('获取分类名称失败:', error)
    categoryName.value = `ID: ${categoryId.value}`
  }
}

// 筛选后的商品数据
const filteredProducts = computed(() => {
  if (!products.value || products.value.length === 0) {
    return []
  }
  
  return products.value
})

// 筛选后的店铺数据
const filteredShops = computed(() => {
  if (!shops.value || shops.value.length === 0) {
    return []
  }
  
  return shops.value
})

// 总记录数
const totalCount = computed(() => {
  if (activeFilter.value === 'product') {
    return filteredProducts.value.length
  } else if (activeFilter.value === 'shop') {
    return filteredShops.value.length
  } else {
    return filteredProducts.value.length + filteredShops.value.length
  }
})

// 页面标题
const pageTitle = computed(() => {
  if (keyword.value) {
    return `搜索关键词: "${keyword.value}"`
  } else if (categoryName.value) {
    return `分类: ${categoryName.value}`
  } else if (categoryId.value) {
    return `分类ID: ${categoryId.value}`
  } else {
    return '请输入搜索条件'
  }
})

// 空状态描述
const emptyDescription = computed(() => {
  if (keyword.value) {
    return `没有找到与'${keyword.value}'相关的内容`
  } else if (categoryName.value) {
    return `分类【${categoryName.value}】下暂无内容`
  } else if (categoryId.value) {
    return `分类ID ${categoryId.value} 下暂无内容`
  } else {
    return '请输入搜索条件'
  }
})

// 处理筛选变化
const handleFilterChange = (filterType) => {
  console.log('筛选类型改变为:', filterType)
  activeFilter.value = filterType
  currentPage.value = 1
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  // 这里可以根据需要实现分页加载
}

// 重置搜索
const resetSearch = () => {
  keyword.value = ''
  categoryId.value = ''
  categoryName.value = ''
  activeFilter.value = 'all'
  currentPage.value = 1
  loadSearchResults()
}

// 加载搜索结果
const loadSearchResults = () => {
  loading.value = true
  
  if (categoryId.value) {
    loadCategoryName()
  } else {
    categoryName.value = ''
  }
  
  // 清空之前的数据
  products.value = []
  shops.value = []
  
  if (categoryId.value) {
    // 按分类搜索商品
    api({
      url: `/system/product/category/${categoryId.value}`,
      method: 'get'
    })
      .then((res) => {
        console.log('分类查询响应:', res)
        
        if (res.code === 200) {
          products.value = res.data || []
          total.value = products.value.length
        } else {
          products.value = []
          total.value = 0
        }
      })
      .catch(() => {
        products.value = []
        total.value = 0
      })
      .finally(() => {
        loading.value = false
      })
  }
  else if (keyword.value) {
    // 同时搜索商品和店铺
    Promise.all([
      // 搜索商品
      api({
        url: '/system/product/search',
        method: 'get',
        params: { keyword: keyword.value }
      }),
      // 搜索店铺
      searchShop(keyword.value)
    ])
      .then(([productRes, shopRes]) => {
        console.log('商品搜索响应:', productRes)
        console.log('店铺搜索响应:', shopRes)
        
        // 处理商品搜索结果
        if (productRes.code === 200) {
          products.value = productRes.data || []
          console.log(products.value)
        } else {
          products.value = []
        }
        
        // 处理店铺搜索结果
        if (shopRes.code === 200) {
          shops.value = shopRes.data || []
        } else {
          shops.value = []
        }
        
        total.value = products.value.length + shops.value.length
      })
      .catch((error) => {
        console.error('搜索失败:', error)
        products.value = []
        shops.value = []
        total.value = 0
      })
      .finally(() => {
        loading.value = false
      })
  } else {
    // 没有搜索条件，加载热门内容
    Promise.all([
      // 加载热门商品
      api({
        url: '/system/product/hot',
        method: 'get',
        params: { limit: 20 }
      }),
      // 加载热门店铺
      listHotShop(10)
    ])
      .then(([productRes, shopRes]) => {
        console.log('热门商品响应:', productRes)
        console.log('热门店铺响应:', shopRes)
        
        if (productRes.code === 200) {
          products.value = productRes.data || []
        } else {
          products.value = []
        }
        
        if (shopRes.code === 200) {
          shops.value = shopRes.data || []
        } else {
          shops.value = []
        }
        
        total.value = products.value.length + shops.value.length
      })
      .catch((error) => {
        console.error('加载热门内容失败:', error)
        products.value = []
        shops.value = []
        total.value = 0
      })
      .finally(() => {
        loading.value = false
      })
  }
}

// 处理商品点击
const handleProductClick = (productId) => {
  // 跳转到商品详情页
  window.open(`/product/detail?id=${productId}`, '_blank')
}

// 处理店铺点击
const handleShopClick = (shopId) => {
  // 跳转到店铺详情页
  window.open(`/shop?id=${shopId}`, '_blank')
}

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 格式化销量
const formatSalesCount = (salesCount) => {
  if (salesCount >= 10000) {
    return (salesCount / 10000).toFixed(1) + '万'
  } else if (salesCount >= 1000) {
    return (salesCount / 1000).toFixed(1) + '千'
  }
  return salesCount
}

// 格式化店铺评分
const formatShopScore = (score) => {
  if (!score) return '0.0'
  return parseFloat(score).toFixed(1)
}

onMounted(() => {
  keyword.value = route.query.keyword || ''
  categoryId.value = route.query.categoryId || ''
  
  console.log('页面加载，参数:', {
    keyword: keyword.value,
    categoryId: categoryId.value
  })
  
  loadSearchResults()
})

watch(() => route.query, (newQuery) => {
  keyword.value = newQuery.keyword || ''
  categoryId.value = newQuery.categoryId || ''
  currentPage.value = 1
  categoryName.value = ''
  activeFilter.value = 'all'
  
  console.log('路由参数变化:', newQuery)
  
  loadSearchResults()
})
</script>

<style scoped>
.search-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb-nav {
  margin-bottom: 20px;
  padding: 0 10px;
}

.breadcrumb-nav .el-breadcrumb {
  font-size: 14px;
}

.search-header {
  margin-bottom: 20px;
  text-align: center;
}

.search-header h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.search-header p {
  color: #666;
  font-size: 16px;
}

/* 筛选选项卡样式 */
.filter-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-stats {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.stat-item {
  padding: 4px 8px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.search-results {
  position: relative;
  min-height: 400px;
}

.search-stats {
  margin-bottom: 20px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
}

.search-stats span {
  font-size: 14px;
  color: #666;
}

/* 商品网格布局 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.product-item {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.product-item:hover {
  transform: translateY(-5px);
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.product-item:hover .product-card {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
  border-color: #e0e0e0;
}

.product-image {
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-item:hover .product-image img {
  transform: scale(1.05);
}

.product-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #409eff;
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: bold;
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

.product-price {
  color: #e4393c;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
}

.product-desc {
  color: #666;
  font-size: 12px;
  line-height: 1.4;
  margin-bottom: 8px;
  height: 34px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-category {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #666;
  margin-top: auto;
}

.product-sales, .product-stock {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
}

/* 店铺网格布局 */
.shop-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.shop-item {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.shop-item:hover {
  transform: translateY(-5px);
}

.shop-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 3px 15px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

.shop-item:hover .shop-card {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
  border-color: #e0e0e0;
}

.shop-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.shop-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.shop-item:hover .shop-image img {
  transform: scale(1.05);
}

.shop-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #67c23a;
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: bold;
}

.shop-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.shop-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shop-rating {
  margin-bottom: 8px;
}

.shop-desc {
  color: #666;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 12px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.shop-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-top: auto;
}

.shop-sales, .shop-products, .shop-followers {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
}

.empty-state, .empty-section {
  text-align: center;
  padding: 50px 20px;
}

.empty-suggestions {
  margin-top: 20px;
  text-align: left;
  max-width: 500px;
  margin: 20px auto 0;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
}

.empty-suggestions p {
  font-weight: bold;
  margin-bottom: 10px;
}

.empty-suggestions ul {
  list-style-type: none;
  padding-left: 0;
}

.empty-suggestions li {
  padding: 5px 0;
  color: #666;
}

.search-pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .shop-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .filter-tabs {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .filter-stats {
    align-self: stretch;
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .search-container {
    padding: 15px;
  }
  
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .shop-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .product-image {
    height: 160px;
  }
  
  .shop-image {
    height: 180px;
  }
}

@media (max-width: 480px) {
  .search-container {
    padding: 12px;
  }
  
  .product-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .filter-stats {
    flex-direction: column;
    gap: 8px;
  }
  
  .stat-item {
    align-self: flex-start;
  }
  
  .product-meta, .shop-meta {
    flex-direction: column;
    gap: 5px;
  }
}
</style>