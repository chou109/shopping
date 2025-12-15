import request from '@/utils/request'

// 查询客服支持列表
export function listCustomerSupport(query) {
  return request({
    url: '/ticketsAndAudience/customerSupport/list',
    method: 'get',
    params: query
  })
}

// 查询客服支持详细
export function getCustomerSupport(id) {
  return request({
    url: '/ticketsAndAudience/customerSupport/' + id,
    method: 'get'
  })
}

// 新增客服支持
export function addCustomerSupport(data) {
  return request({
    url: '/ticketsAndAudience/customerSupport',
    method: 'post',
    data: data
  })
}

// 修改客服支持
export function updateCustomerSupport(data) {
  return request({
    url: '/ticketsAndAudience/customerSupport',
    method: 'put',
    data: data
  })
}

// 删除客服支持
export function delCustomerSupport(id) {
  return request({
    url: '/ticketsAndAudience/customerSupport/' + id,
    method: 'delete'
  })
}
