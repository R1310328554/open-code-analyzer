/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.common.pathencoder.impl;

import com.alibaba.nacos.common.pathencoder.PathEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Windows 平台路径编码器：将路径中非法字符（/ : ? " < > | \）替换为 %A1%～%A9% 占位符，
 * 以便在 Windows 文件系统约束下安全存储配置键等路径字符串。
 * Encode path if illegal char reach in Windows.
 *
 * @author daydreamer-ia
 */
public class WindowsEncoder implements PathEncoder {
    
    /**
     * 不应该含有 / : ? " < > | \.
     */
    private static final String PATTERN_EXP = "[^/:*?\"<>|\\\\]+";
    
    /** 正则替换映射：非法字符模式 → 占位符编码 */
    private static final Map<String, String> REG_MAPPING = new HashMap<>();
    
    /** 解码映射：占位符 → 原始字符 */
    private static final Map<String, String> CHAR_MAPPING = new HashMap<>();
    
    /** 合法路径片段匹配模式，用于 {@link #needEncode(String)} 判断 */
    private static final Pattern PATTERN = Pattern.compile(PATTERN_EXP);
    
    static {
        // 编码：正则 → 占位符
        REG_MAPPING.put("\\\\", "%A1%");
        REG_MAPPING.put("/", "%A2%");
        REG_MAPPING.put(":", "%A3%");
        REG_MAPPING.put("\\*", "%A4%");
        REG_MAPPING.put("\\?", "%A5%");
        REG_MAPPING.put("\"", "%A6%");
        REG_MAPPING.put("<", "%A7%");
        REG_MAPPING.put(">", "%A8%");
        REG_MAPPING.put("\\|", "%A9%");
        
        // 解码：占位符 → 原字符
        CHAR_MAPPING.put("%A1%", "\\\\");
        CHAR_MAPPING.put("%A2%", "/");
        CHAR_MAPPING.put("%A3%", ":");
        CHAR_MAPPING.put("%A4%", "*");
        CHAR_MAPPING.put("%A5%", "?");
        CHAR_MAPPING.put("%A6%", "\"");
        CHAR_MAPPING.put("%A7%", "<");
        CHAR_MAPPING.put("%A8%", ">");
        CHAR_MAPPING.put("%A9%", "|");
    }
    
    /** 按 REG_MAPPING 依次替换非法字符为占位符 */
    @Override
    public String encode(String str, String charset) {
        for (Map.Entry<String, String> entry : REG_MAPPING.entrySet()) {
            str = str.replaceAll(entry.getKey(), entry.getValue());
        }
        return str;
    }
    
    /** 按 CHAR_MAPPING 将占位符还原为原始字符 */
    @Override
    public String decode(String str, String charset) {
        for (Map.Entry<String, String> entry : CHAR_MAPPING.entrySet()) {
            str = str.replaceAll(entry.getKey(), entry.getValue());
        }
        return str;
    }
    
    /** 编码器标识名，固定返回 {@code window} */
    @Override
    public String name() {
        return "window";
    }
    
    /** 判断 key 是否含 Windows 非法字符，需编码则返回 true */
    @Override
    public boolean needEncode(String key) {
        if (key == null) {
            return false;
        }
        return !PATTERN.matcher(key).matches();
    }
}
