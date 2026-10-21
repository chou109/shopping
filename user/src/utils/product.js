export const isBlindBoxProduct = (product) => {
  if (!product) return false
  return String(product.id ?? product.productId) === '9999' ||
    [true, 1, '1', 'true'].includes(product.isBlindBox) ||
    [true, 1, '1', 'true'].includes(product.is_blind_box)
}

export const filterProductsByKeyword = (products, keyword) => {
  const normalizedKeyword = String(keyword || '').trim().toLowerCase()
  if (!normalizedKeyword) return products || []

  return (products || []).filter((product) =>
    [product.name, product.subTitle, product.description]
      .some((value) => String(value || '').toLowerCase().includes(normalizedKeyword))
  )
}
