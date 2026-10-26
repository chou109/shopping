export const GOGO_HANDOFF_STORAGE_KEY = 'gogo-human-handoff-v1'

export function getGogoHandoffReplyStatus(messages) {
  if (!Array.isArray(messages)) return ''
  let handoffIndex = -1
  for (let index = messages.length - 1; index >= 0; index--) {
    const message = messages[index]
    if (Number(message?.senderType) === 1
      && typeof message.content === 'string'
      && message.content.trim().startsWith('逛逛转人工咨询')) {
      handoffIndex = index
      break
    }
  }
  if (handoffIndex < 0) return ''
  return messages.slice(handoffIndex + 1).some(message => Number(message?.senderType) === 2) ? 'replied' : 'waiting'
}

export function createGogoHandoffContext(chatMessages, selectedAnswer = null) {
  if (!Array.isArray(chatMessages)) return null
  let userIndex = -1
  for (let index = chatMessages.length - 1; index >= 0; index--) {
    if (chatMessages[index]?.type === 'user' && typeof chatMessages[index].content === 'string') {
      userIndex = index
      break
    }
  }
  if (userIndex < 0) return null
  const answer = selectedAnswer?.type === 'ai' && typeof selectedAnswer.content === 'string' && selectedAnswer.content.trim()
    ? selectedAnswer
    : chatMessages.slice(userIndex + 1).find(message => message?.type === 'ai' && typeof message.content === 'string' && message.content.trim())
  if (!answer) return null
  return {
    question: chatMessages[userIndex].content.trim().slice(0, 1000),
    answer: answer.content.trim().slice(0, 2000),
    sources: Array.isArray(answer.sources)
      ? answer.sources.slice(0, 3).map(source => String(source?.title || '').trim().slice(0, 120)).filter(Boolean)
      : []
  }
}

export function formatGogoHandoffDraft(context) {
  if (!context || typeof context.question !== 'string' || !context.question.trim()) return ''
  const sections = [
    `逛逛转人工咨询\n我的问题：${context.question.trim()}`,
    typeof context.answer === 'string' && context.answer.trim() ? `逛逛答复：${context.answer.trim()}` : '',
    Array.isArray(context.sources) && context.sources.length ? `参考资料：${context.sources.filter(Boolean).slice(0, 3).join('、')}` : ''
  ].filter(Boolean)
  const draft = sections.join('\n')
  return draft.length <= 500 ? draft : `${draft.slice(0, 499)}…`
}
