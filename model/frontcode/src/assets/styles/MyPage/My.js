import request from '@/utils/request'
import auth from '@/utils/auth'
import defaultAvatar from '@/assets/images/SystemLogo.png'

export default {
    data() {
        return {
            API_BASE: window.API_BASE || 'http://localhost:8080',
            auth: auth,
            user: {
                id: null,
                username: "",
                nickname: "",
                phone: "",
                email: "",
                avatar: defaultAvatar,
                bio: "",
                tags: [],
                friendCount: 0,
                roomCount: 0,
                role: 3,
                status: 1,
                isOnline: 0,
                loading: false // 添加加载状态
            },
            availableTags: ["技术", "学习", "运动", "音乐", "电影"], // 可选的标签
            createdRooms: [], // 已创建房间
            createdRoomsPage: 1, // 已创建房间当前页码
            createdRoomsTotal: 0, // 已创建房间总数
            joinedRooms: [], // 已加入房间
            joinedRoomsPage: 1, // 已加入房间当前页码
            joinedRoomsTotal: 0, // 已加入房间总数
        };
    },
    methods: {
        // 获取用户信息
        async fetchUserInfo() {
            this.loading = true;
            try {
                const userId = auth.getUserId();
                if (!userId) throw new Error("用户未登录");
                // 显式传递用户ID
                const res = await request.get(`/api/user/my/${userId}/info`);

                // 统一处理用户数据
                this.user = {
                    ...this.user,
                    ...res.data,
                    phone: res.data.phone || '未填写',
                    email: res.data.email || '未填写',
                    bio: res.data.bio || '',
                    avatar: res.data.avatar || defaultAvatar, // 头像逻辑
                    tags: res.data.tags
                        ? res.data.tags.split(',').map(tag => tag.trim())
                        : [],
                    friendCount: res.data.friendCount/2
                };
            } catch (error) {
                // console.error('获取用户信息失败:', error);
                this.$message.error('获取个人信息失败');
            } finally {
                this.loading = false;
            }
        },
        // 获取已创建房间
        async fetchCreatedRooms() {
            try {
                const userId = auth.getUserId();
                const pageSize = 10;
                const res = await request.get(`/api/user/${userId}/created-rooms`, {
                    params: { page: this.createdRoomsPage, pageSize }
                });
                this.createdRooms = (res.data || []).map(room => ({
                    id: room.id,
                    name: room.name||'未命名房间',
                    userCount: room.userCount
                }));
                this.createdRoomsTotal = res.data?.total || 0;
            } catch (error) {
                // console.error("获取已创建房间失败",);
            }
        },
        // 获取已加入房间
        async fetchJoinedRooms() {
            try {
                const userId = auth.getUserId();
                const pageSize = 10;
                const res = await request.get(`/api/user/${userId}/joined-rooms`, {
                    params: { page: this.joinedRoomsPage, pageSize }
                });

                this.joinedRooms = (res.data || []).map(room => ({
                    id: room.id,
                    name: room.name||'未命名房间',
                    userCount: room.userCount
                }));
                this.joinedRoomsTotal = res.data?.total || 0;
            } catch (error) {
                console.error("获取已加入房间失败");
            }
        },

        // 进入房间
        enterRoom(roomId) {
            this.$router.push(`/room/${roomId}`);
        },
        // 跳转到好友页面
        goToFriendPage() {
            this.$router.push("/friend");
        },
        // 跳转到房间页面
        goToRoomPage() {
            this.$router.push("/room");
        },
        // 跳转到首页
        goToHomePage() {
            this.$router.push("/");
        },
        // 跳转到我的页面
        goToMyPage() {
            this.$router.push("/my");
        },
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
            this.$router.push("/my");
        },
        // 跳转到站长管理页
        goToAdminPage() {
            this.$router.push('/admin');
        },
        // 跳转到管理员管理页
        goToMasterPage() {
            this.$router.push('/master');
        },
        // 更新昵称
        async updateNickname() {
            try {
                const userId = auth.getUserId();
                if (!userId) throw new Error("用户未登录");
                await request.put(`/api/user/${userId}/update-nickname`, {
                    nickname: this.user.nickname
                });

                this.$message.success("昵称更新成功");
                await this.fetchUserInfo(); // 刷新数据
            } catch (error) {
                console.error("更新昵称失败:", error);
                this.$message.error('昵称更新失败');
            }
        },

        // 更新个人简介
        async updateBio() {
            try {
                const userId = auth.getUserId();
                if (!userId) throw new Error("用户未登录");

                // 统一使用request并传递用户ID
                await request.put(`/api/user/${userId}/update-bio`, {
                    bio: this.user.bio
                });

                this.$message.success("个人简介更新成功");
            } catch (error) {
                console.error("更新个人简介失败:", error);
                this.$message.error('简介更新失败');
            }
        },

        // 更新标签
        async updateTags() {
            try {
                const userId = auth.getUserId();
                if (!userId) throw new Error("用户未登录");
                await request.put(`/api/user/${userId}/update-tags`, {
                    tags: Array.isArray(this.user.tags) ? this.user.tags : []
                });

                this.$message.success("标签更新成功");
            } catch (error) {
                console.error("更新标签失败:", error);
                this.$message.error('标签更新失败');
            }
        },

        // 头像上传前的校验
        beforeAvatarUpload(file) {
            const allowedTypes = ['image/jpeg', 'image/png', 'image/gif'];
            const isImage = allowedTypes.includes(file.type);
            const isLt2M = file.size / 1024 / 1024 < 2;

            if (!isImage) {
                this.$message.error('仅支持 JPG/PNG/GIF 格式!');
            }
            if (!isLt2M) {
                this.$message.error('头像大小不能超过 2MB!');
            }

            return isImage && isLt2M;
        },

        // 头像上传
        async handleCustomUpload({ file }) {
            const userId = auth.getUserId();
            if (!userId) throw new Error("用户未登录");

            const formData = new FormData()
            formData.append('avatarFile', file)

            try {
                const res = await request.put( // 使用导入的request
                    `${this.API_BASE}/api/user/${userId}/avatar`,
                    formData,
                    {
                        headers: {
                            'Content-Type': 'multipart/form-data',
                            'Authorization': `Bearer ${this.auth.getToken()}`
                        }
                    }
                )
                // 双保险更新
                this.user.avatar = URL.createObjectURL(file) // 本地预览
                await this.fetchUserInfo() // 获取后端存储的真实数据
                this.$message.success('头像上传成功')
            } catch (error) {
                console.error('上传失败:', error)
                this.$message.error(`上传失败: ${error.response?.data?.message || error.message}`)
            }
        },

    },



    async mounted() {
        // 初始化数据
        await this.fetchUserInfo();
        await this.fetchCreatedRooms();
        await this.fetchJoinedRooms();
    },
};