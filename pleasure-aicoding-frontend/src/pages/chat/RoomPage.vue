<template>
  <div class="room-page">
    <a-layout>
      <!-- 页面内容 -->
      <a-layout-content class="main-content">
        <a-row :gutter="16">
          <!-- 左侧房间列表 -->
          <a-col :span="8">
            <a-card class="room-list-container">
              <div class="action-buttons">
                <a-button type="primary" @click="showCreateRoomModal">创建房间</a-button>
              </div>

              <a-tabs v-model:activeKey="activeTabKey">
                <a-tab-pane key="allRooms" tab="所有房间">
                  <a-table
                    :columns="roomColumns"
                    :data-source="allRooms"
                    :row-key="(record: RoomInfo) => record.id"
                    :pagination="{ pageSize: 5 }"
                    size="small"
                    :custom-row="(record: RoomInfo) => ({
                      onClick: () => selectRoom(record)
                    })"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'status'">
                        <a-tag :color="record.isPublic === 1 ? 'success' : 'warning'">
                          {{ record.isPublic === 1 ? '公开' : '私密' }}
                        </a-tag>
                      </template>
                      <template v-if="column.key === 'members'">
                        {{ record.currentMembers }}/{{ record.maxMembers }}人
                      </template>
                      <template v-if="column.key === 'action'">
                        <a-button type="link" size="small" @click.stop="joinRoom(record)">
                          加入
                        </a-button>
                      </template>
                    </template>
                  </a-table>
                </a-tab-pane>
                <a-tab-pane key="joinedRooms" tab="已加入房间">
                  <a-table
                    :columns="roomColumns"
                    :data-source="joinedRooms"
                    :row-key="(record: RoomInfo) => record.id"
                    :pagination="{ pageSize: 5 }"
                    size="small"
                    :custom-row="(record: RoomInfo) => ({
                      onClick: () => selectRoom(record)
                    })"
                  >
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'status'">
                        <a-tag :color="record.isPublic === 1 ? 'success' : 'warning'">
                          {{ record.isPublic === 1 ? '公开' : '私密' }}
                        </a-tag>
                      </template>
                      <template v-if="column.key === 'members'">
                        {{ record.currentMembers }}/{{ record.maxMembers }}人
                      </template>
                      <template v-if="column.key === 'action'">
                        <a-button type="link" size="small" @click.stop="leaveRoom(record)">
                          退出
                        </a-button>
                      </template>
                    </template>
                  </a-table>
                </a-tab-pane>
              </a-tabs>
            </a-card>
          </a-col>

          <!-- 右侧房间详情 -->
          <a-col :span="16">
            <a-card v-if="currentRoom" class="room-detail">
              <div class="room-header">
                <div class="room-title">
                  <h2>{{ currentRoom.roomName }}</h2>
                  <a-space>
                    <a-tag>{{ getRoomTypeChinese(currentRoom.roomType) }}</a-tag>
                    <a-tag :color="currentRoom.isPublic === 1 ? 'success' : 'warning'">
                      {{ currentRoom.isPublic === 1 ? '公开' : '私密' }}
                    </a-tag>
                  </a-space>
                </div>
                <div class="room-actions">
                  <a-button
                    v-if="!isJoined(currentRoom.id)"
                    type="primary"
                    @click="joinRoom(currentRoom)"
                  >
                    加入房间
                  </a-button>
                  <a-button
                    v-else
                    type="primary"
                    danger
                    @click="leaveRoom(currentRoom)"
                  >
                    退出房间
                  </a-button>
                </div>
              </div>

              <a-divider orientation="left">房间成员</a-divider>
              <a-table
                :columns="memberColumns"
                :data-source="roomMembers"
                :row-key="(record: UserInfo) => record.id"
                :pagination="{ pageSize: 5 }"
                size="small"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'role'">
                    <a-tag :color="record.role === 'owner' ? 'gold' : 'blue'">
                      {{ record.role === 'owner' ? '房主' : '成员' }}
                    </a-tag>
                  </template>
                </template>
              </a-table>

              <a-divider orientation="left">聊天消息</a-divider>
              <div class="chat-container">
                <div class="message-list" ref="messageListRef">
                  <!-- 调试信息 -->
                  <div v-if="roomMessages.length === 0" class="debug-info">
                    <p>消息列表为空</p>
                    <p>当前房间: {{ currentRoom?.roomName || '未选择' }}</p>
                    <p>消息数量: {{ roomMessages.length }}</p>
                  </div>

                  <div
                    v-for="message in sortedRoomMessages"
                    :key="message.id"
                    class="message-item"
                    :class="{ 'message-mine': message.senderId === currentUser.id }"
                  >
                    <div v-if="message.isRecalled" class="message-recalled">
                      {{ message.senderName }} 撤回了一条消息
                    </div>
                    <div v-else class="message-content">
                      <a-avatar :src="message.senderAvatar || '/default-avatar.png'" class="message-avatar" />
                      <div class="message-body">
                        <div class="message-header">
                          <span class="sender-name">{{ message.senderName }}</span>
                          <span class="message-time">{{ formatTime(message.sendTime) }}</span>
                        </div>

                        <div v-if="message.contentType === 'TEXT'" class="message-text">
                          {{ message.content }}
                        </div>
                        <div v-else-if="message.contentType === 'FILE' || message.contentType === 'file'" class="message-file">
                          <FileOutlined />
                          <a :href="message.fileUrl" target="_blank">{{ message.fileName }}</a>
                        </div>
                        <div v-else class="message-text">
                          <!-- 兜底显示 -->
                          {{ message.content }}
                        </div>
                      </div>
                      <div v-if="message.senderId === currentUser.id && !message.isRecalled" class="message-actions">
                        <a-button type="text" size="small" @click="recallMessage(message)">撤回</a-button>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="chat-input">
                  <a-input
                    v-model:value="messageContent"
                    placeholder="输入消息"
                    @pressEnter="sendTextMessage"
                  >
                    <template #suffix>
                      <upload-outlined @click="showFileUpload" />
                    </template>
                  </a-input>
                  <a-button type="primary" @click="sendTextMessage">发送</a-button>
                </div>
              </div>
            </a-card>
            <a-empty v-else description="请选择一个房间" />
          </a-col>
        </a-row>
      </a-layout-content>
    </a-layout>

    <!-- 创建房间对话框 -->
    <a-modal
      v-model:open="createRoomModalVisible"
      title="创建房间"
      @ok="createRoom"
    >
      <a-form :model="roomForm" :rules="roomFormRules" ref="roomFormRef">
        <a-form-item label="房间名称" name="room_name">
          <a-input v-model:value="roomForm.room_name" />
        </a-form-item>
        <a-form-item label="房间描述" name="description">
          <a-textarea v-model:value="roomForm.description" />
        </a-form-item>
        <a-form-item label="房间类型" name="room_type">
          <a-select v-model:value="roomForm.room_type">
            <a-select-option value="LIFE">生活</a-select-option>
            <a-select-option value="STUDY">学习</a-select-option>
            <a-select-option value="SOCIAL">社交</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="房间状态" name="room_status">
          <a-select v-model:value="roomForm.room_status">
            <a-select-option value="PUBLIC">公开</a-select-option>
            <a-select-option value="PRIVATE">私密</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="人数上限" name="max_members">
          <a-input-number v-model:value="roomForm.max_members" :min="1" :max="100" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 文件上传对话框 -->
    <a-modal
      v-model:open="fileUploadModalVisible"
      title="上传文件"
      @ok="uploadFile"
    >
      <a-upload
        v-model:file-list="fileList"
        :before-upload="beforeUpload"
        :multiple="false"
      >
        <a-button>
          <upload-outlined /> 选择文件
        </a-button>
      </a-upload>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue';
import { message } from 'ant-design-vue';
import { UploadOutlined, FileOutlined } from '@ant-design/icons-vue';
import axios from 'axios';
import type { UploadProps } from 'ant-design-vue';
// 导入openapi2ts生成的API
import { sendMessage } from '@/api/chatMessageController';

// 定义类型
interface RoomInfo {
  id: number;
  roomName: string;
  ownerId: number;
  ownerName: string;
  roomType: string;
  isPublic: number;
  roomDescription?: string;
  currentMembers: number;
  maxMembers: number;
  createTime: string;
}

interface UserInfo {
  id: number;
  userName: string;
  userAvatar?: string;
  userProfile?: string;
  role: string;
  joinTime?: string;
  lastReadTime?: string;
  isMuted?: number;
  mutedUntil?: string;
}

interface MessageInfo {
  id: number;
  messageType?: string;
  roomId?: number;
  senderId: number;
  senderName: string;
  senderAvatar?: string;
  receiverId?: number;
  contentType: 'TEXT' | 'FILE';
  content: string;
  fileUrl?: string;
  fileName?: string;
  fileSize?: number;
  replyToId?: number;
  isRecalled: number;
  sendTime: string;
}

// 创建API实例
const api = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 10000,
});

// 状态管理
const currentUser = reactive({
  id: 0,
  nickname: '',
  avatar: '',
});

const activeTabKey = ref('allRooms');
const allRooms = ref<RoomInfo[]>([]);
const joinedRooms = ref<RoomInfo[]>([]);
const currentRoom = ref<RoomInfo | null>(null);
const roomMembers = ref<UserInfo[]>([]);
const roomMessages = ref<MessageInfo[]>([]);
const messageContent = ref('');
const messageListRef = ref<HTMLElement | null>(null);
const createRoomModalVisible = ref(false);
const fileUploadModalVisible = ref(false);
const fileList = ref<any[]>([]);
const websocket = ref<WebSocket | null>(null);
const heartbeatTimer = ref<number | null>(null);

// 计算属性：排序后的消息列表
const sortedRoomMessages = computed(() => {
  return [...roomMessages.value].sort((a, b) => {
    // 按发送时间升序排列，最旧的消息在上面，最新的在下面
    return new Date(a.sendTime).getTime() - new Date(b.sendTime).getTime();
  });
});

// 表单相关
const roomFormRef = ref();
const roomForm = reactive({
  room_name: '',
  description: '',
  room_type: 'STUDY',
  room_status: 'PUBLIC',
  password: '',
  max_members: 10,
});

const roomFormRules = {
  room_name: [{ required: true, message: '请输入房间名称', trigger: 'blur' }],
  room_type: [{ required: true, message: '请选择房间类型', trigger: 'change' }],
  room_status: [{ required: true, message: '请选择房间状态', trigger: 'change' }],
  password: [{
    required: false,
    validator: (rule: any, value: string) => {
      if (roomForm.room_status === 'PRIVATE' && (!value || value.trim() === '')) {
        return Promise.reject('私密房间必须设置密码');
      }
      return Promise.resolve();
    },
    trigger: 'blur'
  }],
  max_members: [{ required: true, message: '请设置人数上限', trigger: 'change' }],
};

// 表格列定义
const roomColumns = [
  { title: '房间名', dataIndex: 'roomName', key: 'roomName' },
  { title: '房主', dataIndex: 'ownerName', key: 'ownerName', width: 100 },
  {
    title: '类型',
    dataIndex: 'roomType',
    key: 'type',
    width: 80,
    customRender: ({ text }: { text: string }) => getRoomTypeChinese(text)
  },
  { title: '状态', key: 'status', width: 80 },
  { title: '人数', key: 'members', width: 80 },
  { title: '操作', key: 'action', width: 80 },
];

const memberColumns = [
  { title: '昵称', dataIndex: 'userName', key: 'userName' },
  { title: '角色', key: 'role', width: 100 },
];

// 初始化
onMounted(async () => {
  await getCurrentUser();
  await fetchRooms();
});

onUnmounted(() => {
  closeWebSocket();
});

// 用户相关方法
const getCurrentUser = async () => {
  try {
    const response = await api.get('/user/get/login');
    if (response.data.code === 0 && response.data.data) {
      Object.assign(currentUser, response.data.data);
    } else {
      message.error('获取用户信息失败，请重新登录');
      // 跳转到登录页面
      window.location.href = '/user/login';
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    message.error('获取用户信息失败，请重新登录');
    window.location.href = '/user/login';
  }
};


// 房间相关方法
const fetchRooms = async () => {
  try {
    const allResponse = await api.post('/chatRoom/list/page', { current: 1, pageSize: 10 });
    allRooms.value = allResponse.data.data.records || allResponse.data.data;
    const joinedResponse = await api.get('/chatRoom/joined');
    joinedRooms.value = joinedResponse.data.data || [];
  } catch (error) {
    console.error('获取房间列表失败:', error);
  }
};

const selectRoom = async (record: RoomInfo) => {
  // 检查用户是否已登录
  if (!currentUser.id) {
    message.error('请先登录');
    return;
  }

  // 检查用户是否已加入该房间
  if (!isJoined(record.id)) {
    message.warning('您还未加入该房间，请先加入');
    return;
  }

  try {
    currentRoom.value = record;

    await fetchRoomMembers(record.id);
    await fetchRoomMessages(record.id);
    scrollToBottom();
    connectToRoom(record.id);

    message.success(`已进入房间: ${record.roomName}`);
  } catch (error) {
    console.error('进入房间失败:', error);
    message.error('进入房间失败，请重试');
  }
};

const fetchRoomMembers = async (roomId: number) => {
  try {
    const response = await api.get(`/chatRoom/${roomId}/members`);
    roomMembers.value = response.data.data || [];
  } catch (error) {
    message.error('获取房间成员失败');
    console.error(error);
  }
};

const fetchRoomMessages = async (roomId: number) => {
  try {
    const response = await api.post('/chatMessage/room/list/page', { roomId, pageNum: 1, pageSize: 50 });
    roomMessages.value = response.data.data.records || [];
  } catch (error) {
    message.error('获取聊天记录失败');
    console.error(error);
  }
};

const isJoined = (roomId: number) => {
  return joinedRooms.value.some(room => room.id === roomId);
};

const joinRoom = async (room: RoomInfo) => {
  try {
    const response = await api.post('/chatRoom/join', { roomId: room.id });
    // 只有当后端返回成功时才显示成功消息
    if (response.data && response.data.code === 0) {
      message.success(`已加入房间: ${room.roomName}`);
      await fetchRooms();
      if (currentRoom.value && currentRoom.value.id === room.id) {
        await fetchRoomMembers(room.id);
      }
    } else {
      // 处理后端返回的非成功状态
      message.error(response.data?.message || '加入房间失败');
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      message.error(error.response.data.message);
    } else {
      message.error('加入房间失败');
    }
    console.error(error);
  }
};

const leaveRoom = async (room: RoomInfo) => {
  try {
    // 检查当前用户是否是房主
    if (room.ownerId === currentUser.id) {
      message.error('房主不能退出房间，请先转让房主或解散房间');
      return;
    }

    const response = await api.post(`/chatRoom/leave/${room.id}`);
    // 只有当后端返回成功时才显示成功消息
    if (response.data && response.data.code === 0) {
      message.success(`已退出房间: ${room.roomName}`);
      await fetchRooms();
      if (currentRoom.value && currentRoom.value.id === room.id) {
        await fetchRoomMembers(room.id);
      }
    } else {
      // 处理后端返回的非成功状态
      message.error(response.data?.message || '退出房间失败');
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      message.error(error.response.data.message);
    } else {
      message.error('退出房间失败');
    }
    console.error(error);
  }
};
// 创建房间相关
const showCreateRoomModal = () => {
  createRoomModalVisible.value = true;
};

const createRoom = async () => {
  try {
    // 表单验证
    await roomFormRef.value.validate();

    // 检查私密房间是否设置了密码
    if (roomForm.room_status === 'PRIVATE' && (!roomForm.password || roomForm.password.trim() === '')) {
      message.error('私密房间必须设置密码');
      return;
    }

    // 构造与后端匹配的payload
    const payload = {
      roomName: roomForm.room_name,
      roomDescription: roomForm.description,
      roomType: roomForm.room_type,
      maxMembers: roomForm.max_members,
      isPublic: roomForm.room_status === 'PUBLIC' ? 1 : 0,
      password: roomForm.room_status === 'PRIVATE' ? roomForm.password : null // 私密房间必须有密码
    };

    const response = await api.post('/chatRoom/create', payload);
    // 只有当后端返回成功时才显示成功消息
    if (response.data && response.data.code === 0) {
      message.success('创建房间成功');
      createRoomModalVisible.value = false;
      // 重置表单
      roomForm.room_name = '';
      roomForm.description = '';
      roomForm.room_type = 'CHAT';
      roomForm.max_members = 10;
      roomForm.room_status = 'PUBLIC';
      roomForm.password = '';
      // 刷新房间列表
      await fetchRooms();
    } else {
      // 处理后端返回的非成功状态
      message.error(response.data?.message || '创建房间失败');
    }
  } catch (error: any) {
    if (error.response?.data?.message) {
      // 显示后端返回的具体错误信息
      message.error(error.response.data.message);
    } else {
      message.error('创建房间失败');
    }
    console.error(error);
  }
};
  
  // 消息相关方法
  const sendTextMessage = async () => {
  if (!currentRoom.value || !messageContent.value.trim()) return;

  try {
    const messageData = {
      roomId: currentRoom.value.id,
      messageType: 'room',
      contentType: 'text',
      content: messageContent.value,
    };
    
    // 使用openapi2ts生成的API方法
    const response = await sendMessage(messageData);
    
    messageContent.value = '';
    // 消息会通过WebSocket推送，不需要手动添加
  } catch (error) {
    message.error('发送消息失败');
    console.error(error);
  }
};

const recallMessage = async (msg: MessageInfo) => {
  try {
    // 检查消息发送时间是否超过2分钟
    const sendTime = new Date(msg.sendTime);
    const now = new Date();
    const diffMinutes = Math.floor((now.getTime() - sendTime.getTime()) / (1000 * 60));

    if (diffMinutes > 2) {
      message.warning('消息发送超过2分钟，无法撤回');
      return;
    }

    await api.post(`/chatMessage/recall/${msg.id}`);
    message.success('消息已撤回');
    // 消息撤回状态会通过WebSocket更新
  } catch (error: any) {
    if (error.response?.data?.message) {
      message.error(error.response.data.message);
    } else {
      message.error('撤回消息失败');
    }
    console.error(error);
  }
};

// 文件上传相关
const showFileUpload = () => {
  fileUploadModalVisible.value = true;
  fileList.value = [];
};

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  fileList.value = [file];
  return false;
};

const uploadFile = async () => {
  if (fileList.value.length === 0 || !currentRoom.value) {
    message.warning('请选择文件');
    return;
  }

  try {
    const formData = new FormData();
    formData.append('file', fileList.value[0]);
    formData.append('roomId', currentRoom.value.id.toString());
    await api.post('/chatMessage/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    message.success('文件上传成功');
    fileUploadModalVisible.value = false;
    fileList.value = [];
    // 文件消息会通过WebSocket推送\n
  } catch (error) {
    message.error('文件上传失败');
    console.error(error);
  }
};

// WebSocket相关

const closeWebSocket = () => {
  if (websocket.value) {
    websocket.value.close();
    websocket.value = null;
  }
  stopHeartbeat();
};

const startHeartbeat = () => {
  stopHeartbeat();
  heartbeatTimer.value = window.setInterval(() => {
    if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
      websocket.value.send(JSON.stringify({ type: 'PING' }));
    }
  }, 30000);
};

const stopHeartbeat = () => {
  if (heartbeatTimer.value) {
    clearInterval(heartbeatTimer.value);
    heartbeatTimer.value = null;
  }
};

const connectToRoom = (roomId: number) => {
  // 关闭之前的连接
  closeWebSocket();

  // 构建WebSocket URL
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const host = import.meta.env.VITE_APP_WS_HOST || window.location.host;
  const wsUrl = `${protocol}//${host}/api/ws/room/${roomId}?userId=${currentUser.id}`;

  // 创建WebSocket连接
  websocket.value = new WebSocket(wsUrl);

  // 连接打开时的处理
  websocket.value.onopen = () => {
    // WebSocket连接已建立
    message.success('已连接到聊天室');
    startHeartbeat();
  };

  // 接收消息的处理
  websocket.value.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data);
      handleWebSocketMessage(data);
    } catch (error) {
      console.error('处理WebSocket消息失败:', error);
    }
  };

  // 错误处理
  websocket.value.onerror = (error) => {
    console.error('WebSocket连接错误:', error);
    message.error('聊天室连接出错');
  };

  // 连接关闭的处理
  websocket.value.onclose = () => {
    // WebSocket连接已关闭
    stopHeartbeat();
  };
};

const handleWebSocketMessage = (data: any) => {
  switch (data.type) {
    case 'NEW_MESSAGE':
      handleNewMessage(data.data);
      break;
    case 'MESSAGE_RECALLED':
      handleMessageRecall(data.data);
      break;
    case 'ROOM_UPDATE':
      handleRoomUpdate();
      break;
    case 'HEARTBEAT':
      // 心跳响应，不需要处理
      break;
    default:
      // 未知消息类型
      break;
  }
};

const handleNewMessage = (messageData: any) => {
 
  if (currentRoom.value && String(messageData.roomId) === String(currentRoom.value.id)) {
    roomMessages.value.push(messageData); // 改为push，让计算属性来处理排序
    scrollToBottom();
  }
};

const handleMessageRecall = (recallData: any) => {
  // 处理不同的数据格式
  let messageId: number;
  if (typeof recallData === 'number') {
    messageId = recallData;
  } else if (recallData.message_id) {
    messageId = recallData.message_id;
  } else if (recallData.messageId) {
    messageId = recallData.messageId;
  } else {
    return;
  }

  const index = roomMessages.value.findIndex(msg => msg.id === messageId);
  if (index !== -1) {
    roomMessages.value[index].isRecalled = 1;
  }
};

const handleRoomUpdate = async () => {
  // 房间信息更新，刷新列表
  await fetchRooms();

  // 如果当前选中的房间，也刷新成员列表
  if (currentRoom.value) {
    const roomId = currentRoom.value.id;
    // 更新当前房间信息
    const updatedRoom = [...allRooms.value, ...joinedRooms.value].find(r => r.id === roomId);
    if (updatedRoom) {
      currentRoom.value = updatedRoom;
    }
    await fetchRoomMembers(roomId);
  }
};

// 辅助方法
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
    }
  });
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};


// 房间类型映射
const roomTypeMap = {
  'STUDY': '学习',
  'LIFE': '生活',
  'SOCIAL': '社交',
  'ENTERTAINMENT': '娱乐',
  'GAME': '游戏',
  'OTHER': '其他'
};

// 获取房间类型的中文名称
const getRoomTypeChinese = (type: string) => {
  return roomTypeMap[type as keyof typeof roomTypeMap] || type;
};

</script>

<style scoped>
.room-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  padding: 24px;
  flex: 1;
  background-color: #f0f2f5;
}

.room-list-container {
  margin-bottom: 24px;
}

.action-buttons {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
}

.room-detail {
  height: 100%;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.room-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.room-title h2 {
  margin: 0;
  margin-right: 8px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 400px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin-bottom: 16px;
  height: 300px;
}

.message-item {
  margin-bottom: 16px;
}

.message-mine .message-content {
  flex-direction: row-reverse;
}

.message-mine .message-avatar {
  margin-left: 8px;
  margin-right: 0;
}

.message-mine .message-body {
  background-color: #e6f7ff;
}

.message-recalled {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 4px 0;
}

.message-content {
  display: flex;
  align-items: flex-start;
}

.message-avatar {
  margin-right: 8px;
  flex-shrink: 0;
}

.message-body {
  flex: 1;
  background-color: #fff;
  padding: 8px 12px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  max-width: 80%;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.sender-name {
  font-weight: bold;
  color: #1890ff;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-text {
  word-break: break-word;
}

.message-file {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-actions {
  margin-left: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.message-content:hover .message-actions {
  opacity: 1;
}

.chat-input {
  display: flex;
  gap: 8px;
}

.debug-info {
  padding: 16px;
  background-color: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 4px;
  margin-bottom: 16px;
  color: #856404;
}

.debug-info p {
  margin: 4px 0;
  font-size: 14px;
}
@media (max-width: 768px) {
  .main-content {
    padding: 16px;
  }

  .room-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .room-actions {
    margin-top: 8px;
  }
}
</style>
