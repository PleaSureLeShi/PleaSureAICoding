package com.pleasure.pleasureaicoding.ai.tools;

import cn.hutool.json.JSONObject;

// 工具基类,定义所有工具的通用接口
public abstract class BaseTool {

    public abstract String getToolName();

    public abstract String getDisplayName();

    // 生成工具请求时的返回值（显示给用户）
    public String generateToolRequestResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }

    public abstract String generateToolExecutedResult(JSONObject arguments);
}
