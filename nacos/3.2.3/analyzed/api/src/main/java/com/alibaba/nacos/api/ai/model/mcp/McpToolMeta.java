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

import java.util.Map;

/**
 * MCP 工具元数据，承载调用上下文、启用开关与模板等治理信息。
 *
 * <p>以工具名为键存储于 {@link McpToolSpecification#getToolsMeta()} 映射中，
 * 供服务端在工具列表与调用链路中附加 Nacos 侧扩展配置。</p>
 *
 * @author xiweng.yy
 */
public class McpToolMeta {
    
    /** 工具调用时注入的上下文键值对（如租户、链路 ID 等）。 */
    private Map<String, String> invokeContext;
    
    /** 是否启用该工具，默认 true。 */
    private boolean enabled = true;
    
    /** 与工具关联的模板配置（如请求体、响应映射模板）。 */
    private Map<String, Object> templates;
    
    public Map<String, String> getInvokeContext() {
        return invokeContext;
    }
    
    public void setInvokeContext(Map<String, String> invokeContext) {
        this.invokeContext = invokeContext;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Map<String, Object> getTemplates() {
        return templates;
    }
    
    public void setTemplates(Map<String, Object> templates) {
        this.templates = templates;
    }
}
