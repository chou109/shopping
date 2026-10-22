import test from 'node:test'
import assert from 'node:assert/strict'

import { getDashboardActionRoute } from '../src/utils/dashboardRoutes.mjs'

test('maps every dashboard shortcut to a management route', () => {
  assert.equal(getDashboardActionRoute('product'), '/shopping/product')
  assert.equal(getDashboardActionRoute('order'), '/order/info')
  assert.equal(getDashboardActionRoute('shop'), '/store/store')
  assert.equal(getDashboardActionRoute('user'), '/system/user')
})

test('ignores unknown dashboard shortcut actions', () => {
  assert.equal(getDashboardActionRoute('unknown'), null)
  assert.equal(getDashboardActionRoute(null), null)
})
