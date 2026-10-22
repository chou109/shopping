import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

const source = await readFile(new URL('../src/directive/common/copyText.js', import.meta.url), 'utf8')
const copyTextDirective = (await import(`data:text/javascript;base64,${Buffer.from(source).toString('base64')}`)).default

test('updates copied values and callbacks, then removes its click listener on unmount', () => {
  const listeners = new Map()
  const element = {
    addEventListener(type, handler) {
      listeners.set(type, handler)
    },
    removeEventListener(type, handler) {
      if (listeners.get(type) === handler) listeners.delete(type)
    }
  }
  const initialCallback = () => {}
  const updatedCallback = () => {}

  copyTextDirective.beforeMount(element, { value: 'old value' })
  copyTextDirective.beforeMount(element, { value: initialCallback, arg: 'callback' })
  copyTextDirective.updated(element, { value: 'new value' })
  copyTextDirective.updated(element, { value: updatedCallback, arg: 'callback' })

  assert.equal(element.$copyValue, 'new value')
  assert.equal(element.$copyCallback, updatedCallback)
  assert.equal(listeners.has('click'), true)

  copyTextDirective.beforeUnmount(element)

  assert.equal(listeners.has('click'), false)
  assert.equal('$copyValue' in element, false)
  assert.equal('$copyCallback' in element, false)
})
