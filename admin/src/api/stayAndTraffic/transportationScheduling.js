import request from '@/utils/request'

// 查询交通调度列表
export function listTransportationScheduling(query) {
  return request({
    url: '/stayAndTraffic/transportationScheduling/list',
    method: 'get',
    params: query
  })
}

// 查询交通调度详细
export function getTransportationScheduling(id) {
  return request({
    url: '/stayAndTraffic/transportationScheduling/' + id,
    method: 'get'
  })
}

// 新增交通调度
export function addTransportationScheduling(data) {
  return request({
    url: '/stayAndTraffic/transportationScheduling',
    method: 'post',
    data: data
  })
}

// 修改交通调度
export function updateTransportationScheduling(data) {
  return request({
    url: '/stayAndTraffic/transportationScheduling',
    method: 'put',
    data: data
  })
}

// 删除交通调度
export function delTransportationScheduling(id) {
  return request({
    url: '/stayAndTraffic/transportationScheduling/' + id,
    method: 'delete'
  })
}
