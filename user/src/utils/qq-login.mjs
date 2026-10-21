const QQ_LOGIN_ORIGIN = 'https://qq.wch666.com'
const QQ_LOGIN_PATH = '/api/qq.php'

export function getSafeQQLoginUrl(value, baseUrl) {
  const url = new URL(value, baseUrl)
  if (
    url.protocol !== 'https:' ||
    url.origin !== QQ_LOGIN_ORIGIN ||
    url.pathname !== QQ_LOGIN_PATH ||
    url.username ||
    url.password
  ) {
    throw new Error('QQ 登录地址无效')
  }
  return url.toString()
}
