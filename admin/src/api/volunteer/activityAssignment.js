import request from '@/utils/request'

// 查询活动分配列表
export function listActivityAssignment(query) {
  return request({
    url: '/volunteer/activityAssignment/list',
    method: 'get',
    params: query
  })
}

// 查询活动分配详细
export function getActivityAssignment(id) {
  return request({
    url: '/volunteer/activityAssignment/' + id,
    method: 'get'
  })
}

// 新增活动分配
export function addActivityAssignment(data) {
  return request({
    url: '/volunteer/activityAssignment',
    method: 'post',
    data: data
  })
}

// 修改活动分配
export function updateActivityAssignment(data) {
  return request({
    url: '/volunteer/activityAssignment',
    method: 'put',
    data: data
  })
}

// 删除活动分配
export function delActivityAssignment(id) {
  return request({
    url: '/volunteer/activityAssignment/' + id,
    method: 'delete'
  })
}
