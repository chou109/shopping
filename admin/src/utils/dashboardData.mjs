export class DashboardDataError extends Error {
  constructor(message) {
    super(message)
    this.name = 'DashboardDataError'
  }
}

function readDashboardResponse(response, label) {
  if (Number(response?.code) !== 200) {
    throw new DashboardDataError(response?.msg || `${label}加载失败，请重试`)
  }
  return response.data
}

export function readDashboardObject(response, label = '统计数据') {
  const data = readDashboardResponse(response, label)
  if (!data || typeof data !== 'object' || Array.isArray(data)) {
    throw new DashboardDataError(`${label}响应格式异常，请重试`)
  }
  return data
}

export function readDashboardArray(response, label) {
  const data = readDashboardResponse(response, label)
  if (!Array.isArray(data) || data.some(item => !item || typeof item !== 'object' || Array.isArray(item))) {
    throw new DashboardDataError(`${label}响应格式异常，请重试`)
  }
  return data
}

export function readDashboardSalesTrend(response) {
  const data = readDashboardObject(response, '销售趋势')
  if (!Array.isArray(data.dates) || !Array.isArray(data.amounts) || !Array.isArray(data.orderCounts)
      || data.dates.length !== data.amounts.length || data.dates.length !== data.orderCounts.length) {
    throw new DashboardDataError('销售趋势响应格式异常，请重试')
  }
  return data
}
