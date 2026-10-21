export function requiresAuthentication(route) {
  const matched = Array.isArray(route?.matched) ? route.matched : [route]
  return matched.some(record => record?.meta?.requiresAuth === true)
}

export function registerAccessRoutes(router, routes = []) {
  routes.forEach(route => router.addRoute(route))
}
