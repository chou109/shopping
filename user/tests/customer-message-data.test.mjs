import test from 'node:test'
import assert from 'node:assert/strict'

import { CustomerMessageDataError, readCustomerMessageList, readCustomerUnreadCount } from '../src/utils/customerMessageData.mjs'

test('reads successful message lists and unread counts', () => {
  const messages = [{ id: 3, content: '你好' }]
  assert.equal(readCustomerMessageList({ code: 200, data: messages }), messages)
  assert.equal(readCustomerUnreadCount({ code: 200, data: '4' }), 4)
})

test('rejects failed or malformed message list responses instead of treating them as empty', () => {
  assert.throws(() => readCustomerMessageList({ code: 500, msg: '会话不可用' }), /会话不可用/)
  assert.throws(() => readCustomerMessageList({ code: 200, data: null }), CustomerMessageDataError)
})

test('rejects invalid unread counts', () => {
  assert.throws(() => readCustomerUnreadCount({ code: 200, data: -1 }), /格式异常/)
  assert.throws(() => readCustomerUnreadCount({ code: 200, data: 'many' }), /格式异常/)
})
