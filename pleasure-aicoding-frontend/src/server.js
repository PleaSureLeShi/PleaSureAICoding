const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const path = require('path');
const Memcached = require('memcached');

// 创建Express应用
const app = express();
const server = http.createServer(app);

// 创建WebSocket服务器
const wss = new WebSocket.Server({ server });

// 连接到Memcached服务器
const memcached = new Memcached('127.0.0.1:11211');
console.log('已连接到Memcached服务器');

// 存储在线用户
const onlineUsers = new Map();

// 提供静态文件
app.use(express.static(path.join(__dirname, 'dist')));

// WebSocket连接处理
wss.on('connection', (ws) => {
  let userId = null;
  
  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      
      // 处理用户登录
      if (data.type === 'login') {
        userId = data.userId;
        const userInfo = {
          id: data.userId,
          nickname: data.nickname,
          ws: ws
        };
        
        // 将用户信息存储在Memcached中
        memcached.set(`user_${userId}`, JSON.stringify({
          id: data.userId,
          nickname: data.nickname,
          lastSeen: new Date().getTime()
        }), 86400, (err) => {
          if (err) console.error('Memcached存储错误:', err);
          else console.log(`用户 ${userId} 信息已存储到Memcached`);
        });
        
        // 存储在本地Map中
        onlineUsers.set(userId, userInfo);
        
        // 广播用户列表更新
        broadcastUserList();
        
        // 广播用户加入消息
        broadcast(JSON.stringify({
          type: 'userJoined',
          userId: data.userId,
          nickname: data.nickname,
          timestamp: new Date().toISOString()
        }));
      }
      
      // 处理公共消息
      else if (data.type === 'publicMessage') {
        // 将消息存储到Memcached
        const messageKey = `msg_${new Date().getTime()}`;
        memcached.set(messageKey, JSON.stringify(data), 86400, (err) => {
          if (err) console.error('Memcached存储消息错误:', err);
        });
        
        // 广播消息
        broadcast(JSON.stringify(data));
      }
      
      // 处理私人消息
      else if (data.type === 'privateMessage') {
        // 将消息存储到Memcached
        const messageKey = `private_msg_${data.senderId}_${data.receiverId}_${new Date().getTime()}`;
        memcached.set(messageKey, JSON.stringify(data), 86400, (err) => {
          if (err) console.error('Memcached存储私信错误:', err);
        });
        
        // 发送给接收者
        const receiver = onlineUsers.get(data.receiverId);
        if (receiver && receiver.ws) {
          receiver.ws.send(JSON.stringify(data));
        }
        
        // 发送给发送者（回显）
        const sender = onlineUsers.get(data.senderId);
        if (sender && sender.ws) {
          sender.ws.send(JSON.stringify(data));
        }
      }
    } catch (error) {
      console.error('处理消息错误:', error);
    }
  });
  
  // 处理连接关闭
  ws.on('close', () => {
    if (userId) {
      // 从在线用户列表中移除
      onlineUsers.delete(userId);
      
      // 广播用户列表更新
      broadcastUserList();
      
      // 从Memcached中获取用户信息
      memcached.get(`user_${userId}`, (err, data) => {
        if (err || !data) return;
        
        const userInfo = JSON.parse(data);
        
        // 广播用户离开消息
        broadcast(JSON.stringify({
          type: 'userLeft',
          userId: userId,
          nickname: userInfo.nickname,
          timestamp: new Date().toISOString()
        }));
      });
    }
  });
  
  // 发送欢迎消息
  ws.send(JSON.stringify({
    type: 'system',
    content: '已连接到聊天服务器',
    timestamp: new Date().toISOString()
  }));
});

// 广播消息给所有连接的客户端
function broadcast(message) {
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });
}

// 广播用户列表
function broadcastUserList() {
  const users = [];
  onlineUsers.forEach((user) => {
    users.push({
      id: user.id,
      nickname: user.nickname
    });
  });
  
  const message = JSON.stringify({
    type: 'userList',
    users: users
  });
  
  broadcast(message);
}

// 获取历史消息（示例）
app.get('/api/history', (req, res) => {
  // 从Memcached获取历史消息
  res.json({ messages: [] });
});

// 启动服务器
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`服务器运行在 http://localhost:${PORT}`);
});