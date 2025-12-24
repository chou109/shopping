<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
/* ========== 全局基础样式 ========== */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* ========== 滚动优化 ========== */
html {
  scroll-behavior: smooth;
  font-size: 16px; /* 基准字体大小 */
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
  transition: background 0.3s ease;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Firefox 滚动条 */
* {
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 #f1f1f1;
}

/* ========== 图片和媒体响应式 ========== */
img, video, iframe {
  max-width: 100%;
  height: auto;
}

img {
  object-fit: cover; /* 防止图片变形 */
}

/* 响应式视频容器 */
.video-responsive {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 56.25%; /* 16:9 比例 */
}

.video-responsive iframe,
.video-responsive video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 0;
}

/* ========== 响应式字体大小 ========== */
@media (min-width: 1920px) {
  html {
    font-size: 18px;
  }
}

@media (max-width: 1200px) {
  html {
    font-size: 15px;
  }
}

@media (max-width: 768px) {
  html {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  html {
    font-size: 13px;
  }
}

/* ========== 响应式断点工具类 ========== */
.container-fluid {
  width: 100%;
  padding-right: 15px;
  padding-left: 15px;
  margin-right: auto;
  margin-left: auto;
}

@media (min-width: 576px) {
  .container-fluid {
    max-width: 540px;
  }
}

@media (min-width: 768px) {
  .container-fluid {
    max-width: 720px;
  }
}

@media (min-width: 992px) {
  .container-fluid {
    max-width: 960px;
  }
}

@media (min-width: 1200px) {
  .container-fluid {
    max-width: 1140px;
  }
}

@media (min-width: 1400px) {
  .container-fluid {
    max-width: 1320px;
  }
}

/* 响应式显示/隐藏 */
.desktop-only {
  display: block;
}

.mobile-only {
  display: none;
}

@media (max-width: 768px) {
  .desktop-only {
    display: none !important;
  }
  
  .mobile-only {
    display: block !important;
  }
}

/* 移动端优化 */
@media (max-width: 768px) {
  body {
    -webkit-overflow-scrolling: touch;
  }
}

* {
  -webkit-tap-highlight-color: transparent; /* 防止移动端点击高亮 */
}
</style>

<style scoped>
/* App.vue 组件自身的样式 */
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  line-height: 1.6;
  color: #333;
  background-color: #fff;
}
</style>