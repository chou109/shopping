<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ChatDotRound, 
  CircleCheck, 
  ShoppingCart, 
  ShoppingCartFull,
  User,
  ShoppingBag,
  Shop,
  StarFilled,
  Star
} from '@element-plus/icons-vue'
import { getProduct } from '@/api/system/product'
import { addCart } from '@/api/system/cart'
import { getShopInfo } from '@/api/system/shop'
import { 
  listProductComments, 
  getProductCommentStats
} from '@/api/system/productComment'
import { getProductImageUrl, handleImageError } from '@/utils/image'

const route = useRoute()
const router = useRouter()

// 商品数据
const product = ref({
  id: 0,
  name: '',
  subTitle: '',
  price: 0,
  stock: 0,
  mainImage: '',
  subImages: '',
  categoryId: 0,
  categoryName: '',
  shopId: null,
  category: {}
})

// 店铺信息
const shopInfo = ref({
  id: 0,
  shopName: '',
  shopLogo: '',
  shopBanner: '',
  description: '',
  contactPhone: '',
  contactAddress: '',
  shopScore: 0,
  salesCount: 0,
  productCount: 0,
  followerCount: 0,
  status: 1
})

// 商品详情数据
const productDetail = ref({
  detail: '',
  param: {}
})

// 商品评价数据
const productComments = ref([])
const commentStats = ref({
  total: 0,
  averageScore: 0,
  scoreDistribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
})
const commentLoading = ref(false)
const commentPageParams = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 商品图片相关
const currentImageIndex = ref(0)
const quantity = ref(1)
const addingToCart = ref(false)
const isFavorite = ref(false)
const activeTab = ref('detail')

// 放大镜相关
const zoomVisible = ref(false)
const zoomPosition = ref({ x: 0, y: 0 })
const zoomImageUrl = ref('')

// 商品类型选择（预留接口）
const selectedProductType = ref('默认类型')
const productTypes = ref(['默认类型', '类型1', '类型2', '类型3'])

// 加载商品数据
const loadProductData = async () => {
  const productId = route.query.id
  console.log('加载商品详情，ID:', productId)
  
  if (!productId) {
    ElMessage.error('商品不存在')
    router.push('/products')
    return
  }
  
  try {
    const response = await getProduct(productId)
    console.log('商品详情API响应:', response)
    
    if (response.code === 200 && response.data) {
      product.value = response.data
      console.log('商品数据:', product.value)
      
      // 确保category对象存在
      if (!product.value.category && product.value.categoryId) {
        product.value.category = {
          id: product.value.categoryId,
          name: product.value.categoryName || `分类ID: ${product.value.categoryId}`
        }
      }
      
      // 如果有商品详情数据
      if (response.data.productDetail) {
        productDetail.value = response.data.productDetail
      }
      console.log('店1',response)
      // loadShopInfo(response.data.shopId)
      loadShopInfo(1)
      // 如果有店铺ID，加载店铺信息
      if (response.data.shopId) {
      } else if (response.data.shop) {
        // 如果商品数据中直接包含店铺信息
        shopInfo.value = {
          ...shopInfo.value,
          ...response.data.shop,
          shopName: response.data.shop.shopName || response.data.shop.name || '官方旗舰店',
          shopScore: response.data.shop.shopScore || 0
        }
      }
    } else {
      ElMessage.error(response.msg || '获取商品信息失败')
      router.push('/products')
    }
  } catch (error) {
    console.error('加载商品数据失败:', error)
    ElMessage.error('加载商品数据失败')
    router.push('/products')
  }
}

// 加载店铺信息
const loadShopInfo = async (shopId) => {
  try {
    console.log('店铺',shopId)
    const response = await getShopInfo(shopId)
    console.log('店铺信息响应:', response)
    
    if (response.code === 200 && response.data) {
      shopInfo.value = {
        ...shopInfo.value,
        ...response.data,
        shopName: response.data.shopName || '官方旗舰店',
        shopScore: parseFloat(response.data.shopScore || 0)
      }
    } else {
      console.warn('获取店铺信息失败，使用默认店铺信息')
    }
  } catch (error) {
    console.error('加载店铺信息失败:', error)
  }
}

// 加载商品评价
const loadProductComments = async () => {
  if (!product.value.id) return
  
  commentLoading.value = true
  try {
    const params = {
      productId: product.value.id,
      pageNum: commentPageParams.value.pageNum,
      pageSize: commentPageParams.value.pageSize
    }
    
    const response = await listProductComments(params)
    console.log('商品评价列表响应:', response)
    
    if (response.code === 200) {
      // 从响应中获取评论数据
      const comments = response.rows || []
      
      // 如果没有评论，使用模拟数据（临时方案）
      if (comments.length === 0) {
        productComments.value = getMockComments()
        commentPageParams.value.total = productComments.value.length
      } else {
        productComments.value = comments
        commentPageParams.value.total = response.total || 0
      }
    } else {
      // 如果API返回错误，使用模拟数据
      productComments.value = getMockComments()
      commentPageParams.value.total = productComments.value.length
    }
  } catch (error) {
    console.error('加载商品评价失败:', error)
    // 使用模拟数据
    productComments.value = getMockComments()
    commentPageParams.value.total = productComments.value.length
  } finally {
    commentLoading.value = false
  }
}

// 加载评价统计
const loadCommentStats = async () => {
  if (!product.value.id) return
  
  try {
    const response = await getProductCommentStats(product.value.id)
    console.log('评价统计响应:', response)
    
    if (response.code === 200 && response.data) {
      const statsData = response.data
      
      // 格式化统计信息
      commentStats.value = {
        total: statsData.totalCount || 0,
        averageScore: parseFloat(statsData.averageScore || 0),
        scoreDistribution: statsData.scoreDistribution || { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
      }
    } else {
      // 如果没有统计信息，使用模拟数据
      commentStats.value = getMockCommentStats()
    }
  } catch (error) {
    console.error('加载评价统计失败:', error)
    // 使用模拟统计数据
    commentStats.value = getMockCommentStats()
  }
}

// 模拟评论数据（临时方案，等后端修复后移除）
const getMockComments = () => {
  return [
    {
      commentId: 1,
      productId: product.value.id,
      userId: 1,
      nickName: '张先生',
      avatar: '/src/assets/images/default-avatar.png',
      content: '商品质量很好，发货速度也很快，非常满意！',
      score: 5,
      images: [],
      likeCount: 12,
      replyCount: 3,
      createTime: '2025-12-20 10:30:00',
      isAnonymous: 0
    },
    {
      commentId: 2,
      productId: product.value.id,
      userId: 2,
      nickName: '李小姐',
      avatar: '/src/assets/images/default-avatar.png',
      content: '性价比很高，使用起来很方便，会推荐给朋友。',
      score: 4,
      images: [],
      likeCount: 5,
      replyCount: 1,
      createTime: '2025-12-19 14:20:00',
      isAnonymous: 0
    }
  ]
}

// 模拟统计信息（临时方案）
const getMockCommentStats = () => {
  return {
    total: 156,
    averageScore: 4.7,
    scoreDistribution: {
      5: 120,
      4: 25,
      3: 8,
      2: 2,
      1: 1
    }
  }
}

// 计算所有图片
const allImages = computed(() => {
  const images = []
  
  // 添加主图
  if (product.value.mainImage) {
    images.push(getProductImageUrl(product.value.mainImage))
  }
  
  // 如果有子图片，解析并添加
  if (product.value.subImages) {
    try {
      const subImages = product.value.subImages.split(',')
      subImages.forEach(img => {
        if (img.trim()) {
          images.push(getProductImageUrl(img.trim()))
        }
      })
    } catch (e) {
      console.error('解析子图片失败:', e)
    }
  }
  
  // 如果没有图片，使用默认图片
  if (images.length === 0) {
    images.push('/src/assets/images/default-product.png')
  }
  
  return images
})

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 格式化店铺评分显示
const formatShopScore = (score) => {
  const s = parseFloat(score || 0)
  return s.toFixed(1)
}

// 格式化评分百分比
const formatScorePercent = (score) => {
  if (score <= 0) return '0%'
  const percent = (score / 5) * 100
  return `${percent.toFixed(0)}%`
}

// 放大镜功能
const handleImageMouseEnter = (imageUrl) => {
  zoomImageUrl.value = imageUrl
  zoomVisible.value = true
}

const handleImageMouseMove = (event) => {
  const container = event.currentTarget
  const rect = container.getBoundingClientRect()
  
  // 计算鼠标在图片中的位置百分比
  const x = ((event.clientX - rect.left) / rect.width) * 100
  const y = ((event.clientY - rect.top) / rect.height) * 100
  
  // 限制在0-100%之间
  zoomPosition.value = {
    x: Math.max(0, Math.min(100, x)),
    y: Math.max(0, Math.min(100, y))
  }
}

const handleImageMouseLeave = () => {
  zoomVisible.value = false
}

// 切换主图
const changeMainImage = (index) => {
  currentImageIndex.value = index
  zoomVisible.value = false
}

// 数量变化处理
const handleQuantityChange = (value) => {
  if (value > (product.value.stock || 999)) {
    ElMessage.warning('库存不足')
    quantity.value = product.value.stock || 1
  }
}

// 添加到购物车
const handleAddToCart = async () => {
  if (!product.value.id) {
    ElMessage.warning('商品信息不完整')
    return
  }
  
  addingToCart.value = true
  try {
    const cartData = {
      productId: product.value.id,
      productName: product.value.name,
      productSubtitle: product.value.subTitle,
      productMainImage: product.value.mainImage,
      quantity: quantity.value,
      productUnitPrice: product.value.price,
      selected: 1,
      productTotalPrice: product.value.price * quantity.value
    }
    
    console.log('添加到购物车数据:', cartData)
    
    const response = await addCart(cartData)
    console.log('添加到购物车响应:', response)
    
    if (response.code === 200) {
      ElMessage.success('已成功加入购物车')
    } else {
      ElMessage.error(response.msg || '加入购物车失败')
    }
  } catch (error) {
    console.error('添加到购物车失败:', error)
    ElMessage.error('添加到购物车失败，请稍后重试')
  } finally {
    addingToCart.value = false
  }
}

// 立即购买
const handleBuyNow = () => {
  if (!product.value.id) {
    ElMessage.warning('商品信息不完整')
    return
  }
  
  const orderData = {
    productId: product.value.id,
    quantity: quantity.value,
    productName: product.value.name,
    price: product.value.price,
    mainImage: product.value.mainImage
  }
  
  // 跳转到确认订单页面
  router.push({
    path: '/order/confirm',
    query: orderData
  })
}

// 收藏商品
const handleToggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  if (isFavorite.value) {
    ElMessage.success('已收藏商品')
  } else {
    ElMessage.info('已取消收藏')
  }
}

// 联系客服
const contactCustomerService = () => {
  if (shopInfo.value.contactPhone) {
    ElMessageBox.confirm(
      `联系电话: ${shopInfo.value.contactPhone}`,
      '联系客服',
      {
        confirmButtonText: '拨打电话',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(() => {
      window.location.href = `tel:${shopInfo.value.contactPhone}`
    })
  } else {
    ElMessageBox.alert('客服功能正在开发中，敬请期待！', '提示', {
      confirmButtonText: '确定'
    })
  }
}

// 进店逛逛
const goToShopPage = () => {
  if (shopInfo.value.id) {
    router.push({
      path: '/shop',
      query: { id: shopInfo.value.id }
    })
  } else {
    ElMessage.warning('店铺信息不完整')
  }
}

onMounted(() => {
  loadProductData()
  loadProductComments()
  loadCommentStats()
})
</script>

<template>
  <div class="product-detail-container">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="product.categoryName">
          {{ product.categoryName }}
        </el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="detail-layout">
      <!-- 左侧内容区域 -->
      <div class="left-column">
        <!-- 店铺信息 -->
        {{ shopInfo.shopName }}

        <!-- 商品图片展示区域 -->
        <div class="product-images-section">
          <!-- 商品大图（带放大镜） -->
          <div class="main-image-container">
            <div 
              class="main-image-wrapper"
              @mouseenter="handleImageMouseEnter(allImages[currentImageIndex])"
              @mousemove="handleImageMouseMove"
              @mouseleave="handleImageMouseLeave"
            >
              <img 
                :src="allImages[currentImageIndex]" 
                :alt="product.name" 
                class="main-image"
                @error="handleImageError"
              />
            </div>
          </div>
          
          <!-- 商品小图列表 -->
          <div class="thumbnail-list">
            <div 
              v-for="(img, index) in allImages" 
              :key="index"
              :class="['thumbnail-item', { active: currentImageIndex === index }]"
              @click="changeMainImage(index)"
            >
              <img 
                :src="img" 
                :alt="`${product.name}-${index + 1}`"
                @error="handleImageError"
              />
            </div>
          </div>
        </div>

        <!-- 用户评价 -->
        <div class="comments-section">
          <div class="section-header">
            <h3>用户评价</h3>
            <div class="comment-stats">
              综合评分 <span class="score">{{ commentStats.averageScore.toFixed(1) }}</span> 分
              <span class="count">（{{ commentStats.total }}条评价）</span>
            </div>
          </div>
          
          <!-- 评分分布 -->
          <div class="score-distribution" v-if="commentStats.total > 0">
            <div class="distribution-item" v-for="score in 5" :key="score">
              <span class="score-label">{{ 6 - score }}星</span>
              <el-progress 
                :percentage="(commentStats.scoreDistribution[6 - score] || 0) / commentStats.total * 100" 
                :show-text="false"
                class="score-progress"
              />
              <span class="score-count">{{ commentStats.scoreDistribution[6 - score] || 0 }}条</span>
            </div>
          </div>
          
          <!-- 评价列表 -->
          <div v-loading="commentLoading">
            <div v-if="productComments.length > 0" class="comment-list">
              <div v-for="comment in productComments.slice(0, 5)" :key="comment.commentId" class="comment-item">
                <div class="comment-header">
                  <div class="user-info">
                    <el-avatar 
                      :size="40" 
                      :src="comment.avatar || '/src/assets/images/default-avatar.png'"
                      class="user-avatar"
                    />
                    <div class="user-details">
                      <div class="user-name">
                        {{ comment.isAnonymous ? '匿名用户' : (comment.nickName || `用户${comment.userId}`) }}
                      </div>
                      <div class="comment-meta">
                        <span class="comment-time">{{ comment.createTime }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="comment-rating">
                    <el-rate
                      :model-value="comment.score || 5"
                      disabled
                      size="small"
                      text-color="#ff9900"
                    />
                  </div>
                </div>
                <div class="comment-content">
                  {{ comment.content }}
                </div>
              </div>
            </div>
            <div v-else class="no-comments">
              暂无评价
            </div>
          </div>
        </div>

        <!-- 图文详情 -->
        <div class="detail-section">
          <div class="section-header">
            <h3>图文详情</h3>
          </div>
          <div class="detail-content">
            <!-- 使用商品所有图片作为图文详情 -->
            <div v-if="allImages.length > 0" class="detail-images">
              <div 
                v-for="(img, index) in allImages" 
                :key="index"
                class="detail-image-item"
              >
                <img 
                  :src="getProductImageUrl(img)" 
                  :alt="`${product.name}-图${index + 1}`"
                  @error="handleImageError"
                />
              </div>
            </div>
            <div v-else class="no-detail">
              暂无详情
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧固定内容区域 -->
      <div class="right-column">
        <!-- 放大镜显示区域（在右侧内容上方显示） -->
        <div 
          v-if="zoomVisible" 
          class="zoom-container"
          :style="{
            backgroundImage: `url(${zoomImageUrl})`,
            backgroundPosition: `${zoomPosition.x}% ${zoomPosition.y}%`
          }"
        ></div>
        
        <div class="product-info-card">
          <!-- 商品名称 -->
          <h1 class="product-title">{{ product.name }}</h1>
          
          <!-- 商品简介 -->
          <div class="product-subtitle">{{ product.subTitle }}</div>
          
          <!-- 价格区域 -->
          <div class="price-section">
            <div class="price-label">价格</div>
            <div class="price-value">¥{{ formatPrice(product.price) }}</div>
          </div>
          
          <!-- 商品信息 -->
          <div class="product-info-list">
            <div class="info-item">
              <span class="info-label">分类：</span>
              <span class="info-value">{{ product.categoryName || product.category?.name || '未分类' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">库存：</span>
              <span class="info-value">{{ product.stock || 0 }} 件</span>
            </div>
          </div>
          
          <!-- 服务承诺 -->
          <div class="service-promises">
            <div class="service-item">
              <el-icon color="#67C23A"><CircleCheck /></el-icon>
              <span>现货，付款后48小时内发货，晚发必赔</span>
            </div>
            <div class="service-item">
              <el-icon color="#67C23A"><CircleCheck /></el-icon>
              <span>快递: 免运费</span>
            </div>
            <div class="service-item">
              <el-icon color="#67C23A"><CircleCheck /></el-icon>
              <span>退货宝极速退款</span>
            </div>
            <div class="service-item">
              <el-icon color="#67C23A"><CircleCheck /></el-icon>
              <span>支持7天无理由退换</span>
            </div>
          </div>
          
          <!-- 商品类型选择（预留） -->
          <div class="product-type-section" v-if="productTypes.length > 1">
            <div class="type-label">类型：</div>
            <div class="type-options">
              <div 
                v-for="type in productTypes" 
                :key="type"
                :class="['type-option', { selected: selectedProductType === type }]"
                @click="selectedProductType = type"
              >
                {{ type }}
              </div>
            </div>
          </div>
          
          <!-- 数量选择 -->
          <div class="quantity-section">
            <div class="quantity-label">数量</div>
            <div class="quantity-control">
              <el-input-number 
                v-model="quantity" 
                :min="1" 
                :max="product.stock || 999"
                size="large"
                @change="handleQuantityChange"
              />
              <div class="stock-info">库存 {{ product.stock || 0 }} 件</div>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button 
              type="danger" 
              size="large" 
              class="buy-now-btn"
              @click="handleBuyNow"
            >
              <el-icon><ShoppingCart /></el-icon>
              立即购买
            </el-button>
            
            <el-button 
              type="primary" 
              size="large" 
              class="add-cart-btn"
              @click="handleAddToCart"
              :loading="addingToCart"
            >
              <el-icon><ShoppingCartFull /></el-icon>
              加入购物车
            </el-button>
            
            <el-button 
              class="favorite-btn"
              size="large"
              @click="handleToggleFavorite"
            >
              <el-icon v-if="isFavorite"><StarFilled /></el-icon>
              <el-icon v-else><Star /></el-icon>
              收藏
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-layout {
  display: flex;
  gap: 40px;
  position: relative;
}

/* 左侧内容区域 */
.left-column {
  flex: 1;
  min-width: 0;
}

/* 右侧固定区域 */
.right-column {
  width: 420px;
  position: sticky;
  top: 20px;
  height: fit-content;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
}

/* 店铺信息卡片 */
.shop-info-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.shop-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.shop-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}

.shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-details {
  flex: 1;
  min-width: 0;
}

.shop-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.shop-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-text {
  font-size: 14px;
  color: #ff9900;
  font-weight: bold;
}

.shop-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

/* 商品图片区域 */
.product-images-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #f0f0f0;
}

.main-image-container {
  position: relative;
  width: 100%;
  height: 500px;
  margin-bottom: 20px;
  overflow: hidden;
  border-radius: 8px;
}

.main-image-wrapper {
  width: 100%;
  height: 100%;
  cursor: zoom-in;
  position: relative;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 放大镜容器 */
.zoom-container {
  position: absolute;
  right: calc(100% + 20px);
  top: 0;
  width: 500px;
  height: 500px;
  background-color: white;
  background-size: 200%;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  pointer-events: none;
}

/* 缩略图列表 */
.thumbnail-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 10px 0;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  transition: border-color 0.3s;
}

.thumbnail-item.active {
  border-color: #ff0036;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 评价区域 */
.comments-section,
.detail-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #f0f0f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.comment-stats {
  color: #666;
}

.comment-stats .score {
  color: #ff0036;
  font-size: 24px;
  font-weight: bold;
  margin: 0 5px;
}

.score-distribution {
  margin-bottom: 20px;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.score-label {
  width: 40px;
  color: #666;
}

.score-progress {
  flex: 1;
  margin: 0 10px;
}

.score-count {
  width: 60px;
  text-align: right;
  color: #999;
  font-size: 12px;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  background-color: #409eff;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.comment-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

.no-comments {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

/* 图文详情 */
.detail-content {
  margin-top: 20px;
}

.detail-images {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-image-item {
  width: 100%;
}

.detail-image-item img {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 4px;
}

.no-detail {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

/* 右侧商品信息卡片 */
.product-info-card {
  background: #fff;
  border-radius: 8px;
  padding: 25px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: relative;
}

.product-title {
  font-size: 22px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.product-subtitle {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
  line-height: 1.6;
}

/* 价格区域 */
.price-section {
  background: #fff5f5;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price-label {
  font-size: 14px;
  color: #999;
}

.price-value {
  font-size: 28px;
  color: #ff0036;
  font-weight: bold;
}

/* 商品信息列表 */
.product-info-list {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-label {
  color: #666;
  width: 60px;
}

.info-value {
  color: #333;
  flex: 1;
}

/* 服务承诺 */
.service-promises {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.service-item:last-child {
  margin-bottom: 0;
}

/* 商品类型选择 */
.product-type-section {
  margin-bottom: 20px;
}

.type-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.type-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.type-option {
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.type-option:hover {
  border-color: #409eff;
  color: #409eff;
}

.type-option.selected {
  border-color: #ff0036;
  color: #ff0036;
  background: #fff5f5;
}

/* 数量选择 */
.quantity-section {
  margin-bottom: 25px;
}

.quantity-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stock-info {
  font-size: 14px;
  color: #999;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.buy-now-btn {
  background: linear-gradient(135deg, #ff0036 0%, #ff4466 100%);
  border: none;
  padding: 15px;
  font-size: 16px;
  font-weight: bold;
  height: 50px;
}

.add-cart-btn {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  padding: 15px;
  font-size: 16px;
  font-weight: bold;
  height: 50px;
}

.favorite-btn {
  border: 1px solid #e0e0e0;
  color: #666;
  padding: 15px;
  font-size: 16px;
  height: 50px;
}

.favorite-btn:hover {
  border-color: #ff0036;
  color: #ff0036;
}

/* 响应式设计 */
@media (max-width: 1500px) {
  .detail-layout {
    flex-direction: column;
  }
  
  .right-column {
    width: 100%;
    position: static;
    margin-top: 20px;
  }
  
  .zoom-container {
    position: fixed;
    right: 20px;
    top: 100px;
    width: 400px;
    height: 400px;
    z-index: 1000;
  }
}

@media (max-width: 768px) {
  .product-detail-container {
    padding: 10px;
  }
  
  .main-image-container {
    height: 400px;
  }
  
  .shop-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .shop-actions {
    width: 100%;
    justify-content: center;
  }
  
  .zoom-container {
    width: 300px;
    height: 300px;
    right: 10px;
  }
  
  .thumbnail-item {
    width: 60px;
    height: 60px;
  }
}

@media (max-width: 480px) {
  .main-image-container {
    height: 300px;
  }
  
  .product-title {
    font-size: 18px;
  }
  
  .price-value {
    font-size: 24px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .buy-now-btn,
  .add-cart-btn,
  .favorite-btn {
    width: 100%;
  }
  
  .zoom-container {
    width: 200px;
    height: 200px;
  }
}
</style>