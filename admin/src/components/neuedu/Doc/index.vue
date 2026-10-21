<template>
  <a :href="url" target="_blank" rel="noopener noreferrer" aria-label="打开项目使用文档">
    <svg-icon icon-class="question" aria-hidden="true" />
  </a>
</template>

<script setup>
const url = 'http://doc.neuedu.vip/neuedu-vue';
</script>
