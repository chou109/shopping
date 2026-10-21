import assert from 'node:assert/strict'
import test from 'node:test'
import { buildClientHeaders } from '../src/utils/clientHeaders.mjs'

test('user client identity is stable across development and HTTPS ports', () => {
  assert.deepEqual(buildClientHeaders('18183'), { 'X-Client-Type': 'user', 'X-Client-Port': '18183' })
  assert.deepEqual(buildClientHeaders('443'), { 'X-Client-Type': 'user', 'X-Client-Port': '443' })
  assert.deepEqual(buildClientHeaders(''), { 'X-Client-Type': 'user', 'X-Client-Port': '80' })
})
