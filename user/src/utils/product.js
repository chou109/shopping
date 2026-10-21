export const isBlindBoxProduct = (product) => {
  if (!product) return false
  return String(product.id ?? product.productId) === '9999' ||
    [true, 1, '1', 'true'].includes(product.isBlindBox) ||
    [true, 1, '1', 'true'].includes(product.is_blind_box)
}
