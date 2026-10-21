import test from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'

const source = await readFile(new URL('../src/utils/chat.js', import.meta.url), 'utf8')
const { formatChatMessage } = await import(`data:text/javascript;base64,${Buffer.from(source).toString('base64')}`)

test('escapes untrusted HTML while retaining chat line breaks and formatting', () => {
  assert.equal(
    formatChatMessage('<img src=x onerror="alert(1)">\n【推荐商品】价格：12.50元'),
    '&lt;img src=x onerror=&quot;alert(1)&quot;&gt;<br><strong class="highlight">【推荐商品】</strong>价格：<span class="price">12.50元</span>'
  )
})

test('returns empty output for empty chat messages', () => {
  assert.equal(formatChatMessage(''), '')
  assert.equal(formatChatMessage(null), '')
})
