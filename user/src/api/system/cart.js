import request from '@/utils/request'

// 查询购物车列表
export function listCart(query) {
  return request({
    url: '/system/cart/list',
    method: 'get',
    params: query
  })
}

// 查询购物车详细
export function getCart(id) {
  return request({
    url: '/system/cart/' + id,
    method: 'get'
  })
}

// 新增购物车
export function addCart(data) {
  return request({
    url: '/system/cart',
    method: 'post',
    data: data
  })
}

// 修改购物车
export function updateCart(data) {
  return request({
    url: '/system/cart',
    method: 'put',
    data: data
  })
}

// 删除购物车
export function delCart(id) {
  return request({
    url: '/system/cart/' + id,
    method: 'delete'
  })
}
