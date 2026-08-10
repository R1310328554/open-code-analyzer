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

package org.keycloak.models;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.keycloak.common.util.ObjectUtil;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventManager;

/**
 * 客户端模型：OIDC/SAML 客户端的完整配置与作用域、角色、协议映射等。
 * <p>继承 {@link ClientScopeModel}、{@link RoleContainerModel} 等，表示 realm 中的 OAuth/OIDC 客户端。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientModel extends ClientScopeModel, RoleContainerModel,  ProtocolMapperContainerModel, ScopeContainerModel {

    // 客户端通用属性键
    // COMMON ATTRIBUTES

    String PRIVATE_KEY = "privateKey";
    String PUBLIC_KEY = "publicKey";
    String X509CERTIFICATE = "X509Certificate";
    String LOGO_URI ="logoUri";
    String POLICY_URI ="policyUri";
    String TOS_URI ="tosUri";
    String TYPE = "type";

    /** 客户端创建完成事件。 */
    interface ClientCreationEvent extends ProviderEvent {
        ClientModel getCreatedClient();
    }

    // 客户端完全初始化（含全部属性）后也会触发
    // Called also during client creation after client is fully initialized (including all attributes etc)
    /** 客户端批量更新完成事件。 */
    interface ClientUpdatedEvent extends ProviderEvent {
        ClientModel getUpdatedClient();
        KeycloakSession getKeycloakSession();
    }

    /** 客户端 ID 变更事件。 */
    interface ClientIdChangeEvent extends ProviderEvent {
        ClientModel getUpdatedClient();
        String getPreviousClientId();
        String getNewClientId();
        KeycloakSession getKeycloakSession();
    }

    /** 客户端删除事件。 */
    interface ClientRemovedEvent extends ProviderEvent {
        ClientModel getClient();
        KeycloakSession getKeycloakSession();
    }

    /** 客户端协议变更事件。 */
    interface ClientProtocolUpdatedEvent extends ProviderEvent {
        ClientModel getClient();
    }

    /**
     * 通知其他提供者客户端已更新（批量变更完成后调用）。
     * Notifies other providers that this client has been updated.
     * <p>
     * After a client is updated, providers can register for {@link ClientUpdatedEvent}.
     * The setters in this model do not send an update for individual updates of the model.
     * This method is here to allow for sending this event for this client,
     * allowsing for to group multiple changes of a client and signal that
     * all the changes in this client have been performed.
     *
     * @deprecated Do not use, to be removed
     *
     * @see ProviderEvent
     * @see ProviderEventManager
     * @see ClientUpdatedEvent
     */
    void updateClient();

    /**
     * 返回客户端内部 ID（UUID）。
     * Returns client internal ID (UUID).
     * @return
     */
    String getId();

    /**
     * 返回用户定义的客户端 ID（对应 OIDC client_id）。
     * Returns client ID as defined by the user.
     * @return
     */
    String getClientId();

    void setClientId(String clientId);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    default String getType() {
        return getAttribute(TYPE);
    }

    default void setType(String type) {
        setAttribute(TYPE, type);
    }

    boolean isEnabled();

    void setEnabled(boolean enabled);

    boolean isAlwaysDisplayInConsole();

    void setAlwaysDisplayInConsole(boolean alwaysDisplayInConsole);

    boolean isSurrogateAuthRequired();

    void setSurrogateAuthRequired(boolean surrogateAuthRequired);

    Set<String> getWebOrigins();

    void setWebOrigins(Set<String> webOrigins);

    void addWebOrigin(String webOrigin);

    void removeWebOrigin(String webOrigin);

    Set<String> getRedirectUris();

    void setRedirectUris(Set<String> redirectUris);

    void addRedirectUri(String redirectUri);

    void removeRedirectUri(String redirectUri);

    String getManagementUrl();

    void setManagementUrl(String url);

    String getRootUrl();

    void setRootUrl(String url);

    String getBaseUrl();

    void setBaseUrl(String url);

    boolean isBearerOnly();
    void setBearerOnly(boolean only);

    int getNodeReRegistrationTimeout();

    void setNodeReRegistrationTimeout(int timeout);

    String getClientAuthenticatorType();
    void setClientAuthenticatorType(String clientAuthenticatorType);

    boolean validateSecret(String secret);
    String getSecret();
    public void setSecret(String secret);

    String getRegistrationToken();
    void setRegistrationToken(String registrationToken);

    String getProtocol();
    void setProtocol(String protocol);

    void setAttribute(String name, String value);
    void removeAttribute(String name);
    String getAttribute(String name);
    Map<String, String> getAttributes();

    /**
     * 获取客户端对认证流绑定的覆盖（如 browser、direct_grant）。
     * Get authentication flow binding override for this client.  Allows client to override an authentication flow binding.
     *
     * @param binding examples are "browser", "direct_grant"
     *
     * @return
     */
    String getAuthenticationFlowBindingOverride(String binding);
    Map<String, String> getAuthenticationFlowBindingOverrides();
    void removeAuthenticationFlowBindingOverride(String binding);
    void setAuthenticationFlowBindingOverride(String binding, String flowId);

    boolean isFrontchannelLogout();
    void setFrontchannelLogout(boolean flag);

    boolean isFullScopeAllowed();
    void setFullScopeAllowed(boolean value);

    @Override
    default boolean hasDirectScope(RoleModel role) {
        if (getScopeMappingsStream().anyMatch(r -> Objects.equals(r, role))) return true;

        return getRolesStream().anyMatch(r -> Objects.equals(r, role));
    }

    boolean isPublicClient();
    void setPublicClient(boolean flag);

    boolean isConsentRequired();
    void setConsentRequired(boolean consentRequired);

    boolean isStandardFlowEnabled();
    void setStandardFlowEnabled(boolean standardFlowEnabled);

    boolean isImplicitFlowEnabled();
    void setImplicitFlowEnabled(boolean implicitFlowEnabled);

    boolean isDirectAccessGrantsEnabled();
    void setDirectAccessGrantsEnabled(boolean directAccessGrantsEnabled);

    boolean isServiceAccountsEnabled();
    void setServiceAccountsEnabled(boolean serviceAccountsEnabled);

    RealmModel getRealm();

    /**
     * 将 {@link ClientScopeModel} 关联到此客户端（默认或可选作用域）。
     * Add clientScope with this client. Add it as default scope (if parameter 'defaultScope' is true) or optional scope (if parameter 'defaultScope' is false)
     * @param clientScope
     * @param defaultScope
     */
    void addClientScope(ClientScopeModel clientScope, boolean defaultScope);

    /**
     * Add clientScopes with this client. Add as default scopes (if parameter 'defaultScope' is true) or optional scopes (if parameter 'defaultScope' is false)
     * @param clientScopes
     * @param defaultScope
     */
    void addClientScopes(Set<ClientScopeModel> clientScopes, boolean defaultScope);

    void removeClientScope(ClientScopeModel clientScope);

    /**
     * 返回与此客户端关联的默认或可选作用域映射（键为作用域名称）。
     * Return all default scopes (if 'defaultScope' is true) or all optional scopes (if 'defaultScope' is false) linked with this client
     *
     * @param defaultScope
     * @return map where key is the name of the clientScope, value is particular clientScope. Returns empty map if no scopes linked (never returns null).
     */
    Map<String, ClientScopeModel> getClientScopes(boolean defaultScope);

    /**
     * 动态解析未在默认/可选列表中的作用域（已重命名，请用 {@link #getParameterizedClientScope(String)}）。
     * <p>Returns a {@link ClientScopeModel} associated with this client.
     *
     * <p>This method is used as a fallback in order to let clients to resolve a {@code scope} dynamically which is not listed as default or optional scope when calling {@link #getClientScopes(boolean)}.
     *
     * @param scope the scope name
     * @return the client scope
     * @deprecated Use {@link #getParameterizedClientScope(String)} instead as the feature was renamed
     */
    @Deprecated(forRemoval = true)
    default ClientScopeModel getDynamicClientScope(String scope) {
        return null;
    }

    /**
     * 解析参数化作用域（未在默认/可选列表中时的回退）。
     * <p>Returns a {@link ClientScopeModel} associated with this client.
     *
     * <p>This method is used as a fallback in order to let clients to resolve a {@code scope} which is not listed
     * as default or optional scope when calling {@link #getClientScopes(boolean)}.
     *
     * @param scope the scope name
     * @return the client scope
     */
    default ClientScopeModel getParameterizedClientScope(String scope) {
        return getDynamicClientScope(scope);
    }

    /**
     * 自 epoch 起的 not-before 时间（秒）。
     * Time in seconds since epoc
     *
     * @return
     */
    int getNotBefore();

    void setNotBefore(int notBefore);

     Map<String, Integer> getRegisteredNodes();

    /**
     * 注册集群节点或更新已注册节点的最后重新注册时间。
     * Register node or just update the 'lastReRegistration' time if this node is already registered
     *
     * @param nodeHost
     * @param registrationTime
     */
    void registerNode(String nodeHost, int registrationTime);

    void unregisterNode(String nodeHost);


    // 客户端默认不在同意屏显示
    // Clients are not displayed on consent screen by default
    @Override
    default boolean isDisplayOnConsentScreen() {
        String displayVal = getAttribute(DISPLAY_ON_CONSENT_SCREEN);
        return displayVal==null ? false : Boolean.parseBoolean(displayVal);
    }

    // 同意屏文案缺省时回退到 name 或 clientId
    // Fallback to name or clientId if consentScreenText attribute is null
    @Override
    default String getConsentScreenText() {
        String consentScreenText = ClientScopeModel.super.getConsentScreenText();
        if (ObjectUtil.isBlank(consentScreenText)) {
            consentScreenText = getClientId();
        }
        return consentScreenText;
    }

    /**
     * 获取客户端创建时间戳；旧客户端可能为 null。
     * Get timestamp of client creation. May be null for clients created before this feature introduction.
     */
    default Long getCreatedTimestamp() {
        return null;
    }

    /**
     * 获取客户端最后修改时间戳；未修改过的旧客户端可能为 null。
     * Get timestamp of last client modification. May be null for clients that have not been modified
     * since this feature was introduced.
     */
    default Long getLastModifiedTimestamp() {
        return null;
    }
}
