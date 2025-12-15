import request from '@/utils/request'

// 查询交通服务列表
export function listTransportationService(query) {
  return request({
    url: '/stayAndTraffic/transportationService/list',
    method: 'get',
    params: query
  })
}

// 查询交通服务详细
export function getTransportationService(id) {
  return request({
    url: '/stayAndTraffic/transportationService/' + id,
    method: 'get'
  })
}

// 新增交通服务
export function addTransportationService(data) {
  return request({
    url: '/stayAndTraffic/transportationService',
    method: 'post',
    data: data
  })
}

// 修改交通服务
export function updateTransportationService(data) {
  return request({
    url: '/stayAndTraffic/transportationService',
    method: 'put',
    data: data
  })
}

// 删除交通服务
export function delTransportationService(id) {
  return request({
    url: '/stayAndTraffic/transportationService/' + id,
    method: 'delete'
  })
}
