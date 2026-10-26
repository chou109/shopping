import test from 'node:test'
import assert from 'node:assert/strict'
import { getChatReconnectDelay, restoredHistoryContainsCompletedQuestion } from './chatConnection.mjs'

test('chat reconnect delay backs off and caps at 30 seconds', () => {
  assert.deepEqual([1, 2, 3, 4, 5, 6, 20].map(getChatReconnectDelay), [1500, 3000, 6000, 12000, 24000, 30000, 30000])
  assert.equal(getChatReconnectDelay(0), 1500)
})

test('only restored user turns followed by an assistant answer count as completed', () => {
  const oldCompletedTurn = [
    { role: 'user', content: '物流到哪了' },
    { role: 'assistant', content: '请查看订单页。' }
  ]
  assert.equal(restoredHistoryContainsCompletedQuestion(oldCompletedTurn, '物流到哪了'), true)
  assert.equal(restoredHistoryContainsCompletedQuestion(oldCompletedTurn, '物流到哪了', oldCompletedTurn), false)
  assert.equal(restoredHistoryContainsCompletedQuestion([
    ...oldCompletedTurn, { role: 'user', content: '物流到哪了' }, { role: 'assistant', content: '新答案' }
  ], '物流到哪了', oldCompletedTurn), true)
  assert.equal(restoredHistoryContainsCompletedQuestion([
    ...oldCompletedTurn, { role: 'user', content: '别的问题' }, { role: 'assistant', content: '别的答案' }
  ], '物流到哪了', oldCompletedTurn), false)
  assert.equal(restoredHistoryContainsCompletedQuestion([{ role: 'user', content: '物流到哪了' }], '物流到哪了'), false)
  assert.equal(restoredHistoryContainsCompletedQuestion([
    { role: 'user', content: '物流到哪了' }, { role: 'assistant', content: '旧答案' },
    { role: 'user', content: '物流到哪了' }
  ], '物流到哪了'), false)
  assert.equal(restoredHistoryContainsCompletedQuestion([{ role: 'assistant', content: '答案' }], '物流到哪了'), false)
})
