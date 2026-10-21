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

// 使用订单状态迁移接口发货，避免直接覆盖状态字段
export function shipInfo(orderNo) {
  return request({
    url: '/api/admin/orders/' + orderNo + '/ship',
    method: 'put',
    data: {}
  })
}

// 删除订单
export function delInfo(id) {
  return request({
    url: '/api/admin/orders/' + id,
    method: 'delete'
  })
}
