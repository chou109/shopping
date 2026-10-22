import assert from 'node:assert/strict'
import test from 'node:test'
import { PAYMENT_TYPES, getPaymentTypeLabel, getPaymentTypeTag } from '../src/utils/order-payment.mjs'

test('maps backend payment codes to their authoritative labels', () => {
  assert.deepEqual(PAYMENT_TYPES.map(({ value, label }) => [value, label]), [
    [1, '支付宝'],
    [2, '微信支付'],
    [3, '银行卡支付'],
    [4, '余额支付']
  ])
})

test('provides safe labels and tags for unpaid or unknown payment types', () => {
  assert.equal(getPaymentTypeLabel(null), '待支付')
  assert.equal(getPaymentTypeLabel(99), '待支付')
  assert.equal(getPaymentTypeTag(1), 'success')
  assert.equal(getPaymentTypeTag(2), 'primary')
  assert.equal(getPaymentTypeTag(99), 'info')
})
