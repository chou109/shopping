import request from '@/utils/request'

export function listAiKnowledge() {
  return request({ url: '/system/ai/knowledge/list', method: 'get' })
}

export function addAiKnowledge(data) {
  return request({ url: '/system/ai/knowledge', method: 'post', data })
}

export function updateAiKnowledge(data) {
  return request({ url: '/system/ai/knowledge', method: 'put', data })
}

export function updateAiKnowledgeStatus(id, enabled) {
  return request({ url: `/system/ai/knowledge/${id}/status`, method: 'put', params: { enabled } })
}

export function deleteAiKnowledge(id) {
  return request({ url: `/system/ai/knowledge/${id}`, method: 'delete' })
}

export function getAiKnowledgeMetrics() {
  return request({ url: '/system/ai/knowledge/metrics', method: 'get' })
}
