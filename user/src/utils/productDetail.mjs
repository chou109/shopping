export const normalizeProductStock = value => {
  const stock = Number(value)
  return Number.isFinite(stock) && stock > 0 ? Math.floor(stock) : 0
}

export const formatProductPrice = value => {
  const price = Number(value)
  return (Number.isFinite(price) ? price : 0).toFixed(2)
}

export const buildDirectOrderPayload = (purchase, addressId) => ({
  productId: purchase.productId,
  quantity: purchase.quantity,
  addressId
})
