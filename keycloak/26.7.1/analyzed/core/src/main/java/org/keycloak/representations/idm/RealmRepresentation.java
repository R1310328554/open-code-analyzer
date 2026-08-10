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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.logging.Logger;

/**
 * Keycloak 域（Realm）的完整 Admin REST API 表示，涵盖令牌生命周期、认证流、
 * 安全策略、主题、事件、联邦、WebAuthn 与组织等全部域级配置。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RealmRepresentation {

    /** 日志记录器。 */

    private static final Logger logger= Logger.getLogger(RealmRepresentation.class);

    /** 域内部 UUID。 */

    protected String id;
    /** 域名称（realm name）。 */
    protected String realm;
    /** 域显示名称。 */
    protected String displayName;
    /** 域 HTML 格式显示名称。 */
    protected String displayNameHtml;
    /** 令牌全局生效起始时间（not-before）。 */
    protected Integer notBefore;
    /** 默认 JWT 签名算法。 */
    protected String defaultSignatureAlgorithm;
    /** 是否在刷新时撤销旧 refresh token。 */
    protected Boolean revokeRefreshToken;
    /** refresh token 最大复用次数。 */
    protected Integer refreshTokenMaxReuse;
    /** access token 有效期（秒）。 */
    protected Integer accessTokenLifespan;
    /** 隐式流 access token 有效期（秒）。 */
    protected Integer accessTokenLifespanForImplicitFlow;
    /** SSO 会话空闲超时（秒）。 */
    protected Integer ssoSessionIdleTimeout;
    /** SSO 会话最大存活时间（秒）。 */
    protected Integer ssoSessionMaxLifespan;
    /** Remember Me 下 SSO 空闲超时（秒）。 */
    protected Integer ssoSessionIdleTimeoutRememberMe;
    /** Remember Me 下 SSO 最大存活（秒）。 */
    protected Integer ssoSessionMaxLifespanRememberMe;
    /** 离线会话空闲超时（秒）。 */
    protected Integer offlineSessionIdleTimeout;
    // KEYCLOAK-7688：离线 Token 的离线会话最大存活配置
    /** 是否启用离线会话最大存活限制。 */
    protected Boolean offlineSessionMaxLifespanEnabled;
    /** 离线会话最大存活时间（秒）。 */
    protected Integer offlineSessionMaxLifespan;
    /** 客户端会话空闲超时（秒）。 */
    protected Integer clientSessionIdleTimeout;
    /** 客户端会话最大存活（秒）。 */
    protected Integer clientSessionMaxLifespan;
    /** 客户端离线会话空闲超时（秒）。 */
    protected Integer clientOfflineSessionIdleTimeout;
    /** 客户端离线会话最大存活（秒）。 */
    protected Integer clientOfflineSessionMaxLifespan;
    /** 授权码有效期（秒）。 */
    protected Integer accessCodeLifespan;
    /** 用户操作相关授权码有效期（秒）。 */
    protected Integer accessCodeLifespanUserAction;
    /** 登录流程授权码有效期（秒）。 */
    protected Integer accessCodeLifespanLogin;
    /** 管理员生成的操作令牌有效期（秒）。 */
    protected Integer actionTokenGeneratedByAdminLifespan;
    /** 用户生成的操作令牌有效期（秒）。 */
    protected Integer actionTokenGeneratedByUserLifespan;
    /** OAuth2 设备码有效期（秒）。 */
    protected Integer oauth2DeviceCodeLifespan;
    /** OAuth2 设备授权轮询间隔（秒）。 */
    protected Integer oauth2DevicePollingInterval;
    /** 域是否启用。 */
    protected Boolean enabled;
    /** SSL 要求级别（external/all/none）。 */
    protected String sslRequired;
    /** 已弃用：是否允许密码凭据授权。 */
    @Deprecated
    protected Boolean passwordCredentialGrantAllowed;
    /** 是否允许自助注册。 */
    protected Boolean registrationAllowed;
    /** 注册时是否以邮箱作为用户名。 */
    protected Boolean registrationEmailAsUsername;
    /** 是否显示 Remember Me 选项。 */
    protected Boolean rememberMe;
    /** 是否要求验证邮箱。 */
    protected Boolean verifyEmail;
    /** 是否允许以邮箱登录。 */
    protected Boolean loginWithEmailAllowed;
    /** 是否允许重复邮箱。 */
    protected Boolean duplicateEmailsAllowed;
    /** 是否允许重置密码。 */
    protected Boolean resetPasswordAllowed;
    /** 是否允许用户修改用户名。 */
    protected Boolean editUsernameAllowed;

    /** 已弃用：是否启用用户缓存。 */

    @Deprecated
    protected Boolean userCacheEnabled;
    /** 已弃用：是否启用域缓存。 */
    @Deprecated
    protected Boolean realmCacheEnabled;

    // --- 暴力破解防护设置
    /** 是否启用暴力破解防护。 */
    protected Boolean bruteForceProtected;
    /** 是否永久锁定账户。 */
    protected Boolean permanentLockout;
    /** 最大临时锁定次数。 */
    protected Integer maxTemporaryLockouts;
    /** 暴力破解等待时间递增策略。 */
    protected BruteForceStrategy bruteForceStrategy;
    /** 最大失败等待时间（秒）。 */
    protected Integer maxFailureWaitSeconds;
    /** 快速连续登录最小等待（秒）。 */
    protected Integer minimumQuickLoginWaitSeconds;
    /** 等待时间递增量（秒）。 */
    protected Integer waitIncrementSeconds;
    /** 快速登录检测窗口（毫秒）。 */
    protected Long quickLoginCheckMilliSeconds;
    /** 失败计数重置窗口（秒）。 */
    protected Integer maxDeltaTimeSeconds;
    /** 触发锁定的连续失败次数因子。 */
    protected Integer failureFactor;
    /** 二次认证最大失败次数。 */
    protected Integer maxSecondaryAuthFailures;
    // --- 暴力破解防护设置结束

    /** 已弃用：域私钥 PEM。 */

    @Deprecated
    protected String privateKey;
    /** 已弃用：域公钥 PEM。 */
    @Deprecated
    protected String publicKey;
    /** 已弃用：域证书 PEM。 */
    @Deprecated
    protected String certificate;
    /** 已弃用：授权码 HMAC 密钥。 */
    @Deprecated
    protected String codeSecret;
    /** 域与客户端角色集合。 */
    protected RolesRepresentation roles;
    /** 域内组列表。 */
    protected List<GroupRepresentation> groups;
    /** 已弃用：默认角色名称列表。 */
    @Deprecated
    protected List<String> defaultRoles;
    /** 新用户的默认复合角色。 */
    protected RoleRepresentation defaultRole;
    /** 管理权限客户端表示。 */
    protected ClientRepresentation adminPermissionsClient;
    /** 新用户自动加入的默认组。 */
    protected List<String> defaultGroups;
    /** 已弃用：必需凭据类型集合。 */
    @Deprecated
    protected Set<String> requiredCredentials;
    /** 密码策略配置字符串。 */
    protected String passwordPolicy;
    /** OTP 策略类型（totp/hotp）。 */
    protected String otpPolicyType;
    /** OTP 哈希算法。 */
    protected String otpPolicyAlgorithm;
    /** HOTP 初始计数器值。 */
    protected Integer otpPolicyInitialCounter;
    /** OTP 位数。 */
    protected Integer otpPolicyDigits;
    /** OTP 前瞻窗口大小。 */
    protected Integer otpPolicyLookAheadWindow;
    /** TOTP 时间步长（秒）。 */
    protected Integer otpPolicyPeriod;
    /** OTP 码是否可复用。 */
    protected Boolean otpPolicyCodeReusable;
    /** 支持的 OTP 应用名称列表。 */
    protected List<String> otpSupportedApplications;
    /** 国际化文本（语言 → 键值对）。 */
    protected Map<String, Map<String, String>> localizationTexts;

    // --- WebAuthn 双因素认证策略 ---

    /** WebAuthn 双因素：RP 显示名称。 */

    protected String webAuthnPolicyRpEntityName;
    /** WebAuthn 双因素：允许的签名算法。 */
    protected List<String> webAuthnPolicySignatureAlgorithms;
    /** WebAuthn 双因素：RP ID。 */
    protected String webAuthnPolicyRpId;
    /** WebAuthn 双因素：认证传递偏好。 */
    protected String webAuthnPolicyAttestationConveyancePreference;
    /** WebAuthn 双因素：认证器附加类型。 */
    protected String webAuthnPolicyAuthenticatorAttachment;
    /** WebAuthn 双因素：已弃用，是否要求 resident key。 */
    protected String webAuthnPolicyRequireResidentKey;
    /** WebAuthn 双因素：resident key 要求级别。 */
    protected String webAuthnPolicyResidentKey;
    /** WebAuthn 双因素：用户验证要求。 */
    protected String webAuthnPolicyUserVerificationRequirement;
    /** WebAuthn 双因素：注册超时（毫秒）。 */
    protected Integer webAuthnPolicyCreateTimeout;
    /** WebAuthn 双因素：是否禁止重复注册同一认证器。 */
    protected Boolean webAuthnPolicyAvoidSameAuthenticatorRegister;
    /** WebAuthn 双因素：允许的 AAGUID 白名单。 */
    protected List<String> webAuthnPolicyAcceptableAaguids;
    /** WebAuthn 双因素：额外允许的 Origin。 */
    protected List<String> webAuthnPolicyExtraOrigins;

    // --- WebAuthn 无密码认证策略 ---

    /** WebAuthn 无密码：RP 显示名称。 */

    protected String webAuthnPolicyPasswordlessRpEntityName;
    /** WebAuthn 无密码：签名算法列表。 */
    protected List<String> webAuthnPolicyPasswordlessSignatureAlgorithms;
    /** WebAuthn 无密码：RP ID。 */
    protected String webAuthnPolicyPasswordlessRpId;
    /** WebAuthn 无密码：认证传递偏好。 */
    protected String webAuthnPolicyPasswordlessAttestationConveyancePreference;
    /** WebAuthn 无密码：认证器附加类型。 */
    protected String webAuthnPolicyPasswordlessAuthenticatorAttachment;
    /** WebAuthn 无密码：已弃用，resident key 要求。 */
    protected String webAuthnPolicyPasswordlessRequireResidentKey;
    /** WebAuthn 无密码：resident key 要求级别。 */
    protected String webAuthnPolicyPasswordlessResidentKey;
    /** WebAuthn 无密码：用户验证要求。 */
    protected String webAuthnPolicyPasswordlessUserVerificationRequirement;
    /** WebAuthn 无密码：注册超时（毫秒）。 */
    protected Integer webAuthnPolicyPasswordlessCreateTimeout;
    /** WebAuthn 无密码：是否禁止重复注册。 */
    protected Boolean webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister;
    /** WebAuthn 无密码：AAGUID 白名单。 */
    protected List<String> webAuthnPolicyPasswordlessAcceptableAaguids;
    /** WebAuthn 无密码：额外 Origin。 */
    protected List<String> webAuthnPolicyPasswordlessExtraOrigins;
    /** WebAuthn 无密码：是否启用 Passkeys。 */
    protected Boolean webAuthnPolicyPasswordlessPasskeysEnabled;
    /** WebAuthn 无密码：条件 UI 调解模式。 */
    protected String webAuthnPolicyPasswordlessMediation;

    // --- Client Policy / Profile 配置 ---

    /** Client Profile 配置（JSON 节点）。 */

    @JsonProperty("clientProfiles")
    @Schema(implementation = ClientProfilesRepresentation.class)
    protected JsonNode clientProfiles;

    /** Client Policy 配置（JSON 节点）。 */

    @JsonProperty("clientPolicies")
    @Schema(implementation = ClientPoliciesRepresentation.class)
    protected JsonNode clientPolicies;

    /** 域内用户列表（导入/导出用）。 */

    protected List<UserRepresentation> users;
    /** 联邦用户列表。 */
    protected List<UserRepresentation> federatedUsers;
    /** Scope 到角色的映射列表。 */
    protected List<ScopeMappingRepresentation> scopeMappings;
    /** 按客户端分组的 Scope 映射。 */
    protected Map<String, List<ScopeMappingRepresentation>> clientScopeMappings;
    /** 域内 OAuth/OIDC 客户端列表。 */
    protected List<ClientRepresentation> clients;
    /** 客户端 Scope 定义列表。 */
    protected List<ClientScopeRepresentation> clientScopes;
    /** 新客户端默认绑定的 Scope 名称。 */
    protected List<String> defaultDefaultClientScopes;
    /** 新客户端默认可选 Scope 名称。 */
    protected List<String> defaultOptionalClientScopes;
    /** 浏览器安全响应头配置。 */
    protected Map<String, String> browserSecurityHeaders;
    /** SMTP 邮件服务器配置。 */
    protected Map<String, String> smtpServer;
    /** 用户联邦提供者列表。 */
    protected List<UserFederationProviderRepresentation> userFederationProviders;
    /** 用户联邦映射器列表。 */
    protected List<UserFederationMapperRepresentation> userFederationMappers;
    /** 登录页主题名称。 */
    protected String loginTheme;
    /** 账户管理页主题名称。 */
    protected String accountTheme;
    /** 管理控制台主题名称。 */
    protected String adminTheme;
    /** 邮件模板主题名称。 */
    protected String emailTheme;

    /** 是否启用用户事件。 */

    protected Boolean eventsEnabled;
    /** 用户事件保留时长（秒）。 */
    protected Long eventsExpiration;
    /** 事件监听器 SPI ID 列表。 */
    protected List<String> eventsListeners;
    /** 启用的用户事件类型。 */
    protected List<String> enabledEventTypes;

    /** 是否启用管理事件。 */

    protected Boolean adminEventsEnabled;
    /** 管理事件是否记录详情。 */
    protected Boolean adminEventsDetailsEnabled;

    /** 身份提供者（IdP）列表。 */

    private List<IdentityProviderRepresentation> identityProviders;
    /** IdP 属性映射器列表。 */
    private List<IdentityProviderMapperRepresentation> identityProviderMappers;
    /** 域级协议映射器列表。 */
    private List<ProtocolMapperRepresentation> protocolMappers;
    /** 可导出组件（按类型分组）。 */
    private MultivaluedHashMap<String, ComponentExportRepresentation> components;
    /** 是否启用国际化。 */
    protected Boolean internationalizationEnabled;
    /** 支持的语言区域集合。 */
    protected Set<String> supportedLocales;
    /** 默认语言区域。 */
    protected String defaultLocale;
    /** 认证流定义列表。 */
    protected List<AuthenticationFlowRepresentation> authenticationFlows;
    /** 认证器配置列表。 */
    protected List<AuthenticatorConfigRepresentation> authenticatorConfig;
    /** Required Action 提供者列表。 */
    protected List<RequiredActionProviderRepresentation> requiredActions;
    /** 浏览器登录绑定的认证流别名。 */
    protected String browserFlow;
    /** 注册绑定的认证流别名。 */
    protected String registrationFlow;
    /** Direct Grant 绑定的认证流别名。 */
    protected String directGrantFlow;
    /** 重置凭据绑定的认证流别名。 */
    protected String resetCredentialsFlow;
    /** 客户端认证绑定的认证流别名。 */
    protected String clientAuthenticationFlow;
    /** Docker 认证绑定的认证流别名。 */
    protected String dockerAuthenticationFlow;
    /** 首次 Broker 登录绑定的认证流别名。 */
    protected String firstBrokerLoginFlow;

    /** 域自定义属性键值对。 */

    protected Map<String, String> attributes;

    /** 导出时 Keycloak 版本号。 */

    protected String keycloakVersion;

    /** 是否允许用户托管访问（UMA）。 */

    protected Boolean userManagedAccessAllowed;

    /** 是否启用组织功能。 */

    protected Boolean organizationsEnabled;
    /** 域内组织列表。 */
    private List<OrganizationRepresentation> organizations;

    /** 是否启用可验证凭据功能。 */

    protected Boolean verifiableCredentialsEnabled;

    /** 是否启用细粒度管理权限。 */

    protected Boolean adminPermissionsEnabled;

    /** 已弃用：是否启用社交登录。 */

    @Deprecated
    protected Boolean social;
    /** 已弃用：首次社交登录是否更新资料。 */
    @Deprecated
    protected Boolean updateProfileOnInitialSocialLogin;
    /** 已弃用：社交 IdP 配置映射。 */
    @Deprecated
    protected Map<String, String> socialProviders;
    /** 已弃用：应用 Scope 映射。 */
    @Deprecated
    protected Map<String, List<ScopeMappingRepresentation>> applicationScopeMappings;
    /** 已弃用：应用列表。 */
    @Deprecated
    protected List<ApplicationRepresentation> applications;
    /** 已弃用：OAuth 客户端列表。 */
    @Deprecated
    protected List<OAuthClientRepresentation> oauthClients;
    /** 已弃用：客户端模板列表。 */
    @Deprecated
    protected List<ClientTemplateRepresentation> clientTemplates;

    /** 是否启用 SCIM API。 */

    private Boolean scimApiEnabled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayNameHtml() {
        return displayNameHtml;
    }

    public void setDisplayNameHtml(String displayNameHtml) {
        this.displayNameHtml = displayNameHtml;
    }

    public List<UserRepresentation> getUsers() {
        return users;
    }

    public List<ApplicationRepresentation> getApplications() {
        return applications;
    }

    public void setUsers(List<UserRepresentation> users) {
        this.users = users;
    }

    /** 便捷方法：创建并添加指定用户名的用户表示。 */
    public UserRepresentation user(String username) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        if (users == null) users = new ArrayList<>();
        users.add(user);
        return user;
    }

    public List<ClientRepresentation> getClients() {
        return clients;
    }

    public void setClients(List<ClientRepresentation> clients) {
        this.clients = clients;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getSslRequired() {
        return sslRequired;
    }

    public void setSslRequired(String sslRequired) {
        this.sslRequired = sslRequired;
    }

    public String getDefaultSignatureAlgorithm() {
        return defaultSignatureAlgorithm;
    }

    public void setDefaultSignatureAlgorithm(String defaultSignatureAlgorithm) {
        this.defaultSignatureAlgorithm = defaultSignatureAlgorithm;
    }

    public Boolean getRevokeRefreshToken() {
        return revokeRefreshToken;
    }

    public void setRevokeRefreshToken(Boolean revokeRefreshToken) {
        this.revokeRefreshToken = revokeRefreshToken;
    }

    public Integer getRefreshTokenMaxReuse() {
        return refreshTokenMaxReuse;
    }

    public void setRefreshTokenMaxReuse(Integer refreshTokenMaxReuse) {
        this.refreshTokenMaxReuse = refreshTokenMaxReuse;
    }

    public Integer getAccessTokenLifespan() {
        return accessTokenLifespan;
    }

    public void setAccessTokenLifespan(Integer accessTokenLifespan) {
        this.accessTokenLifespan = accessTokenLifespan;
    }

    public Integer getAccessTokenLifespanForImplicitFlow() {
        return accessTokenLifespanForImplicitFlow;
    }

    public void setAccessTokenLifespanForImplicitFlow(Integer accessTokenLifespanForImplicitFlow) {
        this.accessTokenLifespanForImplicitFlow = accessTokenLifespanForImplicitFlow;
    }

    public Integer getSsoSessionIdleTimeout() {
        return ssoSessionIdleTimeout;
    }

    public void setSsoSessionIdleTimeout(Integer ssoSessionIdleTimeout) {
        this.ssoSessionIdleTimeout = ssoSessionIdleTimeout;
    }

    public Integer getSsoSessionMaxLifespan() {
        return ssoSessionMaxLifespan;
    }

    public void setSsoSessionMaxLifespan(Integer ssoSessionMaxLifespan) {
        this.ssoSessionMaxLifespan = ssoSessionMaxLifespan;
    }

    public Integer getSsoSessionMaxLifespanRememberMe() {
        return ssoSessionMaxLifespanRememberMe;
    }

    public void setSsoSessionMaxLifespanRememberMe(Integer ssoSessionMaxLifespanRememberMe) {
        this.ssoSessionMaxLifespanRememberMe = ssoSessionMaxLifespanRememberMe;
    }

    public Integer getSsoSessionIdleTimeoutRememberMe() {
        return ssoSessionIdleTimeoutRememberMe;
    }

    public void setSsoSessionIdleTimeoutRememberMe(Integer ssoSessionIdleTimeoutRememberMe) {
        this.ssoSessionIdleTimeoutRememberMe = ssoSessionIdleTimeoutRememberMe;
    }

    public Integer getOfflineSessionIdleTimeout() {
        return offlineSessionIdleTimeout;
    }

    public void setOfflineSessionIdleTimeout(Integer offlineSessionIdleTimeout) {
        this.offlineSessionIdleTimeout = offlineSessionIdleTimeout;
    }

    // KEYCLOAK-7688：离线 Token 的离线会话最大存活配置
    public Boolean getOfflineSessionMaxLifespanEnabled() {
        return offlineSessionMaxLifespanEnabled;
    }

    public void setOfflineSessionMaxLifespanEnabled(Boolean offlineSessionMaxLifespanEnabled) {
        this.offlineSessionMaxLifespanEnabled = offlineSessionMaxLifespanEnabled;
    }

    public Integer getOfflineSessionMaxLifespan() {
        return offlineSessionMaxLifespan;
    }

    public void setOfflineSessionMaxLifespan(Integer offlineSessionMaxLifespan) {
        this.offlineSessionMaxLifespan = offlineSessionMaxLifespan;
    }

    public Integer getClientSessionIdleTimeout() {
        return clientSessionIdleTimeout;
    }

    public void setClientSessionIdleTimeout(Integer clientSessionIdleTimeout) {
        this.clientSessionIdleTimeout = clientSessionIdleTimeout;
    }

    public Integer getClientSessionMaxLifespan() {
        return clientSessionMaxLifespan;
    }

    public void setClientSessionMaxLifespan(Integer clientSessionMaxLifespan) {
        this.clientSessionMaxLifespan = clientSessionMaxLifespan;
    }

    public Integer getClientOfflineSessionIdleTimeout() {
        return clientOfflineSessionIdleTimeout;
    }

    public void setClientOfflineSessionIdleTimeout(Integer clientOfflineSessionIdleTimeout) {
        this.clientOfflineSessionIdleTimeout = clientOfflineSessionIdleTimeout;
    }

    public Integer getClientOfflineSessionMaxLifespan() {
        return clientOfflineSessionMaxLifespan;
    }

    public void setClientOfflineSessionMaxLifespan(Integer clientOfflineSessionMaxLifespan) {
        this.clientOfflineSessionMaxLifespan = clientOfflineSessionMaxLifespan;
    }

    public List<ScopeMappingRepresentation> getScopeMappings() {
        return scopeMappings;
    }

    /** 便捷方法：为客户端创建 Scope 映射条目。 */
    public ScopeMappingRepresentation clientScopeMapping(String clientName) {
        ScopeMappingRepresentation mapping = new ScopeMappingRepresentation();
        mapping.setClient(clientName);
        if (scopeMappings == null) scopeMappings = new ArrayList<>();
        scopeMappings.add(mapping);
        return mapping;
    }

    /** 便捷方法：为 Client Scope 创建 Scope 映射条目。 */
    public ScopeMappingRepresentation clientScopeScopeMapping(String clientScopeName) {
        ScopeMappingRepresentation mapping = new ScopeMappingRepresentation();
        mapping.setClientScope(clientScopeName);
        if (scopeMappings == null) scopeMappings = new ArrayList<>();
        scopeMappings.add(mapping);
        return mapping;
    }

    @Deprecated
    public Set<String> getRequiredCredentials() {
        return requiredCredentials;
    }
    @Deprecated
    public void setRequiredCredentials(Set<String> requiredCredentials) {
        this.requiredCredentials = requiredCredentials;
    }

    public String getPasswordPolicy() {
        return passwordPolicy;
    }

    public void setPasswordPolicy(String passwordPolicy) {
        this.passwordPolicy = passwordPolicy;
    }

    public Integer getAccessCodeLifespan() {
        return accessCodeLifespan;
    }

    public void setAccessCodeLifespan(Integer accessCodeLifespan) {
        this.accessCodeLifespan = accessCodeLifespan;
    }

    public Integer getAccessCodeLifespanUserAction() {
        return accessCodeLifespanUserAction;
    }

    public void setAccessCodeLifespanUserAction(Integer accessCodeLifespanUserAction) {
        this.accessCodeLifespanUserAction = accessCodeLifespanUserAction;
    }

    public Integer getAccessCodeLifespanLogin() {
        return accessCodeLifespanLogin;
    }

    public void setAccessCodeLifespanLogin(Integer accessCodeLifespanLogin) {
        this.accessCodeLifespanLogin = accessCodeLifespanLogin;
    }

    public Integer getActionTokenGeneratedByAdminLifespan() {
        return actionTokenGeneratedByAdminLifespan;
    }

    public void setActionTokenGeneratedByAdminLifespan(Integer actionTokenGeneratedByAdminLifespan) {
        this.actionTokenGeneratedByAdminLifespan = actionTokenGeneratedByAdminLifespan;
    }

    public void setOAuth2DeviceCodeLifespan(Integer oauth2DeviceCodeLifespan) {
        this.oauth2DeviceCodeLifespan = oauth2DeviceCodeLifespan;
    }

    @Schema(name = "oauth2DeviceCodeLifespan")
    public Integer getOAuth2DeviceCodeLifespan() {
        return oauth2DeviceCodeLifespan;
    }

    public void setOAuth2DevicePollingInterval(Integer oauth2DevicePollingInterval) {
        this.oauth2DevicePollingInterval = oauth2DevicePollingInterval;
    }

    @Schema(name = "oauth2DevicePollingInterval")
    public Integer getOAuth2DevicePollingInterval() {
        return oauth2DevicePollingInterval;
    }

    public Integer getActionTokenGeneratedByUserLifespan() {
        return actionTokenGeneratedByUserLifespan;
    }

    public void setActionTokenGeneratedByUserLifespan(Integer actionTokenGeneratedByUserLifespan) {
        this.actionTokenGeneratedByUserLifespan = actionTokenGeneratedByUserLifespan;
    }

    @Deprecated
    public List<String> getDefaultRoles() {
        return defaultRoles;
    }

    @Deprecated
    public void setDefaultRoles(List<String> defaultRoles) {
        this.defaultRoles = defaultRoles;
    }

    public RoleRepresentation getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(RoleRepresentation defaultRole) {
        this.defaultRole = defaultRole;
    }

    public ClientRepresentation getAdminPermissionsClient() {
        return adminPermissionsClient;
    }

    public void setAdminPermissionsClient(ClientRepresentation adminPermissionsClient) {
        this.adminPermissionsClient = adminPermissionsClient;
    }

    public List<String> getDefaultGroups() {
        return defaultGroups;
    }

    public void setDefaultGroups(List<String> defaultGroups) {
        this.defaultGroups = defaultGroups;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getCodeSecret() {
        return codeSecret;
    }

    public void setCodeSecret(String codeSecret) {
        this.codeSecret = codeSecret;
    }

    public Boolean isPasswordCredentialGrantAllowed() {
        return passwordCredentialGrantAllowed;
    }

    public Boolean isRegistrationAllowed() {
        return registrationAllowed;
    }

    public void setRegistrationAllowed(Boolean registrationAllowed) {
        this.registrationAllowed = registrationAllowed;
    }

    public Boolean isRegistrationEmailAsUsername() {
        return registrationEmailAsUsername;
    }

    public void setRegistrationEmailAsUsername(Boolean registrationEmailAsUsername) {
        this.registrationEmailAsUsername = registrationEmailAsUsername;
    }

    public Boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public Boolean isVerifyEmail() {
        return verifyEmail;
    }

    public void setVerifyEmail(Boolean verifyEmail) {
        this.verifyEmail = verifyEmail;
    }

    public Boolean isLoginWithEmailAllowed() {
        return loginWithEmailAllowed;
    }

    public void setLoginWithEmailAllowed(Boolean loginWithEmailAllowed) {
        this.loginWithEmailAllowed = loginWithEmailAllowed;
    }

    public Boolean isDuplicateEmailsAllowed() {
        return duplicateEmailsAllowed;
    }

    public void setDuplicateEmailsAllowed(Boolean duplicateEmailsAllowed) {
        this.duplicateEmailsAllowed = duplicateEmailsAllowed;
    }

    public Boolean isResetPasswordAllowed() {
        return resetPasswordAllowed;
    }

    public void setResetPasswordAllowed(Boolean resetPassword) {
        this.resetPasswordAllowed = resetPassword;
    }

    public Boolean isEditUsernameAllowed() {
        return editUsernameAllowed;
    }

    public void setEditUsernameAllowed(Boolean editUsernameAllowed) {
        this.editUsernameAllowed = editUsernameAllowed;
    }

    @Deprecated
    public Boolean isSocial() {
        return social;
    }

    @Deprecated
    public Boolean isUpdateProfileOnInitialSocialLogin() {
        return updateProfileOnInitialSocialLogin;
    }

    public Map<String, String> getBrowserSecurityHeaders() {
        return browserSecurityHeaders;
    }

    public void setBrowserSecurityHeaders(Map<String, String> browserSecurityHeaders) {
        this.browserSecurityHeaders = browserSecurityHeaders;
    }

    @Deprecated
    public Map<String, String> getSocialProviders() {
        return socialProviders;
    }

    public Map<String, String> getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(Map<String, String> smtpServer) {
        this.smtpServer = smtpServer;
    }

    @Deprecated
    public List<OAuthClientRepresentation> getOauthClients() {
        return oauthClients;
    }

    public Map<String, List<ScopeMappingRepresentation>> getClientScopeMappings() {
        return clientScopeMappings;
    }

    public void setClientScopeMappings(Map<String, List<ScopeMappingRepresentation>> clientScopeMappings) {
        this.clientScopeMappings = clientScopeMappings;
    }

    @Deprecated
    public Map<String, List<ScopeMappingRepresentation>> getApplicationScopeMappings() {
        return applicationScopeMappings;
    }

    public RolesRepresentation getRoles() {
        return roles;
    }

    public void setRoles(RolesRepresentation roles) {
        this.roles = roles;
    }

    public String getLoginTheme() {
        return loginTheme;
    }

    public void setLoginTheme(String loginTheme) {
        this.loginTheme = loginTheme;
    }

    public String getAccountTheme() {
        return accountTheme;
    }

    public void setAccountTheme(String accountTheme) {
        this.accountTheme = accountTheme;
    }

    public String getAdminTheme() {
        return adminTheme;
    }

    public void setAdminTheme(String adminTheme) {
        this.adminTheme = adminTheme;
    }

    public String getEmailTheme() {
        return emailTheme;
    }

    public void setEmailTheme(String emailTheme) {
        this.emailTheme = emailTheme;
    }

    public Integer getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Integer notBefore) {
        this.notBefore = notBefore;
    }

    public Boolean isBruteForceProtected() {
        return bruteForceProtected;
    }

    public void setBruteForceProtected(Boolean bruteForceProtected) {
        this.bruteForceProtected = bruteForceProtected;
    }

    public Boolean isPermanentLockout() {
        return permanentLockout;
    }

    public void setPermanentLockout(Boolean permanentLockout) {
        this.permanentLockout = permanentLockout;
    }

    public Integer getMaxTemporaryLockouts() {
        return maxTemporaryLockouts;
    }

    public void setMaxTemporaryLockouts(Integer maxTemporaryLockouts) {
        this.maxTemporaryLockouts = maxTemporaryLockouts;
    }

    public BruteForceStrategy getBruteForceStrategy() {
        return this.bruteForceStrategy;
    }

    public void setBruteForceStrategy(BruteForceStrategy bruteForceStrategy) {
        this.bruteForceStrategy = bruteForceStrategy;
    }

    public Integer getMaxFailureWaitSeconds() {
        return maxFailureWaitSeconds;
    }

    public void setMaxFailureWaitSeconds(Integer maxFailureWaitSeconds) {
        this.maxFailureWaitSeconds = maxFailureWaitSeconds;
    }

    public Integer getMinimumQuickLoginWaitSeconds() {
        return minimumQuickLoginWaitSeconds;
    }

    public void setMinimumQuickLoginWaitSeconds(Integer minimumQuickLoginWaitSeconds) {
        this.minimumQuickLoginWaitSeconds = minimumQuickLoginWaitSeconds;
    }

    public Integer getWaitIncrementSeconds() {
        return waitIncrementSeconds;
    }

    public void setWaitIncrementSeconds(Integer waitIncrementSeconds) {
        this.waitIncrementSeconds = waitIncrementSeconds;
    }

    public Long getQuickLoginCheckMilliSeconds() {
        return quickLoginCheckMilliSeconds;
    }

    public void setQuickLoginCheckMilliSeconds(Long quickLoginCheckMilliSeconds) {
        this.quickLoginCheckMilliSeconds = quickLoginCheckMilliSeconds;
    }

    public Integer getMaxDeltaTimeSeconds() {
        return maxDeltaTimeSeconds;
    }

    public void setMaxDeltaTimeSeconds(Integer maxDeltaTimeSeconds) {
        this.maxDeltaTimeSeconds = maxDeltaTimeSeconds;
    }

    public Integer getFailureFactor() {
        return failureFactor;
    }

    public void setFailureFactor(Integer failureFactor) {
        this.failureFactor = failureFactor;
    }

    public Integer getMaxSecondaryAuthFailures() {
        return maxSecondaryAuthFailures;
    }

    public void setMaxSecondaryAuthFailures(Integer maxSecondaryAuthFailures) {
        this.maxSecondaryAuthFailures = maxSecondaryAuthFailures;
    }

    public Boolean isEventsEnabled() {
        return eventsEnabled;
    }

    public void setEventsEnabled(boolean eventsEnabled) {
        this.eventsEnabled = eventsEnabled;
    }

    public Long getEventsExpiration() {
        return eventsExpiration;
    }

    public void setEventsExpiration(long eventsExpiration) {
        this.eventsExpiration = eventsExpiration;
    }

    public List<String> getEventsListeners() {
        return eventsListeners;
    }

    public void setEventsListeners(List<String> eventsListeners) {
        this.eventsListeners = eventsListeners;
    }

    public List<String> getEnabledEventTypes() {
        return enabledEventTypes;
    }

    public void setEnabledEventTypes(List<String> enabledEventTypes) {
        this.enabledEventTypes = enabledEventTypes;
    }

    public Boolean isAdminEventsEnabled() {
        return adminEventsEnabled;
    }

    public void setAdminEventsEnabled(Boolean adminEventsEnabled) {
        this.adminEventsEnabled = adminEventsEnabled;
    }

    public Boolean isAdminEventsDetailsEnabled() {
        return adminEventsDetailsEnabled;
    }

    public void setAdminEventsDetailsEnabled(Boolean adminEventsDetailsEnabled) {
        this.adminEventsDetailsEnabled = adminEventsDetailsEnabled;
    }

    public List<UserFederationProviderRepresentation> getUserFederationProviders() {
        return userFederationProviders;
    }

    public void setUserFederationProviders(List<UserFederationProviderRepresentation> userFederationProviders) {
        this.userFederationProviders = userFederationProviders;
    }

    public List<UserFederationMapperRepresentation> getUserFederationMappers() {
        return userFederationMappers;
    }

    public void setUserFederationMappers(List<UserFederationMapperRepresentation> userFederationMappers) {
        this.userFederationMappers = userFederationMappers;
    }

    public void addUserFederationMapper(UserFederationMapperRepresentation userFederationMapper) {
        if (userFederationMappers == null) userFederationMappers = new LinkedList<>();
        userFederationMappers.add(userFederationMapper);
    }

    public List<IdentityProviderRepresentation> getIdentityProviders() {
        return identityProviders;
    }

    public void setIdentityProviders(List<IdentityProviderRepresentation> identityProviders) {
        this.identityProviders = identityProviders;
    }

    public void addIdentityProvider(IdentityProviderRepresentation identityProviderRepresentation) {
        if (identityProviders == null) identityProviders = new LinkedList<>();
        identityProviders.add(identityProviderRepresentation);
    }

    public List<ProtocolMapperRepresentation> getProtocolMappers() {
        return protocolMappers;
    }

    public void addProtocolMapper(ProtocolMapperRepresentation rep) {
        if (protocolMappers == null) protocolMappers = new LinkedList<ProtocolMapperRepresentation>();
        protocolMappers.add(rep);
    }

    public void setProtocolMappers(List<ProtocolMapperRepresentation> protocolMappers) {
        this.protocolMappers = protocolMappers;
    }

    public Boolean isInternationalizationEnabled() {
        return internationalizationEnabled;
    }

    public void setInternationalizationEnabled(Boolean internationalizationEnabled) {
        this.internationalizationEnabled = internationalizationEnabled;
    }

    public Set<String> getSupportedLocales() {
        return supportedLocales;
    }

    public void addSupportedLocales(String locale) {
        if(supportedLocales == null){
            supportedLocales = new HashSet<>();
        }
        supportedLocales.add(locale);
    }

    public void setSupportedLocales(Set<String> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public List<IdentityProviderMapperRepresentation> getIdentityProviderMappers() {
        return identityProviderMappers;
    }

    public void setIdentityProviderMappers(List<IdentityProviderMapperRepresentation> identityProviderMappers) {
        this.identityProviderMappers = identityProviderMappers;
    }

    public void addIdentityProviderMapper(IdentityProviderMapperRepresentation rep) {
        if (identityProviderMappers == null) identityProviderMappers = new LinkedList<>();
        identityProviderMappers.add(rep);
    }

    public List<AuthenticationFlowRepresentation> getAuthenticationFlows() {
        return authenticationFlows;
    }

    public void setAuthenticationFlows(List<AuthenticationFlowRepresentation> authenticationFlows) {
        this.authenticationFlows = authenticationFlows;
    }

    public List<AuthenticatorConfigRepresentation> getAuthenticatorConfig() {
        return authenticatorConfig;
    }

    public void setAuthenticatorConfig(List<AuthenticatorConfigRepresentation> authenticatorConfig) {
        this.authenticatorConfig = authenticatorConfig;
    }

    public List<RequiredActionProviderRepresentation> getRequiredActions() {
        return requiredActions;
    }

    public void setRequiredActions(List<RequiredActionProviderRepresentation> requiredActions) {
        this.requiredActions = requiredActions;
    }

    public String getOtpPolicyType() {
        return otpPolicyType;
    }

    public void setOtpPolicyType(String otpPolicyType) {
        this.otpPolicyType = otpPolicyType;
    }

    public String getOtpPolicyAlgorithm() {
        return otpPolicyAlgorithm;
    }

    public void setOtpPolicyAlgorithm(String otpPolicyAlgorithm) {
        this.otpPolicyAlgorithm = otpPolicyAlgorithm;
    }

    public Integer getOtpPolicyInitialCounter() {
        return otpPolicyInitialCounter;
    }

    public void setOtpPolicyInitialCounter(Integer otpPolicyInitialCounter) {
        this.otpPolicyInitialCounter = otpPolicyInitialCounter;
    }

    public Integer getOtpPolicyDigits() {
        return otpPolicyDigits;
    }

    public void setOtpPolicyDigits(Integer otpPolicyDigits) {
        this.otpPolicyDigits = otpPolicyDigits;
    }

    public Integer getOtpPolicyLookAheadWindow() {
        return otpPolicyLookAheadWindow;
    }

    public void setOtpPolicyLookAheadWindow(Integer otpPolicyLookAheadWindow) {
        this.otpPolicyLookAheadWindow = otpPolicyLookAheadWindow;
    }

    public Integer getOtpPolicyPeriod() {
        return otpPolicyPeriod;
    }

    public void setOtpPolicyPeriod(Integer otpPolicyPeriod) {
        this.otpPolicyPeriod = otpPolicyPeriod;
    }

    public List<String> getOtpSupportedApplications() {
        return otpSupportedApplications;
    }

    public void setOtpSupportedApplications(List<String> otpSupportedApplications) {
        this.otpSupportedApplications = otpSupportedApplications;
    }

    public Map<String, Map<String, String>> getLocalizationTexts() {
        return localizationTexts;
    }

    public void setLocalizationTexts(Map<String, Map<String, String>> localizationTexts) {
        this.localizationTexts = localizationTexts;
    }

    public Boolean isOtpPolicyCodeReusable() {
        return otpPolicyCodeReusable;
    }

    public void setOtpPolicyCodeReusable(Boolean isCodeReusable) {
        this.otpPolicyCodeReusable = isCodeReusable;
    }

    // --- WebAuthn 双因素认证策略 ---

    public String getWebAuthnPolicyRpEntityName() {
        return webAuthnPolicyRpEntityName;
    }

    public void setWebAuthnPolicyRpEntityName(String webAuthnPolicyRpEntityName) {
        this.webAuthnPolicyRpEntityName = webAuthnPolicyRpEntityName;
    }

    public List<String> getWebAuthnPolicySignatureAlgorithms() {
        return webAuthnPolicySignatureAlgorithms;
    }

    public void setWebAuthnPolicySignatureAlgorithms(List<String> webAuthnPolicySignatureAlgorithms) {
        this.webAuthnPolicySignatureAlgorithms = webAuthnPolicySignatureAlgorithms;
    }

    public String getWebAuthnPolicyRpId() {
        return webAuthnPolicyRpId;
    }

    public void setWebAuthnPolicyRpId(String webAuthnPolicyRpId) {
        this.webAuthnPolicyRpId = webAuthnPolicyRpId;
    }

    public String getWebAuthnPolicyAttestationConveyancePreference() {
        return webAuthnPolicyAttestationConveyancePreference;
    }

    public void setWebAuthnPolicyAttestationConveyancePreference(String webAuthnPolicyAttestationConveyancePreference) {
        this.webAuthnPolicyAttestationConveyancePreference = webAuthnPolicyAttestationConveyancePreference;
    }

    public String getWebAuthnPolicyAuthenticatorAttachment() {
        return webAuthnPolicyAuthenticatorAttachment;
    }

    public void setWebAuthnPolicyAuthenticatorAttachment(String webAuthnPolicyAuthenticatorAttachment) {
        this.webAuthnPolicyAuthenticatorAttachment = webAuthnPolicyAuthenticatorAttachment;
    }

    /**
     * @deprecated Use {@link #getWebAuthnPolicyResidentKey()} instead. Planned to be removed in the future.
     */
    @Deprecated
    public String getWebAuthnPolicyRequireResidentKey() {
        return webAuthnPolicyRequireResidentKey;
    }

    /**
     * @deprecated Use {@link #setWebAuthnPolicyResidentKey(String)} instead. Planned to be removed in the future.
     */
    @Deprecated
    public void setWebAuthnPolicyRequireResidentKey(String webAuthnPolicyRequireResidentKey) {
        this.webAuthnPolicyRequireResidentKey = webAuthnPolicyRequireResidentKey;
    }

    public String getWebAuthnPolicyResidentKey() {
        return webAuthnPolicyResidentKey;
    }

    public void setWebAuthnPolicyResidentKey(String webAuthnPolicyResidentKey) {
        this.webAuthnPolicyResidentKey = webAuthnPolicyResidentKey;
    }

    public String getWebAuthnPolicyUserVerificationRequirement() {
        return webAuthnPolicyUserVerificationRequirement;
    }

    public void setWebAuthnPolicyUserVerificationRequirement(String webAuthnPolicyUserVerificationRequirement) {
        this.webAuthnPolicyUserVerificationRequirement = webAuthnPolicyUserVerificationRequirement;
    }

    public Integer getWebAuthnPolicyCreateTimeout() {
        return webAuthnPolicyCreateTimeout;
    }

    public void setWebAuthnPolicyCreateTimeout(Integer webAuthnPolicyCreateTimeout) {
        this.webAuthnPolicyCreateTimeout = webAuthnPolicyCreateTimeout;
    }

    public Boolean isWebAuthnPolicyAvoidSameAuthenticatorRegister() {
        return webAuthnPolicyAvoidSameAuthenticatorRegister;
    }

    public void setWebAuthnPolicyAvoidSameAuthenticatorRegister(Boolean webAuthnPolicyAvoidSameAuthenticatorRegister) {
        this.webAuthnPolicyAvoidSameAuthenticatorRegister = webAuthnPolicyAvoidSameAuthenticatorRegister;
    }

    public List<String> getWebAuthnPolicyAcceptableAaguids() {
        return webAuthnPolicyAcceptableAaguids;
    }

    public void setWebAuthnPolicyAcceptableAaguids(List<String> webAuthnPolicyAcceptableAaguids) {
        this.webAuthnPolicyAcceptableAaguids = webAuthnPolicyAcceptableAaguids;
    }

    public List<String> getWebAuthnPolicyExtraOrigins(){
        return webAuthnPolicyExtraOrigins;
    }

    public void setWebAuthnPolicyExtraOrigins(List<String> extraOrigins) {
        this.webAuthnPolicyExtraOrigins = extraOrigins;
    }

    // --- WebAuthn 无密码认证策略 ---


    public String getWebAuthnPolicyPasswordlessRpEntityName() {
        return webAuthnPolicyPasswordlessRpEntityName;
    }

    public void setWebAuthnPolicyPasswordlessRpEntityName(String webAuthnPolicyPasswordlessRpEntityName) {
        this.webAuthnPolicyPasswordlessRpEntityName = webAuthnPolicyPasswordlessRpEntityName;
    }

    public List<String> getWebAuthnPolicyPasswordlessSignatureAlgorithms() {
        return webAuthnPolicyPasswordlessSignatureAlgorithms;
    }

    public void setWebAuthnPolicyPasswordlessSignatureAlgorithms(List<String> webAuthnPolicyPasswordlessSignatureAlgorithms) {
        this.webAuthnPolicyPasswordlessSignatureAlgorithms = webAuthnPolicyPasswordlessSignatureAlgorithms;
    }

    public String getWebAuthnPolicyPasswordlessRpId() {
        return webAuthnPolicyPasswordlessRpId;
    }

    public void setWebAuthnPolicyPasswordlessRpId(String webAuthnPolicyPasswordlessRpId) {
        this.webAuthnPolicyPasswordlessRpId = webAuthnPolicyPasswordlessRpId;
    }

    public String getWebAuthnPolicyPasswordlessAttestationConveyancePreference() {
        return webAuthnPolicyPasswordlessAttestationConveyancePreference;
    }

    public void setWebAuthnPolicyPasswordlessAttestationConveyancePreference(String webAuthnPolicyPasswordlessAttestationConveyancePreference) {
        this.webAuthnPolicyPasswordlessAttestationConveyancePreference = webAuthnPolicyPasswordlessAttestationConveyancePreference;
    }

    public String getWebAuthnPolicyPasswordlessAuthenticatorAttachment() {
        return webAuthnPolicyPasswordlessAuthenticatorAttachment;
    }

    public void setWebAuthnPolicyPasswordlessAuthenticatorAttachment(String webAuthnPolicyPasswordlessAuthenticatorAttachment) {
        this.webAuthnPolicyPasswordlessAuthenticatorAttachment = webAuthnPolicyPasswordlessAuthenticatorAttachment;
    }

    /**
     * @deprecated Use {@link #getWebAuthnPolicyPasswordlessResidentKey()} instead. Planned to be removed in the future.
     */
    @Deprecated
    public String getWebAuthnPolicyPasswordlessRequireResidentKey() {
        return webAuthnPolicyPasswordlessRequireResidentKey;
    }

    /**
     * @deprecated Use {@link #setWebAuthnPolicyPasswordlessResidentKey(String)} instead. Planned to be removed in the future.
     */
    @Deprecated
    public void setWebAuthnPolicyPasswordlessRequireResidentKey(String webAuthnPolicyPasswordlessRequireResidentKey) {
        this.webAuthnPolicyPasswordlessRequireResidentKey = webAuthnPolicyPasswordlessRequireResidentKey;
    }

    public String getWebAuthnPolicyPasswordlessResidentKey() {
        return webAuthnPolicyPasswordlessResidentKey;
    }

    public void setWebAuthnPolicyPasswordlessResidentKey(String webAuthnPolicyPasswordlessResidentKey) {
        this.webAuthnPolicyPasswordlessResidentKey = webAuthnPolicyPasswordlessResidentKey;
    }

    public String getWebAuthnPolicyPasswordlessUserVerificationRequirement() {
        return webAuthnPolicyPasswordlessUserVerificationRequirement;
    }

    public void setWebAuthnPolicyPasswordlessUserVerificationRequirement(String webAuthnPolicyPasswordlessUserVerificationRequirement) {
        this.webAuthnPolicyPasswordlessUserVerificationRequirement = webAuthnPolicyPasswordlessUserVerificationRequirement;
    }

    public Integer getWebAuthnPolicyPasswordlessCreateTimeout() {
        return webAuthnPolicyPasswordlessCreateTimeout;
    }

    public void setWebAuthnPolicyPasswordlessCreateTimeout(Integer webAuthnPolicyPasswordlessCreateTimeout) {
        this.webAuthnPolicyPasswordlessCreateTimeout = webAuthnPolicyPasswordlessCreateTimeout;
    }

    public Boolean isWebAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister() {
        return webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister;
    }

    public void setWebAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister(Boolean webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister) {
        this.webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister = webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister;
    }

    public List<String> getWebAuthnPolicyPasswordlessAcceptableAaguids() {
        return webAuthnPolicyPasswordlessAcceptableAaguids;
    }

    public void setWebAuthnPolicyPasswordlessAcceptableAaguids(List<String> webAuthnPolicyPasswordlessAcceptableAaguids) {
        this.webAuthnPolicyPasswordlessAcceptableAaguids = webAuthnPolicyPasswordlessAcceptableAaguids;
    }

    public List<String> getWebAuthnPolicyPasswordlessExtraOrigins(){
        return webAuthnPolicyPasswordlessExtraOrigins;
    }

    public void setWebAuthnPolicyPasswordlessExtraOrigins(List<String> extraOrigins) {
        this.webAuthnPolicyPasswordlessExtraOrigins = extraOrigins;
    }

    public Boolean getWebAuthnPolicyPasswordlessPasskeysEnabled(){
        return webAuthnPolicyPasswordlessPasskeysEnabled;
    }

    public void setWebAuthnPolicyPasswordlessPasskeysEnabled(Boolean webAuthnPolicyPasswordlessPasskeysEnabled) {
        this.webAuthnPolicyPasswordlessPasskeysEnabled = webAuthnPolicyPasswordlessPasskeysEnabled;
    }

    public String getWebAuthnPolicyPasswordlessMediation() {
        return webAuthnPolicyPasswordlessMediation;
    }

    public void setWebAuthnPolicyPasswordlessMediation(String webAuthnPolicyPasswordlessMediation) {
        this.webAuthnPolicyPasswordlessMediation = webAuthnPolicyPasswordlessMediation;
    }

    // --- Client Policy / Profile 配置 ---

    @JsonIgnore
    public ClientProfilesRepresentation getParsedClientProfiles() {
        try {
            if (clientProfiles == null) return null;
            return JsonSerialization.mapper.convertValue(clientProfiles, ClientProfilesRepresentation.class);
        } catch (IllegalArgumentException ioe) {
            logger.warnf("Failed to deserialize client profiles in the realm %s. Fallback to return empty profiles. Details: %s", realm, ioe.getMessage());
            return null;
        }
    }

    @JsonIgnore
    public void setParsedClientProfiles(ClientProfilesRepresentation clientProfiles) {
        if (clientProfiles == null) {
            this.clientProfiles = null;
            return;
        }
        this.clientProfiles = JsonSerialization.mapper.convertValue(clientProfiles, JsonNode.class);
    }

    @JsonIgnore
    public ClientPoliciesRepresentation getParsedClientPolicies() {
        try {
            if (clientPolicies == null) return null;
            return JsonSerialization.mapper.convertValue(clientPolicies, ClientPoliciesRepresentation.class);
        } catch (IllegalArgumentException ioe) {
            logger.warnf("Failed to deserialize client policies in the realm %s. Fallback to return empty profiles. Details: %s", realm, ioe.getMessage());
            return null;
        }
    }

    @JsonIgnore
    public void setParsedClientPolicies(ClientPoliciesRepresentation clientPolicies) {
        if (clientPolicies == null) {
            this.clientPolicies = null;
            return;
        }
        this.clientPolicies = JsonSerialization.mapper.convertValue(clientPolicies, JsonNode.class);
    }

    public String getBrowserFlow() {
        return browserFlow;
    }

    public void setBrowserFlow(String browserFlow) {
        this.browserFlow = browserFlow;
    }

    public String getRegistrationFlow() {
        return registrationFlow;
    }

    public void setRegistrationFlow(String registrationFlow) {
        this.registrationFlow = registrationFlow;
    }

    public String getDirectGrantFlow() {
        return directGrantFlow;
    }

    public void setDirectGrantFlow(String directGrantFlow) {
        this.directGrantFlow = directGrantFlow;
    }

    public String getResetCredentialsFlow() {
        return resetCredentialsFlow;
    }

    public void setResetCredentialsFlow(String resetCredentialsFlow) {
        this.resetCredentialsFlow = resetCredentialsFlow;
    }

    public String getClientAuthenticationFlow() {
        return clientAuthenticationFlow;
    }

    public void setClientAuthenticationFlow(String clientAuthenticationFlow) {
        this.clientAuthenticationFlow = clientAuthenticationFlow;
    }

    public String getDockerAuthenticationFlow() {
        return dockerAuthenticationFlow;
    }

    public RealmRepresentation setDockerAuthenticationFlow(final String dockerAuthenticationFlow) {
        this.dockerAuthenticationFlow = dockerAuthenticationFlow;
        return this;
    }

    public String getFirstBrokerLoginFlow() {
        return firstBrokerLoginFlow;
    }

    public RealmRepresentation setFirstBrokerLoginFlow(String firstBrokerLoginFlow) {
        this.firstBrokerLoginFlow = firstBrokerLoginFlow;
        return this;
    }

    public String getKeycloakVersion() {
        return keycloakVersion;
    }

    public void setKeycloakVersion(String keycloakVersion) {
        this.keycloakVersion = keycloakVersion;
    }

    public List<GroupRepresentation> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupRepresentation> groups) {
        this.groups = groups;
    }

    @Deprecated // use getClientScopes() instead
    public List<ClientTemplateRepresentation> getClientTemplates() {
        return clientTemplates;
    }

    public List<ClientScopeRepresentation> getClientScopes() {
        return clientScopes;
    }

    public void setClientScopes(List<ClientScopeRepresentation> clientScopes) {
        this.clientScopes = clientScopes;
    }

    public List<String> getDefaultDefaultClientScopes() {
        return defaultDefaultClientScopes;
    }

    public void setDefaultDefaultClientScopes(List<String> defaultDefaultClientScopes) {
        this.defaultDefaultClientScopes = defaultDefaultClientScopes;
    }

    public List<String> getDefaultOptionalClientScopes() {
        return defaultOptionalClientScopes;
    }

    public void setDefaultOptionalClientScopes(List<String> defaultOptionalClientScopes) {
        this.defaultOptionalClientScopes = defaultOptionalClientScopes;
    }

    public MultivaluedHashMap<String, ComponentExportRepresentation> getComponents() {
        return components;
    }

    public void setComponents(MultivaluedHashMap<String, ComponentExportRepresentation> components) {
        this.components = components;
    }

    /** 是否已配置至少一个身份提供者（联邦已启用）。 */
    @JsonIgnore
    public boolean isIdentityFederationEnabled() {
        return identityProviders != null && !identityProviders.isEmpty();
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public List<UserRepresentation> getFederatedUsers() {
        return federatedUsers;
    }

    public void setFederatedUsers(List<UserRepresentation> federatedUsers) {
        this.federatedUsers = federatedUsers;
    }

    public void setUserManagedAccessAllowed(Boolean userManagedAccessAllowed) {
        this.userManagedAccessAllowed = userManagedAccessAllowed;
    }

    public Boolean isUserManagedAccessAllowed() {
        return userManagedAccessAllowed;
    }

    public Boolean isOrganizationsEnabled() {
        return organizationsEnabled;
    }

    public void setOrganizationsEnabled(Boolean organizationsEnabled) {
        this.organizationsEnabled = organizationsEnabled;
    }

    public Boolean isAdminPermissionsEnabled() {
        return adminPermissionsEnabled;
    }

    public void setAdminPermissionsEnabled(Boolean adminPermissionsEnabled) {
        this.adminPermissionsEnabled = adminPermissionsEnabled;
    }

    public Boolean isVerifiableCredentialsEnabled() {
        return verifiableCredentialsEnabled;
    }

    public void setVerifiableCredentialsEnabled(Boolean verifiableCredentialsEnabled) {
        this.verifiableCredentialsEnabled = verifiableCredentialsEnabled;
    }

    /** 返回域属性映射，未配置时返回空映射而非 null。 */
    @JsonIgnore
    public Map<String, String> getAttributesOrEmpty() {
        return (Map<String, String>) (attributes == null ? Collections.emptyMap() : attributes);
    }

    public List<OrganizationRepresentation> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrganizationRepresentation> organizations) {
        this.organizations = organizations;
    }

    /** 向域添加组织表示。 */
    public void addOrganization(OrganizationRepresentation org) {
        if (organizations == null) {
            organizations = new ArrayList<>();
        }
        organizations.add(org);
    }

    public void setScimApiEnabled(Boolean scimApiEnabled) {
        this.scimApiEnabled = scimApiEnabled;
    }

    public Boolean isScimApiEnabled() {
        return scimApiEnabled;
    }

    /** 暴力破解锁定后等待时间递增策略。 */
    public enum BruteForceStrategy {
        /** 线性递增等待时间。 */
        LINEAR,
        /** 按失败次数倍数递增。 */
        MULTIPLE;
    }
}
