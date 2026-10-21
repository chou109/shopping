import test from 'node:test'
import assert from 'node:assert/strict'
import { buildDirectOrderPayload, formatProductPrice, normalizeProductStock } from '../src/utils/productDetail.mjs'

test('normalizes stock safely and never treats zero as missing inventory', () => {
  assert.equal(normalizeProductStock(0), 0)
  assert.equal(normalizeProductStock('0'), 0)
  assert.equal(normalizeProductStock('4.8'), 4)
  assert.equal(normalizeProductStock(-1), 0)
  assert.equal(normalizeProductStock('unknown'), 0)
})

test('formats valid prices and prevents malformed values from rendering NaN', () => {
  assert.equal(formatProductPrice(12.5), '12.50')
  assert.equal(formatProductPrice('0'), '0.00')
  assert.equal(formatProductPrice('unknown'), '0.00')
  assert.equal(formatProductPrice(null), '0.00')
})

test('builds direct-order requests from the purchase snapshot and selected address', () => {
  const purchaseSnapshot = { productId: 42, quantity: 2 }
  const payload = buildDirectOrderPayload(purchaseSnapshot, 17)
  assert.deepEqual(payload, { productId: 42, quantity: 2, addressId: 17 })

  purchaseSnapshot.productId = 99
  assert.equal(payload.productId, 42)
})
