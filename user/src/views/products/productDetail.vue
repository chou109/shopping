<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, CircleCheck, ShoppingCart, ShoppingCartFull } from '@element-plus/icons-vue'
import { getProduct } from '@/api/system/product'
import { addCart } from '@/api/system/cart'
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
  categoryName: ''
})

// 商品详情数据
const productDetail = ref({
  detail: '',
  param: {}
})

// 店铺信息 - 预留后端接口
const shopInfo = ref({
  id: 0,
  name: '官方旗舰店',
  avatar: '',
  rating: 4.8
})

// 商品图片相关
const currentImageIndex = ref(0)
const quantity = ref(1)
const addingToCart = ref(false)
const activeTab = ref('detail')

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
      
      // 如果有商品详情数据
      if (response.data.productDetail) {
        productDetail.value = response.data.productDetail
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
  ElMessageBox.alert('客服功能正在开发中，敬请期待！', '提示', {
    confirmButtonText: '确定'
  })
}

onMounted(() => {
  loadProductData()
})
</script>

<template>
  <div class="product-detail-container">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/products' }">商品列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
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
        <div class="shop-info">
          <!-- 店铺头像 - 预留后端接口 -->
          <div class="shop-avatar">
            <el-avatar :size="50" :src="shopInfo.avatar" @error="handleImageError">
              <span>店</span>
            </el-avatar>
          </div>
          
          <div class="shop-details">
            <!-- 店铺名 - 预留后端接口 -->
            <div class="shop-name">{{ shopInfo.name }}</div>
            
            <!-- 店铺星级 - 预留后端接口 -->
            <div class="shop-rating">
              <span class="rating-stars">
                <el-rate
                  v-model="shopInfo.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}"
                />
              </span>
              <span class="rating-score">{{ shopInfo.rating }}分</span>
            </div>
            
            <!-- 客服入口 - 预留后端接口 -->
            <div class="customer-service">
              <el-button type="primary" size="small" @click="contactCustomerService">
                <el-icon><ChatDotRound /></el-icon>
                联系客服
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
          </div>
          
          <!-- 商品分类信息 -->
          <div class="product-category-info" v-if="product.categoryName">
            分类：{{ product.categoryName }}
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

    <!-- 商品详情Tabs -->
    <div class="detail-tabs">
      <el-tabs v-model="activeTab" class="product-tabs">
        <el-tab-pane label="商品详情" name="detail">
          <div class="product-description">
            <div v-if="productDetail.detail" v-html="productDetail.detail"></div>
            <div v-else class="no-detail">
              暂无商品详情
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="规格参数" name="specs">
          <div class="product-specifications">
            <div v-if="productDetail.param && Object.keys(productDetail.param).length > 0">
              <div v-for="(value, key) in productDetail.param" :key="key" class="spec-item">
                <span class="spec-name">{{ key }}:</span>
                <span class="spec-value">{{ value }}</span>
              </div>
            </div>
            <div v-else class="no-specs">
              暂无规格参数
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
  gap: 10px;
  margin-bottom: 10px;
}

.rating-stars {
  display: inline-flex;
  align-items: center;
}

.rating-score {
  color: #ff9900;
  font-size: 14px;
}

.customer-service {
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

.product-category-info {
  font-size: 14px;
  color: #666;
  margin-top: 10px;
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

.product-description {
  padding: 20px;
  min-height: 300px;
}

.no-detail {
  text-align: center;
  color: #999;
  padding: 50px;
}

.product-specifications {
  padding: 20px;
}

.spec-item {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.spec-item:last-child {
  border-bottom: none;
}

.spec-name {
  width: 150px;
  font-weight: bold;
  color: #333;
}

.spec-value {
  flex: 1;
  color: #666;
}

.no-specs {
  text-align: center;
  color: #999;
  padding: 50px;
}

/* 响应式调整 */
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
}

@media (max-width: 480px) {
  .main-image-wrapper {
    height: 250px;
  }
  
  .thumbnail-item {
    width: 50px;
    height: 50px;
  }
  
  .product-title {
    font-size: 18px;
  }
  
  .price-value {
    font-size: 20px;
  }
}
</style>