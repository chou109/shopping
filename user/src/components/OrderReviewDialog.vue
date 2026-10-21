<template>
  <el-dialog v-model="visible" title="订单评价" width="650px" :close-on-click-modal="false">
    <div class="review-dialog-content">
      <div v-for="item in reviewItems" :key="item.productId" class="review-item">
        <div class="review-product">
          <img :src="getProductImageUrl(item.productMainImage)" :alt="item.productName" class="product-img" @error="handleImageError" />
          <div class="product-name">{{ item.productName }}</div>
        </div>
        <div class="review-score">
          <span class="label">商品评分：</span>
          <el-rate v-model="item.score" :texts="['很差', '较差', '一般', '较好', '非常好']" show-text />
        </div>
        <div class="review-content">
          <el-input v-model="item.content" type="textarea" :rows="3"
            placeholder="请分享您的购物体验，帮助其他买家做出更好的选择~" maxlength="500" show-word-limit />
        </div>
        <div class="review-anonymous">
          <el-checkbox v-model="item.isAnonymous">匿名评价</el-checkbox>
        </div>
      </div>
      <el-empty v-if="reviewItems.length === 0" description="订单商品信息不完整，暂时无法评价" />
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" :disabled="reviewItems.length === 0" @click="submit">提交评价</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getProductImageUrl, handleImageError } from '@/utils/image'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null },
  loading: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'submit'])
const reviewItems = ref([])
const visible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

watch(() => props.modelValue, value => {
  if (value) {
    reviewItems.value = (props.order?.orderDetails || []).map(item => ({
      productId: item.productId,
      productName: item.productName,
      productMainImage: item.productMainImage,
      score: 5,
      content: '',
      isAnonymous: false
    }))
  }
})

const submit = () => {
  if (reviewItems.value.some(item => !item.score)) {
    ElMessage.warning('请为所有商品评分')
    return
  }
  emit('submit', reviewItems.value.map(item => ({ ...item })))
}
</script>

<style scoped>
.review-dialog-content { max-height: 500px; overflow-y: auto; }
.review-item { margin-bottom: 16px; padding: 20px; background: #fafafa; border-radius: 8px; }
.review-item:last-child { margin-bottom: 0; }
.review-product { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; padding-bottom: 16px; border-bottom: 1px solid #eee; }
.review-product .product-img { width: 60px; height: 60px; border: 1px solid #eee; border-radius: 6px; object-fit: cover; }
.review-product .product-name { flex: 1; color: #333; font-size: 14px; font-weight: 500; }
.review-score { display: flex; align-items: center; margin-bottom: 16px; }
.review-score .label { margin-right: 12px; color: #666; font-size: 14px; }
.review-content { margin-bottom: 12px; }
.review-anonymous { display: flex; justify-content: flex-end; }
</style>
