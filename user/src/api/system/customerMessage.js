import request from '@/utils/request'

/**
 * 用户端客服消息API
 */

// 发送消息给商家
// receiverId: 店铺主人ID，可选，传递后消息只发送给指定的店铺主人
export function sendMessage(content, messageType = 1, receiverId = null) {
  return request({
    url: '/system/customerMessage/user/send',
    method: 'post',
    data: { content, messageType, receiverId }
  })
}

// 获取用户的消息列表
// receiverId: 可选，指定接收者ID后只获取与该接收者的对话
export function getMessages(receiverId = null) {
  return request({
    url: '/system/customerMessage/user/messages',
    method: 'get',
    params: { receiverId }
  })
}

// 获取用户未读消息数量
export function getUnreadCount() {
  return request({
    url: '/system/customerMessage/user/unread/count',
    method: 'get'
  })
}

// 标记消息为已读
export function markAsRead(receiverId = null) {
  return request({
    url: '/system/customerMessage/user/read',
    method: 'put',
    params: { receiverId }
  })
}

// 获取用户的会话列表
export function getUserSessions() {
  return request({
    url: '/system/customerMessage/user/sessions',
    method: 'get'
  })
}
