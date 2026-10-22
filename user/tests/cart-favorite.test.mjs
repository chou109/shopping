import test from 'node:test'
import assert from 'node:assert/strict'
import { moveCartItemToFavorite } from '../src/utils/cartFavorite.mjs'

test('adds a missing favorite before removing the cart item', async () => {
  const calls = []
  const result = await moveCartItemToFavorite({
    productId: 12,
    cartItemId: 34,
    checkFavorite: async (...args) => { calls.push(['check', ...args]); return { code: 200, data: false } },
    addFavorite: async (...args) => { calls.push(['favorite', ...args]); return { code: 200, data: true } },
    removeCartItem: async (...args) => { calls.push(['remove', ...args]); return { code: 200 } }
  })

  assert.deepEqual(calls, [['check', 1, 12], ['favorite', 1, 12], ['remove', 34]])
  assert.deepEqual(result, { alreadyFavorite: false })
})

test('does not add a duplicate favorite but still removes the cart item', async () => {
  let addCalled = false
  const result = await moveCartItemToFavorite({
    productId: 12,
    cartItemId: 34,
    checkFavorite: async () => ({ code: 200, data: { isFavorite: true } }),
    addFavorite: async () => { addCalled = true; return { code: 200 } },
    removeCartItem: async id => ({ code: id === 34 ? 200 : 500 })
  })

  assert.equal(addCalled, false)
  assert.deepEqual(result, { alreadyFavorite: true })
})

test('keeps the cart item when checking or adding the favorite fails', async () => {
  let removeCalled = false
  await assert.rejects(moveCartItemToFavorite({
    productId: 12,
    cartItemId: 34,
    checkFavorite: async () => ({ code: 200, data: false }),
    addFavorite: async () => ({ code: 500, msg: '收藏失败' }),
    removeCartItem: async () => { removeCalled = true; return { code: 200 } }
  }), /收藏失败/)
  assert.equal(removeCalled, false)
})

test('does not remove the cart item when the server reports that the favorite was not saved', async () => {
  let removeCalled = false
  await assert.rejects(moveCartItemToFavorite({
    productId: 12,
    cartItemId: 34,
    checkFavorite: async () => ({ code: 200, data: false }),
    addFavorite: async () => ({ code: 200, data: false }),
    removeCartItem: async () => { removeCalled = true; return { code: 200 } }
  }), /收藏失败/)
  assert.equal(removeCalled, false)
})

test('marks the favorite as saved when removing the cart item fails', async () => {
  await assert.rejects(moveCartItemToFavorite({
    productId: 12,
    cartItemId: 34,
    checkFavorite: async () => ({ code: 200, data: false }),
    addFavorite: async () => ({ code: 200, data: true }),
    removeCartItem: async () => { throw new Error('network unavailable') }
  }), error => {
    assert.equal(error.favoriteSaved, true)
    assert.equal(error.alreadyFavorite, false)
    assert.equal(error.message, 'network unavailable')
    return true
  })
})
