const TOKEN_KEY = 'access_token'
const USER_KEY = 'user_info'
const EXPIRE_KEY = 'token_expires'

export default {
    // 检查登录状态
    isLoggedIn() {
        const token = localStorage.getItem(TOKEN_KEY)
        const expires = parseInt(localStorage.getItem(EXPIRE_KEY) || 0)
        return !!token && Date.now() < expires
    },
    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },

    setToken(token, rememberMe) {
        localStorage.setItem(TOKEN_KEY, token)
        // 根据记住我设置过期时间
        const expires = Date.now() + (rememberMe ? 12*3600*1000 : 3600*1000) // 12小时或1小时
        localStorage.setItem(EXPIRE_KEY, expires)
    },

    removeToken() {
        localStorage.removeItem(TOKEN_KEY)
    },

    setUserInfo(user) {
        if (!user.id) {
            console.error('必须包含用户ID')
            return
        }
        localStorage.setItem(USER_KEY, JSON.stringify(user))
    },

    getUserInfo() {
        try {
            return JSON.parse(localStorage.getItem(USER_KEY)) || {}; // 空值返回空对象
        } catch (e) {
            return {};
        }
    },

    getUserRole() {
        return this.getUserInfo()?.role || 3
    },

    clearAll() {
        this.removeToken()
        localStorage.removeItem(EXPIRE_KEY)
        localStorage.removeItem(USER_KEY)
    },

    getUserId() {
        return this.getUserInfo()?.id || null
    }
}