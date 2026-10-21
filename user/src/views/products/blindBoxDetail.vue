<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CircleCheck,
  ShoppingCart,
  ShoppingCartFull,
  Timer,
  Plus,
  Minus
} from '@element-plus/icons-vue'
import { addCart } from '@/api/system/cart'
import { createBlindBoxOrder, payOrder, checkBlindBoxPurchase } from '@/api/system/orderInfo'
import { getMyAddressList } from '@/api/system/address'
import PurchaseDialog from '@/components/PurchaseDialog/index.vue'

// 导入盲盒图片
import BlindBoxImage1 from '@/assets/BlindBox/BlindBox.png'
import BlindBoxImage2 from '@/assets/BlindBox/BlindBox (2).png'
import BlindBoxImage3 from '@/assets/BlindBox/BlindBox (3).png'
import BlindBoxImage4 from '@/assets/BlindBox/BlindBox (4).png'
import BlindBoxImage5 from '@/assets/BlindBox/BlindBox (5).png'
import BlindBoxImage6 from '@/assets/BlindBox/BlindBox (6).png'
import BlindBoxImage7 from '@/assets/BlindBox/BlindBox (7).png'
import BlindBoxImage8 from '@/assets/BlindBox/BlindBox (8).png'
import BlindBoxImage9 from '@/assets/BlindBox/BlindBox (9).png'
import BlindBoxImage10 from '@/assets/BlindBox/BlindBox (10).png'
import BlindBoxImage11 from '@/assets/BlindBox/BlindBox (11).png'
import BlindBoxImage12 from '@/assets/BlindBox/BlindBox (12).png'

const router = useRouter()

// 直接创建图片URL数组
const imageUrls = [
  BlindBoxImage1, BlindBoxImage2, BlindBoxImage3,
  BlindBoxImage4, BlindBoxImage5, BlindBoxImage6,
  BlindBoxImage7, BlindBoxImage8, BlindBoxImage9,
  BlindBoxImage10, BlindBoxImage11, BlindBoxImage12
].map(img => {
  return img && typeof img === 'object' && 'default' in img ? img.default : img
})

// 商品数据 - 固定盲盒商品信息
const product = ref({
  id: 9999,
  name: "随机商品盲盒 好货低价易购 官方正品 省钱省心",
  price: 500,
  stock: 9999,
  mainImage: imageUrls[0],
  subImages: imageUrls,
  categoryId: 0,
  categoryName: "盲盒商品",
  shopId: null,
  category: {},
  isBlindBox: true
})

// 商品图片相关
const currentImageIndex = ref(0)
const quantity = ref(1)
const addingToCart = ref(false)
const activeTab = ref('detail')

// 购买弹窗相关
const purchaseDialogVisible = ref(false)
const purchaseProducts = ref([])
const purchaseOrder = ref({})

// 抽奖弹窗相关
const lotteryDialogVisible = ref(false)
const lotteryResult = ref(null)
const isDrawing = ref(false)
const drawAnimationClass = ref('')

// 放大镜相关
const zoomVisible = ref(false)
const zoomImageUrl = ref(imageUrls[0])
const mainImageRef = ref(null)

// 获取当前图片URL
const getCurrentImage = () => {
  return imageUrls[currentImageIndex.value] || imageUrls[0]
}

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 放大镜功能 - 修复版本
const handleImageMouseEnter = () => {
  const currentImage = getCurrentImage()
  if (currentImage) {
    zoomImageUrl.value = currentImage
    zoomVisible.value = true
  }
}

const handleImageMouseMove = (event) => {
  if (!zoomVisible.value || !mainImageRef.value) return
  
  const container = event.currentTarget
  const rect = container.getBoundingClientRect()
  
  // 计算鼠标在图片上的位置百分比
  const mouseX = event.clientX - rect.left
  const mouseY = event.clientY - rect.top
  
  const width = rect.width
  const height = rect.height
  
  // 确保鼠标位置在图片范围内
  if (mouseX < 0 || mouseX > width || mouseY < 0 || mouseY > height) return
  
  // 计算放大镜中显示的区域
  const xPercent = (mouseX / width) * 100
  const yPercent = (mouseY / height) * 100
  
  // 更新放大镜图片的transform
  const zoomImage = document.querySelector('.zoom-image')
  if (zoomImage) {
    zoomImage.style.transformOrigin = `${xPercent}% ${yPercent}%`
    zoomImage.style.transform = 'scale(2)'
  }
}

const handleImageMouseLeave = () => {
  zoomVisible.value = false
  // 重置transform
  const zoomImage = document.querySelector('.zoom-image')
  if (zoomImage) {
    zoomImage.style.transform = 'scale(1)'
  }
}

// 切换主图
const changeMainImage = (index) => {
  currentImageIndex.value = index
  const newImage = getCurrentImage()
  if (newImage) {
    zoomImageUrl.value = newImage
    // 重置transform
    const zoomImage = document.querySelector('.zoom-image')
    if (zoomImage) {
      zoomImage.style.transform = 'scale(1)'
    }
  }
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

  // 验证库存
  if (quantity.value > (product.value.stock || 0)) {
    ElMessage.warning('库存不足')
    return
  }

  addingToCart.value = true
  try {
    const cartData = {
      productId: product.value.id,
      productName: product.value.name,
      productMainImage: imageUrls[0],
      quantity: quantity.value,
      productUnitPrice: product.value.price,
      selected: 1,
      productTotalPrice: product.value.price * quantity.value,
      specs: '',
      shopId: product.value.shopId,
      isBlindBox: true
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

  // 验证库存
  if (quantity.value > (product.value.stock || 0)) {
    ElMessage.warning('库存不足')
    return
  }

  try {
    // 先检查盲盒购买资格
    const checkRes = await checkBlindBoxPurchase()
    if (checkRes.code !== 200) {
      ElMessage.warning(checkRes.msg || '每个用户只能购买一次盲盒')
      return
    }
    
    // 获取用户地址列表
    const addressRes = await getMyAddressList()
    const addressData = addressRes.data || addressRes.rows || []

    if (addressRes.code !== 200 || addressData.length === 0) {
      ElMessage.warning('请先添加收货地址')
      router.push('/user/profile')
      return
    }

    // 选择默认地址或第一个地址
    const defaultAddr = addressData.find(a => a.isDefault === 1 || a.defaultFlag === 1) || addressData[0]
    
    // 确保地址有效
    if (!defaultAddr || !defaultAddr.id) {
      ElMessage.warning('请先选择收货地址')
      router.push('/user/profile')
      return
    }

    // 准备购买数据，但不生成订单
    purchaseOrder.value = {
      // 创建临时订单对象
      orderNo: 'PAY_' + Date.now(),
      totalAmount: product.value.price * quantity.value,
      orderStatus: 0, // 待支付状态
      isBlindBox: true,
      blindBoxProduct: {
        name: product.value.name,
        mainImage: imageUrls[0],
        price: product.value.price,
        quantity: quantity.value
      },
      addressId: defaultAddr.id // 保存地址ID用于后续订单生成
    }
    
    // 设置购买弹窗数据
    purchaseProducts.value = [{
      name: product.value.name,
      mainImage: imageUrls[0],
      price: product.value.price,
      quantity: quantity.value
    }]
    
    // 显示支付弹窗而不是直接显示抽奖弹窗
    purchaseDialogVisible.value = true
  } catch (error) {
    console.error('购买失败:', error)
    ElMessage.error('购买失败，请稍后重试')
  }
}

// 稍后支付
const handlePayLater = async (orderData) => {
  try {
    const response = await createBlindBoxOrder(orderData.addressId || purchaseOrder.value.addressId)
    if (response.code !== 200) throw new Error(response.msg || '创建盲盒订单失败')
    ElMessage.success('奖品已抽取并生成待支付订单')
    purchaseDialogVisible.value = false
    router.push('/user/orders')
  } catch (error) {
    ElMessage.error(error.message || '创建订单失败，请稍后重试')
  }
}

// 立即支付
const handlePayNow = async (orderData) => {
  try {
    const createResponse = await createBlindBoxOrder(orderData.addressId || purchaseOrder.value.addressId)
    if (createResponse.code !== 200) throw new Error(createResponse.msg || '创建盲盒订单失败')
    const order = createResponse.data
    const prize = order.orderDetails?.[0]
    if (!order.orderNo || !prize) throw new Error('服务端未返回有效的抽奖结果')
    purchaseOrder.value.orderNo = order.orderNo
    purchaseOrder.value.paymentType = orderData.paymentType
    lotteryResult.value = {
      name: prize.productName?.replace('【盲盒】', '') || '中奖商品',
      image: prize.productMainImage,
      productId: prize.productId
    }
    lotteryDialogVisible.value = true
    purchaseDialogVisible.value = false
    isDrawing.value = true
    drawAnimationClass.value = 'lottery-spinning'
    await new Promise(resolve => setTimeout(resolve, 1800))
    isDrawing.value = false
    drawAnimationClass.value = ''
    const payResponse = await payOrder(order.orderNo, orderData.paymentType)
    if (payResponse.code !== 200) {
      ElMessage.warning('奖品已锁定，订单仍待支付，可在订单页面继续支付')
      return
    }
    ElMessage.success(`已抽中${lotteryResult.value.name}，支付成功`)
  } catch (error) {
    if (purchaseOrder.value.orderNo) {
      ElMessage.warning('奖品订单已生成，请到订单页面完成支付')
    } else {
      ElMessage.error(error.message || '创建盲盒订单失败，请稍后重试')
    }
  }
}

// 开始抽奖
const startLottery = async () => {
  if (!purchaseOrder.value.orderNo) return
  lotteryDialogVisible.value = false
  router.push('/user/orders')
}

onMounted(() => {
  // 确保放大镜图片URL正确初始化
  zoomImageUrl.value = getCurrentImage()
  // 预热抽奖池
})
</script>

<template>
  <div class="product-detail">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-nav">
      <div class="container">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>

    <!-- 主商品区域 -->
    <div class="product-main">
      <div class="container">
        <div class="product-layout">
          <!-- 左侧区域：商品图片和详情 -->
          <div class="left-column">
            <!-- 商品图片区域 -->
            <div class="product-gallery">
              <!-- 主图区域 -->
              <div class="main-image-area">
                <div class="main-image-container"
                  @mouseenter="handleImageMouseEnter"
                  @mousemove="handleImageMouseMove" 
                  @mouseleave="handleImageMouseLeave">
                  <img 
                    :src="getCurrentImage()" 
                    :alt="product.name" 
                    class="main-image" 
                    ref="mainImageRef"
                  />
                  <div v-if="product.stock === 0" class="out-of-stock">
                    已售罄
                  </div>
                </div>

                <!-- 放大镜预览区域 -->
                <div v-show="zoomVisible" class="zoom-preview">
                  <div class="zoom-image-container">
                    <img 
                      :src="zoomImageUrl" 
                      :alt="product.name" 
                      class="zoom-image"
                      :style="{
                        width: '100%',
                        height: '100%',
                        objectFit: 'contain'
                      }"
                    />
                  </div>
                </div>
              </div>

              <!-- 缩略图列表 -->
              <div class="thumbnail-list">
                <div v-for="(img, index) in imageUrls" :key="index"
                  :class="['thumbnail-item', { active: currentImageIndex === index }]" 
                  @click="changeMainImage(index)">
                  <img :src="img" :alt="`${product.name}-${index + 1}`" />
                </div>
              </div>
            </div>

            <!-- 商品详情区域 -->
            <div class="product-tabs">
              <div class="tabs-header">
                <div :class="['tab-item', { active: activeTab === 'detail' }]" @click="activeTab = 'detail'">
                  图文详情
                </div>
              </div>

              <div class="tabs-content">
                <!-- 图文详情 -->
                <div v-show="activeTab === 'detail'" class="tab-pane detail-pane">
                  <div class="detail-images">
                    <div v-for="(img, index) in imageUrls" :key="index" class="detail-image-item">
                      <img :src="img" :alt="`${product.name}-图${index + 1}`" />
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
                      <CircleCheck />
                    </el-icon>
                    <span>特殊商品不可退货</span>
                  </div>
                </div>

                <!-- 数量选择（盲盒限购1件） -->
                <div class="quantity-section">
                  <div class="quantity-label">数量</div>
                  <div class="quantity-control">
                    <button class="quantity-btn minus" disabled>
                      <el-icon>
                        <Minus />
                      </el-icon>
                    </button>
                    <input v-model.number="quantity" type="number" min="1" max="1"
                      class="quantity-input" readonly />
                    <button class="quantity-btn plus" disabled>
                      <el-icon>
                        <Plus />
                      </el-icon>
                    </button>
                  </div>
                  <div class="stock-info">限购 1 件</div>
                </div>

                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <!-- 盲盒不支持加入购物车，只能直接购买 -->
                  <button class="btn buy-now full-width" @click="handleBuyNow" :disabled="product.stock === 0">
                    立即购买
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
  <PurchaseDialog v-model="purchaseDialogVisible" :products="purchaseProducts" :order="purchaseOrder"
    :need-create-order="false" @pay-later="handlePayLater" @pay-now="handlePayNow" />

  <!-- 抽奖弹窗 -->
  <el-dialog
    v-model="lotteryDialogVisible"
    title="盲盒抽奖"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="!isDrawing"
  >
    <div class="lottery-container">
      <div v-if="isDrawing" class="lottery-drawing">
        <img src="@/assets/images/BlindBox.png" alt="盲盒抽奖" class="blind-box-image" :class="drawAnimationClass" />
        <div class="drawing-text">正在抽奖...</div>
      </div>
      <div v-else-if="lotteryResult" class="lottery-result">
        <div class="result-title">恭喜你抽到了！</div>
        <div class="result-prize">({{ lotteryResult.name }})</div>
        <div class="result-info">奖品已锁定并写入订单</div>
      </div>
      <div v-else class="lottery-waiting">
        <img src="@/assets/images/BlindBox.png" alt="盲盒抽奖" class="blind-box-image" />
        <div class="waiting-text">准备抽奖</div>
        <div class="waiting-subtext">点击开始按钮抽取您的商品</div>
      </div>
    </div>

    <template #footer>
      <div class="lottery-footer">
        <el-button 
          v-if="!isDrawing && !lotteryResult"
          type="primary"
          @click="startLottery"
          :loading="isDrawing"
        >
          开始抽奖
        </el-button>
        <el-button 
          v-if="lotteryResult"
          type="primary"
          @click="startLottery"
        >
          查看订单
        </el-button>
      </div>
    </template>
  </el-dialog>
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
  padding: 16px 0;
  background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
  border-bottom: 1px solid #eee;
}

.breadcrumb-nav .container {
  display: flex;
  align-items: center;
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
  background: rgba(255, 80, 0, 0.08);
  color: #ff5000;
}

.breadcrumb-nav :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  background: rgba(255, 80, 0, 0.1);
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
  background: #ff5000;
  border-radius: 2px;
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
}

/* 右侧列 - 固定位置 */
.right-column {
  position: fixed;
  top: 80px;
  right: calc(50% - 600px + 20px);
  width: 380px;
  height: calc(100vh - 100px);
  overflow-y: auto;
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

.zoom-image-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;
}

.zoom-image {
  transition: transform 0.1s ease-out;
  transform-origin: center center;
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

.btn.buy-now.full-width {
  width: 100%;
}

.btn.buy-now:hover {
  opacity: 0.9;
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
}

/* 抽奖弹窗样式 */
.lottery-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 0;
  min-height: 300px;
}

/* 抽奖转盘样式 */
.lottery-drawing {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 抽奖结果样式 */
.lottery-result {
  text-align: center;
  padding: 30px;
}

.result-title {
  font-size: 24px;
  font-weight: bold;
  color: #ff4400;
  margin-bottom: 20px;
}

.result-prize {
  font-size: 32px;
  font-weight: bold;
  color: #ff4400;
  margin-bottom: 20px;
  padding: 10px 20px;
  background-color: #fff8f0;
  border-radius: 10px;
}

.result-info {
  font-size: 16px;
  color: #666;
}

/* 等待抽奖样式 */
.lottery-waiting {
  text-align: center;
  padding: 50px;
}

.waiting-text {
  font-size: 24px;
  font-weight: bold;
  color: #ff4400;
  margin-bottom: 10px;
}

.waiting-subtext {
  font-size: 16px;
  color: #666;
}

.blind-box-image {
  width: 200px;
  height: 200px;
  object-fit: contain;
  margin-bottom: 20px;
  transition: transform 3s ease-out;
}

.lottery-spinning {
  transform: rotate(1800deg); /* 旋转5圈 */
}

.lottery-footer {
  display: flex;
  justify-content: center;
}
</style>
