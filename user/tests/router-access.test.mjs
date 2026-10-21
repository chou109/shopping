import test from 'node:test'
import assert from 'node:assert/strict'
import { registerAccessRoutes, requiresAuthentication } from '../src/router/access.mjs'

test('requires authentication when a parent route requires it', () => {
  assert.equal(requiresAuthentication({
    matched: [{ meta: {} }, { meta: { requiresAuth: true } }]
  }), true)
})

test('allows public routes and routes without explicit protection', () => {
  assert.equal(requiresAuthentication({ matched: [{ meta: { requiresAuth: false } }] }), false)
  assert.equal(requiresAuthentication({ matched: [{ meta: {} }] }), false)
})

test('registers each generated route with Vue Router', () => {
  const added = []
  const router = { addRoute: route => added.push(route) }
  const routes = [{ path: '/products' }, { path: '/orders' }]

  registerAccessRoutes(router, routes)

  assert.deepEqual(added, routes)
})
