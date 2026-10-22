import test from 'node:test'
import assert from 'node:assert/strict'

import {
  DashboardDataError,
  readDashboardArray,
  readDashboardObject,
  readDashboardSalesTrend
} from '../src/utils/dashboardData.mjs'

test('reads successful dashboard object, list, and trend responses', () => {
  const stats = { role: 'admin' }
  const rows = [{ title: '订单数量', value: '2' }]
  const trend = { dates: ['2026-10-22'], amounts: [10], orderCounts: [1] }
  assert.equal(readDashboardObject({ code: 200, data: stats }), stats)
  assert.equal(readDashboardArray({ code: 200, data: rows }, '统计卡片'), rows)
  assert.equal(readDashboardSalesTrend({ code: 200, data: trend }), trend)
})

test('rejects failed or malformed dashboard responses', () => {
  assert.throws(() => readDashboardArray({ code: 500, msg: '统计服务不可用' }, '订单列表'), /统计服务不可用/)
  assert.throws(() => readDashboardObject({ code: 200, data: [] }), DashboardDataError)
  assert.throws(() => readDashboardArray({ code: 200, data: [null] }, '商品列表'), DashboardDataError)
  assert.throws(() => readDashboardSalesTrend({ code: 200, data: { dates: [], amounts: [1], orderCounts: [] } }), /格式异常/)
})
