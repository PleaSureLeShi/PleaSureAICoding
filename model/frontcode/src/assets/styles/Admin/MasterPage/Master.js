import PlatformInfo from "@/assets/pages/Admin/MasterPage/InfoPage/MasterPlatformInfo.vue";
import UserManagement from "@/assets/pages/Admin/MasterPage/InfoPage/MasterUserManagement.vue";
import RoomManagement from "@/assets/pages/Admin/MasterPage/InfoPage/MasterRoomManagement.vue";
import AdminList from "@/assets/pages/Admin/MasterPage/InfoPage/MasterAdminList.vue";
import logo from "@/assets/images/SystemLogo.png";
import request from '@/utils/request'
import auth from "@/utils/auth.js"
import {ElMessage} from "element-plus";
import response from "assert";

export default {
    data() {
        return {
            currentView: "PlatformInfo", // 默认显示平台信息页面
            user: {
                avatar: logo, // 用户头像
                username: "master_user", // 用户名
                nickname: "站长", // 昵称
                role: 1, // 角色 1=站长, 2=管理员, 3=普通用户
            },
            users: [], // 用户列表
            admins: [], // 管理员列表
            rooms: [], // 房间列表
            platformData: {
                userCount: 0,
                activeUsers: 0,
                roomCount: 0,
            }, // 平台信息数据
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
        // 处理管理员权限更新
        handleUpdateAdmin(userId, isAdmin) {
            const user = this.users.find((u) => u.id === userId);
            if (user) {
                user.role = isAdmin ? 2 : 3; // 2=管理员, 3=普通用户
                this.updateAdminStatus(userId, isAdmin);
            }
        },
        // 处理用户封禁状态切换
        async handleToggleBanUser(user) {
            try {
                await request.post(`/api/users/${user.id}/ban`, {isBanned: user.isBanned})
                this.fetchUsers()
            } catch (error) {
                // console.error("切换用户封禁状态失败:", error);
            }
        },
        // 处理房间封禁状态切换
        async handleToggleBanRoom(room) {
            try {
                const res = await request.post(`/api/master/rooms/${room.id}/ban`, {
                    banned: !room.isBanned
                })
                if (response.ok) {
                    this.$message.success(room.isBanned ? "房间已解封" : "房间已封禁");
                    this.fetchRooms(); // 刷新房间列表
                } else {
                    this.$message.error("操作失败");
                }
            } catch (error) {
                // console.error("切换房间封禁状态失败:", error);
            }
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
        // 从后端获取用户列表
        async fetchUsers() {
            this.loading = true;
            try {
                const res = await request.get('/api/users', {
                    params: {
                        page: this.page,
                        size: this.pageSize
                    }
                });

                const dataArray = res.data?.users || [];
                this.users = dataArray.map(user => ({
                    ...user,
                    role: user.role || 3,
                    isBanned: user.isBanned,
                    status: user.status || 1
                }));

                this.total = res.data?.total || 0;
            } catch (error) {
                ElMessage.error('获取用户列表失败');
            } finally {
                this.loading = false;
            }
        },
        // 从后端获取管理员列表
        async fetchAdmins() {
            try {
                const res = await request.get("/api/admins", {
                    params: {
                        page: this.page,
                        pageSize: 10 // 添加分页参数
                    }
                });
                this.admins = res.data.admins || [];
                this.total = res.data.total || 0;
            } catch (error) {
                console.error("获取管理员列表失败:", error);
            }
        },
        // 从后端获取房间列表
        async fetchRooms() {
            try {
                const res = await request.get("/api/master/rooms")
                this.rooms = res.data
            } catch (error) {
                console.error("获取房间列表失败:", error);
            }
        },
        // 从后端获取平台信息
        async fetchPlatformData() {
            try {
                const res = await request.get("/api/master/platform-info"); // 第171行
                this.platformData = {
                    userCount: res.data.userCount,
                    activeUsers: res.data.activeUsers,
                    roomCount: res.data.roomCount
                };
            } catch (error) {
                console.error("获取平台信息失败:", error);
            }
        },
        // 更新管理员状态
        async updateAdminStatus(userId, isAdmin) {
            try {
                if (isAdmin) {
                    // 设置管理员使用PUT方法
                    const response = await request.put(`/api/admin/users/${userId}/role`,
                        JSON.stringify({ role: 2 }), // 显式转换为JSON字符串
                        {
                            headers: {
                                'Content-Type': 'application/json',
                                'Authorization': `Bearer ${auth.getToken()}`
                            }
                        }
                    );
                    console.log('设置管理员成功:', response); // 添加日志
                } else {
                    // 撤销管理员使用POST方法
                    const response = await request.post(`/api/users/${userId}/revoke-admin`, {}, {
                        headers: {
                            'Authorization': `Bearer ${auth.getToken()}`
                        }
                    });
                }
                this.fetchUsers();
            } catch (error) {
                console.error("管理员状态更新失败:", error);
                ElMessage.error(`操作失败: ${error.response?.data?.message || '服务器错误'}`);
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
        // 添加异步检查
        this.$nextTick(async () => {
            try {
                const userRole = auth.getUserRole()
                if (userRole != 1) {
                    console.warn('[权限不足] 需要站长权限')
                    this.$router.push('/home')
                    return
                }
                await this.fetchUserInfo()
                await Promise.all([
                    this.fetchUsers(),
                    this.fetchAdmins(),
                    this.fetchRooms(),
                    this.fetchPlatformData()
                ])
            } catch (error) {
                console.error('[MasterPage 初始化失败]', error)
            }
        })
    }
};