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

package com.alibaba.nacos.api.ai.model.mcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具规格文档，聚合工具列表、元数据、安全方案与扩展字段。
 *
 * <p>作为 MCP Server 工具配置的持久化载体，支持明文与加密两种存储模式，
 * 对应 Nacos Config 中的 tools 描述配置。</p>
 *
 * @author xiweng.yy
 */
public class McpToolSpecification {
    
    /**
     * 工具规格存储类型，默认 "normal"（明文）。
     * 设为 "encrypted" 或 "encrypt-kms" 等时，服务端原样持久化 encryptData，
     * 并跳过 tools/securitySchemes 解析。
     */
    private String specificationType;
    
    /** 当 specificationType 为加密类型时的密文载荷及元数据。 */
    private EncryptObject encryptData;
    
    /** MCP 工具定义列表。 */
    private List<McpTool> tools = new LinkedList<>();
    
    /** 以工具名为键的元数据映射。 */
    private Map<String, McpToolMeta> toolsMeta = new HashMap<>(1);
    
    /** 工具调用所需的安全认证方案列表。 */
    private List<SecurityScheme> securitySchemes = new ArrayList<>();
    
    /** 厂商或业务自定义扩展字段。 */
    private Map<String, Object> extensions = new HashMap<>(1);
    
    public String getSpecificationType() {
        return specificationType;
    }
    
    public void setSpecificationType(String specificationType) {
        this.specificationType = specificationType;
    }
    
    public EncryptObject getEncryptData() {
        return encryptData;
    }
    
    public void setEncryptData(EncryptObject encryptData) {
        this.encryptData = encryptData;
    }
    
    public List<McpTool> getTools() {
        return tools;
    }
    
    public void setTools(List<McpTool> tools) {
        this.tools = tools;
    }
    
    public Map<String, McpToolMeta> getToolsMeta() {
        return toolsMeta;
    }
    
    public void setToolsMeta(Map<String, McpToolMeta> toolsMeta) {
        this.toolsMeta = toolsMeta;
    }
    
    public List<SecurityScheme> getSecuritySchemes() {
        return securitySchemes;
    }
    
    public void setSecuritySchemes(List<SecurityScheme> securitySchemes) {
        this.securitySchemes = securitySchemes;
    }
    
    public Map<String, Object> getExtensions() {
        return extensions;
    }
    
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }
}
