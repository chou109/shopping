import request from '@/utils/request'

// 查询热门商品
export function listProductHot(limit = 10) {
  return request({
    url: '/system/product/hot',
    method: 'get',
    params: { limit },
    headers: { isToken: false }
  })
}

// 根据分类ID查询商品
export function listProductByCategory(categoryId) {
  return request({
    url: `/system/product/category/${categoryId}`,
    method: 'get',
    headers: { isToken: false }
  })
}

// 查询商品详细
export function getProduct(id) {
  return request({
    url: '/system/product/public/' + id,
    method: 'get',
    headers: { isToken: false }
  })
}
