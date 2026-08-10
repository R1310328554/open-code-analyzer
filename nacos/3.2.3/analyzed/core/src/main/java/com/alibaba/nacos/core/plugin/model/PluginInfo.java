/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.plugin.model;

import com.alibaba.nacos.api.plugin.ConfigItemDefinition;
import com.alibaba.nacos.api.plugin.PluginType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 插件元信息模型：ID、类型、启用/关键/可配置标志及配置定义。
 * Plugin information model.
 *
 * @author WangzJi
 * @since 3.2.0
 */
public class PluginInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 插件唯一 ID，格式 type:name。 */
    /**
     * Plugin ID, format: "{type}:{name}".
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private String pluginId;
    
    /** 插件名称（Provider 注册名）。 */
    /**
     * Plugin name.
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private String pluginName;
    
    /** 插件类型枚举。 */
    /**
     * Plugin type.
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private PluginType pluginType;
    
    /** 插件实现类全限定名。 */
    /**
     * Plugin class name.
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private String className;
    
    /** 插件描述（可选）。 */
    /**
     * Plugin description.
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private String description;
    
    /** 当前是否启用。 */
    /**
     * Whether the plugin is enabled.
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private boolean enabled;
    
    /** 是否为关键插件（不可禁用）。 */
    /**
     * Whether this is a critical plugin (cannot be disabled).
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private boolean critical;
    
    /** 是否支持动态配置。 */
    /**
     * Whether the plugin supports configuration.
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private boolean configurable;
    
    /** 插件加载时间戳（毫秒）。 */
    /**
     * Plugin load timestamp.
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private long loadTimestamp;
    
    /** 当前运行时配置键值。 */
    /**
     * Current configuration.
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private Map<String, String> config;
    
    /** 配置项定义列表（Schema）。 */
    /**
     * Configuration item definitions.
      * <p>插件元信息模型；详见类级说明。</p>
     */
    private List<ConfigItemDefinition> configDefinitions;
    
    public PluginInfo() {
    }
    
    public String getPluginId() {
        return pluginId;
    }
    
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }
    
    public PluginType getPluginType() {
        return pluginType;
    }
    
    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isCritical() {
        return critical;
    }
    
    public void setCritical(boolean critical) {
        this.critical = critical;
    }
    
    public boolean isConfigurable() {
        return configurable;
    }
    
    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }
    
    public long getLoadTimestamp() {
        return loadTimestamp;
    }
    
    public void setLoadTimestamp(long loadTimestamp) {
        this.loadTimestamp = loadTimestamp;
    }
    
    public Map<String, String> getConfig() {
        return config;
    }
    
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
    
    public List<ConfigItemDefinition> getConfigDefinitions() {
        return configDefinitions;
    }
    
    public void setConfigDefinitions(List<ConfigItemDefinition> configDefinitions) {
        this.configDefinitions = configDefinitions;
    }
}
