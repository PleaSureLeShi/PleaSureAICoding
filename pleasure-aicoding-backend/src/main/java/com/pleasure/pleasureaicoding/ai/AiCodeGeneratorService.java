package com.pleasure.pleasureaicoding.ai;

import com.pleasure.pleasureaicoding.ai.model.HtmlCodeResult;
import com.pleasure.pleasureaicoding.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGeneratorService {

    // 生成html代码
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);


    // 生成多文件代码
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);
}
