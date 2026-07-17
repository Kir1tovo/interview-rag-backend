const TOKEN_KEY = 'token'
const USER_ID_KEY = 'userId'
const USERNAME_KEY = 'username'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUserId() {
  const val = localStorage.getItem(USER_ID_KEY)
  return val ? Number(val) : null
}

export function setUserId(userId) {
  localStorage.setItem(USER_ID_KEY, String(userId))
}

export function getUsername() {
  return localStorage.getItem(USERNAME_KEY) || ''
}

export function setUsername(username) {
  localStorage.setItem(USERNAME_KEY, username)
}

/** 登录成功后一次性存储所有用户信息 */
export function setLoginInfo({ token, userId, username }) {
  setToken(token)
  setUserId(userId)
  setUsername(username)
}

/** 注销：清除所有登录信息 */
export function clearLoginInfo() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_ID_KEY)
  localStorage.removeItem(USERNAME_KEY)
}