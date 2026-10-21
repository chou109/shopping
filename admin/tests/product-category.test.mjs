import assert from 'node:assert/strict'
import test from 'node:test'
import { buildCategoryOptions, buildCategoryTree } from '../src/utils/product-category.mjs'

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
