<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script setup>
const url = ref('https://gitee.com/y_project/neuedu-Vue');

function goto() {
  window.open(url.value)
}
</script>
