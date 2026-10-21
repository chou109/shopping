<template>
  <div class="icon-body">
    <el-input
      v-model="iconName"
      style="position: relative;"
      clearable
      placeholder="请输入图标名称"
      @clear="filterIcons"
      @input="filterIcons"
    >
      <template #suffix><el-icon><Search /></el-icon></template>
    </el-input>
    <div class="icon-list">
      <button type="button" v-for="(item, index) in iconList" :key="index" :aria-label="`选择图标：${item}`" @click="selectedIcon(item)">
        <svg-icon :icon-class="item" style="height: 30px;width: 16px;" />
        <span>{{ item }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import icons from './requireIcons'

const iconName = ref('');
const iconList = ref(icons);
const emit = defineEmits(['selected']);

function filterIcons() {
  iconList.value = icons
  if (iconName.value) {
    iconList.value = icons.filter(item => item.indexOf(iconName.value) !== -1)
  }
}

function selectedIcon(name) {
  emit('selected', name)
  document.body.click()
}

function reset() {
  iconName.value = ''
  iconList.value = icons
}

defineExpose({
  reset
})
</script>

<style lang='scss' scoped>
.icon-body {
  width: 100%;
  padding: 10px;
  .icon-list {
    height: 200px;
    overflow-y: scroll;
    button {
      display: flex;
      align-items: center;
      height: 30px;
      line-height: 30px;
      margin-bottom: -5px;
      padding: 0;
      border: 0;
      background: transparent;
      color: inherit;
      cursor: pointer;
      width: 33%;
      float: left;
      text-align: left;
      &:focus-visible {
        outline: 2px solid var(--el-color-primary);
        outline-offset: -2px;
      }
    }
    span {
      display: inline-block;
      vertical-align: -0.15em;
      fill: currentColor;
      overflow: hidden;
    }
  }
}
</style>
