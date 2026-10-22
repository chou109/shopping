<template>
  <div class="favorite-products">
    <!-- 页面头部 -->
    <div class="page-header">
      <span class="count">共 {{ favoriteList.length }} 件商品</span>
    </div>

    <!-- 商品列表 -->
    <div class="products-container" v-loading="loading">
      <el-alert v-if="loadError" :title="loadError" type="error" :closable="false" show-icon>
        <template #default><el-button link type="primary" :disabled="loading" @click="loadFavorites">重试</el-button></template>
      </el-alert>
      <div v-if="!loading && !loadError && favoriteList.length === 0" class="empty-state">
        <el-empty description="暂无收藏商品">
          <el-button type="primary" @click="goShopping">去逛逛</el-button>
        </el-empty>
      </div>

      <div v-if="favoriteList.length > 0" class="product-grid">
        <article v-for="item in favoriteList" :key="item.favoriteId" class="product-card">
          <button type="button" class="product-card-open" :aria-label="`查看商品：${item.productName}`"
            @click="goProductDetail(item.targetId)">
            <div class="product-image">
              <img :src="getProductImageUrl(item.productMainImage)" :alt="item.productName" @error="handleImageError" />
            </div>
            <div class="product-info">
              <div class="product-name">{{ item.productName }}</div>
              <div class="product-subtitle" v-if="item.productSubtitle">{{ item.productSubtitle }}</div>
              <div class="product-price">
                <span class="price">{{ formatPrice(item.productPrice) }}</span>
              </div>
              <div class="favorite-time">
                收藏于 {{ formatTime(item.createTime) }}
              </div>
            </div>
          </button>
          <button type="button" class="favorite-btn" :aria-label="`取消收藏：${item.productName}`"
            :disabled="removingFavoriteIds.has(item.favoriteId)"
            :aria-busy="removingFavoriteIds.has(item.favoriteId)"
            @click="handleRemoveFavorite(item)">
              <el-icon>
                <Star />
              </el-icon>
          </button>
        </article>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import { getMyProductFavorites, removeFavorite } from '@/api/system/favorite'
import { getProductImageUrl, handleImageError } from '@/utils/image'
import { formatProductPrice } from '@/utils/productDetail.mjs'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const loadError = ref('')
const favoriteList = ref([])
const removingFavoriteIds = ref(new Set())
let favoritesRequestId = 0

// 加载收藏列表
const loadFavorites = async () => {
  const requestId = ++favoritesRequestId
  loading.value = true
  loadError.value = ''
  try {
    const res = await getMyProductFavorites()
    if (requestId !== favoritesRequestId) return
    if (res.code !== 200 || !Array.isArray(res.data)) throw new Error(res.msg || '收藏数据格式异常')
    favoriteList.value = res.data
  } catch (e) {
    if (requestId === favoritesRequestId) {
      console.error('加载收藏列表失败', e)
      loadError.value = '收藏列表加载失败，请重试'
    }
  } finally {
    if (requestId === favoritesRequestId) loading.value = false
  }
}

// 取消收藏
const handleRemoveFavorite = async (item) => {
  if (removingFavoriteIds.value.has(item.favoriteId)) return
  removingFavoriteIds.value.add(item.favoriteId)
  try {
    await ElMessageBox.confirm('确定要取消收藏该商品吗？', '取消收藏', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await removeFavorite(1, item.targetId) // 1表示商品
    if (res.code === 200) {
      ElMessage.success('已取消收藏')
      // 从列表中移除
      favoriteList.value = favoriteList.value.filter(f => f.favoriteId !== item.favoriteId)
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') console.error('取消收藏失败', e)
  } finally {
    removingFavoriteIds.value.delete(item.favoriteId)
  }
}

// 格式化价格
const formatPrice = (value) => {
  if (value == null || value === '' || !Number.isFinite(Number(value))) return '价格未知'
  return `¥${formatProductPrice(value)}`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return Number.isFinite(date.getTime()) ? date.toLocaleDateString('zh-CN') : '时间未知'
}

// 跳转商品详情
const goProductDetail = (productId) => {
  router.push({
    path: '/product/detail',
    query: { id: productId }
  })
}

// 去购物
const goShopping = () => {
  router.push('/index')
}

onMounted(() => {
  loadFavorites()
})

onBeforeUnmount(() => {
  favoritesRequestId++
})
</script>

<style scoped>
.favorite-products {
  padding: 0;
  min-height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  padding: 16px 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  position: sticky;
  top: 70px;
  z-index: 10;
}

.page-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.page-header .count {
  font-size: 14px;
  color: #999;
}

.products-container {
  min-height: 400px;
}

.empty-state {
  background: transparent;
  padding: 60px 20px;
  border-radius: 8px;
  text-align: center;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.product-card {
  position: relative;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.product-card-open {
  display: block;
  width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.product-card-open:focus-visible,
.favorite-btn:focus-visible {
  outline: 3px solid #ee0a24;
  outline-offset: 3px;
  z-index: 1;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.product-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
}

.product-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.favorite-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 0;
  padding: 0;
}

.favorite-btn .el-icon {
  font-size: 18px;
  color: #ff5000;
}

.favorite-btn:hover {
  background: #fff;
  transform: scale(1.1);
}

.product-info {
  padding: 12px;
}

.product-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 40px;
}

.product-subtitle {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price .price {
  color: #ff5000;
  font-size: 18px;
  font-weight: 700;
}

.favorite-time {
  font-size: 12px;
  color: #bbb;
  margin-top: 8px;
}
</style>
