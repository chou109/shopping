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
      
      <!-- 页面内搜索框 -->
      <div class="inline-search-box">
        <el-input
          v-model="inlineSearchKeyword"
          label="搜索关键词"
          placeholder="请输入关键词搜索"
          clearable
          @keyup.enter="handleInlineSearch"
        >
          <template #append>
            <el-button type="primary" @click="handleInlineSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
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
        <span class="stat-item" v-if="activeFilter === 'product'">商品: {{ serverPagedResults ? serverProductTotal : filteredProducts.length }} 个</span>
        <span class="stat-item" v-if="activeFilter === 'shop'">店铺: {{ serverPagedResults ? serverShopTotal : filteredShops.length }} 个</span>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-loading="loading" class="search-results" style="max-width: 1300px;margin:0 auto;">
      <el-alert v-if="searchError" :title="searchError" type="error" :closable="false" show-icon />
      <!-- 全部或商品筛选 -->
      <div v-if="activeFilter === 'all' || activeFilter === 'product'" class="product-section">
        <div v-if="filteredProducts.length > 0" class="product-list">
          <div class="search-stats">
            <span>找到 {{ serverPagedResults ? serverProductTotal : filteredProducts.length }} 个相关商品</span>
          </div>

          <div class="product-grid">
            <div class="product-item" v-for="item in visibleProducts" :key="item.id" role="button" tabindex="0"
              :aria-label="`查看商品：${item.name}`" @click="handleProductClick(item.id)"
              @keydown.enter.prevent="handleProductClick(item.id)" @keydown.space.prevent="handleProductClick(item.id)">
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

        <div v-else-if="activeFilter === 'product' && !loading && !searchError" class="empty-section">
          <el-empty description="暂无相关商品">
            <el-button type="primary" @click="resetSearch">重新搜索</el-button>
          </el-empty>
        </div>
      </div>

      <!-- 全部或店铺筛选 -->
      <div v-if="activeFilter === 'all' || activeFilter === 'shop'" class="shop-section">
        <div v-if="filteredShops.length > 0" class="shop-list">
          <div class="search-stats">
            <span>找到 {{ serverPagedResults ? serverShopTotal : filteredShops.length }} 个相关店铺</span>
          </div>

          <div class="shop-grid">
            <div class="shop-item" v-for="shop in visibleShops" :key="shop.id" role="button" tabindex="0"
              :aria-label="`查看店铺：${shop.shopName}`" @click="handleShopClick(shop.id)"
              @keydown.enter.prevent="handleShopClick(shop.id)" @keydown.space.prevent="handleShopClick(shop.id)">
              <div class="shop-card">
                <div class="shop-image">
                  <!-- {{ item.shopLogo }} -->
                  <img :src="getProductImageUrl(shop.shopLogo)" :alt="shop.shopName" @error="handleImageError" />
                  <div class="shop-badge">店铺</div>
                </div>
                <div class="shop-info">
                  <h3 class="shop-name">{{ shop.shopName }}</h3>
                  <div class="shop-rating">
                    <el-rate v-model="shop.shopScore" disabled show-score text-color="#ff9900"
                      :score-template="formatShopScore(shop.shopScore)" />
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

        <div v-else-if="activeFilter === 'shop' && !loading && !searchError" class="empty-section">
          <el-empty description="暂无相关店铺">
            <el-button type="primary" @click="resetSearch">重新搜索</el-button>
          </el-empty>
        </div>
      </div>

      <!-- 全部筛选下的空状态 -->
      <div v-if="activeFilter === 'all' && filteredProducts.length === 0 && filteredShops.length === 0 && !loading && !searchError"
        class="empty-state">
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
      <el-pagination v-if="totalCount > pageSize" v-model:current-page="currentPage"
        v-model:page-size="pageSize" :total="totalCount" prev-text="上一页" next-text="下一页"
        layout="prev, pager, next, jumper"
        @current-change="handlePageChange" class="search-pagination" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from "@/utils/request.js"
import { getFrontCategoryTree } from '@/api/system/category'
import { getProductImageUrl, handleImageError } from '@/utils/image'
import { isBlindBoxProduct } from '@/utils/product'
import { listHotShop } from '@/api/system/shop'

const route = useRoute()
const router = useRouter()
const keyword = ref('')
const categoryId = ref('')
const categoryName = ref('')
const products = ref([])
const shops = ref([]) // 新增店铺数据
const loading = ref(false)
const searchError = ref('')
const total = ref(0)
const serverPagedResults = ref(false)
const serverProductTotal = ref(0)
const serverShopTotal = ref(0)
const serverTotalCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const activeFilter = ref('all') // 筛选类型：all, product, shop
const inlineSearchKeyword = ref('') // 页面内搜索关键词
let searchRequestVersion = 0

// 获取分类名称
const loadCategoryName = async () => {
  if (!categoryId.value) {
    categoryName.value = ''
    return
  }

  try {
    const response = await getFrontCategoryTree()
    const findCategory = (categories = []) => {
      for (const category of categories) {
        if (String(category.id) === String(categoryId.value)) return category
        const child = findCategory(category.children || [])
        if (child) return child
      }
      return null
    }

    if (response.code === 200 && Array.isArray(response.data)) {
      const category = findCategory(response.data)
      categoryName.value = category?.name || `ID: ${categoryId.value}`
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

const visibleProducts = computed(() => {
  if (activeFilter.value === 'shop') return []
  if (serverPagedResults.value) return filteredProducts.value
  const start = (currentPage.value - 1) * pageSize.value
  if (activeFilter.value === 'product') {
    return filteredProducts.value.slice(start, start + pageSize.value)
  }
  const page = [
    ...filteredProducts.value.map(data => ({ type: 'product', data })),
    ...filteredShops.value.map(data => ({ type: 'shop', data }))
  ].slice(start, start + pageSize.value)
  return page.filter(item => item.type === 'product').map(item => item.data)
})

const visibleShops = computed(() => {
  if (activeFilter.value === 'product') return []
  if (serverPagedResults.value) return filteredShops.value
  const start = (currentPage.value - 1) * pageSize.value
  if (activeFilter.value === 'shop') {
    return filteredShops.value.slice(start, start + pageSize.value)
  }
  const page = [
    ...filteredProducts.value.map(data => ({ type: 'product', data })),
    ...filteredShops.value.map(data => ({ type: 'shop', data }))
  ].slice(start, start + pageSize.value)
  return page.filter(item => item.type === 'shop').map(item => item.data)
})

// 总记录数
const totalCount = computed(() => {
  if (serverPagedResults.value) return serverTotalCount.value
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
  activeFilter.value = filterType
  currentPage.value = 1
  if (serverPagedResults.value) loadSearchResults()
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  if (serverPagedResults.value) loadSearchResults()
}

// 重置搜索
const resetSearch = () => {
  keyword.value = ''
  categoryId.value = ''
  categoryName.value = ''
  inlineSearchKeyword.value = ''
  activeFilter.value = 'all'
  currentPage.value = 1
  serverPagedResults.value = false
  if (Object.keys(route.query).length > 0) {
    router.replace({ path: route.path, query: {} })
  } else {
    loadSearchResults()
  }
}

// 处理页面内搜索
const handleInlineSearch = () => {
  if (!inlineSearchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  // 更新路由参数，触发搜索
  router.push({
    path: '/search',
    query: {
      keyword: inlineSearchKeyword.value.trim()
    }
  })
}

// 加载搜索结果
const loadSearchResults = () => {
  const requestVersion = ++searchRequestVersion
  loading.value = true
  searchError.value = ''
  if (categoryId.value) {
    loadCategoryName()
  } else {
    categoryName.value = ''
  }
  products.value = []
  shops.value = []
  serverProductTotal.value = 0
  serverShopTotal.value = 0
  serverTotalCount.value = 0

  if (categoryId.value || keyword.value) {
    serverPagedResults.value = true
    api({
      url: '/system/product/public/search-page',
      method: 'get',
      params: {
        keyword: keyword.value || undefined,
        categoryId: categoryId.value || undefined,
        type: activeFilter.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      },
      headers: { isToken: false }
    })
      .then((res) => {
        if (requestVersion !== searchRequestVersion) return
        if (res.code !== 200) {
          products.value = []
          shops.value = []
          serverProductTotal.value = 0
          serverShopTotal.value = 0
          serverTotalCount.value = 0
          searchError.value = res.msg || '搜索结果加载失败，请重试'
          return
        }
        const data = res.data || {}
        products.value = data.products || []
        shops.value = data.shops || []
        serverProductTotal.value = Number(data.productTotal || 0)
        serverShopTotal.value = Number(data.shopTotal || 0)
        serverTotalCount.value = Number(data.total || 0)
        total.value = serverTotalCount.value
      })
      .catch((error) => {
        if (requestVersion !== searchRequestVersion) return
        console.error('搜索失败:', error)
        products.value = []
        shops.value = []
        serverProductTotal.value = 0
        serverShopTotal.value = 0
        serverTotalCount.value = 0
        total.value = 0
        searchError.value = error.message || '搜索失败，请检查网络后重试'
      })
      .finally(() => {
        if (requestVersion === searchRequestVersion) loading.value = false
      })
  } else {
    serverPagedResults.value = false
    serverProductTotal.value = 0
    serverShopTotal.value = 0
    serverTotalCount.value = 0
    // 没有搜索条件，加载热门内容
    Promise.all([
      // 加载热门商品
      api({
        url: '/system/product/hot',
        method: 'get',
        params: { limit: 20 },
        headers: { isToken: false }
      }),
      // 加载热门店铺
      listHotShop(10)
    ])
      .then(([productRes, shopRes]) => {
        if (requestVersion !== searchRequestVersion) return

        if (productRes.code === 200) {
          products.value = (productRes.data || []).filter(product => !isBlindBoxProduct(product))
        } else {
          products.value = []
          searchError.value = productRes.msg || '热门商品加载失败'
        }

        if (shopRes.code === 200) {
          shops.value = shopRes.data || []
        } else {
          shops.value = []
          searchError.value = searchError.value || shopRes.msg || '热门店铺加载失败'
        }

        total.value = products.value.length + shops.value.length
      })
      .catch((error) => {
        if (requestVersion !== searchRequestVersion) return
        console.error('加载热门内容失败:', error)
        products.value = []
        shops.value = []
        total.value = 0
        searchError.value = error.message || '热门内容加载失败，请检查网络后重试'
      })
      .finally(() => {
        if (requestVersion === searchRequestVersion) loading.value = false
      })
  }
}

// 处理商品点击
const handleProductClick = (productId) => {
  router.push({ path: '/product/detail', query: { id: productId } })
}

// 处理店铺点击
const handleShopClick = (shopId) => {
  router.push({ path: '/shop', query: { id: shopId } })
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

  loadSearchResults()
})

watch(() => route.query, (newQuery) => {
  keyword.value = newQuery.keyword || ''
  categoryId.value = newQuery.categoryId || ''
  currentPage.value = 1
  categoryName.value = ''
  activeFilter.value = 'all'

  loadSearchResults()
})
</script>

<style scoped>
.search-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.breadcrumb-nav {
  margin-bottom: 20px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.breadcrumb-nav .el-breadcrumb {
  font-size: 14px;
}

.breadcrumb-nav :deep(.el-breadcrumb__separator) {
  color: #c0c4cc;
  margin: 0 12px;
}

.breadcrumb-nav :deep(.el-breadcrumb__inner) {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 6px;
  color: #606266;
  font-weight: 500;
  transition: all 0.25s ease;
}

.breadcrumb-nav :deep(.el-breadcrumb__inner:hover) {
  background: rgba(255, 68, 0, 0.08);
  color: #FF4400;
}

.breadcrumb-nav :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  background: rgba(255, 68, 0, 0.1);
  color: #303133;
  font-weight: 600;
  padding-left: 16px;
  position: relative;
}

.breadcrumb-nav :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner::before) {
  content: '';
  position: absolute;
  left: 6px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  background: #FF4400;
  border-radius: 2px;
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

/* 页面内搜索框样式 */
.inline-search-box {
  margin-top: 20px;
  max-width: 800px;
  width: 100%;
  margin-left: auto;
  margin-right: auto;
}

.inline-search-box .el-input {
  width: 100%;
}

.inline-search-box .el-input__wrapper {
  border-radius: 20px;
  border-color: #FF4400;
  box-shadow: 0 2px 8px rgba(255, 68, 0, 0.15);
}

.inline-search-box .el-input__inner {
  height: 40px;
  border-radius: 20px 0 0 20px;
  padding-left: 20px;
}

.inline-search-box .el-button {
  height: 40px;
  border-radius: 0 20px 20px 0;
  background-color: #FF4400;
  border-color: #FF4400;
  width: 100px;
  font-weight: bold;
  color: #fff; /* 搜索按钮字体颜色设置为白色 */
}

.inline-search-box .el-button:hover {
  background-color: #FF5500;
  border-color: #FF5500;
  color: #fff; /* 搜索按钮悬停时字体颜色仍为白色 */
}

/* 筛选选项卡样式 */
.filter-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
  border-bottom: 2px solid #FF4400;
}

/* 分类筛选选项卡样式 - 淘宝风格 */
.filter-tabs .el-radio-button__inner {
  background-color: #fff;
  color: #333;
  border-color: #ddd;
  border-radius: 0;
  transition: all 0.3s ease;
}

.filter-tabs .el-radio-button__inner:hover {
  color: #FF4400;
  border-color: #FF4400;
}

.filter-tabs .el-radio-button__orig-radio:checked + .el-radio-button__inner {
  background-color: #FF4400;
  color: #fff;
  border-color: #FF4400;
}

.filter-tabs .el-radio-button:first-child .el-radio-button__inner {
  border-radius: 4px 0 0 4px;
}

.filter-tabs .el-radio-button:last-child .el-radio-button__inner {
  border-radius: 0 4px 4px 0;
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
  flex: 1;
  width: 100%;
  min-height: calc(100vh - 200px);
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
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.product-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.product-item:focus-visible {
  outline: 3px solid #ff5500;
  outline-offset: 3px;
  border-radius: 8px;
}

.product-item:hover {
  transform: translateY(-2px);
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border: 1px solid #fff;
}

.product-item:hover .product-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: #e5e5e5;
}

.product-image {
  width: 100%;
  height: 240px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.product-item:hover .product-image img {
  transform: scale(1.02);
}

.product-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #FF4400;
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: bold;
}

.product-info {
  padding: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.product-name {
  font-size: 13px;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.5;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  color: #FF4400;
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
  line-height: 1;
}

.product-desc {
  display: none;
}

.product-category {
  display: none;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.product-sales {
  color: #999;
}

.product-stock {
  display: none;
}

/* 店铺网格布局 */
.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.shop-item {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.shop-item:focus-visible {
  outline: 3px solid #ff9900;
  outline-offset: 3px;
  border-radius: 8px;
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
  background: #FF9900;
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

.shop-sales,
.shop-products,
.shop-followers {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
}

.empty-state,
.empty-section {
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

  .product-meta,
  .shop-meta {
    flex-direction: column;
    gap: 5px;
  }
}
</style>
