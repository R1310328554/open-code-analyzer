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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * MCP 工具定义模型，描述单个可调用工具的名称、入参/出参 Schema 及扩展元数据。
 *
 * <p>对应 MCP 协议 tools/list 与 tools/call 中的工具条目，
 * 可携带 {@link McpToolAnnotations} 提示客户端工具行为特征。</p>
 *
 * @author xiweng.yy
 */
public class McpTool {
    
    /** 工具唯一名称，用于 tools/call 请求标识。 */
    private String name;
    
    /** 工具的人类可读描述。 */
    private String description;
    
    /** JSON Schema 形式的入参结构定义。 */
    private Map<String, Object> inputSchema;
    
    /** JSON Schema 形式的出参结构定义（可选）。 */
    private Map<String, Object> outputSchema;
    
    /**
     * MCP 协议 `_meta` 扩展字段，用法见 MCP 规范。
     */
    @JsonProperty("_meta")
    private Map<String, Object> meta;
    
    /**
     * 工具注解，向客户端补充描述工具行为特征的提示信息。
     */
    private McpToolAnnotations annotations;
    
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
    
    public Map<String, Object> getInputSchema() {
        return inputSchema;
    }
    
    public void setInputSchema(Map<String, Object> inputSchema) {
        this.inputSchema = inputSchema;
    }
    
    public Map<String, Object> getOutputSchema() {
        return outputSchema;
    }
    
    public void setOutputSchema(Map<String, Object> outputSchema) {
        this.outputSchema = outputSchema;
    }
    
    public Map<String, Object> getMeta() {
        return meta;
    }
    
    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }
    
    public McpToolAnnotations getAnnotations() {
        return annotations;
    }
    
    public void setAnnotations(McpToolAnnotations annotations) {
        this.annotations = annotations;
    }
    
}
