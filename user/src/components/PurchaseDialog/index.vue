<template>
  <el-dialog
    v-model="dialogVisible"
    title="确认购买"
    width="900px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @closed="handleDialogClosed"
  >
    <div class="purchase-container" v-loading="loading">
      <div class="purchase-content">
        <!-- 左侧订单信息 -->
        <div class="order-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>支付信息</span>
          </div>
          
          <div class="order-info">
            <!-- 订单基本信息 -->
            <div class="info-group">
              <div class="info-item">
                <span class="label">支付单号：</span>
                <span class="value">{{ orderData.orderNo || '待生成' }}</span>
              </div>
              <div class="info-item">
                <span class="label">当前时间：</span>
                <span class="value">{{ formatTime(new Date()) }}</span>
              </div>
              <div class="info-item">
                <span class="label">支付状态：</span>
                <span class="value status-tag">{{ orderData.statusDesc || '待付款' }}</span>
              </div>
            </div>

            <!-- 商品信息 -->
            <div class="products-section">
              <div class="sub-title">商品明细</div>
              <div class="product-list">
                <div 
                  v-for="(item, index) in productList" 
                  :key="index" 
                  class="product-item"
                >
                  <div class="product-image">
                    <img :src="getProductImageUrl(item.mainImage || item.productMainImage)" :alt="item.name || item.productName" @error="handleImageError" />
                  </div>
                  <div class="product-info">
                    <div class="product-name">{{ item.name || item.productName }}</div>
                    <div class="product-spec" v-if="item.specifications">{{ item.specifications }}</div>
                  </div>
                  <div class="product-price">
                    <span class="price">¥{{ formatPrice(item.price || item.productUnitPrice) }}</span>
                    <span class="quantity">×{{ item.quantity }}</span>
                  </div>
                  <div class="product-subtotal">
                    ¥{{ formatPrice((item.price || item.productUnitPrice) * item.quantity) }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 价格汇总 -->
            <div class="price-summary">
              <div class="summary-row">
                <span class="label">商品总价：</span>
                <span class="value">¥{{ formatPrice(totalProductPrice) }}</span>
              </div>
              <div class="summary-row">
                <span class="label">邮费：</span>
                <span class="value free">{{ orderData.postage > 0 ? '¥' + formatPrice(orderData.postage) : '免邮费' }}</span>
              </div>
              <div class="summary-row total">
                <span class="label">实付金额：</span>
                <span class="value total-price">¥{{ formatPrice(orderData.payment || totalPayment) }}</span>
              </div>
            </div>

            <!-- 收货信息 -->
            <div class="address-section">
              <div class="sub-title">
                收货信息
                <el-button v-if="addressList.length > 1" type="primary" link size="small" @click="showAddressSelector = true" style="margin-left: 10px;">
                  更换地址
                </el-button>
              </div>
              <div class="address-info" v-if="orderData.receiveName">
                <div class="info-row">
                  <span class="label">收货人：</span>
                  <span class="value">{{ orderData.receiveName }}</span>
                </div>
                <div class="info-row">
                  <span class="label">联系电话：</span>
                  <span class="value">{{ orderData.receivePhone }}</span>
                </div>
                <div class="info-row">
                  <span class="label">收货地址：</span>
                  <span class="value">{{ fullAddress }}</span>
                </div>
              </div>
              <div class="no-address" v-else>
                <el-alert
                  title="请先选择收货地址"
                  type="warning"
                  :closable="false"
                  show-icon
                />
                <el-button type="primary" size="small" @click="showAddressSelector = true" style="margin-top: 10px;">
                  选择地址
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧支付方式 -->
        <div class="payment-section">
          <div class="section-title">
            <el-icon><CreditCard /></el-icon>
            <span>选择支付方式</span>
          </div>
          
          <div class="payment-methods">
            <div 
              v-for="method in paymentMethods" 
              :key="method.value"
              :class="['payment-method', { active: selectedPaymentType === method.value, disabled: method.disabled }]"
              @click="selectPaymentMethod(method)"
            >
              <div class="method-icon">
                <img :src="method.icon" :alt="method.label" />
              </div>
              <div class="method-info">
                <div class="method-name">{{ method.label }}</div>
                <div class="method-desc">{{ method.desc }}</div>
              </div>
              <div class="method-check" v-if="selectedPaymentType === method.value">
                <el-icon><CircleCheckFilled /></el-icon>
              </div>
              <div class="method-unavailable" v-if="method.disabled">
                <el-tag type="info" size="small">暂未开放</el-tag>
              </div>
            </div>
          </div>

          <!-- 支付提示 -->
          <div class="payment-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>选择支付方式后，点击"确认支付"进行付款</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirmPayment"
          :disabled="!selectedPaymentType || !orderData.receiveName"
          :loading="submitting || processing"
        >
          确认支付
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 地址选择弹窗 -->
  <el-dialog
    v-model="showAddressSelector"
    title="选择收货地址"
    width="500px"
    :close-on-click-modal="true"
  >
    <div class="address-selector">
      <div 
        v-for="addr in addressList" 
        :key="addr.id"
        :class="['address-item', { active: selectedAddressId === addr.id }]"
        @click="handleSelectAddress(addr)"
      >
        <div class="address-content">
          <div class="address-header">
            <span class="name">{{ addr.receiveName }}</span>
            <span class="phone">{{ addr.receivePhone }}</span>
            <el-tag v-if="addr.defaultFlag === 1" type="primary" size="small" style="margin-left: 8px;">默认</el-tag>
          </div>
          <div class="address-detail">
            {{ addr.province }}{{ addr.city }}{{ addr.area }}{{ addr.street }}
          </div>
        </div>
        <div class="address-check" v-if="selectedAddressId === addr.id">
          <el-icon><CircleCheckFilled /></el-icon>
        </div>
      </div>
      <div v-if="addressList.length === 0" class="no-address-tip">
        暂无收货地址，请先在个人中心添加
      </div>
    </div>
  </el-dialog>

  <!-- 支付确认弹窗 -->
  <el-dialog
    v-model="confirmDialogVisible"
    title="支付确认"
    width="400px"
    :close-on-click-modal="false"
  >
    <div class="confirm-content">
      <el-icon class="confirm-icon"><QuestionFilled /></el-icon>
      <div class="confirm-text">
        <p>订单金额：<strong>¥{{ formatPrice(orderData.payment || totalPayment) }}</strong></p>
        <p>支付方式：<strong>{{ getPaymentLabel(selectedPaymentType) }}</strong></p>
        <p class="confirm-question">是否立即支付？</p>
      </div>
    </div>
    <template #footer>
      <div class="confirm-footer">
        <el-button @click="handlePayLater" :disabled="processing">稍后支付</el-button>
        <el-button type="primary" @click="handlePayNow" :loading="processing">立即支付</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Document, 
  CreditCard, 
  CircleCheckFilled, 
  InfoFilled,
  QuestionFilled
} from '@element-plus/icons-vue'
import { getProductImageUrl, handleImageError } from '@/utils/image'
import { getMyAddressList } from '@/api/system/address'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  // 商品列表
  products: {
    type: Array,
    default: () => []
  },
  // 订单数据
  order: {
    type: Object,
    default: () => ({})
  },
  // 是否需要创建订单
  needCreateOrder: {
    type: Boolean,
    default: true
  },
  // 父页面正在创建订单或支付
  processing: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel', 'payNow', 'payLater', 'createOrder'])

// 响应式数据
const dialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const showAddressSelector = ref(false)
const loading = ref(false)
const submitting = ref(false)
const selectedPaymentType = ref(null)
const addressList = ref([])
const selectedAddressId = ref(null)

// 订单数据
const orderData = ref({
  id: null,
  orderNo: '',
  payment: 0,
  paymentType: null,
  paymentTypeDesc: '',
  postage: 0,
  status: 10, // 待付款
  statusDesc: '待付款',
  receiveName: '',
  receivePhone: '',
  province: '',
  city: '',
  area: '',
  street: '',
  postalCode: '',
  createTime: null,
  userId: null
})

// 商品列表
const productList = ref([])

// 支付方式列表
const paymentMethods = ref([
  {
    value: 1,
    label: '支付宝',
    desc: '功能暂未开放',
    icon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCI+PHBhdGggZmlsbD0iIzFDODFFNCIgZD0iTTM4LjUgNGgtMjlDNi40NjIgNCA0IDYuNDYyIDQgOS41djI5QzQgNDEuNTM4IDYuNDYyIDQ0IDkuNSA0NGgyOWMzLjAzOCAwIDUuNS0yLjQ2MiA1LjUtNS41di0yOUM0NCA2LjQ2MiA0MS41MzggNCAzOC41IDR6Ii8+PHBhdGggZmlsbD0iI0ZGRiIgZD0iTTMzLjEzNSAzMS44NjFjLTEuMzY0LS40MTktMi43NC0uOTE1LTQuMjctMS40NjdhMjMuNDg0IDIzLjQ4NCAwIDAwMS44ODEtNC4xNTdoLTcuNjN2LTEuOTZoNC45OTR2LTEuNTEzaC00Ljk5NHYtMy4wMTNoLTIuOTk2djMuMDEzaC01LjEydjEuNTEzaDUuMTJ2MS45NmgtNi4yNTR2MS41MTNoNy4zODZhMTkuNzMzIDE5LjczMyAwIDAxLTEuMjg2IDMuMjcxYy0yLjU2NyAxLjA0NC01LjM3NiAyLjIyOC02LjA4OSAyLjQ5LTEuNTMzLjU2Ny0xLjY1NSAyLjA1Ny0uODE2IDIuODc2Ljc4Ny43NyAyLjI3My44MTQgMy4zNjguMjM1czMuNjQ4LTIuMjI2IDUuNDI0LTMuNTNjMS45MTQuNzU2IDQuMTg0IDEuNiA2LjI3NiAyLjI3NCAyLjQ2NC43OTYgNS43NDYuNDA1IDYuMDA2LTIuNjE2LjI2LTMuMDItMi4zNjYtMi43MDQtNS4wMjQtMy40NXoiLz48L3N2Zz4=',
    disabled: true
  },
  {
    value: 2,
    label: '微信支付',
    desc: '功能暂未开放',
    icon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCI+PHBhdGggZmlsbD0iIzA5QjgzRSIgZD0iTTM4LjUgNGgtMjlDNi40NjIgNCA0IDYuNDYyIDQgOS41djI5QzQgNDEuNTM4IDYuNDYyIDQ0IDkuNSA0NGgyOWMzLjAzOCAwIDUuNS0yLjQ2MiA1LjUtNS41di0yOUM0NCA2LjQ2MiA0MS41MzggNCAzOC41IDR6Ii8+PHBhdGggZmlsbD0iI0ZGRiIgZD0iTTMwLjY4NCAyOS4xMjZjLTEuMzY1LS42MjktMi4zNjYtMS4wNjEtMi45NDgtMS4yNzFsLS4yMzYtLjA4NWMtLjI5Ni0uMTA3LS41MDItLjE4LS42MTgtLjIybC0uMTM0LS4wNDdjLS4xMTgtLjA0Mi0uMTk3LS4wNzEtLjIzNy0uMDg3YS45MTUuOTE1IDAgMDAtLjkzLjE0NC43OTMuNzkzIDAgMDAtLjI4OC44NDdjLjA1LjIxNy4xNjMuNDAxLjMyNS41NDEuMDg0LjA3Mi4xODUuMTQxLjMwNC4yMDkuMTE5LjA2OS4yNy4xNS40NTMuMjQ0LjIyNy4xMTcuNDcuMjQzLjcyNy4zOC4zMDMuMTYxLjU4LjMxNy44MzEuNDY5LjYxNy4zNzQgMS4xNTQuNzgyIDEuNjEgMS4yMjMuNzYxLjczNiAxLjMgMS42NDMgMS42MTYgMi43MTguMDkuMzA3LjE1MS42MjMuMTg0Ljk0OC4wMTYuMTYuMDI3LjM1Ny4wMzMuNTg4YTYuNTI3IDYuNTI3IDAgMDEtLjEyNiAxLjYxOSA3LjMzIDcuMzMgMCAwMS0uNDg3IDEuNTk2Yy0uMTkxLjQ0NS0uNDI4Ljg2NC0uNzEgMS4yNThhNy4xMyA3LjEzIDAgMDEtLjkzIDEuMDYyIDguMjcxIDguMjcxIDAgMDEtLjUyMi40NjdjLS40MjEuMzQ1LS44NzQuNjQ1LTEuMzU4Ljg5OC0uNjQ1LjMzNy0xLjMyOC41NzktMi4wNDcuNzI2YTEwLjQwNyAxMC40MDcgMCAwMS0yLjE2My4yMjRoLS4yNDVhOS4zMDMgOS4zMDMgMCAwMS0yLjI0OC0uMzI1IDkuMDA5IDkuMDA5IDAgMDEtMS45NTEtLjc4NGwtLjE1My0uMDg1Yy0uMjQ2LS4xNC0uNDkxLS4yODctLjczNS0uNDRhNy40MDUgNy40MDUgMCAwMS0uNzE1LS41MTJjLjMxNS4wMzUuNjMuMDUyLjk0Ny4wNTJhNy44NjkgNy44NjkgMCAwMDEuNjI2LS4xNjkgNy43MSA3LjcxIDAgMDAxLjUzNi0uNDgyIDcuNDE3IDcuNDE3IDAgMDAxLjM1LS43MjcgNy4wMDYgNy4wMDYgMCAwMDEuMDktLjlsLjI0NS0uMjQ1LS4zNjYtLjE1MmMtLjMxOC0uMTMyLS42NjEtLjI5NS0xLjAzMS0uNDg4YTEyLjkxNiAxMi45MTYgMCAwMS0xLjE5Mi0uNjk4IDEwLjcxOCAxMC43MTggMCAwMS0xLjIyNS0uOTA5IDguNzkxIDguNzkxIDAgMDEtMS4xMzMtMS4wOTYgNy4zODEgNy4zODEgMCAwMS0uOTE1LTEuMjY1IDYuNDA4IDYuNDA4IDAgMDEtLjU4OS0xLjQxNyA2LjExMiA2LjExMiAwIDAxLS4yMjYtMS41NTEgNS44NjcgNS44NjcgMCAwMS4xMjktMS42MDYgNS41OCA1LjU4IDAgMDEuNDU4LTEuMzY1IDYuMjEyIDYuMjEyIDAgMDEuODYxLTEuMjc2IDcuMTI3IDcuMTI3IDAgMDExLjE3Ni0xLjAzNCA4LjA1IDguMDUgMCAwMTEuNDMtLjc2NyA4Ljc2NSA4Ljc2NSAwIDAxMS42MTktLjQ4MSA5LjM4NyA5LjM4NyAwIDAxMS43NC0uMTY2Yy4zNjYgMCAuNzI5LjAyIDEuMDg4LjA2YTguOTcgOC45NyAwIDAxMS4wNDIuMTc4Yy4zNDEuMDc3LjY3NS4xNzMgMS4wMDIuMjg2YTguMzIxIDguMzIxIDAgMDEuOTUuMzgyIDcuNzM0IDcuNzM0IDAgMDEuODgzLjQ3MWMuMjguMTc4LjU0OS4zNjguODA0LjU2OWE3LjM5MyA3LjM5MyAwIDAxLjcxMi42NTggNi45NjkgNi45NjkgMCAwMS42MDguNzM3IDYuNTYxIDYuNTYxIDAgMDEuNDkuODA2IDYuMjI4IDYuMjI4IDAgMDEuMzYxLjg2MyA2LjAwNCA2LjAwNCAwIDAxLjIyLjkwNiA1Ljg1OSA1Ljg1OSAwIDAxLjA3Ni45MzRjMCAuMjU5LS4wMTkuNTEzLS4wNTYuNzYyLS4wMzguMjUtLjA5Mi40OTUtLjE2MS43MzYtLjA3LjI0MS0uMTU2LjQ3Ny0uMjU4LjcwOGE2LjE0OCA2LjE0OCAwIDAxLS43MjcgMS4yMiA2Ljk0MSA2Ljk0MSAwIDAxLS40NjYuNTQ2IDcuNDk5IDcuNDk5IDAgMDEtLjUzNS41MDMgOC4xNyA4LjE3IDAgMDEtMS4yMy44NDZjLS40MzguMjQ2LS44OTUuNDUxLTEuMzcuNjE0eiIvPjwvc3ZnPg==',
    disabled: true
  },
  {
    value: 3,
    label: '银行卡支付',
    desc: '功能暂未开放',
    icon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCI+PHBhdGggZmlsbD0iIzU1N0VCOSIgZD0iTTQ0IDM2YzAgMi4yMS0xLjc5IDQtNCA0SDhjLTIuMjEgMC00LTEuNzktNC00VjEyYzAtMi4yMSAxLjc5LTQgNC00aDMyYzIuMjEgMCA0IDEuNzkgNCA0djI0eiIvPjxwYXRoIGZpbGw9IiMxRTNBNTkiIGQ9Ik00NCAxNkg0di02YzAtMi4yMSAxLjc5LTQgNC00aDMyYzIuMjEgMCA0IDEuNzkgNCA0djZ6Ii8+PHBhdGggZmlsbD0iI0ZGRiIgZD0iTTggMjRoMTJhMiAyIDAgMDEwIDRIOGEyIDIgMCAwMTAtNHptMCA2aDhhMiAyIDAgMDEwIDRIOGEyIDIgMCAwMTAtNHoiLz48L3N2Zz4=',
    disabled: true
  },
  {
    value: 4,
    label: '余额支付',
    desc: '使用账户余额支付',
    icon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCA0OCA0OCI+PHBhdGggZmlsbD0iI0ZGNjYwMCIgZD0iTTM4LjUgNGgtMjlDNi40NjIgNCA0IDYuNDYyIDQgOS41djI5QzQgNDEuNTM4IDYuNDYyIDQ0IDkuNSA0NGgyOWMzLjAzOCAwIDUuNS0yLjQ2MiA1LjUtNS41di0yOUM0NCA2LjQ2MiA0MS41MzggNCAzOC41IDR6Ii8+PHBhdGggZmlsbD0iI0ZGRiIgZD0iTTI0IDEyYy02LjYyNyAwLTEyIDUuMzczLTEyIDEyczUuMzczIDEyIDEyIDEyIDEyLTUuMzczIDEyLTEyLTUuMzczLTEyLTEyLTEyem0xIDE4aC0ydi0yaDJ2MnptMC00aC0ydi04aDJ2OHoiLz48L3N2Zz4=',
    disabled: false
  }
])

// 计算属性
const totalProductPrice = computed(() => {
  return productList.value.reduce((total, item) => {
    return total + ((item.price || item.productUnitPrice) * item.quantity)
  }, 0)
})

const totalPayment = computed(() => {
  return totalProductPrice.value + (orderData.value.postage || 0)
})

const fullAddress = computed(() => {
  const { province, city, area, street } = orderData.value
  return [province, city, area, street].filter(Boolean).join('')
})

// 监听props变化
watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val) {
    initData()
  }
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

watch(() => props.products, (val) => {
  if (val && val.length > 0) {
    productList.value = [...val]
  }
}, { deep: true, immediate: true })

watch(() => props.order, (val) => {
  if (val && Object.keys(val).length > 0) {
    orderData.value = { ...orderData.value, ...val }
  }
}, { deep: true, immediate: true })

// 方法
const initData = async () => {
  selectedPaymentType.value = null
  productList.value = [...props.products]
  if (props.order && Object.keys(props.order).length > 0) {
    orderData.value = { ...orderData.value, ...props.order }
    selectedAddressId.value = props.order.addressId
  }
  // 加载地址列表
  await loadAddressList()
}

const formatPrice = (price) => {
  if (!price) return '0.00'
  return parseFloat(price).toFixed(2)
}

const formatTime = (time) => {
  if (!time) return ''
  try {
    return new Date(time).toLocaleString('zh-CN')
  } catch (e) {
    return time
  }
}

const getPaymentLabel = (value) => {
  const method = paymentMethods.value.find(m => m.value === value)
  return method ? method.label : ''
}

const selectPaymentMethod = (method) => {
  if (method.disabled) {
    ElMessage.warning('该支付方式暂未开放，请通过余额购买')
    return
  }
  selectedPaymentType.value = method.value
}

// 加载用户地址列表
const loadAddressList = async () => {
  try {
    const res = await getMyAddressList()
    if (res.code === 200) {
      addressList.value = res.data || []
    }
  } catch (e) {
    console.error('加载地址列表失败', e)
  }
}

// 选择地址
const handleSelectAddress = (addr) => {
  selectedAddressId.value = addr.id
  // 更新订单数据中的地址信息
  orderData.value.addressId = addr.id
  orderData.value.receiveName = addr.receiveName
  orderData.value.receivePhone = addr.receivePhone
  orderData.value.province = addr.province
  orderData.value.city = addr.city
  orderData.value.area = addr.area
  orderData.value.street = addr.street
  orderData.value.postalCode = addr.postalCode
  showAddressSelector.value = false
  ElMessage.success('地址已更换')
}

const handleCancel = () => {
  dialogVisible.value = false
  emit('cancel')
}

const handleDialogClosed = () => {
  selectedPaymentType.value = null
  loading.value = false
  submitting.value = false
}

const handleConfirmPayment = () => {
  if (!selectedPaymentType.value) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  if (!orderData.value.receiveName || !orderData.value.addressId) {
    ElMessage.warning('请先选择收货地址')
    showAddressSelector.value = true
    return
  }
  
  // 更新订单的支付类型
  orderData.value.paymentType = selectedPaymentType.value
  orderData.value.paymentTypeDesc = getPaymentLabel(selectedPaymentType.value)
  
  // 显示支付确认弹窗
  confirmDialogVisible.value = true
}

// 稍后支付 - 订单状态为待付款
const handlePayLater = async () => {
  if (props.processing) return
  // emit 不会等待父页面的异步订单请求；保留主弹窗，让父页面反馈成功后关闭。
  confirmDialogVisible.value = false
  emit('payLater', {
    ...orderData.value,
    products: productList.value,
    paymentType: selectedPaymentType.value,
    status: 10,
    statusDesc: '待付款'
  })
}

// 立即支付 - 订单状态也是待付款（实际支付后会更新状态）
const handlePayNow = async () => {
  if (props.processing) return
  confirmDialogVisible.value = false
  emit('payNow', {
    ...orderData.value,
    products: productList.value,
    paymentType: selectedPaymentType.value,
    status: 10,
    statusDesc: '待付款'
  })
}

// 更新订单数据
const updateOrderData = (data) => {
  orderData.value = { ...orderData.value, ...data }
}

// 暴露方法
defineExpose({
  updateOrderData
})
</script>

<style scoped>
.purchase-container {
  min-height: 400px;
}

.purchase-content {
  display: flex;
  gap: 24px;
}

.order-section {
  flex: 1;
  min-width: 0;
}

.payment-section {
  width: 280px;
  flex-shrink: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  padding-bottom: 12px;
  border-bottom: 2px solid #ff5000;
  margin-bottom: 16px;
}

.section-title .el-icon {
  color: #ff5000;
  font-size: 20px;
}

.order-info {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
}

.info-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e5e5;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item .label {
  color: #666;
  font-size: 13px;
  min-width: 70px;
}

.info-item .value {
  color: #333;
  font-size: 13px;
  font-weight: 500;
}

.status-tag {
  color: #ff5000 !important;
}

.sub-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.products-section {
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e5e5;
  margin-bottom: 16px;
}

.product-list {
  max-height: 200px;
  overflow-y: auto;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 6px;
  margin-bottom: 8px;
}

.product-item:last-child {
  margin-bottom: 0;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-spec {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.product-price {
  text-align: right;
  min-width: 100px;
}

.product-price .price {
  color: #ff5000;
  font-size: 14px;
  font-weight: 600;
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
  min-width: 80px;
  text-align: right;
}

.price-summary {
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e5e5;
  margin-bottom: 16px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.summary-row .label {
  color: #666;
  font-size: 13px;
}

.summary-row .value {
  color: #333;
  font-size: 13px;
  font-weight: 500;
}

.summary-row .value.free {
  color: #67c23a;
}

.summary-row.total {
  padding-top: 12px;
  border-top: 1px dashed #e5e5e5;
  margin-top: 4px;
}

.summary-row .total-price {
  color: #ff5000;
  font-size: 18px;
  font-weight: 700;
}

.address-section .address-info {
  background: #fff;
  padding: 12px;
  border-radius: 6px;
}

.address-section .info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
}

.address-section .info-row:last-child {
  margin-bottom: 0;
}

.address-section .label {
  color: #666;
  font-size: 13px;
  min-width: 70px;
  flex-shrink: 0;
}

.address-section .value {
  color: #333;
  font-size: 13px;
  line-height: 1.5;
}

.no-address {
  background: #fff;
  padding: 16px;
  border-radius: 6px;
  text-align: center;
}

/* 支付方式 */
.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.payment-method:hover {
  background: #fff;
  border-color: #ff5000;
}

.payment-method.active {
  background: #fff7f5;
  border-color: #ff5000;
}

.method-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.method-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.method-info {
  flex: 1;
  min-width: 0;
}

.method-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.method-desc {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.method-check {
  color: #ff5000;
  font-size: 20px;
}

.payment-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 16px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 6px;
  font-size: 12px;
  color: #409eff;
}

/* 确认弹窗 */
.confirm-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20px 0;
}

.confirm-icon {
  font-size: 60px;
  color: #ff9900;
  margin-bottom: 16px;
}

.confirm-text p {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
}

.confirm-text strong {
  color: #333;
}

.confirm-text .confirm-question {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-top: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.confirm-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
}

/* 地址选择器 */
.address-selector {
  max-height: 400px;
  overflow-y: auto;
}

.address-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border: 2px solid #e5e5e5;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #ff5000;
  background: #fff7f5;
}

.address-item.active {
  border-color: #ff5000;
  background: #fff7f5;
}

.address-content {
  flex: 1;
}

.address-header {
  margin-bottom: 8px;
}

.address-header .name {
  font-weight: 600;
  color: #333;
  margin-right: 12px;
}

.address-header .phone {
  color: #666;
}

.address-detail {
  color: #666;
  font-size: 13px;
  line-height: 1.5;
}

.address-check {
  color: #ff5000;
  font-size: 24px;
}

.no-address-tip {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

/* 支付方式禁用状态 */
.payment-method.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.payment-method.disabled:hover {
  background: #f8f9fa;
  border-color: transparent;
}

.method-unavailable {
  margin-left: auto;
}
</style>
