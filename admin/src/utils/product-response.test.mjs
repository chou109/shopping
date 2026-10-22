import test from 'node:test'
import assert from 'node:assert/strict'

import { getProductDetailData, normalizeProductPageResponse } from './product-response.mjs'

test('normalizes a product page with numeric or string total', () => {
  const rows = [{ id: 9, name: '测试商品' }]

  assert.deepEqual(normalizeProductPageResponse({ rows, total: '12' }), { rows, total: 12 })
  assert.deepEqual(normalizeProductPageResponse({ rows: [], total: 0 }), { rows: [], total: 0 })
})

test('rejects malformed product page responses instead of treating them as empty', () => {
  for (const response of [null, {}, { rows: {}, total: 1 }, { rows: [], total: null }, { rows: [], total: '' }, { rows: [], total: -1 }, { rows: [], total: 'invalid' }]) {
    assert.throws(() => normalizeProductPageResponse(response), /商品列表响应格式异常/)
  }
})

test('returns valid product detail data', () => {
  const data = { id: 9, name: '测试商品' }
  assert.equal(getProductDetailData({ data }), data)
})

test('rejects missing or non-object product detail data', () => {
  for (const response of [null, {}, { data: null }, { data: [] }, { data: '商品' }]) {
    assert.throws(() => getProductDetailData(response), /商品详情响应格式异常/)
  }
})
