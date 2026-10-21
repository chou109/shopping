<template>
  <div class="shop-detail-page">
    <!-- 店铺头部 -->
    <div class="shop-header">
      <div class="shop-banner">
        <div class="shop-info-wrapper">
          <div class="shop-logo">
            <img :src="getProductImageUrl(shopInfo.shopLogo)" alt="店铺logo" />
          </div>
          <div class="shop-meta">
            <h1 class="shop-name">{{ shopInfo.shopName || '店铺名称' }}</h1>
            <div class="shop-stats">
              <div class="stat-item">
                <span class="stat-value">{{ shopInfo.productCount || 0 }}</span>
                <span class="stat-label">全部商品</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ shopInfo.followerCount || 0 }}</span>
                <span class="stat-label">粉丝</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ formatScore(shopInfo.shopScore) }}</span>
                <span class="stat-label">店铺评分</span>
              </div>
            </div>
            <div class="shop-desc">{{ shopInfo.description || '暂无店铺简介' }}</div>
          </div>
          <div class="shop-actions">
            <el-button type="primary" @click="followShop" :icon="Star">
              {{ isFollowed ? '已关注' : '关注店铺' }}
            </el-button>
            <el-button style="margin-left: -0.1px;" @click="contactService" :icon="Service">联系客服</el-button>
            <div class="view-options">
              <el-button-group>
                <el-button style="width: 20px;" :type="viewMode === 'grid' ? 'primary' : ''" size="small"
                  @click="viewMode = 'grid'">
                  <el-icon>
                    <Grid />
                  </el-icon>
                </el-button>
                <el-button style="width: 20px;" :type="viewMode === 'list' ? 'primary' : ''" size="small"
                  @click="viewMode = 'list'">
                  <el-icon>
                    <List />
                  </el-icon>
                </el-button>
              </el-button-group>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- 店铺导航 -->
    <div class="shop-nav">
      <div class="nav-content">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="全部商品" name="all"></el-tab-pane>
          <el-tab-pane name="price">
            <template #label>
              <span @click.stop="togglePriceSort">价格 {{ priceOrder === 'asc' ? '↑' : '↓' }}</span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="销量排行" name="sales"></el-tab-pane>
        </el-tabs>
        <div class="search-box">
          <el-input v-model="searchKeyword" placeholder="搜索店内商品" clearable @keyup.enter="searchProducts">
            <template #append>
              <el-button :icon="Search" @click="searchProducts" />
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div class="products-section" v-loading="loading">
      <el-alert v-if="loadError" :title="loadError" type="error" :closable="false" show-icon>
        <template #default>
          <el-button link type="primary" @click="loadShopData">重试</el-button>
        </template>
      </el-alert>

      <!-- 商品网格 -->
      <div v-if="viewMode === 'grid'" class="products-grid">
        <div v-for="product in filteredProducts" :key="product.id" class="product-card"
          @click="goToProduct(product.id)">
          <div class="product-image">
            <img :src="getImageUrl(product.mainImage)" :alt="product.name" />
            <div class="product-tags">
              <span v-if="product.activityId" class="tag hot">活动</span>
            </div>
          </div>
          <div class="product-info">
            <h3 class="product-title">{{ product.name }}</h3>
            <p class="product-desc">{{ product.subTitle || '暂无商品简介' }}</p>
            <div class="product-bottom">
              <div class="price-section">
                <span class="current-price">¥{{ formatPrice(product.price) }}</span>
              </div>
              <div class="sales-info">
                <span v-if="product.salesCount !== undefined && product.salesCount !== null">月销 {{ product.salesCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品列表视图 -->
      <div v-else class="products-list">
        <div v-for="product in filteredProducts" :key="product.id" class="product-item"
          @click="goToProduct(product.id)">
          <div class="item-image">
            <img :src="getImageUrl(product.mainImage)" :alt="product.name" />
          </div>
          <div class="item-content">
            <h3 class="item-title">{{ product.name }}</h3>
            <p class="item-desc">{{ product.subTitle || '暂无商品简介' }}</p>
            <div class="item-meta">
              <span class="item-price">¥{{ formatPrice(product.price) }}</span>
              <span v-if="product.salesCount !== undefined && product.salesCount !== null" class="item-sales">月销 {{ product.salesCount }}</span>
              <span class="item-stock">库存 {{ product.stock }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && !loadError && filteredProducts.length === 0" description="暂无商品" />
    </div>
  </div>
</template>

<script setup>
import { DEFAULT_PRODUCT_IMAGE, getProductImageUrl, handleImageError } from '@/utils/image'
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, Service, Search, Grid, List } from '@element-plus/icons-vue'
import { getShopInfo, getShopProducts } from '@/api/system/shop'
import { recordFootprint } from '@/api/system/footprint'
import { checkFavorite, toggleFavorite } from '@/api/system/favorite'
import { isBlindBoxProduct } from '@/utils/product'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const shopInfo = ref({})
const products = ref([])
const loading = ref(false)
const loadError = ref('')
const activeTab = ref('all')
const searchKeyword = ref('')
const sortBy = ref('default')
const priceOrder = ref('asc')
const viewMode = ref('grid')
const isFollowed = ref(false)

// 获取图片URL
const getImageUrl = (path) => {
  if (!path) return DEFAULT_PRODUCT_IMAGE
  if (path.startsWith('http')) return path
  return import.meta.env.VITE_APP_BASE_API + path
}

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return Number(price).toFixed(2)
}

// 格式化评分
const formatScore = (score) => {
  if (score === null || score === undefined || score === '') return '0.0'
  return Number(score).toFixed(1)
}

// 筛选和排序商品
const filteredProducts = computed(() => {
  let result = [...products.value]

  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(p =>
      p.name?.toLowerCase().includes(keyword) ||
      p.description?.toLowerCase().includes(keyword)
    )
  }

  // 排序
  switch (sortBy.value) {
    case 'sales':
      result.sort((a, b) => (b.salesCount || 0) - (a.salesCount || 0))
      break
    case 'price':
      result.sort((a, b) => priceOrder.value === 'asc' ? a.price - b.price : b.price - a.price)
      break
    case 'new':
      result.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      break
  }

  return result
})

// 切换价格排序
const togglePriceSort = () => {
  if (sortBy.value === 'price') {
    priceOrder.value = priceOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = 'price'
    priceOrder.value = 'asc'
  }
}

// 搜索商品
const searchProducts = () => {
  // 搜索通过computed自动过滤
}

// 跳转商品详情
const goToProduct = (id) => {
  router.push({ path: '/product/detail', query: { id } })
}

// 关注店铺
const followShop = async () => {
  const shopId = route.query.id
  if (!shopId) return
  try {
    const res = await toggleFavorite(2, shopId) // 2表示店铺
    if (res.code === 200) {
      isFollowed.value = res.data // 后端返回 true/false
      ElMessage.success(isFollowed.value ? '关注成功' : '已取消关注')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 检查是否已关注
const checkFollowStatus = async () => {
  const shopId = route.query.id
  if (!shopId) return
  if (!getToken()) {
    isFollowed.value = false
    return
  }
  try {
    const res = await checkFavorite(2, shopId, { silentAuthFailure: true })
    if (res.code === 200 && res.data) {
      isFollowed.value = res.data.isFavorite || false
    }
  } catch (e) {
    console.error('检查关注状态失败', e)
  }
}

// 联系客服
const contactService = () => {
  router.push({
    path: '/customer-service',
    query: { receiverId: shopInfo.value.userId }
  })
}

// Tab点击
const handleTabClick = (tab) => {
  switch (tab.props.name) {
    case 'new':
      sortBy.value = 'new'
      break
    case 'sales':
      sortBy.value = 'sales'
      break
    default:
      sortBy.value = 'default'
  }
}

// 加载店铺数据
const loadShopData = async () => {
  const shopId = route.query.id
  loadError.value = ''
  
  if (!shopId) {
    loadError.value = '店铺 ID 不存在，无法加载店铺信息'
    return
  }

  loading.value = true
  try {
    // 获取店铺信息
    const shopRes = await getShopInfo(shopId)
    if (shopRes.code === 200) {
      shopInfo.value = shopRes.data || {}
      if (getToken()) {
        recordFootprint({ targetType: 2, targetId: shopId }).catch(error => {
          console.warn('记录店铺浏览足迹失败:', error)
        })
      }
    } else {
      loadError.value = shopRes.msg || '获取店铺信息失败，请重试'
      return
    }

    // 获取店铺商品
    const productRes = await getShopProducts(shopId)
    if (productRes.code === 200) {
      products.value = (productRes.data || []).filter(product => !isBlindBoxProduct(product))
    } else {
      loadError.value = productRes.msg || '获取店铺商品失败，请重试'
    }
  } catch (error) {
    console.error('加载店铺数据失败:', error)
    loadError.value = error.message || '加载店铺数据失败，请检查网络后重试'
  } finally {
    loading.value = false
  }
}

watch(() => route.query.id, () => {
  products.value = []
  shopInfo.value = {}
  loadShopData()
  checkFollowStatus()
}, { immediate: true })
</script>

<style scoped lang="scss">
.shop-detail-page {
  background: #f5f5f5;
  min-height: 100vh;
}

.shop-header {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);

  .shop-banner {
    max-width: 1200px;
    margin: 0 auto;
    padding: 40px 20px;
  }

  .shop-info-wrapper {
    display: flex;
    align-items: flex-start;
    gap: 30px;
  }

  .shop-logo {
    width: 120px;
    height: 120px;
    border-radius: 12px;
    overflow: hidden;
    background: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .shop-meta {
    flex: 1;
    color: #fff;

    .shop-name {
      font-size: 28px;
      font-weight: 600;
      margin: 0 0 15px 0;
    }

    .shop-stats {
      display: flex;
      gap: 40px;
      margin-bottom: 15px;

      .stat-item {
        text-align: center;

        .stat-value {
          display: block;
          font-size: 24px;
          font-weight: 600;
        }

        .stat-label {
          font-size: 12px;
          opacity: 0.9;
        }
      }
    }

    .shop-desc {
      font-size: 14px;
      opacity: 0.9;
      max-width: 500px;
    }
  }

  .shop-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;


    .el-button {
      width: 120px;
      margin-bottom: 10px;
    }
  }
}

@media (max-width: 768px) {
  .shop-header {
    .shop-banner {
      padding: 24px 16px;
    }

    .shop-info-wrapper {
      flex-wrap: wrap;
      gap: 16px;
    }

    .shop-logo {
      width: 72px;
      height: 72px;
    }

    .shop-meta {
      min-width: 0;
      flex: 1 1 calc(100% - 88px);

      .shop-name {
        margin-bottom: 10px;
        font-size: 22px;
        overflow-wrap: anywhere;
      }

      .shop-stats {
        flex-wrap: wrap;
        gap: 16px;
        margin-bottom: 10px;

        .stat-value {
          font-size: 20px;
        }
      }

      .shop-desc {
        max-width: none;
        overflow-wrap: anywhere;
      }
    }

    .shop-actions {
      width: 100%;
      flex-direction: row;
      flex-wrap: wrap;
      align-items: center;
      gap: 8px;

      .el-button {
        width: auto;
        flex: 1 1 120px;
        margin: 0;
      }

      .view-options {
        margin-left: auto;
      }
    }
  }
}

.shop-nav {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;

  .nav-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-box {
    width: 250px;
  }

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
}

.products-section {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.products-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;

  .sort-options {
    display: flex;
    gap: 30px;

    span {
      cursor: pointer;
      color: #666;
      font-size: 14px;
      padding: 5px 0;
      border-bottom: 2px solid transparent;
      transition: all 0.3s;

      &:hover,
      &.active {
        color: #ff6b35;
        border-bottom-color: #ff6b35;
      }
    }
  }
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(4, 1fr);
  }

  @media (max-width: 992px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  }

  .product-image {
    position: relative;
    aspect-ratio: 1;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;
    }

    &:hover img {
      transform: scale(1.05);
    }

    .product-tags {
      position: absolute;
      top: 8px;
      left: 8px;
      display: flex;
      gap: 5px;

      .tag {
        padding: 2px 8px;
        border-radius: 3px;
        font-size: 12px;
        color: #fff;

        &.hot {
          background: #ff4757;
        }
      }
    }
  }

  .product-info {
    padding: 12px;

    .product-title {
      font-size: 14px;
      color: #333;
      margin: 0 0 8px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      line-height: 1.4;
      height: 2.8em;
    }

    .product-desc {
      font-size: 12px;
      color: #999;
      margin: 0 0 10px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .product-bottom {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;

      .current-price {
        font-size: 18px;
        font-weight: 600;
        color: #ff4757;
      }

      .original-price {
        font-size: 12px;
        color: #999;
        text-decoration: line-through;
        margin-left: 5px;
      }

      .sales-info {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

.products-list {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.product-item {
  display: flex;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;

  &:hover {
    background: #fafafa;
  }

  &:last-child {
    border-bottom: none;
  }

  .item-image {
    width: 150px;
    height: 150px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .item-content {
    flex: 1;
    padding-left: 20px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .item-title {
      font-size: 16px;
      color: #333;
      margin: 0 0 10px 0;
    }

    .item-desc {
      font-size: 14px;
      color: #666;
      margin: 0;
      flex: 1;
    }

    .item-meta {
      display: flex;
      align-items: center;
      gap: 20px;

      .item-price {
        font-size: 20px;
        font-weight: 600;
        color: #ff4757;
      }

      .item-sales,
      .item-stock {
        font-size: 13px;
        color: #999;
      }
    }
  }
}
</style>
