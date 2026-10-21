import test from 'node:test'
import assert from 'node:assert/strict'
import { getSafeQQLoginUrl } from '../src/utils/qq-login.mjs'

test('accepts the configured HTTPS QQ login endpoint', () => {
  const loginUrl = 'https://qq.wch666.com/api/qq.php?token=public&msg=user_login&display=pc'
  assert.equal(getSafeQQLoginUrl(loginUrl, 'http://localhost:18183'), loginUrl)
})

test('rejects insecure, unrelated, or malformed QQ login redirect targets', () => {
  const baseUrl = 'https://shop.example'
  for (const value of [
    'http://qq.wch666.com/api/qq.php',
    'https://attacker.example/api/qq.php',
    'https://qq.wch666.com/other.php',
    'https://qq.wch666.com:8443/api/qq.php',
    'https://user@qq.wch666.com/api/qq.php',
    '/api/qq.php'
  ]) {
    assert.throws(() => getSafeQQLoginUrl(value, baseUrl), /QQ 登录地址无效/)
  }
})
