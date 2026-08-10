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
 * 配置主实体：在 {@link ConfigInfoBase} 基础上扩展 tenant、appName、类型、
 * 描述、标签与修改时间，是 Config 模块最核心的持久化与 API 模型。
 * ConfigInfo.
 *
 * @author boyan
 * @date 2010-5-4
 */
public class ConfigInfo extends ConfigInfoBase {
    
    static final long serialVersionUID = 3115358782431229202L;
    
    /** 命名空间 ID（tenant） */
    private String tenant;
    
    /** 关联应用名，用于归属与检索 */
    private String appName;
    
    /** 配置内容类型（text/json/yaml 等） */
    private String type;
    
    /** 配置描述 */
    private String desc;
    
    /** 配置标签，逗号分隔 */
    private String configTags;
    
    /** 最后修改时间（毫秒时间戳） */
    private Long gmtModified;
    
    public ConfigInfo() {
    }
    
    public ConfigInfo(String dataId, String group, String content) {
        super(dataId, group, content);
    }
    
    public ConfigInfo(String dataId, String group, String appName, String content) {
        super(dataId, group, content);
        this.appName = appName;
    }
    
    /** 以 dataId、group、tenant、appName 与 content 构造完整配置实体 */
    public ConfigInfo(String dataId, String group, String tenant, String appName, String content) {
        super(dataId, group, content);
        this.tenant = tenant;
        this.appName = appName;
    }
    
    public String getTenant() {
        return tenant;
    }
    
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public String getConfigTags() {
        return configTags;
    }
    
    public void setConfigTags(String configTags) {
        this.configTags = configTags;
    }
    
    public Long getGmtModified() {
        return gmtModified;
    }
    
    public void setGmtModified(Long gmtModified) {
        this.gmtModified = gmtModified;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    @Override
    public String toString() {
        return "ConfigInfo{" + "id=" + getId() + ", dataId='" + getDataId() + '\'' + ", group='"
            + getGroup() + '\''
            + ", tenant='" + tenant + '\'' + ", appName='" + appName + '\'' + ", content='"
            + getContent() + '\''
            + ", md5='" + getMd5() + '\'' + ", type='" + type + '\'' + ", desc='" + desc + '\''
            + ", configTags='" + configTags + '\'' + '}';
    }
    
}
