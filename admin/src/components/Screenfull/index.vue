<template>
  <div>
    <button
      type="button"
      class="screenfull-control"
      :aria-label="isFullscreen ? '退出全屏' : '进入全屏'"
      :aria-pressed="isFullscreen"
      @click="toggle"
    >
      <svg-icon :icon-class="isFullscreen ? 'exit-fullscreen' : 'fullscreen'" aria-hidden="true" />
    </button>
  </div>
</template>

<script setup>
import { useFullscreen } from '@vueuse/core'

const { isFullscreen, enter, exit, toggle } = useFullscreen();
</script>

<style lang='scss' scoped>
.screenfull-svg {
  display: inline-block;
  cursor: pointer;
  fill: #5a5e66;
  width: 20px;
  height: 20px;
  vertical-align: 10px;
}
.screenfull-control {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  color: inherit;
  background: transparent;
  cursor: pointer;
}
.screenfull-control:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 3px;
}
</style>
