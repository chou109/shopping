<template>
  <a :href="url" target="_blank" rel="noopener noreferrer" aria-label="打开项目源码仓库">
    <svg-icon icon-class="github" aria-hidden="true" />
  </a>
</template>

<script setup>
const url = 'https://gitee.com/y_project/neuedu-Vue';
</script>
