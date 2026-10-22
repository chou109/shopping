<template>
  <div class="shopping-cart">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-actions">
        <el-button type="danger" plain :disabled="cartItems.length === 0 || clearingCart || cartInteractionLocked" :loading="clearingCart" @click="handleClearCart">
          清空购物车
        </el-button>
      </div>
    </div>

    <!-- 购物车内容 -->
    <div class="cart-content" v-loading="loading || checkoutPreparing">
      <div v-if="loadError" class="cart-load-error" role="alert">
        <span>{{ loadError }}</span>
        <el-button link type="primary" :disabled="loading" @click="loadCartData">重试</el-button>
      </div>
      <div v-if="cartItems.length > 0" class="cart-container">
        <!-- 左侧商品列表 -->
        <div class="cart-list-section">
          <!-- 购物车工具栏 -->
          <div class="cart-toolbar">
            <div class="toolbar-left">
              <el-checkbox v-model="isAllSelected" :disabled="selectionBatchSaving || selectionSavingIds.size > 0 || cartInteractionLocked"
                @change="handleSelectAll" :indeterminate="isIndeterminate">
                全选
              </el-checkbox>
              <span class="selected-count">已选 {{ selectedCount }} 件商品</span>
            </div>
            <div class="toolbar-right">
              <el-button link @click="handleBatchDelete" :disabled="selectedCount === 0 || cartInteractionLocked" :loading="batchDeleting">
                删除选中
              </el-button>
            </div>
          </div>

          <!-- 商品列表 -->
          <div class="cart-items">
            <div v-for="item in cartItems" :key="item.id" class="cart-item" :class="{ selected: item.selected }">
              <div class="item-select">
                <el-checkbox v-model="item.selected" :aria-label="`选择商品：${item.productName}`"
                  :disabled="selectionBatchSaving || selectionSavingIds.has(item.id) || cartInteractionLocked"
                  @change="handleItemSelect(item)" />
              </div>

              <div class="item-content">
                <!-- 商品图片 -->
                <router-link class="item-image" :to="productDetailLocation(item)"
                  :aria-label="`查看商品：${item.productName}`">
                  <img
                    :src="isBlindBoxCartItem(item) ? BLIND_BOX_IMAGE : getProductImageUrl(item.productMainImage)"
                    :alt="item.productName" @error="handleImageError" />
                </router-link>

                <!-- 商品信息 -->
                <div class="item-info">
                  <h3 class="product-name">
                    <router-link class="product-name-link" :to="productDetailLocation(item)">
                      {{ item.productName }}
                    </router-link>
                  </h3>
                  <p class="product-subtitle">{{ item.productSubtitle }}</p>
                  <el-tag v-if="item.priceChanged" type="warning" size="small">商品价格已更新，结算按当前价</el-tag>
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
                    <el-button size="small" :disabled="quantitySavingIds.has(item.id) || item.quantity <= 1 || cartItemStock(item) === 0 || cartInteractionLocked" @click="decreaseQuantity(item)">
                      -
                    </el-button>
                    <el-input-number v-model="item.quantity" :min="1" :max="cartItemStock(item) ?? 999" :disabled="quantitySavingIds.has(item.id) || cartItemStock(item) === 0 || cartInteractionLocked" size="small"
                      controls-position="right" @change="updateQuantity(item)" />
                    <el-button size="small" :disabled="quantitySavingIds.has(item.id) || (cartItemStock(item) != null && item.quantity >= cartItemStock(item)) || cartInteractionLocked"
                      @click="increaseQuantity(item)">
                      +
                    </el-button>
                  </div>

                  <!-- 小计 -->
                  <div class="total-price">小计: ¥{{ formatPrice(item.productTotalPrice) }}</div>
                </div>

                <!-- 操作按钮 -->
                <div class="item-actions">
                  <el-button type="danger" text size="small" :loading="deletingCartIds.has(item.id)"
                    :disabled="deletingCartIds.has(item.id) || favoriteSavingIds.has(item.id) || cartInteractionLocked" @click="handleDeleteItem(item.id)">
                    删除
                  </el-button>
                  <el-button type="primary" text size="small" :loading="favoriteSavingIds.has(item.id)"
                    :disabled="favoriteSavingIds.has(item.id) || deletingCartIds.has(item.id) || batchDeleting || clearingCart || cartInteractionLocked"
                    @click="moveToFavorites(item)">
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

            <el-alert
              v-if="hasMultipleSelectedShops"
              class="multi-shop-alert"
              title="所选商品来自多个店铺，请按店铺分开选择并结算"
              type="warning"
              :closable="false"
              show-icon
            />

            <div class="price-details">
              <div class="price-row">
                <span class="label">商品总价</span>
                <span class="value">¥{{ formatPrice(selectedTotalPrice) }}</span>
              </div>
              <div class="price-row total">
                <span class="label">实付金额</span>
                <span class="value total-amount">¥{{ formatPrice(actualTotalPrice) }}</span>
              </div>
            </div>

            <div class="checkout-actions">
              <el-button type="danger" size="large" class="checkout-btn"
                :disabled="selectedCount === 0 || hasMultipleSelectedShops || checkoutPreparing || checkoutInFlight || cartMutationInFlight"
                :loading="checkoutPreparing"
                @click="handleCheckout">
                去结算 ({{ selectedCount }}件)
              </el-button>

              <div class="security-tips">
                <el-icon>
                  <Lock />
                </el-icon>
                <span>当前仅支持账户余额支付</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空购物车状态 -->
      <div v-else-if="!loading && !loadError" class="empty-cart">
        <el-empty description="购物车还是空的">
          <el-button type="primary" @click="goShopping">去逛逛</el-button>
        </el-empty>
      </div>
    </div>
  </div>

  <!-- 购买弹窗 -->
  <PurchaseDialog v-model="purchaseDialogVisible" :products="purchaseProducts" :order="purchaseOrder"
    :processing="checkoutInFlight" @pay-later="handlePayLater" @pay-now="handlePayNow"
    @cancel="purchaseDialogVisible = false" />

</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { BLIND_BOX_IMAGE, getProductImageUrl, handleImageError } from '@/utils/image'
import { listCart, updateCart, delCart, batchDelCart, clearCart, batchSelectCart } from '@/api/system/cart'
import { createOrderFromCart, payOrder } from '@/api/system/orderInfo'
import { getMyAddressList } from '@/api/system/address'
import { addFavorite, checkFavorite } from '@/api/system/favorite'
import { isBlindBoxProduct } from '@/utils/product'
import { moveCartItemToFavorite } from '@/utils/cartFavorite.mjs'
import { formatProductPrice } from '@/utils/productDetail.mjs'
import PurchaseDialog from '@/components/PurchaseDialog/index.vue'

const router = useRouter()
const store = useStore()
const loading = ref(false)
const loadError = ref('')
let cartLoadRequestId = 0
const batchDeleting = ref(false)
const clearingCart = ref(false)
const selectionSavingIds = ref(new Set())
const selectionBatchSaving = ref(false)
const quantitySavingIds = ref(new Set())
const deletingCartIds = ref(new Set())
const favoriteSavingIds = ref(new Set())

// 购物车数据
const cartItems = ref([])

// 购买弹窗相关
const purchaseDialogVisible = ref(false)
const checkoutPreparing = ref(false)
const checkoutInFlight = ref(false)
const purchaseProducts = ref([])
const purchaseOrder = ref({})
const cartInteractionLocked = computed(() => checkoutPreparing.value || purchaseDialogVisible.value || checkoutInFlight.value)
const cartMutationInFlight = computed(() => selectionBatchSaving.value || selectionSavingIds.value.size > 0
  || quantitySavingIds.value.size > 0 || batchDeleting.value || clearingCart.value
  || deletingCartIds.value.size > 0 || favoriteSavingIds.value.size > 0)

// 计算属性
const selectedItems = computed(() => {
  return cartItems.value.filter(item => item.selected)
})

const selectedCount = computed(() => {
  return selectedItems.value.length
})

const hasMultipleSelectedShops = computed(() => {
  const shopIds = new Set(selectedItems.value.map(item => item.product?.shopId ?? null))
  return shopIds.size > 1
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

const actualTotalPrice = computed(() => {
  return selectedTotalPrice.value
})

// 方法
const formatPrice = formatProductPrice

// 加载购物车数据
const loadCartData = async () => {
  const requestId = ++cartLoadRequestId
  loading.value = true
  loadError.value = ''
  try {
    const userId = store.state.user?.userId
    if (!userId) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    const response = await listCart()
    if (requestId !== cartLoadRequestId) return
    if (response.code === 200) {
      cartItems.value = Array.isArray(response.data) ? response.data : []
      // 计算小计并转换selected为布尔值
      cartItems.value.forEach(item => {
        // 后端返回的selected是数字(0/1)，需要转换为布尔值
        item.selected = item.selected === 1 || item.selected === true
        const currentPrice = item.product?.price
        item.priceChanged = currentPrice != null && Number(currentPrice) !== Number(item.productUnitPrice)
        if (currentPrice != null) {
          item.productUnitPrice = Number(currentPrice)
        }
        item.productTotalPrice = Number(item.productUnitPrice || 0) * Number(item.quantity || 0)
      })
    } else {
      loadError.value = response.msg || '获取购物车数据失败，请重试'
    }

  } catch (error) {
    if (requestId !== cartLoadRequestId) return
    console.error('加载购物车数据失败:', error)
    loadError.value = error.message || '加载购物车数据失败，请检查网络后重试'
  } finally {
    if (requestId === cartLoadRequestId) loading.value = false
  }
}

// 全选/取消全选
const handleSelectAll = async (selected) => {
  if (cartInteractionLocked.value || selectionBatchSaving.value || selectionSavingIds.value.size > 0) return
  selectionBatchSaving.value = true
  try {
    // 更新前端状态
    cartItems.value.forEach(item => {
      item.selected = selected
    })
    // 调用API更新后端状态
    const response = await batchSelectCart(selected ? 1 : 0)
    if (response.code !== 200) throw new Error(response.msg || '更新失败')
    // ElMessage.success('选择状态已更新')
  } catch (error) {
    console.error('更新全选状态失败:', error)
    ElMessage.error('更新失败')
    await loadCartData()
  } finally {
    selectionBatchSaving.value = false
  }
}

// 单个商品选择
const handleItemSelect = async (item) => {
  if (cartInteractionLocked.value || selectionBatchSaving.value || selectionSavingIds.value.has(item.id)) return
  selectionSavingIds.value = new Set([...selectionSavingIds.value, item.id])
  try {
    const response = await updateCart(item.id, {
      selected: item.selected ? 1 : 0
    })
    if (response.code !== 200) throw new Error(response.msg || '更新失败')
  } catch (error) {
    console.error('更新商品选择状态失败:', error)
    ElMessage.error(error.message || '更新失败，正在重新读取购物车状态')
    // 请求失败可能发生在服务端已写入、客户端未收到响应时，以服务端状态为准。
    await loadCartData()
  } finally {
    const savingIds = new Set(selectionSavingIds.value)
    savingIds.delete(item.id)
    selectionSavingIds.value = savingIds
  }
}

// 增加数量
const increaseQuantity = async (item) => {
  if (cartInteractionLocked.value || quantitySavingIds.value.has(item.id)) return
  const stockLimit = cartItemStock(item) ?? 999
  if (item.quantity >= stockLimit) {
    ElMessage.warning('库存不足')
    return
  }

  item.quantity++
  await updateCartItem(item)
}

// 减少数量
const decreaseQuantity = async (item) => {
  if (cartInteractionLocked.value || quantitySavingIds.value.has(item.id)) return
  if (item.quantity <= 1) {
    return
  }

  item.quantity--
  await updateCartItem(item)
}

// 更新数量
const updateQuantity = async (item) => {
  if (cartInteractionLocked.value || quantitySavingIds.value.has(item.id)) return
  if (item.quantity < 1) {
    item.quantity = 1
  }

  const stockLimit = cartItemStock(item) ?? 999
  if (item.quantity > stockLimit) {
    item.quantity = stockLimit
    ElMessage.warning('库存不足')
  }

  await updateCartItem(item)
}

// 更新购物车商品
const updateCartItem = async (item) => {
  if (cartInteractionLocked.value || quantitySavingIds.value.has(item.id)) return
  quantitySavingIds.value.add(item.id)
  try {
    // 重新计算小计
    item.productTotalPrice = item.productUnitPrice * item.quantity

    // 调用API更新数据库
    const response = await updateCart(item.id, {
      quantity: item.quantity,
      productTotalPrice: item.productTotalPrice
    })

    if (response.code === 200) {
      return
    }
    throw new Error(response.msg || '更新失败')
  } catch (error) {
    console.error('更新购物车失败:', error)
    ElMessage.error(error.message || '更新失败')
    await loadCartData()
  } finally {
    quantitySavingIds.value.delete(item.id)
  }
}

// 删除单个商品
const handleDeleteItem = async (itemId) => {
  if (cartInteractionLocked.value || deletingCartIds.value.has(itemId) || favoriteSavingIds.value.has(itemId)) return
  deletingCartIds.value.add(itemId)
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
  } finally {
    deletingCartIds.value.delete(itemId)
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (cartInteractionLocked.value || batchDeleting.value) return
  if (favoriteSavingIds.value.size > 0) {
    ElMessage.warning('请等待移入收藏操作完成后再删除')
    return
  }
  if (selectedCount.value === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }

  batchDeleting.value = true
  const selectedIds = selectedItems.value.map(item => item.id)
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.length} 件商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await batchDelCart(selectedIds)
    if (response.code !== 200) throw new Error(response.msg || '删除失败')
    cartItems.value = cartItems.value.filter(item => !selectedIds.includes(item.id))
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('批量删除失败:', error)
    }
  } finally {
    batchDeleting.value = false
  }
}

// 清空购物车
const handleClearCart = async () => {
  if (cartInteractionLocked.value || clearingCart.value) return
  if (favoriteSavingIds.value.size > 0) {
    ElMessage.warning('请等待移入收藏操作完成后再清空')
    return
  }
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车已经是空的')
    return
  }

  clearingCart.value = true
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
  } finally {
    clearingCart.value = false
  }
}

// 去结算
const handleCheckout = async () => {
  if (checkoutPreparing.value || checkoutInFlight.value) return
  if (cartMutationInFlight.value) {
    ElMessage.warning('请等待购物车更新完成后再结算')
    return
  }
  const selected = selectedItems.value
  if (selected.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }
  if (hasMultipleSelectedShops.value) {
    ElMessage.warning('所选商品来自多个店铺，请按店铺分开选择并结算')
    return
  }
  if (selected.some(isBlindBoxCartItem)) {
    ElMessage.warning('盲盒请前往盲盒页面购买')
    return
  }
  if (selected.some(item => cartItemStock(item) != null && item.quantity > cartItemStock(item))) {
    ElMessage.warning('部分商品库存不足，请调整数量后再结算')
    await loadCartData()
    return
  }

  checkoutPreparing.value = true
  try {
    const addressRes = await getMyAddressList()
    const addressData = addressRes.data || addressRes.rows || []
    if (addressRes.code !== 200 || addressData.length === 0) {
      ElMessage.warning('请先添加收货地址')
      router.push('/user/profile')
      return
    }
    const address = addressData.find(item => item.isDefault === 1 || item.defaultFlag === 1) || addressData[0]
    purchaseProducts.value = selected.map(item => ({
      name: item.productName,
      mainImage: item.productMainImage,
      price: item.productUnitPrice,
      quantity: item.quantity
    }))
    purchaseOrder.value = {
      totalAmount: actualTotalPrice.value,
      payment: actualTotalPrice.value,
      addressId: address.id,
      receiveName: address.receiveName,
      receivePhone: address.receivePhone,
      province: address.province,
      city: address.city,
      area: address.area,
      street: address.street
    }
    purchaseDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '结算失败，请稍后重试')
  } finally {
    checkoutPreparing.value = false
  }
}

const createCartOrder = async (addressId) => {
  const response = await createOrderFromCart(addressId || purchaseOrder.value.addressId)
  if (response.code !== 200) throw new Error(response.msg || '创建订单失败')
  return response.data
}

const handlePayLater = async (orderData) => {
  if (checkoutInFlight.value) return
  checkoutInFlight.value = true
  try {
    await createCartOrder(orderData.addressId)
    ElMessage.success('订单已创建，请尽快完成支付')
    purchaseDialogVisible.value = false
    await loadCartData()
    router.push('/user/orders')
  } catch (error) {
    ElMessage.error(error.message || '创建订单失败，请稍后重试')
  } finally {
    checkoutInFlight.value = false
  }
}

const handlePayNow = async (orderData) => {
  if (checkoutInFlight.value) return
  checkoutInFlight.value = true
  let createdOrder = null
  try {
    createdOrder = await createCartOrder(orderData.addressId)
    const response = await payOrder(createdOrder.orderNo, orderData.paymentType)
    if (response.code !== 200) throw new Error(response.msg || '支付失败，订单已保留待支付')
    ElMessage.success('支付成功')
    purchaseDialogVisible.value = false
    await loadCartData()
    router.push('/user/orders')
  } catch (error) {
    if (createdOrder) {
      ElMessage.warning('订单已创建，支付结果需确认；请前往订单页面查看订单状态')
      purchaseDialogVisible.value = false
      await loadCartData()
      router.push('/user/orders')
    } else {
      ElMessage.error(error.message || '创建订单失败，请稍后重试')
    }
  } finally {
    checkoutInFlight.value = false
  }
}

// 移入收藏：收藏确认成功后再从购物车删除，避免失败时丢失商品。
const moveToFavorites = async (item) => {
  if (cartInteractionLocked.value || favoriteSavingIds.value.has(item.id) || deletingCartIds.value.has(item.id)
    || batchDeleting.value || clearingCart.value) return
  favoriteSavingIds.value.add(item.id)
  try {
    const result = await moveCartItemToFavorite({
      productId: item.productId,
      cartItemId: item.id,
      checkFavorite,
      addFavorite,
      removeCartItem: delCart
    })
    cartItems.value = cartItems.value.filter(cartItem => cartItem.id !== item.id)
    ElMessage.success(result.alreadyFavorite ? '商品已在收藏中，并已从购物车移除' : '已移入收藏')
  } catch (error) {
    console.error('移入收藏失败:', error)
    if (error.favoriteSaved) {
      await loadCartData()
      if (!cartItems.value.some(cartItem => cartItem.id === item.id)) {
        ElMessage.success(error.alreadyFavorite ? '商品已在收藏中，并已从购物车移除' : '已移入收藏')
      } else {
        ElMessage.warning(error.alreadyFavorite
          ? '商品已在收藏中，但未能从购物车移除，请重试'
          : '商品已加入收藏，但未能从购物车移除，请重试')
      }
    } else {
      ElMessage.error(error.message || '移入收藏失败')
    }
  } finally {
    favoriteSavingIds.value.delete(item.id)
  }
}

const isBlindBoxCartItem = (item) => isBlindBoxProduct(item.product || {
  productId: item.productId,
  isBlindBox: item.isBlindBox
})

const cartItemStock = (item) => {
  const stock = item.product?.stock ?? item.stock
  return stock == null ? null : Number(stock)
}

const productDetailLocation = (item) => ({
  path: isBlindBoxCartItem(item) ? '/product/blind-box' : '/product/detail',
  query: { id: item.productId }
})

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

.cart-load-error {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px 16px;
  color: #b42318;
  background: #fef3f2;
  border: 1px solid #fecdca;
  border-radius: 8px;
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
  display: block;
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
}

.item-image:focus-visible,
.product-name-link:focus-visible {
  outline: 3px solid #ee0a24;
  outline-offset: 3px;
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

.product-name-link {
  color: inherit;
  text-decoration: none;
}

.product-name-link:hover {
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

/* 抽奖弹窗样式 */
.lottery-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  position: relative;
}

.lottery-drawing {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

/* 盲盒图片样式 */
.blind-box-image {
  width: 200px;
  height: 200px;
  object-fit: contain;
  margin-bottom: 20px;
  transition: transform 3s ease-out;
}

.lottery-spinning {
  transform: rotate(1800deg);
  /* 旋转5圈 */
}

.lottery-drawing .drawing-text {
  font-size: 18px;
  color: #ff5000;
  font-weight: bold;
}

.lottery-result {
  text-align: center;
}

.result-title {
  font-size: 24px;
  font-weight: bold;
  color: #FF6B6B;
  margin-bottom: 20px;
}

.result-prize {
  font-size: 32px;
  font-weight: bold;
  color: #FF4500;
  margin-bottom: 20px;
}

.result-info {
  font-size: 16px;
  color: #666;
}

.lottery-waiting {
  text-align: center;
}

.waiting-text {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.waiting-subtext {
  font-size: 14px;
  color: #666;
}
</style>
