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
 * 聊天房间 实体类
 *
 * @author PleaSure乐事
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_room")
public class ChatRoom implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 房间ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 房间名称
     */
    @Column("roomName")
    private String roomName;

    /**
     * 房间描述
     */
    @Column("roomDescription")
    private String roomDescription;

    /**
     * 房间类型
     */
    @Column("roomType")
    private String roomType;

    /**
     * 最大成员数
     */
    @Column("maxMembers")
    private Integer maxMembers;

    /**
     * 当前成员数
     */
    @Column("currentMembers")
    private Integer currentMembers;

    /**
     * 房主ID
     */
    @Column("ownerId")
    private Long ownerId;

    /**
     * 是否公开：0-私有，1-公开
     */
    @Column("isPublic")
    private Integer isPublic;

    /**
     * 房间密码（私有房间）
     */
    @Column("password")
    private String password;

    /**
     * 房间状态：0-封禁，1-正常
     */
    @Column("status")
    private Integer status;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;
}