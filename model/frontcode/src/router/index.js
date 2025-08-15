import {createRouter, createWebHistory} from 'vue-router'
import auth from '@/utils/auth'
import LoginPage from '@/assets/pages/LoginPage/LoginPage.vue'
import HomePage from '@/assets/pages/HomePage/HomePage.vue'
import FriendPage from '@/assets/pages/FriendPage/FriendPage.vue'
import RoomPage from '@/assets/pages/RoomPage/RoomPage.vue'
import MyPage from '@/assets/pages/MyPage/MyPage.vue'
import MasterPage from '@/assets/pages/Admin/MasterPage/MasterPage.vue'
import AdminPage from '@/assets/pages/Admin/AdminPage/AdminPage.vue'
import request from '@/utils/request'
import {ElMessage} from "element-plus";

const routes = [
    // 默认重定向
    {
        path: '/',
        redirect: '/home',
        meta: {
            requiresAuth: true
        }
    },

    // 登录页
    {
        path: '/login',
        component: LoginPage,
        meta: {
            guestOnly: true  // 仅未登录用户可访问
        }
    },

    // 需要认证的路由
    {
        path: '/home',
        component: HomePage,
        meta: {
            requiresAuth: true,
            title: '首页'
        }
    },
    {
        path: '/friend',
        component: FriendPage,
        meta: {
            requiresAuth: true,
            title: '好友'
        }
    },
    {
        path: '/room',
        component: RoomPage,
        meta: {
            requiresAuth: true,
            title: '房间'
        }
    },
    {
        path: '/my',
        component: MyPage,
        meta: {
            requiresAuth: true,
            title: '个人中心'
        }
    },

    // 管理员路由
    {
        path: '/master',
        component: MasterPage,
        meta: {
            requiresAuth: true,
            role: 1,
            title: '站长管理'
        }
    },
    {
        path: '/admin',
        component: AdminPage,
        meta: {
            requiresAuth: true,
            role: 2,
            title: '管理员面板'
        }
    },
]

const router = createRouter({
    history: createWebHistory(window.API_BASE),
    routes,
    // 统一设置滚动行为
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        } else {
            return {top: 0}
        }
    }
})

// 全局路由守卫
router.beforeEach(async (to) => {
    if (to.meta.requiresAuth && !auth.isLoggedIn()) {
        auth.clearAll() // 清除过期token
        return { path: '/login', query: { redirect: to.fullPath } }
    }

    if (to.meta.role) {
        try {
            const userId = auth.getUserId()
            if (!userId) throw new Error("用户未登录")

            // 使用标准用户信息接口
            const res = await request.get(`/api/user/${userId}/info`)
            const userRole = res.data.role || auth.getUserRole()

            if (userRole !== to.meta.role) {
                return { path: '/home', query: { error: '权限不足' } }
            }
            if (userId) {
                const res = await request.get(`/api/user/home/${userId}/info`);
                if (res.data.status === 0) {
                    ElMessage.warning('账号已被封禁');
                    auth.clearAll();
                    return '/login';
                }
                // 更新本地存储状态
                auth.setUserInfo(res.data);
            }
        } catch (e) {
            auth.clearAll()
            return '/login'
        }
    }
})


export default router