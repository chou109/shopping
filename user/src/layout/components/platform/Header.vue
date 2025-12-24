<template>
  <div style="display: flex; justify-content: space-between;align-items: center;" class="header">
    <router-link to="/">
      <div class="title-wrapper">
        <img src="@/assets/logo/logo.png" class="logo" /><span class="title">网购商城</span>
      </div>
    </router-link>
    <el-tabs v-model="activeName" class="tabs" :stretch="true" @tab-click="handleTabChange">
      <el-tab-pane label="首页" name="index" />
      <el-tab-pane label="新闻公告" name="news" />
      <el-tab-pane label="用户反馈" name="feedback" />
    </el-tabs>
    <div class="login">
      <div @click="handleClickUserInfo">
        <img style="width: 32px;height: 32px;" src="@/assets/images/avatar.png" alt="">
      </div>

      <div class="user">
        <span v-if="name === ''" @click="handleLogin">请登录</span>
        <el-dropdown v-else>
          <span @click="handleClickUserInfo">{{ name }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item divided @click.native="logout">
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-icon>
          <CaretBottom />
        </el-icon>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Header',
  data() {
    return {
      activeName: 'index',
      routers: ['/index', '/news/list', '/feedback/list', '/volunteer/list']
    }
  },
  methods: {
    async logout() {
      this.$confirm('是否退出登录？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('LogOut').then(() => {
          location.href = '/login'
        })
      })
    },

    handleTabChange(tabItem) {
      this.$router.push(this.routers[tabItem.index])
    },
    handleClickUserInfo() {
      // this.$router.push('/enterprise/enterpriseInfo')
    },
    handleLogin() {
      this.$router.push('/login')
    },
    pathAction(path) {
      if (path.includes('index')) {
        this.activeName = 'index'
      } else if (path.includes('news')) {
        this.activeName = 'news'
      } else if (path.includes('feedback')) {
        this.activeName = 'feedback'
      } else if (path.includes('volunteer')) {
        this.activeName = 'volunteer'
      }
    }
  },
  computed: {
    ...mapGetters(['name', 'avatar'])
  },
  watch: {
    $route(to, from) {
      console.log(`Route changed from ${from.fullPath} to ${to.fullPath}`);
      this.pathAction(to.fullPath);
    }
  },
  created() {
    this.pathAction(this.$route.fullPath);
  }
}
</script>

<style lang="scss" scoped>
.header {
  display: flex;
  padding: 10px 0 10px 30px;

  .title-wrapper {
    white-space: nowrap;
    width: 188px;
  }

  .logo {
    position: relative;
    width: 36px;
    height: 36px;
    opacity: 1;
  }

  .title {
    height: 34px;
    opacity: 1;
    margin-left: 10px;
    font-family: MicrosoftYaHei;
    font-size: 24px;
    font-weight: normal;
    letter-spacing: 0em;
    position: relative;
    top: -8px;
    color: #ff5500;
  }

  // .tabs {
  //   margin-left: 20%;
  // }

  :deep(.el-tabs__nav-wrap)::after {
    background-color: #FFFFFF;
  }

  :deep(.el-tabs__active-bar) {
    background: #13B69F;
    color: #13B69F;
    height: 3px !important;
    margin-top: 5px;
  }

  :deep(.el-tabs__item.is-active) {
    color: #13B69F;
    height: 46px;
  }

  :deep(.el-tabs__item) {
    font-family: MicrosoftYaHei, sans-serif;
    font-size: 16px;
    font-weight: normal;
    letter-spacing: 0em;
    color: #464646;
  }

  .login {
    // position: absolute;
    // right: 0;
    display: flex;
    justify-content: center;
    align-content: center;
    align-items: center;
    margin-right: 30px;
    cursor: pointer;

    .user {
      margin-left: 14px;
    }
  }
}

:deep(.el-tabs__header) {
  margin: 0;
}

@media screen and (max-width: 540px) {
  .header {
    .title-wrapper {
      white-space: nowrap;
      text-align: left;
    }

    .tabs {
      margin-left: 0;
    }

    .login {
      .user {
        display: none;
      }
    }
  }
}
</style>
