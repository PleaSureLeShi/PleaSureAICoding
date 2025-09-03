package com.pleasure.pleasureaicoding.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息数据
     */
    private Object data;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 房间ID（群聊）
     */
    private Long roomId;

    /**
     * 接收者ID（私聊）
     */
    private Long receiverId;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 消息类型常量
     */
    public static class Type {
        public static final String SEND_MESSAGE = "SEND_MESSAGE";
        public static final String NEW_MESSAGE = "NEW_MESSAGE";
        public static final String RECALL_MESSAGE = "RECALL_MESSAGE";
        public static final String MESSAGE_RECALLED = "MESSAGE_RECALLED";
        public static final String JOIN_ROOM = "JOIN_ROOM";
        public static final String LEAVE_ROOM = "LEAVE_ROOM";
        public static final String USER_ONLINE = "USER_ONLINE";
        public static final String USER_OFFLINE = "USER_OFFLINE";
        public static final String ERROR = "ERROR";
        public static final String HEARTBEAT = "HEARTBEAT";
    }
}