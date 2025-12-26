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
          <div class="cart-items" ref="cartListRef">
            <div v-for="item in displayedItems" :key="item.id" class="cart-item" :class="{ selected: item.selected }">
              <div class="item-select">
                <el-checkbox v-model="item.selected" @change="handleItemSelect(item)" />
              </div>

              <div class="item-content">
                <!-- 商品图片 -->
                <div class="item-image" @click="goToProductDetail(item.productId)">
                  <img :src="getProductImageUrl(item.productMainImage)" :alt="item.name" @error="handleImageError" />
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
            <!-- 加载更多提示 -->
            <div v-if="hasMore" class="load-more">
              <el-icon class="loading-icon" v-if="loadingMore">
                <Loading />
              </el-icon>
              <span v-else>下滑加载更多</span>
            </div>
            <div v-else-if="displayedItems.length > 0" class="no-more">
              已加载全部商品
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
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, inject, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, Loading } from '@element-plus/icons-vue'
import { getProductImageUrl, handleImageError } from '@/utils/image'
import { listCart, updateCart, delCart, clearCart, batchSelectCart } from '@/api/system/cart'
import { createOrderFromCart, payOrder } from '@/api/system/orderInfo'
import { getMyAddressList } from '@/api/system/address'
import PurchaseDialog from '@/components/PurchaseDialog/index.vue'


const router = useRouter()
const store = useStore()

// 注入父组件的滚动容器引用
const scrollContainer = inject('scrollContainer', null)
const loading = ref(false)

// 购物车数据
const cartItems = ref([])

// 懒加载相关
const cartListRef = ref(null)
const pageSize = 10 // 每次加载数量
const displayCount = ref(10) // 当前显示数量
const loadingMore = ref(false)

// 显示的商品列表（懒加载）
const displayedItems = computed(() => {
  return cartItems.value.slice(0, displayCount.value)
})

// 是否还有更多
const hasMore = computed(() => {
  return displayCount.value < cartItems.value.length
})

// 滚动加载更多（监听父容器 .user-content 的滚动事件）
const handleScroll = () => {
  if (!scrollContainer || !scrollContainer.value) return

  const { scrollTop, scrollHeight, clientHeight } = scrollContainer.value

  // 滚动到底部时加载更多
  if (scrollHeight - scrollTop - clientHeight < 100 && hasMore.value && !loadingMore.value) {
    loadMore()
  }
}

// 加载更多
const loadMore = () => {
  if (loadingMore.value || !hasMore.value) return

  loadingMore.value = true
  setTimeout(() => {
    displayCount.value += pageSize
    loadingMore.value = false
  }, 300)
}

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

// 重置懒加载
const resetLazyLoad = () => {
  displayCount.value = pageSize
}

// 方法
const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 加载购物车数据
// 加载购物车数据
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
      // 强制设置所有商品为未选中状态
      cartItems.value.forEach(item => {
        // 确保selected字段有值且为false
        item.selected = false
        // 计算小计
        if (item.productUnitPrice && item.quantity && !item.productTotalPrice) {
          item.productTotalPrice = item.productUnitPrice * item.quantity
        }
      })
      // 同步未选中状态到后端
      try {
        await batchSelectCart(0)
      } catch (error) {
        console.error('同步未选中状态失败:', error)
      }
      // 重置懒加载
      resetLazyLoad()
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
  cartItems.value.forEach(item => {
    item.selected = selected
  })
  // 同步选中状态到后端
  try {
    const response = await batchSelectCart(selected ? 1 : 0)
    if (response.code !== 200) {
      ElMessage.error('更新选中状态失败')
    }
  } catch (error) {
    console.error('更新选中状态失败:', error)
    ElMessage.error('更新选中状态失败')
  }
}

// 单个商品选择
const handleItemSelect = async (item) => {
  console.log('商品选择状态变化:', item.productName, item.selected)
  try {
    // 同步选中状态到后端
    const response = await updateCart(item.id, {
      selected: item.selected ? 1 : 0
    })
    if (response.code !== 200) {
      // 如果更新失败，恢复原来的状态
      item.selected = !item.selected
      ElMessage.error('更新选中状态失败')
    }
  } catch (error) {
    console.error('更新选中状态失败:', error)
    // 如果更新失败，恢复原来的状态
    item.selected = !item.selected
    ElMessage.error('更新选中状态失败')
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
const handleCheckout = () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }

  router.push({
    path: '/order/confirm',
    query: {
      cartItems: JSON.stringify(selectedItems.value)
    }
  })
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
  router.push({
    path: '/product/detail',
    query: { id: productId }
  })
}

// 去购物
const goShopping = () => {
  router.push('/products')
}

onMounted(() => {
  loadCartData()
  // 使用 nextTick 等待父容器准备完毕
  nextTick(() => {
    if (scrollContainer && scrollContainer.value) {
      scrollContainer.value.addEventListener('scroll', handleScroll)
    }
  })
})

onUnmounted(() => {
  if (scrollContainer && scrollContainer.value) {
    scrollContainer.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped>
.shopping-cart {
  padding: 0;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  max-width: 1500px;
  margin-left: auto;
  margin-right: auto;
  position: sticky;
  top: 70px;
  background: white;
  z-index: 10;
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
  background: transparent;
  border-radius: 8px;
}

.cart-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: white;
  position: sticky;
  top: 130px;
  z-index: 9;
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
  /* 不设置滚动，让内容自然擑开 */
}

/* 加载更多提示 */
.load-more,
.no-more {
  padding: 20px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.load-more .loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
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
  background: transparent;
  border-radius: 8px;
  overflow: hidden;
}

.panel-header {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: transparent;
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
  background: transparent;
  border-radius: 8px;
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