import test from 'node:test'
import assert from 'node:assert/strict'

import { getOrderActions } from './orderActions.mjs'

const actionIds = order => getOrderActions(order).map(action => action.id)

test('keeps the allowed action set aligned with each persisted order state', () => {
  assert.deepEqual(actionIds({ status: 10 }), ['cancel', 'pay', 'delete'])
  assert.deepEqual(actionIds({ status: 20 }), ['refund'])
  assert.deepEqual(actionIds({ status: 30 }), ['confirm-receipt', 'logistics'])
  assert.deepEqual(actionIds({ status: 40 }), ['review'])
  assert.deepEqual(actionIds({ status: 50 }), ['detail', 'delete'])
  assert.deepEqual(actionIds({ status: 60 }), ['delete'])
  assert.deepEqual(actionIds({ status: 999 }), ['detail'])
  assert.deepEqual(actionIds({ status: '10' }), ['detail'])
})

test('prevents refund actions for blind-box orders and labels their payment action', () => {
  const blindBoxOrder = {
    status: 20,
    isBlindBox: true,
    orderDetails: [{ productName: '【盲盒】神秘奖品' }]
  }
  const pendingPayment = getOrderActions({ ...blindBoxOrder, status: 10 })

  assert.deepEqual(actionIds(blindBoxOrder), ['blind-box-refund-not-allowed'])
  assert.equal(pendingPayment.find(action => action.id === 'pay').label, '继续付款')
})
