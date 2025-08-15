import axios from 'axios'
import auth from './auth'
import router from '@/router'

const service = axios.create({
    baseURL: window.API_BASE || 'http://localhost:8080', // 通过全局变量访问
    timeout: 10000
})

let isRefreshing = false
let requestsQueue = []

// 请求拦截器
service.interceptors.request.use(config => {
    const token = localStorage.getItem('access_token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// 响应拦截器
service.interceptors.response.use(
    response => response,
    async error => {
        if (error.response?.status === 401) {
            if (!isRefreshing) {
                isRefreshing = true
                try {
                    const newToken = await refreshToken()
                    auth.setToken(newToken)
                    requestsQueue.forEach(cb => cb(newToken))
                } catch {
                    auth.clearAll()
                    router.push('/login')
                } finally {
                    requestsQueue = []
                    isRefreshing = false
                }
            }
            return new Promise(resolve => {
                requestsQueue.push(token => {
                    error.config.headers.Authorization = `Bearer ${token}`
                    resolve(service(error.config))
                })
            })
        }
        return Promise.reject(error)
    }
)
// 定义一个 refreshToken 方法来刷新 token
async function refreshToken() {
    try {
        const response = await axios.post('/auth/refresh-token')
        return response.data.token
    } catch (error) {
        throw new Error('Token refresh failed')
    }
}

service.interceptors.response.use(
    response => response,
    async error => {
        // 增强401错误处理
        if (error.response?.status === 401) {
            console.warn('[token过期] 跳转登录页')
            localStorage.removeItem('userInfo')
            router.push('/login')
        }
        return Promise.reject(error)
    }
)

export default service