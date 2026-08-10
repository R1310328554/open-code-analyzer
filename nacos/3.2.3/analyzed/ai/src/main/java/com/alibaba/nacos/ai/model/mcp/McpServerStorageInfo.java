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

package com.alibaba.nacos.ai.model.mcp;

import com.alibaba.nacos.api.ai.model.mcp.McpServerBasicInfo;

/**
 * Storage model for AI MCP server.
 * <p>MCP 服务持久化存储模型，继承 {@link McpServerBasicInfo}，额外持有 tools/prompt/resource 描述内容的外部引用（dataId 或 URL）。</p>
 *
 * @author xiweng.yy
 */
public class McpServerStorageInfo extends McpServerBasicInfo {
    
    /** Tools 描述内容的配置引用或存储路径。 */
    private String toolsDescriptionRef;
    
    /** Prompt 描述内容的配置引用或存储路径。 */
    private String promptDescriptionRef;
    
    /** Resource 描述内容的配置引用或存储路径。 */
    private String resourceDescriptionRef;
    
    public String getToolsDescriptionRef() {
        return toolsDescriptionRef;
    }
    
    public void setToolsDescriptionRef(String toolsDescriptionRef) {
        this.toolsDescriptionRef = toolsDescriptionRef;
    }
    
    public String getPromptDescriptionRef() {
        return promptDescriptionRef;
    }
    
    public void setPromptDescriptionRef(String promptDescriptionRef) {
        this.promptDescriptionRef = promptDescriptionRef;
    }
    
    public String getResourceDescriptionRef() {
        return resourceDescriptionRef;
    }
    
    public void setResourceDescriptionRef(String resourceDescriptionRef) {
        this.resourceDescriptionRef = resourceDescriptionRef;
    }
}
