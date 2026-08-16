import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// ==================== Mock 数据（无需后端即可调试）====================
const USE_MOCK = false // 切换为 false 即使用真实后端

const mockUsers = {
  admin: { password: '123456', userType: 2, id: 1, username: 'admin', nickname: '系统管理员', avatar: '' },
  user:  { password: '123456', userType: 1, id: 2, username: 'user',  nickname: '普通用户',   avatar: '' }
}

const mockLogin = (username, password) => {
  const u = mockUsers[username]
  if (!u || u.password !== password) {
    return { code: '500', msg: '账号或密码错误' }
  }
  const token = 'mock-token-' + username + '-' + Date.now()
  const userInfo = { id: u.id, username: u.username, nickname: u.nickname, avatar: u.avatar, userType: u.userType }
  return { code: '200', data: { token, userInfo } }
}
// ====================================================================

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 请求的前缀
  timeout: 5000, // 请求的超时时间
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // Mock：登录接口直接本地返回
    if (USE_MOCK && config.url === '/user/login') {
      const body = config.data || {}
      const result = mockLogin(body.username, body.password)
      return Promise.reject({ __MOCK_OK__: true, __MOCK_RESULT__: result })
    }
    // 在发送请求之前做些什么
    // 后端 jwt.header=Authorization, jwt.token-prefix="Bearer "
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    // Mock 成功回调路径
    if (error && error.__MOCK_OK__) {
      return Promise.reject(error)
    }
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const { data, config } = response
    // 后端统一返回 { code, data, msg }，成功码为 "200"
    if (data.code === '200' || data.code === 200) {
        return data.data
    }
    // 非成功业务码（如 "500"、"A0301" 等）
    const msg = data.msg || '请求失败'
    // 登录接口的错误由调用方(login.vue)自行处理，不在这里重复提示
    if (!config.url?.includes('/login')) {
        ElMessage.error(msg)
    }
    return Promise.reject(new Error(msg))
  },
  (error) => {
    // Mock 结果：从 request 拦截器转发过来
    if (error && error.__MOCK_OK__) {
      const result = error.__MOCK_RESULT__
      if (result.code === '200') {
        return result.data
      } else {
        ElMessage.error(result.msg || '请求失败')
        return Promise.reject(result.msg)
      }
    }
    // 401: 登录态问题 - 不再一刀切立刻清token+跳转（避免"登录成功刚跳进来又被踢回去"的死循环）
    // 只提示错误，让页面自行处理；多次出现后用户自然会手动去重新登录
    if (error?.response?.status === 401) {
      const currentPath = router.currentRoute.value.path
      // 仅在以下情况才清token跳转：当前在后台管理页 /back/*，且token确实失效
      const isBackendPath = currentPath.startsWith('/back/')
      if (isBackendPath && !currentPath.startsWith('/auth/')) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElMessage.warning('登录状态已失效，请重新登录')
        const redirect = encodeURIComponent(window.location.pathname + window.location.search)
        router.replace(`/auth/login${redirect ? '?redirect=' + redirect : ''}`)
      } else {
        // 前台页面只给提示，不清token不强制跳转（让用户至少还能看页面）
        ElMessage.warning('当前无法访问该功能')
      }
      return Promise.reject(error)
    }
    // 404: 接口不存在，静默处理（后端可能还在开发中），只打印日志不弹窗
    if (error?.response?.status === 404) {
      console.warn(`[API 未实现] ${error?.config?.url}`);
      // 返回一个空对象让 .then() 或 await 代码继续执行，避免业务层再次报错
      return Promise.resolve({ records: [], total: 0 }); 
    }

    // 其他错误统一提示
    const msg = error?.response?.data?.msg || error?.message || '请求失败'
    // 登录接口的错误由调用方自行处理（login.vue 已有 catch）
    if (!error?.config?.url?.includes('/login')) {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default service