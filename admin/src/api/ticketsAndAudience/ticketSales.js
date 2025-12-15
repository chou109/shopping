import request from '@/utils/request'

// 查询门票销售列表
export function listTicketSales(query) {
  return request({
    url: '/ticketsAndAudience/ticketSales/list',
    method: 'get',
    params: query
  })
}

// 查询门票销售详细
export function getTicketSales(id) {
  return request({
    url: '/ticketsAndAudience/ticketSales/' + id,
    method: 'get'
  })
}

// 新增门票销售
export function addTicketSales(data) {
  return request({
    url: '/ticketsAndAudience/ticketSales',
    method: 'post',
    data: data
  })
}

// 修改门票销售
export function updateTicketSales(data) {
  return request({
    url: '/ticketsAndAudience/ticketSales',
    method: 'put',
    data: data
  })
}

// 删除门票销售
export function delTicketSales(id) {
  return request({
    url: '/ticketsAndAudience/ticketSales/' + id,
    method: 'delete'
  })
}
