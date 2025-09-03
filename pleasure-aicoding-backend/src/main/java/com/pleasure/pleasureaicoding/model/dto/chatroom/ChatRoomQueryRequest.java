package com.pleasure.pleasureaicoding.model.dto.chatroom;

import com.pleasure.pleasureaicoding.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询房间请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatRoomQueryRequest extends PageRequest implements Serializable {

    /**
     * 房间名称
     */
    private String roomName;

    /**
     * 房间类型
     */
    private String roomType;

    /**
     * 房间状态
     */
    private Integer status;

    /**
     * 是否公开
     */
    private Integer isPublic;

    /**
     * 房主ID
     */
    private Long ownerId;
}