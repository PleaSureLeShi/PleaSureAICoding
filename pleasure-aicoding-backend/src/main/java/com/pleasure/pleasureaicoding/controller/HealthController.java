package com.pleasure.pleasureaicoding.controller;

import com.pleasure.pleasureaicoding.common.BaseResponse;
import com.pleasure.pleasureaicoding.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


//http://localhost:8123/api/doc.html#/home
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success( "ok");
    }
}

