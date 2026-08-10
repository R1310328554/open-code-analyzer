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

package com.alibaba.nacos.ai.config;

import java.util.List;
import java.util.Map;

/**
 * Unified legacy prompt data for migration. Holds prompt metadata and version list
 * (without version content, which is loaded on demand via
 * {@link PromptLegacyDataReader#readVersionContent}).
 * <p>旧版 Prompt 迁移统一数据模型：包含命名空间、promptKey、描述、标签及版本列表；版本正文按需通过 {@link PromptLegacyDataReader} 加载。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public class LegacyPromptData {
    
    /** 命名空间 ID */
    private String namespaceId;
    
    /** Prompt 唯一键 */
    private String promptKey;
    
    /** 描述信息 */
    private String description;
    
    /** 业务标签列表 */
    private List<String> bizTags;
    
    /** 扩展标签键值对 */
    private Map<String, String> labels;
    
    /** 最新版本号 */
    private String latestVersion;
    
    /** 全部历史版本号列表（不含正文） */
    private List<String> versions;
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getPromptKey() {
        return promptKey;
    }
    
    public void setPromptKey(String promptKey) {
        this.promptKey = promptKey;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getBizTags() {
        return bizTags;
    }
    
    public void setBizTags(List<String> bizTags) {
        this.bizTags = bizTags;
    }
    
    public Map<String, String> getLabels() {
        return labels;
    }
    
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }
    
    public String getLatestVersion() {
        return latestVersion;
    }
    
    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }
    
    public List<String> getVersions() {
        return versions;
    }
    
    public void setVersions(List<String> versions) {
        this.versions = versions;
    }
}
