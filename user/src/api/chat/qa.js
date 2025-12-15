import request from '@/utils/request'

// 查询问答管理列表
export function listQa(query) {
  return request({
    url: '/chat/qa/list',
    method: 'get',
    params: query
  })
}

export function getAnswer(query) {
  return request({
    url: '/chat/qa/getAnswer/' + query,
    method: 'get',
  })
}

// 查询问答管理详细
export function getQa(id) {
  return request({
    url: '/chat/qa/' + id,
    method: 'get'
  })
}

// 新增问答管理
export function addQa(data) {
  return request({
    url: '/chat/qa',
    method: 'post',
    data: data
  })
}

// 修改问答管理
export function updateQa(data) {
  return request({
    url: '/chat/qa',
    method: 'put',
    data: data
  })
}

// 删除问答管理
export function delQa(id) {
  return request({
    url: '/chat/qa/' + id,
    method: 'delete'
  })
}

// 导出问答管理
export function exportQa(query) {
  return request({
    url: '/chat/qa/export',
    method: 'get',
    params: query
  })
}

// 下载用户导入模板
export function importTemplate() {
  return request({
    url: '/chat/qa/importTemplate',
    method: 'get'
  })
}
