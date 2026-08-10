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

/**
 * McpServerIndexData.
 * <p>MCP 服务索引轻量数据模型，仅持有 MCP ID 与命名空间 ID，用于缓存与快速路由，不含完整服务详情。</p>
 * 
 * @author xinluo
 */
public class McpServerIndexData {
    
    /** MCP 服务唯一 ID。 */
    private String id;
    
    /** 所属命名空间 ID。 */
    private String namespaceId;
    
    /**
     * Factory method for index data.
     * <p>工厂方法，快速构造索引数据实例。</p>
     * @param id server id
     * @param namespaceId namespaceId
     * @return index
     */
    public static McpServerIndexData newIndexData(String id, String namespaceId) {
        McpServerIndexData data = new McpServerIndexData();
        data.setNamespaceId(namespaceId);
        data.setId(id);
        return data;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNamespaceId() {
        return namespaceId;
    }
    
    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }
}
