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

package org.keycloak.models.cache.infinispan.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;

/**
 * OAuth/OIDC 客户端（Client）的 Infinispan 缓存快照实体。
 * <p>
 * 继承 {@link AbstractCachedClientScope}，缓存客户端 ID、密钥、协议、流程开关、
 * 重定向 URI、角色映射与节点注册等完整配置字段。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CachedClient extends AbstractCachedClientScope<ClientModel> {
    /** 客户端标识符（clientId）。 */
    protected String clientId;
    /** 客户端显示名称。 */
    protected String name;
    /** 客户端描述。 */
    protected String description;
    /** 所属领域 ID。 */
    protected String realm;
    /** 合法重定向 URI 集合。 */
    protected Set<String> redirectUris = new HashSet<>();
    /** 客户端是否启用。 */
    protected boolean enabled;
    /** 是否始终在控制台显示。 */
    protected boolean alwaysDisplayInConsole;
    /** 客户端认证器类型。 */
    protected String clientAuthenticatorType;
    /** 客户端密钥。 */
    protected String secret;
    /** 注册令牌。 */
    protected String registrationToken;
    /** 使用的协议（如 openid-connect）。 */
    protected String protocol;
    /** 自定义属性键值对。 */
    protected Map<String, String> attributes = new HashMap<>();
    /** 认证流程绑定覆盖映射。 */
    protected Map<String, String> authFlowBindings = new HashMap<>();
    /** 是否为公开客户端（无密钥）。 */
    protected boolean publicClient;
    /** 是否允许全作用域访问。 */
    protected boolean fullScopeAllowed;
    /** 是否启用前端通道登出。 */
    protected boolean frontchannelLogout;
    /** notBefore 时间戳，用于令牌失效控制。 */
    protected int notBefore;
    /** 映射的角色 ID 集合。 */
    protected Set<String> scope = new HashSet<>();
    /** 允许的 Web Origins 集合。 */
    protected Set<String> webOrigins = new HashSet<>();
    /** 是否需要代理认证。 */
    protected boolean surrogateAuthRequired;
    /** 管理 URL。 */
    protected String managementUrl;
    /** 根 URL。 */
    protected String rootUrl;
    /** 基础 URL。 */
    protected String baseUrl;
    /** 是否为 Bearer-Only 客户端。 */
    protected boolean bearerOnly;
    /** 是否需要用户同意。 */
    protected boolean consentRequired;
    /** 是否启用标准授权码流程。 */
    protected boolean standardFlowEnabled;
    /** 是否启用隐式流程。 */
    protected boolean implicitFlowEnabled;
    /** 是否启用直接访问授权（密码模式）。 */
    protected boolean directAccessGrantsEnabled;
    /** 是否启用服务账户。 */
    protected boolean serviceAccountsEnabled;
    /** 节点重新注册超时（秒）。 */
    protected int nodeReRegistrationTimeout;
    /** 已注册节点及其最后访问时间映射。 */
    protected Map<String, Integer> registeredNodes;
    /** 客户端创建时间戳。 */
    protected Long createdTimestamp;
    /** 客户端最后修改时间戳。 */
    protected Long lastModifiedTimestamp;

    /** 从客户端模型构造缓存快照。 */
    public CachedClient(long revision, RealmModel realm, ClientModel model) {
        super(revision, model);
        clientAuthenticatorType = model.getClientAuthenticatorType();
        secret = model.getSecret();
        registrationToken = model.getRegistrationToken();
        clientId = model.getClientId();
        name = model.getName();
        description = model.getDescription();
        this.realm = realm.getId();
        enabled = model.isEnabled();
        alwaysDisplayInConsole = model.isAlwaysDisplayInConsole();
        protocol = model.getProtocol();
        attributes.putAll(model.getAttributes());
        authFlowBindings.putAll(model.getAuthenticationFlowBindingOverrides());
        notBefore = model.getNotBefore();
        frontchannelLogout = model.isFrontchannelLogout();
        publicClient = model.isPublicClient();
        fullScopeAllowed = model.isFullScopeAllowed();
        redirectUris.addAll(model.getRedirectUris());
        webOrigins.addAll(model.getWebOrigins());
        scope.addAll(model.getScopeMappingsStream().map(RoleModel::getId).collect(Collectors.toSet()));
        surrogateAuthRequired = model.isSurrogateAuthRequired();
        managementUrl = model.getManagementUrl();
        rootUrl = model.getRootUrl();
        baseUrl = model.getBaseUrl();
        bearerOnly = model.isBearerOnly();
        consentRequired = model.isConsentRequired();
        standardFlowEnabled = model.isStandardFlowEnabled();
        implicitFlowEnabled = model.isImplicitFlowEnabled();
        directAccessGrantsEnabled = model.isDirectAccessGrantsEnabled();
        serviceAccountsEnabled = model.isServiceAccountsEnabled();

        nodeReRegistrationTimeout = model.getNodeReRegistrationTimeout();
        registeredNodes = new TreeMap<>(model.getRegisteredNodes());
        createdTimestamp = model.getCreatedTimestamp();
        lastModifiedTimestamp = model.getLastModifiedTimestamp();
    }

    /** 返回客户端标识符。 */
    public String getClientId() {
        return clientId;
    }

    /** 返回客户端显示名称。 */
    public String getName() {
        return name;
    }

    /** 返回客户端描述。 */
    public String getDescription() { return description; }

    /** 设置客户端描述。 */
    public void setDescription(String description) { this.description = description; }

    /** 返回所属领域 ID。 */
    public String getRealm() {
        return realm;
    }

    public Set<String> getRedirectUris() {
        return redirectUris;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAlwaysDisplayInConsole() {
        return alwaysDisplayInConsole;
    }

    public String getClientAuthenticatorType() {
        return clientAuthenticatorType;
    }

    public String getSecret() {
        return secret;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public boolean isPublicClient() {
        return publicClient;
    }

    public int getNotBefore() {
        return notBefore;
    }

    public Set<String> getScope() {
        return scope;
    }

    public Set<String> getWebOrigins() {
        return webOrigins;
    }

    public boolean isFullScopeAllowed() {
        return fullScopeAllowed;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public boolean isFrontchannelLogout() {
        return frontchannelLogout;
    }

    public boolean isSurrogateAuthRequired() {
        return surrogateAuthRequired;
    }

    public String getManagementUrl() {
        return managementUrl;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public boolean isBearerOnly() {
        return bearerOnly;
    }

    public boolean isConsentRequired() {
        return consentRequired;
    }

    public boolean isStandardFlowEnabled() {
        return standardFlowEnabled;
    }

    public boolean isImplicitFlowEnabled() {
        return implicitFlowEnabled;
    }

    public boolean isDirectAccessGrantsEnabled() {
        return directAccessGrantsEnabled;
    }

    public boolean isServiceAccountsEnabled() {
        return serviceAccountsEnabled;
    }

    public int getNodeReRegistrationTimeout() {
        return nodeReRegistrationTimeout;
    }

    public Map<String, Integer> getRegisteredNodes() {
        return registeredNodes;
    }

    public Map<String, String> getAuthFlowBindings() {
        return authFlowBindings;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public Long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }
}
