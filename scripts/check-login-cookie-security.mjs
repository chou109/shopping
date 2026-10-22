import { existsSync, readFileSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const loginFiles = [
  'user/src/views/login.vue',
  'admin/src/views/login.vue'
]
const failures = []

for (const relativePath of loginFiles) {
  const source = readFileSync(path.join(root, relativePath), 'utf8')
  if (/jsencrypt|\b(?:encrypt|decrypt)\s*\(/i.test(source)) {
    failures.push(`${relativePath}: 登录页仍引用客户端可还原密码的加密逻辑`)
  }
  if (/Cookies\.(?:set|get)\s*\(\s*['"]password['"]/.test(source)) {
    failures.push(`${relativePath}: 登录页仍从 Cookie 保存或读取密码`)
  }
  if (!/Cookies\.remove\s*\(\s*['"]password['"]/.test(source)) {
    failures.push(`${relativePath}: 登录页未清理旧版密码 Cookie`)
  }
  if (!source.includes('记住账号')) {
    failures.push(`${relativePath}: 记住账号的界面说明缺失`)
  }
}

for (const relativePath of ['user/src/utils/jsencrypt.js', 'admin/src/utils/jsencrypt.js']) {
  if (existsSync(path.join(root, relativePath))) {
    failures.push(`${relativePath}: 含前端 RSA 私钥的旧工具仍存在`)
  }
}

for (const relativePath of ['user/package.json', 'admin/package.json']) {
  const packageJson = JSON.parse(readFileSync(path.join(root, relativePath), 'utf8'))
  if (packageJson.dependencies?.jsencrypt || packageJson.devDependencies?.jsencrypt) {
    failures.push(`${relativePath}: 已无用途的 jsencrypt 仍作为依赖安装`)
  }
}

if (failures.length) {
  console.error('登录页密码 Cookie 安全检查失败：')
  for (const failure of failures) console.error(`- ${failure}`)
  process.exitCode = 1
} else {
  console.log('登录页不再保存密码，旧密码 Cookie 会在打开登录页时清理')
}
