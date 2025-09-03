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
 * 房间成员 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("room_member")
public class RoomMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成员关系ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 房间ID
     */
    @Column("roomId")
    private Long roomId;

    /**
     * 用户ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 角色：owner-房主，admin-管理员，member-普通成员
     */
    @Column("role")
    private String role;

    /**
     * 加入时间
     */
    @Column("joinTime")
    private LocalDateTime joinTime;

    /**
     * 最后阅读时间
     */
    @Column("lastReadTime")
    private LocalDateTime lastReadTime;

    /**
     * 是否被禁言：0-否，1-是
     */
    @Column("isMuted")
    private Integer isMuted;

    /**
     * 禁言到期时间
     */
    @Column("mutedUntil")
    private LocalDateTime mutedUntil;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;
}