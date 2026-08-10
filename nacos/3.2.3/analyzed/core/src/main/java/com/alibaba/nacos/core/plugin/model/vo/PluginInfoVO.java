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

package com.alibaba.nacos.core.plugin.model.vo;

/**
 * 插件列表视图对象，用于控制台插件管理页展示摘要与集群可用性。
 * Plugin Info VO for list display.
 *
 * @author Nacos
 */
public class PluginInfoVO {
    
    /** 插件唯一 ID。 */
    private String pluginId;
    
    /** 插件类型。 */
    private String pluginType;
    
    /** 插件名称。 */
    private String pluginName;
    
    /** 是否已启用。 */
    private Boolean enabled;
    
    /** 是否为关键插件。 */
    private Boolean critical;
    
    /** 是否支持动态配置。 */
    private Boolean configurable;
    
    /**
     * 该插件类型是否互斥（同一时刻仅允许一个实例生效）。
     * AUTH、DATASOURCE_DIALECT 等互斥插件不可通过 UI 切换。
     * Whether this plugin type is exclusive (only one can be active at a time).
     * Exclusive plugins (AUTH, DATASOURCE_DIALECT) cannot be switched via UI.
     */
    /** 是否互斥类型插件。 */
    private Boolean exclusive;
    
    /** 集群中已加载该插件的可用节点数。 */
    private Integer availableNodeCount;
    
    /** 集群总节点数。 */
    private Integer totalNodeCount;
    
    /** 获取插件 ID。 */
    public String getPluginId() {
        return pluginId;
    }
    
    /** 设置插件 ID。 */
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
    
    /** 获取插件类型。 */
    public String getPluginType() {
        return pluginType;
    }
    
    /** 设置插件类型。 */
    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }
    
    /** 获取插件名称。 */
    public String getPluginName() {
        return pluginName;
    }
    
    /** 设置插件名称。 */
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }
    
    /** 获取启用状态。 */
    public Boolean getEnabled() {
        return enabled;
    }
    
    /** 设置启用状态。 */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    /** 获取是否关键插件。 */
    public Boolean getCritical() {
        return critical;
    }
    
    /** 设置是否关键插件。 */
    public void setCritical(Boolean critical) {
        this.critical = critical;
    }
    
    /** 获取是否可配置。 */
    public Boolean getConfigurable() {
        return configurable;
    }
    
    /** 设置是否可配置。 */
    public void setConfigurable(Boolean configurable) {
        this.configurable = configurable;
    }
    
    /** 获取是否互斥类型。 */
    public Boolean getExclusive() {
        return exclusive;
    }
    
    /** 设置是否互斥类型。 */
    public void setExclusive(Boolean exclusive) {
        this.exclusive = exclusive;
    }
    
    /** 获取可用节点数。 */
    public Integer getAvailableNodeCount() {
        return availableNodeCount;
    }
    
    /** 设置可用节点数。 */
    public void setAvailableNodeCount(Integer availableNodeCount) {
        this.availableNodeCount = availableNodeCount;
    }
    
    /** 获取集群总节点数。 */
    public Integer getTotalNodeCount() {
        return totalNodeCount;
    }
    
    /** 设置集群总节点数。 */
    public void setTotalNodeCount(Integer totalNodeCount) {
        this.totalNodeCount = totalNodeCount;
    }
    
    /** 返回插件摘要的字符串表示。 */
    @Override
    public String toString() {
        return "PluginInfoVO{" + "pluginId='" + pluginId + '\'' + ", pluginType='" + pluginType
            + '\''
            + ", pluginName='" + pluginName + '\'' + ", enabled=" + enabled + ", critical="
            + critical
            + ", configurable=" + configurable + ", exclusive=" + exclusive + '}';
    }
}
