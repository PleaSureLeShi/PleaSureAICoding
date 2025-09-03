package com.pleasure.pleasureaicoding.model.dto.chatroom;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 加入房间请求
 */
@Data
public class ChatRoomJoinRequest implements Serializable {

    /**
     * 房间ID
     */
    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    /**
     * 房间密码（私有房间需要）
     */
    private String password;
}