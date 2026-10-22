import assert from 'node:assert/strict'
import test from 'node:test'
import { buildCategoryOptions, buildCategoryTree, fetchAllCategoryPages, getCategoryPageRows, getCategoryPageTotal } from '../src/utils/product-category.mjs'

test('builds a nested tree from flat category rows independent of row order', () => {
  const tree = buildCategoryTree([
    { id: 12, parentId: 10, name: '手机' },
    { id: 10, parentId: 0, name: '数码' },
    { id: 99, parentId: 88, name: '孤立分类' }
  ])

  assert.deepEqual(tree, [
    { id: 10, parentId: 0, name: '数码', children: [{ id: 12, parentId: 10, name: '手机', children: [] }] },
    { id: 99, parentId: 88, name: '孤立分类', children: [] }
  ])
})

test('creates leaf options with their complete category paths', () => {
  const tree = buildCategoryTree([
    { id: 1, parentId: 0, name: '数码' },
    { id: 2, parentId: 1, name: '手机' },
    { id: 3, parentId: 2, name: '安卓手机' },
    { id: 4, parentId: 1, name: '电脑' }
  ])

  assert.deepEqual(buildCategoryOptions(tree), [
    { id: 3, name: '数码 / 手机 / 安卓手机', originalName: '安卓手机', level: 2 },
    { id: 4, name: '数码 / 电脑', originalName: '电脑', level: 1 }
  ])
})

test('keeps categories already returned as a nested tree and handles an empty tree', () => {
  const nested = [{ id: 1, name: '服装', children: [{ id: 2, name: '鞋靴', children: [] }] }]
  assert.deepEqual(buildCategoryOptions(nested), [
    { id: 2, name: '服装 / 鞋靴', originalName: '鞋靴', level: 1 }
  ])
  assert.deepEqual(buildCategoryOptions([]), [])
})

test('reads paginated category responses from rows and preserves explicit totals', () => {
  const response = { rows: [{ id: 1 }], total: 31 }
  assert.deepEqual(getCategoryPageRows(response), [{ id: 1 }])
  assert.equal(getCategoryPageTotal(response), 31)
})

test('supports direct tree data responses and safely handles malformed payloads', () => {
  const response = { data: [{ id: 2 }] }
  assert.deepEqual(getCategoryPageRows(response), [{ id: 2 }])
  assert.equal(getCategoryPageTotal(response), 1)
  assert.deepEqual(getCategoryPageRows({ rows: null, data: null }), [])
  assert.equal(getCategoryPageTotal({ total: 'invalid' }), 0)
})

test('fetches all matching admin category pages with the same filters', async () => {
  const requests = []
  const rows = await fetchAllCategoryPages(async params => {
    requests.push(params)
    return params.pageNum === 1
      ? { code: 200, rows: [{ id: 1 }, { id: 2 }], total: 3 }
      : { code: 200, rows: [{ id: 3 }], total: 3 }
  }, { name: '手机', status: 0 }, 2)

  assert.deepEqual(rows, [{ id: 1 }, { id: 2 }, { id: 3 }])
  assert.deepEqual(requests, [
    { name: '手机', status: 0, pageNum: 1, pageSize: 2 },
    { name: '手机', status: 0, pageNum: 2, pageSize: 2 }
  ])
})

test('stops paged category loading on an API error', async () => {
  await assert.rejects(fetchAllCategoryPages(async () => ({ code: 500, msg: 'denied' })), /denied/)
})
