<template>
  <div class="breadcrumb-wrapper">
    <el-breadcrumb class="app-breadcrumb" :separator-icon="ArrowRight">
      <transition-group name="breadcrumb">
        <el-breadcrumb-item v-for="(item,index) in levelList" :key="item.path">
          <template v-if="index === 0">
            <button type="button" class="home-link" :aria-label="`返回${item.meta.title}`" @click="handleLink(item)">
              <el-icon class="home-icon"><HomeFilled /></el-icon>
              <span>{{ item.meta.title }}</span>
            </button>
          </template>
          <template v-else-if="item.redirect==='noRedirect'||index==levelList.length-1">
            <span class="current-page">{{ item.meta.title }}</span>
          </template>
          <template v-else>
            <button type="button" class="breadcrumb-link" @click="handleLink(item)">{{ item.meta.title }}</button>
          </template>
        </el-breadcrumb-item>
      </transition-group>
    </el-breadcrumb>
  </div>
</template>

<script>
import { HomeFilled, ArrowRight } from '@element-plus/icons-vue'

export default {
  components: {
    HomeFilled,
    ArrowRight
  },
  data() {
    return {
      levelList: null,
      ArrowRight
    }
  },
  watch: {
    $route(route) {
      // if you go to the redirect page, do not update the breadcrumbs
      if (route.path.startsWith('/redirect/')) {
        return
      }
      this.getBreadcrumb()
    }
  },
  created() {
    this.getBreadcrumb()
  },
  methods: {
    getBreadcrumb() {
      // only show routes with meta.title
      let matched = this.$route.matched.filter(item => item.meta && item.meta.title)
      const first = matched[0]

      if (!this.isDashboard(first)) {
        matched = [{ path: '/index', meta: { title: '数据中心' }}].concat(matched)
      }

      this.levelList = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
    },
    isDashboard(route) {
      const name = route && route.name
      if (!name) {
        return false
      }
      return name.trim() === '数据中心'
    },
    handleLink(item) {
      const { redirect, path } = item
      if (redirect) {
        this.$router.push(redirect)
        return
      }
      this.$router.push(path)
    }
  }
}
</script>

<style lang="scss" scoped>
.breadcrumb-wrapper {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin: 0 0 16px 0;
}

.app-breadcrumb.el-breadcrumb {
  display: flex;
  align-items: center;
  font-size: 14px;
  line-height: 1.5;

  :deep(.el-breadcrumb__separator) {
    color: #c0c4cc;
    margin: 0 10px;
  }

  :deep(.el-breadcrumb__inner) {
    display: flex;
    align-items: center;
  }

  .home-link {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #606266;
    cursor: pointer;
    padding: 0;
    border: 0;
    background: transparent;
    font: inherit;
    padding: 4px 10px;
    border-radius: 6px;
    transition: all 0.25s ease;
    font-weight: 500;

    .home-icon {
      font-size: 16px;
      color: #ff6b35;
    }

    &:hover {
      background: rgba(255, 107, 53, 0.08);
      color: #ff6b35;
    }
  }

  .breadcrumb-link {
    color: #606266;
    cursor: pointer;
    padding: 0;
    border: 0;
    background: transparent;
    font: inherit;
    padding: 4px 10px;
    border-radius: 6px;
    transition: all 0.25s ease;
    text-decoration: none;
    font-weight: 500;

    &:hover {
      background: rgba(255, 107, 53, 0.08);
      color: #ff6b35;
    }
  }

  .current-page {
    color: #303133;
    font-weight: 600;
    padding: 4px 10px;
    background: rgba(255, 107, 53, 0.1);
    border-radius: 6px;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 14px;
      background: #ff6b35;
      border-radius: 2px;
    }
  }
}

/* 动画 */
.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.3s ease;
}

.breadcrumb-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.breadcrumb-leave-to {
  opacity: 0;
  transform: translateX(10px);
}
</style>
