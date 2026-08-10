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
 * 配置变更标识：以 dataId、group、tenant 三元组描述一条发生变更的配置。
 * 用于长轮询比对、集群同步及变更通知的去重键。
 * ConfigInfoChanged.
 *
 * @author leiwen.zh
 */
public class ConfigInfoChanged implements Serializable {
    
    private static final long serialVersionUID = -1819539062100125171L;
    
    /** 配置 dataId */
    private String dataId;
    
    /** 配置 group */
    private String group;
    
    /** 命名空间/租户标识 */
    private String tenant;
    
    /**
     * 构造变更标识。
     *
     * @param dataId 配置 dataId
     * @param group  配置 group
     * @param tenant 命名空间
     */
    public ConfigInfoChanged(String dataId, String group, String tenant) {
        this.dataId = dataId;
        this.group = group;
        this.setTenant(tenant);
    }
    
    /** 无参构造，供序列化使用 */
    public ConfigInfoChanged() {
        
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConfigInfoChanged other = (ConfigInfoChanged) obj;
        if (dataId == null) {
            if (other.dataId != null) {
                return false;
            }
        } else if (!dataId.equals(other.dataId)) {
            return false;
        }
        if (group == null) {
            if (other.group != null) {
                return false;
            }
        } else if (!group.equals(other.group)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dataId, group);
    }
    
    @Override
    public String toString() {
        return "ConfigInfoChanged [dataId=" + dataId + ", group=" + group + "]";
    }
    
    /** 获取命名空间 */
    public String getTenant() {
        return tenant;
    }
    
    /** 设置命名空间 */
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
}
