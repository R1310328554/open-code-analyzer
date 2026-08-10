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
 * Nacos 配置详情信息，在 {@link ConfigBasicInfo} 基础上包含完整内容与创建元数据。
 *
 * <p>控制台查询、导出等场景返回本类型。</p>
 *
 * @author xiweng.yy
 */
public class ConfigDetailInfo extends ConfigBasicInfo {
    
    private static final long serialVersionUID = -6659977504609721215L;
    
    /** 配置内容正文。 */
    private String content;
    
    /** 加密数据密钥（启用加密时非空）。 */
    private String encryptedDataKey;
    
    /** 创建该配置的用户名。 */
    private String createUser;
    
    /** 创建该配置的客户端 IP。 */
    private String createIp;
    
    /** 获取配置内容。 */
    public String getContent() {
        return content;
    }
    
    /** 设置配置内容。 */
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
    
    /** 获取创建用户。 */
    public String getCreateUser() {
        return createUser;
    }
    
    /** 设置创建用户。 */
    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }
    
    /** 获取创建 IP。 */
    public String getCreateIp() {
        return createIp;
    }
    
    /** 设置创建 IP。 */
    public void setCreateIp(String createIp) {
        this.createIp = createIp;
    }
}
