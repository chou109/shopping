import request from '@/utils/request'

// 查询运动员个人信息管理列表
export function listAthletesPersonalInfo(query) {
  return request({
    url: '/athletes/athletesPersonalInfo/list',
    method: 'get',
    params: query
  })
}

// 查询运动员个人信息管理详细
export function getAthletesPersonalInfo(id) {
  return request({
    url: '/athletes/athletesPersonalInfo/' + id,
    method: 'get'
  })
}

// 新增运动员个人信息管理
export function addAthletesPersonalInfo(data) {
  return request({
    url: '/athletes/athletesPersonalInfo',
    method: 'post',
    data: data
  })
}

// 修改运动员个人信息管理
export function updateAthletesPersonalInfo(data) {
  return request({
    url: '/athletes/athletesPersonalInfo',
    method: 'put',
    data: data
  })
}

// 删除运动员个人信息管理
export function delAthletesPersonalInfo(id) {
  return request({
    url: '/athletes/athletesPersonalInfo/' + id,
    method: 'delete'
  })
}
