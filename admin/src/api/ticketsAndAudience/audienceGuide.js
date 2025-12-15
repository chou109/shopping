import request from '@/utils/request'

// 查询观众指南列表
export function listAudienceGuide(query) {
  return request({
    url: '/ticketsAndAudience/audienceGuide/list',
    method: 'get',
    params: query
  })
}

// 查询观众指南详细
export function getAudienceGuide(id) {
  return request({
    url: '/ticketsAndAudience/audienceGuide/' + id,
    method: 'get'
  })
}

// 新增观众指南
export function addAudienceGuide(data) {
  return request({
    url: '/ticketsAndAudience/audienceGuide',
    method: 'post',
    data: data
  })
}

// 修改观众指南
export function updateAudienceGuide(data) {
  return request({
    url: '/ticketsAndAudience/audienceGuide',
    method: 'put',
    data: data
  })
}

// 删除观众指南
export function delAudienceGuide(id) {
  return request({
    url: '/ticketsAndAudience/audienceGuide/' + id,
    method: 'delete'
  })
}
