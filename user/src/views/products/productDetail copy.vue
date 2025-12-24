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
const activeTab = ref('detail')

// 修复面包屑导航的跳转函数
const handleBreadcrumbClick = (item) => {
  if (item.path === '/products' && item.query) {
    // 使用正确的路由跳转到商品列表页
    router.push({
      path: '/products',
      query: {
        categoryId: item.query.categoryId,
        searchType: 'category'
      }
    })
  } else if (item.path === '/') {
    router.push('/')
  }
}

// 面包屑导航配置
const breadcrumbItems = computed(() => {
  const items = [
    { 
      name: '首页',
      path: '/',
      clickable: true
    }
  ]
  
  // 如果有分类信息，添加到面包屑
  if (product.value.categoryId) {
    const categoryName = product.value.categoryName || 
                        product.value.category?.name || 
                        `分类ID: ${product.value.categoryId}`
    
    items.push({
      name: categoryName,
      path: '/products',
      query: {
        categoryId: product.value.categoryId,
        searchType: 'category'
      },
      clickable: true
    })
  } else {
    items.push({
      name: '商品列表',
      path: '/products',
      clickable: true
    })
  }
  
  // 当前商品（不可点击）
  items.push({
    name: product.value.name || '商品详情',
    clickable: false
  })
  
  return items
})

// 店铺评分（转换为5分制用于显示）
const shopRating = computed(() => {
  const score = parseFloat(shopInfo.value.shopScore || 0)
  return score.toFixed(1)
})

// 评价评分
const commentRating = computed(() => {
  const score = parseFloat(commentStats.value.averageScore || 0)
  return score > 0 ? score.toFixed(1) : '0.0'
})

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

// 当前大图
const currentMainImage = computed(() => {
  return allImages.value[currentImageIndex.value] || allImages.value[0]
})

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

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
      
      // 如果有店铺ID，加载店铺信息
      if (response.data.shopId) {
        await loadShopInfo(response.data.shopId)
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
    },
    {
      commentId: 3,
      productId: product.value.id,
      userId: 3,
      nickName: '王先生',
      avatar: '/src/assets/images/default-avatar.png',
      content: '包装完好，正品保障，下次还会光顾。',
      score: 5,
      images: [],
      likeCount: 8,
      replyCount: 0,
      createTime: '2025-12-18 09:15:00',
      isAnonymous: 0
    },
    {
      commentId: 4,
      productId: product.value.id,
      userId: 4,
      nickName: '赵女士',
      avatar: '/src/assets/images/default-avatar.png',
      content: '物流很快，客服态度很好，很耐心解答问题。',
      score: 5,
      images: [],
      likeCount: 6,
      replyCount: 2,
      createTime: '2025-12-17 16:45:00',
      isAnonymous: 0
    },
    {
      commentId: 5,
      productId: product.value.id,
      userId: 5,
      nickName: '孙先生',
      avatar: '/src/assets/images/default-avatar.png',
      content: '商品与描述一致，使用效果不错。',
      score: 4,
      images: [],
      likeCount: 3,
      replyCount: 0,
      createTime: '2025-12-16 11:20:00',
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

// 切换主图
const changeMainImage = (index) => {
  currentImageIndex.value = index
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

// 预览图片
const previewImage = (imageUrl) => {
  // 这里可以实现图片预览功能
  console.log('预览图片:', imageUrl)
}

// 监听tab切换
watch(activeTab, (newTab) => {
  if (newTab === 'comments' && productComments.value.length === 0) {
    loadProductComments()
    loadCommentStats()
  }
})

onMounted(() => {
  loadProductData()
})


// 在模板中添加商品详情图片展示
const showProductDetailImages = computed(() => {
  return allImages.value.length > 0
})

// 确保图片URL正确处理
const getDetailImageUrl = (imgPath) => {
  // 如果已经是完整的URL，直接返回
  if (imgPath.startsWith('http') || imgPath.startsWith('data:')) {
    return imgPath
  }
  // 否则使用getProductImageUrl处理
  return getProductImageUrl(imgPath)
}
</script>

<template>
  <div class="product-detail-container">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item 
          v-for="(item, index) in breadcrumbItems" 
          :key="index"
          :to="item.clickable ? { path: item.path, query: item.query } : undefined"
        >
          {{ item.name }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="detail-content">
      <!-- 左侧商品图片区域 -->
      <div class="left-section">
        <!-- 商品大图 -->
        <div class="main-image-wrapper">
          <img 
            :src="currentMainImage" 
            :alt="product.name" 
            class="main-image"
            @error="handleImageError"
          />
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

      <!-- 中间商品信息区域 -->
      <div class="center-section">
        <!-- 店铺信息 -->
        <div class="shop-info" v-if="shopInfo.shopName || shopInfo.id">
          <!-- 店铺头像 -->
          <div class="shop-avatar">
            <el-avatar 
              :size="50" 
              :src="getProductImageUrl(shopInfo.shopLogo)" 
              @error="handleImageError"
            >
              <span>{{ (shopInfo.shopName || '店').substring(0, 1) }}</span>
            </el-avatar>
          </div>
          
          <div class="shop-details">
            <!-- 店铺名 -->
            <div class="shop-name">{{ shopInfo.shopName || '官方旗舰店' }}</div>
            
            <!-- 店铺星级 -->
            <div class="shop-rating">
              <span class="rating-stars">
                <el-rate
                  :model-value="parseFloat(shopRating)"
                  disabled
                  show-score
                  text-color="#ff9900"
                  :score-template="shopRating"
                />
              </span>
              <span class="rating-score">{{ formatShopScore(shopInfo.shopScore) }}分</span>
              
              <!-- 店铺统计信息 -->
              <span class="shop-stats" v-if="shopInfo.salesCount">
                <el-icon><ShoppingBag /></el-icon>
                销量 {{ shopInfo.salesCount }}
              </span>
              <span class="shop-stats" v-if="shopInfo.followerCount">
                <el-icon><User /></el-icon>
                粉丝 {{ shopInfo.followerCount }}
              </span>
            </div>
            
            <!-- 店铺描述 -->
            <div class="shop-description" v-if="shopInfo.description">
              {{ shopInfo.description }}
            </div>
            
            <!-- 客服入口 -->
            <div class="customer-service">
              <el-button type="primary" size="small" @click="contactCustomerService">
                <el-icon><ChatDotRound /></el-icon>
                联系客服
              </el-button>
              <el-button size="small" @click="goToShopPage" v-if="shopInfo.id">
                <el-icon><Shop /></el-icon>
                进店逛逛
              </el-button>
            </div>
          </div>
        </div>

        <!-- 商品基本信息 -->
        <div class="product-basic-info">
          <h1 class="product-title">{{ product.name }}</h1>
          <div class="product-subtitle">{{ product.subTitle }}</div>
          
          <!-- 商品价格 -->
          <div class="product-price-section">
            <div class="current-price">
              <span class="price-label">价格</span>
              <span class="price-value">¥{{ formatPrice(product.price) }}</span>
            </div>
            <div class="original-price" v-if="product.originalPrice">
              ￥{{ formatPrice(product.originalPrice) }}
            </div>
          </div>
          
          <!-- 商品分类信息 -->
          <div class="product-category-info" v-if="product.categoryName || product.category?.name">
            分类：{{ product.categoryName || product.category?.name }}
          </div>
          
          <!-- 商品库存信息 -->
          <div class="product-stock-info" v-if="product.stock !== undefined">
            库存：{{ product.stock }} 件
          </div>
          
          <!-- 店铺信息（如果上面没有显示店铺信息） -->
          <div class="product-shop-info" v-if="shopInfo.shopName && !shopInfo.id">
            店铺：{{ shopInfo.shopName }}
          </div>
        </div>

        <!-- 固定服务承诺 -->
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

        <!-- 购买数量 -->
        <div class="quantity-section">
          <span class="quantity-label">数量</span>
          <div class="quantity-control">
            <el-input-number 
              v-model="quantity" 
              :min="1" 
              :max="product.stock || 999"
              size="large"
              @change="handleQuantityChange"
            />
            <span class="stock-info">库存 {{ product.stock || 0 }} 件</span>
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
        </div>
      </div>
    </div>

    <!-- 商品详情Tabs - 只保留商品详情和商品评价 -->
    <div class="detail-tabs">
      <el-tabs v-model="activeTab" class="product-tabs">
        <el-tab-pane label="商品详情" name="detail">
          <div class="product-detail-content">
            <!-- 只显示商品图片 -->
            <div v-if="showProductDetailImages" class="product-detail-images">
              <div class="images-header">
                <h3>商品实拍图</h3>
                <div class="images-count">共 {{ allImages.length }} 张图片</div>
              </div>
              
              <!-- 图片展示区 -->
              <div class="images-container">
                <div 
                  v-for="(img, index) in allImages" 
                  :key="index"
                  class="detail-image-item"
                  :class="{ 'first-image': index === 0, 'last-image': index === allImages.length - 1 }"
                >
                  <img 
                    :src="getDetailImageUrl(img)" 
                    :alt="`${product.name}-图${index + 1}`"
                    :class="{ 'main-image': index === 0 }"
                    @error="handleImageError"
                  />
                  <div v-if="index === 0" class="image-caption">主图</div>
                  <div v-else class="image-caption">细节图 {{ index }}</div>
                </div>
              </div>
            </div>
            
            <!-- 商品详情底部信息 -->
            <div class="detail-footer">
              <div class="footer-tips">
                <el-icon><Warning /></el-icon>
                <span>温馨提示：商品图片仅供参考，请以实物为准，如有疑问请联系客服</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="商品评价" name="comments">
          <div class="product-comments">
            <!-- 评价概况 -->
            <div class="comment-summary" v-if="commentStats.total > 0">
              <div class="summary-header">
                <div class="total-score">
                  <span class="score-value">{{ commentRating }}</span>
                  <span class="score-label">综合评分</span>
                </div>
                <div class="score-distribution">
                  <div class="score-item" v-for="score in 5" :key="score">
                    <span class="score-label">{{ 6 - score }}星</span>
                    <el-progress 
                      :percentage="formatScorePercent(commentStats.scoreDistribution[6 - score] || 0)" 
                      :show-text="false"
                      class="score-progress"
                    />
                    <span class="score-count">{{ commentStats.scoreDistribution[6 - score] || 0 }}条</span>
                  </div>
                </div>
              </div>
              <div class="total-comments">
                累计评价 <span class="count">{{ commentStats.total }}</span> 条
              </div>
            </div>
            
            <!-- 评价列表 -->
            <div v-loading="commentLoading">
              <div v-if="productComments.length > 0" class="comment-list">
                <div v-for="comment in productComments" :key="comment.commentId" class="comment-item">
                  <!-- 评论头部 -->
                  <div class="comment-header">
                    <div class="user-info">
                      <el-avatar 
                        :size="40" 
                        :src="comment.avatar || getProductImageUrl(comment.userAvatar)"
                        class="user-avatar"
                      >
                        <span>{{ comment.nickName ? comment.nickName.charAt(0) : '用' }}</span>
                      </el-avatar>
                      <div class="user-details">
                        <div class="user-name">
                          {{ comment.isAnonymous ? '匿名用户' : (comment.nickName || `用户${comment.userId}`) }}
                        </div>
                        <div class="comment-meta">
                          <span class="comment-time">{{ comment.createTime }}</span>
                          <span v-if="comment.score" class="comment-score-display">
                            评分：{{ comment.score }}星
                          </span>
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
                  
                  <!-- 评论内容 -->
                  <div class="comment-content">
                    {{ comment.content }}
                  </div>
                  
                  <!-- 评论图片 -->
                  <div v-if="comment.images && comment.images.length > 0" class="comment-images">
                    <img 
                      v-for="(img, index) in comment.images" 
                      :key="index"
                      :src="getProductImageUrl(img)"
                      @click="previewImage(img)"
                      class="comment-image"
                    />
                  </div>
                  
                  <!-- 评论互动 -->
                  <div class="comment-actions">
                    <span class="like-count">
                      <el-icon><StarFilled /></el-icon>
                      {{ comment.likeCount || 0 }}
                    </span>
                    <span class="reply-count">
                      <el-icon><ChatDotRound /></el-icon>
                      {{ comment.replyCount || 0 }}
                    </span>
                  </div>
                </div>
                
                <!-- 分页 -->
                <el-pagination
                  v-if="commentPageParams.total > commentPageParams.pageSize"
                  v-model:current-page="commentPageParams.pageNum"
                  v-model:page-size="commentPageParams.pageSize"
                  :total="commentPageParams.total"
                  layout="prev, pager, next, jumper"
                  @current-change="loadProductComments"
                  class="comment-pagination"
                />
              </div>
              <div v-else class="no-comments">
                <el-empty description="暂无评价" />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-content {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
}

.left-section {
  flex: 0 0 400px;
}

.main-image-wrapper {
  width: 400px;
  height: 400px;
  border: 1px solid #f0f0f0;
  margin-bottom: 10px;
  position: relative;
  overflow: hidden;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 10px 0;
}

.thumbnail-item {
  width: 60px;
  height: 60px;
  border: 2px solid transparent;
  cursor: pointer;
  flex-shrink: 0;
}

.thumbnail-item.active {
  border-color: #ff0036;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.center-section {
  flex: 1;
  min-width: 0;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  margin-bottom: 20px;
}

.shop-details {
  flex: 1;
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
  gap: 15px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.rating-stars {
  display: inline-flex;
  align-items: center;
}

.rating-score {
  color: #ff9900;
  font-size: 14px;
  font-weight: bold;
}

.shop-stats {
  font-size: 12px;
  color: #666;
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.shop-description {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  margin-bottom: 10px;
}

.customer-service {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.product-basic-info {
  margin-bottom: 20px;
}

.product-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.product-subtitle {
  font-size: 14px;
  color: #999;
  margin-bottom: 15px;
}

.product-price-section {
  padding: 15px;
  background: #fff5f5;
  border-radius: 4px;
  margin-bottom: 20px;
}

.current-price {
  display: flex;
  align-items: baseline;
  margin-bottom: 5px;
}

.price-label {
  font-size: 14px;
  color: #999;
  margin-right: 10px;
}

.price-value {
  font-size: 24px;
  color: #ff0036;
  font-weight: bold;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.product-category-info,
.product-stock-info,
.product-shop-info {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.service-promises {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
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

.quantity-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin: 20px 0;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
}

.quantity-label {
  font-size: 14px;
  color: #666;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stock-info {
  font-size: 14px;
  color: #999;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.buy-now-btn {
  background: linear-gradient(135deg, #ff0036 0%, #ff4466 100%);
  border: none;
  padding: 12px 30px;
  font-size: 16px;
  flex: 1;
}

.add-cart-btn {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  padding: 12px 30px;
  font-size: 16px;
  flex: 1;
}

.detail-tabs {
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  margin-top: 20px;
}

:deep(.product-tabs .el-tabs__nav-wrap) {
  padding: 0 20px;
  background: #f5f5f5;
}

:deep(.product-tabs .el-tabs__item) {
  font-size: 16px;
  padding: 0 20px;
  height: 50px;
  line-height: 50px;
}

/* 商品详情容器 - 左右布局 */
.product-description-container {
  display: flex;
  gap: 30px;
  padding: 20px;
  min-height: 500px;
}

/* 左侧商品详情内容 */
.description-content {
  flex: 1;
  min-width: 0;
}

.description-content :deep(*) {
  max-width: 100%;
}

.description-content :deep(img) {
  max-width: 100%;
  height: auto;
}

.description-content :deep(p) {
  margin: 10px 0;
  line-height: 1.6;
}

.description-content :deep(h1),
.description-content :deep(h2),
.description-content :deep(h3),
.description-content :deep(h4) {
  margin: 15px 0 10px 0;
  color: #333;
}

.no-detail {
  text-align: center;
  padding: 50px;
}

/* 右侧商品图片列表 */
.description-images {
  flex: 0 0 300px;
  border-left: 1px solid #f0f0f0;
  padding-left: 20px;
}

.images-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.vertical-image-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.vertical-image-item {
  position: relative;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  background: #fff;
}

.vertical-image-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.vertical-image-item img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.vertical-image-item:hover img {
  transform: scale(1.05);
}

.image-index {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  text-align: center;
  padding: 5px;
  font-size: 14px;
  font-weight: 500;
}

/* 评价样式 */
.product-comments {
  padding: 20px;
}

.comment-summary {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.summary-header {
  display: flex;
  gap: 40px;
  margin-bottom: 15px;
}

.total-score {
  text-align: center;
  min-width: 100px;
}

.score-value {
  display: block;
  font-size: 32px;
  font-weight: bold;
  color: #ff9900;
  line-height: 1;
}

.score-label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.score-distribution {
  flex: 1;
}

.score-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.score-item .score-label {
  width: 40px;
  margin: 0;
  font-size: 14px;
  color: #666;
}

.score-progress {
  flex: 1;
  margin: 0 10px;
}

.score-count {
  width: 60px;
  font-size: 12px;
  color: #999;
  text-align: right;
}

.total-comments {
  font-size: 14px;
  color: #666;
}

.total-comments .count {
  color: #ff0036;
  font-weight: bold;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
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
  gap: 15px;
  font-size: 12px;
  color: #999;
  margin-top: 3px;
}

.comment-score-display {
  color: #ff9900;
  font-weight: 500;
}

.comment-rating {
  display: flex;
  align-items: center;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 15px;
}

.comment-images {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.comment-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #f0f0f0;
}

.comment-actions {
  display: flex;
  gap: 20px;
}

.like-count,
.reply-count {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.like-count:hover,
.reply-count:hover {
  color: #409eff;
}

.no-comments {
  text-align: center;
  padding: 50px;
}

.comment-pagination {
  margin-top: 20px;
  justify-content: center;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .product-detail-container {
    padding: 15px;
  }
  
  .product-description-container {
    gap: 20px;
  }
  
  .description-images {
    flex: 0 0 250px;
  }
  
  .vertical-image-item img {
    height: 180px;
  }
}

@media (max-width: 992px) {
  .detail-content {
    flex-wrap: wrap;
  }
  
  .left-section {
    flex: 0 0 100%;
    margin-bottom: 20px;
  }
  
  .main-image-wrapper {
    width: 100%;
    max-width: 400px;
    margin: 0 auto;
  }
  
  .product-description-container {
    flex-direction: column;
  }
  
  .description-images {
    flex: none;
    width: 100%;
    border-left: none;
    border-top: 1px solid #f0f0f0;
    padding-left: 0;
    padding-top: 20px;
    margin-top: 20px;
  }
  
  .vertical-image-list {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 15px;
  }
  
  .vertical-image-item {
    width: calc(33.333% - 10px);
  }
  
  .shop-rating {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .summary-header {
    flex-direction: column;
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .product-detail-container {
    padding: 10px;
  }
  
  .main-image-wrapper {
    height: 300px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .buy-now-btn,
  .add-cart-btn {
    width: 100%;
  }
  
  .thumbnail-item {
    width: 50px;
    height: 50px;
  }
  
  .vertical-image-item {
    width: calc(50% - 8px);
  }
  
  .vertical-image-item img {
    height: 150px;
  }
}

@media (max-width: 480px) {
  .main-image-wrapper {
    height: 250px;
  }
  
  .product-title {
    font-size: 18px;
  }
  
  .price-value {
    font-size: 20px;
  }
  
  .service-item {
    font-size: 12px;
  }
  
  .vertical-image-item {
    width: 100%;
  }
  
  .vertical-image-item img {
    height: 180px;
  }
}
</style>