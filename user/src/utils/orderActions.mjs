import { isBlindBoxOrder } from './order.mjs'

const ACTIONS_BY_STATUS = Object.freeze({
  10: [
    { id: 'cancel', label: '取消订单', event: 'cancel' },
    { id: 'pay', label: '立即付款', event: 'pay', type: 'primary' },
    { id: 'delete', label: '删除订单', event: 'delete', type: 'danger' }
  ],
  30: [
    { id: 'confirm-receipt', label: '确认收货', event: 'confirm-receipt', type: 'primary' },
    { id: 'logistics', label: '查看物流', event: 'logistics' }
  ],
  40: [
    { id: 'review', label: '去评价', event: 'review', type: 'primary' }
  ],
  50: [
    { id: 'detail', label: '查看详情', event: 'detail' },
    { id: 'delete', label: '删除订单', event: 'delete', type: 'danger' }
  ],
  60: [
    { id: 'delete', label: '删除订单', event: 'delete', type: 'danger' }
  ]
})

export function getOrderActions(order) {
  const status = order?.status
  if (typeof status !== 'number') return [{ id: 'detail', label: '查看详情', event: 'detail' }]

  if (status === 20) {
    return isBlindBoxOrder(order)
      ? [{ id: 'blind-box-refund-not-allowed', label: '盲盒订单不支持退款' }]
      : [{ id: 'refund', label: '取消并退款', event: 'refund', type: 'warning' }]
  }

  const actions = ACTIONS_BY_STATUS[status]
  if (!actions) return [{ id: 'detail', label: '查看详情', event: 'detail' }]

  return actions.map(action => action.id === 'pay' && order?.isBlindBox
    ? { ...action, label: '继续付款' }
    : { ...action })
}
