import test from 'node:test'
import assert from 'node:assert/strict'

import { buildOrderReviewItems, submitOrderReviewBatch } from './orderReview.mjs'

test('builds review fields only when every order detail has a valid product ID', () => {
  const order = {
    orderDetails: [
      { productId: 7, productName: '商品 A', productMainImage: '/a.jpg' },
      { productId: '8', productName: '商品 B', productMainImage: '/b.jpg' }
    ]
  }
  assert.deepEqual(buildOrderReviewItems(order), [
    { productId: 7, productName: '商品 A', productMainImage: '/a.jpg', score: 5, content: '', isAnonymous: false },
    { productId: '8', productName: '商品 B', productMainImage: '/b.jpg', score: 5, content: '', isAnonymous: false }
  ])
  assert.deepEqual(buildOrderReviewItems({ orderDetails: [{ productId: 7 }, { productName: '缺少 ID' }] }), [])
  assert.deepEqual(buildOrderReviewItems({ orderDetails: [] }), [])
})

test('submits reviews sequentially and completes the order after all reviews succeed', async () => {
  const calls = []
  const order = { id: 42, orderNo: 'ORD-42' }
  const items = [
    { productId: 7, score: 5, content: '', isAnonymous: false },
    { productId: 8, score: 4, content: '不错', isAnonymous: true }
  ]

  await submitOrderReviewBatch(
    order,
    items,
    async payload => { calls.push(['review', payload.productId, payload]) },
    async orderNo => { calls.push(['complete', orderNo]) }
  )

  assert.deepEqual(calls.map(([kind, value]) => [kind, value]), [
    ['review', 7],
    ['review', 8],
    ['complete', 'ORD-42']
  ])
  assert.equal(calls[0][2].content, '用户未填写评价内容')
  assert.equal(calls[0][2].isAnonymous, 0)
  assert.equal(calls[1][2].isAnonymous, 1)
})

test('leaves the order incomplete after a partial failure and safely retries idempotent reviews', async () => {
  const order = { id: 42, orderNo: 'ORD-42' }
  const items = [
    { productId: 7, score: 5, content: '商品 A', isAnonymous: false },
    { productId: 8, score: 4, content: '商品 B', isAnonymous: false }
  ]
  const savedProductIds = new Set()
  const submittedProductIds = []
  let shouldFailSecondReview = true
  let completionCalls = 0
  const submitReview = async ({ productId }) => {
    submittedProductIds.push(productId)
    if (productId === 8 && shouldFailSecondReview) {
      shouldFailSecondReview = false
      throw new Error('短暂网络错误')
    }
    savedProductIds.add(productId)
  }
  const completeOrder = async () => { completionCalls += 1 }

  await assert.rejects(submitOrderReviewBatch(order, items, submitReview, completeOrder), /短暂网络错误/)
  assert.deepEqual([...savedProductIds], [7])
  assert.equal(completionCalls, 0)

  await submitOrderReviewBatch(order, items, submitReview, completeOrder)
  assert.deepEqual(submittedProductIds, [7, 8, 7, 8])
  assert.equal(completionCalls, 1)
})
