<template>
  <el-config-provider :locale="zhCn" :size="appStore.size">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import useAppStore from '@/store/modules/app'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { handleThemeStyle } from '@/utils/theme'

const appStore = useAppStore()

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme)
  })
})
</script>
