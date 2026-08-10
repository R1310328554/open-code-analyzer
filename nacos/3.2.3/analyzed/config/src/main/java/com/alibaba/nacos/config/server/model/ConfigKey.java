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

import java.io.Serializable;
import java.util.Objects;

/**
 * 配置逻辑键：由 appName、dataId、group 组成，用于索引与去重。
 * 不含 tenant，命名空间由调用上下文单独传递。
 * ConfigKey.
 *
 * @author Nacos
 */
public class ConfigKey implements Serializable {
    
    private static final long serialVersionUID = -1748953484511867580L;
    
    /** 关联应用名 */
    private String appName;
    
    /** 配置 dataId */
    private String dataId;
    
    /** 配置 group */
    private String group;
    
    /** 无参构造 */
    public ConfigKey() {
    }
    
    /** 获取应用名 */
    public String getAppName() {
        return appName;
    }
    
    /** 设置应用名 */
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    /** 获取 dataId */
    public String getDataId() {
        return dataId;
    }
    
    /** 设置 dataId */
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
    /** 获取 group */
    public String getGroup() {
        return group;
    }
    
    /** 设置 group */
    public void setGroup(String group) {
        this.group = group;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConfigKey configKey = (ConfigKey) o;
        return Objects.equals(appName, configKey.appName)
            && Objects.equals(dataId, configKey.dataId) && Objects.equals(
                group, configKey.group);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(appName, dataId, group);
    }
}
