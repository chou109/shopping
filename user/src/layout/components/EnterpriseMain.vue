<template>
  <div class="main">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/index' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: lastBreadcrumbPath }"> {{ lastBreadcrumbText }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div style="margin-top: 20px">
      <el-container>
        <aside class="aside">
          <el-menu background-color="#17C2AA" text-color="#FFFFFF" active-text-color="#17C2AA">
            <el-menu-item v-for="({icon, label, path}, index) in pathList" :key="index" @click="jumpTo(path, label)">
              <i :class="icon"></i>
              <span slot="title">{{ label }}</span>
            </el-menu-item>
          </el-menu>
        </aside>

        <el-main>
          <el-scrollbar wrap-class="scrollbar-wrapper">
            <app-main/>
          </el-scrollbar>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script>
import { AppMain } from '@/layout/components/index'

export default {
  name: 'EnterpriseMain',
  components: { AppMain },
  data() {
    return {
      menuCollapseState: false,
      lastBreadcrumbPath: '',
      lastBreadcrumbText: '',

      pathList: [
        {
          label: '企业信息',
          path: '/enterprise/enterpriseInfo',
          icon: 'el-icon-menu',
        },
        {
          label: '采购公告',
          path: '/enterprise/bidding',
          icon: 'el-icon-document',
        },
        {
          label: '参标项目',
          path: '/enterprise/myBidding',
          icon: 'el-icon-setting'
        }
      ]
    }
  },
  methods: {
    jumpTo(path, info) {
      this.lastBreadcrumbPath = path
      this.lastBreadcrumbText = info
      this.$router.push(path)
    }
  }
}
</script>

<style lang="scss" scoped>

aside {
  background: #17C2AA;
  box-shadow: 4px 0 4px 0 rgba(0, 0, 0, 0.08);
  height: 80vh;
  width: 195px;
}

.el-main {
  padding: 0;
}

::v-deep .el-menu {
  border-right: 0;

  .el-menu-item {
    color: #FFFFFF !important;

    i {
      color: #FFFFFF !important;
    }
  }

  .el-menu-item.is-active {
    // color: #17C2AA !important;
    // background-color: #FFFFFF !important;
    border-radius: 6px;

    i {
      color: #17C2AA !important;
    }
  }
}

.aside {
  width: 195px;
}

@media screen and (max-width: 540px) {
  .aside {
    width: 50px;
    padding: 0;
  }

  ::v-deep .el-menu-item {
    padding-left: 15px !important;

    > span {
      display: none;
    }
  }
}
</style>
