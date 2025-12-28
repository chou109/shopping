import request from '@/utils/request'

// 查询我的订单列表
export function getMyOrders(status) {
  return request({
    url: '/system/orderInfo',
    method: 'get',
    params: { status }
  })
}

// 查询订单列表
export function listInfo(query) {
  return request({
    url: '/system/orderInfo/list',
    method: 'get',
    params: query
  })
}

// 查询订单详细
export function getInfo(id) {
  return request({
    url: '/system/orderInfo/' + id,
    method: 'get'
  })
}

// 根据订单号查询订单
export function getOrderByNo(orderNo) {
  return request({
    url: '/system/orderInfo/detail/' + orderNo,
    method: 'get'
  })
}

// 新增订单
export function addInfo(data) {
  return request({
    url: '/system/orderInfo',
    method: 'post',
    data: data
  })
}

// 从购物车创建订单
export function createOrderFromCart(addressId) {
  return request({
    url: '/system/orderInfo',
    method: 'post',
    params: { addressId }
  })
}

// 直接购买创建订单（单个商品）
export function createDirectOrder(data) {
  return request({
    url: '/system/orderInfo/direct',
    method: 'post',
    data: data
  })
}

// 支付订单
export function payOrder(orderNo, paymentType) {
  return request({
    url: '/system/orderInfo/' + orderNo + '/pay',
    method: 'put',
    params: { paymentType }
  })
}

// 余额支付订单
export function payOrderByBalance(orderNo) {
  return request({
    url: '/system/orderInfo/' + orderNo + '/pay',
    method: 'put',
    params: { paymentType: 4 }  // 4为余额支付
  })
}

// 取消订单
export function cancelOrder(orderNo, cancelReason) {
  return request({
    url: '/system/orderInfo/' + orderNo + '/cancel',
    method: 'put',
    params: { cancelReason }
  })
}

// 确认收货
export function confirmReceipt(orderNo) {
  return request({
    url: '/system/orderInfo/' + orderNo + '/confirm-receipt',
    method: 'put'
  })
}

// 修改订单
export function updateInfo(data) {
  return request({
    url: '/system/orderInfo',
    method: 'put',
    data: data
  })
}

// 删除订单（用户端）
export function delInfo(id) {
  return request({
    url: '/system/orderInfo/delete-my-order',
    method: 'post',
    params: { id }
  })
}

// 获取用户余额
export function getUserBalance() {
  return request({
    url: '/system/user/current',
    method: 'get'
  })
}

// 申请退款
export function applyRefund(data) {
  return request({
    url: '/system/orderInfo/apply-refund',
    method: 'post',
    data: data
  })
}
