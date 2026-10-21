export function logoutWithLocalCleanup(requestLogout, userStore, removeToken) {
  return requestLogout().finally(() => {
    userStore.token = ''
    userStore.roles = []
    userStore.permissions = []
    userStore.userId = null
    userStore.name = ''
    userStore.avatar = ''
    removeToken()
  })
}
