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

package org.keycloak.models.jpa.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.keycloak.common.util.Time;

import org.hibernate.annotations.Nationalized;

/**
 * OAuth/OIDC 客户端 JPA 实体，映射 CLIENT 表。
 * <p>
 * realm 内 clientId 唯一；关联重定向 URI、Web Origin、协议映射器、
 * 认证流绑定、scope 角色映射及扩展属性。{@link #updateTimestampsOnCreate} /
 * {@link #updateLastModifiedTimestamp} 维护审计时间戳。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Entity
@Table(name="CLIENT", uniqueConstraints = {@UniqueConstraint(columnNames = {"REALM_ID", "CLIENT_ID"})})
@NamedQueries({
        @NamedQuery(name="getClientById", query="select client from ClientEntity client where client.id = :id and client.realmId = :realm"),
        @NamedQuery(name="getAlwaysDisplayInConsoleClients", query="select client.id from ClientEntity client where client.alwaysDisplayInConsole = true and client.realmId = :realm order by client.clientId"),
        @NamedQuery(name="findClientIdByClientId", query="select client.id from ClientEntity client where client.clientId = :clientId and client.realmId = :realm"),
        @NamedQuery(name="findClientByClientId", query="select client from ClientEntity client where client.clientId = :clientId and client.realmId = :realm"),
        @NamedQuery(name="getAllRedirectUrisOfEnabledClients", query="select new map(client as client, r as redirectUri) from ClientEntity client join client.redirectUris r where client.realmId = :realm and client.enabled = true"),
})
public class ClientEntity {

    public static final int ID_MAX_LENGTH = 36;

    /** 内部 UUID 主键；PROPERTY 访问避免关联仅取 id 时额外查实体。 */
    @Id
    @Column(name="ID", length = ID_MAX_LENGTH)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    private String id;
    /** 展示名称。 */
    @Nationalized
    @Column(name = "NAME")
    private String name;
    @Nationalized
    @Column(name = "DESCRIPTION")
    private String description;
    /** 对外 client_id（OAuth client identifier）。 */
    @Column(name = "CLIENT_ID")
    private String clientId;
    /** 是否启用该客户端。 */
    @Column(name="ENABLED")
    private boolean enabled;
    /** 是否在 Account Console 客户端列表中始终展示。 */
    @Column(name = "ALWAYS_DISPLAY_IN_CONSOLE")
    private boolean alwaysDisplayInConsole;
    /** 客户端密钥（confidential client）。 */
    @Column(name="SECRET")
    private String secret;
    /** 动态客户端注册令牌。 */
    @Column(name="REGISTRATION_TOKEN")
    private String registrationToken;
    /** 客户端认证方式（client-secret、JWT 等）。 */
    @Column(name="CLIENT_AUTHENTICATOR_TYPE")
    private String clientAuthenticatorType;
    /** 令牌在此时间戳之前视为无效（realm 级 notBefore 同步）。 */
    @Column(name="NOT_BEFORE")
    private int notBefore;
    /** 公开客户端（无 secret，如 SPA）。 */
    @Column(name="PUBLIC_CLIENT")
    private boolean publicClient;
    /** 协议：openid-connect、saml 等。 */
    @Column(name="PROTOCOL")
    private String protocol;
    /** 是否使用前通道 logout。 */
    @Column(name="FRONTCHANNEL_LOGOUT")
    private boolean frontchannelLogout;
    /** 是否允许请求 full scope（否则受 client scope 限制）。 */
    @Column(name="FULL_SCOPE_ALLOWED")
    private boolean fullScopeAllowed;

    /** 所属 realm ID（非 JPA 关联，便于按 realm 查询）。 */
    @Column(name = "REALM_ID")
    protected String realmId;

    @Column(name = "CREATED_TIMESTAMP")
    private Long createdTimestamp;

    @Column(name = "LAST_MODIFIED_TIMESTAMP")
    private Long lastModifiedTimestamp;

    /** CORS 允许的 Web Origin 集合。 */
    @ElementCollection
    @Column(name="VALUE")
    @CollectionTable(name = "WEB_ORIGINS", joinColumns={ @JoinColumn(name="CLIENT_ID") })
    protected Set<String> webOrigins;

    /** OAuth 重定向 URI 白名单。 */
    @ElementCollection
    @Column(name="VALUE")
    @CollectionTable(name = "REDIRECT_URIS", joinColumns={ @JoinColumn(name="CLIENT_ID") })
    protected Set<String> redirectUris;

    /** 扩展键值属性（CLIENT_ATTRIBUTES 表）。 */
    @OneToMany(cascade ={CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "client")
    protected Collection<ClientAttributeEntity> attributes = new LinkedList<>();

    /** 认证流绑定：binding name → flow ID。 */
    @ElementCollection
    @MapKeyColumn(name="BINDING_NAME")
    @Column(name="FLOW_ID", length = 4000)
    @CollectionTable(name="CLIENT_AUTH_FLOW_BINDINGS", joinColumns={ @JoinColumn(name="CLIENT_ID") })
    protected Map<String, String> authFlowBindings;

    /** OIDC/SAML 协议映射器。 */
    @OneToMany(cascade ={CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "client")
    Collection<ProtocolMapperEntity> protocolMappers = new LinkedList<>();

    @Column(name="SURROGATE_AUTH_REQUIRED")
    private boolean surrogateAuthRequired;

    /** 应用根 URL（用于相对路径解析）。 */
    @Column(name="ROOT_URL")
    private String rootUrl;

    @Column(name="BASE_URL")
    private String baseUrl;

    @Column(name="MANAGEMENT_URL")
    private String managementUrl;

    /** Bearer-only 客户端（资源服务器，不参与 browser flow）。 */
    @Column(name="BEARER_ONLY")
    private boolean bearerOnly;

    /** 是否需要用户 consent。 */
    @Column(name="CONSENT_REQUIRED")
    private boolean consentRequired;

    /** 标准 Authorization Code flow。 */
    @Column(name="STANDARD_FLOW_ENABLED")
    private boolean standardFlowEnabled;

    /** Implicit flow（已弃用但仍可配置）。 */
    @Column(name="IMPLICIT_FLOW_ENABLED")
    private boolean implicitFlowEnabled;

    /** Resource Owner Password Credentials grant。 */
    @Column(name="DIRECT_ACCESS_GRANTS_ENABLED")
    private boolean directAccessGrantsEnabled;

    /** 是否启用 service account（客户端凭证 grant）。 */
    @Column(name="SERVICE_ACCOUNTS_ENABLED")
    private boolean serviceAccountsEnabled;

    /** 集群节点重新注册超时（秒）。 */
    @Column(name="NODE_REREG_TIMEOUT")
    private int nodeReRegistrationTimeout;

    /** 客户端默认 scope 角色映射（SCOPE_MAPPING 表）。 */
    @ElementCollection
    @Column(name="ROLE_ID")
    @CollectionTable(name="SCOPE_MAPPING", joinColumns = { @JoinColumn(name="CLIENT_ID")})
    private Set<String> scopeMappingIds;

    /** 已注册集群节点：host → 最后注册时间戳。 */
    @ElementCollection
    @MapKeyColumn(name="NAME")
    @Column(name="VALUE")
    @CollectionTable(name="CLIENT_NODE_REGISTRATIONS", joinColumns={ @JoinColumn(name="CLIENT_ID") })
    Map<String, Integer> registeredNodes;

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public Long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    /** 首次 persist 时设置 created/lastModified 时间戳。 */
    @PrePersist
    public void updateTimestampsOnCreate() {
        long now = Time.currentTimeMillis();
        if (createdTimestamp == null) {
            createdTimestamp = now;
        }
        if (lastModifiedTimestamp == null) {
            lastModifiedTimestamp = createdTimestamp;
        }
    }

    /** 每次 update 刷新 lastModified。 */
    @PreUpdate
    public void updateLastModifiedTimestamp() {
        lastModifiedTimestamp = Time.currentTimeMillis();
    }

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAlwaysDisplayInConsole() {
        return alwaysDisplayInConsole;
    }

    public void setAlwaysDisplayInConsole(boolean alwaysDisplayInConsole) {
        this.alwaysDisplayInConsole = alwaysDisplayInConsole;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Set<String> getWebOrigins() {
        if (webOrigins == null) {
            webOrigins = new HashSet<>();
        }
        return webOrigins;
    }

    public void setWebOrigins(Set<String> webOrigins) {
        this.webOrigins = webOrigins;
    }

    public Set<String> getRedirectUris() {
        if (redirectUris == null) {
            redirectUris = new HashSet<>();
        }
        return redirectUris;
    }

    public void setRedirectUris(Set<String> redirectUris) {
        this.redirectUris = redirectUris;
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

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public int getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(int notBefore) {
        this.notBefore = notBefore;
    }

    public boolean isPublicClient() {
        return publicClient;
    }

    public void setPublicClient(boolean publicClient) {
        this.publicClient = publicClient;
    }

    public boolean isFullScopeAllowed() {
        return fullScopeAllowed;
    }

    public void setFullScopeAllowed(boolean fullScopeAllowed) {
        this.fullScopeAllowed = fullScopeAllowed;
    }

    public Collection<ClientAttributeEntity> getAttributes() {
        if (attributes == null) {
            attributes = new LinkedList<>();
        }
        return attributes;
    }

    public void setAttributes(Collection<ClientAttributeEntity> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getAuthFlowBindings() {
        if (authFlowBindings == null) {
            authFlowBindings = new HashMap<>();
        }
        return authFlowBindings;
    }

    public void setAuthFlowBindings(Map<String, String> authFlowBindings) {
        this.authFlowBindings = authFlowBindings;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isFrontchannelLogout() {
        return frontchannelLogout;
    }

    public void setFrontchannelLogout(boolean frontchannelLogout) {
        this.frontchannelLogout = frontchannelLogout;
    }

    public Collection<ProtocolMapperEntity> getProtocolMappers() {
        if (protocolMappers == null) {
            protocolMappers = new LinkedList<>();
        }
        return protocolMappers;
    }

    public void setProtocolMappers(Collection<ProtocolMapperEntity> protocolMappers) {
        this.protocolMappers = protocolMappers;
    }

    public boolean isSurrogateAuthRequired() {
        return surrogateAuthRequired;
    }

    public void setSurrogateAuthRequired(boolean surrogateAuthRequired) {
        this.surrogateAuthRequired = surrogateAuthRequired;
    }

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getManagementUrl() {
        return managementUrl;
    }

    public void setManagementUrl(String managementUrl) {
        this.managementUrl = managementUrl;
    }

    public boolean isBearerOnly() {
        return bearerOnly;
    }

    public void setBearerOnly(boolean bearerOnly) {
        this.bearerOnly = bearerOnly;
    }

    public boolean isConsentRequired() {
        return consentRequired;
    }

    public void setConsentRequired(boolean consentRequired) {
        this.consentRequired = consentRequired;
    }

    public boolean isStandardFlowEnabled() {
        return standardFlowEnabled;
    }

    public void setStandardFlowEnabled(boolean standardFlowEnabled) {
        this.standardFlowEnabled = standardFlowEnabled;
    }

    public boolean isImplicitFlowEnabled() {
        return implicitFlowEnabled;
    }

    public void setImplicitFlowEnabled(boolean implicitFlowEnabled) {
        this.implicitFlowEnabled = implicitFlowEnabled;
    }

    public boolean isDirectAccessGrantsEnabled() {
        return directAccessGrantsEnabled;
    }

    public void setDirectAccessGrantsEnabled(boolean directAccessGrantsEnabled) {
        this.directAccessGrantsEnabled = directAccessGrantsEnabled;
    }

    public boolean isServiceAccountsEnabled() {
        return serviceAccountsEnabled;
    }

    public void setServiceAccountsEnabled(boolean serviceAccountsEnabled) {
        this.serviceAccountsEnabled = serviceAccountsEnabled;
    }

    public int getNodeReRegistrationTimeout() {
        return nodeReRegistrationTimeout;
    }

    public void setNodeReRegistrationTimeout(int nodeReRegistrationTimeout) {
        this.nodeReRegistrationTimeout = nodeReRegistrationTimeout;
    }

    public Map<String, Integer> getRegisteredNodes() {
        if (registeredNodes == null) {
            registeredNodes = new HashMap<>();
        }
        return registeredNodes;
    }

    public void setRegisteredNodes(Map<String, Integer> registeredNodes) {
        this.registeredNodes = registeredNodes;
    }

    public Set<String> getScopeMappingIds() {
        if (scopeMappingIds == null) {
            scopeMappingIds = new HashSet<>();
        }
        return scopeMappingIds;
    }

    public void setScopeMapping(Set<String> scopeMappingIds) {
        this.scopeMappingIds = scopeMappingIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof ClientEntity)) return false;

        ClientEntity that = (ClientEntity) o;

        if (!id.equals(that.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
