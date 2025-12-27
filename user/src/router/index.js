// router/index.js
import { createWebHistory, createRouter } from 'vue-router'
import Layout from '@/layout'
import { AppMain } from '@/layout/components'

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  // AI客服聊天页面
  {
    path: '/chat',
    component: Layout,
    children: [
      {
        path: '',
        name: 'Chat',
        component: () => import('@/views/chat/index.vue'),
        meta: {
          title: 'AI客服',
          icon: 'chat-dot-round',
          requiresAuth: true
        }
      }
    ]
  },
  // 人工客服页面
  {
    path: '/customer-service',
    component: Layout,
    children: [
      {
        path: '',
        name: 'CustomerService',
        component: () => import('@/views/message/CustomerService.vue'),
        meta: {
          title: '人工客服',
          icon: 'service',
          requiresAuth: true
        }
      }
    ]
  },
  // 用户中心统一路由 - 使用动态参数
  {
    path: '/user/:page?',
    component: Layout,
    meta: { requiresAuth: true },
    props: (route) => ({
      page: route.params.page || 'profile',
      query: route.query
    }),
    children: [
      {
        path: '',
        name: 'UserCenter',
        component: () => import('@/views/user/UserCenter.vue'),
        meta: {
          title: '用户中心',
          pageTitles: {
            profile: '个人中心',
            orders: '我的订单',
            cart: '购物车',
            message: '我的消息',
            footprint: '我的足迹',
            favoriteShop: '收藏店铺',
            favoriteProduct: '收藏商品',
            review: '我的评价',
          }
        }
      }
    ]
  },
  // 商品相关路由保持不变
  {
    path: '/products',
    name: 'ProductsSearch',
    component: Layout,
    children: [
      {
        path: '',
        component: () => import('@/views/products/ProductsSearch.vue'),
        meta: { title: '商品搜索' }
      }
    ]
  },
  {
    path: '/product/detail',
    name: 'ProductDetail',
    component: Layout,
    children: [
      {
        path: '',
        component: () => import('@/views/products/productDetail.vue'),
        meta: { title: '商品详情' }
      }
    ]
  },
  {
    path: '/product/blind-box',
    name: 'BlindBoxDetail',
    component: Layout,
    children: [
      {
        path: '',
        component: () => import('@/views/products/blindBoxDetail.vue'),
        meta: { title: '盲盒商品' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

// 添加路由守卫进行调试
router.beforeEach((to, from, next) => {
  console.log('路由跳转:', from.path, '->', to.path, to.params)
  next()
})

export default router