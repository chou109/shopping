<template>
  <div class="shopping-cart">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-actions">
        <el-button type="danger" plain :disabled="cartItems.length === 0" @click="handleClearCart">
          清空购物车
        </el-button>
      </div>
    </div>

    <!-- 购物车内容 -->
    <div class="cart-content" v-loading="loading">
      <div v-if="cartItems.length > 0" class="cart-container">
        <!-- 左侧商品列表 -->
        <div class="cart-list-section">
          <!-- 购物车工具栏 -->
          <div class="cart-toolbar">
            <div class="toolbar-left">
              <el-checkbox v-model="isAllSelected" @change="handleSelectAll" :indeterminate="isIndeterminate">
                全选
              </el-checkbox>
              <span class="selected-count">已选 {{ selectedCount }} 件商品</span>
            </div>
            <div class="toolbar-right">
              <el-button type="text" @click="handleBatchDelete" :disabled="selectedCount === 0">
                删除选中
              </el-button>
            </div>
          </div>

          <!-- 商品列表 -->
          <div class="cart-items">
            <div v-for="item in cartItems" :key="item.id" class="cart-item" :class="{ selected: item.selected }">
              <div class="item-select">
                <el-checkbox v-model="item.selected" @change="handleItemSelect(item)" />
              </div>

              <div class="item-content">
                <!-- 商品图片 -->
<div class="item-image" @click="goToProductDetail(item.productId)">
  <img :src="item.productId === 9999 || item.productName?.includes('盲盒') ? '/src/assets/images/BlindBox.png' : getProductImageUrl(item.productMainImage)" :alt="item.productName" @error="handleImageError" />
</div>

                <!-- 商品信息 -->
                <div class="item-info">
                  <h3 class="product-name" @click="goToProductDetail(item.productId)">
                    {{ item.productName }}
                  </h3>
                  <p class="product-subtitle">{{ item.productSubtitle }}</p>
                  <div class="product-spec" v-if="item.specifications">
                    {{ item.specifications }}
                  </div>
                </div>

                <!-- 价格和数量控制区域 -->
                <div class="item-controls">
                  <!-- 价格 -->
                  <div class="price-section">
                    <span class="current-price">¥{{ formatPrice(item.productUnitPrice) }}</span>
                    <span class="quantity">×{{ item.quantity }}</span>
                  </div>

                  <!-- 数量控制 -->
                  <div class="quantity-control">
                    <el-button size="small" :disabled="item.quantity <= 1" @click="decreaseQuantity(item)">
                      -
                    </el-button>
                    <el-input-number v-model="item.quantity" :min="1" :max="item.stock || 999" size="small"
                      controls-position="right" @change="updateQuantity(item)" />
                    <el-button size="small" :disabled="item.quantity >= (item.stock || 999)"
                      @click="increaseQuantity(item)">
                      +
                    </el-button>
                  </div>

                  <!-- 小计 -->
                  <div class="total-price">小计: ¥{{ formatPrice(item.productTotalPrice) }}</div>
                </div>

                <!-- 操作按钮 -->
                <div class="item-actions">
                  <el-button type="danger" text size="small" @click="handleDeleteItem(item.id)">
                    删除
                  </el-button>
                  <el-button type="primary" text size="small" @click="addToFavorites(item)">
                    移入收藏
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧结算面板 -->
        <div class="checkout-section">
          <div class="checkout-panel">
            <div class="panel-header">
              <h3>结算信息</h3>
            </div>

            <div class="price-details">
              <div class="price-row">
                <span class="label">商品总价</span>
                <span class="value">¥{{ formatPrice(selectedTotalPrice) }}</span>
              </div>
              <div class="price-row">
                <span class="label">运费</span>
                <span class="value free">免运费</span>
              </div>
              <div class="price-row" v-if="discount > 0">
                <span class="label">优惠</span>
                <span class="value discount">-¥{{ formatPrice(discount) }}</span>
              </div>
              <div class="price-row total">
                <span class="label">实付金额</span>
                <span class="value total-amount">¥{{ formatPrice(actualTotalPrice) }}</span>
              </div>
            </div>

            <div class="checkout-actions">
              <el-button type="danger" size="large" class="checkout-btn" :disabled="selectedCount === 0"
                @click="handleCheckout">
                去结算 ({{ selectedCount }}件)
              </el-button>

              <div class="security-tips">
                <el-icon>
                  <Lock />
                </el-icon>
                <span>安全支付保障</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空购物车状态 -->
      <div v-else class="empty-cart">
        <el-empty description="购物车还是空的">
          <el-button type="primary" @click="goShopping">去逛逛</el-button>
        </el-empty>
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

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { getProductImageUrl, handleImageError } from '@/utils/image'
import { listCart, updateCart, delCart, clearCart, batchSelectCart } from '@/api/system/cart'
import { createOrderFromCart, payOrder } from '@/api/system/orderInfo'
import { getMyAddressList } from '@/api/system/address'
import PurchaseDialog from '@/components/PurchaseDialog/index.vue'

const router = useRouter()
const store = useStore()
const loading = ref(false)

// 购物车数据
const cartItems = ref([])

// 购买弹窗相关
const purchaseDialogVisible = ref(false)
const purchaseProducts = ref([])
const purchaseOrder = ref({})

// 计算属性
const selectedItems = computed(() => {
  return cartItems.value.filter(item => item.selected)
})

const selectedCount = computed(() => {
  return selectedItems.value.length
})

const isAllSelected = computed({
  get: () => cartItems.value.length > 0 && cartItems.value.every(item => item.selected),
  set: (value) => {
    cartItems.value.forEach(item => {
      item.selected = value
    })
  }
})

const isIndeterminate = computed(() => {
  return selectedCount.value > 0 && selectedCount.value < cartItems.value.length
})

const selectedTotalPrice = computed(() => {
  return selectedItems.value.reduce((total, item) => {
    return total + (item.productUnitPrice * item.quantity)
  }, 0)
})

// 修改：所有商品都免运费
const shippingFee = computed(() => {
  return 0
})

const discount = computed(() => {
  let discount = 0
  // 简单的优惠计算逻辑
  if (selectedTotalPrice.value >= 200) {
    discount += 20
  }
  if (selectedTotalPrice.value >= 500) {
    discount += 50
  }
  return discount
})

const actualTotalPrice = computed(() => {
  return Math.max(0, selectedTotalPrice.value + shippingFee.value - discount.value)
})

const availablePromotions = computed(() => {
  const promotions = []

  if (selectedTotalPrice.value >= 200) {
    promotions.push({
      id: 1,
      type: '满减',
      description: '满200减20',
      discount: 20
    })
  }

  if (selectedTotalPrice.value >= 500) {
    promotions.push({
      id: 2,
      type: '满减',
      description: '满500减50',
      discount: 50
    })
  }

  return promotions
})

// 方法
const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 加载购物车数据
const loadCartData = async () => {
  loading.value = true
  try {
    const userId = store.state.user?.userId
    if (!userId) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    const response = await listCart()
    console.log("购物车数据:", response)
    if (response.code === 200) {
      cartItems.value = response.data || []
      // 调试购物车商品数据
      console.log("购物车商品列表:", cartItems.value)
      // 查看第一个商品的详细信息，特别是图片相关字段
      if (cartItems.value.length > 0) {
        const firstItem = cartItems.value[0]
        console.log("第一个商品详细信息:", firstItem)
        console.log("商品主图数据结构:", firstItem.productMainImage)
        console.log("商品主图类型:", typeof firstItem.productMainImage)
        console.log("处理后的图片URL:", getProductImageUrl(firstItem.productMainImage))
      }
      // 计算小计并转换selected为布尔值
      cartItems.value.forEach(item => {
        // 后端返回的selected是数字(0/1)，需要转换为布尔值
        item.selected = item.selected === 1 || item.selected === true
        // 计算小计
        if (item.productUnitPrice && item.quantity && !item.productTotalPrice) {
          item.productTotalPrice = item.productUnitPrice * item.quantity
        }
      })
    } else {
      ElMessage.error('获取购物车数据失败')
      cartItems.value = []
    }

  } catch (error) {
    console.error('加载购物车数据失败:', error)
    ElMessage.error('加载购物车数据失败')
    cartItems.value = []
  } finally {
    loading.value = false
  }
}

// 全选/取消全选
const handleSelectAll = async (selected) => {
  try {
    // 更新前端状态
    cartItems.value.forEach(item => {
      item.selected = selected
    })
    // 调用API更新后端状态
    await batchSelectCart(selected ? 1 : 0)
    ElMessage.success('选择状态已更新')
  } catch (error) {
    console.error('更新全选状态失败:', error)
    ElMessage.error('更新失败')
  }
}

// 单个商品选择
const handleItemSelect = async (item) => {
  try {
    // 调用API更新后端状态
    await updateCart(item.id, {
      selected: item.selected ? 1 : 0
    })
    console.log('商品选择状态变化:', item.productName, item.selected)
    ElMessage.success('选择状态已更新')
  } catch (error) {
    console.error('更新商品选择状态失败:', error)
    ElMessage.error('更新失败')
    // 恢复原状态
    item.selected = !item.selected
  }
}

// 增加数量
const increaseQuantity = async (item) => {
  if (item.quantity >= (item.stock || 999)) {
    ElMessage.warning('库存不足')
    return
  }

  item.quantity++
  await updateCartItem(item)
}

// 减少数量
const decreaseQuantity = async (item) => {
  if (item.quantity <= 1) {
    return
  }

  item.quantity--
  await updateCartItem(item)
}

// 更新数量
const updateQuantity = async (item) => {
  if (item.quantity < 1) {
    item.quantity = 1
  }

  if (item.quantity > (item.stock || 999)) {
    item.quantity = item.stock || 999
    ElMessage.warning('库存不足')
  }

  await updateCartItem(item)
}

// 更新购物车商品
const updateCartItem = async (item) => {
  try {
    // 重新计算小计
    item.productTotalPrice = item.productUnitPrice * item.quantity

    // 调用API更新数据库
    const response = await updateCart(item.id, {
      quantity: item.quantity,
      productTotalPrice: item.productTotalPrice
    })

    if (response.code === 200) {
      ElMessage.success('数量已更新')
    } else {
      ElMessage.error(response.msg || '更新失败')
    }
  } catch (error) {
    console.error('更新购物车失败:', error)
    ElMessage.error('更新失败')
  }
}

// 删除单个商品
const handleDeleteItem = async (itemId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await delCart(itemId)
    if (response.code === 200) {
      cartItems.value = cartItems.value.filter(item => item.id !== itemId)
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedCount.value} 件商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    cartItems.value = cartItems.value.filter(item => !item.selected)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 清空购物车
const handleClearCart = async () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车已经是空的')
    return
  }

  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await clearCart()
    if (response.code === 200) {
      cartItems.value = []
      ElMessage.success('购物车已清空')
    } else {
      ElMessage.error(response.msg || '清空失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空购物车失败:', error)
      ElMessage.error('清空失败')
    }
  }
}

// 去结算
const handleCheckout = async () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }

  // 检查是否有选中的商品
  const selected = selectedItems.value
  console.log('选中的商品:', selected, '数量:', selected.length)
  
  if (selected.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }

  // 获取用户地址列表
  try {
    const addressRes = await getMyAddressList()
    const addressData = addressRes.data || addressRes.rows || []
    if (addressRes.code === 200 && addressData.length > 0) {
      // 选择默认地址或第一个地址
      const defaultAddr = addressData.find(a => a.isDefault === 1 || a.defaultFlag === 1) || addressData[0]
      console.log('使用地址:', defaultAddr)
      
      // 创建订单 - 使用主键id
      const orderRes = await createOrderFromCart(defaultAddr.id)
      console.log('创建订单响应:', orderRes)
      
      if (orderRes.code === 200 && orderRes.data) {
        // 设置购买弹窗数据
        purchaseProducts.value = selected.map(item => ({
          name: item.productName,
          mainImage: item.productMainImage,
          price: item.productUnitPrice,
          quantity: item.quantity
        }))
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
    console.error('结算失败:', error)
    ElMessage.error(error.message || '结算失败，请稍后重试')
  }
}

// 稍后支付
const handlePayLater = (orderData) => {
  ElMessage.success('订单已创建，请尽快完成支付')
  purchaseDialogVisible.value = false
  // 刷新购物车
  loadCartData()
  router.push('/user/orders')
}

// 立即支付
const handlePayNow = async (orderData) => {
  try {
    const payRes = await payOrder(orderData.orderNo, orderData.paymentType)
    if (payRes.code === 200) {
      ElMessage.success('支付成功')
      purchaseDialogVisible.value = false
      // 刷新购物车
      loadCartData()
      router.push('/user/orders')
    } else {
      ElMessage.error(payRes.msg || '支付失败')
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.message || '支付失败，请稍后重试')
  }
}

// 添加到收藏
const addToFavorites = async (item) => {
  try {
    ElMessage.success('已添加到收藏')
  } catch (error) {
    console.error('添加到收藏失败:', error)
    ElMessage.error('添加失败')
  }
}

// 跳转到商品详情
const goToProductDetail = (productId) => {
  // 判断是否为盲盒商品（ID为9999或包含"blind"字符串）
  const isBlindBox = productId === 9999 || (typeof productId === 'string' && (productId.includes('blind') || productId.startsWith('blind-')));
  
  if (isBlindBox) {
    router.push({
      path: '/product/blind-box',
      query: { id: productId }
    })
  } else {
    router.push({
      path: '/product/detail',
      query: { id: productId }
    })
  }
}

// 去购物
const goShopping = () => {
  router.push('/products')
}

onMounted(() => {
  loadCartData()
})
</script>

<style scoped>
.shopping-cart {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 20px;
  max-width: 1500px;
  margin-left: auto;
  margin-right: auto;
}

.cart-content {
  max-width: 1500px;
  margin: 0 auto;
  padding: 0 20px;
}

.cart-container {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

/* 左侧商品列表 - 淘宝风格布局 */
.cart-list-section {
  flex: 1;
  min-width: 0;
  /* 防止flex元素溢出 */
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.cart-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.selected-count {
  font-size: 14px;
  color: #666;
}

.cart-items {
  max-height: 100%;
  overflow-y: auto;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.cart-item:hover {
  background-color: #fafafa;
}

.cart-item.selected {
  background-color: #fffaf0;
}

.item-select {
  margin-right: 15px;
  flex-shrink: 0;
}

.item-content {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
  min-width: 0;
  /* 防止内容溢出 */
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
  margin-right: 20px;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-name:hover {
  color: #ff5500;
}

.product-subtitle {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-spec {
  font-size: 12px;
  color: #999;
}

/* 价格和数量控制区域 */
.item-controls {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  min-width: 150px;
  flex-shrink: 0;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff5500;
}

.quantity {
  font-size: 14px;
  color: #666;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 5px;
}

.total-price {
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 80px;
  flex-shrink: 0;
}

/* 右侧结算面板 */
.checkout-section {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 20px;
}

.checkout-panel {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.panel-header {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.price-details {
  padding: 20px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.price-row.total {
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
  margin-top: 10px;
  font-size: 16px;
  font-weight: 600;
}

.label {
  color: #666;
}

.value {
  color: #333;
  font-weight: 500;
}

.value.free {
  color: #67c23a;
}

.value.discount {
  color: #67c23a;
}

.total-amount {
  color: #ff5500;
  font-size: 18px;
}

.checkout-actions {
  padding: 0 20px 20px;
}

.checkout-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  margin-bottom: 15px;
}

.security-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  font-size: 12px;
  color: #999;
}

/* 空购物车状态 */
.empty-cart {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 60px 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .cart-container {
    flex-direction: column;
  }

  .checkout-section {
    width: 100%;
    position: static;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .cart-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .item-content {
    width: 100%;
    flex-wrap: wrap;
  }

  .item-info {
    margin-right: 0;
    margin-bottom: 10px;
  }

  .item-controls {
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
    align-items: center;
  }

  .item-actions {
    flex-direction: row;
    width: 100%;
    justify-content: flex-end;
    gap: 15px;
  }
}

@media (max-width: 480px) {
  .cart-content {
    padding: 0 10px;
  }

  .cart-toolbar {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .toolbar-right {
    align-self: flex-end;
  }

  .item-content {
    gap: 10px;
  }

  .item-image {
    width: 60px;
    height: 60px;
  }
}
</style>