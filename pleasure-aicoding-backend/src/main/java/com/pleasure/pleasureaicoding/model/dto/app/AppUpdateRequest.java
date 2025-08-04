package com.pleasure.pleasureaicoding.model.dto.app;

import lombok.Data;

import java.io.Serializable;

// 更新应用请求
@Data
public class AppUpdateRequest implements Serializable {

    private Long id;
    private String appName;

    private static final long serialVersionUID = 1L;
}
