import request from '@/utils/request'

// 查询订单列表
export function orderList(query) {
  return request({
    url: '/bid/order/front/list',
    method: 'get',
    params: query
  })
}


// 退款
export function orderRefund(data) {
  return request({
    url: '/bid/order/orderRefund',
    method: 'post',
    data: data
  })
}
