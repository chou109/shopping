import test from 'node:test'
import assert from 'node:assert/strict'

import { findPhoneCategory, readCategoryList, readProductRows } from './featuredProducts.mjs'

test('reads featured products from direct, table and Ajax responses', () => {
  const products = [{ id: 1, name: '测试商品' }]
  assert.equal(readProductRows(products), products)
  assert.equal(readProductRows({ code: 200, rows: products }), products)
  assert.equal(readProductRows({ code: 200, data: products }), products)
})

test('rejects failed and malformed featured product responses', () => {
  assert.throws(() => readProductRows({ code: 500, msg: '服务异常' }), /服务异常/)
  assert.throws(() => readProductRows({ code: 200, data: {} }), /商品列表响应格式异常/)
})

test('reads enabled categories from supported response shapes', () => {
  const categories = [{ id: 3, name: '手机数码' }]
  assert.equal(readCategoryList(categories), categories)
  assert.equal(readCategoryList({ code: 200, data: categories }), categories)
  assert.equal(readCategoryList({ code: 200, data: { rows: categories } }), categories)
})

test('rejects failed and malformed category responses', () => {
  assert.throws(() => readCategoryList({ code: 500, msg: '分类不可用' }), /分类不可用/)
  assert.throws(() => readCategoryList({ code: 200, data: {} }), /分类列表响应格式异常/)
})

test('finds phone and digital categories through nested children', () => {
  const category = { id: 8, name: '手机数码' }
  assert.equal(findPhoneCategory([{ id: 1, name: '家居', children: [category] }]), category)
  assert.equal(findPhoneCategory([{ id: 9, categoryName: '数码配件', childList: [] }]).id, 9)
  assert.equal(findPhoneCategory([{ id: 2, name: '服装' }]), null)
})
