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

test('keeps user-info loading in the login error chain and clears a partial login', () => {
  const source = readFileSync(filename, 'utf8')
  const storeSource = readFileSync(new URL('../src/store/modules/user.js', import.meta.url), 'utf8')
  assert.match(source, /\.then\(\(\) => this\.\$store\.dispatch\("GetInfo"\)\)/)
  assert.match(source, /this\.\$store\.dispatch\('FedLogOut'\)\.finally\(/)
  assert.match(source, /验证码加载失败，请点击验证码区域重试/)
  assert.match(storeSource, /function clearUserState\(commit\)[\s\S]*SET_NAME[\s\S]*SET_AVATAR[\s\S]*SET_USERID[\s\S]*SET_REALNAME[\s\S]*SET_ROLES[\s\S]*SET_PERMISSIONS/)
})

test('honors the server captcha switch on consumer login and registration forms', () => {
  const loginSource = readFileSync(filename, 'utf8')
  const registerFilename = fileURLToPath(new URL('../src/views/register.vue', import.meta.url))
  const registerSource = readFileSync(registerFilename, 'utf8')
  const { descriptor: loginDescriptor } = parse(loginSource, { filename })
  const { descriptor: registerDescriptor } = parse(registerSource, { filename: registerFilename })
  const loginTemplate = compileTemplate({ source: loginDescriptor.template.content, filename, id: 'login' })
  const registerTemplate = compileTemplate({ source: registerDescriptor.template.content, filename: registerFilename, id: 'register' })

  assert.deepEqual(loginTemplate.errors, [])
  assert.deepEqual(registerTemplate.errors, [])
  assert.match(loginTemplate.code, /_ctx\.captchaEnabled/)
  assert.match(registerTemplate.code, /_ctx\.captchaEnabled/)
  assert.match(loginSource, /this\.captchaEnabled && !value/)
  assert.match(registerSource, /if \(!captchaEnabled\.value\)/)
})
