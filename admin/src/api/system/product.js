import request from '@/utils/request'

// 查询商品列表
export function listProduct(query) {
  return request({
    url: '/system/product/list',
    method: 'get',
    params: query
  })
}

// 查询商品详细
export function getProduct(id) {
  return request({
    url: '/system/product/' + id,
    method: 'get'
  })
}

// 新增商品
export function addProduct(data) {
  return request({
    url: '/system/product',
    method: 'post',
    data: data
  })
}

// 修改商品
export function updateProduct(data) {
  return request({
    url: '/system/product',
    method: 'put',
    data: data
  })
}

// 删除商品
export function delProduct(id) {
  return request({
    url: '/system/product/' + id,
    method: 'delete'
  })
}

// 查询热门商品列表（用于首页展示）
export function getHotProducts(limit = 10) {
  return request({
    url: '/system/product/hot',
    method: 'get',
    params: { limit }
  })
}

// 更新商品状态（上架/下架）- 注意：后端接口是 /{productId}/status
export function updateProductStatus(productId, status) {
  return request({
    url: `/system/product/${productId}/status`,
    method: 'put',
    params: { status }
  })
}

// 更新商品热门状态 - 修正URL为 /{productId}/hot
export function updateProductHotFlag(productId, hotFlag) {
  return request({
    url: `/system/product/${productId}/hot`,
    method: 'put',
    params: { hotFlag }
  })
}
