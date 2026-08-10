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

package com.alibaba.nacos.core.plugin.model.form;

/**
 * 插件启用/禁用状态更新表单，用于控制台切换插件开关。
 * Plugin Status Update Form.
 *
 * @author Nacos
 */
public class PluginStatusForm {
    
    /** 插件类型标识。 */
    private String pluginType;
    
    /** 插件名称。 */
    private String pluginName;
    
    /** 目标启用状态。 */
    private boolean enabled;
    
    /** 是否仅在本节点生效，跳过集群同步。 */
    private boolean localOnly = false;
    
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
    
    /** 获取目标启用状态。 */
    public boolean isEnabled() {
        return enabled;
    }
    
    /** 设置目标启用状态。 */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /** 是否仅本地生效。 */
    public boolean isLocalOnly() {
        return localOnly;
    }
    
    /** 设置是否仅本地生效。 */
    public void setLocalOnly(boolean localOnly) {
        this.localOnly = localOnly;
    }
}
