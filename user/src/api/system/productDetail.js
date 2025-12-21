import request from '@/utils/request'

// 查询商品明细列表
export function listDetail(query) {
  return request({
    url: '/system/productDetail/list',
    method: 'get',
    params: querys
  })
}

// 查询商品明细详细
export function getDetail(id) {
  return request({
    url: '/system/productDetail/' + id,
    method: 'get'
  })
}

// 新增商品明细
export function addDetail(data) {
  return request({
    url: '/system/productDetail',
    method: 'post',
    data: data
  })
}

// 修改商品明细
export function updateDetail(data) {
  return request({
    url: '/system/productDetail',
    method: 'put',
    data: data
  })
}

// 删除商品明细
export function delDetail(id) {
  return request({
    url: '/system/productDetail/' + id,
    method: 'delete'
  })
}
