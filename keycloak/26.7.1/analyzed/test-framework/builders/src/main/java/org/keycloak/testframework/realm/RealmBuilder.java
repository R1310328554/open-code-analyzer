package org.keycloak.testframework.realm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.keycloak.models.jpa.entities.RealmAttributes;
import org.keycloak.representations.idm.ClientPoliciesRepresentation;
import org.keycloak.representations.idm.ClientPolicyRepresentation;
import org.keycloak.representations.idm.ClientProfileRepresentation;
import org.keycloak.representations.idm.ClientProfilesRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.ScopeMappingRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * {@link RealmRepresentation} 的流式构建器，用于在集成测试中快速组装完整领域配置
 *（客户端、用户、角色、令牌策略、WebAuthn、事件等）。
 */
public class RealmBuilder extends Builder<RealmRepresentation> {

    /** 基于已有领域表示对象构造构建器。 */
    private RealmBuilder(RealmRepresentation rep) {
        super(rep);
    }

    /** 创建默认启用的领域构建器。 */
    public static RealmBuilder create() {
        return new RealmBuilder(new RealmRepresentation()).enabled(true);
    }

    /** 基于已有领域表示对象创建更新用构建器。 */
    public static RealmBuilder update(RealmRepresentation rep) {
        return new RealmBuilder(rep);
    }

    /** 设置领域是否启用。 */
    public RealmBuilder enabled(boolean enabled) {
        rep.setEnabled(enabled);
        return this;
    }

    /** 设置领域内部 ID。 */
    public RealmBuilder id(String id) {
        rep.setId(id);
        return this;
    }

    /** 设置领域名称（realm 标识符）。 */
    public RealmBuilder name(String name) {
        rep.setRealm(name);
        return this;
    }

    /** 设置领域显示名称。 */
    public RealmBuilder displayName(String displayName) {
        rep.setDisplayName(displayName);
        return this;
    }

    @Deprecated
    /** （已弃用）设置领域公钥。 */
    public RealmBuilder publicKey(String publicKey) {
        rep.setPublicKey(publicKey);
        return this;
    }

    @Deprecated
    /** （已弃用）设置领域私钥。 */
    public RealmBuilder privateKey(String privateKey) {
        rep.setPrivateKey(privateKey);
        return this;
    }

    /** 追加客户端定义。 */
    public RealmBuilder clients(ClientRepresentation... clients) {
        rep.setClients(combine(rep.getClients(), clients));
        return this;
    }

    /** 追加客户端定义。 */
    public RealmBuilder clients(ClientBuilder... clients) {
        rep.setClients(combine(rep.getClients(), clients));
        return this;
    }

    /** 追加用户定义。 */
    public RealmBuilder users(UserRepresentation... users) {
        rep.setUsers(combine(rep.getUsers(), users));
        return this;
    }

    /** 追加用户定义。 */
    public RealmBuilder users(UserBuilder... users) {
        rep.setUsers(combine(rep.getUsers(), users));
        return this;
    }

    /** 追加组定义。 */
    public RealmBuilder groups(String... groups) {
        rep.setGroups(combine(GroupBuilder::create, rep.getGroups(), groups));
        return this;
    }

    /** 追加组定义。 */
    public RealmBuilder groups(GroupBuilder... groups) {
        rep.setGroups(combine(rep.getGroups(), groups));
        return this;
    }

    /** 设置新用户默认加入的组。 */
    public RealmBuilder defaultGroups(String... groupsNames) {
        rep.setDefaultGroups(combine(rep.getDefaultGroups(), groupsNames));
        return this;
    }

    /** 追加领域级角色（按名称）。 */
    public RealmBuilder roles(String... roleNames) {
        rep.setRoles(createIfNull(rep.getRoles(), RolesRepresentation::new));
        rep.getRoles().setRealm(combine(RoleBuilder::create, rep.getRoles().getRealm(), roleNames));
        return this;
    }

    /** 追加领域级角色。 */
    public RealmBuilder realmRoles(String... realmRoles) {
        rep.setRoles(createIfNull(rep.getRoles(), RolesRepresentation::new));
        rep.getRoles().setRealm(combine(RoleBuilder::create, rep.getRoles().getRealm(), realmRoles));
        return this;
    }

    /** 追加领域级角色。 */
    public RealmBuilder realmRoles(RoleBuilder... realmRoles) {
        rep.setRoles(createIfNull(rep.getRoles(), RolesRepresentation::new));
        rep.getRoles().setRealm(combine(rep.getRoles().getRealm(), realmRoles));
        return this;
    }

    /** 追加领域级角色。 */
    public RealmBuilder realmRoles(RoleRepresentation... realmRoles) {
        rep.setRoles(createIfNull(rep.getRoles(), RolesRepresentation::new));
        rep.getRoles().setRealm(combine(rep.getRoles().getRealm(), realmRoles));
        return this;
    }

    /** 为指定客户端追加客户端角色。 */
    public RealmBuilder clientRoles(String client, String... clientRoles) {
        rep.setRoles(createIfNull(rep.getRoles(), RolesRepresentation::new));
        rep.getRoles().setClient(combine(RoleBuilder::create, rep.getRoles().getClient(), client, clientRoles));
        return this;
    }

    /** 为指定客户端追加客户端角色。 */
    public RealmBuilder clientRoles(String client, RoleBuilder... clientRoles) {
        rep.setRoles(createIfNull(rep.getRoles(), RolesRepresentation::new));
        rep.getRoles().setClient(combine(rep.getRoles().getClient(), client, clientRoles));
        return this;
    }

    /** 追加认证流定义。 */
    public RealmBuilder authenticationFlows(AuthenticationFlowBuilder... authenticationFlows) {
        rep.setAuthenticationFlows(combine(rep.getAuthenticationFlows(), authenticationFlows));
        return this;
    }

    /** 追加身份提供者。 */
    public RealmBuilder identityProviders(IdentityProviderRepresentation... identityProviders) {
        rep.setIdentityProviders(combine(rep.getIdentityProviders(), identityProviders));
        return this;
    }

    /** 追加身份提供者。 */
    public RealmBuilder identityProviders(IdentityProviderBuilder... identityProviders) {
        rep.setIdentityProviders(combine(rep.getIdentityProviders(), identityProviders));
        return this;
    }

    /** 追加身份提供者映射器。 */
    public RealmBuilder identityProviderMappers(IdentityProviderMapperRepresentation... identityProviderMappers) {
        rep.setIdentityProviderMappers(combine(rep.getIdentityProviderMappers(), identityProviderMappers));
        return this;
    }

    /** 设置是否允许使用邮箱登录。 */
    public RealmBuilder loginWithEmailAllowed(boolean loginWithEmailAllowed) {
        rep.setLoginWithEmailAllowed(loginWithEmailAllowed);
        return this;
    }

    /** 设置注册时是否以邮箱作为用户名。 */
    public RealmBuilder registrationEmailAsUsername(boolean registrationEmailAsUsername) {
        rep.setRegistrationEmailAsUsername(registrationEmailAsUsername);
        return this;
    }

    /** 设置是否允许自助注册。 */
    public RealmBuilder registrationAllowed(boolean allowed) {
        rep.setRegistrationAllowed(allowed);
        return this;
    }

    /** 设置是否要求验证邮箱。 */
    public RealmBuilder verifyEmail(boolean verifyEmail) {
        rep.setVerifyEmail(verifyEmail);
        return this;
    }

    /** 设置是否允许用户修改用户名。 */
    public RealmBuilder editUsernameAllowed(boolean allowed) {
        rep.setEditUsernameAllowed(allowed);
        return this;
    }

    /** 设置默认签名算法。 */
    public RealmBuilder defaultSignatureAlgorithm(String algorithm) {
        rep.setDefaultSignatureAlgorithm(algorithm);
        return this;
    }

    /** 设置是否启用细粒度管理员权限。 */
    public RealmBuilder adminPermissionsEnabled(boolean enabled) {
        rep.setAdminPermissionsEnabled(enabled);
        return this;
    }

    /** 设置是否启用用户事件。 */
    public RealmBuilder eventsEnabled(boolean enabled) {
        rep.setEventsEnabled(enabled);
        return this;
    }

    /** 设置是否启用管理员事件。 */
    public RealmBuilder adminEventsEnabled(boolean enabled) {
        rep.setAdminEventsEnabled(enabled);
        return this;
    }

    /** 设置管理员事件是否包含详情。 */
    public RealmBuilder adminEventsDetailsEnabled(boolean enabled) {
        rep.setAdminEventsDetailsEnabled(enabled);
        return this;
    }

    /** 追加启用的事件类型。 */
    public RealmBuilder enabledEventTypes(String... enabledEventTypes) {
        rep.setEnabledEventTypes(combine(rep.getEnabledEventTypes(), enabledEventTypes));
        return this;
    }

    /** 直接设置启用的事件类型列表。 */
    public RealmBuilder setEnabledEventTypes(List<String> enabledEventTypes) {
        rep.setEnabledEventTypes(enabledEventTypes);
        return this;
    }

    /** 追加事件监听器。 */
    public RealmBuilder eventsListeners(String... eventListeners) {
        rep.setEventsListeners(combine(rep.getEventsListeners(), eventListeners));
        return this;
    }

    /** 移除指定事件监听器。 */
    public RealmBuilder removeEventListeners(String... eventListeners) {
        rep.setEventsListeners(removeValues(rep.getEventsListeners(), eventListeners));
        return this;
    }

    /** 添加浏览器安全响应头。 */
    public RealmBuilder browserSecurityHeader(String name, String value) {
        rep.setBrowserSecurityHeaders(createIfNull(rep.getBrowserSecurityHeaders(), HashMap::new));
        rep.getBrowserSecurityHeaders().put(name, value);
        return this;
    }

    /** 直接设置事件监听器列表。 */
    public RealmBuilder setEventsListeners(List<String> eventListeners) {
        rep.setEventsListeners(eventListeners);
        return this;
    }

    /** 设置用户事件保留时间（秒）。 */
    public RealmBuilder eventsExpiration(long eventsExpiration) {
        rep.setEventsExpiration(eventsExpiration);
        return this;
    }

    /** 设置管理员事件保留时间（秒）。 */
    public RealmBuilder adminEventsExpiration(long adminEventsExpiration) {
        return attribute(RealmAttributes.ADMIN_EVENTS_EXPIRATION, String.valueOf(adminEventsExpiration));
    }

    /** 设置是否启用国际化。 */
    public RealmBuilder internationalizationEnabled(boolean enabled) {
        rep.setInternationalizationEnabled(enabled);
        return this;
    }

    /** 追加支持的语言区域。 */
    public RealmBuilder supportedLocales(String... supportedLocales) {
        rep.setSupportedLocales(combine(rep.getSupportedLocales(), supportedLocales));
        return this;
    }

    /** 设置默认语言区域。 */
    public RealmBuilder defaultLocale(String locale) {
        rep.setDefaultLocale(locale);
        return this;
    }

    /** 配置 SMTP 邮件服务器（主机、端口、发件人）。 */
    public RealmBuilder smtp(String host, int port, String from) {
        Map<String, String> config = new HashMap<>();
        config.put("host", host);
        config.put("port", Integer.toString(port));
        config.put("from", from);
        rep.setSmtpServer(config);
        return this;
    }

    /** 设置是否启用组织功能。 */
    public RealmBuilder organizationsEnabled(boolean enabled) {
        rep.setOrganizationsEnabled(enabled);
        return this;
    }

    /** 设置是否在刷新时撤销旧 refresh token。 */
    public RealmBuilder revokeRefreshToken(boolean enabled) {
        rep.setRevokeRefreshToken(enabled);
        return this;
    }

    /** 设置 refresh token 最大复用次数。 */
    public RealmBuilder refreshTokenMaxReuse(Integer refreshTokenMaxReuse) {
        rep.setRefreshTokenMaxReuse(refreshTokenMaxReuse);
        return this;
    }

    /** 设置 access token 有效期（秒）。 */
    public RealmBuilder accessTokenLifespan(int accessTokenLifespan) {
        rep.setAccessTokenLifespan(accessTokenLifespan);
        return this;
    }

    /** 设置 SSO 会话空闲超时（秒）。 */
    public RealmBuilder ssoSessionIdleTimeout(Integer ssoSessionIdleTimeout) {
        rep.setSsoSessionIdleTimeout(ssoSessionIdleTimeout);
        return this;
    }

    /** 设置「记住我」SSO 会话空闲超时。 */
    public RealmBuilder ssoSessionIdleTimeoutRememberMe(Integer ssoSessionIdleTimeoutRememberMe) {
        rep.setSsoSessionIdleTimeoutRememberMe(ssoSessionIdleTimeoutRememberMe);
        return this;
    }

    /** 设置 SSO 会话最大生命周期。 */
    public RealmBuilder ssoSessionMaxLifespan(Integer ssoSessionMaxLifespan) {
        rep.setSsoSessionMaxLifespan(ssoSessionMaxLifespan);
        return this;
    }

    /** 设置「记住我」SSO 会话最大生命周期。 */
    public RealmBuilder ssoSessionMaxLifespanRememberMe(Integer ssoSessionMaxLifespanRememberMe) {
        rep.setSsoSessionMaxLifespanRememberMe(ssoSessionMaxLifespanRememberMe);
        return this;
    }

    /** 设置客户端会话最大生命周期。 */
    public RealmBuilder clientSessionMaxLifespan(Integer clientSessionMaxLifespan) {
        rep.setClientSessionMaxLifespan(clientSessionMaxLifespan);
        return this;
    }

    /** 设置离线会话空闲超时。 */
    public RealmBuilder offlineSessionIdleTimeout(int offlineSessionIdleTimeout) {
        rep.setOfflineSessionIdleTimeout(offlineSessionIdleTimeout);
        return this;
    }

    /** 设置离线会话最大生命周期。 */
    public RealmBuilder offlineSessionMaxLifespan(int offlineSessionMaxLifespan) {
        rep.setOfflineSessionMaxLifespan(offlineSessionMaxLifespan);
        return this;
    }

    /** 设置是否启用离线会话最大生命周期限制。 */
    public RealmBuilder offlineSessionMaxLifespanEnabled(boolean offlineSessionMaxLifespanEnabled) {
        rep.setOfflineSessionMaxLifespanEnabled(offlineSessionMaxLifespanEnabled);
        return this;
    }

    /** 设置授权码有效期。 */
    public RealmBuilder accessCodeLifespan(int accessCodeLifespan) {
        rep.setAccessCodeLifespan(accessCodeLifespan);
        return this;
    }

    /** 设置用户操作相关授权码有效期。 */
    public RealmBuilder accessCodeLifespanUserAction(int accessCodeLifespanUserAction) {
        rep.setAccessCodeLifespanUserAction(accessCodeLifespanUserAction);
        return this;
    }

    /** 设置客户端会话空闲超时。 */
    public RealmBuilder clientSessionIdleTimeout(Integer clientSessionIdleTimeout) {
        rep.setClientSessionIdleTimeout(clientSessionIdleTimeout);
        return this;
    }

    /** 设置客户端会话最大生命周期。 */
    public RealmBuilder clientSessionMaxLifespan(int clientSessionMaxLifespan) {
        rep.setClientSessionMaxLifespan(clientSessionMaxLifespan);
        return this;
    }

    /** 设置客户端离线会话空闲超时。 */
    public RealmBuilder clientOfflineSessionIdleTimeout(int clientOfflineSessionIdleTimeout) {
        rep.setClientOfflineSessionIdleTimeout(clientOfflineSessionIdleTimeout);
        return this;
    }

    /** 设置客户端离线会话最大生命周期。 */
    public RealmBuilder clientOfflineSessionMaxLifespan(int clientOfflineSessionMaxLifespan) {
        rep.setClientOfflineSessionMaxLifespan(clientOfflineSessionMaxLifespan);
        return this;
    }

    /** 设置领域 not-before 时间戳。 */
    public RealmBuilder notBefore(int i) {
        rep.setNotBefore(i);
        return this;
    }

    /** 设置 OTP 位数策略。 */
    public RealmBuilder otpDigits(int i) {
        rep.setOtpPolicyDigits(i);
        return this;
    }

    /** 设置 TOTP 时间步长（秒）。 */
    public RealmBuilder otpPeriod(int i) {
        rep.setOtpPolicyPeriod(i);
        return this;
    }

    /** 设置 OTP 类型（totp/hotp）。 */
    public RealmBuilder otpType(String type) {
        rep.setOtpPolicyType(type);
        return this;
    }

    /** 设置 OTP HMAC 算法。 */
    public RealmBuilder otpAlgorithm(String algorithm) {
        rep.setOtpPolicyAlgorithm(algorithm);
        return this;
    }

    /** 设置 HOTP 初始计数器。 */
    public RealmBuilder otpInitialCounter(int i) {
        rep.setOtpPolicyInitialCounter(i);
        return this;
    }

    /** 设置 OTP 前瞻窗口。 */
    public RealmBuilder otpLookAheadWindow(int i) {
        rep.setOtpPolicyLookAheadWindow(i);
        return this;
    }

    /** 设置密码策略表达式。 */
    public RealmBuilder passwordPolicy(String passwordPolicy) {
        rep.setPasswordPolicy(passwordPolicy);
        return this;
    }

    /** 设置是否启用暴力破解防护。 */
    public RealmBuilder bruteForceProtected(boolean enabled) {
        rep.setBruteForceProtected(enabled);
        return this;
    }

    /** 设置锁定前允许的最大失败次数。 */
    public RealmBuilder failureFactor(int count) {
        rep.setFailureFactor(count);
        return this;
    }

    /** 设置是否允许重复邮箱。 */
    public RealmBuilder duplicateEmailsAllowed(boolean allowed) {
        rep.setDuplicateEmailsAllowed(allowed);
        return this;
    }

    /** 设置 SSL 要求级别（external/none 等）。 */
    public RealmBuilder sslRequired(String sslRequired) {
        rep.setSslRequired(sslRequired);
        return this;
    }

    /** 设置是否启用「记住我」。 */
    public RealmBuilder setRememberMe(boolean enabled) {
        rep.setRememberMe(enabled);
        return this;
    }

    /** 设置是否允许重置密码。 */
    public RealmBuilder resetPasswordAllowed(boolean allowed) {
        rep.setResetPasswordAllowed(allowed);
        return this;
    }

    /** 清空已解析的客户端策略。 */
    public RealmBuilder resetClientPolicies() {
        rep.setParsedClientPolicies(null);
        return this;
    }

    /** 追加客户端策略定义。 */
    public RealmBuilder clientPolicy(ClientPolicyRepresentation clientPolicyRep) {
        ClientPoliciesRepresentation clientPolicies = rep.getParsedClientPolicies();
        if (clientPolicies == null) {
            clientPolicies = new ClientPoliciesRepresentation();
        }
        List<ClientPolicyRepresentation> policies = clientPolicies.getPolicies();
        policies.add(clientPolicyRep);
        rep.setParsedClientPolicies(clientPolicies);
        return this;
    }

    /** 清空已解析的客户端配置档。 */
    public RealmBuilder resetClientProfiles() {
        rep.setParsedClientProfiles(null);
        return this;
    }

    /** 追加客户端配置档定义。 */
    public RealmBuilder clientProfile(ClientProfileRepresentation clientProfileRep) {
        ClientProfilesRepresentation clientProfiles = rep.getParsedClientProfiles();
        if (clientProfiles == null) {
            clientProfiles = new ClientProfilesRepresentation();
        }
        List<ClientProfileRepresentation> profiles = clientProfiles.getProfiles();
        profiles.add(clientProfileRep);
        rep.setParsedClientProfiles(clientProfiles);
        return this;
    }

    /** 设置浏览器认证流别名。 */
    public RealmBuilder browserFlow(String browserFlow) {
        rep.setBrowserFlow(browserFlow);
        return this;
    }

    /** 追加必需操作提供者。 */
    public RealmBuilder requiredActions(RequiredActionProviderRepresentation... requiredActions) {
        rep.setRequiredActions(combine(rep.getRequiredActions(), requiredActions));
        return this;
    }

    /** 设置是否启用可验证凭据。 */
    public RealmBuilder verifiableCredentialsEnabled(boolean enabled) {
        rep.setVerifiableCredentialsEnabled(enabled);
        return this;
    }

    /** 设置 WebAuthn 签名算法列表。 */
    public RealmBuilder webAuthnPolicySignatureAlgorithms(List<String> algorithms) {
        rep.setWebAuthnPolicySignatureAlgorithms(algorithms);
        return this;
    }

    /** 设置 WebAuthn 证明传达偏好。 */
    public RealmBuilder webAuthnPolicyAttestationConveyancePreference(String preference) {
        rep.setWebAuthnPolicyAttestationConveyancePreference(preference);
        return this;
    }

    /** 设置 WebAuthn 认证器附着方式。 */
    public RealmBuilder webAuthnPolicyAuthenticatorAttachment(String attachment) {
        rep.setWebAuthnPolicyAuthenticatorAttachment(attachment);
        return this;
    }

    /**
     * @deprecated Use {@link #webAuthnPolicyResidentKey(String)} instead.
     */
    @Deprecated
    public RealmBuilder webAuthnPolicyRequireResidentKey(String requireResidentKey) {
        rep.setWebAuthnPolicyRequireResidentKey(requireResidentKey);
        return this;
    }

    /** 设置 WebAuthn 常驻密钥策略。 */
    public RealmBuilder webAuthnPolicyResidentKey(String residentKey) {
        rep.setWebAuthnPolicyResidentKey(residentKey);
        return this;
    }

    /** 设置 WebAuthn 用户验证要求。 */
    public RealmBuilder webAuthnPolicyUserVerificationRequirement(String requirement) {
        rep.setWebAuthnPolicyUserVerificationRequirement(requirement);
        return this;
    }

    /** 设置 WebAuthn 依赖方实体名称。 */
    public RealmBuilder webAuthnPolicyRpEntityName(String entityName) {
        rep.setWebAuthnPolicyRpEntityName(entityName);
        return this;
    }

    /** 设置 WebAuthn 依赖方 ID。 */
    public RealmBuilder webAuthnPolicyRpId(String rpId) {
        rep.setWebAuthnPolicyRpId(rpId);
        return this;
    }

    /** 设置 WebAuthn 注册超时（毫秒）。 */
    public RealmBuilder webAuthnPolicyCreateTimeout(Integer timeout) {
        rep.setWebAuthnPolicyCreateTimeout(timeout);
        return this;
    }

    /** 设置是否避免重复注册同一认证器。 */
    public RealmBuilder webAuthnPolicyAvoidSameAuthenticatorRegister(Boolean register) {
        rep.setWebAuthnPolicyAvoidSameAuthenticatorRegister(register);
        return this;
    }

    /** 设置无密码 WebAuthn 签名算法。 */
    public RealmBuilder webAuthnPolicyPasswordlessSignatureAlgorithms(List<String> algorithms) {
        rep.setWebAuthnPolicySignatureAlgorithms(algorithms);
        return this;
    }

    /** 设置无密码 WebAuthn 证明传达偏好。 */
    public RealmBuilder webAuthnPolicyPasswordlessAttestationConveyancePreference(String preference) {
        rep.setWebAuthnPolicyPasswordlessAttestationConveyancePreference(preference);
        return this;
    }

    /** 设置无密码 WebAuthn 认证器附着方式。 */
    public RealmBuilder webAuthnPolicyPasswordlessAuthenticatorAttachment(String attachment) {
        rep.setWebAuthnPolicyPasswordlessAuthenticatorAttachment(attachment);
        return this;
    }

    /**
     * @deprecated Use {@link #webAuthnPolicyPasswordlessResidentKey(String)} instead.
     */
    @Deprecated
    public RealmBuilder webAuthnPolicyPasswordlessRequireResidentKey(String requireResidentKey) {
        rep.setWebAuthnPolicyPasswordlessRequireResidentKey(requireResidentKey);
        return this;
    }

    /** 设置无密码 WebAuthn 常驻密钥策略。 */
    public RealmBuilder webAuthnPolicyPasswordlessResidentKey(String residentKey) {
        rep.setWebAuthnPolicyPasswordlessResidentKey(residentKey);
        return this;
    }

    /** 设置无密码 WebAuthn 用户验证要求。 */
    public RealmBuilder webAuthnPolicyPasswordlessUserVerificationRequirement(String requirement) {
        rep.setWebAuthnPolicyPasswordlessUserVerificationRequirement(requirement);
        return this;
    }

    /** 设置无密码 WebAuthn 依赖方实体名称。 */
    public RealmBuilder webAuthnPolicyPasswordlessRpEntityName(String entityName) {
        rep.setWebAuthnPolicyPasswordlessRpEntityName(entityName);
        return this;
    }

    /** 设置无密码 WebAuthn 注册超时。 */
    public RealmBuilder webAuthnPolicyPasswordlessCreateTimeout(Integer timeout) {
        rep.setWebAuthnPolicyPasswordlessCreateTimeout(timeout);
        return this;
    }

    /** 设置无密码 WebAuthn 是否避免重复注册。 */
    public RealmBuilder webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister(Boolean register) {
        rep.setWebAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister(register);
        return this;
    }

    /** 设置是否启用 Passkeys。 */
    public RealmBuilder webAuthnPolicyPasswordlessPasskeysEnabled(Boolean enabled) {
        rep.setWebAuthnPolicyPasswordlessPasskeysEnabled(enabled);
        return this;
    }

    /** 设置无密码 WebAuthn mediation 策略。 */
    public RealmBuilder webAuthnPolicyPasswordlessMediation(String mediation) {
        rep.setWebAuthnPolicyPasswordlessMediation(mediation);
        return this;
    }

    /** 设置可接受的 WebAuthn AAGUID 列表。 */
    public RealmBuilder webAuthnPolicyAcceptableAaguids(List<String> aaguids) {
        rep.setWebAuthnPolicyAcceptableAaguids(aaguids);
        return this;
    }

    /** 设置是否启用 SCIM API。 */
    public RealmBuilder scimEnabled(boolean enabled) {
        rep.setScimApiEnabled(enabled);
        return this;
    }

    /** 添加领域自定义属性。 */
    public RealmBuilder attribute(String key, String value) {
        rep.setAttributes(createIfNull(rep.getAttributes(), HashMap::new));
        rep.getAttributes().put(key, value);
        return this;
    }

    /** 合并 {@link RealmAttributesBuilder} 构建的属性。 */
    public RealmBuilder attributes(RealmAttributesBuilder attributes) {
        combineMap(rep.getAttributes(), attributes.build());
        return this;
    }

    /** 追加客户端作用域。 */
    public RealmBuilder clientScopes(ClientScopeRepresentation... clientScopes) {
        rep.setClientScopes(combine(rep.getClientScopes(), clientScopes));
        return this;
    }

    /** 追加客户端作用域。 */
    public RealmBuilder clientScopes(ClientScopeBuilder... clientScopes) {
        rep.setClientScopes(combine(rep.getClientScopes(), clientScopes));
        return this;
    }

    /** 为客户端作用域追加领域角色映射。 */
    public RealmBuilder addClientScopeRealmRoleMapping(String clientScopeName, String... roleNames) {
        ScopeMappingRepresentation mapping = rep.clientScopeScopeMapping(clientScopeName);
        for (String roleName : roleNames) {
            mapping.role(roleName);
        }
        return this;
    }

    /** 为指定客户端的作用域追加客户端角色映射。 */
    public RealmBuilder addClientScopeClientRoleMapping(String clientName, String clientScopeName, String... roleNames) {
        ScopeMappingRepresentation mapping = new ScopeMappingRepresentation();
        mapping.setClientScope(clientScopeName);
        for (String roleName : roleNames) {
            mapping.role(roleName);
        }
        Map<String, List<ScopeMappingRepresentation>> mappings = rep.getClientScopeMappings();
        if (mappings == null) {
            mappings = new HashMap<>();
            rep.setClientScopeMappings(mappings);
        }
        mappings.computeIfAbsent(clientName, k -> new LinkedList<>()).add(mapping);
        return this;
    }

    /** 设置账户控制台主题。 */
    public RealmBuilder accountTheme(String accountTheme) {
        rep.setAccountTheme(accountTheme);
        return this;
    }

    /** 设置是否永久锁定（暴力破解）。 */
    public RealmBuilder permanentLockout(boolean permanentLockout) {
        rep.setPermanentLockout(permanentLockout);
        return this;
    }

    /** 设置最大临时锁定次数。 */
    public RealmBuilder maxTemporaryLockouts(int maxTemporaryLockouts) {
        rep.setMaxTemporaryLockouts(maxTemporaryLockouts);
        return this;
    }

    /** 设置暴力破解检测时间窗口（秒）。 */
    public RealmBuilder maxDeltaTimeSeconds(int maxDeltaTimeSeconds) {
        rep.setMaxDeltaTimeSeconds(maxDeltaTimeSeconds);
        return this;
    }

    /**
     * 配置领域时优先使用其他便捷方法；框架演进期间若缺少对应 API，可通过此方法直接修改底层表示。
     *
     * @param update 自定义更新函数
     * @return this
     * @deprecated
     */
    public RealmBuilder update(RealmUpdate... update) {
        Arrays.stream(update).forEach(u -> u.update(rep));
        return this;
    }

    public RealmRepresentation build() {
        return rep;
    }

    /** 对 {@link RealmRepresentation} 执行任意更新的函数式接口。 */
    public interface RealmUpdate {

        /** 应用更新到领域表示对象。 */
        void update(RealmRepresentation realm);

    }

}
