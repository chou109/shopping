import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import indexLogin from '@/views/Login/indexLogin.vue'
import indexHome from '@/views/indexHome.vue'
import NavHeader from '@/views/NavHeader.vue'
import NavFooter from '@/views/NavFooter.vue'
import indexBanner from '@/views/Home/indexBanner.vue'
import indexHot from '@/views/Home/indexHot.vue'
import indexPhone from '@/views/Home/indexPhone.vue'
import oneHome from '@/views/Home/oneHome.vue'
import indexWhole from '@/views/Whole/indexWhole.vue'
import indexDetails from '@/views/Details/indexDetails.vue'
import indexCate from '@/views/Home/indexCate.vue'
import NavServiceBar from '@/views/NavServiceBar.vue'
import indexCart from '@/views/cart/indexCart.vue'
import indexAdd from '@/views/Add/indexAdd.vue'
import indexOrder from '@/views/Order/indexOrder.vue'
import OrderChild from '@/views/Order/OrderChild.vue'
import indexChilder from '@/views/Order/indexChilder.vue'
import indexSet from '@/views/settlement/indexSet.vue'
import indexConfirmorder from '@/views/settlement/indexConfirmorder.vue'
import indexManage from '@/views/Manage/indexManage.vue'
import indexUser from '@/views/Manage/indexUser.vue'
import indexProduct from '@/views/Manage/indexProduct.vue'
import indexRecharge from '@/views/Manage/indexRecharge.vue'
import indexSLS from '@/views/404/indexSLS.vue'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/home',
      name: 'home',
      component: indexHome,
      redirect: '/index',
      children: [
        {
          path: '/index',
          name: 'index',
          meta: {
            name: 'index',
            nacy: "true"
          },
          component: oneHome,
        },
        {
          path: '/whole',
          name: '全部商品',
          component: indexWhole,
        },
        {//详情
          path: '/details',
          name: 'details',
          component: indexDetails
        },


      ]
    },
    {
      path: '/',
      name: 'login',
      component: indexLogin
    },
    {
      path: '/navheader',
      name: 'navheader',
      component: NavHeader
    },
    {
      path: '/navfooter',
      name: 'navfooter',
      component: NavFooter
    },
    {
      path: '/banner',
      name: 'banner',
      component: indexBanner
    },
    {
      path: '/hot',
      name: 'hot',
      component: indexHot
    },
    {
      path: '/phone',
      name: 'phone',
      component: indexPhone
    },
    {
      path: '/cate',
      name: 'cate',
      component: indexCate
    },
    {
      path: '/NavServiceBar',
      name: 'NavServiceBar',
      component: NavServiceBar
    },
    {
      path: '/cart',
      name: 'cart',
      component: indexCart
    },
    {
      path: '/add',
      name: 'add',
      component: indexAdd
    },
    {
      path: '/order',
      name: 'order',
      component: indexOrder
    },
    {
      path: '/orderchild',
      name: 'orderchild',
      component: OrderChild
    },
    {
      path: '/childer',
      name: 'childer',
      component: indexChilder
    },
    {
      path: '/set',
      name: 'set',
      component: indexSet
    },
    {
      path: '/confirmorder',
      name: 'confirmorder',
      component: indexConfirmorder
    },
    {
      path: '/:pathMatch(.*)',
      name: 'sls',
      component: indexSLS
    },
    {
      path: '/manage',
      name: 'manage',
      redirect: '/user',
      component: indexManage,
      children: [
        {
          path: '/user',
          name: 'user',
          component: indexUser
        },
        {
          path: '/product',
          name: 'product',
          component: indexProduct
        },
        {
          path: '/recharge',
          name: 'recharge',
          component: indexRecharge
        }
      ]
    },

  ]

  // const router=createRouter({
  //   history:createWebHistory(),
  //   routes
  // })
})

// router.beforeEach((to,from,next)=>{
//   // var token = localStorage.getItem('token')
//   if(to.path=='/')return next()
//     const result=window.localStorage.getItem('Authorization')
//   // const user=JSON.parse(localStorage.getItem('user'))
//   // const type=user.type
//   // if(!result&&type==="user")
//   //   return next('/')
//   if(result||to.meta.nacy=='ture'){

//     return next()
//   }

//     else{
//       ElMessage.error('请先登录')
//     return next('/')
//     }
// })

export default router
