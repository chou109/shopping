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
  Star,
  Location,
  OfficeBuilding,
  Phone,
  Service,
  Timer,
  Check,
  Plus,
  Minus
} from '@element-plus/icons-vue'
import { getProduct } from '@/api/system/product'
import { addCart } from '@/api/system/cart'
import { getShopInfo } from '@/api/system/shop'
import {
  listProductComments,
  getProductCommentStats,
  getProductCommentsFront,
  getCommentTree
} from '@/api/system/productComment'
import { getProductImageUrl, handleImageError } from '@/utils/image'
import { listFootprintByUser, deleteFootprint, recordFootprint } from '@/api/system/footprint'
import { createDirectOrder, payOrder } from '@/api/system/orderInfo'
import { getMyAddressList } from '@/api/system/address'
import PurchaseDialog from '@/components/PurchaseDialog/index.vue'

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

// 商品规格选择
const selectedSpecs = ref([])
const productSpecs = ref([
  { name: '颜色', options: ['黑色', '白色', '红色', '蓝色'] },
  { name: '尺寸', options: ['S', 'M', 'L', 'XL'] },
  { name: '版本', options: ['标准版', '旗舰版', '尊享版'] }
])

// 购买弹窗相关
const purchaseDialogVisible = ref(false)
const purchaseProducts = ref([])
const purchaseOrder = ref({})
const addressList = ref([])
const selectedAddressId = ref(null)

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
      recordFootprint({ targetType: 1, targetId: route.query.id })

      if (!product.value.category && product.value.categoryId) {
        product.value.category = {
          id: product.value.categoryId,
          name: product.value.categoryName || `分类ID: ${product.value.categoryId}`
        }
      }

      loadShopInfo(product.value.shopId)
      // loadShopInfo(product.value.shopId)
      loadProductComments()
      loadCommentStats()
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
        id: response.data.id || response.data.shopId || 0,
        shopName: response.data.shopName || response.data.name || '官方旗舰店',
        shopLogo: response.data.shopLogo || response.data.logo || '',
        shopBanner: response.data.shopBanner || response.data.banner || '',
        description: response.data.description || '',
        contactPhone: response.data.contactPhone || response.data.phone || '',
        contactAddress: response.data.contactAddress || response.data.address || '',
        shopScore: parseFloat(response.data.shopScore || response.data.score || 0),
        salesCount: response.data.salesCount || response.data.sales || 0,
        productCount: response.data.productCount || response.data.productCount || 0,
        followerCount: response.data.followerCount || response.data.followers || 0,
        status: response.data.status || 1
      }
    } else {
      shopInfo.value = {
        id: shopId,
        shopName: '官方旗舰店',
        shopLogo: '',
        shopBanner: '',
        description: '优质商品，诚信经营',
        contactPhone: '400-000-0000',
        contactAddress: '',
        shopScore: 5.0,
        salesCount: 0,
        productCount: 0,
        followerCount: 0,
        status: 1
      }
    }
  } catch (error) {
    console.error('加载店铺信息失败:', error)
    shopInfo.value = {
      id: shopId,
      shopName: '官方旗舰店',
      shopLogo: '',
      shopBanner: '',
      description: '优质商品，诚信经营',
      contactPhone: '400-000-0000',
      contactAddress: '',
      shopScore: 5.0,
      salesCount: 0,
      productCount: 0,
      followerCount: 0,
      status: 1
    }
  }
}

// 加载商品评价
const loadProductComments = async () => {
  if (!product.value.id) return

  commentLoading.value = true
  try {
    const response = await getCommentTree(product.value.id)

    if (response.code === 200) {
      let comments = []

      if (response.rows && Array.isArray(response.rows)) {
        comments = response.rows
      } else if (response.data && Array.isArray(response.data)) {
        comments = response.data
      } else if (Array.isArray(response)) {
        comments = response
      }

      // 转换后端返回的数据格式为前端需要的格式
      productComments.value = comments.map(comment => ({
        commentId: comment.id || comment.commentId,
        productId: product.value.id,
        userId: comment.userId,
        parentId: comment.parentId || 0,
        nickName: comment.userName || '匿名用户',
        avatar: comment.userAvatar || '/src/assets/images/default-avatar.png',
        content: comment.content,
        score: comment.score || 0,
        images: comment.images ? (Array.isArray(comment.images) ? comment.images : []) : [],
        likeCount: comment.likeCount || 0,
        replyCount: comment.children ? comment.children.length : 0,
        createTime: comment.createTime,
        isAnonymous: 0,
        children: comment.children ? comment.children.map(reply => ({
          commentId: reply.id || reply.commentId,
          productId: product.value.id,
          userId: reply.userId,
          parentId: comment.id || comment.commentId,
          nickName: reply.userName || '匿名用户',
          avatar: reply.userAvatar || '/src/assets/images/default-avatar.png',
          content: reply.content,
          score: 0,
          images: reply.images ? (Array.isArray(reply.images) ? reply.images : []) : [],
          likeCount: reply.likeCount || 0,
          replyCount: reply.children ? reply.children.length : 0,
          createTime: reply.createTime,
          isAnonymous: 0,
          children: reply.children || []
        })) : []
      }))
      commentPageParams.value.total = productComments.value.length

      // 如果没有评论,使用模拟数据
      if (productComments.value.length === 0) {
        productComments.value = getMockComments()
        commentPageParams.value.total = productComments.value.length
      }

      // 评论加载完成后，重新计算统计
      commentStats.value = calculateCommentStatsFromData()
    } else {
      productComments.value = getMockComments()
      commentPageParams.value.total = productComments.value.length
      commentStats.value = calculateCommentStatsFromData()
    }
  } catch (error) {
    console.error('加载商品评价失败:', error)
    productComments.value = getMockComments()
    commentPageParams.value.total = productComments.value.length
    commentStats.value = calculateCommentStatsFromData()
  } finally {
    commentLoading.value = false
  }
}

// 加载评价统计
const loadCommentStats = async () => {
  if (!product.value.id) return

  try {
    // 先尝试使用独立API
    const response = await getProductCommentStats(product.value.id)

    if (response.code === 200 && response.data) {
      const statsData = response.data

      commentStats.value = {
        total: statsData.total || statsData.totalCount || 0,
        averageScore: parseFloat(statsData.averageScore || statsData.avgScore || 0),
        scoreDistribution: statsData.scoreDistribution || { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
      }
    } else {
      // 如果API失败，自己计算统计（只统计根评论）
      commentStats.value = calculateCommentStatsFromData()
    }
  } catch (error) {
    console.error('加载评价统计失败:', error)
    // 自己计算统计
    commentStats.value = calculateCommentStatsFromData()
  }
}

// 从评论数据计算统计（只统计根评论）
const calculateCommentStatsFromData = () => {
  if (!productComments.value || productComments.value.length === 0) {
    return {
      total: 0,
      averageScore: 0,
      scoreDistribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
    }
  }

  // 只统计根评论（排除回复）
  const rootComments = productComments.value.filter(comment =>
    !comment.parentId || comment.parentId === 0
  )

  if (rootComments.length === 0) {
    return {
      total: 0,
      averageScore: 0,
      scoreDistribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
    }
  }

  // 计算总评分和分布
  let totalScore = 0
  const scoreDistribution = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }

  rootComments.forEach(comment => {
    const score = comment.score || 0
    if (score >= 1 && score <= 5) {
      totalScore += score
      scoreDistribution[score] = (scoreDistribution[score] || 0) + 1
    }
  })

  const total = rootComments.length
  const averageScore = total > 0 ? parseFloat((totalScore / total).toFixed(1)) : 0

  return {
    total,
    averageScore,
    scoreDistribution
  }
}

// 模拟评论数据（修改版）
const getMockComments = () => {
  const rootComments = [
    {
      commentId: 1,
      productId: product.value.id,
      userId: 1,
      parentId: 0, // 根评论的 parentId 为 0
      nickName: '张先生',
      avatar: '/src/assets/images/default-avatar.png',
      content: '商品质量很好，发货速度也很快，非常满意！',
      score: 5, // 根评论有评分
      images: [],
      likeCount: 12,
      replyCount: 3,
      createTime: '2025-12-20 10:30:00',
      isAnonymous: 0,
      children: [
        {
          commentId: 11,
          productId: product.value.id,
          userId: 2,
          parentId: 1, // 回复的 parentId 为根评论的ID
          nickName: '客服小张',
          avatar: '/src/assets/images/service-avatar.png',
          content: '感谢您的认可！我们会继续努力提供更好的服务。',
          score: 0, // 回复没有评分
          images: [],
          likeCount: 2,
          replyCount: 0,
          createTime: '2025-12-20 11:00:00',
          isAnonymous: 0
        },
        {
          commentId: 12,
          productId: product.value.id,
          userId: 3,
          parentId: 1,
          nickName: '李女士',
          avatar: '/src/assets/images/default-avatar.png',
          content: '我也买了这款，确实不错！',
          score: 0, // 回复没有评分
          images: [],
          likeCount: 1,
          replyCount: 0,
          createTime: '2025-12-20 14:30:00',
          isAnonymous: 0
        }
      ]
    },
    {
      commentId: 2,
      productId: product.value.id,
      userId: 2,
      parentId: 0, // 根评论
      nickName: '李小姐',
      avatar: '/src/assets/images/default-avatar.png',
      content: '性价比很高，使用起来很方便，会推荐给朋友。',
      score: 4, // 根评论有评分
      images: [],
      likeCount: 5,
      replyCount: 1,
      createTime: '2025-12-19 14:20:00',
      isAnonymous: 0,
      children: [
        {
          commentId: 21,
          productId: product.value.id,
          userId: 4,
          parentId: 2,
          nickName: '店铺客服',
          avatar: '/src/assets/images/service-avatar.png',
          content: '感谢您的支持！期待您再次光临。',
          score: 0, // 回复没有评分
          images: [],
          likeCount: 0,
          replyCount: 0,
          createTime: '2025-12-19 15:00:00',
          isAnonymous: 0
        }
      ]
    },
    {
      commentId: 3,
      productId: product.value.id,
      userId: 3,
      parentId: 0,
      nickName: '王先生',
      avatar: '/src/assets/images/default-avatar.png',
      content: '中规中矩，没有想象中好。',
      score: 3, // 根评论有评分
      images: [],
      likeCount: 2,
      replyCount: 0,
      createTime: '2025-12-18 09:15:00',
      isAnonymous: 0,
      children: []
    }
  ]

  // 计算根评论的统计
  const total = rootComments.length
  const totalScore = rootComments.reduce((sum, comment) => sum + (comment.score || 0), 0)
  const averageScore = total > 0 ? parseFloat((totalScore / total).toFixed(1)) : 0

  // 计算评分分布
  const scoreDistribution = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  rootComments.forEach(comment => {
    const score = comment.score || 0
    if (score >= 1 && score <= 5) {
      scoreDistribution[score] = (scoreDistribution[score] || 0) + 1
    }
  })

  // 设置统计
  commentStats.value = {
    total,
    averageScore,
    scoreDistribution
  }

  return rootComments
}

// 模拟统计信息（修改版）
const getMockCommentStats = () => {
  // 根据上面的模拟数据计算
  // 只有根评论计入统计：5星1条，4星1条，3星1条
  const total = 3
  const totalScore = 5 + 4 + 3
  const averageScore = parseFloat((totalScore / total).toFixed(1))

  return {
    total: total,
    averageScore: averageScore,
    scoreDistribution: {
      5: 1,
      4: 1,
      3: 1,
      2: 0,
      1: 0
    }
  }
}

// 计算所有图片
const allImages = computed(() => {
  const images = []

  if (product.value.mainImage) {
    images.push(product.value.mainImage)
  }

  if (product.value.subImages) {
    try {
      const subImages = product.value.subImages.split(',')
      subImages.forEach(img => {
        if (img.trim()) {
          images.push(img.trim())
        }
      })
    } catch (e) {
      console.error('解析子图片失败:', e)
    }
  }

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

// 放大镜功能
const handleImageMouseEnter = (imageUrl) => {
  zoomImageUrl.value = imageUrl
  zoomVisible.value = true
}

const handleImageMouseMove = (event) => {
  const container = event.currentTarget
  const rect = container.getBoundingClientRect()

  const x = ((event.clientX - rect.left) / rect.width) * 100
  const y = ((event.clientY - rect.top) / rect.height) * 100

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

// 选择规格
const selectSpec = (specIndex, optionIndex) => {
  selectedSpecs.value[specIndex] = productSpecs.value[specIndex].options[optionIndex]
}

// 数量变化处理
const handleQuantityChange = (value) => {
  if (value > (product.value.stock || 999)) {
    ElMessage.warning('库存不足')
    quantity.value = product.value.stock || 1
  }
}

const increaseQuantity = () => {
  if (quantity.value < (product.value.stock || 999)) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
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

    const response = await addCart(cartData)

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
const handleBuyNow = async () => {
  if (!product.value.id) {
    ElMessage.warning('商品信息不完整')
    return
  }

  // 获取用户地址列表
  try {
    const addressRes = await getMyAddressList()
    const addressData = addressRes.data || addressRes.rows || []
    if (addressRes.code === 200 && addressData.length > 0) {
      addressList.value = addressData
      // 选择默认地址或第一个地址
      const defaultAddr = addressData.find(a => a.isDefault === 1 || a.defaultFlag === 1) || addressData[0]
      selectedAddressId.value = defaultAddr.id
      
      // 创建订单 - 使用主键id
      const orderRes = await createDirectOrder({
        productId: product.value.id,
        quantity: quantity.value,
        addressId: defaultAddr.id
      })
      
      if (orderRes.code === 200 && orderRes.data) {
        // 设置购买弹窗数据
        purchaseProducts.value = [{
          name: product.value.name,
          mainImage: product.value.mainImage,
          price: product.value.price,
          quantity: quantity.value
        }]
        purchaseOrder.value = orderRes.data
        purchaseDialogVisible.value = true
      } else {
        ElMessage.error(orderRes.msg || '创建订单失败')
      }
    } else {
      ElMessage.warning('请先添加收货地址')
      router.push('/user/profile')
    }
  } catch (error) {
    console.error('购买失败:', error)
    ElMessage.error(error.message || '购买失败，请稍后重试')
  }
}

// 稍后支付
const handlePayLater = (orderData) => {
  ElMessage.success('订单已创建，请尽快完成支付')
  purchaseDialogVisible.value = false
  router.push('/user/orders')
}

// 立即支付
const handlePayNow = async (orderData) => {
  try {
    const payRes = await payOrder(orderData.orderNo, orderData.paymentType)
    if (payRes.code === 200) {
      ElMessage.success('支付成功')
      purchaseDialogVisible.value = false
      router.push('/user/orders')
    } else {
      ElMessage.error(payRes.msg || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.message || '支付失败，请稍后重试')
  }
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

// 评分分布相关
const getScorePercentage = (score) => {
  if (commentStats.value.total === 0) return 0

  // 确保 scoreDistribution 存在
  const distribution = commentStats.value.scoreDistribution || { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  const count = distribution[score] || 0
  return commentStats.value.total > 0 ? (count / commentStats.value.total) * 100 : 0
}

const getScoreCount = (score) => {
  const distribution = commentStats.value.scoreDistribution || { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  return distribution[score] || 0
}

const formatTime = (timeString) => {
  if (!timeString) return ''
  try {
    return new Date(timeString).toLocaleDateString('zh-CN')
  } catch (error) {
    return timeString
  }
}

onMounted(() => {
  loadProductData()
})
</script>

<template>
  <div class="product-detail">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-nav">
      <div class="container">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="product.categoryName">
            {{ product.categoryName }}
          </el-breadcrumb-item>
          <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 主商品区域 -->
    <div class="product-main">
      <div class="container">
        <div class="product-layout">
          <!-- 左侧区域：商品图片和详情/评价 -->
          <div class="left-column">
            <!-- 商品图片区域 -->
            <div class="product-gallery">
              <!-- 主图区域 -->
              <div class="main-image-area">
                <div class="main-image-container"
                  @mouseenter="handleImageMouseEnter(getProductImageUrl(allImages[currentImageIndex]))"
                  @mousemove="handleImageMouseMove" @mouseleave="handleImageMouseLeave">
                  <img :src="getProductImageUrl(allImages[currentImageIndex])" :alt="product.name" class="main-image"
                    @error="handleImageError" />
                  <div v-if="product.stock === 0" class="out-of-stock">
                    已售罄
                  </div>
                </div>

                <!-- 放大镜预览区域 -->
                <div v-if="zoomVisible" class="zoom-preview">
                  <div class="zoom-image" :style="{
                    backgroundImage: `url(${getProductImageUrl(zoomImageUrl)})`,
                    backgroundPosition: `${zoomPosition.x}% ${zoomPosition.y}%`
                  }"></div>
                </div>
              </div>

              <!-- 缩略图列表 -->
              <div class="thumbnail-list">
                <div v-for="(img, index) in allImages" :key="index"
                  :class="['thumbnail-item', { active: currentImageIndex === index }]" @click="changeMainImage(index)">
                  <img :src="getProductImageUrl(img)" :alt="`${product.name}-${index + 1}`" @error="handleImageError" />
                </div>
              </div>

              <!-- 店铺信息卡片 -->
              <div class="shop-card">
                <div class="shop-header">
                  <div class="shop-logo">
                    <img :src="getProductImageUrl(shopInfo.shopLogo) || '/src/assets/images/default-shop.png'"
                      :alt="shopInfo.shopName" @error="handleImageError" />
                  </div>
                  <div class="shop-info">
                    <div class="shop-name">{{ shopInfo.shopName }}</div>
                    <div class="shop-rating">
                      <span class="rating-score">{{ shopInfo.shopScore.toFixed(1) }}</span>
                      <div class="stars">
                        <span v-for="n in 5" :key="n"
                          :class="['star', { active: n <= Math.floor(shopInfo.shopScore) }]">★</span>
                      </div>
                      <span class="rating-text">店铺评分</span>
                    </div>
                  </div>
                  <div class="shop-actions">
                    <el-button size="small" class="enter-shop" @click="goToShopPage">
                      进店逛逛
                    </el-button>
                    <el-button size="small" class="contact-service" @click="contactCustomerService">
                      联系客服
                    </el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 商品详情和评价区域 -->
            <div class="product-tabs">
              <div class="tabs-header">
                <div :class="['tab-item', { active: activeTab === 'detail' }]" @click="activeTab = 'detail'">
                  图文详情
                </div>
                <div :class="['tab-item', { active: activeTab === 'comments' }]" @click="activeTab = 'comments'">
                  商品评价
                  <span class="comment-count" v-if="commentStats.total > 0">({{ commentStats.total }})</span>
                </div>
              </div>

              <div class="tabs-content">
                <!-- 图文详情 -->
                <div v-show="activeTab === 'detail'" class="tab-pane detail-pane">
                  <div class="detail-images">
                    <div v-for="(img, index) in allImages" :key="index" class="detail-image-item">
                      <img :src="getProductImageUrl(img)" :alt="`${product.name}-图${index + 1}`"
                        @error="handleImageError" />
                    </div>
                  </div>
                </div>

                <!-- 商品评价 -->
                <div v-show="activeTab === 'comments'" class="tab-pane comments-pane">
                  <!-- 评价概览 -->
                  <div class="comments-overview">
                    <div class="overview-score">
                      <div class="score-main">{{ commentStats.averageScore.toFixed(1) }}</div>
                      <div class="score-text">综合评分</div>
                      <div class="score-total">{{ commentStats.total }}人评价</div>
                    </div>

                    <div class="overview-distribution">
                      <div v-for="score in 5" :key="6 - score" class="distribution-item">
                        <span class="score-star">{{ 6 - score }}星</span>
                        <div class="progress-bar">
                          <div class="progress-fill" :style="{ width: `${getScorePercentage(6 - score)}%` }"></div>
                        </div>
                        <span class="score-count">{{ getScoreCount(6 - score) }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- 评价列表 -->
                  <div class="comments-list">
                    <div v-if="commentLoading" class="loading-comments">
                      加载中...
                    </div>
                    <div v-else-if="productComments.length > 0">
                      <!-- 递归渲染评论树 -->
                      <template v-for="comment in productComments" :key="comment.commentId">
                        <div class="comment-item">
                          <!-- 评论内容 -->
                          <div class="comment-header">
                            <img :src="getProductImageUrl(comment.avatar)" :alt="comment.nickName" class="user-avatar"
                              @error="handleImageError" />
                            <div class="user-info">
                              <div class="user-name">{{ comment.isAnonymous ? '匿名用户' : comment.nickName }}</div>
                              <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
                            </div>
                          </div>

                          <div class="comment-rating">
                            <div class="stars">
                              <span v-for="n in 5" :key="n" :class="['star', { active: n <= comment.score }]">★</span>
                            </div>
                          </div>

                          <div class="comment-content">
                            {{ comment.content }}
                          </div>

                          <div v-if="comment.images && comment.images.length > 0" class="comment-images">
                            <div v-for="(img, imgIndex) in comment.images" :key="imgIndex" class="comment-image">
                              <img :src="img" :alt="`评价图片${imgIndex + 1}`" @error="handleImageError" />
                            </div>
                          </div>

                          <!-- 渲染回复 -->
                          <div v-if="comment.children && comment.children.length > 0" class="comment-replies">
                            <div v-for="reply in comment.children" :key="reply.commentId" class="reply-item">
                              <div class="reply-header">
                                <img :src="getProductImageUrl(reply.avatar)" :alt="reply.nickName" class="reply-avatar"
                                  @error="handleImageError" />
                                <div class="reply-user-info">
                                  <div class="reply-user-name">
                                    <span v-if="reply.userId === shopInfo.id" class="shop-tag">商家</span>
                                    {{ reply.nickName }}
                                  </div>
                                  <div class="reply-time">{{ formatTime(reply.createTime) }}</div>
                                </div>
                              </div>
                              <div class="reply-content">
                                {{ reply.content }}
                              </div>
                              <div v-if="reply.images && reply.images.length > 0" class="reply-images">
                                <div v-for="(img, imgIndex) in reply.images" :key="imgIndex" class="reply-image">
                                  <img :src="img" :alt="`回复图片${imgIndex + 1}`" @error="handleImageError" />
                                </div>
                              </div>

                              <!-- 如果有更深层的回复（二级回复） -->
                              <div v-if="reply.children && reply.children.length > 0" class="reply-replies">
                                <div v-for="subReply in reply.children" :key="subReply.commentId"
                                  class="sub-reply-item">
                                  <div class="sub-reply-header">
                                    <img :src="subReply.avatar" :alt="subReply.nickName" class="sub-reply-avatar"
                                      @error="handleImageError" />
                                    <div class="sub-reply-user-info">
                                      <div class="sub-reply-user-name">
                                        <span v-if="subReply.userId === shopInfo.id" class="shop-tag">商家</span>
                                        {{ subReply.nickName }}
                                      </div>
                                      <div class="sub-reply-time">{{ formatTime(subReply.createTime) }}</div>
                                    </div>
                                  </div>
                                  <div class="sub-reply-content">
                                    {{ subReply.content }}
                                  </div>
                                  <div v-if="subReply.images && subReply.images.length > 0" class="sub-reply-images">
                                    <div v-for="(img, imgIndex) in subReply.images" :key="imgIndex"
                                      class="sub-reply-image">
                                      <img :src="img" :alt="`二级回复图片${imgIndex + 1}`" @error="handleImageError" />
                                    </div>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>

                          <div class="comment-footer">
                            <span class="like-count">👍 {{ comment.likeCount }}</span>
                            <span class="reply-count" v-if="comment.replyCount > 0">💬 {{ comment.replyCount }}</span>
                          </div>
                        </div>
                      </template>
                    </div>

                    <div v-else class="no-comments">
                      <div class="empty-state">
                        <el-icon>
                          <ChatDotRound />
                        </el-icon>
                        <p>暂无评价</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧区域：商品信息（固定位置） -->
          <div class="right-column">
            <div class="product-info-fixed">
              <div class="info-card">
                <!-- 商品标题 -->
                <h1 class="product-title">{{ product.name }}</h1>

                <!-- 商品副标题 -->
                <div class="product-subtitle">{{ product.subTitle }}</div>

                <!-- 价格区域 -->
                <div class="price-section">
                  <div class="price-row">
                    <span class="price-label">价格</span>
                    <div class="price-main">
                      <span class="price-currency">¥</span>
                      <span class="price-value">{{ formatPrice(product.price) }}</span>
                    </div>
                  </div>
                  <div class="price-extra">
                    <span class="sales">月销量 {{ shopInfo.salesCount || 0 }} 件</span>
                    <span class="stock">库存 {{ product.stock || 0 }} 件</span>
                  </div>
                </div>

                <!-- 服务承诺 -->
                <div class="service-promise">
                  <div class="service-item">
                    <el-icon>
                      <Timer />
                    </el-icon>
                    <span>现货，付款后48小时内发货</span>
                  </div>
                  <div class="service-item">
                    <el-icon>
                      <CircleCheck />
                    </el-icon>
                    <span>快递：免运费</span>
                  </div>
                  <div class="service-item">
                    <el-icon>
                      <Service />
                    </el-icon>
                    <span>7天无理由退货</span>
                  </div>
                </div>

                <!-- 规格选择 -->
                <div v-for="(spec, specIndex) in productSpecs" :key="spec.name" class="spec-section">
                  <div class="spec-label">{{ spec.name }}</div>
                  <div class="spec-options">
                    <div v-for="(option, optionIndex) in spec.options" :key="option"
                      :class="['spec-option', { selected: selectedSpecs[specIndex] === option }]"
                      @click="selectSpec(specIndex, optionIndex)">
                      {{ option }}
                    </div>
                  </div>
                </div>

                <!-- 数量选择 -->
                <div class="quantity-section">
                  <div class="quantity-label">数量</div>
                  <div class="quantity-control">
                    <button class="quantity-btn minus" @click="decreaseQuantity" :disabled="quantity <= 1">
                      <el-icon>
                        <Minus />
                      </el-icon>
                    </button>
                    <input v-model.number="quantity" type="number" min="1" :max="product.stock || 999"
                      class="quantity-input" @change="handleQuantityChange" />
                    <button class="quantity-btn plus" @click="increaseQuantity"
                      :disabled="quantity >= (product.stock || 999)">
                      <el-icon>
                        <Plus />
                      </el-icon>
                    </button>
                  </div>
                  <div class="stock-info">库存 {{ product.stock || 0 }} 件</div>
                </div>

                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <button class="btn add-cart" :class="{ loading: addingToCart }" @click="handleAddToCart"
                    :disabled="product.stock === 0">
                    <el-icon>
                      <ShoppingCartFull />
                    </el-icon>
                    <span v-if="!addingToCart">加入购物车</span>
                    <span v-else>添加中...</span>
                  </button>
                  <button class="btn buy-now" @click="handleBuyNow" :disabled="product.stock === 0">
                    立即购买
                  </button>
                  <button class="btn favorite" @click="handleToggleFavorite">
                    <el-icon :class="{ 'favorited': isFavorite }">
                      <StarFilled v-if="isFavorite" />
                      <Star v-else />
                    </el-icon>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 购买弹窗 -->
  <PurchaseDialog
    v-model="purchaseDialogVisible"
    :products="purchaseProducts"
    :order="purchaseOrder"
    :need-create-order="false"
    @pay-later="handlePayLater"
    @pay-now="handlePayNow"
    @cancel="purchaseDialogVisible = false"
  />
</template>

<style scoped>
/* 重置样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.product-detail {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
  font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 面包屑导航 */
.breadcrumb-nav {
  height: 40px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
}

.breadcrumb-nav .el-breadcrumb {
  font-size: 12px;
  color: #666;
}

.breadcrumb-nav :deep(.el-breadcrumb__inner) {
  color: #666;
}

.breadcrumb-nav :deep(.el-breadcrumb__inner:hover) {
  color: #ff5000;
}

/* 主商品区域 */
.product-main {
  padding: 20px 0;
  background: #fff;
}

.product-layout {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  position: relative;
}

/* 左侧列 */
.left-column {
  flex: 1;
  min-width: 0;
  margin-right: 400px;
  /* 为右侧固定列预留空间 */
}

/* 右侧列 - 固定位置 */
.right-column {
  position: fixed;
  top: 80px;
  /* 面包屑导航高度 + 间距 */
  right: calc(50% - 600px + 20px);
  /* 居中容器的一半宽度减去自身宽度的一半，再加padding */
  width: 380px;
  height: calc(100vh - 100px);
  /* 视口高度减去顶部空间 */
  overflow-y: auto;
  /* 如果内容超过高度，允许滚动 */
  z-index: 100;
}

.product-info-fixed {
  position: sticky;
  top: 0;
}

/* 商品图片区域 */
.product-gallery {
  background: #fff;
  padding: 20px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  margin-bottom: 20px;
}

/* 主图区域 */
.main-image-area {
  position: relative;
  margin-bottom: 20px;
}

.main-image-container {
  width: 100%;
  height: 500px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  background: #f8f8f8;
  cursor: crosshair;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.out-of-stock {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
}

/* 放大镜预览区域 */
.zoom-preview {
  position: absolute;
  left: 100%;
  top: 0;
  margin-left: 20px;
  width: 400px;
  height: 400px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  background: #fff;
}

.zoom-image {
  width: 100%;
  height: 100%;
  background-size: 200%;
  background-repeat: no-repeat;
}

/* 缩略图列表 */
.thumbnail-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 10px 0;
  margin-bottom: 20px;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s ease;
  background: #f8f8f8;
}

.thumbnail-item:hover,
.thumbnail-item.active {
  border-color: #ff5000;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 店铺卡片 */
.shop-card {
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  padding: 15px;
  background: #fff;
}

.shop-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.shop-logo {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid #e5e5e5;
  flex-shrink: 0;
}

.shop-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-info {
  flex: 1;
  min-width: 0;
}

.shop-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.shop-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-score {
  color: #ff5000;
  font-weight: bold;
  font-size: 16px;
}

.stars {
  display: flex;
}

.star {
  color: #ccc;
  font-size: 14px;
}

.star.active {
  color: #ff9900;
}

.rating-text {
  font-size: 12px;
  color: #999;
}

.shop-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.enter-shop {
  background: #f5f5f5;
  border: 1px solid #e5e5e5;
  color: #333;
}

.contact-service {
  background: #ff5000;
  border-color: #ff5000;
  color: #fff;
}

/* 商品详情和评价区域 */
.product-tabs {
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  overflow: hidden;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid #e5e5e5;
  background: #f8f9fa;
}

.tab-item {
  padding: 15px 30px;
  font-size: 16px;
  color: #333;
  cursor: pointer;
  position: relative;
  user-select: none;
  flex: 1;
  text-align: center;
}

.tab-item:hover {
  color: #ff5000;
}

.tab-item.active {
  color: #ff5000;
  font-weight: 600;
  background: #fff;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 3px;
  background: #ff5000;
}

.comment-count {
  color: #999;
  font-size: 14px;
  font-weight: normal;
  margin-left: 4px;
}

.tabs-content {
  padding: 20px;
}

/* 图文详情 */
.detail-images {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-image-item img {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
}

/* 商品评价 */
.comments-overview {
  display: flex;
  gap: 40px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 30px;
}

.overview-score {
  text-align: center;
  padding: 20px;
  min-width: 200px;
}

.score-main {
  font-size: 36px;
  color: #ff5000;
  font-weight: bold;
  margin-bottom: 10px;
}

.score-text {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.score-total {
  font-size: 12px;
  color: #999;
}

.overview-distribution {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-star {
  width: 40px;
  font-size: 14px;
  color: #666;
}

.progress-bar {
  flex: 1;
  height: 12px;
  background: #e5e5e5;
  border-radius: 6px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffd700, #ff9900);
  border-radius: 6px;
}

.score-count {
  width: 40px;
  font-size: 14px;
  color: #666;
  text-align: right;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.loading-comments {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

.comment-item {
  padding: 20px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  background: #fff;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-rating {
  margin-bottom: 12px;
}

.comment-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 12px;
}

.comment-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.comment-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e5e5e5;
  cursor: pointer;
}

.comment-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 在 style 部分添加以下样式 */
.comment-replies {
  margin-top: 15px;
  margin-left: 20px;
  padding-left: 15px;
  border-left: 2px solid #f0f0f0;
}

.reply-item {
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.reply-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.reply-user-info {
  flex: 1;
}

.reply-user-name {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.reply-time {
  font-size: 11px;
  color: #999;
}

.reply-content {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  margin-bottom: 8px;
}

.reply-images {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.reply-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e5e5e5;
  cursor: pointer;
}

.reply-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 二级回复样式 */
.reply-replies {
  margin-top: 8px;
  margin-left: 10px;
  padding-left: 10px;
  border-left: 1px solid #e8e8e8;
}

.sub-reply-item {
  padding: 8px 0;
}

.sub-reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.sub-reply-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
}

.sub-reply-user-info {
  flex: 1;
}

.sub-reply-user-name {
  font-size: 11px;
  color: #888;
  font-weight: 500;
}

.sub-reply-time {
  font-size: 10px;
  color: #aaa;
}

.sub-reply-content {
  font-size: 12px;
  color: #888;
  line-height: 1.3;
  margin-bottom: 6px;
}

.sub-reply-images {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.sub-reply-image {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e5e5e5;
  cursor: pointer;
}

.sub-reply-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-tag {
  display: inline-block;
  background: linear-gradient(135deg, #ff5000 0%, #ff9000 100%);
  color: #fff;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
  margin-right: 6px;
  vertical-align: middle;
}

/* 商家回复特殊样式 */
.reply-item[data-shop="true"],
.sub-reply-item[data-shop="true"] {
  background: #f8f9fa;
  border-left: 3px solid #ff5000;
  margin-left: -15px;
  padding-left: 12px;
}

.comment-footer {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #999;
  margin-top: 10px;
}

.no-comments .empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.no-comments .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #ccc;
}

/* 右侧固定卡片 */
.info-card {
  padding: 20px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.product-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  line-height: 1.4;
}

.product-subtitle {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
  line-height: 1.5;
}

/* 价格区域 */
.price-section {
  padding: 15px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffecec 100%);
  border-radius: 4px;
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  align-items: flex-end;
  margin-bottom: 8px;
}

.price-label {
  font-size: 14px;
  color: #666;
  margin-right: 10px;
}

.price-main {
  display: flex;
  align-items: baseline;
}

.price-currency {
  font-size: 18px;
  color: #ff5000;
  font-weight: 600;
}

.price-value {
  font-size: 30px;
  color: #ff5000;
  font-weight: bold;
  line-height: 1;
}

.price-extra {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #666;
}

/* 服务承诺 */
.service-promise {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 20px;
  border-left: 3px solid #ff5000;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
}

.service-item .el-icon {
  color: #52c41a;
  font-size: 14px;
}

/* 规格选择 */
.spec-section {
  margin-bottom: 20px;
}

.spec-label {
  font-size: 14px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 500;
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-option {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  color: #333;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
  user-select: none;
}

.spec-option:hover {
  border-color: #ff5000;
  color: #ff5000;
}

.spec-option.selected {
  background: #ff5000;
  border-color: #ff5000;
  color: #fff;
}

/* 数量选择 */
.quantity-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e5e5;
}

.quantity-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 1px;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  background: #f5f5f5;
  border: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 12px;
}

.quantity-btn:hover {
  background: #e8e8e8;
}

.quantity-btn:disabled {
  background: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.quantity-btn.minus {
  border-radius: 4px 0 0 4px;
}

.quantity-btn.plus {
  border-radius: 0 4px 4px 0;
}

.quantity-input {
  width: 50px;
  height: 32px;
  border: 1px solid #dcdfe6;
  border-left: none;
  border-right: none;
  text-align: center;
  font-size: 14px;
  color: #333;
}

.quantity-input:focus {
  outline: none;
  border-color: #ff5000;
}

.stock-info {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 10px;
}

.btn {
  flex: 1;
  height: 44px;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: none;
}

.btn.add-cart {
  background: linear-gradient(135deg, #ff9000 0%, #ff5000 100%);
  color: #fff;
}

.btn.add-cart:hover {
  opacity: 0.9;
}

.btn.add-cart:disabled,
.btn.buy-now:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn.buy-now {
  background: linear-gradient(135deg, #ff3c00 0%, #e60000 100%);
  color: #fff;
}

.btn.buy-now:hover {
  opacity: 0.9;
}

.btn.favorite {
  width: 44px;
  background: #f5f5f5;
  color: #666;
  border: 1px solid #dcdfe6;
}

.btn.favorite:hover {
  background: #e8e8e8;
}

.btn.favorite .el-icon.favorited {
  color: #ff5000;
}

.btn.loading {
  opacity: 0.7;
  cursor: wait;
}

/* 响应式设计 */
@media (max-width: 1199px) {
  .container {
    max-width: 100%;
  }

  .left-column {
    margin-right: 350px;
  }

  .right-column {
    width: 350px;
    right: 20px;
  }
}

@media (max-width: 991px) {
  .product-layout {
    flex-direction: column;
  }

  .left-column {
    width: 100%;
    margin-right: 0;
  }

  .right-column {
    position: static;
    width: 100%;
    height: auto;
    overflow-y: visible;
  }

  .product-info-fixed {
    position: static;
  }

  .zoom-preview {
    display: none;
  }

  .main-image-container {
    height: 400px;
  }
}

@media (max-width: 767px) {
  .container {
    padding: 0 15px;
  }

  .main-image-container {
    height: 320px;
  }

  .thumbnail-item {
    width: 70px;
    height: 70px;
  }

  .product-title {
    font-size: 18px;
  }

  .price-value {
    font-size: 26px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .btn.favorite {
    width: 100%;
  }

  .comments-overview {
    flex-direction: column;
    gap: 20px;
  }

  .overview-score {
    min-width: auto;
  }

  .tabs-header {
    flex-direction: column;
  }

  .tab-item {
    padding: 12px 20px;
    text-align: center;
  }
}

@media (max-width: 479px) {
  .container {
    padding: 0 10px;
  }

  .main-image-container {
    height: 260px;
  }

  .thumbnail-item {
    width: 60px;
    height: 60px;
  }

  .spec-options {
    gap: 8px;
  }

  .spec-option {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>