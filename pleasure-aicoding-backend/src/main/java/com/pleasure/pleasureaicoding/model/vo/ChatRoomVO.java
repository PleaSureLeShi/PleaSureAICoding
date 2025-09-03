package com.pleasure.pleasureaicoding.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房间视图对象
 */
@Data
public class ChatRoomVO implements Serializable {

    /**
     * 房间ID
     */
    private Long id;

    /**
     * 房间名称
     */
    private String roomName;

    /**
     * 房间描述
     */
    private String roomDescription;

    /**
     * 房间类型
     */
    private String roomType;

    /**
     * 最大成员数
     */
    private Integer maxMembers;

    /**
     * 当前成员数
     */
    private Integer currentMembers;

    /**
     * 房主ID
     */
    private Long ownerId;

    /**
     * 房主名称
     */
    private String ownerName;

    /**
     * 是否公开
     */
    private Integer isPublic;

    /**
     * 房间状态
     */
    private Integer status;

    /**
     * 是否已加入
     */
    private Boolean isJoined;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}