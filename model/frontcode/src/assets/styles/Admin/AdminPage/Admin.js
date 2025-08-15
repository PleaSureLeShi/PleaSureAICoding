import PlatformInfo from "@/assets/pages/Admin/AdminPage/InfoPage/PlatformInfo.vue";
import UserManagement from "@/assets/pages/Admin/AdminPage/InfoPage/UserManagement.vue";
import RoomManagement from "@/assets/pages/Admin/AdminPage/InfoPage/RoomManagement.vue";
import AdminList from "@/assets/pages/Admin/AdminPage/InfoPage/AdminList.vue";
import logo from "@/assets/images/SystemLogo.png";
import request from '@/utils/request'
import auth from "@/utils/auth.js";

export default {
    data() {
        return {
            currentView: "PlatformInfo", // 默认显示平台信息页面
            user: {
                avatar: logo, // 用户头像
                role: 2, // 角色 1=站长, 2=管理员, 3=普通用户
            },
        };
    },
    methods: {
        // 处理下拉菜单命令
        handleCommand(command) {
            if (command === "logout") {
                this.handleLogout();
            } else if (command === "profile") {
                this.goToProfile();
            }
        },
        // 退出登录
        handleLogout() {
            localStorage.removeItem('access_token')
            localStorage.removeItem('user_info') // 清除用户信息
            this.$router.push("/login"); // 跳转到登录页
        },
        // 进入个人中心
        goToProfile() {
            this.$router.push("/my"); // 跳转到个人中心页
        },
        // 跳转到首页
        goToHomePage() {
            this.$router.push("/");
        },
        // 跳转到好友页面
        goToFriendPage() {
            this.$router.push("/friend");
        },
        // 跳转到房间页面
        goToRoomPage() {
            this.$router.push("/room");
        },
        // 跳转到个人页面
        goToMyPage() {
            this.$router.push("/my");
        },
        // 跳转到管理员管理页
        goToAdminPage() {
            this.$router.push("/admin");
        },
        // 跳转到站长管理页
        goToMasterPage() {
            this.$router.push("/master");
        },
        // 处理侧边栏菜单选择
        handleMenuSelect(index) {
            const views = {
                platform: "PlatformInfo",
                users: "UserManagement",
                rooms: "RoomManagement",
                admins: "AdminList",
            };
            this.currentView = views[index];
        },
        // 从后端获取用户信息
        async fetchUserInfo() {
            this.loading = true;
            try {
                const userId = auth.getUserId();
                if (!userId) throw new Error("用户ID不存在");
                // 显式传递用户ID
                const res = await request.get(`/api/user/home/${userId}/info`)
                // 更新用户信息
                this.user = {
                    ...res.data,
                    avatar: res.data.avatar || logo
                };
            } catch (error) {
                // console.error('获取用户信息失败:', error);
            } finally {
                this.loading = false;
            }
        },
        // 从后端获取数据
        async fetchData() {
            try {
                const [userRes, roomRes, adminRes] = await Promise.all([
                    request.get('/api/users'),
                    request.get('/api/rooms'),
                    request.get('/api/admins')
                ])
                this.users = userRes.data
                this.rooms = roomRes.data
                this.admins = adminRes.data
            } catch (error) {
                // console.error("获取数据失败:", error);
            }
        },
    },
    components: {
        PlatformInfo,
        UserManagement,
        RoomManagement,
        AdminList,
    },
    mounted() {
        // 权限检查
        const userRole = JSON.parse(localStorage.getItem('user_info'))?.role || 3
        if (userRole != 2) {
            this.$router.push('/home')
        }
        this.fetchUserInfo()
    },
};