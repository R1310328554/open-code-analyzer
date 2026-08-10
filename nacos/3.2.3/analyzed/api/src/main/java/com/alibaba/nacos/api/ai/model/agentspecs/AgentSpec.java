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

package com.alibaba.nacos.api.ai.model.agentspecs;

import java.util.Map;

/**
 * HiClaw Worker 包管理的 AgentSpec 实体，结构与 Skill 类似，以 manifest.json 为主内容。
 *
 * <p>包含命名空间、名称、描述、业务标签、manifest 原文及资源文件映射，
 * 供 Nacos AI 模块发布与订阅 Agent 工作包。</p>
 *
 * @author nacos
 */
public class AgentSpec {
    
    /** Nacos 命名空间 ID（治理字段）。 */
    
    private String namespaceId;
    
    /** AgentSpec 名称（取自 manifest.json 的 worker.suggested_name）。 */
    
    private String name;
    
    /** AgentSpec 描述信息。 */
    
    private String description;
    
    /** 业务标签，以 JSON 数组字符串存储。 */
    
    private String bizTags;
    
    /** manifest.json 原始 JSON 字符串内容。 */
    
    private String content;
    
    /** 资源文件映射（键为资源路径，如 config/SOUL.md）。 */
    
    private Map<String, AgentSpecResource> resource;
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBizTags() {
        return bizTags;
    }
    
    public void setBizTags(String bizTags) {
        this.bizTags = bizTags;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Map<String, AgentSpecResource> getResource() {
        return resource;
    }
    
    public void setResource(Map<String, AgentSpecResource> resource) {
        this.resource = resource;
    }
}
