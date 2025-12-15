import request from '@/utils/request'

// 查询参赛资格审核列表
export function listEligibilityReview(query) {
  return request({
    url: '/athletes/eligibilityReview/list',
    method: 'get',
    params: query
  })
}

// 查询参赛资格审核详细
export function getEligibilityReview(id) {
  return request({
    url: '/athletes/eligibilityReview/' + id,
    method: 'get'
  })
}

// 新增参赛资格审核
export function addEligibilityReview(data) {
  return request({
    url: '/athletes/eligibilityReview',
    method: 'post',
    data: data
  })
}

// 修改参赛资格审核
export function updateEligibilityReview(data) {
  return request({
    url: '/athletes/eligibilityReview',
    method: 'put',
    data: data
  })
}

// 删除参赛资格审核
export function delEligibilityReview(id) {
  return request({
    url: '/athletes/eligibilityReview/' + id,
    method: 'delete'
  })
}
