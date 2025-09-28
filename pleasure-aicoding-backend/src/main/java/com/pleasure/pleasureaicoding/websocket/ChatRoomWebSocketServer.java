package com.pleasure.pleasureaicoding.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.pleasure.pleasureaicoding.model.dto.chatmessage.ChatMessageSendRequest;
import com.pleasure.pleasureaicoding.model.entity.User;
import com.pleasure.pleasureaicoding.model.vo.ChatMessageVO;
import com.pleasure.pleasureaicoding.service.ChatMessageService;
import com.pleasure.pleasureaicoding.service.RoomMemberService;
import com.pleasure.pleasureaicoding.service.UserService;
import com.pleasure.pleasureaicoding.utils.SpringContextUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 聊天房间 WebSocket 服务器
 */
@ServerEndpoint("/ws/room/{roomId}")
@Component
@Slf4j
public class ChatRoomWebSocketServer {

    /**
     * 存放每个房间的WebSocket连接集合
     * key: roomId, value: 该房间的所有连接
     */
    private static final ConcurrentHashMap<Long, CopyOnWriteArraySet<ChatRoomWebSocketServer>> ROOM_WEBSOCKETS = new ConcurrentHashMap<>();

    /**
     * 存放用户ID与WebSocket的映射
     * key: userId, value: WebSocket连接
     */
    private static final ConcurrentHashMap<Long, ChatRoomWebSocketServer> USER_WEBSOCKETS = new ConcurrentHashMap<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户信息
     */
    private User user;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("roomId") Long roomId) {
        try {
            this.session = session;
            this.roomId = roomId;
            
            // 从查询参数中获取用户ID
            String userIdStr = session.getRequestParameterMap().get("userId").get(0);
            if (StrUtil.isBlank(userIdStr)) {
                log.error("WebSocket连接失败：缺少用户ID参数");
                session.close();
                return;
            }
            
            this.userId = Long.parseLong(userIdStr);
            
            // 获取用户信息
            UserService userService = SpringContextUtils.getBean(UserService.class);
            this.user = userService.getById(userId);
            if (user == null) {
                log.error("WebSocket连接失败：用户不存在，userId: {}", userId);
                session.close();
                return;
            }
            
            // 检查用户是否在房间中
            RoomMemberService roomMemberService = SpringContextUtils.getBean(RoomMemberService.class);
            boolean isInRoom = roomMemberService.isUserInRoom(roomId, userId);
            if (!isInRoom) {
                log.error("WebSocket连接失败：用户不在房间中，userId: {}, roomId: {}", userId, roomId);
                sendError("您不在该房间中");
                session.close();
                return;
            }
            
            // 添加到房间连接集合
            ROOM_WEBSOCKETS.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(this);
            
            // 添加到用户连接映射
            USER_WEBSOCKETS.put(userId, this);
            
            // 广播用户上线消息
            WebSocketMessage userOnlineMessage = new WebSocketMessage();
            userOnlineMessage.setType(WebSocketMessage.Type.USER_ONLINE);
            userOnlineMessage.setSenderId(userId);
            userOnlineMessage.setRoomId(roomId);
            userOnlineMessage.setTimestamp(System.currentTimeMillis());
            userOnlineMessage.setData(user.getUserName() + " 加入了房间");

            broadcastToRoom(userOnlineMessage, userId);
                    
        } catch (Exception e) {
            log.error("WebSocket连接异常", e);
            try {
                session.close();
            } catch (IOException ioException) {
                log.error("关闭WebSocket连接异常", ioException);
            }
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        try {
            // 从用户连接映射中移除
            USER_WEBSOCKETS.remove(userId);
            
            // 从房间连接集合中移除
            CopyOnWriteArraySet<ChatRoomWebSocketServer> roomConnections = ROOM_WEBSOCKETS.get(roomId);
            if (roomConnections != null) {
                roomConnections.remove(this);
                if (roomConnections.isEmpty()) {
                    ROOM_WEBSOCKETS.remove(roomId);
                }
            }
            
            // 通知房间其他用户有用户下线
            if (user != null) {
                broadcastToRoom(WebSocketMessage.builder()
                        .type(WebSocketMessage.Type.USER_OFFLINE)
                        .senderId(userId)
                        .roomId(roomId) // 使用JsonConfig全局配置处理Long转String
                        .data(user.getUserName() + " 离开了房间")
                        .timestamp(System.currentTimeMillis())
                        .build(), userId);
            }
            
        } catch (Exception e) {
            log.error("WebSocket关闭异常", e);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            WebSocketMessage wsMessage = JSONUtil.toBean(message, WebSocketMessage.class);
            String type = wsMessage.getType();
            
            if (WebSocketMessage.Type.SEND_MESSAGE.equals(type)) {
                handleSendMessage(wsMessage);
            } else if (WebSocketMessage.Type.RECALL_MESSAGE.equals(type)) {
                handleRecallMessage(wsMessage);
            } else if (WebSocketMessage.Type.HEARTBEAT.equals(type)) {
                // 心跳响应
                WebSocketMessage heartbeatResponse = new WebSocketMessage();
                heartbeatResponse.setType(WebSocketMessage.Type.HEARTBEAT);
                heartbeatResponse.setTimestamp(System.currentTimeMillis());
                sendToUser(heartbeatResponse);
            } else {
                log.warn("未知的WebSocket消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息异常", e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误，userId: {}, roomId: {}", userId, roomId, error);
    }

    /**
     * 处理发送消息
     */
    private void handleSendMessage(WebSocketMessage wsMessage) {
        try {
            // 构建发送请求
            ChatMessageSendRequest sendRequest = new ChatMessageSendRequest();
            sendRequest.setMessageType("room");
            sendRequest.setRoomId(roomId);
            sendRequest.setContent(wsMessage.getData().toString());
            sendRequest.setContentType("text");
            
            // 调用服务发送消息
            ChatMessageService chatMessageService = SpringContextUtils.getBean(ChatMessageService.class);
            Long messageId = chatMessageService.sendMessage(sendRequest, user);
            
            // 获取完整的消息信息
            ChatMessageVO messageVO = chatMessageService.getChatMessageVO(messageId, user);
            
            // 广播给房间所有用户
            broadcastToRoom(WebSocketMessage.builder()
                    .type(WebSocketMessage.Type.NEW_MESSAGE)
                    .senderId(userId)
                    .roomId(roomId) // 使用JsonConfig全局配置处理Long转String
                    .data(messageVO)
                    .timestamp(System.currentTimeMillis())
                    .build(), null);
                    
        } catch (Exception e) {
            log.error("处理发送消息异常", e);
            sendError("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 处理撤回消息
     */
    private void handleRecallMessage(WebSocketMessage wsMessage) {
        try {
            Long messageId = Long.parseLong(wsMessage.getData().toString());
            
            // 调用服务撤回消息
            ChatMessageService chatMessageService = SpringContextUtils.getBean(ChatMessageService.class);
            boolean success = chatMessageService.recallMessage(messageId, user);
            
            if (success) {
                // 广播撤回消息给房间所有用户
                broadcastToRoom(WebSocketMessage.builder()
                        .type(WebSocketMessage.Type.MESSAGE_RECALLED)
                        .senderId(userId)
                        .roomId(roomId) // 使用JsonConfig全局配置处理Long转String
                        .data(messageId)
                        .timestamp(System.currentTimeMillis())
                        .build(), null);
            } else {
                sendError("撤回消息失败");
            }
            
        } catch (Exception e) {
            log.error("处理撤回消息异常", e);
            sendError("撤回消息失败: " + e.getMessage());
        }
    }

    /**
     * 向当前用户发送消息
     */
    private void sendToUser(WebSocketMessage message) {
        try {
            String messageJson = convertWebSocketMessageToJson(message);
            session.getBasicRemote().sendText(messageJson);
        } catch (Exception e) {
            log.error("发送WebSocket消息失败，userId: {}", userId, e);
        }
    }
    
    /**
     * 将WebSocket消息转换为JSON字符串，处理Long类型精度丢失问题
     */
    private String convertWebSocketMessageToJson(WebSocketMessage message) {
        // 创建一个Map来手动构建JSON，确保Long类型转为String
        java.util.Map<String, Object> jsonMap = new java.util.HashMap<>();
        jsonMap.put("type", message.getType());
        
        // 处理data字段，如果是ChatMessageVO需要特殊处理其中的Long类型字段
        Object data = message.getData();
        if (data instanceof com.pleasure.pleasureaicoding.model.vo.ChatMessageVO) {
            com.pleasure.pleasureaicoding.model.vo.ChatMessageVO messageVO = (com.pleasure.pleasureaicoding.model.vo.ChatMessageVO) data;
            java.util.Map<String, Object> dataMap = new java.util.HashMap<>();
            dataMap.put("id", messageVO.getId() != null ? messageVO.getId().toString() : null);
            dataMap.put("messageType", messageVO.getMessageType());
            dataMap.put("roomId", messageVO.getRoomId() != null ? messageVO.getRoomId().toString() : null);
            dataMap.put("senderId", messageVO.getSenderId() != null ? messageVO.getSenderId().toString() : null);
            dataMap.put("receiverId", messageVO.getReceiverId() != null ? messageVO.getReceiverId().toString() : null);
            dataMap.put("senderName", messageVO.getSenderName());
            dataMap.put("senderAvatar", messageVO.getSenderAvatar());
            dataMap.put("contentType", messageVO.getContentType());
            dataMap.put("content", messageVO.getContent());
            dataMap.put("fileUrl", messageVO.getFileUrl());
            dataMap.put("fileName", messageVO.getFileName());
            dataMap.put("fileSize", messageVO.getFileSize() != null ? messageVO.getFileSize().toString() : null);
            dataMap.put("replyToId", messageVO.getReplyToId() != null ? messageVO.getReplyToId().toString() : null);
            dataMap.put("sendTime", messageVO.getSendTime());
            dataMap.put("isRecalled", messageVO.getIsRecalled());
            jsonMap.put("data", dataMap);
        } else {
            jsonMap.put("data", data);
        }
        
        // 将Long类型字段转换为String
        if (message.getSenderId() != null) {
            jsonMap.put("senderId", message.getSenderId().toString());
        }
        if (message.getRoomId() != null) {
            jsonMap.put("roomId", message.getRoomId().toString());
        }
        if (message.getReceiverId() != null) {
            jsonMap.put("receiverId", message.getReceiverId().toString());
        }
        if (message.getTimestamp() != null) {
            jsonMap.put("timestamp", message.getTimestamp());
        }
        
        return JSONUtil.toJsonStr(jsonMap);
    }

    /**
     * 向房间所有用户广播消息
     */
    private void broadcastToRoom(WebSocketMessage message, Long excludeUserId) {
        CopyOnWriteArraySet<ChatRoomWebSocketServer> roomConnections = ROOM_WEBSOCKETS.get(roomId);
        if (roomConnections != null) {
            for (ChatRoomWebSocketServer connection : roomConnections) {
                // 排除指定用户
                if (excludeUserId != null && excludeUserId.equals(connection.userId)) {
                    continue;
                }
                connection.sendToUser(message);
            }
        }
    }

    /**
     * 发送错误消息
     */
    private void sendError(String errorMessage) {
        sendToUser(WebSocketMessage.builder()
                .type(WebSocketMessage.Type.ERROR)
                .data(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build());
    }

    /**
     * 静态方法：向房间广播新消息（供HTTP API调用）
     */
    public static void broadcastNewMessage(Long roomId, ChatMessageVO messageVO, Long senderId) {
        CopyOnWriteArraySet<ChatRoomWebSocketServer> roomConnections = ROOM_WEBSOCKETS.get(roomId);
        if (roomConnections != null && !roomConnections.isEmpty()) {
            WebSocketMessage message = new WebSocketMessage();
            message.setType(WebSocketMessage.Type.NEW_MESSAGE);
            message.setSenderId(senderId);
            message.setRoomId(roomId);
            message.setTimestamp(System.currentTimeMillis());
            message.setData(messageVO);

            for (ChatRoomWebSocketServer connection : roomConnections) {
                if (!connection.userId.equals(senderId)) {
                    connection.sendToUser(message);
                }
            }
        }
    }

    /**
     * 静态方法：向房间广播消息撤回（供HTTP API调用）
     */
    public static void broadcastMessageRecalled(Long roomId, Long messageId, Long senderId) {
        CopyOnWriteArraySet<ChatRoomWebSocketServer> roomConnections = ROOM_WEBSOCKETS.get(roomId);
        if (roomConnections != null) {
            WebSocketMessage message = WebSocketMessage.builder()
                    .type(WebSocketMessage.Type.MESSAGE_RECALLED)
                    .senderId(senderId)
                    .roomId(roomId) // 使用JsonConfig全局配置处理Long转String
                    .data(messageId)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            for (ChatRoomWebSocketServer connection : roomConnections) {
                connection.sendToUser(message);
            }
        }
    }

    /**
     * 获取房间在线用户数
     */
    public static int getRoomOnlineCount(Long roomId) {
        CopyOnWriteArraySet<ChatRoomWebSocketServer> roomConnections = ROOM_WEBSOCKETS.get(roomId);
        return roomConnections != null ? roomConnections.size() : 0;
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isUserOnline(Long userId) {
        return USER_WEBSOCKETS.containsKey(userId);
    }
}