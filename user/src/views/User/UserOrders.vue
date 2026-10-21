<template>
  <div class="user-orders">
    <!-- 订单状态标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部订单" name="all">
        <template #label>
          <span>全部订单</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="待付款" name="waitPay">
        <template #label>
          <span>待付款</span>
          <el-badge v-if="orderCounts.waitPay > 0" :value="orderCounts.waitPay" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="待发货" name="waitShip">
        <template #label>
          <span>待发货</span>
          <el-badge v-if="orderCounts.waitShip > 0" :value="orderCounts.waitShip" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="待收货" name="waitReceive">
        <template #label>
          <span>待收货</span>
          <el-badge v-if="orderCounts.waitReceive > 0" :value="orderCounts.waitReceive" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="待评价" name="waitReview">
        <template #label>
          <span>待评价</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已完成" name="completed">
        <template #label>
          <span>已完成</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已取消" name="cancelled">
        <template #label>
          <span>已取消</span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 订单列表 -->
    <div class="orders-container" v-loading="loading">
      <div v-if="loadError" class="empty-orders">
        <el-empty :description="loadError">
          <el-button type="primary" @click="loadOrders">重试</el-button>
        </el-empty>
      </div>

      <div v-else-if="orderList.length === 0" class="empty-orders">
        <el-empty description="暂无订单">
          <el-button type="primary" @click="goShopping">去购物</el-button>
        </el-empty>
      </div>

      <div v-else class="order-list">
        <div v-for="order in orderList" :key="order.id" class="order-card">
          <!-- 订单头部 -->
          <div class="order-header">
            <div class="order-info">
              <span class="order-no">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ formatTime(order.createTime) }}</span>
            </div>
            <div class="order-status" :class="getStatusClass(order.status)">
              {{ order.statusDesc }}
            </div>
          </div>

          <!-- 订单商品列表 -->
          <div class="order-products">
            <router-link v-for="(detail, index) in order.orderDetails" :key="index" class="product-item"
              :to="{ path: '/product/detail', query: { id: detail.productId } }"
              :aria-label="`查看商品：${detail.productName || '商品详情'}`">
              <div class="product-image">
                <img :src="getProductImageUrl(detail.productMainImage)" :alt="detail.productName"
                  @error="handleImageError" />
              </div>
              <div class="product-info">
                <div class="product-name">
                  <el-tag v-if="detail.productName && detail.productName.startsWith('【盲盒】')" type="warning" size="small"
                    class="blind-box-tag">盲盒</el-tag>
                  {{ detail.productName ? detail.productName.replace('【盲盒】', '') : '' }}
                </div>
                <div class="product-spec" v-if="detail.specifications">{{ detail.specifications }}</div>
              </div>
              <div class="product-price">
                <span class="price">¥{{ formatPrice(detail.currentUnitPrice) }}</span>
                <span class="quantity">×{{ detail.quantity }}</span>
              </div>
              <div class="product-subtotal">
                ¥{{ formatPrice(detail.totalPrice) }}
              </div>
            </router-link>
          </div>

          <!-- 订单底部 -->
          <div class="order-footer">
            <div class="order-address" v-if="order.receiveName">
              <el-icon>
                <Location />
              </el-icon>
              <span>{{ order.receiveName }} {{ order.receivePhone }}</span>
              <span class="address-detail">{{ order.province }}{{ order.city }}{{ order.area }}{{ order.street }}</span>
            </div>
            <div class="order-summary">
              <div class="total-info">
                共 <span class="count">{{ getTotalQuantity(order) }}</span> 件商品，
                实付：<span class="total-price">¥{{ formatPrice(order.payment) }}</span>
              </div>
              <OrderActions
                :order="order"
                :receipt-action-order-no="receiptActionOrderNo"
                :receipt-submitting="receiptSubmitting"
                :deleting-order-ids="deletingOrderIds"
                @cancel="handleCancelOrder"
                @pay="handlePayOrder"
                @refund="handleRefund"
                @confirm-receipt="handleConfirmReceipt"
                @logistics="viewLogistics"
                @review="handleReview"
                @detail="viewOrderDetail"
                @delete="handleDeleteOrder"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="total > pageSize">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
          @current-change="handleCurrentChange" />
      </div>
    </div>

    <OrderLifecycleDialogs
      v-model:pay-visible="payDialogVisible"
      v-model:cancel-visible="cancelDialogVisible"
      v-model:logistics-visible="logisticsDialogVisible"
      v-model:cancel-reason="cancelReason"
      :order="currentOrder"
      :logistics-order="logisticsOrder"
      :paying="paying"
      :cancelling="cancelling"
      @confirm-pay="confirmPay"
      @confirm-cancel="confirmCancel"
    />
    <OrderRefundDialog v-model="refundDialogVisible" :order="currentOrder" :loading="refunding" @submit="submitRefund" />

    <OrderDetailDialog v-model="detailDialogVisible" :order="detailOrder" />

    <OrderReviewDialog v-model="reviewDialogVisible" :order="reviewingOrder" :loading="submittingReview" @submit="submitReviewHandler" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import OrderRefundDialog from '@/components/OrderRefundDialog.vue'
import OrderReviewDialog from '@/components/OrderReviewDialog.vue'
import OrderActions from '@/components/OrderActions.vue'
import OrderDetailDialog from '@/components/OrderDetailDialog.vue'
import OrderLifecycleDialogs from '@/components/OrderLifecycleDialogs.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { getMyOrders, payOrder, cancelOrder, confirmReceipt, delInfo, applyRefund } from '@/api/system/orderInfo'
import { submitReview, completeOrder } from '@/api/comment'
import { getProductImageUrl, handleImageError } from '@/utils/image'
import { isBlindBoxOrder } from '@/utils/order.mjs'
import { useOrderList } from '@/composables/useOrderList.mjs'
import {
  formatOrderPrice as formatPrice,
  formatOrderTime as formatTime,
  getOrderStatusClass as getStatusClass,
  getOrderTotalQuantity as getTotalQuantity
} from '@/utils/orderDisplay.mjs'

const router = useRouter()

const {
  activeTab,
  loading,
  loadError,
  orderList,
  total,
  pageNum,
  pageSize,
  orderCounts,
  loadOrders,
  loadOrderCounts,
  handleTabChange,
  handleSizeChange,
  handleCurrentChange
} = useOrderList(getMyOrders)

// 支付相关
const payDialogVisible = ref(false)
const currentOrder = ref(null)
const paying = ref(false)

// 取消订单相关
const cancelDialogVisible = ref(false)
const cancelReason = ref('')
const cancelling = ref(false)
const receiptActionOrderNo = ref(null)
const receiptSubmitting = ref(false)

// 退款相关
const refundDialogVisible = ref(false)
const refunding = ref(false)

// 评价相关
const reviewDialogVisible = ref(false)
const submittingReview = ref(false)
const reviewingOrder = ref(null)
const deletingOrderIds = ref(new Set())

// 去购物
const goShopping = () => {
  router.push('/index')
}

// 查看订单详情
const detailDialogVisible = ref(false)
const detailOrder = ref(null)
const logisticsDialogVisible = ref(false)
const logisticsOrder = ref(null)

const viewOrderDetail = (order) => {
  detailOrder.value = order
  detailDialogVisible.value = true
}

// 查看物流
const viewLogistics = (order) => {
  logisticsOrder.value = order
  logisticsDialogVisible.value = true
}

// 立即付款
const handlePayOrder = (order) => {
  currentOrder.value = order
  payDialogVisible.value = true
}

// 确认支付
const confirmPay = async () => {
  if (!currentOrder.value || paying.value) return

  paying.value = true
  try {
    const res = await payOrder(currentOrder.value.orderNo, 4) // 4=余额支付
    if (res.code === 200) {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      loadOrders()
      loadOrderCounts()
    } else {
      ElMessage.error(res.msg || '支付失败')
    }
  } catch (e) {
    ElMessage.error('支付失败：' + (e.message || '未知错误'))
  } finally {
    paying.value = false
  }
}

// 取消订单
const handleCancelOrder = (order) => {
  currentOrder.value = order
  cancelReason.value = ''
  cancelDialogVisible.value = true
}

// 确认取消订单
const confirmCancel = async () => {
  if (!currentOrder.value || cancelling.value) return

  cancelling.value = true
  try {
    const res = await cancelOrder(currentOrder.value.orderNo, cancelReason.value || '用户主动取消')
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      cancelDialogVisible.value = false
      loadOrders()
      loadOrderCounts()
    } else {
      ElMessage.error(res.msg || '取消失败')
    }
  } catch (e) {
    ElMessage.error('取消失败：' + (e.message || '未知错误'))
  } finally {
    cancelling.value = false
  }
}

// 确认收货
const handleConfirmReceipt = async (order) => {
  if (receiptActionOrderNo.value !== null) return
  receiptActionOrderNo.value = order.orderNo
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })

    receiptSubmitting.value = true
    const res = await confirmReceipt(order.orderNo)
    if (res.code === 200) {
      ElMessage.success('已确认收货')
      loadOrders()
      loadOrderCounts()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.message ? `操作失败：${e.message}` : '操作失败')
    }
  } finally {
    receiptSubmitting.value = false
    receiptActionOrderNo.value = null
  }
}

// 申请退款
const handleRefund = (order) => {
  if (order.status !== 20) {
    ElMessage.warning('仅待发货订单可取消并退款')
    return
  }
  // 盲盒商品禁止退款
  if (isBlindBoxOrder(order)) {
    ElMessage.warning('盲盒订单不支持退款')
    return
  }
  currentOrder.value = order
  refundDialogVisible.value = true
}

// 提交退款申请
const submitRefund = async (reason) => {
  if (!currentOrder.value || refunding.value) return
  refunding.value = true
  try {
    const res = await applyRefund({ orderNo: currentOrder.value.orderNo, reason })
    if (res.code === 200) {
      ElMessage.success('订单已取消，款项已退回账户余额')
      refundDialogVisible.value = false
      loadOrders()
      loadOrderCounts()
    } else {
      ElMessage.error(res.msg || '取消退款失败')
    }
  } catch (e) {
    ElMessage.error('取消退款失败：' + (e.message || '未知错误'))
  } finally {
    refunding.value = false
  }
}

// 去评价
const handleReview = (order) => {
  reviewingOrder.value = order
  reviewDialogVisible.value = true
}

// 提交评价
const submitReviewHandler = async (reviewItems) => {
  if (!reviewingOrder.value || submittingReview.value) return

  submittingReview.value = true
  try {
    // 批量提交评价
    for (const item of reviewItems) {
      await submitReview({
        productId: item.productId,
        orderId: reviewingOrder.value.id,
        content: item.content || '用户未填写评价内容',
        score: item.score,
        isAnonymous: item.isAnonymous ? 1 : 0
      })
    }

    // 更新订单状态为已完成
    await completeOrder(reviewingOrder.value.orderNo)

    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    loadOrders()
  } catch (e) {
    ElMessage.error('评价失败：' + (e.message || '未知错误'))
  } finally {
    submittingReview.value = false
  }
}

// 删除订单
const handleDeleteOrder = async (order) => {
  if (deletingOrderIds.value.has(order.id)) return
  deletingOrderIds.value.add(order.id)
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？删除后不可恢复！', '删除订单', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' })

    const res = await delInfo(order.id)
    if (res.code === 200) {
      ElMessage.success('订单已删除')
      loadOrders()
      loadOrderCounts()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败：' + (e.message || '未知错误'))
    }
  } finally {
    deletingOrderIds.value.delete(order.id)
  }
}

onMounted(() => {
  loadOrders()
  loadOrderCounts()
})
</script>

<style scoped>
.user-orders {
  padding: 0;
  min-height: 100%;
}

.el-tabs {
  background: white;
  padding: 0 20px;
  border-radius: 8px;
  margin-bottom: 16px;
  position: sticky;
  top: 70px;
  z-index: 10;
}

:deep(.el-tabs__header) {
  margin-bottom: 0;
}

.tab-badge {
  margin-left: 4px;
}

.orders-container {
  min-height: 400px;
}

.empty-orders {
  background: transparent;
  padding: 60px 20px;
  border-radius: 8px;
  text-align: center;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.order-no {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.order-time {
  font-size: 13px;
  color: #999;
}

.order-status {
  font-size: 14px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 4px;
}

.status-wait-pay {
  color: #ff5000;
  background: #fff7f5;
}

.status-wait-ship {
  color: #409eff;
  background: #ecf5ff;
}

.status-wait-receive {
  color: #e6a23c;
  background: #fdf6ec;
}

.status-wait-review {
  color: #9c27b0;
  background: #f3e5f5;
}

.status-completed {
  color: #67c23a;
  background: #f0f9eb;
}

.status-cancelled {
  color: #909399;
  background: #f4f4f5;
}

.order-products {
  padding: 16px 20px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background 0.3s;
  color: inherit;
  text-decoration: none;
}

.product-item:hover {
  background: #f9f9f9;
}

.product-item:focus-visible {
  outline: 2px solid #409eff;
  outline-offset: 2px;
}

.product-item:last-child {
  margin-bottom: 0;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-spec {
  font-size: 12px;
  color: #999;
}

.blind-box-tag {
  margin-right: 6px;
  vertical-align: middle;
}

.product-price {
  text-align: right;
  min-width: 100px;
}

.product-price .price {
  color: #333;
  font-size: 14px;
}

.product-price .quantity {
  color: #999;
  font-size: 12px;
  margin-left: 4px;
}

.product-subtotal {
  color: #ff5000;
  font-size: 14px;
  font-weight: 600;
  min-width: 100px;
  text-align: right;
}

.order-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.order-address {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
}

.order-address .el-icon {
  color: #ff5000;
}

.address-detail {
  color: #999;
  margin-left: 8px;
}

.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-info {
  font-size: 14px;
  color: #666;
}

.total-info .count {
  color: #ff5000;
  font-weight: 600;
}

.total-info .total-price {
  color: #ff5000;
  font-size: 18px;
  font-weight: 700;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  background: transparent;
  padding: 16px;
  border-radius: 8px;
}

</style>
