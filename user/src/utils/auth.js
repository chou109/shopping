import Cookies from 'js-cookie'

// const TokenKey = 'Admin-Token'
// 添加端口区分
const port = window.location.port || '80';
const TokenKey = `User-Token-${port}`;  // 改为：User-Token-18183

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}
