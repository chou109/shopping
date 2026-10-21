<script setup>
import { RouterView } from 'vue-router';
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

</script>

<template>

  <el-config-provider :locale="zhCn">
    <div style="width: 1800px;margin: 0 auto;">
      <RouterView></RouterView>
    </div>
  </el-config-provider>
</template>

<style scoped></style>
