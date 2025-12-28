import request from '@/utils/request'

/**
 * 首页统计Dashboard API
 * 根据用户角色返回不同的统计数据
 */

// 获取首页统计数据
export function getStatistics() {
  return request({
    url: '/system/dashboard/statistics',
    method: 'get'
  })
}

// 获取统计卡片数据
export function getStatsCards() {
  return request({
    url: '/system/dashboard/stats-cards',
    method: 'get'
  })
}

// 获取销售趋势数据
export function getSalesTrend(timeRange = 'week') {
  return request({
    url: '/system/dashboard/sales-trend',
    method: 'get',
    params: { timeRange }
  })
}

// 获取热门商品
export function getHotProducts(limit = 5) {
  return request({
    url: '/system/dashboard/hot-products',
    method: 'get',
    params: { limit }
  })
}

// 获取最新订单
export function getRecentOrders(limit = 5) {
  return request({
    url: '/system/dashboard/recent-orders',
    method: 'get',
    params: { limit }
  })
}

// 获取分类统计
export function getCategoryStats() {
  return request({
    url: '/system/dashboard/category-stats',
    method: 'get'
  })
}

// 获取订单状态分布
export function getOrderStatusDistribution() {
  return request({
    url: '/system/dashboard/order-status-distribution',
    method: 'get'
  })
}

// 获取我的店铺信息
export function getMyShop() {
  return request({
    url: '/system/dashboard/my-shop',
    method: 'get'
  })
}
