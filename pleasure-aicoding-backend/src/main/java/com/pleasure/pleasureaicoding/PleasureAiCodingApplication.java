package com.pleasure.pleasureaicoding;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pleasure.pleasureaicoding.mapper")
public class PleasureAiCodingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PleasureAiCodingApplication.class, args);
    }

}
