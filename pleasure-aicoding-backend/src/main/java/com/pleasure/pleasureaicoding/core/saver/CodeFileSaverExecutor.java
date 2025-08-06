package com.pleasure.pleasureaicoding.core.saver;

import com.pleasure.pleasureaicoding.ai.model.HtmlCodeResult;
import com.pleasure.pleasureaicoding.ai.model.MultiFileCodeResult;
import com.pleasure.pleasureaicoding.exception.BusinessException;
import com.pleasure.pleasureaicoding.exception.ErrorCode;
import com.pleasure.pleasureaicoding.model.enums.CodeGenTypeEnum;

import java.io.File;

// 代码文件保存执行器
// 根据代码生成类型执行相应的保存逻辑
public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();

    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();

    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType, Long appId) {
        return switch (codeGenType) {
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult, appId);
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}
