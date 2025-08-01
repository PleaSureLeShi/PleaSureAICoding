<template>
  <div 
    class="message-item" 
    :class="{
      'system-message': message.type === 'system',
      'my-message': isMyMessage,
      'other-message': !isMyMessage && message.type !== 'system',
      'private-message': message.type === 'private'
    }"
  >
    <div v-if="message.type === 'system'" class="system-content">
      <el-tag size="small" type="info">系统消息</el-tag>
      <span>{{ message.content }}</span>
      <span class="message-time">{{ formatTime(message.timestamp) }}</span>
    </div>
    
    <template v-else>
      <div class="message-header">
        <span class="sender-name">
          {{ isMyMessage ? '我' : message.senderName }}
          <el-tag 
            v-if="message.type === 'private'" 
            size="mini" 
            type="warning"
            effect="dark"
          >
            私信
          </el-tag>
        </span>
        <span class="message-time">{{ formatTime(message.timestamp) }}</span>
      </div>
      
      <div class="message-content">
        <div v-if="message.type === 'private' && !isMyMessage" class="private-info">
          私信给您
        </div>
        <div v-else-if="message.type === 'private' && isMyMessage" class="private-info">
          私信给 {{ message.receiverId }}
        </div>
        <div class="content-text">{{ message.content }}</div>
      </div>
    </template>
  </div>
</template>

<script>
export default {
  name: 'MessageItem',
  props: {
    message: {
      type: Object,
      required: true
    },
    currentUser: {
      type: Object,
      required: true
    }
  },
  computed: {
    isMyMessage() {
      return this.message.senderId === this.currentUser.id
    }
  },
  methods: {
    formatTime(timestamp) {
      if (!timestamp) return ''
      
      const date = new Date(timestamp)
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      
      return `${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.message-item {
  margin-bottom: 15px;
  max-width: 80%;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.system-message {
  max-width: 100%;
  text-align: center;
  margin: 10px 0;
}

.system-content {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px;
  background-color: #f5f7fa;
  border-radius: 15px;
  font-size: 0.9em;
  color: #606266;
}

.my-message {
  margin-left: auto;
}

.other-message {
  margin-right: auto;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
  font-size: 0.85em;
}

.sender-name {
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 5px;
}

.message-time {
  color: #909399;
  font-size: 0.85em;
}

.message-content {
  padding: 10px 15px;
  border-radius: 10px;
  position: relative;
}

.my-message .message-content {
  background-color: #ecf5ff;
  color: #303133;
}

.other-message .message-content {
  background-color: #f4f4f5;
  color: #303133;
}

.private-message .message-content {
  background-color: #fdf6ec;
}

.private-info {
  font-size: 0.8em;
  color: #e6a23c;
  margin-bottom: 5px;
}

.content-text {
  word-break: break-word;
}
</style>