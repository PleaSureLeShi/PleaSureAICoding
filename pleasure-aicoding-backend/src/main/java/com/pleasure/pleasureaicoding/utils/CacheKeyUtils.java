package com.pleasure.pleasureaicoding.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

// 缓存 key 生成工具类
public class CacheKeyUtils {

    // 根据对象生成缓存key (JSON + MD5)
    public static String generateKey(Object obj) {
        if (obj == null) {
            return DigestUtil.md5Hex("null");
        }
        // 先转JSON，再MD5
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);
    }
}
