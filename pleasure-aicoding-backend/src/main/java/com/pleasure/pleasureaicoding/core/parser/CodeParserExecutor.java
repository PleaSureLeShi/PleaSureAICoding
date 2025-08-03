package com.pleasure.pleasureaicoding.core.parser;

import com.pleasure.pleasureaicoding.exception.BusinessException;
import com.pleasure.pleasureaicoding.exception.ErrorCode;
import com.pleasure.pleasureaicoding.model.enums.CodeGenTypeEnum;

// 根据代码生成类型执行相应的解析逻辑
// 代码解析器执行器
public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}
