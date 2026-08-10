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

package com.alibaba.nacos.config.server.model;

/**
 * 灰度配置包装类：在 {@link ConfigInfo} 上附加灰度名称、规则、来源用户及修改时间。
 * 用于灰度发布场景下向客户端返回完整灰度元数据。
 * ConfigInfoGrayWrapper.
 *
 * @author rong
 */
public class ConfigInfoGrayWrapper extends ConfigInfo {
    
    private static final long serialVersionUID = 4511997591465712505L;
    
    /** 灰度配置最后修改时间（毫秒） */
    private long lastModified;
    
    /** 灰度版本名称标识 */
    private String grayName;
    
    /** 灰度匹配规则表达式或 JSON */
    private String grayRule;
    
    /** 创建或最后修改该灰度配置的用户 */
    private String srcUser;
    
    /** 无参构造 */
    public ConfigInfoGrayWrapper() {
    }
    
    /** 获取最后修改时间 */
    public long getLastModified() {
        return lastModified;
    }
    
    /** 设置最后修改时间 */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    /** 获取灰度名称 */
    public String getGrayName() {
        return grayName;
    }
    
    /** 设置灰度名称 */
    public void setGrayName(String grayName) {
        this.grayName = grayName;
    }
    
    /** 获取灰度规则 */
    public String getGrayRule() {
        return grayRule;
    }
    
    /** 设置灰度规则 */
    public void setGrayRule(String grayRule) {
        this.grayRule = grayRule;
    }
    
    /** 获取来源用户 */
    public String getSrcUser() {
        return srcUser;
    }
    
    /** 设置来源用户 */
    public void setSrcUser(String srcUser) {
        this.srcUser = srcUser;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
