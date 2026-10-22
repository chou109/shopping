export class ProductListResponseError extends Error {
  constructor(message) {
    super(message)
    this.name = 'ProductListResponseError'
  }
}

export function readProductPage(response) {
  if (response?.code !== undefined && Number(response.code) !== 200) {
    throw new ProductListResponseError(response.msg || '商品加载失败')
  }

  const rows = Array.isArray(response?.rows)
    ? response.rows
    : Array.isArray(response?.data)
      ? response.data
      : null
  const total = Number(response?.total)

  if (!rows || response?.total == null || response.total === '' || !Number.isSafeInteger(total) || total < 0) {
    throw new ProductListResponseError('商品列表响应格式异常，请重试')
  }

  return { rows, total }
}

export function hasNextProductPage({ page, pageSize, total, rowCount }) {
  return rowCount >= pageSize && page * pageSize < total
}
