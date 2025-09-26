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
                    @row-click="selectRoom"
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
                    @row-click="selectRoom"
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
                    <a-tag :color="record.role === 'OWNER' ? 'gold' : 'blue'">
                      {{ record.role === 'OWNER' ? '房主' : '成员' }}
                    </a-tag>
                  </template>
                </template>
              </a-table>

              <a-divider orientation="left">聊天消息</a-divider>
              <div class="chat-container">
                <div class="message-list" ref="messageListRef">
                  <div 
                    v-for="message in roomMessages" 
                    :key="message.id" 
                    class="message-item"
                    :class="{ 'message-mine': message.sender_id === currentUser.id }"
                  >
                    <div v-if="message.is_recalled" class="message-recalled">
                      {{ message.sender_nickname }} 撤回了一条消息
                    </div>
                    <div v-else class="message-content">
                      <a-avatar :src="message.sender_avatar || '/default-avatar.png'" class="message-avatar" />
                      <div class="message-body">
                        <div class="message-header">
                          <span class="sender-name">{{ message.sender_nickname }}</span>
                          <span class="message-time">{{ formatTime(message.create_time) }}</span>
                        </div>
                        <div v-if="message.message_type === 'TEXT'" class="message-text">
                          {{ message.content }}
                        </div>
                        <div v-else-if="message.message_type === 'FILE'" class="message-file">
                          <file-outlined /> 
                          <a :href="message.file_url" target="_blank">{{ message.file_name }}</a>
                        </div>
                      </div>
                      <div v-if="message.sender_id === currentUser.id && !message.is_recalled" class="message-actions">
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
  nickname: string;
  avatar?: string;
  role: string;
}

interface MessageInfo {
  id: number;
  room_id: number;
  sender_id: number;
  sender_nickname: string;
  sender_avatar?: string;
  message_type: 'TEXT' | 'FILE';
  content: string;
  file_url?: string;
  file_name?: string;
  is_recalled: boolean;
  create_time: string;
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
  { title: '昵称', dataIndex: 'nickname', key: 'nickname' },
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
          Object.assign(currentUser, response.data.data);
            } catch (error) {
                console.error('获取用户信息失败:', error);
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
  currentRoom.value = record;
  await fetchRoomMembers(record.id);
  await fetchRoomMessages(record.id);
  scrollToBottom();
  // connectToRoom(record.id);
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
    const response = await api.post('/chatMessage/room/list/page', { roomId, current: 1, pageSize: 50 });
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
      room_id: currentRoom.value.id,
      message_type: 'TEXT',
      content: messageContent.value,
    };
    await api.post('/chatMessage/send', messageData);
    messageContent.value = '';
    // 消息会通过WebSocket推送，不需要手动添加
  } catch (error) {
    message.error('发送消息失败');
    console.error(error);
  }
};

const recallMessage = async (msg: MessageInfo) => {
  try {
    await api.post(`/chatMessage/${msg.id}/recall`);
    message.success('消息已撤回');
    // 消息撤回状态会通过WebSocket更新
  } catch (error) {
    message.error('撤回消息失败');
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
    formData.append('room_id', currentRoom.value.id.toString());
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

const handleWebSocketMessage = (data: any) => {
  switch (data.type) {
    case 'MESSAGE':
      handleNewMessage(data.data);
      break;
    case 'RECALL':
      handleMessageRecall(data.data);
      break;
    case 'ROOM_UPDATE':
      handleRoomUpdate();
      break;
    case 'PONG':
      // 心跳响应，不需要处理
      break;
    default:
      console.log('未知的WebSocket消息类型', data);
  }
};

const handleNewMessage = (messageData: MessageInfo) => {
  if (currentRoom.value && messageData.room_id === currentRoom.value.id) {
    roomMessages.value.push(messageData);
    scrollToBottom();
  }
};

const handleMessageRecall = (recallData: { message_id: number }) => {
  const index = roomMessages.value.findIndex(msg => msg.id === recallData.message_id);
  if (index !== -1) {
    roomMessages.value[index].is_recalled = true;
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

/* 响应式调整 */
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