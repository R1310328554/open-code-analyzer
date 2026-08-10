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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * MCP 资源规范模型，描述 Server 暴露的资源列表与模板。
 *
 * <p>支持明文与加密两种存储模式；加密模式下服务端原样持久化
 * {@link #encryptData}，跳过 resources/resourceTemplates 解析。</p>
 *
 * @author xiweng.yy
 */
public class McpResourceSpecification {
    
    /**
     * 资源规范存储类型，默认 {@code normal}（明文存储）。
     * 设为 {@code encrypted}（或 {@code encrypt-kms} 等厂商扩展值）时，
     * 服务端原样持久化 {@link #encryptData}，不再解析 resources/resourceTemplates。
     */
    private String specificationType;
    
    /** 加密模式下的载荷与元数据，类型为 {@link EncryptObject}。 */
    private EncryptObject encryptData;
    
    /** MCP 资源定义列表（明文模式）。 */
    private List<Map<String, Object>> resources = new LinkedList<>();
    
    /** MCP 资源模板列表（明文模式）。 */
    private List<Map<String, Object>> resourceTemplates = new LinkedList<>();
    
    /** 扩展字段键值对。 */
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
    
    public List<Map<String, Object>> getResources() {
        return resources;
    }
    
    public void setResources(List<Map<String, Object>> resources) {
        this.resources = resources;
    }
    
    public List<Map<String, Object>> getResourceTemplates() {
        return resourceTemplates;
    }
    
    public void setResourceTemplates(List<Map<String, Object>> resourceTemplates) {
        this.resourceTemplates = resourceTemplates;
    }
    
    public Map<String, Object> getExtensions() {
        return extensions;
    }
    
    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }
}
