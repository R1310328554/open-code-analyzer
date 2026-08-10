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
 * Nacos 配置历史记录概要信息。
 *
 * <p>包含操作来源、操作类型及发布方式等元数据，不含完整配置内容。</p>
 *
 * @author xiweng.yy
 */
public class ConfigHistoryBasicInfo extends ConfigBasicInfo {
    
    private static final long serialVersionUID = -5429814695967367742L;
    
    /** 执行操作的来源 IP。 */
    private String srcIp;
    
    /** 执行操作的用户名。 */
    private String srcUser;
    
    /** 操作类型，如新增、更新或删除。 */
    private String opType;
    
    /** 发布方式（如正式或灰度）。 */
    private String publishType;
    
    /** 获取操作来源 IP。 */
    public String getSrcIp() {
        return srcIp;
    }
    
    /** 设置操作来源 IP。 */
    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }
    
    /** 获取操作用户。 */
    public String getSrcUser() {
        return srcUser;
    }
    
    /** 设置操作用户。 */
    public void setSrcUser(String srcUser) {
        this.srcUser = srcUser;
    }
    
    /** 获取操作类型。 */
    public String getOpType() {
        return opType;
    }
    
    /** 设置操作类型。 */
    public void setOpType(String opType) {
        this.opType = opType;
    }
    
    /** 获取发布方式。 */
    public String getPublishType() {
        return publishType;
    }
    
    /** 设置发布方式。 */
    public void setPublishType(String publishType) {
        this.publishType = publishType;
    }
}
