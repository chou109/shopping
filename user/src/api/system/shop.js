import request from '@/utils/request'

// 获取店铺信息
export function getShopInfo(id) {
  return request({
    url: `/system/shop/${id}`,
    method: 'get'
  })
}

// 搜索店铺
export function searchShop(keyword, status) {
  return request({
    url: '/system/shop/search',
    method: 'get',
    params: { keyword, status }
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

// 获取我的店铺
export function getMyShop() {
  return request({
    url: '/system/shop/my',
    method: 'get'
  })
}

// 更新店铺状态
export function updateShopStatus(shopId, status) {
  return request({
    url: '/system/shop/status',
    method: 'put',
    data: { shopId, status }
  })
}

// 逻辑删除店铺
export function deleteShopLogic(id) {
  return request({
    url: `/system/shop/logic/${id}`,
    method: 'delete'
  })
}

// 批量逻辑删除店铺
export function batchDeleteShopLogic(ids) {
  return request({
    url: `/system/shop/logic/batch/${ids}`,
    method: 'delete'
  })
}

// 获取店铺统计信息
export function getShopStatistics(id) {
  return request({
    url: `/system/shop/statistics/${id}`,
    method: 'get'
  })
}

// 增加店铺销量
export function increaseShopSales(shopId, quantity) {
  return request({
    url: `/system/shop/increaseSales/${shopId}`,
    method: 'post',
    params: { quantity }
  })
}

// 更新店铺评分
export function updateShopScore(shopId, score) {
  return request({
    url: `/system/shop/updateScore/${shopId}`,
    method: 'post',
    params: { score }
  })
}

// 更新店铺logo
export function updateShopLogo(id, logoUrl) {
  return request({
    url: `/system/shop/updateLogo/${id}`,
    method: 'post',
    data: { logoUrl }
  })
}

// 更新店铺横幅
export function updateShopBanner(id, bannerUrl) {
  return request({
    url: `/system/shop/updateBanner/${id}`,
    method: 'post',
    data: { bannerUrl }
  })
}