/**
 * Generate a UUID used to correlate WebSocket messages.
 */
export function generateUUID(): string {
  let uuid = ''
  const chars = '0123456789abcdef'

  for (let i = 0; i < 32; i++) {
    if (i === 8 || i === 12 || i === 16 || i === 20) {
      uuid += '-'
    }
    uuid += chars[Math.floor(Math.random() * chars.length)]
  }

  return uuid
}
