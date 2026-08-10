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

package com.alibaba.nacos.plugin.ai.importer.model;

import java.util.List;
import java.util.Map;

/**
 * 由运维配置解析得到的运行时导入来源。
 *
 * <p>模型可包含端点地址与凭据引用，供导入插件在服务端内部使用，
 * 不得原样返回给终端用户。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportSource {
    
    /** 来源唯一标识，用户通过 sourceId 选择导入源。 */
    private String sourceId;
    
    /** 控制台展示名称。 */
    private String displayName;
    
    /** 来源描述信息。 */
    private String description;
    
    /** 负责该来源的导入插件名称。 */
    private String pluginName;
    
    /** 该来源支持的资源类型列表。 */
    private List<String> resourceTypes;
    
    /** 外部 API 或服务端点 URL。 */
    private String endpoint;
    
    /** 是否启用该导入来源。 */
    private boolean enabled;
    
    /** 认证凭据引用（非明文密钥）。 */
    private String authRef;
    
    /** HTTP 连接超时（毫秒）。 */
    private int connectTimeoutMillis;
    
    /** HTTP 读取超时（毫秒）。 */
    private int readTimeoutMillis;
    
    /** 搜索翻页最大页数限制。 */
    private int maxPageCount;
    
    /** 单次导入最大条目数限制。 */
    private int maxItemCount;
    
    /** 单个 Artifact 最大字节数限制。 */
    private long maxArtifactSize;
    
    /** 插件自定义扩展属性。 */
    private Map<String, String> properties;
    
    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }
    
    public List<String> getResourceTypes() {
        return resourceTypes;
    }
    
    public void setResourceTypes(List<String> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getAuthRef() {
        return authRef;
    }
    
    public void setAuthRef(String authRef) {
        this.authRef = authRef;
    }
    
    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }
    
    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }
    
    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }
    
    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }
    
    public int getMaxPageCount() {
        return maxPageCount;
    }
    
    public void setMaxPageCount(int maxPageCount) {
        this.maxPageCount = maxPageCount;
    }
    
    public int getMaxItemCount() {
        return maxItemCount;
    }
    
    public void setMaxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;
    }
    
    public long getMaxArtifactSize() {
        return maxArtifactSize;
    }
    
    public void setMaxArtifactSize(long maxArtifactSize) {
        this.maxArtifactSize = maxArtifactSize;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
