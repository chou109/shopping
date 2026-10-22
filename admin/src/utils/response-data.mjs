export function normalizePagedResponse(response) {
  if (response?.total === null || response?.total === undefined || (typeof response.total === 'string' && !response.total.trim())) {
    throw new TypeError('列表响应格式异常')
  }
  const total = Number(response.total)
  if (!Array.isArray(response?.rows) || !Number.isInteger(total) || total < 0) {
    throw new TypeError('列表响应格式异常')
  }

  return { rows: response.rows, total }
}

export function getObjectResponseData(response) {
  const data = response?.data
  if (!data || typeof data !== 'object' || Array.isArray(data)) {
    throw new TypeError('详情响应格式异常')
  }

  return data
}
