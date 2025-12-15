import request from '@/utils/request'

// 查询志愿者信息中心列表
export function listVolunteer(query) {
  return request({
    url: '/message/volunteer/list',
    method: 'get',
    params: query
  })
}

// 查询志愿者信息中心详细
export function getVolunteer(id) {
  return request({
    url: '/message/volunteer/' + id,
    method: 'get'
  })
}

// 新增志愿者信息中心
export function addVolunteer(data) {
  return request({
    url: '/message/volunteer',
    method: 'post',
    data: data
  })
}

// 修改志愿者信息中心
export function updateVolunteer(data) {
  return request({
    url: '/message/volunteer',
    method: 'put',
    data: data
  })
}

// 删除志愿者信息中心
export function delVolunteer(id) {
  return request({
    url: '/message/volunteer/' + id,
    method: 'delete'
  })
}
