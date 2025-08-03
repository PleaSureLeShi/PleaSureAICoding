package com.pleasure.pleasureaicoding.ai;

import com.pleasure.pleasureaicoding.ai.model.HtmlCodeResult;
import com.pleasure.pleasureaicoding.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

// ai service的junit测试类
@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做个PleaSure乐事小哥哥的工作记录小工具，不超过25行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("做个PleaSure乐事的留言板，不超过70行");
        Assertions.assertNotNull(multiFileCode);
    }
}
