<template>
  <el-dialog v-model="visible" title="取消订单并退款" width="600px">
    <el-alert title="仅待发货订单可操作；确认后订单会立即取消，款项全额退回账户余额。" type="warning" :closable="false" show-icon />
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="refund-form">
      <el-form-item label="商品信息">
        <div class="refund-product-info">
          <div v-for="(detail, index) in order?.orderDetails || []" :key="detail.id || detail.productId || index" class="refund-product-item">
            <div class="product-image">
              <img :src="getProductImageUrl(detail.productMainImage)" :alt="detail.productName" @error="handleImageError" />
            </div>
            <div class="product-info">
              <div class="product-name">{{ detail.productName }}</div>
              <div v-if="detail.specifications" class="product-spec">{{ detail.specifications }}</div>
              <div class="product-quantity">数量：{{ detail.quantity }}</div>
            </div>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="取消原因" prop="reason">
        <el-select v-model="form.reason" placeholder="请选择取消原因" style="width: 100%">
          <el-option label="不想要了" value="不想要了" />
          <el-option label="重量/数量/大小等描述不符" value="重量/数量/大小等描述不符" />
          <el-option label="图片/品种/口味/标签等描述不符" value="图片/品种/口味/标签等描述不符" />
          <el-option label="包装/商品破损" value="包装/商品破损" />
          <el-option label="少件/漏发" value="少件/漏发" />
          <el-option label="商品变质/发霉/有异物" value="商品变质/发霉/有异物" />
          <el-option label="生产日期/保质期与商品描述不符" value="生产日期/保质期与商品描述不符" />
          <el-option label="质量问题" value="质量问题" />
          <el-option label="商家发错货" value="商家发错货" />
          <el-option label="假冒品牌" value="假冒品牌" />
          <el-option label="其他原因" value="其他原因" />
        </el-select>
      </el-form-item>
      <el-form-item label="退款金额">
        <div class="refund-amount">¥{{ formatPrice(order?.payment) }}</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">返回</el-button>
      <el-button type="danger" :loading="loading" @click="submit">确认取消并退款</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { getProductImageUrl, handleImageError } from '@/utils/image'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'submit'])
const formRef = ref()
const form = reactive({ reason: '' })
const rules = {
  reason: [{ required: true, message: '请选择取消原因', trigger: 'change' }]
}
const visible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

watch(() => props.modelValue, value => {
  if (value) form.reason = ''
})

const formatPrice = value => (Number(value) || 0).toFixed(2)
const submit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    emit('submit', form.reason)
  } catch {
    // Element Plus 已显示具体校验错误。
  }
}
</script>

<style scoped>
.refund-form { margin-top: 18px; }
.refund-product-info { max-height: 200px; overflow-y: auto; padding: 10px; background: #fafafa; border-radius: 6px; }
.refund-product-item { display: flex; align-items: center; gap: 16px; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.refund-product-item:last-child { border-bottom: none; }
.product-image { width: 60px; height: 60px; flex-shrink: 0; border-radius: 4px; overflow: hidden; }
.product-image img { width: 100%; height: 100%; object-fit: cover; }
.product-info { flex: 1; min-width: 0; }
.product-name { margin-bottom: 4px; overflow: hidden; color: #333; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.product-spec { margin-bottom: 4px; color: #999; font-size: 12px; }
.product-quantity { color: #666; font-size: 12px; }
.refund-amount { padding: 10px 0; color: #ff5000; font-size: 20px; font-weight: 700; }
</style>
