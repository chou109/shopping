import test from 'node:test'
import assert from 'node:assert/strict'
import { createGogoHandoffContext, formatGogoHandoffDraft, getGogoHandoffReplyStatus } from './gogoHandoff.mjs'

test('creates human handoff context from the latest completed user turn', () => {
  const context = createGogoHandoffContext([
    { type: 'user', content: '旧问题' }, { type: 'ai', content: '旧答复' },
    { type: 'user', content: '怎么申请退款？' },
    { type: 'ai', content: '请到订单页提交售后申请。', sources: [{ title: '售后政策' }, { title: '退款流程' }] }
  ])
  assert.deepEqual(context, {
    question: '怎么申请退款？',
    answer: '请到订单页提交售后申请。',
    sources: ['售后政策', '退款流程']
  })
  const draft = formatGogoHandoffDraft(context)
  assert.match(draft, /我的问题：怎么申请退款？/)
  assert.match(draft, /逛逛答复：请到订单页提交售后申请。/)
  assert.match(draft, /参考资料：售后政策、退款流程/)
})

test('bounds user-submitted handoff drafts and ignores incomplete turns', () => {
  assert.equal(createGogoHandoffContext([{ type: 'user', content: '没有回答' }]), null)
  const draft = formatGogoHandoffDraft({ question: '问题'.repeat(400), answer: '答复'.repeat(1200), sources: ['政策'] })
  assert.equal(draft.length, 500)
  assert.ok(draft.startsWith('逛逛转人工咨询'))
})

test('recognizes pending and replied human handoffs from the message history', () => {
  const messages = [
    { id: 10, senderType: 1, content: '较早的问题' },
    { id: 11, senderType: 2, content: '较早的回复' },
    { id: 12, senderType: 1, content: '逛逛转人工咨询\n我的问题：问题摘要' },
    { id: 13, senderType: 2, content: '客服已查看' }
  ]
  assert.equal(getGogoHandoffReplyStatus(messages), 'replied')
  assert.equal(getGogoHandoffReplyStatus(messages.slice(0, 3)), 'waiting')
  assert.equal(getGogoHandoffReplyStatus(messages.slice(0, 2)), '')
  assert.equal(getGogoHandoffReplyStatus(null), '')
})
