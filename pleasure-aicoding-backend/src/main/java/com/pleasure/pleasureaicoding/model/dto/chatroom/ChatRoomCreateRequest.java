package com.pleasure.pleasureaicoding.model.dto.chatroom;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.Serializable;

/**
 * 创建房间请求
 */
@Data
public class ChatRoomCreateRequest implements Serializable {

    /**
     * 房间名称
     */
    @NotBlank(message = "房间名称不能为空")
    private String roomName;

    /**
     * 房间描述
     */
    private String roomDescription;

    /**
     * 房间类型
     */
    @NotBlank(message = "房间类型不能为空")
    private String roomType;

    /**
     * 最大成员数
     */
    @NotNull(message = "最大成员数不能为空")
    @Min(value = 2, message = "最大成员数不能少于2人")
    @Max(value = 500, message = "最大成员数不能超过500人")
    private Integer maxMembers;

    /**
     * 是否公开：0-私有，1-公开
     */
    @NotNull(message = "房间类型不能为空")
    private Integer isPublic;

    /**
     * 房间密码（私有房间）
     */
    private String password;
}