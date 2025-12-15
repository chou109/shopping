import request from '@/utils/request'

// 查询赛事管理-赛程规划列表
export function listEventSchedule(query) {
  return request({
    url: '/competition/eventSchedule/list',
    method: 'get',
    params: query
  })
}

// 查询赛事管理-赛程规划详细
export function getEventSchedule(id) {
  return request({
    url: '/competition/eventSchedule/' + id,
    method: 'get'
  })
}

// 新增赛事管理-赛程规划
export function addEventSchedule(data) {
  return request({
    url: '/competition/eventSchedule',
    method: 'post',
    data: data
  })
}

// 修改赛事管理-赛程规划
export function updateEventSchedule(data) {
  return request({
    url: '/competition/eventSchedule',
    method: 'put',
    data: data
  })
}

// 删除赛事管理-赛程规划
export function delEventSchedule(id) {
  return request({
    url: '/competition/eventSchedule/' + id,
    method: 'delete'
  })
}
