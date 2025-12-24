import request from '@/utils/request'

// 查询用户地址列表
export function listAddress(query) {
  return request({
    url: '/system/address/list',
    method: 'get',
    params: query
  })
}

// 查询用户地址详细
export function getAddress(id) {
  return request({
    url: '/system/address/' + id,
    method: 'get'
  })
}

// 新增用户地址
export function addAddress(data) {
  return request({
    url: '/system/address',
    method: 'post',
    data: data
  })
}

// 修改用户地址
export function updateAddress(data) {
  return request({
    url: '/system/address',
    method: 'put',
    data: data
  })
}

// 删除用户地址
export function delAddress(id) {
  return request({
    url: '/system/address/' + id,
    method: 'delete'
  })
}

// 根据用户ID查询地址列表
export function listAddressByUserId(userId) {
  return request({
    url: '/system/address/user/' + userId,
    method: 'get'
  })
}

// 获取用户的默认地址
export function getDefaultAddress(userId) {
  return request({
    url: '/system/address/user/' + userId + '/default',
    method: 'get'
  })
}

// 设置默认地址
export function setDefaultAddress(data) {
  return request({
    url: '/system/address/setDefault',
    method: 'post',
    data: data
  })
}

// 验证地址信息
export function validateAddress(data) {
  return request({
    url: '/system/address/validate',
    method: 'post',
    data: data
  })
}