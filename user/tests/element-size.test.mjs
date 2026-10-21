import test from 'node:test'
import assert from 'node:assert/strict'
import { readdirSync, readFileSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { normalizeElementSize } from '../src/utils/element-size.mjs'

const sourceDirectory = fileURLToPath(new URL('../src', import.meta.url))

function collectSourceFiles(directory) {
  return readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const entryPath = path.join(directory, entry.name)
    return entry.isDirectory()
      ? collectSourceFiles(entryPath)
      : /\.(vue|js|mjs)$/.test(entry.name) ? [entryPath] : []
  })
}

test('keeps supported Element Plus sizes', () => {
  assert.equal(normalizeElementSize('default'), 'default')
  assert.equal(normalizeElementSize('small'), 'small')
  assert.equal(normalizeElementSize('large'), 'large')
})

test('maps legacy RuoYi sizes to supported Element Plus sizes', () => {
  assert.equal(normalizeElementSize('medium'), 'default')
  assert.equal(normalizeElementSize('mini'), 'small')
})

test('uses the default size for missing or unknown values', () => {
  assert.equal(normalizeElementSize(undefined), 'default')
  assert.equal(normalizeElementSize('compact'), 'default')
})

test('does not pass unsupported medium or mini sizes to Element Plus', () => {
  const invalidSize = /\bsize\s*=\s*["'](?:medium|mini)["']|\bsize\s*:\s*["'](?:medium|mini)["']/
  const invalidFiles = collectSourceFiles(sourceDirectory).filter((file) => invalidSize.test(readFileSync(file, 'utf8')))
  assert.deepEqual(invalidFiles, [])
})
