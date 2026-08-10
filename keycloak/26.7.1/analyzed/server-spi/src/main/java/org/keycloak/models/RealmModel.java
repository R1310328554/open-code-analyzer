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

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.common.enums.SslRequired;
import org.keycloak.component.ComponentModel;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * Realm 模型：Keycloak 租户核心，管理用户、客户端、认证流、令牌生命周期与组织等配置。
 * <p>继承 {@link RoleContainerModel}，是大多数 Provider 操作的上下文边界。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface RealmModel extends RoleContainerModel {

    /** 按 Realm 名称排序的比较器。 */
    Comparator<RealmModel> COMPARE_BY_NAME = Comparator.comparing(RealmModel::getName);

    /** Realm 创建事件。 */
    interface RealmCreationEvent extends ProviderEvent {
        RealmModel getCreatedRealm();
        KeycloakSession getKeycloakSession();
    }

    /** Realm 创建后事件。 */
    interface RealmPostCreateEvent extends ProviderEvent {
        RealmModel getCreatedRealm();
        KeycloakSession getKeycloakSession();
    }

    /** Realm 删除事件。 */
    interface RealmRemovedEvent extends ProviderEvent {
        RealmModel getRealm();
        KeycloakSession getKeycloakSession();
    }

    /** 身份提供方更新事件。 */
    interface IdentityProviderUpdatedEvent extends ProviderEvent {
        RealmModel getRealm();
        IdentityProviderModel getUpdatedIdentityProvider();
        KeycloakSession getKeycloakSession();
    }

    /** 身份提供方删除事件。 */
    interface IdentityProviderRemovedEvent extends ProviderEvent {
        RealmModel getRealm();
        IdentityProviderModel getRemovedIdentityProvider();
        KeycloakSession getKeycloakSession();
    }

    /** Realm 属性更新事件。 */
    interface RealmAttributeUpdateEvent extends ProviderEvent {
        RealmModel getRealm();
        String getAttributeName();
        String getAttributeValue();
        KeycloakSession getKeycloakSession();
    }

    @Override
    String getId();

    String getName();

    /** 设置 Name */
    void setName(String name);

    String getDisplayName();

    void setDisplayName(String displayName);

    String getDisplayNameHtml();

    void setDisplayNameHtml(String displayNameHtml);

    /** @return 是否Enabled */
    boolean isEnabled();

    void setEnabled(boolean enabled);

    SslRequired getSslRequired();

    void setSslRequired(SslRequired sslRequired);

    boolean isRegistrationAllowed();

    /** 设置 RegistrationAllowed */
    void setRegistrationAllowed(boolean registrationAllowed);

    boolean isRegistrationEmailAsUsername();

    void setRegistrationEmailAsUsername(boolean registrationEmailAsUsername);

    boolean isRememberMe();

    void setRememberMe(boolean rememberMe);

    /** @return 是否EditUsernameAllowed */
    boolean isEditUsernameAllowed();

    void setEditUsernameAllowed(boolean editUsernameAllowed);

    boolean isUserManagedAccessAllowed();

    void setUserManagedAccessAllowed(boolean userManagedAccessAllowed);

    boolean isOrganizationsEnabled();

    /** 设置 OrganizationsEnabled */
    void setOrganizationsEnabled(boolean organizationsEnabled);

    boolean isAdminPermissionsEnabled();

    void setAdminPermissionsEnabled(boolean adminPermissionsEnabled);

    boolean isVerifiableCredentialsEnabled();

    void setVerifiableCredentialsEnabled(boolean verifiableCredentialsEnabled);

    /** 设置 ScimApiEnabled */
    void setScimApiEnabled(boolean enabled);

    boolean isScimApiEnabled();

    void setAttribute(String name, String value);
    default void setAttribute(String name, Boolean value) {
        setAttribute(name, value.toString());
    }
    default void setAttribute(String name, Integer value) {
        setAttribute(name, value.toString());
    }
    default void setAttribute(String name, Long value) {
        setAttribute(name, value.toString());
    }
    /** 移除 Attribute */
    void removeAttribute(String name);
    String getAttribute(String name);
    default Integer getAttribute(String name, Integer defaultValue) {
        String v = getAttribute(name);
        return v != null && !v.isEmpty() ? Integer.valueOf(v) : defaultValue;
    }
    default Long getAttribute(String name, Long defaultValue) {
        String v = getAttribute(name);
        return v != null && !v.isEmpty() ? Long.valueOf(v) : defaultValue;
    }
    default Boolean getAttribute(String name, Boolean defaultValue) {
        String v = getAttribute(name);
        return v != null && !v.isEmpty() ? Boolean.valueOf(v) : defaultValue;
    }
    default <V extends Enum<V>> V getAttribute(String name, Class<V> enumClass, V defaultValue) {
        String value = getAttribute(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
    Map<String, String> getAttributes();

    //--- 暴力破解防护设置
    /** @return 是否BruteForceProtected */
    boolean isBruteForceProtected();
    void setBruteForceProtected(boolean value);
    boolean isPermanentLockout();
    void setPermanentLockout(boolean val);
    int getMaxTemporaryLockouts();
    void setMaxTemporaryLockouts(int val);
    RealmRepresentation.BruteForceStrategy getBruteForceStrategy();
    void setBruteForceStrategy(RealmRepresentation.BruteForceStrategy val);
    int getMaxFailureWaitSeconds();
    /** 设置 MaxFailureWaitSeconds */
    void setMaxFailureWaitSeconds(int val);
    int getWaitIncrementSeconds();
    void setWaitIncrementSeconds(int val);
    int getMinimumQuickLoginWaitSeconds();
    void setMinimumQuickLoginWaitSeconds(int val);
    long getQuickLoginCheckMilliSeconds();
    void setQuickLoginCheckMilliSeconds(long val);
    int getMaxDeltaTimeSeconds();
    void setMaxDeltaTimeSeconds(int val);
    int getFailureFactor();
    /** 设置 FailureFactor */
    void setFailureFactor(int failureFactor);
    int getMaxSecondaryAuthFailures();
    void setMaxSecondaryAuthFailures(int maxSecondaryAuthFailures);
    //--- 暴力破解防护设置结束


    boolean isVerifyEmail();

    void setVerifyEmail(boolean verifyEmail);

    /** @return 是否LoginWithEmailAllowed */
    boolean isLoginWithEmailAllowed();

    void setLoginWithEmailAllowed(boolean loginWithEmailAllowed);

    boolean isDuplicateEmailsAllowed();

    void setDuplicateEmailsAllowed(boolean duplicateEmailsAllowed);

    boolean isResetPasswordAllowed();

    /** 设置 ResetPasswordAllowed */
    void setResetPasswordAllowed(boolean resetPasswordAllowed);

    String getDefaultSignatureAlgorithm();
    void setDefaultSignatureAlgorithm(String defaultSignatureAlgorithm);

    boolean isRevokeRefreshToken();
    void setRevokeRefreshToken(boolean revokeRefreshToken);

    int getRefreshTokenMaxReuse();
    /** 设置 RefreshTokenMaxReuse */
    void setRefreshTokenMaxReuse(int revokeRefreshTokenCount);

    int getSsoSessionIdleTimeout();
    void setSsoSessionIdleTimeout(int seconds);

    int getSsoSessionMaxLifespan();
    void setSsoSessionMaxLifespan(int seconds);

    int getSsoSessionIdleTimeoutRememberMe();
    /** 设置 SsoSessionIdleTimeoutRememberMe */
    void setSsoSessionIdleTimeoutRememberMe(int seconds);

    int getSsoSessionMaxLifespanRememberMe();
    void setSsoSessionMaxLifespanRememberMe(int seconds);

    int getOfflineSessionIdleTimeout();
    void setOfflineSessionIdleTimeout(int seconds);

    int getAccessTokenLifespan();

    // KEYCLOAK-7688 Offline Session Max for Offline Token
    /** @return 是否OfflineSessionMaxLifespanEnabled */
    boolean isOfflineSessionMaxLifespanEnabled();
    void setOfflineSessionMaxLifespanEnabled(boolean offlineSessionMaxLifespanEnabled);

    int getOfflineSessionMaxLifespan();
    void setOfflineSessionMaxLifespan(int seconds);

    int getClientSessionIdleTimeout();
    void setClientSessionIdleTimeout(int seconds);

    int getClientSessionMaxLifespan();
    /** 设置 ClientSessionMaxLifespan */
    void setClientSessionMaxLifespan(int seconds);

    int getClientOfflineSessionIdleTimeout();
    void setClientOfflineSessionIdleTimeout(int seconds);

    int getClientOfflineSessionMaxLifespan();
    void setClientOfflineSessionMaxLifespan(int seconds);

    void setAccessTokenLifespan(int seconds);

    int getAccessTokenLifespanForImplicitFlow();
    /** 设置 AccessTokenLifespanForImplicitFlow */
    void setAccessTokenLifespanForImplicitFlow(int seconds);

    int getAccessCodeLifespan();

    void setAccessCodeLifespan(int seconds);

    int getAccessCodeLifespanUserAction();

    void setAccessCodeLifespanUserAction(int seconds);

    OAuth2DeviceConfig getOAuth2DeviceConfig();

    CibaConfig getCibaPolicy();

    ParConfig getParPolicy();

    /**
     * 返回所有用户操作令牌生命周期映射，永不为 null。
     * This method will return a map with all the lifespans available
     * or an empty map, but never null.
     * @return map with user action token lifespans
     */
    Map<String, Integer> getUserActionTokenLifespans();

    int getAccessCodeLifespanLogin();

    void setAccessCodeLifespanLogin(int seconds);

    int getActionTokenGeneratedByAdminLifespan();
    /** 设置 ActionTokenGeneratedByAdminLifespan */
    void setActionTokenGeneratedByAdminLifespan(int seconds);

    int getActionTokenGeneratedByUserLifespan();
    void setActionTokenGeneratedByUserLifespan(int seconds);

    int getActionTokenGeneratedByUserLifespan(String actionTokenType);
    void setActionTokenGeneratedByUserLifespan(String actionTokenType, Integer seconds);

    /**
     * 以流形式返回必需凭证。
     * Returns required credentials as a stream.
     * @return Stream of {@link RequiredCredentialModel}. Never returns {@code null}.
     */
    Stream<RequiredCredentialModel> getRequiredCredentialsStream();

    void addRequiredCredential(String cred);

    PasswordPolicy getPasswordPolicy();

    /** 设置 PasswordPolicy */
    void setPasswordPolicy(PasswordPolicy policy);

    OTPPolicy getOTPPolicy();
    void setOTPPolicy(OTPPolicy policy);

    /**
     * 返回双因素认证的 WebAuthn 策略。
     * @return  WebAuthn policy for 2-factor authentication
     */
    WebAuthnPolicy getWebAuthnPolicy();

    /**
     * 设置双因素认证的 WebAuthn 策略。
     * Set WebAuthn policy for 2-factor authentication
     *
     * @param policy
     */
    void setWebAuthnPolicy(WebAuthnPolicy policy);

    /**
     *
     * @return WebAuthn passwordless policy below. This is temporary and will be removed later.
     */
    WebAuthnPolicy getWebAuthnPolicyPasswordless();

    /**
     * 设置无密码 WebAuthn 策略（临时 API）。
     * Set WebAuthn passwordless policy below. This is temporary and will be removed later.
     * @param policy
     */
    void setWebAuthnPolicyPasswordless(WebAuthnPolicy policy);

    RoleModel getRoleById(String id);

    /**
     * 以流形式返回默认组。
     * Returns default groups as a stream.
     * @return Stream of {@link GroupModel}. Never returns {@code null}.
     */
    Stream<GroupModel> getDefaultGroupsStream();

    void addDefaultGroup(GroupModel group);

    void removeDefaultGroup(GroupModel group);

    /**
     * 以流形式返回客户端。
     * Returns clients as a stream.
     * @return Stream of {@link ClientModel}. Never returns {@code null}.
     */
    Stream<ClientModel> getClientsStream();

    /**
     * Returns clients as a stream.
     * @param firstResult {@code Integer} Index of the first desired client. Ignored if negative or {@code null}.
     * @param maxResults {@code Integer} Maximum number of returned clients. Ignored if negative or {@code null}.
     * @return Stream of {@link ClientModel}. Never returns {@code null}.
     */
    Stream<ClientModel> getClientsStream(Integer firstResult, Integer maxResults);

    Long getClientsCount();

    /**
     * 以流形式返回在管理控制台始终显示的客户端。
     * Returns clients which are always displayed in the admin console as a stream.
     * @return Stream of {@link ClientModel}. Never returns {@code null}.
     */
    Stream<ClientModel> getAlwaysDisplayInConsoleClientsStream();

    ClientModel addClient(String name);

    ClientModel addClient(String id, String clientId);

    /** 移除 Client */
    boolean removeClient(String id);

    ClientModel getClientById(String id);
    ClientModel getClientByClientId(String clientId);

    /**
     * 按客户端 ID 搜索客户端。
     * Search for clients by provided client's id.
     * @param clientId {@code String} Id of the client.
     * @param firstResult Index of the first desired client. Ignored if negative or {@code null}.
     * @param maxResults Maximum number of returned clients. Ignored if negative or {@code null}.
     * @return Stream of {@link ClientModel}. Never returns {@code null}.
     */
    Stream<ClientModel> searchClientByClientIdStream(String clientId, Integer firstResult, Integer maxResults);

    Stream<ClientModel> searchClientByAttributes(Map<String, String> attributes, Integer firstResult, Integer maxResults);

    Stream<ClientModel> searchClientByAuthenticationFlowBindingOverrides(Map<String, String> overrides, Integer firstResult, Integer maxResults);

    /** 更新 RequiredCredentials */
    void updateRequiredCredentials(Set<String> creds);

    Map<String, String> getBrowserSecurityHeaders();
    void setBrowserSecurityHeaders(Map<String, String> headers);

    Map<String, String> getSmtpConfig();

    void setSmtpConfig(Map<String, String> smtpConfig);

    AuthenticationFlowModel getBrowserFlow();
    /** 设置 BrowserFlow */
    void setBrowserFlow(AuthenticationFlowModel flow);

    AuthenticationFlowModel getRegistrationFlow();
    void setRegistrationFlow(AuthenticationFlowModel flow);

    AuthenticationFlowModel getDirectGrantFlow();
    void setDirectGrantFlow(AuthenticationFlowModel flow);

    AuthenticationFlowModel getResetCredentialsFlow();
    /** 设置 ResetCredentialsFlow */
    void setResetCredentialsFlow(AuthenticationFlowModel flow);

    AuthenticationFlowModel getClientAuthenticationFlow();
    void setClientAuthenticationFlow(AuthenticationFlowModel flow);

    AuthenticationFlowModel getDockerAuthenticationFlow();
    void setDockerAuthenticationFlow(AuthenticationFlowModel flow);

    AuthenticationFlowModel getFirstBrokerLoginFlow();
    /** 设置 FirstBrokerLoginFlow */
    void setFirstBrokerLoginFlow(AuthenticationFlowModel flow);

    /**
     * 以流形式返回认证流。
     * Returns authentications flows as a stream.
     * @return Stream of {@link AuthenticationFlowModel}. Never returns {@code null}.
     */
    Stream<AuthenticationFlowModel> getAuthenticationFlowsStream();

    AuthenticationFlowModel getFlowByAlias(String alias);
    AuthenticationFlowModel addAuthenticationFlow(AuthenticationFlowModel model);
    AuthenticationFlowModel getAuthenticationFlowById(String id);
    /** 移除 AuthenticationFlow */
    void removeAuthenticationFlow(AuthenticationFlowModel model);
    void updateAuthenticationFlow(AuthenticationFlowModel model);

    /**
     * 按优先级返回排序的认证执行流。
     * Returns sorted (according to priority) {@link AuthenticationExecutionModel AuthenticationExecutionModel} as a stream.
     * It should be used with forEachOrdered if the ordering is required.
     * @param flowId {@code String} Id of the flow.
     * @return Sorted stream of {@link AuthenticationExecutionModel}. Never returns {@code null}.
     */
    Stream<AuthenticationExecutionModel> getAuthenticationExecutionsStream(String flowId);

    AuthenticationExecutionModel getAuthenticationExecutionById(String id);
    AuthenticationExecutionModel getAuthenticationExecutionByFlowId(String flowId);
    /** 添加 AuthenticatorExecution */
    AuthenticationExecutionModel addAuthenticatorExecution(AuthenticationExecutionModel model);
    void updateAuthenticatorExecution(AuthenticationExecutionModel model);
    void removeAuthenticatorExecution(AuthenticationExecutionModel model);

    /**
     * 以流形式返回认证器配置。
     * Returns authentication configs as a stream.
     * @return Stream of {@link AuthenticatorConfigModel}. Never returns {@code null}.
     */
    Stream<AuthenticatorConfigModel> getAuthenticatorConfigsStream();

    AuthenticatorConfigModel addAuthenticatorConfig(AuthenticatorConfigModel model);
    void updateAuthenticatorConfig(AuthenticatorConfigModel model);
    void removeAuthenticatorConfig(AuthenticatorConfigModel model);
    AuthenticatorConfigModel getAuthenticatorConfigById(String id);
    AuthenticatorConfigModel getAuthenticatorConfigByAlias(String alias);

    RequiredActionConfigModel getRequiredActionConfigById(String id);
    RequiredActionConfigModel getRequiredActionConfigByAlias(String alias);
    /** 移除 RequiredActionProviderConfig */
    void removeRequiredActionProviderConfig(RequiredActionConfigModel model);
    void updateRequiredActionConfig(RequiredActionConfigModel model);
    Stream<RequiredActionConfigModel> getRequiredActionConfigsStream();

    /**
     * 按优先级返回排序的必需操作 Provider 流。
     * Returns sorted {@link RequiredActionProviderModel RequiredActionProviderModel} as a stream.
     * It should be used with forEachOrdered if the ordering is required.
     * @return Sorted stream of {@link RequiredActionProviderModel}. Never returns {@code null}.
     */
    Stream<RequiredActionProviderModel> getRequiredActionProvidersStream();

    RequiredActionProviderModel addRequiredActionProvider(RequiredActionProviderModel model);
    void updateRequiredActionProvider(RequiredActionProviderModel model);
    /** 移除 RequiredActionProvider */
    void removeRequiredActionProvider(RequiredActionProviderModel model);
    RequiredActionProviderModel getRequiredActionProviderById(String id);
    RequiredActionProviderModel getRequiredActionProviderByAlias(String alias);

    /**
     * 以流形式返回身份提供方（已弃用）。
     * Returns identity providers as a stream.
     *
     * @return Stream of {@link IdentityProviderModel}. Never returns {@code null}.
     * @deprecated Use {@link IdentityProviderStorageProvider#getAllStream(IdentityProviderQuery)} instead.
     */
    @Deprecated
    Stream<IdentityProviderModel> getIdentityProvidersStream();

    /**
     * 已弃用：请使用 {@link IdentityProviderStorageProvider#getByAlias(String)}。
     * @deprecated Use {@link IdentityProviderStorageProvider#getByAlias(String)} instead.
     */
    @Deprecated
    IdentityProviderModel getIdentityProviderByAlias(String alias);

    /**
     * 已弃用：请使用 {@link IdentityProviderStorageProvider#create(IdentityProviderModel)}。
     * @deprecated Use {@link IdentityProviderStorageProvider#create(IdentityProviderModel)} instead.
     */
    @Deprecated
    void addIdentityProvider(IdentityProviderModel identityProvider);

    /**
     * 已弃用：请使用 {@link IdentityProviderStorageProvider#remove(String)}。
     * @deprecated Use {@link IdentityProviderStorageProvider#remove(String)} instead.
     */
    @Deprecated
    void removeIdentityProviderByAlias(String alias);

    /**
     * 已弃用：请使用 {@link IdentityProviderStorageProvider#update(IdentityProviderModel)}。
     * @deprecated Use {@link IdentityProviderStorageProvider#update(IdentityProviderModel)} instead.
     */
    @Deprecated
    void updateIdentityProvider(IdentityProviderModel identityProvider);

    /**
     * 以流形式返回 IdP 映射器（已弃用）。
     * Returns identity provider mappers as a stream.
     * @return Stream of {@link IdentityProviderMapperModel}. Never returns {@code null}.
     * @deprecated Use {@link IDPProvider#getMappersStream()} instead.
     */
    @Deprecated
    Stream<IdentityProviderMapperModel> getIdentityProviderMappersStream();

    /**
     * 按 broker 别名返回 IdP 映射器流（已弃用）。
     * Returns identity provider mappers by the provided alias as a stream.
     * @param brokerAlias {@code String} Broker's alias to filter results.
     * @return Stream of {@link IdentityProviderMapperModel} Never returns {@code null}.
     * @deprecated Use {@link IDPProvider#getMappersByAliasStream(String)} instead.
     */
    @Deprecated
    Stream<IdentityProviderMapperModel> getIdentityProviderMappersByAliasStream(String brokerAlias);

    /**
     * 已弃用：请使用 {@link IDPProvider#createMapper(IdentityProviderMapperModel)}。
     * @deprecated Use {@link IDPProvider#createMapper(IdentityProviderMapperModel)} instead.
     */
    @Deprecated
    IdentityProviderMapperModel addIdentityProviderMapper(IdentityProviderMapperModel model);

    /**
     * 已弃用：请使用 {@link IDPProvider#removeMapper(IdentityProviderMapperModel)}。
     * @deprecated Use {@link IDPProvider#removeMapper(IdentityProviderMapperModel)} instead.
     */
    @Deprecated
    void removeIdentityProviderMapper(IdentityProviderMapperModel mapping);

    /**
     * 已弃用：请使用 {@link IDPProvider#updateMapper(IdentityProviderMapperModel)}。
     * @deprecated Use {@link IDPProvider#updateMapper(IdentityProviderMapperModel)} instead.
     */
    @Deprecated
    void updateIdentityProviderMapper(IdentityProviderMapperModel mapping);

    /**
     * 已弃用：请使用 {@link IDPProvider#getMapperById(String)}。
     * @deprecated Use {@link IDPProvider#getMapperById(String)} instead.
     */
    @Deprecated
    IdentityProviderMapperModel getIdentityProviderMapperById(String id);

    /**
     * 已弃用：请使用 {@link IDPProvider#getMapperByName(String, String)}。
     * @deprecated Use {@link IDPProvider#getMapperByName(String, String)} instead.
     */
    @Deprecated
    IdentityProviderMapperModel getIdentityProviderMapperByName(String brokerAlias, String name);


    /**
     * 添加组件模型，并调用 ComponentFactory.onCreate()。
     * Adds component model.  Will call onCreate() method of ComponentFactory
     *
     * @param model
     * @return
     */
    ComponentModel addComponentModel(ComponentModel model);

    /**
     * 导入组件模型，不调用 ComponentFactory.onCreate()。
     * Adds component model.  Will NOT call onCreate() method of ComponentFactory
     *
     * @param model
     * @return
     */
    ComponentModel importComponentModel(ComponentModel model);

    /**
     * 更新组件模型，并调用 ComponentFactory.onUpdate()。
     * Updates component model. Will call onUpdate() method of ComponentFactory
     * @param component to be updated
     */
    void updateComponent(ComponentModel component);

    /**
     * 删除组件，并调用 ComponentFactory.preRemove()。
     * Removes given component. Will call preRemove() method of ComponentFactory.
     * Also calls {@code this.removeComponents(component.getId())}.
     *
     * @param component to be removed
     */
    void removeComponent(ComponentModel component);

    /**
     * 删除指定父 ID 下的所有组件。
     * Removes all components with given {@code parentId}
     * @param parentId {@code String} id of parent
     */
    void removeComponents(String parentId);

    /**
     * 按父 ID 与 Provider 类型返回组件流。
     * Returns stream of ComponentModels for specific parentId and providerType.
     * @param parentId {@code String} id of parent
     * @param providerType {@code String} type of provider
     * @return Stream of {@link ComponentModel}. Never returns {@code null}.
     */
    Stream<ComponentModel> getComponentsStream(String parentId, String providerType);

    /**
     * 按父 ID 返回组件流。
     * Returns stream of ComponentModels for specific parentId.
     * @param parentId {@code String} id of parent
     * @return Stream of {@link ComponentModel}. Never returns {@code null}.
     */
    Stream<ComponentModel> getComponentsStream(String parentId);

    /**
     * 以流形式返回所有组件模型。
     * Returns stream of component models.
     * @return Stream of {@link ComponentModel}. Never returns {@code null}.
     */
    Stream<ComponentModel> getComponentsStream();

    ComponentModel getComponent(String id);

    /**
     * 返回表示 StorageProvider 的组件流。
     * Returns stream of ComponentModels that represent StorageProviders for class storageProviderClass in this realm.
     * @param storageProviderClass {@code Class<? extends Provider>}
     * @return Stream of {@link ComponentModel}. Never returns {@code null}.
     */
    default Stream<ComponentModel> getStorageProviders(Class<? extends Provider> storageProviderClass) {
        return getComponentsStream(getId(), storageProviderClass.getName());
    }

    String getLoginTheme();

    /** 设置 LoginTheme */
    void setLoginTheme(String name);

    String getAccountTheme();

    void setAccountTheme(String name);

    String getAdminTheme();

    void setAdminTheme(String name);

    String getEmailTheme();

    /** 设置 EmailTheme */
    void setEmailTheme(String name);


    /**
     * 自 epoch 起的 not-before 时间（秒）。
     * Time in seconds since epoc
     *
     * @return
     */
    int getNotBefore();

    void setNotBefore(int notBefore);

    boolean isEventsEnabled();

    /** 设置 EventsEnabled */
    void setEventsEnabled(boolean enabled);

//    boolean isPersistUserSessions();
//
//    void setPersistUserSessions();

    long getEventsExpiration();

    void setEventsExpiration(long expiration);

    /**
     * 以流形式返回事件监听器。
     * Returns events listeners as a stream.
     * @return Stream of {@code String}. Never returns {@code null}.
     */
    Stream<String> getEventsListenersStream();

    void setEventsListeners(Set<String> listeners);

    /**
     * 以流形式返回已启用的事件类型。
     * Returns enabled event types as a stream.
     * @return Stream of {@code String}. Never returns {@code null}.
     */
    Stream<String> getEnabledEventTypesStream();

    void setEnabledEventTypes(Set<String> enabledEventTypes);

    boolean isAdminEventsEnabled();

    /** 设置 AdminEventsEnabled */
    void setAdminEventsEnabled(boolean enabled);

    boolean isAdminEventsDetailsEnabled();

    void setAdminEventsDetailsEnabled(boolean enabled);

    ClientModel getMasterAdminClient();

    void setMasterAdminClient(ClientModel client);

    /**
     * 返回 Realm 默认角色，客户端默认角色作为其复合角色。
     * Returns default realm role. All both realm and client default roles are assigned as composite of this role.
     * @return Default role of this realm
     */
    RoleModel getDefaultRole();

    /**
     * 设置 Realm 默认角色。
     * Sets default role for this realm
     * @param role to be set
     */
    void setDefaultRole(RoleModel role);

    ClientModel getAdminPermissionsClient();

    void setAdminPermissionsClient(ClientModel client);

    /**
     * 已弃用：请使用 {@link IdentityProviderStorageProvider#isIdentityFederationEnabled()}。
     * @deprecated use {@link IdentityProviderStorageProvider#isIdentityFederationEnabled()} instead.
     */
    @Deprecated
    boolean isIdentityFederationEnabled();

    boolean isInternationalizationEnabled();
    void setInternationalizationEnabled(boolean enabled);

    /**
     * 以流形式返回支持的语言区域。
     * Returns supported locales as a stream.
     * @return Stream of {@code String}. Never returns {@code null}.
     */
    Stream<String> getSupportedLocalesStream();

    void setSupportedLocales(Set<String> locales);
    String getDefaultLocale();
    void setDefaultLocale(String locale);

    default GroupModel createGroup(String name) {
    /** 创建 Group */
        return createGroup(null, name, null);
    };

    default GroupModel createGroup(String id, String name) {
        return createGroup(id, name, null);
    };

    default GroupModel createGroup(String name, GroupModel toParent) {
        return createGroup(null, name, toParent);
    };

    /** 创建 Group */
    GroupModel createGroup(String id, String name, GroupModel toParent);

    GroupModel getGroupById(String id);

    /**
     * 以流形式返回组。
     * Returns groups as a stream.
     * @return Stream of {@link GroupModel}. Never returns {@code null}.
     */
    Stream<GroupModel> getGroupsStream();

    Long getGroupsCount(Boolean onlyTopGroups);
    Long getGroupsCountByNameContaining(String search);

    @Deprecated
    /**
     * 已弃用：请改用 {@link KeycloakSession} 的 {@link GroupProvider}。
     * @deprecated It is now preferable to use {@link GroupProvider} from a {@link KeycloakSession}
     * Returns top level groups as a stream.
     * @return Stream of {@link GroupModel}. Never returns {@code null}.
     */
    Stream<GroupModel> getTopLevelGroupsStream();

    @Deprecated
    /**
     * @deprecated It is now preferable to use {@link GroupProvider} from a {@link KeycloakSession}
     * Returns top level groups as a stream.
     * @param first {@code Integer} Index of the first desired group. Ignored if negative or {@code null}.
     * @param max {@code Integer} Maximum number of returned groups. Ignored if negative or {@code null}.
     * @return Stream of {@link GroupModel}. Never returns {@code null}.
     */
    Stream<GroupModel> getTopLevelGroupsStream(Integer first, Integer max);

    /** 移除 Group */
    boolean removeGroup(GroupModel group);
    void moveGroup(GroupModel group, GroupModel toParent);

    /**
     * 以流形式返回所有客户端范围。
     * Returns all client scopes of this realm as a stream.
     * @return Stream of {@link ClientScopeModel}. Never returns {@code null}.
     */
    Stream<ClientScopeModel> getClientScopesStream();

    /**
     * 创建指定名称的客户端范围，内部 ID 自动生成。
     * Creates new client scope with the given name. Internal ID is created automatically.
     * If given name contains spaces, those are replaced by underscores.
     * @param name {@code String} name of the client scope.
     * @return Model of the created client scope.
     * @throws ModelDuplicateException if client scope with same id or name already exists.
     */
    ClientScopeModel addClientScope(String name);

    /**
     * 使用给定 ID 与名称创建客户端范围。
     * Creates new client scope with the given internal ID and name.
     * If given name contains spaces, those are replaced by underscores.
     * @param id {@code String} id of the client scope.
     * @param name {@code String} name of the client scope.
     * @return Model of the created client scope.
     * @throws ModelDuplicateException if client scope with same id or name already exists.
     */
    ClientScopeModel addClientScope(String id, String name);

    /**
     * 从 Realm 中删除指定 ID 的客户端范围。
     * Removes client scope with given {@code id} from this realm.
     * @param id of the client scope
     * @return true if the realm contained the scope and the removal was successful, false otherwise
     */
    boolean removeClientScope(String id);

    /**
     * 按 ID 获取客户端范围。
     * @param id of the client scope
     * @return Client scope with the given {@code id}, or {@code null} when the scope does not exist.
     */
    ClientScopeModel getClientScopeById(String id);

    /**
     * 将客户端范围加入默认或可选范围列表。
     * Adds given client scope among default/optional client scopes of this realm.
     * The scope will be assigned to each new client.
     * @param clientScope to be added
     * @param defaultScope if {@code true} the scope will be added among default client scopes,
     * if {@code false} it will be added among optional client scopes
     */
    void addDefaultClientScope(ClientScopeModel clientScope, boolean defaultScope);

    /**
     * 从默认或可选范围列表中移除客户端范围。
     * Removes given client scope from default or optional client scopes of this realm.
     * @param clientScope to be removed
     */
    void removeDefaultClientScope(ClientScopeModel clientScope);

    /**
     * 创建或更新指定语言区域的 Realm 本地化文本。
     * Creates or updates the realm-specific localization texts for the given locale.
     * This method will not delete any text.
     * It updates texts, which are already stored or create new ones if the key does not exist yet.
     */
    void createOrUpdateRealmLocalizationTexts(String locale, Map<String, String> localizationTexts);
    boolean removeRealmLocalizationTexts(String locale);
    Map<String, Map<String, String>> getRealmLocalizationTexts();
    Map<String, String> getRealmLocalizationTextsByLocale(String locale);

    /**
     * 返回默认或可选客户端范围流。
     * Returns default client scopes of this realm either default ones or optional ones.
     * @param defaultScope if {@code true} default client scopes are returned,
     * if {@code false} optional client scopes are returned.
     * @return Stream of {@link ClientScopeModel}. Never returns {@code null}.
     */
    Stream<ClientScopeModel> getDefaultClientScopesStream(boolean defaultScope);

    /**
     * 将角色添加为 Realm 默认角色的复合角色。
     * Adds a role as a composite to default role of this realm.
     * @param role to be added
     */
    default void addToDefaultRoles(RoleModel role) {
        getDefaultRole().addCompositeRole(role);
    }

    ClientInitialAccessModel createClientInitialAccessModel(int expiration, int count);
    ClientInitialAccessModel getClientInitialAccessModel(String id);
    /** 移除 ClientInitialAccessModel */
    void removeClientInitialAccessModel(String id);
    Stream<ClientInitialAccessModel> getClientInitialAccesses();
    void decreaseRemainingCount(ClientInitialAccessModel clientInitialAccess);
}
