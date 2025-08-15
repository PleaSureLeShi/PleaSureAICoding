package com.pleasure.pleasureaicoding.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserSetRoleRequest implements Serializable {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}