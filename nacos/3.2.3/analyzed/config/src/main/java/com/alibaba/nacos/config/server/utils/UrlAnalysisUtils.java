/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.utils;

import com.alibaba.nacos.config.server.constant.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL 解析工具：从增量发布内容中提取 scheme + 主机 + 端口作为内容标识。
 * Url util.
 *
 * @author leiwen.zh
 */
public class UrlAnalysisUtils {
    
    /** 匹配 scheme://host:port?query 形式的 URL 正则 */
    private static final Pattern URL_PATTERN =
        Pattern.compile("^(\\w+://)?([\\w\\.]+:)(\\d*)?(\\??.*)");
    
    /**
     * 从增量发布内容解析 URL 标识（scheme + address + port），非法内容返回 null。
     *
     * @param content 增量发布单行内容
     * @return URL 标识字符串，校验失败时返回 null
     */
    public static String getContentIdentity(String content) {
        
        if (!verifyIncrementPubContent(content)) {
            return null;
        }
        
        Matcher matcher = URL_PATTERN.matcher(content);
        StringBuilder buf = new StringBuilder();
        if (matcher.find()) {
            String scheme = matcher.group(1);
            String address = matcher.group(2);
            String port = matcher.group(3);
            if (scheme != null) {
                buf.append(scheme);
            }
            buf.append(address);
            if (port != null) {
                buf.append(port);
            }
        }
        return buf.toString();
    }
    
    /**
     * 校验增量发布内容：非空、不含换行符、不含词分隔符 {@link Constants#WORD_SEPARATOR}。
     */
    private static boolean verifyIncrementPubContent(String content) {
        
        if (content == null || content.length() == 0) {
            return false;
        }
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\r' || c == '\n') {
                return false;
            }
            if (c == Constants.WORD_SEPARATOR.charAt(0)) {
                return false;
            }
        }
        return true;
    }
}
