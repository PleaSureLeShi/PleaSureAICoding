package com.pleasure.pleasureaicoding.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息视图对象
 */
@Data
public class ChatMessageVO implements Serializable {

    /**
     * 消息ID
     */
    private Long id;

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
     * 发送者名称
     */
    private String senderName;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 回复的消息ID
     */
    private Long replyToId;

    /**
     * 是否已撤回
     */
    private Integer isRecalled;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
}