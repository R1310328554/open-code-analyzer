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
 *
 */

package com.alibaba.nacos.api.ai.model.a2a;

import java.util.Map;
import java.util.Objects;

/**
 * Agent 扩展声明模型，描述 Agent 支持的自定义扩展 URI 及其参数。
 *
 * <p>用于 A2A Agent Card 中声明可选或必需的扩展能力，
 * 客户端可据此发现额外协议或插件接口。</p>
 *
 * @author KiteSoar
 */
public class AgentExtension {
    
    /** 扩展 URI 标识。 */
    private String uri;
    
    /** 扩展功能描述。 */
    private String description;
    
    /** 客户端是否必须支持该扩展。 */
    private Boolean required;
    
    /** 扩展附加参数键值对。 */
    private Map<String, Object> params;
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getRequired() {
        return required;
    }
    
    public void setRequired(Boolean required) {
        this.required = required;
    }
    
    public Map<String, Object> getParams() {
        return params;
    }
    
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AgentExtension that = (AgentExtension) o;
        return Objects.equals(uri, that.uri) && Objects.equals(description, that.description)
            && Objects.equals(
                required, that.required)
            && Objects.equals(params, that.params);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(uri, description, required, params);
    }
}
