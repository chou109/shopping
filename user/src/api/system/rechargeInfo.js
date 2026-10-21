import request from '@/utils/request'

// 查询充值记录列表
export function listInfo(query) {
  return request({
    url: '/system/rechargeInfo/list',
    method: 'get',
    params: query
  })
}

// 查询充值记录详细
export function getInfo(id) {
  return request({
    url: '/system/rechargeInfo/' + id,
    method: 'get'
  })
}

// 创建充值订单
export function createRechargeOrder(data) {
  return request({
    url: '/system/rechargeInfo/create',
    method: 'post',
    data: data
  })
}

// 根据用户ID查询充值记录
export function getRechargeByUserId(userId) {
  return request({
    url: '/system/rechargeInfo/user/' + userId,
    method: 'get'
  })
}

// 获取用户充值总额
export function getTotalRecharge(userId) {
  return request({
    url: '/system/rechargeInfo/user/' + userId + '/total',
    method: 'get'
  })
}

// 新增充值记录
export function addInfo(data) {
  return request({
    url: '/system/rechargeInfo',
    method: 'post',
    data: data
  })
}

// 修改充值记录
export function updateInfo(data) {
  return request({
    url: '/system/rechargeInfo',
    method: 'put',
    data: data
  })
}

// 删除充值记录
export function delInfo(id) {
  return request({
    url: '/system/rechargeInfo/' + id,
    method: 'delete'
  })
}
