import request from '@/utils/request'
import auth from "@/utils/auth.js";
import defaultAvatar from "@/assets/images/SystemLogo.png";

export default {
    data() {
        return {
            // 用户信息
            user: {
                id: null,
                username: '',
                nickname: '',
                avatar: '', // 头像 URL
                friendCount: 0,
                roomCount: 0,
                role: 3,
                status: 1,
                isOnline: 0,
                defaultAvatar: defaultAvatar,
            },
            // 好友数据
            friends: [], // 已添加好友
            friendRequests: [], // 好友请求
            selectedFriend: null, // 当前选中的好友
            chatMessages: [], // 聊天消息
            newMessage: '', // 新消息
            socket: null, // WebSocket 实例
            // AI 对话框相关
            aiDialogVisible: false, // 控制 AI 对话框显示
            aiInput: '', // 用户输入的问题
            aiMessages: [], // AI 对话记录
        };
    },
    methods: {
        // 跳转页面
        goToHomePage() {
            this.$router.push('/');
        },
        goToRoomPage() {
            this.$router.push('/room');
        },
        goToMyPage() {
            this.$router.push('/my');
        },
        goToMasterPage() {
            this.$router.push('/master');
        },
        goToAdminPage() {
            this.$router.push('/admin');
        },
        // 处理下拉菜单命令
        handleCommand(command) {
            if (command === 'logout') {
                this.handleLogout();
            } else if (command === 'profile') {
                this.goToProfile();
            }
        },
        // 退出登录
        handleLogout() {
            localStorage.removeItem('access_token')
            localStorage.removeItem('user_info')
            this.$router.push('/login');
        },
        // 进入个人中心
        goToProfile() {
            this.$router.push('/my');
        },
        // 查看好友主页
        async viewFriendProfile(friend) {
            try {
                const res = await request.get(`/api/user/my/${friend.id}/info`);

                this.selectedFriend = {
                    ...res.data,
                    tags: res.data.tags
                        ? res.data.tags.split(',').map(tag => tag.trim())
                        : [],
                    bio: res.data.bio || '暂无简介',
                    avatar: res.data.avatar || this.defaultAvatar
                };

                await this.fetchChatMessages(friend.id);
            } catch (error) {
                this.$message.error('好友信息加载失败');
            }
        },
        // 初始化 WebSocket 连接
        initSocket() {
            if (this.socket) return;

            const userId = auth.getUserId();
            const token = auth.getToken(); // 假设从 auth 中获取 token
            if (!userId || !token) {
                // console.error('用户ID或Token不存在');
                return;
            }

            // 构建 WebSocket 连接 URL
            const wsUrl = `ws://localhost:8080/ws/single/${userId}`;
            this.socket = new WebSocket(wsUrl);

            this.socket.onopen = () => {
                console.log('WebSocket 连接成功');
                this.startHeartbeat(); // 启动心跳机制
            };

            this.socket.onmessage = async (event) => {
                const message = JSON.parse(event.data);
                if (message.type === 'pong') {
                    console.log('收到 Pong 响应');
                    return;
                }
                // console.log('收到消息:', message);
                const currentUserId = auth.getUserId();
                if (this.selectedFriend) {
                    const isRelevantMessage =
                        (message.from === currentUserId && message.to === this.selectedFriend.id) ||
                        (message.from === this.selectedFriend.id && message.to === currentUserId);

                    if (isRelevantMessage) {
                        this.chatMessages.push({
                            content: message.message,
                            sender_name: message.from === currentUserId ? '我' : this.selectedFriend.nickname,
                            timestamp: new Date(message.timestamp).toLocaleString()
                        });
                        await this.fetchChatMessages(this.selectedFriend.id);
                    }
                }
            };

            this.socket.onerror = (error) => {
                console.error('WebSocket 错误:', error);
            };

            this.socket.onclose = (event) => {
                console.log('WebSocket 连接关闭:', event.code, event.reason);
                this.stopHeartbeat(); // 关闭心跳机制
            };

            // 监听页面可见性变化
            document.addEventListener('visibilitychange', () => {
                if (document.visibilityState === 'visible') {
                    console.log('页面可见');
                } else {
                    console.log('页面不可见，发送心跳包保持连接');
                    if (this.socket?.readyState === WebSocket.OPEN) {
                        this.socket.send(JSON.stringify({ type: 'ping' }));
                    }
                }
            });
        },

        // 心跳机制
        startHeartbeat() {
            this.heartbeatInterval = setInterval(() => {
                if (this.socket?.readyState === WebSocket.OPEN) {
                    console.log('发送心跳包');
                    this.socket.send(JSON.stringify({ type: 'ping' })); // 发送心跳包
                }
            }, 30000); // 每 30 秒发送一次
        },

        stopHeartbeat() {
            if (this.heartbeatInterval) {
                clearInterval(this.heartbeatInterval);
            }
        },
        // 获取聊天消息
        async fetchChatMessages(friendId) {
            try {
                const userId = auth.getUserId();
                const res = await request.get(`/getMessages`, {
                    params: {
                        user1: userId,
                        user2: friendId,
                        page: 0,
                        size: 30,
                    }
                });

                this.chatMessages = res.data.content.reverse().map(msg => ({
                    content: msg.content,
                    sender_name: msg.from === userId ? '我' : this.selectedFriend.nickname,
                    timestamp: new Date(msg.timestamp).toLocaleString()
                }));
            } catch (error) {
                console.error('获取消息失败:', error);
            }
        },

        // 发送消息
        async sendMessage() {
            if (!this.newMessage.trim()) return;
            const userId = auth.getUserId();

            // WebSocket 发送格式必须与接口文档一致
            if (this.socket?.readyState === WebSocket.OPEN) {
                const message = {
                    from: userId,
                    to: this.selectedFriend.id,
                    message: this.newMessage, // 字段名需对应接口要求
                    timestamp: new Date().toISOString()
                };
                console.log('发送消息:', message); // 打印发送的消息
                this.socket.send(JSON.stringify(message));
            }

            // 清空输入并立即本地显示（无需等待接口返回）
            this.chatMessages.push({
                content: this.newMessage,
                sender_name: '我',
                timestamp: new Date().toLocaleString()
            });
            this.newMessage = '';
        },
        // 同意好友请求
        async acceptFriendRequest(friendReq) {
            try {
                await request.post(`/api/friends/requests/${friendReq.id}/accept`);
                this.$message.success('已接受好友请求');
                this.fetchFriendRequests();
                this.fetchFriends();
            } catch (error) {
                this.$message.error(error.response?.data?.message || '操作失败');
            }
        },
        // 拒绝好友请求
        async rejectFriendRequest(friendReq) {
            try {
                await request.post(`/api/friends/requests/${friendReq.id}/reject`);
                this.$message.success('已拒绝好友请求');
                this.fetchFriendRequests();
            } catch (error) {
                this.$message.error(error.response?.data?.message || '操作失败');
            }
        },
        // 获取好友列表
        async fetchFriends() {
            try {
                const userId = auth.getUserId();
                const res = await request.get(`/api/${userId}/friends`);

                this.friends = await Promise.all(
                    res.data.map(async friend => {
                        try {
                            const detailRes = await request.get(`/api/user/my/${friend.id}/info`);

                            return {
                                ...friend,
                                tags: detailRes.data.tags
                                    ? detailRes.data.tags.split(',').map(tag => tag.trim())
                                    : [],
                                bio: detailRes.data.bio || '暂无简介',
                                avatar: detailRes.data.avatar || this.defaultAvatar
                            };
                        } catch (error) {
                            return friend;
                        }
                    })
                );
            } catch (error) {
                console.error('获取好友列表失败');
            }
        },
        // 获取好友请求
        async fetchFriendRequests() {
            try {
                const userId = auth.getUserId();
                const res = await request.get(`/api/${userId}/friends/requests`);
                this.friendRequests = res.data;
            } catch (error) {
                console.error('获取好友请求失败:', error);
            }
        },
        // 获取用户信息
        async fetchUserInfo() {
            this.loading = true;
            try {
                const userId = auth.getUserId();
                if (!userId) throw new Error("用户ID不存在");
                const res = await request.get(`/api/user/${userId}/info`);
                // 更新用户信息
                this.user = {
                    ...res.data,
                    avatar: res.data.avatar || defaultAvatar
                };
            } catch (error) {
                console.error('获取用户信息失败:', error);
            } finally {
                this.loading = false;
            }
        },
        // 显示 AI 对话框
        showAIDialog() {
            this.aiDialogVisible = true;
        },
        // 发送问题给 AI
        // AI询问
        async sendToAI() {
            if (!this.aiInput.trim()) return;

            this.aiMessages.push({role: 'user', content: this.aiInput});

            try {
                const res = await request.post('/api/ai/ask', {
                    question: this.aiInput
                })
                this.aiMessages.push({
                    role: 'ai',
                    content: res.data.answer
                })
            } catch (error) {
                console.error('AI请求失败:', error)
            }

            this.aiInput = ''
        },
    },
    async mounted() {
        await this.fetchUserInfo();
        await this.fetchFriends();
        await this.fetchFriendRequests();
        this.initSocket();
    },
    beforeDestroy() {
        // 清理 WebSocket 连接
        if (this.socket) {
            this.socket.close();
        }
    }
};