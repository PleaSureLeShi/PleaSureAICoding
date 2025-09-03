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
 * 好友关系 实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("friendship")
public class Friendship implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 好友关系ID
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户ID
     */
    @Column("userId")
    private Long userId;

    /**
     * 好友ID
     */
    @Column("friendId")
    private Long friendId;

    /**
     * 关系状态：0-待确认，1-已同意，2-已拒绝，3-已拉黑
     */
    @Column("status")
    private Integer status;

    /**
     * 好友申请消息
     */
    @Column("requestMessage")
    private String requestMessage;

    /**
     * 申请时间
     */
    @Column("requestTime")
    private LocalDateTime requestTime;

    /**
     * 响应时间
     */
    @Column("responseTime")
    private LocalDateTime responseTime;

    /**
     * 好友备注
     */
    @Column("remark")
    private String remark;

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