import request from '@/utils/request'

// 查询成绩录入列表
export function listPerformanceTracking(query) {
  return request({
    url: '/athletes/performanceTracking/list',
    method: 'get',
    params: query
  })
}

// 查询成绩录入详细
export function getPerformanceTracking(id) {
  return request({
    url: '/athletes/performanceTracking/' + id,
    method: 'get'
  })
}

// 新增成绩录入
export function addPerformanceTracking(data) {
  return request({
    url: '/athletes/performanceTracking',
    method: 'post',
    data: data
  })
}

// 修改成绩录入
export function updatePerformanceTracking(data) {
  return request({
    url: '/athletes/performanceTracking',
    method: 'put',
    data: data
  })
}

// 删除成绩录入
export function delPerformanceTracking(id) {
  return request({
    url: '/athletes/performanceTracking/' + id,
    method: 'delete'
  })
}
