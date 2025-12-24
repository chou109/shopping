import request from '@/utils/request'

// 查询商品列表
export function listProduct(query) {
  return request({
    url: '/system/product/list',
    method: 'get',
    params: query
  })
}

// 查询商品列表（随机排序）
export function listProductRandom(query) {
  return request({
    url: '/system/product/list/random',
    method: 'get',
    params: query
  })
}

// 查询热门商品
export function listProductHot(limit = 10) {
  return request({
    url: '/system/product/hot',
    method: 'get',
    params: { limit }
  })
}

// 根据分类ID查询商品
export function listProductByCategory(categoryId) {
  return request({
    url: `/system/product/category/${categoryId}`,
    method: 'get'
  })
}

// 根据活动ID查询商品
export function listProductByActivity(activityId) {
  return request({
    url: `/system/product/activity/${activityId}`,
    method: 'get'
  })
}

// 搜索商品
export function searchProduct(keyword) {
  return request({
    url: '/system/product/search',
    method: 'get',
    params: { keyword }
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
export function delProduct(ids) {
  return request({
    url: '/system/product/' + ids,
    method: 'delete'
  })
}

// 设置商品热门状态
export function setProductHot(productId, hotFlag) {
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

// 上架/下架商品
export function updateProductStatus(productId, status) {
  return request({
    url: `/system/product/${productId}/status`,
    method: 'put',
    params: { status }
  })
}

// 导出商品列表
export function exportProduct(data) {
  return request({
    url: '/system/product/export',
    method: 'post',
    data: data,
    responseType: 'blob'
  })
}