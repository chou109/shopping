<template>
  <el-dialog
    :model-value="payVisible"
    title="订单支付"
    width="400px"
    @update:model-value="emit('update:payVisible', $event)"
  >
    <div class="pay-dialog-content">
      <p>订单金额：<strong>¥{{ formatPrice(order?.payment) }}</strong></p>
      <p>支付方式：余额支付</p>
    </div>
    <template #footer>
      <el-button @click="emit('update:payVisible', false)">取消</el-button>
      <el-button type="primary" :loading="paying" @click="emit('confirm-pay')">确认支付</el-button>
    </template>
  </el-dialog>

  <el-dialog
    :model-value="cancelVisible"
    title="取消订单"
    width="400px"
    @update:model-value="emit('update:cancelVisible', $event)"
  >
    <el-form>
      <el-form-item label="取消原因">
        <el-input
          :model-value="cancelReason"
          type="textarea"
          :rows="3"
          placeholder="请输入取消原因"
          @update:model-value="emit('update:cancelReason', $event)"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:cancelVisible', false)">返回</el-button>
      <el-button type="danger" :loading="cancelling" @click="emit('confirm-cancel')">确认取消</el-button>
    </template>
  </el-dialog>

  <el-dialog
    :model-value="logisticsVisible"
    title="物流信息"
    width="480px"
    @update:model-value="emit('update:logisticsVisible', $event)"
  >
    <el-descriptions v-if="logisticsOrder" :column="1" border>
      <el-descriptions-item label="订单编号">{{ logisticsOrder.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="订单状态">{{ logisticsOrder.statusDesc }}</el-descriptions-item>
      <el-descriptions-item label="发货时间">
        {{ logisticsOrder.shipTime ? formatTime(logisticsOrder.shipTime) : '暂无发货记录' }}
      </el-descriptions-item>
    </el-descriptions>
    <el-alert
      title="暂无物流轨迹信息，商家尚未录入承运商和运单号。"
      type="info"
      :closable="false"
      show-icon
      style="margin-top: 16px"
    />
    <template #footer>
      <el-button @click="emit('update:logisticsVisible', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {
  formatOrderPrice as formatPrice,
  formatOrderTime as formatTime
} from '@/utils/orderDisplay.mjs'

defineProps({
  payVisible: Boolean,
  cancelVisible: Boolean,
  logisticsVisible: Boolean,
  order: {
    type: Object,
    default: null
  },
  logisticsOrder: {
    type: Object,
    default: null
  },
  cancelReason: {
    type: String,
    default: ''
  },
  paying: Boolean,
  cancelling: Boolean
})

const emit = defineEmits([
  'update:payVisible', 'update:cancelVisible', 'update:logisticsVisible',
  'update:cancelReason', 'confirm-pay', 'confirm-cancel'
])
</script>

<style scoped>
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
