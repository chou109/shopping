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
