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

import java.util.Map;

/**
 * 导入插件搜索返回的外部 AI 资源摘要（候选项）。
 *
 * <p>候选项仅用于用户浏览与勾选，不得包含完整可导入载荷；
 * 完整内容须通过 {@link com.alibaba.nacos.plugin.ai.importer.spi.AiResourceImportService#fetch}
 * 单独拉取。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
public class AiResourceImportCandidate {
    
    /** 资源类型标识。 */
    private String resourceType;
    
    /** 外部系统唯一 ID，fetch 时作为定位键。 */
    private String externalId;
    
    /** 候选资源名称。 */
    private String name;
    
    /** 候选资源版本。 */
    private String version;
    
    /** 候选资源描述。 */
    private String description;
    
    /** 搜索侧附加元数据，不含完整载荷。 */
    private Map<String, String> metadata;
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getExternalId() {
        return externalId;
    }
    
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
}
