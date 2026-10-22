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
