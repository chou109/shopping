import test from 'node:test'
import assert from 'node:assert/strict'

import { useOrderList } from './useOrderList.mjs'

function deferred() {
  let resolve
  let reject
  const promise = new Promise((resolvePromise, rejectPromise) => {
    resolve = resolvePromise
    reject = rejectPromise
  })
  return { promise, resolve, reject }
}

test('loads the selected status and requested page', async () => {
  const calls = []
  const state = useOrderList(async (...args) => {
    calls.push(args)
    return { code: 200, rows: [{ id: 9 }], total: '21' }
  })
  state.activeTab.value = 'waitShip'
  state.pageNum.value = 3
  state.pageSize.value = 20

  await state.loadOrders()

  assert.deepEqual(calls, [[20, 3, 20]])
  assert.deepEqual(state.orderList.value, [{ id: 9 }])
  assert.equal(state.total.value, 21)
  assert.equal(state.loading.value, false)
})

test('shows an error instead of an empty state for a malformed successful list response', async () => {
  const state = useOrderList(async () => ({ code: 200, total: 0 }))

  await state.loadOrders()

  assert.deepEqual(state.orderList.value, [])
  assert.equal(state.total.value, 0)
  assert.equal(state.loadError.value, '订单数据格式异常，请重试')
  assert.equal(state.loading.value, false)
})

test('ignores a stale response after a newer status request succeeds', async () => {
  const older = deferred()
  const newer = deferred()
  let callCount = 0
  const state = useOrderList(() => (++callCount === 1 ? older.promise : newer.promise))

  const oldRequest = state.loadOrders()
  state.activeTab.value = 'waitShip'
  const newRequest = state.loadOrders()
  newer.resolve({ code: 200, rows: [{ id: 'new' }], total: 1 })
  await newRequest
  older.resolve({ code: 200, rows: [{ id: 'old' }], total: 1 })
  await oldRequest

  assert.deepEqual(state.orderList.value, [{ id: 'new' }])
})

test('backs up one page when deletion empties the current last page', async () => {
  const calls = []
  const state = useOrderList(async (status, pageNum) => {
    calls.push([status, pageNum])
    return pageNum === 3
      ? { code: 200, rows: [], total: 20 }
      : { code: 200, rows: [{ id: 'last-page' }], total: 20 }
  })
  state.pageNum.value = 3

  await state.loadOrders()

  assert.deepEqual(calls, [[null, 3], [null, 2]])
  assert.equal(state.pageNum.value, 2)
  assert.deepEqual(state.orderList.value, [{ id: 'last-page' }])
})

test('loads the three status badge counts from their matching filters', async () => {
  const calls = []
  const totals = { 10: 2, 20: 3, 30: 4 }
  const state = useOrderList(async (...args) => {
    calls.push(args)
    return { code: 200, total: totals[args[0]] }
  })

  await state.loadOrderCounts()

  assert.deepEqual(calls, [[10, 1, 1], [20, 1, 1], [30, 1, 1]])
  assert.deepEqual(state.orderCounts.value, { waitPay: 2, waitShip: 3, waitReceive: 4 })
  assert.equal(state.orderCountsError.value, '')
  assert.equal(state.orderCountsLoading.value, false)
})

test('reports failed order count filters and keeps valid counts available', async () => {
  const state = useOrderList(async status => {
    if (status === 20) throw new Error('network unavailable')
    return { code: 200, total: status === 10 ? 2 : 4 }
  })

  await state.loadOrderCounts()

  assert.deepEqual(state.orderCounts.value, { waitPay: 2, waitShip: 0, waitReceive: 4 })
  assert.equal(state.orderCountsError.value, '部分订单数量暂时无法更新，请重试')
  assert.equal(state.orderCountsLoading.value, false)
})

test('keeps valid order badge counts when one successful response has an invalid shape', async () => {
  const state = useOrderList(async status => {
    if (status === 20) return null
    return { code: 200, total: status === 10 ? 2 : 4 }
  })

  await state.loadOrderCounts()

  assert.deepEqual(state.orderCounts.value, { waitPay: 2, waitShip: 0, waitReceive: 4 })
  assert.equal(state.orderCountsError.value, '部分订单数量暂时无法更新，请重试')
  assert.equal(state.orderCountsLoading.value, false)
})

test('clears the order count error after a successful retry', async () => {
  let failed = true
  const state = useOrderList(async status => {
    if (failed && status === 20) return { code: 500, total: 0 }
    return { code: 200, total: status / 10 }
  })

  await state.loadOrderCounts()
  assert.notEqual(state.orderCountsError.value, '')

  failed = false
  await state.loadOrderCounts()

  assert.equal(state.orderCountsError.value, '')
  assert.deepEqual(state.orderCounts.value, { waitPay: 1, waitShip: 2, waitReceive: 3 })
})
