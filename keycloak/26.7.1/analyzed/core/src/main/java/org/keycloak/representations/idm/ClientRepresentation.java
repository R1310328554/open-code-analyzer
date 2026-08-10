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

package org.keycloak.representations.idm;

import java.util.List;
import java.util.Map;

import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;

/**
 * OAuth/OIDC 客户端的 Admin REST API 表示，涵盖客户端标识、流配置、Scope 与授权设置等。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClientRepresentation {
    /** 客户端内部 UUID。 */
    protected String id;
    /** 客户端标识符（client_id）。 */
    protected String clientId;
    /** 客户端显示名称。 */
    protected String name;
    /** 客户端描述。 */
    protected String description;
    /** 客户端类型（如 OpenID Connect）。 */
    protected String type;
    /** 应用根 URL。 */
    protected String rootUrl;
    /** 管理回调 URL。 */
    protected String adminUrl;
    /** 默认基础 URL。 */
    protected String baseUrl;
    /** 是否需要代理认证。 */
    protected Boolean surrogateAuthRequired;
    /** 客户端是否启用。 */
    protected Boolean enabled;
    /** 是否始终在控制台显示。 */
    protected Boolean alwaysDisplayInConsole;
    /** 客户端认证器类型。 */
    protected String clientAuthenticatorType;
    /** 客户端密钥。 */
    protected String secret;
    /** 动态注册访问令牌。 */
    protected String registrationAccessToken;
    /** 已弃用：默认角色列表。 */
    @Deprecated
    protected String[] defaultRoles;
    /** 合法重定向 URI 列表。 */
    protected List<String> redirectUris;
    /** 允许的 Web 来源（CORS）。 */
    protected List<String> webOrigins;
    /** 令牌生效起始时间（not-before）。 */
    protected Integer notBefore;
    /** 是否为仅 Bearer 客户端。 */
    protected Boolean bearerOnly;
    /** 是否需要用户同意。 */
    protected Boolean consentRequired;
    /** 是否启用标准授权码流。 */
    protected Boolean standardFlowEnabled;
    /** 是否启用隐式流。 */
    protected Boolean implicitFlowEnabled;
    /** 是否启用资源所有者密码凭据流。 */
    protected Boolean directAccessGrantsEnabled;
    /** 是否启用服务账户。 */
    protected Boolean serviceAccountsEnabled;
    /** 是否启用授权服务（UMA）。 */
    protected Boolean authorizationServicesEnabled;
    /** 已弃用：是否仅允许直接授权。 */
    @Deprecated
    protected Boolean directGrantsOnly;
    /** 是否为公开客户端（无密钥）。 */
    protected Boolean publicClient;
    /** 是否启用前端通道登出。 */
    protected Boolean frontchannelLogout;
    /** 协议标识（如 openid-connect）。 */
    protected String protocol;
    /** 客户端自定义属性。 */
    protected Map<String, String> attributes;
    /** 认证流绑定覆盖配置。 */
    protected Map<String, String> authenticationFlowBindingOverrides;
    /** 是否允许完整 Scope。 */
    protected Boolean fullScopeAllowed;
    /** 节点重新注册超时（秒）。 */
    protected Integer nodeReRegistrationTimeout;
    /** 已注册适配器节点及其最后注册时间。 */
    protected Map<String, Integer> registeredNodes;
    /** 协议映射器列表。 */
    protected List<ProtocolMapperRepresentation> protocolMappers;

    /** 已弃用：关联的客户端模板 ID。 */
    @Deprecated
    protected String clientTemplate;
    /** 已弃用：是否继承模板配置。 */
    @Deprecated
    private Boolean useTemplateConfig;
    /** 已弃用：是否继承模板 Scope。 */
    @Deprecated
    private Boolean useTemplateScope;
    /** 已弃用：是否继承模板映射器。 */
    @Deprecated
    private Boolean useTemplateMappers;

    /** 默认客户端 Scope 名称列表。 */
    protected List<String> defaultClientScopes;
    /** 可选客户端 Scope 名称列表。 */
    protected List<String> optionalClientScopes;

    /** UMA 资源服务器授权设置。 */
    private ResourceServerRepresentation authorizationSettings;
    /** 当前用户对客户端的管理权限映射。 */
    private Map<String, Boolean> access;
    /** 加载该客户端的 {@code ClientStorageProvider} ID；本地存储时为 {@code null}。 */
    protected String origin;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isAlwaysDisplayInConsole() {
        return alwaysDisplayInConsole;
    }

    public void setAlwaysDisplayInConsole(Boolean alwaysDisplayInConsole) {
        this.alwaysDisplayInConsole = alwaysDisplayInConsole;
    }

    public Boolean isSurrogateAuthRequired() {
        return surrogateAuthRequired;
    }

    public void setSurrogateAuthRequired(Boolean surrogateAuthRequired) {
        this.surrogateAuthRequired = surrogateAuthRequired;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getClientAuthenticatorType() {
        return clientAuthenticatorType;
    }

    public void setClientAuthenticatorType(String clientAuthenticatorType) {
        this.clientAuthenticatorType = clientAuthenticatorType;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getRegistrationAccessToken() {
        return registrationAccessToken;
    }

    public void setRegistrationAccessToken(String registrationAccessToken) {
        this.registrationAccessToken = registrationAccessToken;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public List<String> getWebOrigins() {
        return webOrigins;
    }

    public void setWebOrigins(List<String> webOrigins) {
        this.webOrigins = webOrigins;
    }

    @Deprecated
    public String[] getDefaultRoles() {
        return defaultRoles;
    }

    @Deprecated
    public void setDefaultRoles(String[] defaultRoles) {
        this.defaultRoles = defaultRoles;
    }

    public Integer getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Integer notBefore) {
        this.notBefore = notBefore;
    }

    public Boolean isBearerOnly() {
        return bearerOnly;
    }

    public void setBearerOnly(Boolean bearerOnly) {
        this.bearerOnly = bearerOnly;
    }

    public Boolean isConsentRequired() {
        return consentRequired;
    }

    public void setConsentRequired(Boolean consentRequired) {
        this.consentRequired = consentRequired;
    }

    public Boolean isStandardFlowEnabled() {
        return standardFlowEnabled;
    }

    public void setStandardFlowEnabled(Boolean standardFlowEnabled) {
        this.standardFlowEnabled = standardFlowEnabled;
    }

    public Boolean isImplicitFlowEnabled() {
        return implicitFlowEnabled;
    }

    public void setImplicitFlowEnabled(Boolean implicitFlowEnabled) {
        this.implicitFlowEnabled = implicitFlowEnabled;
    }

    public Boolean isDirectAccessGrantsEnabled() {
        return directAccessGrantsEnabled;
    }

    public void setDirectAccessGrantsEnabled(Boolean directAccessGrantsEnabled) {
        this.directAccessGrantsEnabled = directAccessGrantsEnabled;
    }

    public Boolean isServiceAccountsEnabled() {
        return serviceAccountsEnabled;
    }

    public void setServiceAccountsEnabled(Boolean serviceAccountsEnabled) {
        this.serviceAccountsEnabled = serviceAccountsEnabled;
    }

    /** 若已配置 authorizationSettings 则视为启用授权服务。 */
    public Boolean getAuthorizationServicesEnabled() {
        if (authorizationSettings != null) {
            return true;
        }
        return authorizationServicesEnabled;
    }

    public void setAuthorizationServicesEnabled(Boolean authorizationServicesEnabled) {
        this.authorizationServicesEnabled = authorizationServicesEnabled;
    }

    @Deprecated
    public Boolean isDirectGrantsOnly() {
        return directGrantsOnly;
    }

    public void setDirectGrantsOnly(Boolean directGrantsOnly) {
        this.directGrantsOnly = directGrantsOnly;
    }

    public Boolean isPublicClient() {
        return publicClient;
    }

    public void setPublicClient(Boolean publicClient) {
        this.publicClient = publicClient;
    }

    public Boolean isFullScopeAllowed() {
        return fullScopeAllowed;
    }

    public void setFullScopeAllowed(Boolean fullScopeAllowed) {
        this.fullScopeAllowed = fullScopeAllowed;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getAuthenticationFlowBindingOverrides() {
        return authenticationFlowBindingOverrides;
    }

    public void setAuthenticationFlowBindingOverrides(Map<String, String> authenticationFlowBindingOverrides) {
        this.authenticationFlowBindingOverrides = authenticationFlowBindingOverrides;
    }

    public Integer getNodeReRegistrationTimeout() {
        return nodeReRegistrationTimeout;
    }

    public void setNodeReRegistrationTimeout(Integer nodeReRegistrationTimeout) {
        this.nodeReRegistrationTimeout = nodeReRegistrationTimeout;
    }

    public Map<String, Integer> getRegisteredNodes() {
        return registeredNodes;
    }

    public void setRegisteredNodes(Map<String, Integer> registeredNodes) {
        this.registeredNodes = registeredNodes;
    }

    public Boolean isFrontchannelLogout() {
        return frontchannelLogout;
    }

    public void setFrontchannelLogout(Boolean frontchannelLogout) {
        this.frontchannelLogout = frontchannelLogout;
    }

    public List<ProtocolMapperRepresentation> getProtocolMappers() {
        return protocolMappers;
    }

    public void setProtocolMappers(List<ProtocolMapperRepresentation> protocolMappers) {
        this.protocolMappers = protocolMappers;
    }

    @Deprecated
    public String getClientTemplate() {
        return clientTemplate;
    }

    @Deprecated
    public Boolean isUseTemplateConfig() {
        return useTemplateConfig;
    }

    @Deprecated
    public Boolean isUseTemplateScope() {
        return useTemplateScope;
    }

    @Deprecated
    public Boolean isUseTemplateMappers() {
        return useTemplateMappers;
    }

    public List<String> getDefaultClientScopes() {
        return defaultClientScopes;
    }

    public void setDefaultClientScopes(List<String> defaultClientScopes) {
        this.defaultClientScopes = defaultClientScopes;
    }

    public List<String> getOptionalClientScopes() {
        return optionalClientScopes;
    }

    public void setOptionalClientScopes(List<String> optionalClientScopes) {
        this.optionalClientScopes = optionalClientScopes;
    }

    public ResourceServerRepresentation getAuthorizationSettings() {
        return authorizationSettings;
    }

    public void setAuthorizationSettings(ResourceServerRepresentation authorizationSettings) {
        this.authorizationSettings = authorizationSettings;
    }

    public Map<String, Boolean> getAccess() {
        return access;
    }

    public void setAccess(Map<String, Boolean> access) {
        this.access = access;
    }


    /**
     * 返回加载该客户端的 ClientStorageProvider ID。
     *
     * @return 本地存储时返回 {@code null}
     */
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

}
