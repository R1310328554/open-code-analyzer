/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.config;

import java.io.Serializable;

/**
 * 配置查询结果，包含配置内容及 MD5 等元数据。
 *
 * <p>可用于 CAS（Compare-And-Swap）发布等需要内容校验的场景。</p>
 *
 * @author nacos
 * @since 3.0
 */
public class ConfigQueryResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 配置内容正文。 */
    private String content;
    
    /** 配置内容的 MD5 摘要，用于 CAS 校验。 */
    private String md5;
    
    /** 配置类型（json、yaml、properties 等）。 */
    private String configType;
    
    /** 加密数据密钥（启用加密时非空）。 */
    private String encryptedDataKey;
    
    /** 无参构造，供序列化或框架实例化使用。 */
    public ConfigQueryResult() {
    }
    
    /**
     * 构造包含内容与 MD5 的查询结果。
     *
     * @param content 配置内容
     * @param md5     内容 MD5
     */
    public ConfigQueryResult(String content, String md5) {
        this.content = content;
        this.md5 = md5;
    }
    
    /** 获取配置内容。 */
    public String getContent() {
        return content;
    }
    
    /** 设置配置内容。 */
    public void setContent(String content) {
        this.content = content;
    }
    
    /** 获取内容 MD5 摘要。 */
    public String getMd5() {
        return md5;
    }
    
    /** 设置内容 MD5 摘要。 */
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    /** 获取配置类型。 */
    public String getConfigType() {
        return configType;
    }
    
    /** 设置配置类型。 */
    public void setConfigType(String configType) {
        this.configType = configType;
    }
    
    /** 获取加密数据密钥。 */
    public String getEncryptedDataKey() {
        return encryptedDataKey;
    }
    
    /** 设置加密数据密钥。 */
    public void setEncryptedDataKey(String encryptedDataKey) {
        this.encryptedDataKey = encryptedDataKey;
    }
    
    @Override
    public String toString() {
        return "ConfigQueryResult{" + "content='"
            + (content != null ? content.substring(0, Math.min(50, content.length())) + "..."
                : "null")
            + '\'' + ", md5='" + md5 + '\'' + ", configType='" + configType + '\'' + '}';
    }
}
