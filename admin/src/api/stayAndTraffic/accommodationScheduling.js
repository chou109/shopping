import request from '@/utils/request'

// 查询住宿调度列表
export function listAccommodationScheduling(query) {
  return request({
    url: '/stayAndTraffic/accommodationScheduling/list',
    method: 'get',
    params: query
  })
}

// 查询住宿调度详细
export function getAccommodationScheduling(id) {
  return request({
    url: '/stayAndTraffic/accommodationScheduling/' + id,
    method: 'get'
  })
}

// 新增住宿调度
export function addAccommodationScheduling(data) {
  return request({
    url: '/stayAndTraffic/accommodationScheduling',
    method: 'post',
    data: data
  })
}

// 修改住宿调度
export function updateAccommodationScheduling(data) {
  return request({
    url: '/stayAndTraffic/accommodationScheduling',
    method: 'put',
    data: data
  })
}

// 删除住宿调度
export function delAccommodationScheduling(id) {
  return request({
    url: '/stayAndTraffic/accommodationScheduling/' + id,
    method: 'delete'
  })
}
