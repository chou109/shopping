import request from '@/utils/request'

// 查询门票管理列表
export function listTicketManagement(query) {
  return request({
    url: '/ticketsAndAudience/ticketManagement/list',
    method: 'get',
    params: query
  })
}

// 查询门票管理详细
export function getTicketManagement(id) {
  return request({
    url: '/ticketsAndAudience/ticketManagement/' + id,
    method: 'get'
  })
}

// 新增门票管理
export function addTicketManagement(data) {
  return request({
    url: '/ticketsAndAudience/ticketManagement',
    method: 'post',
    data: data
  })
}

// 修改门票管理
export function updateTicketManagement(data) {
  return request({
    url: '/ticketsAndAudience/ticketManagement',
    method: 'put',
    data: data
  })
}

// 删除门票管理
export function delTicketManagement(id) {
  return request({
    url: '/ticketsAndAudience/ticketManagement/' + id,
    method: 'delete'
  })
}
