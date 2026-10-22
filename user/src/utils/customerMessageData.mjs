export class CustomerMessageDataError extends Error {
  constructor(message) {
    super(message)
    this.name = 'CustomerMessageDataError'
  }
}

export function readCustomerMessageList(response, label = '消息列表') {
  if (Number(response?.code) !== 200) {
    throw new CustomerMessageDataError(response?.msg || `${label}加载失败，请重试`)
  }
  if (!Array.isArray(response.data)) {
    throw new CustomerMessageDataError(`${label}响应格式异常，请重试`)
  }
  return response.data
}

export function readCustomerUnreadCount(response) {
  if (Number(response?.code) !== 200) {
    throw new CustomerMessageDataError(response?.msg || '未读消息数量加载失败')
  }
  const count = Number(response.data)
  if (response.data == null || !Number.isSafeInteger(count) || count < 0) {
    throw new CustomerMessageDataError('未读消息数量响应格式异常')
  }
  return count
}
