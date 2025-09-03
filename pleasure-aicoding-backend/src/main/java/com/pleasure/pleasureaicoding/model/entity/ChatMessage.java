package com.pleasure.pleasureaicoding.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_message")
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 消息类型：room-群聊，private-私聊
     */
    @Column("messageType")
    private String messageType;

    /**
     * 房间ID（群聊消息）
     */
    @Column("roomId")
    private Long roomId;

    /**
     * 发送者ID
     */
    @Column("senderId")
    private Long senderId;

    /**
     * 接收者ID（私聊消息）
     */
    @Column("receiverId")
    private Long receiverId;

    /**
     * 内容类型：text-文本，image-图片，system-系统消息
     */
    @Column("contentType")
    private String contentType;

    /**
     * 消息内容
     */
    @Column("content")
    private String content;

    /**
     * 文件URL（图片消息）
     */
    @Column("fileUrl")
    private String fileUrl;

    /**
     * 文件名
     */
    @Column("fileName")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @Column("fileSize")
    private Long fileSize;

    /**
     * 回复的消息ID
     */
    @Column("replyToId")
    private Long replyToId;

    /**
     * 是否已撤回：0-否，1-是
     */
    @Column("isRecalled")
    private Integer isRecalled;

    /**
     * 发送时间
     */
    @Column("sendTime")
    private LocalDateTime sendTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;
}