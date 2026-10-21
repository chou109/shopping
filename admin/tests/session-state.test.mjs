import assert from 'node:assert/strict'
import test from 'node:test'

import { logoutWithLocalCleanup } from '../src/utils/session-state.mjs'

function createUserStore() {
  return {
    token: 'expired-token',
    roles: ['admin'],
    permissions: ['system:user:list'],
    userId: 7,
    name: '管理员',
    avatar: '/profile/avatar.png'
  }
}

test('clears local credentials and user data when remote logout succeeds', async () => {
  const userStore = createUserStore()
  let removeTokenCalls = 0

  const result = await logoutWithLocalCleanup(
    () => Promise.resolve({ code: 200 }),
    userStore,
    () => { removeTokenCalls += 1 }
  )

  assert.deepEqual(result, { code: 200 })
  assert.deepEqual(userStore, {
    token: '',
    roles: [],
    permissions: [],
    userId: null,
    name: '',
    avatar: ''
  })
  assert.equal(removeTokenCalls, 1)
})

test('clears local credentials and user data when remote logout fails', async () => {
  const userStore = createUserStore()
  const logoutError = new Error('network unavailable')
  let removeTokenCalls = 0

  await assert.rejects(
    logoutWithLocalCleanup(
      () => Promise.reject(logoutError),
      userStore,
      () => { removeTokenCalls += 1 }
    ),
    error => error === logoutError
  )

  assert.deepEqual(userStore, {
    token: '',
    roles: [],
    permissions: [],
    userId: null,
    name: '',
    avatar: ''
  })
  assert.equal(removeTokenCalls, 1)
})
