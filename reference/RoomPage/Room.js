import request from '@/utils/request'
import auth from "@/utils/auth.js";
import logo from "@/assets/images/SystemLogo.png";

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
                // WebSocket 实例
                socket: null,
            },
            typeMap: {
                game: '生活',
                study: '学习',
                social: '社交'
            },
            isJoined: false, // 当前是否已加入房间
            // isRoomOwner: false, 
            // 房间数据
            hotRooms: [],
            joinedRooms: [],
            selectedRoom: null, // 当前选中的房间
            roomMembers: [], // 房间成员
            roomMessages: [], // 房间消息
            newMessage: '', // 新消息
            createRoomDialogVisible: false, // 新建房间弹窗
            newRoomForm: {
                room_name: '',
                room_desc: '',
                room_type: 'game',
                room_status: 'public',
                max_members: 10,
            },
            // AI 对话框相关
            aiDialogVisible: false, // 控制 AI 对话框显示
            aiInput: '', // 用户输入的问题
            aiMessages: [], // AI 对话记录
            errorStates: {
                memberLoading: false
            },
            //websocket关闭处理，防止内存泄漏
            heartbeat: null,
            autoReconnect: null,
        };
    },
    methods: {
        // 跳转页面
        goToHomePage() {
            this.$router.push('/');
        },
        goToFriendPage() {
            this.$router.push('/friend');
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
        // 状态处理方法
        getStatusTagType(status) {
            if (!status) return 'info';
            const statusMap = {
                PUBLIC: 'success',
                PRIVATE: 'warning',
                ENCRYPTED: 'danger',
                BANNED: 'info'
            };
            return statusMap[status.toUpperCase()] || 'info';
        },


        formatRoomStatus(status) {
            if (!status) return '未知状态';
            const statusTextMap = {
                PUBLIC: '公开',
                PRIVATE: '私密',
                ENCRYPTED: '加密',
                SUSPENDED: '已封禁'
            };
            return statusTextMap[status.toUpperCase()] || status;
        },
        addFriend(member) {
            if (member.id === this.user.id) {
                this.$message.warning('不能添加自己为好友');
                return;
            }
            this.sendFriendRequest(member.id);
        },
        //发送好友请求
        async sendFriendRequest(targetId) {
            try {
                const res = await request.post(`/api/${this.user.id}/friends/requests`, {
                    targetUserId: targetId
                });

                if (res.data.success) {
                    this.$message.success(res.data.message || '好友请求已发送');
                } else {
                    this.$message.warning(res.data.message || '操作失败');
                }
            } catch (error) {
                const msg = error.response?.data?.message;
                if (msg) {
                    this.$message.error({
                        '不能添加自己为好友': '不能添加自己',
                        '目标用户不存在': '用户不存在',
                        '用户不存在': '用户不存在'
                    }[msg] || msg);
                } else {
                    this.$message.error('请求发送失败');
                }
            }
        },
        getRowClassName({row}) {
            return row.room_status === 'SUSPENDED' ? 'disabled-row' : '';
        },
        // 选择房间
        selectRoom(room) {
            this.selectedRoom = room;
            if (room.room_status === 'SUSPENDED') {
                this.$message.error('该房间已被封禁，无法查看');
                this.selectedRoom = null;
                return;
            } else if (room.room_status === 'PRIVATE') {
                this.$message.error('该房间是私密房间，无法查看');
                this.selectedRoom = null;
                return;
            } else {
                this.$message.success('正在进入...');
            }
            // 判断是否已加入
            this.isJoined = this.joinedRooms.some(r => r.id === room.id);
            // 如果是已加入房间才加载详情
            if (this.isJoined) {
                this.fetchRoomMembers(room.id);
                this.fetchRoomMessages(room.id);  // 添加这一行来获取历史消息
                this.initWebSocket();
            } else {
                // 清除非成员数据
                this.roomMembers = [];
                this.roomMessages = [];
            }
        },
        // 获取房间成员
        async fetchRoomMembers(roomId) {
            try {
                const res = await request.get(`/api/rooms/${roomId}/members`);
                this.roomMembers = res.data.map(user => ({
                    id: user.id,
                    nickname: user.nickname || '匿名用户',
                    isOwner: user.id === this.hotRooms.find(r => r.id === roomId)?.owner?.id
                }));
            } catch (error) {
                this.$message.error('成员加载失败');
            }
        },
        // 获取房间消息
        async fetchRoomMessages(roomId) {
            try {
                const res = await request.get(`/api/rooms/${roomId}/messages`, {
                    params: { page: 0, size: 20 }
                });

                this.roomMessages = res.data.content.reverse().map(msg => ({
                    id: msg.id,
                    content: msg.content,
                    senderId: msg.user_id,
                    type: msg.type || 'MESSAGE',  // 新增类型字段
                    fileName: msg.fileName,       // 新增文件名字段
                    sender_name: msg.user_id === this.user.id ? '我' : msg.sender?.nickname,
                    timestamp: new Date(msg.timestamp).toLocaleTimeString()
                }));
            } catch {
                this.$message.error('消息加载失败');
            }
        },
        initWebSocket() {
            if (!this.isJoined || !this.selectedRoom) return;

            // 关闭旧连接
            if (this.socket) {
                this.socket.close();
                clearInterval(this.heartbeat);
            }
            const token = localStorage.getItem('access_token');
            const wsURL = `ws://localhost:8080/ws/rooms/${this.selectedRoom.id}/users/${this.user.id}?token=${token}`;
            // 创建新连接
            this.socket = new WebSocket(wsURL);
            // 监听连接成功
            this.socket.onopen = () => {

            };
            // 监听消息
            this.socket.onmessage = async (event) => {
                try {
                    const message = JSON.parse(event.data);
                    if (message.type === 'RECALL') {
                        const index = this.roomMessages.findIndex(m => m.id === message.messageId);
                        if (index !== -1) {
                            this.roomMessages.splice(index, 1, {
                                ...this.roomMessages[index],
                                status: 'deleted',
                                content: '该消息已撤回'
                            });
                        }
                        return;
                    }
                    let senderName = '系统消息';

                    if (message.senderId === this.user.id) {
                        senderName = '我';
                    } else {
                        const userRes = await request.get(`/api/user/${message.senderId}/info`);
                        senderName = userRes.data.nickname || '匿名用户';
                    }

                    const newMessage = {
                        id: message.messageId || message.id,
                        senderId: message.senderId,
                        type: message.type || 'MESSAGE',
                        messageType: message.message_type,
                        content: message.content,
                        fileName: message.fileName,  // 新增文件名字段
                        sender_name: senderName,
                        timestamp: new Date(message.timestamp).toLocaleTimeString(),
                        status: message.status || 'active'
                    };


                    this.roomMessages.push(newMessage);

                    // 滚动到底部
                    await this.$nextTick(() => {
                        const container = this.$el.querySelector('.chat-container');
                        if (container) container.scrollTop = container.scrollHeight;
                    });
                } catch (e) {
                    // console.error('消息处理失败', e);
                }
            };

            // 错误处理
            this.socket.onerror = (error) => {
                // console.error('WebSocket error:', error);
                this.$message.error('实时通信连接失败');
            };

            // 监听连接关闭
            this.socket.onclose = () => {
                // 尝试重连
                setTimeout(() => {
                    this.initWebSocket();
                }, 3000);
            };

            // 心跳检测
            this.heartbeat = setInterval(() => {
                if (this.socket?.readyState === WebSocket.OPEN) {
                    this.socket.send(JSON.stringify({ type: 'ping' }));
                }
            }, 30000);
        },
        // 发送消息
        async sendMessage() {
            if (!this.newMessage.trim()) return;

            // 构造消息对象
            const message = {
                senderId: this.user.id,
                type: "MESSAGE",
                roomId: this.selectedRoom.id,
                messageType: "TEXT",
                content: this.newMessage.trim(),
                fileSize: 0,
                status: "active",
                sender_name: this.user.nickname,
                timestamp: new Date().toISOString()
            };

            try {
                // 检查 WebSocket 连接状态
                if (this.socket?.readyState === WebSocket.OPEN) {
                    this.socket.send(JSON.stringify(message));
                    this.newMessage = ''; // 仅清空输入框
                } else {
                    this.$message.error('实时通信未连接');
                }
            } catch (error) {
                this.$message.error('发送失败');
            }
        },
        //撤回
        async recallMessage(message) {
            try {
                if (!message?.id) {
                    return this.$message.error('无法撤回：消息ID缺失');
                }
                const index = this.roomMessages.findIndex(m => m.id === message.id);
                if (index !== -1) {
                    this.roomMessages.splice(index, 1, {
                        ...this.roomMessages[index],
                        status: 'deleted',
                        content: '该消息已撤回'
                    });
                }

                if (this.socket?.readyState === WebSocket.OPEN) {
                    const recallCommand = {
                        type: "RECALL",
                        messageId: Number(message.id),
                        roomId: Number(this.selectedRoom.id),
                        userId: Number(this.user.id)
                    };
                    this.socket.send(JSON.stringify(recallCommand));
                }
            } catch (error) {
                this.$message.error('撤回操作失败');
                // console.error('撤回错误详情:', error);
            }
        },
        //上传文件
        async uploadFile(file) {
            try {
                const formData = new FormData();
                formData.append('file', file);
                formData.append('roomId', this.selectedRoom.id);
                formData.append('userId', this.user.id);

                const res = await request.post('/api/files/upload', formData, {
                    headers: { 'Content-Type': 'multipart/form-data' }
                });

                // 从 URL 中提取原始文件名
                const originalFilename = res.data.fileUrl.split('_').slice(1).join('_');

                if (this.socket?.readyState === WebSocket.OPEN) {
                    const message = {
                        type: "FILE",
                        roomId: this.selectedRoom.id,
                        messageId: res.data.messageId,
                        content: `http://localhost:8080${res.data.fileUrl}`,
                        fileName: originalFilename,  // 使用解析后的文件名
                        fileSize: file.size,
                        senderId: this.user.id,
                        timestamp: new Date().toISOString()
                    };
                    this.socket.send(JSON.stringify(message));
                }

                return res.data;
            } catch (error) {
                this.$message.error('文件上传失败: 请检查文件类型及文件体积，体积不能超过2mb');
            }
        },

        // 新增文件名解析方法
        extractFilename(url) {
            try {
                const decodedUrl = decodeURIComponent(url);
                const parts = decodedUrl.split('/').pop().split('_');
                return parts.length > 1 ? parts.slice(1).join('_') : decodedUrl.split('/').pop();
            } catch {
                return '未命名文件';
            }
        },
        handleFileUpload() {
            const input = document.createElement('input');
            input.type = 'file';
            input.onchange = async (e) => {
                const file = e.target.files[0];
                if (file) {
                    try {
                        await this.uploadFile(file);
                    } catch (error) {
                        this.$message.error('文件上传失败: 请检查文件类型及文件体积，体积不能超过2mb');
                    }
                }
            };
            input.click();
        },
        openFileInNewWindow(fileUrl) {
            // 创建预览窗口
            const previewWindow = window.open('', '_blank');

            // 获取文件扩展名
            const extension = fileUrl.split('.').pop().toLowerCase();
            const previewUrl = this.generatePreviewUrl(fileUrl, extension);

            // 通用预览模板
            previewWindow.document.write(`
      <html>
        <head>
          <title>文件预览</title>
          <style>
            body { margin: 0; height: 100vh; display: flex; }
            iframe, #pdf-viewer { 
              width: 100%; 
              height: 100%; 
              border: none;
            }
          </style>
          ${
                extension === 'pdf'
                    ? '<script src="https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.4.120/pdf.min.js"><\/script>'
                    : ''
            }
        </head>
        <body>
          ${
                this.getPreviewContent(fileUrl, extension)
            }
        </body>
      </html>
    `);
            previewWindow.document.close();
        },
        
        showCreateRoomDialog() {
            this.createRoomDialogVisible = true;
        },
        // 创建房间
        async createRoom() {
            try {
                if (!this.newRoomForm.room_name.trim()) {
                    this.$message.error('房间名称不能为空');
                    return;
                }
                if (!this.newRoomForm.room_desc.trim()) {
                    this.$message.error('房间简介不能为空');
                    return;
                }
                const res = await request.post('/api/rooms', {
                    room_name: this.newRoomForm.room_name,
                    room_desc: this.newRoomForm.room_desc,
                    room_type: this.newRoomForm.room_type,
                    room_status: this.newRoomForm.room_status,
                    max_members: this.newRoomForm.max_members,
                    owner_id: auth.getUserId()
                });
                this.$message.success('房间创建成功');
                this.createRoomDialogVisible = false;
                this.fetchHotRooms();
            } catch (error) {
                // console.error('创建房间失败:', error);
                this.$message.error('创建房间失败，请稍后重试');
            }

        },
        // 获取所有房间
        async fetchHotRooms() {
            try {
                const res = await request.get('/api/rooms/room/hot');
                this.hotRooms = res.data.map(room => ({
                    ...room,
                    id: room.id,
                    room_name: room.roomName || '未命名房间',
                    owner: {
                        id: room.owner?.id,
                        nickname: room.owner?.nickname
                    },
                    room_type: this.typeMap[room.roomType?.toLowerCase()] || room.roomType,
                    room_status: room.roomStatus,
                    max_members: room.maxMembers,
                    owner_nickname: room.owner?.nickname || '未知房主',
                    current_members: room.userCount
                }));
            } catch (error) {
                console.error('获取热门房间失败:', error);
            }
        },
        // 获取已加入房间
        async fetchJoinedRooms() {
            try {
                const userId = auth.getUserId();
                const res = await request.get(`/api/user/${userId}/joined-rooms`);
                this.joinedRooms = res.data.map(joinedRoom => {
                    // 从热门房间中匹配完整信息
                    const hotRoom = this.hotRooms.find(room => room.id === joinedRoom.id);
                    return {
                        id: joinedRoom.id,
                        room_name: joinedRoom.name || '未命名房间',
                        room_type: hotRoom?.room_type || '社交',
                        room_status: hotRoom?.room_status || '公共',
                        current_members: joinedRoom.userCount,
                        max_members: hotRoom?.max_members || joinedRoom.maxMembers || 10,
                        owner_nickname: hotRoom?.owner_nickname || '未知房主',
                    };
                });
                if (this.selectedRoom) {
                    this.isRoomOwner = this.joinedRooms.find(r => r.id === this.selectedRoom.id)?.ownerId === this.user.id;
                }
            } catch (error) {
                console.error('获取已加入房间失败:', error);
            }
        },
        async joinRoom() {
            try {
                // 强制获取最新房间数据
                await this.fetchHotRooms();

                // 获取最新房间信息
                const room = this.hotRooms.find(r => r.id === this.selectedRoom.id);
                if (!room) {
                    this.$message.error('房间信息获取失败');
                    return;
                }

                // 直接判断当前人数
                if (room.current_members >= room.max_members) {
                    this.$message.error('超过最高上限');
                    return; // 直接返回不发送请求
                }

                // 发送加入请求
                await request.post(`/api/rooms/${room.id}/enter`, {
                    userId: this.user.id
                });

                // 更新本地状态
                this.isJoined = true;
                await this.fetchJoinedRooms();
                this.initWebSocket();

                this.$message.success('加入成功');

            } catch (error) {
                // 统一错误处理
                const msg = error.response?.data?.message || '加入失败';
                this.$message.error(msg.includes('满') ? '超过最高上限' : msg);

                // 同步加入状态
                await this.fetchJoinedRooms();
                this.isJoined = this.joinedRooms.some(r => r.id === this.selectedRoom.id);
            }
        },
        async leaveRoom() {
            try {
                await request.post(`/api/rooms/${this.selectedRoom.id}/exit`, {
                    userId: this.user.id
                });
                this.isJoined = false;
                await this.fetchJoinedRooms();
                this.$message.success('已退出房间');
            } catch (error) {
                this.$message.error(error.response?.data?.message || '退出失败');
            }
        },
        // 获取用户信息
        async fetchUserInfo() {
            this.loading = true;
            try {
                // 从 localStorage 或 auth 模块获取用户ID
                const userId = auth.getUserId();
                if (!userId) throw new Error("用户ID不存在");

                // 显式传递用户ID
                const res = await request.get(`/api/user/${userId}/info`);
                // 更新用户信息
                this.user = {
                    ...res.data,
                    avatar: res.data.avatar || logo
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
        async sendToAI() {
            if (!this.aiInput.trim()) return;

            // 将用户输入添加到对话记录
            this.aiMessages.push({role: 'user', content: this.aiInput});

            try {
                const res = await request.post('/api/ai/ask', {
                    question: this.aiInput
                });
                this.aiMessages.push({role: 'ai', content: res.data.answer});
            } catch (error) {
                console.error('获取 AI 回答失败:', error);
            }
            // 清空输入框
            this.aiInput = '';
        },
    },
    async mounted() {
        await this.fetchUserInfo();
        await this.fetchHotRooms();
        await this.fetchJoinedRooms();
        this.autoRefresh = setInterval(() => {
            if (this.selectedRoom && this.isJoined) {
                this.fetchRoomMembers(this.selectedRoom.id);
            }
        }, 30000);
    },
    beforeUnmount() {
        clearInterval(this.autoRefresh);
        if (this.socket) {
            this.socket.close();
        }
        if (this.heartbeat) {
            clearInterval(this.heartbeat);
        }
    },
    computed: {
        isRoomOwner() {
            return this.selectedRoom?.owner?.id === this.user.id;
        }
    },
    watch: {
        roomMessages() {
            this.$nextTick(() => {
                const container = this.$el.querySelector('.chat-container');
                // 添加空值检查
                if (container && container.scrollHeight) {
                    container.scrollTop = container.scrollHeight;
                }
            });
        }
    }
};