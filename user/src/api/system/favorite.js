import request from '@/utils/request'

// 获取我的商品收藏列表
export function getMyProductFavorites() {
  return request({
    url: '/system/favorite/my/products',
    method: 'get'
  })
}

// 获取我的店铺收藏列表
export function getMyShopFavorites() {
  return request({
    url: '/system/favorite/my/shops',
    method: 'get'
  })
}

// 添加收藏
export function addFavorite(targetType, targetId) {
  return request({
    url: '/system/favorite/add',
    method: 'post',
    data: { targetType, targetId }
  })
}

// 取消收藏
export function removeFavorite(targetType, targetId) {
  return request({
    url: '/system/favorite/remove',
    method: 'post',
    data: { targetType, targetId }
  })
}

// 切换收藏状态
export function toggleFavorite(targetType, targetId) {
  return request({
    url: '/system/favorite/toggle',
    method: 'post',
    data: { targetType, targetId }
  })
}

// 检查是否已收藏
export function checkFavorite(targetType, targetId) {
  return request({
    url: '/system/favorite/check',
    method: 'get',
    params: { targetType, targetId }
  })
}

// 获取收藏统计
export function getFavoriteStats() {
  return request({
    url: '/system/favorite/my/stats',
    method: 'get'
  })
}
