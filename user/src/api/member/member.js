import request from '@/utils/request'

// 查询商品列表
export function goodsList(query) {
  return request({
    url: '/bid/goods/front/list',
    method: 'get',
    params: query
  })
}

// 查询商品详细
export function getGoodsDetail(goodsId) {
  return request({
    url: `/bid/goods/front/${goodsId}`,
    method: 'get'
  })
}


// 二维码
export function getQrCode(payMethod) {
  return request({
    url: `/bid/payment/record/qrcode/${payMethod}`,
    method: 'get'
  })
}

// 提交订单
export function sumbitOrder(data) {
  return request({
    url: '/bid/order/submitOrder',
    method: 'post',
    data: data
  })
}

// 提交支付
export function orderPay(data) {
  return request({
    url: '/bid/order/orderPay',
    method: 'post',
    data: data
  })
}
