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

function collectAnchorIssues(node, filename) {
  const issues = []
  if (node.type === 1 && node.tag === 'a') {
    const hasHref = node.props.some(property =>
      (property.type === 6 && property.name === 'href') ||
      (property.type === 7 && property.name === 'bind' && property.arg?.content === 'href')
    )
    const hasClick = node.props.some(property => property.type === 7 && property.name === 'on' && property.arg?.content === 'click')
    if (hasClick && !hasHref) {
      issues.push(`${path.relative(root, filename)}:${node.loc.start.line}: 可点击的 <a> 缺少 href，请使用链接或按钮语义`)
    }
  }
  for (const child of node.children || []) issues.push(...collectAnchorIssues(child, filename))
  return issues
}

function collectTargetBlankIssues(node, filename) {
  const issues = []
  if (node.type === 1) {
    const targetBlank = node.props.some(property =>
      (property.type === 6 && property.name === 'target' && property.value?.content === '_blank') ||
      (property.type === 7 && property.name === 'bind' && property.arg?.content === 'target' && property.exp?.content?.trim().replace(/^['"]|['"]$/g, '') === '_blank')
    )
    if (targetBlank) {
      const relValue = node.props.find(property =>
        (property.type === 6 && property.name === 'rel') ||
        (property.type === 7 && property.name === 'bind' && property.arg?.content === 'rel')
      )
      const rel = relValue?.type === 6 ? relValue.value?.content : relValue?.exp?.content?.replace(/^['"]|['"]$/g, '')
      const relTokens = new Set((rel || '').split(/\s+/))
      if (!relTokens.has('noopener') || !relTokens.has('noreferrer')) {
        issues.push(`${path.relative(root, filename)}:${node.loc.start.line}: target="_blank" 需同时设置 rel="noopener noreferrer"`)
      }
    }
  }
  for (const child of node.children || []) issues.push(...collectTargetBlankIssues(child, filename))
  return issues
}

function collectButtonIssues(node, filename) {
  const issues = []
  if (node.type === 1 && node.tag === 'button') {
    const hasType = node.props.some(property =>
      (property.type === 6 && property.name === 'type') ||
      (property.type === 7 && property.name === 'bind' && property.arg?.content === 'type')
    )
    if (!hasType) {
      issues.push(`${path.relative(root, filename)}:${node.loc.start.line}: 原生 <button> 缺少 type 属性，请明确指定 button、submit 或 reset`)
    }
  }
  for (const child of node.children || []) issues.push(...collectButtonIssues(child, filename))
  return issues
}

function collectDeprecatedElementPlusButtonIssues(node, filename) {
  const issues = []
  if (node.type === 1 && node.tag === 'el-button' && node.props.some(property =>
    property.type === 6 && property.name === 'type' && property.value?.content === 'text'
  )) {
    issues.push(`${path.relative(root, filename)}:${node.loc.start.line}: el-button 的 type="text" 已弃用，请改用 link 属性`)
  }
  for (const child of node.children || []) issues.push(...collectDeprecatedElementPlusButtonIssues(child, filename))
  return issues
}

function collectEmptyElementPlusOptionIssues(node, filename) {
  const issues = []
  if (node.type === 1 && node.tag === 'el-option' && node.props.some(property =>
    property.type === 7 && property.name === 'bind' && property.arg?.content === 'value'
      && ['null', 'undefined'].includes(property.exp?.content?.trim())
  )) {
    issues.push(`${path.relative(root, filename)}:${node.loc.start.line}: el-option 的值不能为 null 或 undefined，请依靠可清除选择器表示空选项`)
  }
  for (const child of node.children || []) issues.push(...collectEmptyElementPlusOptionIssues(child, filename))
  return issues
}

function collectClickableElementIssues(node, filename) {
  const issues = []
  const nativeInteractiveElements = new Set([
    'a', 'button', 'input', 'select', 'textarea', 'option', 'summary', 'details', 'label', 'audio', 'video', 'area'
  ])
  const isNativeNonInteractiveElement = node.type === 1 &&
    !node.tag.includes('-') &&
    !nativeInteractiveElements.has(node.tag)
  if (isNativeNonInteractiveElement) {
    const props = node.props || []
    const hasClick = props.some(property => property.type === 7 && property.name === 'on' && property.arg?.content === 'click')
    if (hasClick) {
      const hasRole = props.some(property =>
        (property.type === 6 && property.name === 'role') ||
        (property.type === 7 && property.name === 'bind' && property.arg?.content === 'role')
      )
      const hasTabindex = props.some(property =>
        (property.type === 6 && property.name === 'tabindex') ||
        (property.type === 7 && property.name === 'bind' && property.arg?.content === 'tabindex')
      )
      const hasKeyboardHandler = props.some(property =>
        property.type === 7 && property.name === 'on' && ['keydown', 'keyup', 'keypress'].includes(property.arg?.content)
      )
      if (!hasRole || !hasTabindex || !hasKeyboardHandler) {
        issues.push(`${path.relative(root, filename)}:${node.loc.start.line}: 可点击的 <${node.tag}> 需提供 role、tabindex 和键盘事件；优先改用原生按钮或链接`)
      }
    }
  }
  for (const child of node.children || []) issues.push(...collectClickableElementIssues(child, filename))
  return issues
}

function collectLegacySlotIssues(node, filename) {
  const issues = []
  if (node.type === 1 && node.props.some(property => property.type === 6 && property.name === 'slot')) {
    issues.push(`${path.relative(root, filename)}:${node.loc.start.line}: 使用了 Vue 2 的 slot 属性，请改用 Vue 3 命名插槽语法`)
  }
  if (node.type === 1 && node.props.some(property =>
    property.type === 7 && property.name === 'on' && !property.arg && property.exp?.content === '$listeners'
  )) {
    issues.push(`${path.relative(root, filename)}:${node.loc.start.line}: Vue 3 已移除 $listeners，请依赖根节点事件透传`)
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
    failures.push(...collectAnchorIssues(parsedTemplate.ast, filename))
    failures.push(...collectTargetBlankIssues(parsedTemplate.ast, filename))
    failures.push(...collectButtonIssues(parsedTemplate.ast, filename))
    failures.push(...collectDeprecatedElementPlusButtonIssues(parsedTemplate.ast, filename))
    failures.push(...collectEmptyElementPlusOptionIssues(parsedTemplate.ast, filename))
    failures.push(...collectClickableElementIssues(parsedTemplate.ast, filename))
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
