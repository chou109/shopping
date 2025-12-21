import request from '@/utils/request'

// 获取当前用户购物车列表
export function listCart() {
  return request({
    url: '/system/cart/list',
    method: 'get'
  })
}

// 添加商品到购物车
export function addCart(data) {
  return request({
    url: '/system/cart',
    method: 'post',
    data: data
  })
}

// 修改购物车商品数量
export function updateCart(id, data) {
  return request({
    url: `/system/cart/${id}`,
    method: 'put',
    data: data
  })
}

// 删除购物车商品
export function delCart(id) {
  return request({
    url: `/system/cart/${id}`,
    method: 'delete'
  })
}

// 批量删除购物车商品
export function batchDelCart(ids) {
  return request({
    url: '/system/cart/batch',
    method: 'delete',
    data: ids
  })
}

// 清空购物车
export function clearCart() {
  return request({
    url: '/system/cart/clear',
    method: 'delete'
  })
}

// 获取购物车统计信息
export function getCartCount() {
  return request({
    url: '/system/cart/count',
    method: 'get'
  })
}