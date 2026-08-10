/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
 *
 */

package com.alibaba.nacos.api.model.v2;

/**
 * 公告/通知支持的语言枚举。
 *
 * <p>每个常量对应 BCP 47 语言标签，供控制台公告模块校验请求语言。</p>
 *
 * @author zhangyukun on:2024/9/24
 */
public enum SupportedLanguage {
    
    /** 简体中文（zh-CN）。 */
    ZH_CN("zh-CN"),
    
    /** 美式英语（en-US）。 */
    EN_US("en-US");
    
    /** BCP 47 语言标签。 */
    private final String language;
    
    /** 绑定语言标签。 */
    SupportedLanguage(String language) {
        this.language = language;
    }
    
    /** 返回 BCP 47 语言标签。 */
    public String getLanguage() {
        return language;
    }
    
    /**
     * 判断给定语言标签是否受支持。
     *
     * @param language 待校验的语言标签
     * @return 支持则 {@code true}
     */
    public static boolean isSupported(String language) {
        for (SupportedLanguage lang : SupportedLanguage.values()) {
            if (lang.getLanguage().equals(language)) {
                return true;
            }
        }
        return false;
    }
}
