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

package com.alibaba.nacos.api.config.model;

/**
 * Nacos 配置历史详情，包含某次变更的完整内容与扩展信息。
 *
 * <p>继承 {@link ConfigHistoryBasicInfo} 的操作元数据。</p>
 *
 * @author xiweng.yy
 */
public class ConfigHistoryDetailInfo extends ConfigHistoryBasicInfo {
    
    private static final long serialVersionUID = 5498431203024164923L;
    
    /** 该历史版本对应的配置内容。 */
    private String content;
    
    /** 加密数据密钥。 */
    private String encryptedDataKey;
    
    /** 关联的灰度名称（灰度发布时）。 */
    private String grayName;
    
    /** 扩展信息 JSON 字符串。 */
    private String extInfo;
    
    /** 获取历史配置内容。 */
    public String getContent() {
        return content;
    }
    
    /** 设置历史配置内容。 */
    public void setContent(String content) {
        this.content = content;
    }
    
    /** 获取加密数据密钥。 */
    public String getEncryptedDataKey() {
        return encryptedDataKey;
    }
    
    /** 设置加密数据密钥。 */
    public void setEncryptedDataKey(String encryptedDataKey) {
        this.encryptedDataKey = encryptedDataKey;
    }
    
    /** 获取灰度名称。 */
    public String getGrayName() {
        return grayName;
    }
    
    /** 设置灰度名称。 */
    public void setGrayName(String grayName) {
        this.grayName = grayName;
    }
    
    /** 获取扩展信息。 */
    public String getExtInfo() {
        return extInfo;
    }
    
    /** 设置扩展信息。 */
    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }
}
