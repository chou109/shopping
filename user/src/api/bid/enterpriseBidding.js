import request from '@/utils/request'

// 查询企业投标列表
export function listEnterpriseBidding(query) {
  return request({
    url: '/bid/enterprise/bidding/front/list',
    method: 'get',
    params: query
  })
}

// 查询企业投标详细
export function getEnterpriseBidding(id) {
  return request({
    url: '/bid/enterprise/bidding/front/' + id,
    method: 'get'
  })
}

// 新增企业投标
export function addEnterpriseBidding(data) {
  return request({
    url: '/bid/enterprise/bidding/front/add',
    method: 'post',
    data: data
  })
}

// 修改企业投标
export function updateEnterpriseBidding(data) {
  return request({
    url: '/bid/enterprise/bidding/front/edit',
    method: 'post',
    data: data
  })
}

// 删除企业投标
export function delEnterpriseBidding(id) {
  return request({
    url: '/bid/enterprise/bidding/' + id,
    method: 'delete'
  })
}

// 导出企业投标
export function exportEnterpriseBidding(query) {
  return request({
    url: '/bid/enterprise/bidding/export',
    method: 'get',
    params: query
  })
}
