import request from '@/utils/request'

// 查询观众服务列表
export function listAudienceService(query) {
  return request({
    url: '/ticketsAndAudience/audienceService/list',
    method: 'get',
    params: query
  })
}

// 查询观众服务详细
export function getAudienceService(id) {
  return request({
    url: '/ticketsAndAudience/audienceService/' + id,
    method: 'get'
  })
}

// 新增观众服务
export function addAudienceService(data) {
  return request({
    url: '/ticketsAndAudience/audienceService',
    method: 'post',
    data: data
  })
}

// 修改观众服务
export function updateAudienceService(data) {
  return request({
    url: '/ticketsAndAudience/audienceService',
    method: 'put',
    data: data
  })
}

// 删除观众服务
export function delAudienceService(id) {
  return request({
    url: '/ticketsAndAudience/audienceService/' + id,
    method: 'delete'
  })
}
