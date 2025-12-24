import request from '@/utils/request'

// 获取用户订单统计
export function getUserOrderStats(userId) {
  return request({
    url: '/system/orderDetail/user/stats/' + userId,
    method: 'get'
  })
}

// 获取用户收藏统计
export function getUserFavoriteStats(userId) {
  return request({
    url: '/system/favorite/user/stats/' + userId,
    method: 'get'
  })
}

// 获取用户足迹统计
export function getUserFootprintStats(userId) {
  return request({
    url: '/system/footprint/user/stats/' + userId,
    method: 'get'
  })
}

// 获取用户购物车统计
export function getUserCartStats(userId) {
  return request({
    url: '/system/cart/user/stats/' + userId,
    method: 'get'
  })
}