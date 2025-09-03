package com.pleasure.pleasureaicoding.model.dto.chatmessage;

import com.pleasure.pleasureaicoding.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 查询消息请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatMessageQueryRequest extends PageRequest implements Serializable {

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 最后消息时间（用于游标分页）
     */
    private LocalDateTime lastMessageTime;
}