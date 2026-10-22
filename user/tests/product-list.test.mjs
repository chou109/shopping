import test from 'node:test'
import assert from 'node:assert/strict'

import { hasNextProductPage, ProductListResponseError, readProductPage } from '../src/utils/productList.mjs'

test('reads product pages from the table and compatible array response shapes', () => {
  const products = [{ id: 1 }]
  assert.deepEqual(readProductPage({ code: 200, rows: products, total: 21 }), { rows: products, total: 21 })
  assert.deepEqual(readProductPage({ code: 200, data: products, total: '21' }), { rows: products, total: 21 })
})

test('rejects failed and malformed successful product page responses', () => {
  assert.throws(() => readProductPage({ code: 500, msg: '商品服务暂不可用' }), /商品服务暂不可用/)
  assert.throws(() => readProductPage({ code: 200, rows: {}, total: 3 }), ProductListResponseError)
  assert.throws(() => readProductPage({ code: 200, rows: [], total: 'NaN' }), /响应格式异常/)
  assert.throws(() => readProductPage({ code: 200, rows: [], total: null }), /响应格式异常/)
})

test('uses the server page boundary rather than the visible count after filtering products', () => {
  assert.equal(hasNextProductPage({ page: 1, pageSize: 20, total: 40, rowCount: 20 }), true)
  assert.equal(hasNextProductPage({ page: 2, pageSize: 20, total: 40, rowCount: 20 }), false)
  assert.equal(hasNextProductPage({ page: 1, pageSize: 20, total: 35, rowCount: 15 }), false)
})
