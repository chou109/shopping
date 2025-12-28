import request from '@/utils/request'

// 获取我的评论列表
export function getMyComments(query) {
  return request({
    url: '/system/comment/my',
    method: 'get',
    params: query
  })
}

// 删除评论
export function deleteComment(commentId) {
  return request({
    url: `/system/comment/${commentId}`,
    method: 'delete'
  })
}

// 提交评价
export function submitReview(data) {
  return request({
    url: '/system/comment',
    method: 'post',
    data: data
  })
}

// 评价后更新订单状态为已完成
export function completeOrder(orderNo) {
  return request({
    url: `/system/orderInfo/${orderNo}/complete`,
    method: 'put'
  })
}
