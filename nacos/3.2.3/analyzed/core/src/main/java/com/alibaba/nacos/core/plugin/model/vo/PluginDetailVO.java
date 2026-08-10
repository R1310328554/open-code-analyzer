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

import com.alibaba.nacos.api.plugin.ConfigItemDefinition;

import java.util.List;
import java.util.Map;

/**
 * 插件详情视图对象，包含配置项定义与当前配置值，供控制台详情页展示。
 * Plugin Detail VO.
 *
 * @author Nacos
 */
public class PluginDetailVO {
    
    /** 插件唯一 ID，格式 {@code type:name}。 */
    private String pluginId;
    
    /** 插件类型。 */
    private String pluginType;
    
    /** 插件名称。 */
    private String pluginName;
    
    /** 是否已启用。 */
    private Boolean enabled;
    
    /** 是否为关键插件（不可禁用）。 */
    private Boolean critical;
    
    /** 是否支持动态配置。 */
    private Boolean configurable;
    
    /** 当前配置键值对。 */
    private Map<String, String> config;
    
    /** 可配置项元数据定义列表。 */
    private List<ConfigItemDefinition> configDefinitions;
    
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
    
    /** 获取当前配置。 */
    public Map<String, String> getConfig() {
        return config;
    }
    
    /** 设置当前配置。 */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
    
    /** 获取配置项定义列表。 */
    public List<ConfigItemDefinition> getConfigDefinitions() {
        return configDefinitions;
    }
    
    /** 设置配置项定义列表。 */
    public void setConfigDefinitions(List<ConfigItemDefinition> configDefinitions) {
        this.configDefinitions = configDefinitions;
    }
    
    /** 返回插件详情的字符串表示。 */
    @Override
    public String toString() {
        return "PluginDetailVO{" + "pluginId='" + pluginId + '\'' + ", pluginType='" + pluginType
            + '\''
            + ", pluginName='" + pluginName + '\'' + ", enabled=" + enabled + ", critical="
            + critical
            + ", configurable=" + configurable + ", config=" + config + ", configDefinitions="
            + configDefinitions
            + '}';
    }
}
