import test from 'node:test'
import assert from 'node:assert/strict'
import { isBlindBoxOrder } from '../src/utils/order.mjs'

test('recognizes blind box orders from the persisted order flag', () => {
  assert.equal(isBlindBoxOrder({ isBlindBox: true, orderDetails: [] }), true)
  assert.equal(isBlindBoxOrder({ isBlindBox: 1, orderDetails: [] }), true)
  assert.equal(isBlindBoxOrder({ isBlindBox: 'true', orderDetails: [] }), true)
})

test('recognizes legacy blind box orders from the order detail snapshot prefix', () => {
  assert.equal(isBlindBoxOrder({
    orderDetails: [{ productName: '【盲盒】蓝牙耳机' }]
  }), true)
})

test('does not infer a blind box order from unrelated fields or generic product names', () => {
  assert.equal(isBlindBoxOrder({
    isBlindBox: false,
    remark: '盲盒活动订单',
    attributes: '{"isBlindBox":true}',
    orderDetails: [{ productName: '盲盒主题收纳盒' }]
  }), false)
})

test('returns false for missing or malformed order details', () => {
  assert.equal(isBlindBoxOrder(null), false)
  assert.equal(isBlindBoxOrder({ orderDetails: null }), false)
  assert.equal(isBlindBoxOrder({ orderDetails: [{ productName: null }] }), false)
})
