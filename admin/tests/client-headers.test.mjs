import assert from 'node:assert/strict'
import test from 'node:test'
import { buildClientHeaders } from '../src/utils/clientHeaders.mjs'

test('admin client identity is stable across development and HTTPS ports', () => {
  assert.deepEqual(buildClientHeaders('8080'), { 'X-Client-Type': 'admin', 'X-Client-Port': '8080' })
  assert.deepEqual(buildClientHeaders('443'), { 'X-Client-Type': 'admin', 'X-Client-Port': '443' })
  assert.deepEqual(buildClientHeaders(''), { 'X-Client-Type': 'admin', 'X-Client-Port': '80' })
})
