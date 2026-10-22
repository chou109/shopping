import test from 'node:test'
import assert from 'node:assert/strict'

import { getObjectResponseData, normalizePagedResponse } from './response-data.mjs'

test('normalizes paged responses with numeric or string totals', () => {
  const rows = [{ id: 9 }]
  assert.deepEqual(normalizePagedResponse({ rows, total: '12' }), { rows, total: 12 })
  assert.deepEqual(normalizePagedResponse({ rows: [], total: 0 }), { rows: [], total: 0 })
})

test('rejects malformed paged responses instead of displaying an empty table', () => {
  for (const response of [null, {}, { rows: {}, total: 1 }, { rows: [], total: null }, { rows: [], total: '' }, { rows: [], total: -1 }, { rows: [], total: 1.5 }, { rows: [], total: 'invalid' }]) {
    assert.throws(() => normalizePagedResponse(response), /列表响应格式异常/)
  }
})

test('returns valid object data from detail responses', () => {
  const data = { id: 9, name: '测试记录' }
  assert.equal(getObjectResponseData({ data }), data)
})

test('rejects missing or non-object detail data', () => {
  for (const response of [null, {}, { data: null }, { data: [] }, { data: '测试记录' }]) {
    assert.throws(() => getObjectResponseData(response), /详情响应格式异常/)
  }
})
