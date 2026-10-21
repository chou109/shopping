import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import * as ElIcons from '@element-plus/icons-vue';
import router from './router'
import axios from './axios/request'



// axios.defaults.baseURL = 'http://localhost:8081/api'
const app = createApp(App)

for (const iconName in ElIcons) {
    app.component(iconName,ElIcons[iconName])
}

app.config.globalProperties.$http = axios
// app.component('Pagination', Pagination)
// app.component(ElIcons)
app.use(createPinia())
app.provide("$axios",axios)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
app.config.globalProperties.$axios=axios
