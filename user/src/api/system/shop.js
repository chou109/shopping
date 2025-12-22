import request from '@/utils/request'

// 获取店铺信息
export function getShopInfo(id) {
  return request({
    url: `/system/shop/${id}`,
    method: 'get'
  })
}

// 搜索店铺
export function searchShop(keyword) {
  return request({
    url: '/system/shop/search',
    method: 'get',
    params: { keyword }
  })
}

// 热门店铺列表
export function listHotShop(limit = 10) {
  return request({
    url: '/system/shop/hot',
    method: 'get',
    params: { limit }
  })
}