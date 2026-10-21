import test from 'node:test'
import assert from 'node:assert/strict'
import { getBlindBoxOrderFailureMessage } from '../src/utils/blindBoxCheckout.mjs'

test('distinguishes a known order from an uncertain order creation result', () => {
  assert.equal(getBlindBoxOrderFailureMessage('ORDER-123'), '奖品订单已生成，请到订单页面完成支付')
  assert.equal(getBlindBoxOrderFailureMessage(''), '盲盒订单创建结果未知，请先到订单页面确认，避免重复下单')
  assert.equal(getBlindBoxOrderFailureMessage(null), '盲盒订单创建结果未知，请先到订单页面确认，避免重复下单')
})
