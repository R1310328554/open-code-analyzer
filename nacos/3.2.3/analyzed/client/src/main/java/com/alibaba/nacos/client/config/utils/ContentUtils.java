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

package com.alibaba.nacos.client.config.utils;

import com.alibaba.nacos.api.common.Constants;

import static com.alibaba.nacos.api.common.Constants.WORD_SEPARATOR;

/**
 * 配置内容校验与截取工具。
 *
 * <p>用于增量发布/删除场景的内容格式校验，以及日志输出时的内容截断。</p>
 *
 * @author Nacos
 */
public class ContentUtils {
    
    /** 日志展示时配置内容的最大长度。 */
    private static final int SHOW_CONTENT_SIZE = 100;
    
    /**
     * 校验增量发布/删除内容格式。
     *
     * <p>内容不得为空，且不能包含回车、换行及 {@link Constants#WORD_SEPARATOR} 分隔符。</p>
     *
     * @param content 待校验的配置内容
     * @throws IllegalArgumentException 内容不合法时抛出
     */
    public static void verifyIncrementPubContent(String content) {
        
        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("publish/delete content can not be null");
        }
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\r' || c == '\n') {
                throw new IllegalArgumentException(
                    "publish/delete content can not contain return and linefeed");
            }
            if (c == Constants.WORD_SEPARATOR.charAt(0)) {
                throw new IllegalArgumentException("publish/delete content can not contain(char)2");
            }
        }
    }
    
    /**
     * 从带分隔符的增量内容中提取 identity 段（分隔符之前）。
     *
     * @param content 含 {@link Constants#WORD_SEPARATOR} 的完整内容
     * @return identity 字符串
     * @throws IllegalArgumentException 未包含分隔符时抛出
     */
    public static String getContentIdentity(String content) {
        int index = content.indexOf(WORD_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("content does not contain separator");
        }
        return content.substring(0, index);
    }
    
    /**
     * 从带分隔符的增量内容中提取 payload 段（分隔符之后）。
     *
     * @param content 含 {@link Constants#WORD_SEPARATOR} 的完整内容
     * @return 实际配置内容
     * @throws IllegalArgumentException 未包含分隔符时抛出
     */
    public static String getContent(String content) {
        int index = content.indexOf(WORD_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("content does not contain separator");
        }
        return content.substring(index + 1);
    }
    
    /**
     * 截断过长配置内容，便于日志输出。
     *
     * @param content 原始配置内容
     * @return 不超过 {@link #SHOW_CONTENT_SIZE} 字符的内容，超出部分以 {@code ...} 结尾
     */
    public static String truncateContent(String content) {
        if (content == null) {
            return "";
        } else if (content.length() <= SHOW_CONTENT_SIZE) {
            return content;
        } else {
            return content.substring(0, SHOW_CONTENT_SIZE) + "...";
        }
    }
}
