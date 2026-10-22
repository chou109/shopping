import assert from 'node:assert/strict'
import { readdir, readFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const app = process.argv[2]
assert.ok(app === 'user' || app === 'admin', '用法：node scripts/check-svg-sprites.mjs user|admin')

const iconDirectory = path.join(root, app, 'src/assets/icons/svg')
const files = (await readdir(iconDirectory)).filter((name) => name.endsWith('.svg')).sort()
const expectedIds = new Set()

for (const file of files) {
  const id = `icon-${path.basename(file, '.svg')}`
  assert.ok(!expectedIds.has(id), `${app} 图标名称重复：${id}`)
  expectedIds.add(id)

  const source = await readFile(path.join(iconDirectory, file), 'utf8')
  const stack = []
  for (const match of source.matchAll(/<\/?([A-Za-z_][\w:.-]*)\b[^>]*>/g)) {
    const [tag, name] = match
    if (tag.startsWith('</')) {
      assert.equal(stack.pop(), name, `${app}/${file} XML 标签不匹配：${name}`)
    } else if (!tag.endsWith('/>')) {
      stack.push(name)
    }
  }
  assert.deepEqual(stack, [], `${app}/${file} 存在未闭合的 XML 标签`)
  assert.match(source, /<svg\b/, `${app}/${file} 缺少 SVG 根节点`)
}

console.log(`${app} SVG 源文件检查通过：${files.length} 个图标名称唯一且 XML 结构有效`)
