<template>
  <div class="user-orders">
    <!-- 订单状态标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部订单" name="all">
        <template #label>
          <span>全部订单</span>
          <el-badge v-if="orderCounts.all > 0" :value="orderCounts.all" :max="99" class="tab-badge" />
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
          <el-badge v-if="orderCounts.waitReview > 0" :value="orderCounts.waitReview" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane label="已完成" name="completed">
        <template #label>
          <span>已完成</span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 订单列表 -->
    <div class="orders-container" v-loading="loading">
      <div v-if="orderList.length === 0" class="empty-orders">
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
            <div 
              v-for="(detail, index) in order.orderDetails" 
              :key="index" 
              class="product-item"
              @click="goProductDetail(detail.productId)"
            >
              <div class="product-image">
                <img :src="getProductImageUrl(detail.productMainImage)" :alt="detail.productName" @error="handleImageError" />
              </div>
              <div class="product-info">
                <div class="product-name">{{ detail.productName }}</div>
                <div class="product-spec" v-if="detail.specifications">{{ detail.specifications }}</div>
              </div>
              <div class="product-price">
                <span class="price">¥{{ formatPrice(detail.currentUnitPrice) }}</span>
                <span class="quantity">×{{ detail.quantity }}</span>
              </div>
              <div class="product-subtotal">
                ¥{{ formatPrice(detail.totalPrice) }}
              </div>
            </div>
          </div>
          
          <!-- 订单底部 -->
          <div class="order-footer">
            <div class="order-address" v-if="order.receiveName">
              <el-icon><Location /></el-icon>
              <span>{{ order.receiveName }} {{ order.receivePhone }}</span>
              <span class="address-detail">{{ order.province }}{{ order.city }}{{ order.area }}{{ order.street }}</span>
            </div>
            <div class="order-summary">
              <div class="total-info">
                共 <span class="count">{{ getTotalQuantity(order) }}</span> 件商品，
                实付：<span class="total-price">¥{{ formatPrice(order.payment) }}</span>
              </div>
              <div class="order-actions">
                <!-- 待付款订单 -->
                <template v-if="order.status === 10">
                  <el-button size="small" @click="handleCancelOrder(order)">取消订单</el-button>
                  <el-button type="primary" size="small" @click="handlePayOrder(order)">立即付款</el-button>
                </template>
                
                <!-- 待发货订单 -->
                <template v-else-if="order.status === 30">
                  <el-button size="small" @click="handleRefund(order)">申请退款</el-button>
                  <el-button size="small" @click="viewOrderDetail(order)">查看详情</el-button>
                </template>
                
                <!-- 待收货订单 -->
                <template v-else-if="order.status === 50">
                  <el-button size="small" @click="viewLogistics(order)">查看物流</el-button>
                  <el-button type="primary" size="small" @click="handleConfirmReceipt(order)">确认收货</el-button>
                </template>
                
                <!-- 待评价订单 -->
                <template v-else-if="order.status === 60 && !order.isReviewed">
                  <el-button size="small" @click="viewOrderDetail(order)">查看详情</el-button>
                  <el-button type="primary" size="small" @click="handleReview(order)">去评价</el-button>
                </template>
                
                <!-- 已完成/其他状态订单 -->
                <template v-else>
                  <el-button size="small" @click="viewOrderDetail(order)">查看详情</el-button>
                  <el-button size="small" @click="handleBuyAgain(order)">再次购买</el-button>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-container" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 支付弹窗 -->
    <el-dialog v-model="payDialogVisible" title="订单支付" width="400px">
      <div class="pay-dialog-content">
        <p>订单金额：<strong>¥{{ formatPrice(currentOrder?.payment) }}</strong></p>
        <p>支付方式：余额支付</p>
      </div>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="confirmPay">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- 取消订单弹窗 -->
    <el-dialog v-model="cancelDialogVisible" title="取消订单" width="400px">
      <el-form>
        <el-form-item label="取消原因">
          <el-input v-model="cancelReason" type="textarea" :rows="3" placeholder="请输入取消原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">返回</el-button>
        <el-button type="danger" :loading="cancelling" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { getMyOrders, payOrder, cancelOrder, confirmReceipt } from '@/api/system/orderInfo'
import { getProductImageUrl, handleImageError } from '@/utils/image'

const router = useRouter()

// 状态映射
const STATUS_MAP = {
  all: null,
  waitPay: 10,      // 待付款
  waitShip: 30,     // 待发货
  waitReceive: 50,  // 待收货
  waitReview: 60,   // 待评价（已完成但未评价）
  completed: 60     // 已完成
}

// 响应式数据
const activeTab = ref('all')
const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

// 订单数量统计
const orderCounts = ref({
  all: 0,
  waitPay: 0,
  waitShip: 0,
  waitReceive: 0,
  waitReview: 0
})

// 支付相关
const payDialogVisible = ref(false)
const currentOrder = ref(null)
const paying = ref(false)

// 取消订单相关
const cancelDialogVisible = ref(false)
const cancelReason = ref('')
const cancelling = ref(false)

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const status = STATUS_MAP[activeTab.value]
    const res = await getMyOrders(status)
    if (res.code === 200) {
      // 按创建时间倒序排列
      orderList.value = (res.rows || []).sort((a, b) => {
        return new Date(b.createTime) - new Date(a.createTime)
      })
      total.value = res.total || 0
    }
  } catch (e) {
    console.error('加载订单失败', e)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

// 加载订单数量统计
const loadOrderCounts = async () => {
  try {
    // 获取各状态订单数量
    const [allRes, waitPayRes, waitShipRes, waitReceiveRes] = await Promise.all([
      getMyOrders(null),
      getMyOrders(10),
      getMyOrders(30),
      getMyOrders(50)
    ])
    
    orderCounts.value = {
      all: allRes.total || 0,
      waitPay: waitPayRes.total || 0,
      waitShip: waitShipRes.total || 0,
      waitReceive: waitReceiveRes.total || 0,
      waitReview: 0 // 待评价需要特殊处理
    }
  } catch (e) {
    console.error('加载订单数量失败', e)
  }
}

// 标签页切换
const handleTabChange = (tab) => {
  pageNum.value = 1
  loadOrders()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  loadOrders()
}

const handleCurrentChange = (val) => {
  pageNum.value = val
  loadOrders()
}

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  try {
    return new Date(time).toLocaleString('zh-CN')
  } catch (e) {
    return time
  }
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = {
    10: 'status-wait-pay',
    20: 'status-paid',
    30: 'status-wait-ship',
    40: 'status-shipped',
    50: 'status-wait-receive',
    60: 'status-completed',
    70: 'status-cancelled'
  }
  return classMap[status] || ''
}

// 获取订单总商品数量
const getTotalQuantity = (order) => {
  if (!order.orderDetails) return 0
  return order.orderDetails.reduce((sum, item) => sum + (item.quantity || 0), 0)
}

// 去购物
const goShopping = () => {
  router.push('/index')
}

// 跳转商品详情
const goProductDetail = (productId) => {
  router.push(`/product/${productId}`)
}

// 查看订单详情
const viewOrderDetail = (order) => {
  ElMessage.info('订单详情功能开发中')
}

// 查看物流
const viewLogistics = (order) => {
  ElMessage.info('物流查询功能开发中')
}

// 立即付款
const handlePayOrder = (order) => {
  currentOrder.value = order
  payDialogVisible.value = true
}

// 确认支付
const confirmPay = async () => {
  if (!currentOrder.value) return
  
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
  if (!currentOrder.value) return
  
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
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const res = await confirmReceipt(order.orderNo)
    if (res.code === 200) {
      ElMessage.success('已确认收货')
      loadOrders()
      loadOrderCounts()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 申请退款
const handleRefund = (order) => {
  ElMessage.info('退款功能开发中')
}

// 去评价
const handleReview = (order) => {
  ElMessage.info('评价功能开发中')
}

// 再次购买
const handleBuyAgain = (order) => {
  if (order.orderDetails && order.orderDetails.length > 0) {
    const productId = order.orderDetails[0].productId
    router.push(`/product/${productId}`)
  }
}

onMounted(() => {
  loadOrders()
  loadOrderCounts()
})
</script>

<style scoped>
.user-orders {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100%;
}

.el-tabs {
  background: #fff;
  padding: 0 20px;
  border-radius: 8px;
  margin-bottom: 16px;
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
  background: #fff;
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

.status-paid, .status-wait-ship {
  color: #409eff;
  background: #ecf5ff;
}

.status-shipped, .status-wait-receive {
  color: #e6a23c;
  background: #fdf6ec;
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
}

.product-item:hover {
  background: #f9f9f9;
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

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  background: #fff;
  padding: 16px;
  border-radius: 8px;
}

.pay-dialog-content {
  text-align: center;
  padding: 20px 0;
}

.pay-dialog-content p {
  margin: 10px 0;
  font-size: 14px;
  color: #666;
}

.pay-dialog-content strong {
  color: #ff5000;
  font-size: 24px;
}
</style>
