import router from './router'
import store from './store'
// import {Message} from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { registerAccessRoutes, requiresAuthentication } from '@/router/access.mjs'

NProgress.configure({ showSpinner: false })

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    if (to.path === '/login') {
      next({ path: '/index' })
      NProgress.done()
    } else {
      if (store.getters.roles.length === 0) {
        store.dispatch('GetInfo').then(res => {
          return store.dispatch('GenerateRoutes', { roles: res.roles })
        }).then(accessRoutes => {
            registerAccessRoutes(router, accessRoutes)
            next({ ...to, replace: true })
        }).catch(() => {
          store.dispatch('LogOut').then(() => {
            next({ path: '/login', query: { redirect: to.fullPath }, replace: true })
          }, () => {
            next({ path: '/login', query: { redirect: to.fullPath }, replace: true })
          })
        })
      } else {
        next()
      }
    }
  } else if (requiresAuthentication(to)) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    NProgress.done()
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})
