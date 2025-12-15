import request from '@/utils/request'

// 查询聊天记录列表
export function listHistory(query) {
  return request({
    url: '/chat/history/list',
    method: 'get',
    params: query
  })
}

// 查询聊天记录详细
export function getHistory(messageid) {
  return request({
    url: '/chat/history/' + messageid,
    method: 'get'
  })
}

// 新增聊天记录
export function addHistory(data) {
  return request({
    url: '/chat/history',
    method: 'post',
    data: data
  })
}

// 修改聊天记录
export function updateHistory(data) {
  return request({
    url: '/chat/history',
    method: 'put',
    data: data
  })
}

// 删除聊天记录
export function delHistory(messageid) {
  return request({
    url: '/chat/history/' + messageid,
    method: 'delete'
  })
}

// 导出聊天记录
export function exportHistory(query) {
  return request({
    url: '/chat/history/export',
    method: 'get',
    params: query
  })
}