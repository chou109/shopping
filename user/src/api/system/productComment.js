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

// 获取商品评论统计
export function getProductCommentStats(productId) {
  return request({
    url: `/system/comment/product/${productId}/statistics`,
    method: 'get'
  })
}