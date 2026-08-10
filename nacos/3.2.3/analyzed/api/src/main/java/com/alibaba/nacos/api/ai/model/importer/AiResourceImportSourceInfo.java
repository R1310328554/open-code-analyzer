/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model.importer;

import java.io.Serializable;
import java.util.List;

/**
 * 返回给客户端的脱敏导入源信息。
 *
 * <p>不含敏感连接凭证，仅暴露展示名、插件名、支持的资源类型与能力列表，
 * 供控制台渲染可选导入源下拉与说明文案。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportSourceInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String sourceId;
    
    private String displayName;
    
    private String description;
    
    private String pluginName;
    
    private List<String> resourceTypes;
    
    private boolean enabled;
    
    private List<String> capabilities;
    
    /** 返回导入源唯一 ID。 */
    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    /** 返回控制台展示名称。 */
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /** 返回导入源描述。 */
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /** 返回底层 SPI 插件名称。 */
    public String getPluginName() {
        return pluginName;
    }
    
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }
    
    /** 返回该源支持的 AI 资源类型列表。 */
    public List<String> getResourceTypes() {
        return resourceTypes;
    }
    
    public void setResourceTypes(List<String> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }
    
    /** 导入源是否已启用。 */
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /** 返回插件能力标识列表（如搜索、校验、批量导入等）。 */
    public List<String> getCapabilities() {
        return capabilities;
    }
    
    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }
}
