package com.pleasure.pleasureaicoding.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// AI代码生成类型路由服务工厂
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    // 直接使用先前完成的对话模式
    @Resource
    private ChatModel chatModel;

    // 创建AI代码生成类型路由服务实例
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }
}
