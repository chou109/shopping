const BLIND_BOX_NAME_PREFIX = '【盲盒】'

export function isBlindBoxOrder(order) {
  if (!order || typeof order !== 'object') return false

  const hasBlindBoxFlag = order.isBlindBox === true
    || order.isBlindBox === 1
    || order.isBlindBox === 'true'

  if (hasBlindBoxFlag) return true

  return Array.isArray(order.orderDetails)
    && order.orderDetails.some(detail => detail?.productName?.startsWith(BLIND_BOX_NAME_PREFIX))
}
