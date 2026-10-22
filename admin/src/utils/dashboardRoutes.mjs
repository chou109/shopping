const DASHBOARD_ACTION_ROUTES = Object.freeze({
  product: '/shopping/product',
  order: '/order/info',
  shop: '/store/store',
  user: '/system/user'
})

export function getDashboardActionRoute(action) {
  return DASHBOARD_ACTION_ROUTES[action] || null
}
