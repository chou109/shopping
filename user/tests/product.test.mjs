import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

const source = await readFile(new URL('../src/utils/product.js', import.meta.url), 'utf8')
const { isBlindBoxProduct } = await import(`data:text/javascript;base64,${Buffer.from(source).toString('base64')}`)

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
