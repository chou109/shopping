import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'

const require = createRequire(new URL('../package.json', import.meta.url))
const { compileTemplate, parse } = require('@vue/compiler-sfc')
const filename = fileURLToPath(new URL('../src/views/login.vue', import.meta.url))

test('binds Enter on both password and captcha inputs to the Vue 3 login handler', () => {
  const { descriptor, errors } = parse(readFileSync(filename, 'utf8'), { filename })
  assert.deepEqual(errors, [])

  const compiled = compileTemplate({
    source: descriptor.template.content,
    filename,
    id: path.basename(filename)
  })
  assert.deepEqual(compiled.errors, [])
  assert.equal(compiled.code.split('onKeyup: _withKeys(_ctx.handleLogin').length - 1, 2)
})
