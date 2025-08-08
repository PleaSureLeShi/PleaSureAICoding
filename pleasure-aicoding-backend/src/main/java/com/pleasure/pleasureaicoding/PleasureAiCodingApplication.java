package com.pleasure.pleasureaicoding;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.pleasure.pleasureaicoding.mapper")
public class PleasureAiCodingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PleasureAiCodingApplication.class, args);
    }

}
