import request from '@/utils/request'

// 获取商品评论列表
export function listProductComments(query) {
  return request({
    url: '/system/comment/list',
    method: 'get',
    params: query
  })
}

// 添加商品评论
export function addProductComment(data) {
  return request({
    url: '/system/comment',
    method: 'post',
    data: data
  })
}

// 发表评论
export function publishComment(data) {
  return request({
    url: '/system/comment/publish',
    method: 'post',
    data: data
  })
}

// 回复评论
export function replyComment(parentId, content) {
  return request({
    url: `/system/comment/reply/${parentId}`,
    method: 'post',
    data: { content }
  })
}

// 获取商品评论统计
export function getProductCommentStats(productId) {
  return request({
    url: `/system/comment/product/${productId}/statistics`,
    method: 'get'
  })
}

// 获取商品评论列表（前台）
export function getProductComments(productId, score, hasImages) {
  return request({
    url: `/system/comment/product/${productId}`,
    method: 'get',
    params: { score, hasImages }
  })
}

// 获取商品的根评论列表
export function getRootComments(productId) {
  return request({
    url: `/system/comment/product/${productId}/roots`,
    method: 'get'
  })
}

// 获取评论的回复列表
export function getCommentReplies(commentId) {
  return request({
    url: `/system/comment/${commentId}/replies`,
    method: 'get'
  })
}

// 点赞评论
export function likeComment(commentId) {
  return request({
    url: `/system/comment/${commentId}/like`,
    method: 'post'
  })
}

// 取消点赞评论
export function unlikeComment(commentId) {
  return request({
    url: `/system/comment/${commentId}/unlike`,
    method: 'post'
  })
}

// 获取商品评分分布
export function getScoreDistribution(productId) {
  return request({
    url: `/system/comment/product/${productId}/scoreDistribution`,
    method: 'get'
  })
}

// 获取商品平均评分
export function getAverageScore(productId) {
  return request({
    url: `/system/comment/product/${productId}/averageScore`,
    method: 'get'
  })
}

// 审核评论
export function auditComment(commentId, status, reason) {
  return request({
    url: `/system/comment/audit/${commentId}`,
    method: 'post',
    data: { status, reason }
  })
}

// 我的评论列表
export function getMyComments() {
  return request({
    url: '/system/comment/my',
    method: 'get'
  })
}

// 获取带图片的评论
export function getCommentsWithImages(productId, limit = 6) {
  return request({
    url: `/system/comment/product/${productId}/withImages`,
    method: 'get',
    params: { limit }
  })
}

// 检查用户是否评论过商品
export function checkUserComment(productId, orderId) {
  return request({
    url: '/system/comment/check',
    method: 'get',
    params: { productId, orderId }
  })
}

// 批量审核评论
export function batchAuditComments(commentIds, status, reason) {
  return request({
    url: '/system/comment/batchAudit',
    method: 'post',
    data: { commentIds, status, reason }
  })
}

// 获取评论详情（包含回复）
export function getCommentWithReplies(commentId) {
  return request({
    url: `/system/comment/detail/${commentId}/withReplies`,
    method: 'get'
  })
}

// 获取商品评论树
export function getCommentTree(productId) {
  return request({
    url: `/system/comment/product/${productId}/tree`,
    method: 'get'
  })
}

// 获取商品评论树（分页版）
export function getCommentTreePage(productId, pageNum = 1, pageSize = 10) {
  return request({
    url: `/system/comment/product/${productId}/tree/page`,
    method: 'get',
    params: { pageNum, pageSize }
  })
}