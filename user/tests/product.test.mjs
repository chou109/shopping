import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

const source = await readFile(new URL('../src/utils/product.js', import.meta.url), 'utf8')
const { filterProductsByKeyword, isBlindBoxProduct } = await import(`data:text/javascript;base64,${Buffer.from(source).toString('base64')}`)

test('identifies blind box products by product id and supported API flag formats', () => {
  assert.equal(isBlindBoxProduct({ id: 9999 }), true)
  assert.equal(isBlindBoxProduct({ productId: '9999' }), true)
  assert.equal(isBlindBoxProduct({ isBlindBox: true }), true)
  assert.equal(isBlindBoxProduct({ isBlindBox: '1' }), true)
  assert.equal(isBlindBoxProduct({ is_blind_box: 1 }), true)
})

test('keeps ordinary products and missing values out of blind box classification', () => {
  assert.equal(isBlindBoxProduct({ id: 101, isBlindBox: 0, is_blind_box: false }), false)
  assert.equal(isBlindBoxProduct(null), false)
  assert.equal(isBlindBoxProduct(undefined), false)
})

test('searches shop products by name, subtitle, and description without case sensitivity', () => {
  const products = [
    { name: '商品 A', subTitle: '超长续航，玩得更久' },
    { name: '商品 B', subTitle: '轻薄机身', description: 'Long battery life' },
    { name: '商品 C', subTitle: '轻薄机身' }
  ]

  assert.deepEqual(filterProductsByKeyword(products, ' 长续航 '), [products[0]])
  assert.deepEqual(filterProductsByKeyword(products, 'LONG BATTERY'), [products[1]])
  assert.deepEqual(filterProductsByKeyword(products, ''), products)
  assert.deepEqual(filterProductsByKeyword(null, '手机'), [])
})
