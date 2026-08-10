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

package com.alibaba.nacos.api.config;

/**
 * 配置变更项，描述单个配置键在一次变更中的旧值、新值及变更类型。
 *
 * <p>通常由 {@link ConfigChangeEvent} 聚合，供监听器对比差异。</p>
 *
 * @author rushsky518
 */
public class ConfigChangeItem {
    
    /** 发生变更的配置键名。 */
    private String key;
    
    /** 变更前的旧值。 */
    private String oldValue;
    
    /** 变更后的新值。 */
    private String newValue;
    
    /** 变更类型（新增、修改或删除）。 */
    private PropertyChangeType type;
    
    /**
     * 构造配置变更项（变更类型由调用方后续设置）。
     *
     * @param key      配置键
     * @param oldValue 旧值
     * @param newValue 新值
     */
    public ConfigChangeItem(String key, String oldValue, String newValue) {
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    /** 获取配置键名。 */
    public String getKey() {
        return key;
    }
    
    /** 设置配置键名。 */
    public void setKey(String key) {
        this.key = key;
    }
    
    /** 获取变更前的旧值。 */
    public String getOldValue() {
        return oldValue;
    }
    
    /** 设置变更前的旧值。 */
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
    
    /** 获取变更后的新值。 */
    public String getNewValue() {
        return newValue;
    }
    
    /** 设置变更后的新值。 */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
    
    /** 获取变更类型。 */
    public PropertyChangeType getType() {
        return type;
    }
    
    /** 设置变更类型。 */
    public void setType(PropertyChangeType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "ConfigChangeItem{" + "key='" + key + '\'' + ", oldValue='" + oldValue + '\''
            + ", newValue='" + newValue
            + '\'' + ", type=" + type + '}';
    }
}
