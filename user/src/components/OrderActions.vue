<template>
  <div class="order-actions">
    <template v-for="action in actions" :key="action.id">
      <span v-if="action.id === 'blind-box-refund-not-allowed'" class="no-refund-tip">
        {{ action.label }}
      </span>
      <el-button
        v-else
        size="small"
        :type="action.type"
        :loading="(action.id === 'delete' && deletingOrderIds.has(order.id)) || (action.id === 'confirm-receipt' && receiptSubmitting && receiptActionOrderNo === order.orderNo)"
        :disabled="action.id === 'confirm-receipt' && receiptActionOrderNo !== null && receiptActionOrderNo !== order.orderNo"
        @click="emit(action.event, order)"
      >
        {{ action.label }}
      </el-button>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { getOrderActions } from '@/utils/orderActions.mjs'

const props = defineProps({
  order: {
    type: Object,
    required: true
  },
  receiptActionOrderNo: {
    type: [String, Number],
    default: null
  },
  receiptSubmitting: {
    type: Boolean,
    default: false
  },
  deletingOrderIds: {
    type: Set,
    default: () => new Set()
  }
})

const emit = defineEmits([
  'cancel', 'pay', 'refund', 'confirm-receipt', 'logistics', 'review', 'detail', 'delete'
])
const actions = computed(() => getOrderActions(props.order))
</script>

<style scoped>
.order-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.no-refund-tip {
  color: #909399;
  font-size: 12px;
  padding: 5px 10px;
  background: #f4f4f5;
  border-radius: 4px;
}
</style>
