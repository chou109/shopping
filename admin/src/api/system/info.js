import request from '@/utils/request'

// 查询订单列表
export function listInfo(query) {
  return request({
    url: '/api/admin/orders/list',
    method: 'get',
    params: query
  })
}

// 查询订单详细
export function getInfo(id) {
  return request({
    url: '/api/admin/orders/' + id,
    method: 'get'
  })
}

// 新增订单
export function addInfo(data) {
  return request({
    url: '/api/admin/orders/',
    method: 'post',
    data: data
  })
}

// 修改订单
export function updateInfo(data) {
  return request({
    url: '/api/admin/orders/',
    method: 'put',
    data: data
  })
}

// 删除订单
export function delInfo(id) {
  return request({
    url: '/api/admin/orders/' + id,
    method: 'delete'
  })
}
