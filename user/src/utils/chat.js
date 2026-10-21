const HTML_ESCAPES = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;',
  "'": '&#39;'
}

export function formatChatMessage(content) {
  if (!content) return ''

  return String(content)
    .replace(/[&<>"']/g, character => HTML_ESCAPES[character])
    .replace(/\n/g, '<br>')
    .replace(/【(.*?)】/g, '<strong class="highlight">【$1】</strong>')
    .replace(/价格：\s*(\d+(\.\d{1,2})?)元/g, '价格：<span class="price">$1元</span>')
}
