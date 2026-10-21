import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const sourceRoots = ['user/src', 'admin/src']
const extensions = ['.vue', '.js', '.jsx', '.ts', '.tsx', '.json', '.scss', '.css']
const failures = []
const importPattern = /(?:\bfrom\s*|\bimport\s*\(\s*|\bimport\s*)['"](@\/[^'"]+)['"]/g

function collectSourceFiles(directory) {
  const files = []
  for (const entry of readdirSync(directory, { withFileTypes: true })) {
    const filename = path.join(directory, entry.name)
    if (entry.isDirectory()) files.push(...collectSourceFiles(filename))
    else if (entry.isFile() && /\.(vue|js|jsx|ts|tsx)$/.test(entry.name)) files.push(filename)
  }
  return files
}

function caseInsensitiveMatch(directory, name) {
  const lowerName = name.toLowerCase()
  return readdirSync(directory).find(entry => entry.toLowerCase() === lowerName)
}

function inspectImport(sourceRoot, specifier, importer) {
  const parts = specifier.slice(2).split('/').filter(Boolean)
  let current = path.join(root, sourceRoot)

  for (let index = 0; index < parts.length; index += 1) {
    const part = parts[index]
    const isLast = index === parts.length - 1
    const directoryEntries = readdirSync(current)
    const exactEntry = directoryEntries.includes(part) ? part : null
    const extensionMatches = isLast && !path.extname(part)
      ? extensions.map(extension => `${part}${extension}`).filter(candidate => directoryEntries.includes(candidate))
      : []
    const selected = extensionMatches[0] || exactEntry

    if (selected) {
      const selectedPath = path.join(current, selected)
      if (!isLast && !statSync(selectedPath).isDirectory()) {
        failures.push(`${path.relative(root, importer)}: 导入 ${specifier} 的 ${part} 不是目录`)
        return
      }
      current = selectedPath
      continue
    }

    const actualCase = caseInsensitiveMatch(current, part)
    if (actualCase) {
      failures.push(`${path.relative(root, importer)}: 导入路径 ${specifier} 大小写不匹配，实际目录项为 ${actualCase}`)
      return
    }

    if (isLast && !path.extname(part)) {
      const extensionMatch = extensions
        .map(extension => caseInsensitiveMatch(current, `${part}${extension}`))
        .find(Boolean)
      if (extensionMatch) {
        failures.push(`${path.relative(root, importer)}: 导入路径 ${specifier} 大小写不匹配，实际文件为 ${extensionMatch}`)
        return
      }
    }

    failures.push(`${path.relative(root, importer)}: 导入路径 ${specifier} 不存在`)
    return
  }

  if (current === path.join(root, sourceRoot) || !existsSync(current)) {
    failures.push(`${path.relative(root, importer)}: 导入路径 ${specifier} 不存在`)
    return
  }

  if (statSync(current).isDirectory()) {
    const entries = readdirSync(current)
    const indexFiles = extensions.map(extension => `index${extension}`)
    if (!indexFiles.some(name => entries.includes(name))) {
      failures.push(`${path.relative(root, importer)}: 导入目录 ${specifier} 没有可解析的 index 文件`)
    }
  }
}

for (const sourceRoot of sourceRoots) {
  const sourceDirectory = path.join(root, sourceRoot)
  for (const importer of collectSourceFiles(sourceDirectory)) {
    const source = readFileSync(importer, 'utf8')
    for (const match of source.matchAll(importPattern)) {
      inspectImport(sourceRoot, match[1], importer)
    }
  }
}

if (failures.length) {
  console.error('大小写敏感导入检查失败：')
  for (const failure of [...new Set(failures)]) console.error(`- ${failure}`)
  process.exitCode = 1
} else {
  console.log('大小写敏感导入检查通过')
}
