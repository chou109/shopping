import { createApp } from 'vue'

import { ElLoading, ElLoadingDirective, ElMessage, ElMessageBox, ElNotification } from 'element-plus'

import '@/assets/styles/index.scss' // global css
import '@/assets/storefront-polish.css' // storefront visual system and responsive layout

import App from './App'
import store from './store'
import router from './router'
// import directive from './directive' // directive
import permission from './directive/permission'

// 注册指令
import plugins from './plugins' // plugins
import svgSprite from 'virtual:svg-sprite'

document.body.insertAdjacentHTML('afterbegin', svgSprite)

import SvgIcon from '@/components/SvgIcon'

import './permission' // permission control

const app = createApp(App)

app.config.globalProperties.msgError = function (msg) {
  this.$message({ showClose: true, message: msg, type: "error" });
}

app.use(permission)

app.use(router)
app.use(store)
app.use(plugins)
app.component('svg-icon', SvgIcon)

// directive(app)

// 按需加载 Element Plus 组件，保留全局提示 API 和尺寸切换兼容。
app.config.globalProperties.$ELEMENT = { size: store.getters.size }
app.config.globalProperties.$message = ElMessage
app.config.globalProperties.$notify = ElNotification
app.config.globalProperties.$loading = ElLoading.service
app.config.globalProperties.$msgbox = ElMessageBox
app.config.globalProperties.$alert = ElMessageBox.alert
app.config.globalProperties.$confirm = ElMessageBox.confirm
app.config.globalProperties.$prompt = ElMessageBox.prompt
app.directive('loading', ElLoadingDirective)

app.mount('#app')
