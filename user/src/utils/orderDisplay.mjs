const ORDER_STATUS_CLASSES = Object.freeze({
  10: 'status-wait-pay',
  20: 'status-wait-ship',
  30: 'status-wait-receive',
  40: 'status-wait-review',
  50: 'status-completed',
  60: 'status-cancelled'
})

export function formatOrderPrice(price) {
  if (!price) return '0.00'

  const amount = Number.parseFloat(price)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

export function formatOrderTime(time) {
  if (!time) return ''

  const date = new Date(time)
  return Number.isNaN(date.getTime()) ? String(time) : date.toLocaleString('zh-CN')
}

export function getOrderStatusClass(status) {
  return ORDER_STATUS_CLASSES[status] || ''
}

export function getOrderTotalQuantity(order) {
  if (!Array.isArray(order?.orderDetails)) return 0

  return order.orderDetails.reduce((sum, item) => {
    const quantity = Number(item?.quantity)
    return sum + (Number.isFinite(quantity) ? quantity : 0)
  }, 0)
}
