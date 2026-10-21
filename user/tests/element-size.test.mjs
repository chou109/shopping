import test from 'node:test'
import assert from 'node:assert/strict'
import { normalizeElementSize } from '../src/utils/element-size.mjs'

test('keeps supported Element Plus sizes', () => {
  assert.equal(normalizeElementSize('default'), 'default')
  assert.equal(normalizeElementSize('small'), 'small')
  assert.equal(normalizeElementSize('large'), 'large')
})

test('maps legacy RuoYi sizes to supported Element Plus sizes', () => {
  assert.equal(normalizeElementSize('medium'), 'default')
  assert.equal(normalizeElementSize('mini'), 'small')
})

test('uses the default size for missing or unknown values', () => {
  assert.equal(normalizeElementSize(undefined), 'default')
  assert.equal(normalizeElementSize('compact'), 'default')
})
