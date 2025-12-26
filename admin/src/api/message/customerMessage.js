import request from '@/utils/request'

/**
 * 商家端客服消息API
 */

// 获取会话列表
export function getSessionList() {
  return request({
    url: '/system/customerMessage/admin/sessions',
    method: 'get'
  })
}

// 获取指定会话的消息列表
export function getSessionMessages(sessionId) {
  return request({
    url: `/system/customerMessage/admin/session/${sessionId}`,
    method: 'get'
  })
}

// 回复消息给用户
export function replyMessage(sessionId, content, messageType = 1) {
  return request({
    url: '/system/customerMessage/admin/reply',
    method: 'post',
    data: { sessionId, content, messageType }
  })
}

// 获取商家未读消息数量
export function getUnreadCount() {
  return request({
    url: '/system/customerMessage/admin/unread/count',
    method: 'get'
  })
}

// 标记会话消息为已读
export function markAsRead(sessionId) {
  return request({
    url: `/system/customerMessage/admin/read/${sessionId}`,
    method: 'put'
  })
}

// 查询消息列表（分页）
export function listCustomerMessage(query) {
  return request({
    url: '/system/customerMessage/list',
    method: 'get',
    params: query
  })
}

// 删除消息
export function delCustomerMessage(ids) {
  return request({
    url: `/system/customerMessage/${ids}`,
    method: 'delete'
  })
}
