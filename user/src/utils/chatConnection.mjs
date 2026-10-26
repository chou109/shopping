const INITIAL_RECONNECT_DELAY_MS = 1500
const MAX_RECONNECT_DELAY_MS = 30000

export function getChatReconnectDelay(attempt) {
  const safeAttempt = Math.max(1, Math.floor(Number(attempt) || 1))
  const exponent = Math.min(safeAttempt - 1, 5)
  return Math.min(MAX_RECONNECT_DELAY_MS, INITIAL_RECONNECT_DELAY_MS * (2 ** exponent))
}

export function restoredHistoryContainsCompletedQuestion(messages, question, previousMessages = []) {
  if (!question || !Array.isArray(messages)) return false
  const normalize = items => items.map(message => ({ role: message?.role, content: message?.content }))
  if (JSON.stringify(normalize(messages)) === JSON.stringify(normalize(Array.isArray(previousMessages) ? previousMessages : []))) return false
  let lastUserIndex = -1
  for (let index = messages.length - 1; index >= 0; index--) {
    if (messages[index]?.role === 'user') { lastUserIndex = index; break }
  }
  if (lastUserIndex < 0 || messages[lastUserIndex].content !== question) return false
  const answer = messages[lastUserIndex + 1]
  return answer?.role === 'assistant' && typeof answer.content === 'string' && Boolean(answer.content.trim())
}
