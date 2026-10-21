import test from 'node:test'
import assert from 'node:assert/strict'
import {
  formatOrderPrice,
  formatOrderTime,
  getOrderStatusClass,
  getOrderTotalQuantity
} from './orderDisplay.mjs'

test('formats order prices and safely handles malformed amounts', () => {
  assert.equal(formatOrderPrice(12), '12.00')
  assert.equal(formatOrderPrice('12.5'), '12.50')
  assert.equal(formatOrderPrice(0), '0.00')
  assert.equal(formatOrderPrice('invalid'), '0.00')
})

test('preserves missing and malformed order timestamps without showing Invalid Date', () => {
  assert.equal(formatOrderTime(null), '')
  assert.equal(formatOrderTime('not-a-date'), 'not-a-date')
  assert.notEqual(formatOrderTime('2026-10-22T00:00:00Z'), 'Invalid Date')
})

test('maps order states to page styles and leaves unknown states unstyled', () => {
  assert.equal(getOrderStatusClass(10), 'status-wait-pay')
  assert.equal(getOrderStatusClass('50'), 'status-completed')
  assert.equal(getOrderStatusClass(999), '')
})

test('totals numeric and string quantities while ignoring malformed details', () => {
  assert.equal(getOrderTotalQuantity({
    orderDetails: [{ quantity: 2 }, { quantity: '3' }, { quantity: 'bad' }, null]
  }), 5)
  assert.equal(getOrderTotalQuantity({ orderDetails: null }), 0)
  assert.equal(getOrderTotalQuantity(null), 0)
})
