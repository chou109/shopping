<template>
  <div v-loading="loading" :style="'height:' + height">
    <iframe 
      :src="url" 
      frameborder="no" 
      style="width: 100%; height: 100%" 
      scrolling="auto" />
  </div>
</template>

<script setup>
const props = defineProps({
  src: {
    type: String,
    required: true
  }
})

const height = ref(document.documentElement.clientHeight - 94.5 + "px;")
const loading = ref(true)
const url = computed(() => props.src)
let loadingTimer

const updateHeight = () => {
  height.value = document.documentElement.clientHeight - 94.5 + 'px;'
}

onMounted(() => {
  loadingTimer = window.setTimeout(() => {
    loading.value = false;
  }, 300);
  window.addEventListener('resize', updateHeight)
})

onBeforeUnmount(() => {
  window.clearTimeout(loadingTimer)
  window.removeEventListener('resize', updateHeight)
})
</script>
