import request from '@/utils/request'

// 查询订单明细列表
export function listDetail(query) {
  return request({
    url: '/system/orderDetail/list',
    method: 'get',
    params: query
  })
}

// 查询订单明细详细
export function getDetail(id) {
  return request({
    url: '/system/orderDetail/' + id,
    method: 'get'
  })
}

// 新增订单明细
export function addDetail(data) {
  return request({
    url: '/system/orderDetail',
    method: 'post',
    data: data
  })
}

// 修改订单明细
export function updateDetail(data) {
  return request({
    url: '/system/orderDetail',
    method: 'put',
    data: data
  })
}

// 删除订单明细
export function delDetail(id) {
  return request({
    url: '/system/orderDetail/' + id,
    method: 'delete'
  })
}
