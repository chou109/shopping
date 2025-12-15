<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script setup>
const url = ref('http://doc.neuedu.vip/neuedu-vue');

function goto() {
  window.open(url.value)
}
</script>