/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.representations.adapters.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Keycloak Policy Enforcer（策略执行器）的配置表示，定义路径级授权规则、执行模式与缓存策略。
 * <p>
 * 供 Java adapter 在应用层拦截请求并执行 UMA/Authorization 策略。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PolicyEnforcerConfig {

    /** 全局执行模式，默认 ENFORCING。 */
    @JsonProperty("enforcement-mode")
    private EnforcementMode enforcementMode = EnforcementMode.ENFORCING;

    /** 路径级授权配置列表。 */
    @JsonProperty("paths")
    @JsonInclude(Include.NON_EMPTY)
    private List<PathConfig> paths = new ArrayList<>();

    /** 路径匹配缓存配置。 */
    @JsonProperty("path-cache")
    @JsonInclude(Include.NON_EMPTY)
    private PathCacheConfig pathCacheConfig;

    /** 是否延迟加载路径配置。 */
    @JsonProperty("lazy-load-paths")
    private Boolean lazyLoadPaths = Boolean.FALSE;

    /** 拒绝访问时的重定向 URL。 */
    @JsonProperty("on-deny-redirect-to")
    @JsonInclude(Include.NON_NULL)
    private String onDenyRedirectTo;

    /** 用户托管访问（UMA）配置。 */
    @JsonProperty("user-managed-access")
    @JsonInclude(Include.NON_NULL)
    private UserManagedAccessConfig userManagedAccess;

    /** 声明信息点（Claim Information Point）配置。 */
    @JsonProperty("claim-information-point")
    @JsonInclude(Include.NON_NULL)
    private Map<String, Map<String, Object>> claimInformationPointConfig;

    /** 是否将 HTTP 方法映射为 scope。 */
    @JsonProperty("http-method-as-scope")
    private Boolean httpMethodAsScope;

    /** Realm 名称。 */
    private String realm;

    /** 认证服务器 URL。 */
    @JsonProperty("auth-server-url")
    private String authServerUrl;

    /** 客户端凭证。 */
    @JsonProperty("credentials")
    protected Map<String, Object> credentials = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /** 客户端资源名称。 */
    @JsonProperty("resource")
    private String resource;

    public List<PathConfig> getPaths() {
        return this.paths;
    }

    public PathCacheConfig getPathCacheConfig() {
        return pathCacheConfig;
    }

    public Boolean getLazyLoadPaths() {
        return lazyLoadPaths;
    }

    public void setLazyLoadPaths(Boolean lazyLoadPaths) {
        this.lazyLoadPaths = lazyLoadPaths;
    }

    public EnforcementMode getEnforcementMode() {
        return this.enforcementMode;
    }

    public void setEnforcementMode(EnforcementMode enforcementMode) {
        this.enforcementMode = enforcementMode;
    }

    public UserManagedAccessConfig getUserManagedAccess() {
        return this.userManagedAccess;
    }

    public void setPaths(List<PathConfig> paths) {
        this.paths = paths;
    }

    public void setPathCacheConfig(PathCacheConfig pathCacheConfig) {
        this.pathCacheConfig = pathCacheConfig;
    }

    public String getOnDenyRedirectTo() {
        return onDenyRedirectTo;
    }

    public void setUserManagedAccess(UserManagedAccessConfig userManagedAccess) {
        this.userManagedAccess = userManagedAccess;
    }

    public void setOnDenyRedirectTo(String onDenyRedirectTo) {
        this.onDenyRedirectTo = onDenyRedirectTo;
    }

    public Map<String, Map<String, Object>> getClaimInformationPointConfig() {
        return claimInformationPointConfig;
    }

    public void setClaimInformationPointConfig(Map<String, Map<String, Object>> config) {
        this.claimInformationPointConfig = config;
    }

    public Boolean getHttpMethodAsScope() {
        return httpMethodAsScope;
    }

    public void setHttpMethodAsScope(Boolean httpMethodAsScope) {
        this.httpMethodAsScope = httpMethodAsScope;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public void setAuthServerUrl(String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, Object> credentials) {
        this.credentials = credentials;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * 单条路径的授权配置，可包含 HTTP 方法、scope 及执行模式。
     */
    public static class PathConfig {

        /**
         * 从 {@link ResourceRepresentation} 批量创建路径配置。
         *
         * @param resourceDescription 授权资源描述
         * @return 路径配置集合
         */
        public static Set<PathConfig> createPathConfigs(ResourceRepresentation resourceDescription) {
            Set<PathConfig> pathConfigs = new HashSet<>();

            for (String uri : resourceDescription.getUris()) {

                PathConfig pathConfig = new PathConfig();

                pathConfig.setId(resourceDescription.getId());
                pathConfig.setName(resourceDescription.getName());

                if (uri == null || "".equals(uri.trim())) {
                    throw new RuntimeException("Failed to configure paths. Resource [" + resourceDescription.getName() + "] has an invalid or empty URI [" + uri + "].");
                }

                pathConfig.setPath(uri);

                List<String> scopeNames = new ArrayList<>();

                for (ScopeRepresentation scope : resourceDescription.getScopes()) {
                    scopeNames.add(scope.getName());
                }

                pathConfig.setScopes(scopeNames);
                pathConfig.setType(resourceDescription.getType());

                pathConfigs.add(pathConfig);
            }

            return pathConfigs;
        }

        /** 资源名称。 */
        private String name;
        /** 资源类型。 */
        private String type;
        /** 路径模式（可含 {@code {param}} 占位符）。 */
        private String path;
        /** HTTP 方法级配置列表。 */
        private List<MethodConfig> methods = new ArrayList<>();
        /** 所需 scope 列表。 */
        private List<String> scopes = new ArrayList<>();
        /** 资源 ID。 */
        private String id;

        /** 该路径的执行模式。 */
        @JsonProperty("enforcement-mode")
        private EnforcementMode enforcementMode = EnforcementMode.ENFORCING;

        /** 路径级声明信息点配置。 */
        @JsonProperty("claim-information-point")
        private Map<String, Map<String, Object>> claimInformationPointConfig;

        /** 父路径配置（实例化路径时引用）。 */
        @JsonIgnore
        private PathConfig parentConfig;

        /** 是否已失效（需重新加载）。 */
        private boolean invalidated;

        /** 是否为静态路径（非模板实例）。 */
        private boolean staticPath;

        public String getPath() {
            return this.path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public List<String> getScopes() {
            return this.scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }

        public List<MethodConfig> getMethods() {
            return methods;
        }

        public void setMethods(List<MethodConfig> methods) {
            this.methods = methods;
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

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public EnforcementMode getEnforcementMode() {
            return enforcementMode;
        }

        public void setEnforcementMode(EnforcementMode enforcementMode) {
            this.enforcementMode = enforcementMode;
        }

        public Map<String, Map<String, Object>> getClaimInformationPointConfig() {
            return claimInformationPointConfig;
        }

        public void setClaimInformationPointConfig(Map<String, Map<String, Object>> claimInformationPointConfig) {
            this.claimInformationPointConfig = claimInformationPointConfig;
        }

        @Override
        public String toString() {
            return "PathConfig{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", path='" + path + '\'' +
                    ", scopes=" + scopes +
                    ", id='" + id + '\'' +
                    ", enforcerMode='" + enforcementMode + '\'' +
                    '}';
        }

        /** 路径是否包含 {@code {}} 模板占位符。 */
        @JsonIgnore
        public boolean hasPattern() {
            return getPath().indexOf("{") != -1;
        }

        /** 是否为父路径的实例化副本。 */
        @JsonIgnore
        public boolean isInstance() {
            return this.parentConfig != null;
        }

        public void setParentConfig(PathConfig parentConfig) {
            this.parentConfig = parentConfig;
        }

        public PathConfig getParentConfig() {
            return parentConfig;
        }

        /** 标记该路径配置已失效。 */
        public void invalidate() {
            this.invalidated = true;
        }

        public boolean isInvalidated() {
            return invalidated;
        }

        public boolean isStatic() {
            return staticPath;
        }

        public void setStatic(boolean staticPath) {
            this.staticPath = staticPath;
        }
    }

    /**
     * HTTP 方法级 scope 授权配置。
     */
    public static class MethodConfig {

        /** HTTP 方法名（如 GET、POST）。 */
        private String method;
        /** 所需 scope 列表。 */
        private List<String> scopes = new ArrayList<>();

        /** scope 执行模式，默认 ALL（需满足全部 scope）。 */
        @JsonProperty("scopes-enforcement-mode")
        private ScopeEnforcementMode scopesEnforcementMode = ScopeEnforcementMode.ALL;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<String> getScopes() {
            return scopes;
        }

        public void setScopes(List<String> scopes) {
            this.scopes = scopes;
        }

        public void setScopesEnforcementMode(ScopeEnforcementMode scopesEnforcementMode) {
            this.scopesEnforcementMode = scopesEnforcementMode;
        }

        public ScopeEnforcementMode getScopesEnforcementMode() {
            return scopesEnforcementMode;
        }
    }

    /** 路径匹配结果缓存配置。 */
    public static class PathCacheConfig {

        /** 最大缓存条目数，默认 1000。 */
        @JsonProperty("max-entries")
        int maxEntries = 1000;
        /** 缓存条目存活时间（毫秒），默认 30000。 */
        @JsonProperty("lifespan")
        long lifespan = 30000;

        public int getMaxEntries() {
            return maxEntries;
        }

        public void setMaxEntries(int maxEntries) {
            this.maxEntries = maxEntries;
        }

        public long getLifespan() {
            return lifespan;
        }

        public void setLifespan(long lifespan) {
            this.lifespan = lifespan;
        }
    }

    /** 策略执行模式枚举。 */
    public enum EnforcementMode {
        /** 宽松模式：未匹配路径允许访问。 */
        PERMISSIVE,
        /** 强制模式：未匹配路径拒绝访问。 */
        ENFORCING,
        /** 禁用策略执行。 */
        DISABLED
    }

    /** scope 校验模式枚举。 */
    public enum ScopeEnforcementMode {
        /** 需持有全部 scope。 */
        ALL,
        /** 持有任一 scope 即可。 */
        ANY,
        /** 不校验 scope。 */
        DISABLED
    }

    /** 用户托管访问（UMA）占位配置类。 */
    public static class UserManagedAccessConfig {

    }
}
