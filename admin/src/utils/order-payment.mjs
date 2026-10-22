export const PAYMENT_TYPES = Object.freeze([
  Object.freeze({ value: 1, label: '支付宝', tagType: 'success' }),
  Object.freeze({ value: 2, label: '微信支付', tagType: 'primary' }),
  Object.freeze({ value: 3, label: '银行卡支付', tagType: 'warning' }),
  Object.freeze({ value: 4, label: '余额支付', tagType: '' })
])

export const getPaymentType = value => PAYMENT_TYPES.find(type => type.value === Number(value))

export const getPaymentTypeLabel = value => getPaymentType(value)?.label || '待支付'

export const getPaymentTypeTag = value => getPaymentType(value)?.tagType || 'info'
