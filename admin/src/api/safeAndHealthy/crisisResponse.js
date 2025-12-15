import request from '@/utils/request'

// 查询危机响应列表
export function listCrisisResponse(query) {
  return request({
    url: '/safeAndHealthy/crisisResponse/list',
    method: 'get',
    params: query
  })
}

// 查询危机响应详细
export function getCrisisResponse(id) {
  return request({
    url: '/safeAndHealthy/crisisResponse/' + id,
    method: 'get'
  })
}

// 新增危机响应
export function addCrisisResponse(data) {
  return request({
    url: '/safeAndHealthy/crisisResponse',
    method: 'post',
    data: data
  })
}

// 修改危机响应
export function updateCrisisResponse(data) {
  return request({
    url: '/safeAndHealthy/crisisResponse',
    method: 'put',
    data: data
  })
}

// 删除危机响应
export function delCrisisResponse(id) {
  return request({
    url: '/safeAndHealthy/crisisResponse/' + id,
    method: 'delete'
  })
}
