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

function collectImageAltIssues(node, filename) {
  const issues = []
  if (node.type === 1 && node.tag === 'img') {
    const hasAlt = node.props.some(property =>
      (property.type === 6 && property.name === 'alt') ||
      (property.type === 7 && property.name === 'bind' && property.arg?.content === 'alt')
    )
    if (!hasAlt) issues.push(`${path.relative(root, filename)}:${node.loc.start.line}: <img> 缺少 alt 属性`)
  }
  for (const child of node.children || []) issues.push(...collectImageAltIssues(child, filename))
  return issues
}

function collectLegacySlotIssues(node, filename) {
  const issues = []
  if (node.type === 1 && node.props.some(property => property.type === 6 && property.name === 'slot')) {
    issues.push(`${path.relative(root, filename)}:${node.loc.start.line}: 使用了 Vue 2 的 slot 属性，请改用 Vue 3 命名插槽语法`)
  }
  for (const child of node.children || []) issues.push(...collectLegacySlotIssues(child, filename))
  return issues
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
    if (!descriptor.template) continue

    const templateOptions = { source: descriptor.template.content, filename, id: filename }
    const parsedTemplate = compileTemplate(templateOptions)
    if (parsedTemplate.errors.length) {
      failures.push(`${path.relative(root, filename)}: ${parsedTemplate.errors.join('; ')}`)
      continue
    }
    failures.push(...collectImageAltIssues(parsedTemplate.ast, filename))
    failures.push(...collectLegacySlotIssues(parsedTemplate.ast, filename))
    if (!descriptor.scriptSetup) continue

    try {
      const script = compileScript(descriptor, { id: filename })
      for (const match of descriptor.scriptSetup.content.matchAll(/\bthis\.([A-Za-z_$][\w$]*)/g)) {
        if (Object.hasOwn(script.bindings, match[1])) {
          const line = descriptor.scriptSetup.content.slice(0, match.index).split('\n').length
          failures.push(`${path.relative(root, filename)}:${line}: script setup 不能通过 this.${match[1]} 访问 setup 绑定`)
        }
      }
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
  console.log('Vue 模板引用与图片替代文本检查通过')
}
