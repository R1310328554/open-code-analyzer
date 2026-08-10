/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.oidc.authorization;

/**
 * 发往 IdP 授权端点的请求模型。
 *
 * <p>包含用户令牌、资源 URI 组件（类型/命名空间/分组/名称）及操作类型，
 * 可序列化为 JSON 请求体。</p>
 *
 * @author WangzJi
 */
public class AuthorizationRequest {
    
    /**
     * 用户访问令牌。
     */
    private String token;
    
    /**
     * 资源标识符（如 {@code nacos:config:dev:app.yaml}）。
     */
    private String resource;
    
    /**
     * 操作类型（如 read、write）。
     */
    private String action;
    
    /**
     * 资源类型（如 config、naming）。
     */
    private String resourceType;
    
    /**
     * 命名空间 ID。
     */
    private String namespace;
    
    /**
     * 分组名称。
     */
    private String group;
    
    /**
     * 资源名称（配置场景下通常为 dataId）。
     */
    private String resourceName;
    
    public AuthorizationRequest() {
    }
    
    public AuthorizationRequest(String token, String resource, String action) {
        this.token = token;
        this.resource = resource;
        this.action = action;
    }
    
    /**
     * 根据各组件拼接资源 URI。
     *
     * <p>格式：{@code nacos:{type}:{namespace}:{group}:{name}}；若已设置 resource 字段则直接返回。</p>
     *
     * @return 资源 URI 字符串
     */
    public String buildResourceUri() {
        if (resource != null) {
            return resource;
        }
        StringBuilder uri = new StringBuilder("nacos");
        if (resourceType != null) {
            uri.append(":").append(resourceType);
        }
        if (namespace != null) {
            uri.append(":").append(namespace);
        }
        if (group != null) {
            uri.append(":").append(group);
        }
        if (resourceName != null) {
            uri.append(":").append(resourceName);
        }
        return uri.toString();
    }
    
    /**
     * 序列化为 JSON 字符串，作为 HTTP 请求体发送给 IdP。
     *
     * @return JSON 字符串
     */
    public String toJson() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"token\":\"").append(escapeJson(token)).append("\"");
        json.append(",\"resource\":\"").append(escapeJson(buildResourceUri())).append("\"");
        json.append(",\"action\":\"").append(escapeJson(action)).append("\"");
        if (resourceType != null) {
            json.append(",\"resourceType\":\"").append(escapeJson(resourceType)).append("\"");
        }
        if (namespace != null) {
            json.append(",\"namespace\":\"").append(escapeJson(namespace)).append("\"");
        }
        if (group != null) {
            json.append(",\"group\":\"").append(escapeJson(group)).append("\"");
        }
        if (resourceName != null) {
            json.append(",\"resourceName\":\"").append(escapeJson(resourceName)).append("\"");
        }
        json.append("}");
        return json.toString();
    }
    
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
    
    // Getter 与 Setter
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getResource() {
        return resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    /**
     * {@link AuthorizationRequest} 的流式构建器。
     */
    public static class Builder {
        
        private final AuthorizationRequest request = new AuthorizationRequest();
        
        public Builder token(String token) {
            request.setToken(token);
            return this;
        }
        
        public Builder resource(String resource) {
            request.setResource(resource);
            return this;
        }
        
        public Builder action(String action) {
            request.setAction(action);
            return this;
        }
        
        public Builder resourceType(String resourceType) {
            request.setResourceType(resourceType);
            return this;
        }
        
        public Builder namespace(String namespace) {
            request.setNamespace(namespace);
            return this;
        }
        
        public Builder group(String group) {
            request.setGroup(group);
            return this;
        }
        
        public Builder resourceName(String resourceName) {
            request.setResourceName(resourceName);
            return this;
        }
        
        public AuthorizationRequest build() {
            return request;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
