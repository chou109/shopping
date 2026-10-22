import test from 'node:test'
import assert from 'node:assert/strict'

import { CustomerMessageDataError, readCustomerMessageList } from '../src/utils/customerMessageData.mjs'

test('reads successful session and message lists', () => {
  const rows = [{ sessionId: 'CS_2' }]
  assert.equal(readCustomerMessageList({ code: 200, data: rows }, '会话列表'), rows)
})

test('rejects failed or malformed session and message responses', () => {
  assert.throws(() => readCustomerMessageList({ code: 403, msg: '无权访问会话' }, '会话列表'), /无权访问会话/)
  assert.throws(() => readCustomerMessageList({ code: 200, data: {} }), CustomerMessageDataError)
})
