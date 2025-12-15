import request from '@/utils/request'

// 查询赛事管理-结果记录列表
export function listResultRecord(query) {
  return request({
    url: '/competition/resultRecord/list',
    method: 'get',
    params: query
  })
}

// 查询赛事管理-结果记录详细
export function getResultRecord(id) {
  return request({
    url: '/competition/resultRecord/' + id,
    method: 'get'
  })
}

// 新增赛事管理-结果记录
export function addResultRecord(data) {
  return request({
    url: '/competition/resultRecord',
    method: 'post',
    data: data
  })
}

// 修改赛事管理-结果记录
export function updateResultRecord(data) {
  return request({
    url: '/competition/resultRecord',
    method: 'put',
    data: data
  })
}

// 删除赛事管理-结果记录
export function delResultRecord(id) {
  return request({
    url: '/competition/resultRecord/' + id,
    method: 'delete'
  })
}
