<template>
  <div class="order-route-redirect" role="status" aria-live="polite">
    正在打开订单管理…
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  router.replace('/order/info')
})
</script>
