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

// 批量删除商品
export function delProducts(ids) {
  return request({
    url: '/system/product/' + ids.join(','),
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

// 根据分类查询商品
export function getProductsByCategory(categoryId) {
  return request({
    url: `/system/product/category/${categoryId}`,
    method: 'get'
  })
}

// 根据活动查询商品
export function getProductsByActivity(activityId) {
  return request({
    url: `/system/product/activity/${activityId}`,
    method: 'get'
  })
}

// 搜索商品
export function searchProducts(keyword) {
  return request({
    url: '/system/product/search',
    method: 'get',
    params: { keyword }
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

// 更新商品库存
export function updateProductStock(productId, quantity) {
  return request({
    url: `/system/product/${productId}/stock`,
    method: 'put',
    params: { quantity }
  })
}

// 导出商品
export function exportProduct(query) {
  return request({
    url: '/system/product/export',
    method: 'post',
    data: query,
    responseType: 'blob'
  })
}