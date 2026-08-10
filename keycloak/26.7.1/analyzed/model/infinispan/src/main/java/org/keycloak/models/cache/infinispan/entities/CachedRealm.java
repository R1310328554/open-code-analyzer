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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.keycloak.common.enums.SslRequired;
import org.keycloak.common.util.ConcurrentMultivaluedHashMap;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.MultivaluedMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.CibaConfig;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.OAuth2DeviceConfig;
import org.keycloak.models.OTPPolicy;
import org.keycloak.models.ParConfig;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.RequiredCredentialModel;
import org.keycloak.models.WebAuthnPolicy;
import org.keycloak.models.cache.infinispan.DefaultLazyLoader;
import org.keycloak.models.cache.infinispan.LazyLoader;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * 领域（Realm）的 Infinispan 缓存快照实体。
 * <p>
 * 缓存领域的完整配置：令牌生命周期、暴力破解防护、认证流程、主题、
 * 组件、事件监听与国际化等。继承 {@link AbstractExtendableRevisioned}，
 * 部分关联数据（如默认客户端作用域）通过 {@link LazyLoader} 按需加载。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CachedRealm extends AbstractExtendableRevisioned {

    /** 领域内部名称。 */
    protected String name;
    /** 领域显示名称。 */
    protected String displayName;
    /** 领域 HTML 显示名称。 */
    protected String displayNameHtml;
    /** 领域是否启用。 */
    protected boolean enabled;
    /** SSL 要求级别。 */
    protected SslRequired sslRequired;
    /** 是否允许自助注册。 */
    protected boolean registrationAllowed;
    /** 注册时是否以邮箱作为用户名。 */
    protected boolean registrationEmailAsUsername;
    /** 是否启用"记住我"。 */
    protected boolean rememberMe;
    /** 是否要求验证邮箱。 */
    protected boolean verifyEmail;
    /** 是否允许使用邮箱登录。 */
    protected boolean loginWithEmailAllowed;
    /** 是否允许重复邮箱。 */
    protected boolean duplicateEmailsAllowed;
    /** 是否允许重置密码。 */
    protected boolean resetPasswordAllowed;
    /** 是否启用身份联邦。 */
    protected boolean identityFederationEnabled;
    /** 是否允许编辑用户名。 */
    protected boolean editUsernameAllowed;
    /** 是否启用组织功能。 */
    protected boolean organizationsEnabled;
    /** 是否启用管理员权限细粒度控制。 */
    protected boolean adminPermissionsEnabled;
    /** 是否启用可验证凭证功能。 */
    protected boolean verifiableCredentialsEnabled;
    /** 是否启用 SCIM API。 */
    protected boolean scimApiEnabled;
    //--- 暴力破解防护设置
    /** 是否启用暴力破解防护。 */
    protected boolean bruteForceProtected;
    /** 是否永久锁定（而非临时锁定）。 */
    protected boolean permanentLockout;
    /** 最大临时锁定次数。 */
    protected int maxTemporaryLockouts;
    /** 暴力破解策略。 */
    protected RealmRepresentation.BruteForceStrategy bruteForceStrategy;
    /** 最大失败等待秒数。 */
    protected int maxFailureWaitSeconds;
    /** 快速登录最小等待秒数。 */
    protected int minimumQuickLoginWaitSeconds;
    /** 等待递增秒数。 */
    protected int waitIncrementSeconds;
    /** 快速登录检测窗口（毫秒）。 */
    protected long quickLoginCheckMilliSeconds;
    /** 最大增量时间（秒）。 */
    protected int maxDeltaTimeSeconds;
    /** 失败因子。 */
    protected int failureFactor;
    /** 二次认证最大失败次数。 */
    protected int maxSecondaryAuthFailures;
    //--- 暴力破解防护设置结束

    /** 默认签名算法。 */
    protected String defaultSignatureAlgorithm;
    /** 是否在刷新时撤销旧 refresh token。 */
    protected boolean revokeRefreshToken;
    /** refresh token 最大复用次数。 */
    protected int refreshTokenMaxReuse;
    /** SSO 会话空闲超时（秒）。 */
    protected int ssoSessionIdleTimeout;
    /** SSO 会话最大生命周期（秒）。 */
    protected int ssoSessionMaxLifespan;
    /** "记住我" SSO 会话空闲超时（秒）。 */
    protected int ssoSessionIdleTimeoutRememberMe;
    /** "记住我" SSO 会话最大生命周期（秒）。 */
    protected int ssoSessionMaxLifespanRememberMe;
    /** 离线会话空闲超时（秒）。 */
    protected int offlineSessionIdleTimeout;
    // KEYCLOAK-7688 Offline Session Max for Offline Token
    /** 是否启用离线会话最大生命周期限制。 */
    protected boolean offlineSessionMaxLifespanEnabled;
    /** 离线会话最大生命周期（秒）。 */
    protected int offlineSessionMaxLifespan;
    /** 客户端会话空闲超时（秒）。 */
    protected int clientSessionIdleTimeout;
    /** 客户端会话最大生命周期（秒）。 */
    protected int clientSessionMaxLifespan;
    /** 客户端离线会话空闲超时（秒）。 */
    protected int clientOfflineSessionIdleTimeout;
    /** 客户端离线会话最大生命周期（秒）。 */
    protected int clientOfflineSessionMaxLifespan;
    /** 访问令牌生命周期（秒）。 */
    protected int accessTokenLifespan;
    /** 隐式流程访问令牌生命周期（秒）。 */
    protected int accessTokenLifespanForImplicitFlow;
    /** 授权码生命周期（秒）。 */
    protected int accessCodeLifespan;
    /** 用户操作授权码生命周期（秒）。 */
    protected int accessCodeLifespanUserAction;
    /** 登录授权码生命周期（秒）。 */
    protected int accessCodeLifespanLogin;
    /** OAuth2 设备授权配置懒加载器。 */
    protected LazyLoader<RealmModel, OAuth2DeviceConfig> deviceConfig;
    /** 管理员生成的操作令牌生命周期（秒）。 */
    protected int actionTokenGeneratedByAdminLifespan;
    /** 用户生成的操作令牌生命周期（秒）。 */
    protected int actionTokenGeneratedByUserLifespan;
    /** notBefore 时间戳，用于令牌失效控制。 */
    protected int notBefore;
    /** 密码策略。 */
    protected PasswordPolicy passwordPolicy;
    /** OTP 策略。 */
    protected OTPPolicy otpPolicy;
    /** WebAuthn 策略。 */
    protected WebAuthnPolicy webAuthnPolicy;
    /** 无密码 WebAuthn 策略。 */
    protected WebAuthnPolicy webAuthnPasswordlessPolicy;

    /** 登录主题名称。 */
    protected String loginTheme;
    /** 账户主题名称。 */
    protected String accountTheme;
    /** 管理控制台主题名称。 */
    protected String adminTheme;
    /** 邮件主题名称。 */
    protected String emailTheme;
    /** Master 领域管理客户端 ID。 */
    protected String masterAdminClient;

    /** 必需凭证类型列表。 */
    protected List<RequiredCredentialModel> requiredCredentials;
    /** 按父组件 ID 索引的组件映射。 */
    protected MultivaluedMap<String, ComponentModel> componentsByParent = new MultivaluedHashMap<>();
    /** 按父 ID 与 provider 类型索引的组件映射。 */
    protected MultivaluedMap<String, ComponentModel> componentsByParentAndType = new ConcurrentMultivaluedHashMap<>();
    /** 按组件 ID 索引的组件映射。 */
    protected Map<String, ComponentModel> components;

    /** 浏览器安全响应头配置。 */
    protected Map<String, String> browserSecurityHeaders;
    /** SMTP 邮件服务器配置。 */
    protected Map<String, String> smtpConfig;
    /** 认证流程映射（按流程 ID）。 */
    protected Map<String, AuthenticationFlowModel> authenticationFlows = new HashMap<>();
    /** 认证流程列表（保持顺序）。 */
    protected List<AuthenticationFlowModel> authenticationFlowList;
    /** 认证器配置映射（按配置 ID）。 */
    protected Map<String, AuthenticatorConfigModel> authenticatorConfigs;
    /** 必需操作配置映射（按配置 ID）。 */
    protected Map<String, RequiredActionConfigModel> requiredActionProviderConfigs = new HashMap<>();
    /** 必需操作配置映射（按别名）。 */
    protected Map<String, RequiredActionConfigModel> requiredActionProviderConfigsByAlias = new HashMap<>();
    /** 必需操作提供者映射（按 ID）。 */
    protected Map<String, RequiredActionProviderModel> requiredActionProviders = new HashMap<>();
    /** 必需操作提供者列表（保持顺序）。 */
    protected List<RequiredActionProviderModel> requiredActionProviderList;
    /** 必需操作提供者映射（按别名）。 */
    protected Map<String, RequiredActionProviderModel> requiredActionProvidersByAlias = new HashMap<>();
    /** 认证执行步骤映射（按流程 ID 分组）。 */
    protected MultivaluedHashMap<String, AuthenticationExecutionModel> authenticationExecutions = new MultivaluedHashMap<>();
    /** 认证执行映射（按执行 ID）。 */
    protected Map<String, AuthenticationExecutionModel> executionsById = new HashMap<>();
    /** 认证执行映射（按子流程 ID）。 */
    protected Map<String, AuthenticationExecutionModel> executionsByFlowId = new HashMap<>();

    /** 浏览器登录认证流程。 */
    protected AuthenticationFlowModel browserFlow;
    /** 用户注册认证流程。 */
    protected AuthenticationFlowModel registrationFlow;
    /** 直接授权（Resource Owner Password）流程。 */
    protected AuthenticationFlowModel directGrantFlow;
    /** 重置凭证认证流程。 */
    protected AuthenticationFlowModel resetCredentialsFlow;
    /** 客户端认证流程。 */
    protected AuthenticationFlowModel clientAuthenticationFlow;
    /** Docker 认证流程。 */
    protected AuthenticationFlowModel dockerAuthenticationFlow;
    /** 首次 Broker 登录流程。 */
    protected AuthenticationFlowModel firstBrokerLoginFlow;

    /** 是否启用用户事件。 */
    protected boolean eventsEnabled;
    /** 用户事件过期时间（秒）。 */
    protected long eventsExpiration;
    /** 事件监听器名称集合。 */
    protected Set<String> eventsListeners;
    /** 已启用的事件类型集合。 */
    protected Set<String> enabledEventTypes;
    /** 是否启用管理员事件。 */
    protected boolean adminEventsEnabled;
    /** 管理员事件是否记录详情。 */
    protected boolean adminEventsDetailsEnabled;
    /** 默认角色 ID。 */
    protected String defaultRoleId;
    /** 管理员权限客户端 ID。 */
    protected String adminPermissionsClientId;
    /** 是否允许用户自主管理访问权限（UMA）。 */
    private boolean allowUserManagedAccess;

    /** 默认组 ID 列表。 */
    protected List<String> defaultGroups;
    /** 默认客户端作用域 ID 懒加载器。 */
    protected DefaultLazyLoader<RealmModel, List<String>> defaultDefaultClientScopes;
    /** 可选默认客户端作用域 ID 懒加载器。 */
    protected DefaultLazyLoader<RealmModel, List<String>> optionalDefaultClientScopes;
    /** 是否启用国际化。 */
    protected boolean internationalizationEnabled;
    /** 支持的语言区域集合。 */
    protected Set<String> supportedLocales;
    /** 默认语言区域。 */
    protected String defaultLocale;

    /** 领域自定义属性键值对。 */
    protected Map<String, String> attributes;

    /** 各操作令牌类型的生命周期映射（按 actionTokenId）。 */
    private Map<String, Integer> userActionTokenLifespans;

    /** 领域本地化文本（语言 -> 键值对）。 */
    protected Map<String, Map<String,String>> realmLocalizationTexts;

    /** 从领域模型构造缓存快照。 */
    public CachedRealm(long revision, RealmModel model) {
        super(revision, model.getId());
        name = model.getName();
        displayName = model.getDisplayName();
        displayNameHtml = model.getDisplayNameHtml();
        enabled = model.isEnabled();
        allowUserManagedAccess = model.isUserManagedAccessAllowed();
        sslRequired = model.getSslRequired();
        registrationAllowed = model.isRegistrationAllowed();
        registrationEmailAsUsername = model.isRegistrationEmailAsUsername();
        rememberMe = model.isRememberMe();
        verifyEmail = model.isVerifyEmail();
        loginWithEmailAllowed = model.isLoginWithEmailAllowed();
        duplicateEmailsAllowed = model.isDuplicateEmailsAllowed();
        resetPasswordAllowed = model.isResetPasswordAllowed();
        editUsernameAllowed = model.isEditUsernameAllowed();
        organizationsEnabled = model.isOrganizationsEnabled();
        adminPermissionsEnabled = model.isAdminPermissionsEnabled();
        verifiableCredentialsEnabled = model.isVerifiableCredentialsEnabled();
        scimApiEnabled = model.isScimApiEnabled();
        //--- brute force settings
        bruteForceProtected = model.isBruteForceProtected();
        permanentLockout = model.isPermanentLockout();
        maxTemporaryLockouts = model.getMaxTemporaryLockouts();
        bruteForceStrategy = model.getBruteForceStrategy();
        maxFailureWaitSeconds = model.getMaxFailureWaitSeconds();
        minimumQuickLoginWaitSeconds = model.getMinimumQuickLoginWaitSeconds();
        waitIncrementSeconds = model.getWaitIncrementSeconds();
        quickLoginCheckMilliSeconds = model.getQuickLoginCheckMilliSeconds();
        maxDeltaTimeSeconds = model.getMaxDeltaTimeSeconds();
        failureFactor = model.getFailureFactor();
        maxSecondaryAuthFailures = model.getMaxSecondaryAuthFailures();
        //--- end brute force settings

        defaultSignatureAlgorithm = model.getDefaultSignatureAlgorithm();
        revokeRefreshToken = model.isRevokeRefreshToken();
        refreshTokenMaxReuse = model.getRefreshTokenMaxReuse();
        ssoSessionIdleTimeout = model.getSsoSessionIdleTimeout();
        ssoSessionMaxLifespan = model.getSsoSessionMaxLifespan();
        ssoSessionIdleTimeoutRememberMe = model.getSsoSessionIdleTimeoutRememberMe();
        ssoSessionMaxLifespanRememberMe = model.getSsoSessionMaxLifespanRememberMe();
        offlineSessionIdleTimeout = model.getOfflineSessionIdleTimeout();
        // KEYCLOAK-7688 Offline Session Max for Offline Token
        offlineSessionMaxLifespanEnabled = model.isOfflineSessionMaxLifespanEnabled();
        offlineSessionMaxLifespan = model.getOfflineSessionMaxLifespan();
        clientSessionIdleTimeout = model.getClientSessionIdleTimeout();
        clientSessionMaxLifespan = model.getClientSessionMaxLifespan();
        clientOfflineSessionIdleTimeout = model.getClientOfflineSessionIdleTimeout();
        clientOfflineSessionMaxLifespan = model.getClientOfflineSessionMaxLifespan();
        accessTokenLifespan = model.getAccessTokenLifespan();
        accessTokenLifespanForImplicitFlow = model.getAccessTokenLifespanForImplicitFlow();
        accessCodeLifespan = model.getAccessCodeLifespan();
        deviceConfig = new DefaultLazyLoader<>(OAuth2DeviceConfig::new, null);
        accessCodeLifespanUserAction = model.getAccessCodeLifespanUserAction();
        accessCodeLifespanLogin = model.getAccessCodeLifespanLogin();
        actionTokenGeneratedByAdminLifespan = model.getActionTokenGeneratedByAdminLifespan();
        actionTokenGeneratedByUserLifespan = model.getActionTokenGeneratedByUserLifespan();
        notBefore = model.getNotBefore();
        passwordPolicy = model.getPasswordPolicy();
        otpPolicy = model.getOTPPolicy();
        webAuthnPolicy = model.getWebAuthnPolicy();
        webAuthnPasswordlessPolicy = model.getWebAuthnPolicyPasswordless();

        loginTheme = model.getLoginTheme();
        accountTheme = model.getAccountTheme();
        adminTheme = model.getAdminTheme();
        emailTheme = model.getEmailTheme();

        requiredCredentials = model.getRequiredCredentialsStream().collect(Collectors.toList());
        userActionTokenLifespans = Map.copyOf(model.getUserActionTokenLifespans());

        smtpConfig = model.getSmtpConfig();
        browserSecurityHeaders = model.getBrowserSecurityHeaders();

        eventsEnabled = model.isEventsEnabled();
        eventsExpiration = model.getEventsExpiration();
        eventsListeners = model.getEventsListenersStream().collect(Collectors.toSet());
        enabledEventTypes = model.getEnabledEventTypesStream().collect(Collectors.toSet());

        adminEventsEnabled = model.isAdminEventsEnabled();
        adminEventsDetailsEnabled = model.isAdminEventsDetailsEnabled();
        adminPermissionsClientId = model.getAdminPermissionsClient() == null ? null : model.getAdminPermissionsClient().getId();

        if(Objects.isNull(model.getDefaultRole())) {
            throw new ModelException("Default Role is null for Realm " + name);
        } else {
            defaultRoleId = model.getDefaultRole().getId();
        }
        ClientModel masterAdminClient = model.getMasterAdminClient();
        this.masterAdminClient = (masterAdminClient != null) ? masterAdminClient.getId() : null;

        defaultDefaultClientScopes = new DefaultLazyLoader<>(realm -> realm.getDefaultClientScopesStream(true).map(ClientScopeModel::getId)
                .collect(Collectors.toList()), null);
        optionalDefaultClientScopes = new DefaultLazyLoader<>(realm -> realm.getDefaultClientScopesStream(false).map(ClientScopeModel::getId)
                .collect(Collectors.toList()), null);

        internationalizationEnabled = model.isInternationalizationEnabled();
        supportedLocales = model.getSupportedLocalesStream().collect(Collectors.toSet());
        defaultLocale = model.getDefaultLocale();
        authenticationFlowList = model.getAuthenticationFlowsStream().collect(Collectors.toList());
        for (AuthenticationFlowModel flow : authenticationFlowList) {
            this.authenticationFlows.put(flow.getId(), flow);
            authenticationExecutions.put(flow.getId(), new LinkedList<>());
            model.getAuthenticationExecutionsStream(flow.getId()).forEachOrdered(execution -> {
                authenticationExecutions.add(flow.getId(), execution);
                executionsById.put(execution.getId(), execution);
                if (execution.getFlowId() != null) {
                    executionsByFlowId.put(execution.getFlowId(), execution);
                }
            });
        }

        authenticatorConfigs = model.getAuthenticatorConfigsStream()
                .collect(Collectors.toMap(AuthenticatorConfigModel::getId, Function.identity()));
        model.getRequiredActionConfigsStream()
                .forEach(requiredActionConfig -> {
                    requiredActionProviderConfigs.put(requiredActionConfig.getId(), requiredActionConfig);
                    requiredActionProviderConfigsByAlias.put(requiredActionConfig.getAlias(), requiredActionConfig);
                });

        requiredActionProviderList = model.getRequiredActionProvidersStream().collect(Collectors.toList());
        for (RequiredActionProviderModel action : requiredActionProviderList) {
            requiredActionProviders.put(action.getId(), action);
            requiredActionProvidersByAlias.put(action.getAlias(), action);
        }

        defaultGroups = model.getDefaultGroupsStream().map(GroupModel::getId).collect(Collectors.toList());

        browserFlow = model.getBrowserFlow();
        registrationFlow = model.getRegistrationFlow();
        directGrantFlow = model.getDirectGrantFlow();
        resetCredentialsFlow = model.getResetCredentialsFlow();
        clientAuthenticationFlow = model.getClientAuthenticationFlow();
        dockerAuthenticationFlow = model.getDockerAuthenticationFlow();
        firstBrokerLoginFlow = model.getFirstBrokerLoginFlow();

        model.getComponentsStream().forEach(component ->
            componentsByParentAndType.add(component.getParentId() + component.getProviderType(), component)
        );
        model.getComponentsStream().forEach(component ->
            componentsByParent.add(component.getParentId(), component)
        );
        components = model.getComponentsStream().collect(Collectors.toMap(ComponentModel::getId, Function.identity()));

        try {
            attributes = model.getAttributes();
        } catch (UnsupportedOperationException ex) {
        }

        realmLocalizationTexts = model.getRealmLocalizationTexts();
    }

    public String getMasterAdminClient() {
        return masterAdminClient;
    }

    public String getDefaultRoleId() {
        return defaultRoleId;
    }

    public String getAdminPermissionsClientId() {
        return adminPermissionsClientId;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayNameHtml() {
        return displayNameHtml;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public SslRequired getSslRequired() {
        return sslRequired;
    }

    public boolean isRegistrationAllowed() {
        return registrationAllowed;
    }

    public boolean isRegistrationEmailAsUsername() {
        return registrationEmailAsUsername;
    }

    public boolean isRememberMe() {
        return this.rememberMe;
    }

    public boolean isBruteForceProtected() {
        return bruteForceProtected;
    }

    public boolean isPermanentLockout() {
        return permanentLockout;
    }

    public int getMaxTemporaryLockouts() {
        return maxTemporaryLockouts;
    }

    public RealmRepresentation.BruteForceStrategy getBruteForceStrategy() {
        return bruteForceStrategy;
    }

    public int getMaxFailureWaitSeconds() {
        return this.maxFailureWaitSeconds;
    }

    public int getWaitIncrementSeconds() {
        return this.waitIncrementSeconds;
    }

    public int getMinimumQuickLoginWaitSeconds() {
        return this.minimumQuickLoginWaitSeconds;
    }

    public long getQuickLoginCheckMilliSeconds() {
        return quickLoginCheckMilliSeconds;
    }

    public int getMaxDeltaTimeSeconds() {
        return maxDeltaTimeSeconds;
    }

    public int getFailureFactor() {
        return failureFactor;
    }

    public int getMaxSecondaryAuthFailures() {
        return maxSecondaryAuthFailures;
    }

    public boolean isVerifyEmail() {
        return verifyEmail;
    }

    public boolean isLoginWithEmailAllowed() {
        return loginWithEmailAllowed;
    }

    public boolean isDuplicateEmailsAllowed() {
        return duplicateEmailsAllowed;
    }

    public boolean isResetPasswordAllowed() {
        return resetPasswordAllowed;
    }

    public boolean isEditUsernameAllowed() {
        return editUsernameAllowed;
    }

    public boolean isOrganizationsEnabled() {
        return organizationsEnabled;
    }

    public boolean isAdminPermissionsEnabled() {
        return adminPermissionsEnabled;
    }

    public boolean isVerifiableCredentialsEnabled() {
        return verifiableCredentialsEnabled;
    }

    public String getDefaultSignatureAlgorithm() {
        return defaultSignatureAlgorithm;
    }

    public boolean isRevokeRefreshToken() {
        return revokeRefreshToken;
    }

    public int getRefreshTokenMaxReuse() {
        return refreshTokenMaxReuse;
    }

    public int getSsoSessionIdleTimeout() {
        return ssoSessionIdleTimeout;
    }

    public int getSsoSessionMaxLifespan() {
        return ssoSessionMaxLifespan;
    }

    public int getSsoSessionIdleTimeoutRememberMe() {
        return ssoSessionIdleTimeoutRememberMe;
    }

    public int getSsoSessionMaxLifespanRememberMe() {
        return ssoSessionMaxLifespanRememberMe;
    }

    public int getOfflineSessionIdleTimeout() {
        return offlineSessionIdleTimeout;
    }

    // KEYCLOAK-7688 Offline Session Max for Offline Token
    public boolean isOfflineSessionMaxLifespanEnabled() {
        return offlineSessionMaxLifespanEnabled;
    }

    public int getOfflineSessionMaxLifespan() {
        return offlineSessionMaxLifespan;
    }

    public int getClientSessionIdleTimeout() {
        return clientSessionIdleTimeout;
    }

    public int getClientSessionMaxLifespan() {
        return clientSessionMaxLifespan;
    }

    public int getClientOfflineSessionIdleTimeout() {
        return clientOfflineSessionIdleTimeout;
    }

    public int getClientOfflineSessionMaxLifespan() {
        return clientOfflineSessionMaxLifespan;
    }

    public int getAccessTokenLifespan() {
        return accessTokenLifespan;
    }

    public int getAccessTokenLifespanForImplicitFlow() {
        return accessTokenLifespanForImplicitFlow;
    }

    public int getAccessCodeLifespan() {
        return accessCodeLifespan;
    }

    public int getAccessCodeLifespanUserAction() {
        return accessCodeLifespanUserAction;
    }

    public Map<String, Integer> getUserActionTokenLifespans() {
        return userActionTokenLifespans;
    }

    public int getAccessCodeLifespanLogin() {
        return accessCodeLifespanLogin;
    }

    public OAuth2DeviceConfig getOAuth2DeviceConfig(KeycloakSession session, Supplier<RealmModel> modelSupplier) {
        return deviceConfig.get(session, modelSupplier);
    }

    public CibaConfig getCibaConfig(Supplier<RealmModel> modelSupplier) {
        return CibaConfig.fromCache(modelSupplier, Collections.unmodifiableMap(attributes));
    }

    public ParConfig getParConfig(Supplier<RealmModel> modelSupplier) {
        return ParConfig.fromCache(modelSupplier, Collections.unmodifiableMap(attributes));
    }

    public int getActionTokenGeneratedByAdminLifespan() {
        return actionTokenGeneratedByAdminLifespan;
    }

    public int getActionTokenGeneratedByUserLifespan() {
        return actionTokenGeneratedByUserLifespan;
    }

    /**
     * 根据操作令牌 ID 返回用户生成的操作令牌生命周期。
     * 若未指定 ID 或未配置专用生命周期，则返回默认值。
     *
     * @param actionTokenId 操作令牌类型 ID
     * @return 生命周期（秒）
     */
    public int getActionTokenGeneratedByUserLifespan(String actionTokenId) {
        if (actionTokenId == null || this.userActionTokenLifespans.get(actionTokenId) == null)
            return getActionTokenGeneratedByUserLifespan();
        return this.userActionTokenLifespans.get(actionTokenId);
    }

    public List<RequiredCredentialModel> getRequiredCredentials() {
        return requiredCredentials;
    }

    public PasswordPolicy getPasswordPolicy() {
        return passwordPolicy;
    }

    public Map<String, String> getSmtpConfig() {
        return smtpConfig;
    }

    public Map<String, String> getBrowserSecurityHeaders() {
        return browserSecurityHeaders;
    }

    public String getLoginTheme() {
        return loginTheme;
    }

    public String getAccountTheme() {
        return accountTheme;
    }

    public String getAdminTheme() {
        return this.adminTheme;
    }

    public String getEmailTheme() {
        return emailTheme;
    }

    public int getNotBefore() {
        return notBefore;
    }

    public boolean isEventsEnabled() {
        return eventsEnabled;
    }

    public long getEventsExpiration() {
        return eventsExpiration;
    }

    public Set<String> getEventsListeners() {
        return eventsListeners;
    }

    public Set<String> getEnabledEventTypes() {
        return enabledEventTypes;
    }

    public boolean isAdminEventsEnabled() {
        return adminEventsEnabled;
    }

    public boolean isAdminEventsDetailsEnabled() {
        return adminEventsDetailsEnabled;
    }

    public boolean isInternationalizationEnabled() {
        return internationalizationEnabled;
    }

    public Set<String> getSupportedLocales() {
        return supportedLocales;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public Map<String, AuthenticationFlowModel> getAuthenticationFlows() {
        return authenticationFlows;
    }

    public Map<String, AuthenticatorConfigModel> getAuthenticatorConfigs() {
        return authenticatorConfigs;
    }

    public MultivaluedHashMap<String, AuthenticationExecutionModel> getAuthenticationExecutions() {
        return authenticationExecutions;
    }

    public AuthenticationExecutionModel getAuthenticationExecutionByFlowId(String flowId) {
        return executionsByFlowId.get(flowId);
    }

    public Map<String, AuthenticationExecutionModel> getExecutionsById() {
        return executionsById;
    }

    public Map<String, RequiredActionProviderModel> getRequiredActionProviders() {
        return requiredActionProviders;
    }

    public Map<String, RequiredActionProviderModel> getRequiredActionProvidersByAlias() {
        return requiredActionProvidersByAlias;
    }

    public OTPPolicy getOtpPolicy() {
        return otpPolicy;
    }

    public WebAuthnPolicy getWebAuthnPolicy() {
        return webAuthnPolicy;
    }

    public WebAuthnPolicy getWebAuthnPasswordlessPolicy() {
        return webAuthnPasswordlessPolicy;
    }

    public AuthenticationFlowModel getBrowserFlow() {
        return browserFlow;
    }

    public AuthenticationFlowModel getRegistrationFlow() {
        return registrationFlow;
    }

    public AuthenticationFlowModel getDirectGrantFlow() {
        return directGrantFlow;
    }

    public AuthenticationFlowModel getResetCredentialsFlow() {
        return resetCredentialsFlow;
    }

    public AuthenticationFlowModel getClientAuthenticationFlow() {
        return clientAuthenticationFlow;
    }

    public AuthenticationFlowModel getDockerAuthenticationFlow() {
        return dockerAuthenticationFlow;
    }

    public AuthenticationFlowModel getFirstBrokerLoginFlow() {
        return firstBrokerLoginFlow;
    }

    public List<String> getDefaultGroups() {
        return defaultGroups;
    }

    public List<String> getDefaultDefaultClientScopes(KeycloakSession session, Supplier<RealmModel> modelSupplier) {
        return defaultDefaultClientScopes.get(session, modelSupplier);
    }

    public List<String> getOptionalDefaultClientScopes(KeycloakSession session, Supplier<RealmModel> modelSupplier) {
        return optionalDefaultClientScopes.get(session, modelSupplier);
    }

    public List<AuthenticationFlowModel> getAuthenticationFlowList() {
        return authenticationFlowList;
    }

    public List<RequiredActionProviderModel> getRequiredActionProviderList() {
        return requiredActionProviderList;
    }

    public MultivaluedMap<String, ComponentModel> getComponentsByParent() {
        return new MultivaluedHashMap<>(componentsByParent);
    }

    public MultivaluedMap<String, ComponentModel> getComponentsByParentAndType() {
        return new ConcurrentMultivaluedHashMap<>(componentsByParentAndType);
    }

    public Map<String, ComponentModel> getComponents() {
        return components;
    }

    public String getAttribute(String name) {
        return attributes != null ? attributes.get(name) : null;
    }

    public Integer getAttribute(String name, Integer defaultValue) {
        String v = getAttribute(name);
        return v != null && !v.isEmpty() ? Integer.valueOf(v) : defaultValue;
    }

    public Long getAttribute(String name, Long defaultValue) {
        String v = getAttribute(name);
        return v != null && !v.isEmpty() ? Long.valueOf(v) : defaultValue;
    }

    public Boolean getAttribute(String name, Boolean defaultValue) {
        String v = getAttribute(name);
        return v != null && !v.isEmpty() ? Boolean.valueOf(v) : defaultValue;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public boolean isAllowUserManagedAccess() {
        return allowUserManagedAccess;
    }

    public Map<String, Map<String, String>> getRealmLocalizationTexts() {
        return realmLocalizationTexts;
    }

    public Map<String, RequiredActionConfigModel> getRequiredActionProviderConfigsByAlias() {
        return requiredActionProviderConfigsByAlias;
    }

    public Map<String, RequiredActionConfigModel> getRequiredActionProviderConfigs() {
        return requiredActionProviderConfigs;
    }

    public boolean isScimApiEnabled() {
        return scimApiEnabled;
    }
}
