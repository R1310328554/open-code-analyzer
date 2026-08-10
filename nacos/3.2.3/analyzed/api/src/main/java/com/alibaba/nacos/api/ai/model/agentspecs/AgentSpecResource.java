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
 * Worker 包内单个资源文件的结构，ZIP 中除 manifest.json 外的每个条目对应一项。
 *
 * <p>包含资源路径、类型、内容及可选元数据，二进制文件以 Base64 编码存储。</p>
 *
 * @author nacos
 */
public class AgentSpecResource {
    
    /** 资源名称（含路径，如 config/SOUL.md）。 */
    
    private String name;
    
    /** 资源类型：config、skill、cron、dockerfile、other 等。 */
    
    private String type;
    
    /** 资源内容（文本直接存储，二进制文件 Base64 编码）。 */
    
    private String content;
    
    /** 可选的资源元数据键值对。 */
    
    private Map<String, Object> metadata;
    
    /**
     * 获取资源唯一标识：type 非空时为 "type::name"，否则为 "name"。
     * 分隔符 "::" 不在 type/name 允许字符集中，可避免歧义。
     *
     * @return resource unique identifier
     */
    public String getResourceIdentifier() {
        if (type != null && !type.trim().isEmpty()) {
            return type + "::" + name;
        }
        return name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
