<template>
  <div class="header">
    <img src="@/assets/logo/logo.png" class="logo" />
    <div class="title">网购商城</div>
  </div>
  <div class="login">
    <!-- <img src="@/assets/logo/OIP-C.webp" class="sub-bg" /> -->
    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">欢迎登录</h3>
      <el-form-item prop="username">
        <el-input v-model="loginForm.username" type="text" auto-complete="off" placeholder="账号">
          <template #prefix>
            <el-icon class="el-input__icon">
              <UserFilled />
            </el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" auto-complete="off" placeholder="密码" @keyup.enter.
          native="handleLogin">
          <template #prefix>
            <el-icon class="el-input__icon">
              <Lock />
            </el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code">
        <el-input v-model="loginForm.code" auto-complete="off" placeholder="验证码" style="width: 63%" @keyup.enter.
          native="handleLogin">
          <template #prefix>
            <el-icon class="el-input__icon">
              <ElementPlus />
            </el-icon>
          </template>
        </el-input>
        <div class="login-code">
          <button type="button" class="login-code-button" aria-label="刷新验证码" @click="getCode">
            <img :src="codeUrl" alt="验证码" class="login-code-img" />
          </button>
        </div>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button :loading="loading" size="medium" type="primary" round style="width:100%;" @click="handleLogin">
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button><br><br>
        <el-button :loading="loading" size="medium" type="primary" round style="width:100%;"
          @click="this.$router.push('/register')">
          <span>注册</span>
        </el-button>
      </el-form-item>

      <!-- QQ登录按钮 -->
      <el-form-item style="width:100%;">
        <el-button size="medium" round style="width:100%; background: #12B7F5; border-color: #12B7F5; color: #fff;"
          @click="handleQQLogin">
          <svg style="width:18px;height:18px;vertical-align:middle;margin-right:8px;" viewBox="0 0 24 24"
            fill="currentColor">
            <path
              d="M12.003 2c-2.265 0-6.29 1.364-6.29 7.325v1.195S3.55 14.96 3.55 17.474c0 .665.17 1.025.281 1.025.114 0 .902-.484 1.748-2.072 0 0-.18 2.197 1.904 3.967 0 0-1.77.495-1.77 1.182 0 .686 4.078.43 6.29.43 2.212 0 6.29.256 6.29-.43 0-.687-1.77-1.182-1.77-1.182 2.085-1.77 1.904-3.967 1.904-3.967.846 1.588 1.634 2.072 1.748 2.072.111 0 .281-.36.281-1.025 0-2.514-2.162-6.954-2.162-6.954V9.325C18.294 3.364 14.27 2 12.003 2z" />
          </svg>
          <span>QQ登录</span>
        </el-button>
      </el-form-item>
    </el-form>

  </div>
</template>

<script>
import { getCodeImg } from "@/api/login";
import Cookies from "js-cookie";
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    return {
      codeUrl: "",
      cookiePassword: "",
      loginForm: {
        username: "",
        password: "",
        rememberMe: false,
        code: "",
        uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "用户名不能为空" }
        ],
        password: [
          { required: true, trigger: "blur", message: "密码不能为空" }
        ],
        code: [{ required: true, trigger: "change", message: "验证码不能为空" }]
      },
      loading: false,
      redirect: undefined
    };
  },
  watch: {
    $route: {
      handler: function (route) {
        // 记录redirect参数，但如果是注册页就忽略
        this.redirect = route.query && route.query.redirect;
        // 如果redirect是注册页，设为undefined，这样登录后跳转到首页
        if (this.redirect === '/register') {
          this.redirect = undefined;
        }
      },
      immediate: true
    }
  },
  created() {
    this.getCode();
    this.getCookie();
  },
  methods: {
    handleQQLogin() {
      // 跳转到QQ登录页面
      const qqLoginUrl = 'https://qq.wch666.com/api/qq.php?token=4922a38240ef9fd21bfc5dd47bff232e&msg=user_login&display=pc';
      window.location.href = qqLoginUrl;
    },
    getCode() {
      getCodeImg().then(res => {
        this.codeUrl = "data:image/gif;base64," + res.img;
        this.loginForm.uuid = res.uuid;
      });
    },
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true;
          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }
          this.$store.dispatch("Login", this.loginForm).then(() => {
            this.$store.dispatch("GetInfo").then(() => {
              // 处理跳转逻辑
              if (this.redirect && this.redirect !== '/register') {
                // 跳转到之前想访问的页面
                this.$router.push({ path: this.redirect });
              } else {
                // 默认跳转到首页
                this.$router.push({ path: "/index" });
              }
            });
          }).catch(() => {
            this.loading = false;
            this.getCode();
          });
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.header {
  width: 100%;
  height: 60px;
  display: flex;
  align-items: center;

  .logo {
    width: 36px;
    height: 36px;
    margin-left: 30px;
    margin-right: 10px;
  }

  .title {
    font-size: 24px;
    font-weight: bold;
    color: #222222;
  }
}

.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100% - 60px);
  background-image: url("../assets/images/login-bg.png");
  background-size: cover;
  position: relative;

  .title {
    margin: 0px auto 30px auto;
    text-align: center;
    color: #707070;
  }

  .sub-bg {
    width: 30%;
    position: absolute;
    left: 20%;
    top: 20%;
  }
}

.login-form {

  border-radius: 6px;
  background: #ffffff;
  width: 550px;
  padding: 25px 25px 5px 25px;

  .el-input {
    height: 38px;

    input {
      height: 38px;
    }
  }

  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 2px;
  }
}

@media screen and (max-width: 540px) {
  .login-form {
    margin: auto;
  }
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}

.login-code {
  width: 23%;
  height: 38px;
  float: right;
  margin-left: 5%;

  img {
    cursor: pointer;
    vertical-align: middle;
  }
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}

.login-code-img {
  height: 38px;
}

.login-code-button {
  display: block;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.login-code-button:focus-visible {
  outline: 3px solid #409eff;
  outline-offset: 2px;
}
</style>
