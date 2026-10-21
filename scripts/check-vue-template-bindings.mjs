import { readdirSync, readFileSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createRequire } from 'node:module'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire(path.join(root, 'user/package.json'))
const { compileScript, compileTemplate, parse } = require('@vue/compiler-sfc')
const sourceRoots = ['user/src', 'admin/src']
const allowedGlobals = new Set(['$emit', '$slots', '$router', 'parseTime'])
const failures = []

function collectVueFiles(directory) {
  const files = []
  for (const entry of readdirSync(directory, { withFileTypes: true })) {
    const entryPath = path.join(directory, entry.name)
    if (entry.isDirectory()) files.push(...collectVueFiles(entryPath))
    else if (entry.isFile() && entry.name.endsWith('.vue')) files.push(entryPath)
  }
  return files
}

for (const sourceRoot of sourceRoots) {
  for (const filename of collectVueFiles(path.join(root, sourceRoot))) {
    const source = readFileSync(filename, 'utf8')
    const parsed = parse(source, { filename })
    if (parsed.errors.length) {
      failures.push(`${path.relative(root, filename)}: ${parsed.errors.map(error => error.message).join('; ')}`)
      continue
    }

    const { descriptor } = parsed
    if (!descriptor.scriptSetup || !descriptor.template) continue

    try {
      const script = compileScript(descriptor, { id: filename })
      const template = compileTemplate({
        source: descriptor.template.content,
        filename,
        id: filename,
        compilerOptions: { bindingMetadata: script.bindings }
      })
      if (template.errors.length) {
        failures.push(`${path.relative(root, filename)}: ${template.errors.join('; ')}`)
        continue
      }

      const unresolved = [...template.code.matchAll(/_ctx\.([A-Za-z_$][\w$]*)/g)]
        .map(match => match[1])
        .filter(name => !allowedGlobals.has(name))
      if (unresolved.length) {
        failures.push(`${path.relative(root, filename)}: 未声明的模板引用 ${[...new Set(unresolved)].join(', ')}`)
      }
    } catch (error) {
      failures.push(`${path.relative(root, filename)}: ${error.message}`)
    }
  }
}

if (failures.length) {
  console.error('Vue 模板引用检查失败：')
  for (const failure of failures) console.error(`- ${failure}`)
  process.exitCode = 1
} else {
  console.log('Vue 模板引用检查通过')
}
