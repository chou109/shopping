export function readProductRows(response) {
  if (Array.isArray(response)) return response
  if (response?.code !== undefined && Number(response.code) !== 200) {
    throw new Error(response.msg || '商品加载失败')
  }
  if (Array.isArray(response?.rows)) return response.rows
  if (Array.isArray(response?.data)) return response.data
  throw new TypeError('商品列表响应格式异常')
}

export function readCategoryList(response) {
  if (Array.isArray(response)) return response
  if (response?.code !== undefined && Number(response.code) !== 200) {
    throw new Error(response.msg || '分类加载失败')
  }
  const data = response?.data
  if (Array.isArray(data)) return data
  if (Array.isArray(data?.list)) return data.list
  if (Array.isArray(data?.rows)) return data.rows
  throw new TypeError('分类列表响应格式异常')
}

export function findPhoneCategory(categories) {
  const queue = [...categories]
  while (queue.length) {
    const category = queue.shift()
    const name = category?.name || category?.categoryName || ''
    if (name.includes('手机') || name.includes('数码')) return category
    const children = category?.children || category?.childList || []
    if (Array.isArray(children)) queue.push(...children)
  }
  return null
}
