<template>
  <el-dialog
    :model-value="modelValue"
    title="订单详情"
    width="700px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div v-if="order" class="order-detail-dialog">
      <div class="detail-section">
        <div class="detail-row">
          <span class="label">订单编号：</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="detail-row">
          <span class="label">订单状态：</span>
          <span class="value status" :class="getStatusClass(order.status)">{{ order.statusDesc }}</span>
        </div>
        <div class="detail-row">
          <span class="label">创建时间：</span>
          <span class="value">{{ formatTime(order.createTime) }}</span>
        </div>
        <div v-if="order.paymentTime" class="detail-row">
          <span class="label">支付时间：</span>
          <span class="value">{{ formatTime(order.paymentTime) }}</span>
        </div>
        <div v-if="order.paymentTypeDesc" class="detail-row">
          <span class="label">支付方式：</span>
          <span class="value">{{ order.paymentTypeDesc }}</span>
        </div>
      </div>

      <div v-if="order.receiveName" class="detail-section">
        <div class="section-title">收货信息</div>
        <div class="detail-row">
          <span class="label">收货人：</span>
          <span class="value">{{ order.receiveName }} {{ order.receivePhone }}</span>
        </div>
        <div class="detail-row">
          <span class="label">收货地址：</span>
          <span class="value">{{ order.province }}{{ order.city }}{{ order.area }}{{ order.street }}</span>
        </div>
      </div>

      <div class="detail-section">
        <div class="section-title">商品信息</div>
        <div class="detail-products">
          <div v-for="(item, index) in order.orderDetails" :key="index" class="detail-product-item">
            <img
              :src="getProductImageUrl(item.productMainImage)"
              :alt="item.productName || '商品图片'"
              class="product-img"
            />
            <div class="product-info">
              <div class="product-name">{{ item.productName }}</div>
              <div v-if="item.specifications" class="product-spec">{{ item.specifications }}</div>
            </div>
            <div class="product-price">¥{{ formatPrice(item.currentUnitPrice) }} x {{ item.quantity }}</div>
            <div class="product-total">¥{{ formatPrice(item.totalPrice) }}</div>
          </div>
        </div>
      </div>

      <div class="detail-section total-section">
        <div class="detail-row">
          <span class="label">商品总价：</span>
          <span class="value">¥{{ formatPrice(order.payment) }}</span>
        </div>
        <div class="detail-row">
          <span class="label">运费：</span>
          <span class="value">¥{{ formatPrice(order.postage || 0) }}</span>
        </div>
        <div class="detail-row total-row">
          <span class="label">实付款：</span>
          <span class="value total-price">¥{{ formatPrice(order.payment) }}</span>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { getProductImageUrl } from '@/utils/image'
import {
  formatOrderPrice as formatPrice,
  formatOrderTime as formatTime,
  getOrderStatusClass as getStatusClass
} from '@/utils/orderDisplay.mjs'

defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  order: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])
</script>

<style scoped>
.order-detail-dialog {
  max-height: 500px;
  overflow-y: auto;
}

.detail-section {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-section:last-child {
  border-bottom: none;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.detail-row {
  display: flex;
  margin-bottom: 8px;
}

.detail-row .label {
  width: 90px;
  color: #999;
  flex-shrink: 0;
}

.detail-row .value {
  flex: 1;
  color: #333;
}

.detail-row .value.status {
  font-weight: 600;
}

.detail-products {
  background: #fafafa;
  border-radius: 8px;
  padding: 12px;
}

.detail-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-product-item:last-child {
  border-bottom: none;
}

.detail-product-item .product-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.detail-product-item .product-info {
  flex: 1;
  min-width: 0;
}

.detail-product-item .product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.detail-product-item .product-spec {
  font-size: 12px;
  color: #999;
}

.detail-product-item .product-price {
  color: #666;
  font-size: 14px;
}

.detail-product-item .product-total {
  color: #ff5000;
  font-size: 14px;
  font-weight: 600;
  min-width: 80px;
  text-align: right;
}

.total-section {
  background: #fff9f5;
  padding: 16px;
  border-radius: 8px;
}

.total-row {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #f0f0f0;
}

.total-row .label {
  font-weight: 600;
}

.total-row .total-price {
  color: #ff5000;
  font-size: 20px;
  font-weight: 700;
}
</style>
