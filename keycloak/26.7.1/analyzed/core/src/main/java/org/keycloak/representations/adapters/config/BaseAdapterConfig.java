/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.representations.adapters.config;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Keycloak adapter 的公共配置基类，继承 {@link BaseRealmConfig} 并扩展客户端资源、CORS、Bearer 模式等选项。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@JsonPropertyOrder({"realm", "realm-public-key", "auth-server-url", "ssl-required",
        "resource", "public-client", "credentials",
        "use-resource-role-mappings",
        "enable-cors", "cors-max-age", "cors-allowed-methods", "cors-exposed-headers",
        "expose-token", "bearer-only", "autodetect-bearer-only", "enable-basic-auth"})
public class BaseAdapterConfig extends BaseRealmConfig {
    /** 客户端资源名称（client-id）。 */
    @JsonProperty("resource")
    protected String resource;
    /** 是否使用资源级角色映射。 */
    @JsonProperty("use-resource-role-mappings")
    protected boolean useResourceRoleMappings;
    /** 是否启用 CORS 支持。 */
    @JsonProperty("enable-cors")
    protected boolean cors;
    /** CORS 预检缓存最大时长（秒）。 */
    @JsonProperty("cors-max-age")
    protected int corsMaxAge = -1;
    /** CORS 允许的请求头。 */
    @JsonProperty("cors-allowed-headers")
    protected String corsAllowedHeaders;
    /** CORS 允许的 HTTP 方法。 */
    @JsonProperty("cors-allowed-methods")
    protected String corsAllowedMethods;
    /** CORS 暴露的响应头。 */
    @JsonProperty("cors-exposed-headers")
    protected String corsExposedHeaders;
    /** 是否在响应中暴露令牌。 */
    @JsonProperty("expose-token")
    protected boolean exposeToken;
    /** 是否为纯 Bearer 模式（不维护会话）。 */
    @JsonProperty("bearer-only")
    protected boolean bearerOnly;
    /** 是否自动检测 Bearer 令牌请求。 */
    @JsonProperty("autodetect-bearer-only")
    protected boolean autodetectBearerOnly;
    /** 是否启用 HTTP Basic 认证。 */
    @JsonProperty("enable-basic-auth")
    protected boolean enableBasicAuth;
    /** 是否为公开客户端（无 client secret）。 */
    @JsonProperty("public-client")
    protected boolean publicClient;
    /** 客户端凭证（如 secret、JWT 等）。 */
    @JsonProperty("credentials")
    protected Map<String, Object> credentials = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    /** 重定向 URL 重写规则映射。 */
     @JsonProperty("redirect-rewrite-rules")
    protected Map<String, String> redirectRewriteRules;

    public boolean isUseResourceRoleMappings() {
        return useResourceRoleMappings;
    }

    public void setUseResourceRoleMappings(boolean useResourceRoleMappings) {
        this.useResourceRoleMappings = useResourceRoleMappings;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public boolean isCors() {
         return cors;
     }

    public void setCors(boolean cors) {
         this.cors = cors;
     }

    public int getCorsMaxAge() {
         return corsMaxAge;
     }

    public void setCorsMaxAge(int corsMaxAge) {
         this.corsMaxAge = corsMaxAge;
     }

    public String getCorsAllowedHeaders() {
         return corsAllowedHeaders;
     }

    public void setCorsAllowedHeaders(String corsAllowedHeaders) {
         this.corsAllowedHeaders = corsAllowedHeaders;
     }

    public String getCorsAllowedMethods() {
         return corsAllowedMethods;
     }

    public void setCorsAllowedMethods(String corsAllowedMethods) {
         this.corsAllowedMethods = corsAllowedMethods;
     }

    public String getCorsExposedHeaders() {
        return corsExposedHeaders;
    }

    public void setCorsExposedHeaders(String corsExposedHeaders) {
        this.corsExposedHeaders = corsExposedHeaders;
    }

    public boolean isExposeToken() {
         return exposeToken;
     }

    public void setExposeToken(boolean exposeToken) {
         this.exposeToken = exposeToken;
     }

    public boolean isBearerOnly() {
        return bearerOnly;
    }

    public void setBearerOnly(boolean bearerOnly) {
        this.bearerOnly = bearerOnly;
    }

    public boolean isAutodetectBearerOnly() {
        return autodetectBearerOnly;
    }

    public void setAutodetectBearerOnly(boolean autodetectBearerOnly) {
        this.autodetectBearerOnly = autodetectBearerOnly;
    }

    public boolean isEnableBasicAuth() {
        return enableBasicAuth;
    }

    public void setEnableBasicAuth(boolean enableBasicAuth) {
        this.enableBasicAuth = enableBasicAuth;
    }

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, Object> credentials) {
        this.credentials = credentials;
    }

    public boolean isPublicClient() {
        return publicClient;
    }

    public void setPublicClient(boolean publicClient) {
        this.publicClient = publicClient;
    }

    public Map<String, String> getRedirectRewriteRules() {
        return redirectRewriteRules;
    }

    public void setRedirectRewriteRules(Map<String, String> redirectRewriteRules) {
        this.redirectRewriteRules = redirectRewriteRules;
    }
    
    
}
