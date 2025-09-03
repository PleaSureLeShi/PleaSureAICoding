package com.pleasure.pleasureaicoding.model.dto.chatmessage;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 发送消息请求
 */
@Data
public class ChatMessageSendRequest implements Serializable {

    /**
     * 消息类型：room-群聊，private-私聊
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /**
     * 房间ID（群聊消息）
     */
    private Long roomId;

    /**
     * 接收者ID（私聊消息）
     */
    private Long receiverId;

    /**
     * 内容类型：text-文本，image-图片
     */
    private String contentType = "text";

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 回复的消息ID
     */
    private Long replyToId;
}