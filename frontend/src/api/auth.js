import request from './request'

/**
 * 演示用登录：不校验密码，直接按用户名签发 token。
 * admin 有全部权限，guest 只能问答、不能上传文档。
 */
export function login(username = 'admin') {
  return request.post('/auth/login', null, {
    params: { username },
    skipErrorHandler: true,
  })
}

export function logout() {
  return request.post('/auth/logout')
}

export function getUserInfo() {
  return request.get('/auth/info')
}
