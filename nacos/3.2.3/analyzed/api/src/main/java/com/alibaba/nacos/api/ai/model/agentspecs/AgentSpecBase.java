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

package com.alibaba.nacos.api.ai.model.agentspecs;

/**
 * AgentSpec 相关模型的基类，封装命名空间、名称与描述等公共基础字段。
 *
 * <p>被 {@link AgentSpecBasicInfo} 等子类继承，避免重复定义治理元数据。</p>
 *
 * @author nacos
 */
public class AgentSpecBase {
    
    /** Nacos 命名空间 ID。 */
    private String namespaceId;
    
    /** AgentSpec 名称。 */
    private String name;
    
    /** AgentSpec 描述。 */
    private String description;
    
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
}
