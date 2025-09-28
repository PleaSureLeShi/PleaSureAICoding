package com.pleasure.pleasureaicoding.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 房间成员视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomMemberVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 在房间中的角色：owner-房主，admin-管理员，member-普通成员
     */
    private String role;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 最后阅读时间
     */
    private LocalDateTime lastReadTime;

    /**
     * 是否被禁言：0-否，1-是
     */
    private Integer isMuted;

    /**
     * 禁言到期时间
     */
    private LocalDateTime mutedUntil;
}