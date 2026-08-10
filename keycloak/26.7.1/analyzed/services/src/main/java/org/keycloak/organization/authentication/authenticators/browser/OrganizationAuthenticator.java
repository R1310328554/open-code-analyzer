/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.organization.authentication.authenticators.browser;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.WebAuthnConstants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.FlowStatus;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.authentication.authenticators.browser.IdentityProviderAuthenticator;
import org.keycloak.authentication.authenticators.browser.WebAuthnConditionalUIAuthenticator;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.AuthenticationContextBean;
import org.keycloak.forms.login.freemarker.model.IdentityProviderBean;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationDomainModel;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.OrganizationModel.IdentityProviderRedirectMode;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.forms.login.freemarker.model.OrganizationAwareAuthenticationContextBean;
import org.keycloak.organization.forms.login.freemarker.model.OrganizationAwareIdentityProviderBean;
import org.keycloak.organization.forms.login.freemarker.model.OrganizationAwareRealmBean;
import org.keycloak.organization.protocol.mappers.oidc.OrganizationScope;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.Booleans;

import static org.keycloak.authentication.AuthenticatorUtil.isSSOAuthentication;
import static org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator.USER_SET_BEFORE_USERNAME_PASSWORD_AUTH;
import static org.keycloak.models.OrganizationDomainModel.ANY_DOMAIN;
import static org.keycloak.models.utils.KeycloakModelUtils.findUserByNameOrEmail;
import static org.keycloak.organization.utils.Organizations.getEmailDomain;
import static org.keycloak.organization.utils.Organizations.getMatchingDomain;
import static org.keycloak.organization.utils.Organizations.isEnabledAndOrganizationsPresent;
import static org.keycloak.organization.utils.Organizations.resolveHomeBroker;
import static org.keycloak.utils.StringUtil.isBlank;

/**
 * 组织身份优先登录认证器：根据用户名/邮箱域名解析组织，自动重定向至组织 IdP 或展示组织选择/身份优先登录页。
 * <p>继承 {@link IdentityProviderAuthenticator}，集成 WebAuthn 条件 UI，支持成员资格校验、多组织选择与 SSO 再认证场景。</p>
 */
public class OrganizationAuthenticator extends IdentityProviderAuthenticator {

    private final KeycloakSession session;
    private final WebAuthnConditionalUIAuthenticator webauthnAuth;

    /** @param session Keycloak 会话 */
    public OrganizationAuthenticator(KeycloakSession session) {
        this.session = session;
        this.webauthnAuth = new WebAuthnConditionalUIAuthenticator(session, (context) -> createLoginForm(context));
    }

    @Override
    /** 解析 loginHint/组织上下文，决定初始挑战或继续 action 流程。 */
    public void authenticate(AuthenticationFlowContext context) {
        OrganizationProvider provider = getOrganizationProvider();

        if (!isEnabledAndOrganizationsPresent(provider)) {
            attempted(context);
            return;
        }

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String loginHint = authSession.getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);
        OrganizationModel organization = Organizations.resolveOrganization(session);

        if (loginHint == null && organization == null) {
            initialChallenge(context);
            return;
        }

        if (organization != null) {
            // 将组织写入认证会话，供后续请求记住上下文
            authSession.setAuthNote(OrganizationModel.ORGANIZATION_ATTRIBUTE, organization.getId());
        }

        action(context, loginHint);
    }

    @Override
    /** 处理表单提交：rememberMe、WebAuthn、用户名校验及组织解析。 */
    public void action(AuthenticationFlowContext context) {
        HttpRequest request = context.getHttpRequest();
        MultivaluedMap<String, String> parameters = request.getDecodedFormParameters();
        String username = parameters.getFirst(UserModel.USERNAME);

        // 从 select-organization.ftl 等无 rememberMe 字段的表单重入时跳过，避免清除已保存的 authNote
        // 无条件调用会清除首次 action() 保存的 authNote
        if (parameters.containsKey("rememberMe")) {
            AuthenticatorUtils.processRememberMe(context, parameters);
        }

        // 检测 WebAuthn 提交并执行通行密钥登录
        if (webauthnAuth.isPasskeysEnabled() && (parameters.containsKey(WebAuthnConstants.AUTHENTICATOR_DATA)
                || parameters.containsKey(WebAuthnConstants.ERROR))) {
            webauthnAuth.action(context);
            if (FlowStatus.SUCCESS != context.getStatus()) {
                // WebAuthn 失败则返回；成功则继续组织校验
                return;
            }
        }

        UserModel user = context.getUser();

        if (user == null && isBlank(username)) {
            initialChallenge(context, form -> {
                form.addError(new FormMessage(UserModel.USERNAME, Messages.INVALID_USERNAME));
                return form.createLoginUsername();
            });
            return;
        }

        action(context, username);
    }

    /** 按用户名解析用户与组织，处理 IdP 重定向、成员校验及 SSO 分支。 */
    private void action(AuthenticationFlowContext context, String username) {
        UserModel user = resolveUser(context, username);
        RealmModel realm = context.getRealm();
        String domain = getEmailDomain(username);
        OrganizationModel organization = resolveOrganization(user, domain);

        if (organization == null) {
            // 组织选择挑战前记住用户名，切换组织时可从 ATTEMPTED_USERNAME 读取
            // 用户切换组织时由 switch handler 读取 ATTEMPTED_USERNAME
            if (user != null && username != null) {
                context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);
            }
            if (shouldUserSelectOrganization(context, user)) {
                return;
            }

            if (isMembershipRequired(context, null, user)) {
                return;
            }

            clearAuthenticationSession(context);
            // 请求未映射到任何组织，进入下一步/子流程
            attempted(context, username);
            return;
        }

        // 在认证会话生命周期内记住组织
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        authSession.setAuthNote(OrganizationModel.ORGANIZATION_ATTRIBUTE, organization.getId());
        // 将会组织写入会话上下文，供 FreeMarker 模板使用
        session.getContext().setOrganization(organization);

        if (isMembershipRequired(context, organization, user)) {
            return;
        }

        if (tryRedirectBroker(context, organization, user, username, domain)) {
            return;
        }

        if (user == null) {
            unknownUserChallenge(context, organization, realm, domain != null);
            return;
        }

        // 用户存在，检查是否启用
        if (!user.isEnabled()) {
            context.failure(AuthenticationFlowError.INVALID_USER);
            return;
        }

        if (isSSOAuthentication(authSession)) {
            // 在组织范围内再认证时直接成功
            context.success();
        } else {
            attempted(context, username);
        }
    }

    @Override
    /** @return 领域启用组织功能时可用 */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return realm.isOrganizationsEnabled();
    }

    private OrganizationModel resolveOrganization(UserModel user, String domain) {
        KeycloakContext context = session.getContext();
        HttpRequest request = context.getHttpRequest();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        MultivaluedMap<String, String> parameters = request.getDecodedFormParameters();
        // 来自组织选择页的参数
        List<String> alias = parameters.getOrDefault(OrganizationModel.ORGANIZATION_ATTRIBUTE, List.of());
        OrganizationModel organization;

        if (alias.isEmpty()) {
            organization = Organizations.resolveOrganization(session, user, domain);
        } else {
            OrganizationProvider provider = getOrganizationProvider();
            organization = provider.getByAlias(alias.get(0));
        }

        if (organization == null || !organization.isEnabled()) {
            return null;
        }

        if (!alias.isEmpty() || isSSOAuthentication(authSession)) {
            // 用户所选组织写入 client session note，供 mapper 与签发令牌时使用
            authSession.setClientNote(OrganizationModel.ORGANIZATION_ATTRIBUTE, organization.getId());
        }

        if (!alias.isEmpty()) {
            // 用户显式选择组织，允许后续流程中切换
            authSession.setAuthNote(OrganizationModel.ORGANIZATION_SWITCHABLE_ATTRIBUTE, Boolean.TRUE.toString());
        }

        return organization;
    }

    private boolean shouldUserSelectOrganization(AuthenticationFlowContext context, UserModel user) {
        if (user == null || !OrganizationScope.ANY.equals(OrganizationScope.valueOfScope(session))) {
            return false;
        }

        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        if (authSession.getClientNote(OrganizationModel.ORGANIZATION_ATTRIBUTE) != null) {
            // 组织已选定，无需再次选择
            return false;
        }

        OrganizationProvider provider = getOrganizationProvider();
        Stream<OrganizationModel> organizations = provider.getByMember(user).filter(OrganizationModel::isEnabled);

        if (organizations.count() > 1) {
            LoginFormsProvider form = context.form();
            form.setAttribute("user", new ProfileBean(user, session));
            form.setAttributeMapper(new Function<Map<String, Object>, Map<String, Object>>() {
                @Override
                public Map<String, Object> apply(Map<String, Object> attributes) {
                    attributes.computeIfPresent("auth",
                            (key, bean) -> new OrganizationAwareAuthenticationContextBean((AuthenticationContextBean) bean, false)
                    );
                    return attributes;
                }
            });
            clearAuthenticationSession(context);
            context.challenge(form.createForm("select-organization.ftl"));
            return true;
        }

        return false;
    }

    private boolean tryRedirectBroker(AuthenticationFlowContext context, OrganizationModel organization, UserModel user, String username, String domain) {
        // 用户已设置凭据时不自动重定向，允许自行选择认证方式
        if (user != null && user.credentialManager().getFirstFactorCredentialsStream().findAny().isPresent()) {
            return false;
        }

        List<IdentityProviderModel> broker = resolveHomeBroker(session, user);

        if (broker.size() == 1) {
            // 托管成员且仅关联一个 broker 时自动重定向
            redirect(context, broker.get(0).getAlias(), user.getEmail());
            return true;
        }

        domain = domain == null ? getEmailDomain(user) : domain;

        return redirect(context, organization, username, domain);
    }

    private boolean redirect(AuthenticationFlowContext context, OrganizationModel organization, String username, String domain) {
        if (domain == null) {
            return false;
        }

        OrganizationDomainModel matching = getMatchingDomain(domain, organization);

        if (matching == null) {
            return false;
        }

        // 优先查找与指定域名精确匹配（忽略大小写）的 IdP
        IdentityProviderModel idp = organization.getIdentityProviders()
                .filter(IdentityProviderRedirectMode.EMAIL_MATCH::isSet)
                .filter(broker -> {
                    String brokerDomain = broker.getConfig().get(OrganizationModel.ORGANIZATION_DOMAIN_ATTRIBUTE);

                    if (brokerDomain == null) {
                        return false;
                    }

                    String excludedDomains = broker.getConfig().get(OrganizationModel.ORGANIZATION_EXCLUDED_DOMAIN_ATTRIBUTE);

                    if (excludedDomains != null) {
                        for (String excludedDomain : excludedDomains.split(",")) {
                            if (Organizations.isSameDomain(domain, excludedDomain.trim())) {
                                return false;
                            }
                        }
                    }

                    if (ANY_DOMAIN.equals(brokerDomain)) {
                        return true;
                    }

                    return brokerDomain.equals(matching.getName());
                }).findFirst().orElse(null);

        if (idp != null) {
            redirect(context, idp.getAlias(), username);
            return true;
        }

        return false;
    }

    private UserModel resolveUser(AuthenticationFlowContext context, String username) {
        if (context.getUser() != null) {
            return context.getUser();
        }

        if (username == null) {
            return null;
        }

        RealmModel realm = session.getContext().getRealm();
        UserModel user = findUserByNameOrEmail(session, realm, username);

        // 清除旧组织上下文，按所提供用户名重新解析组织
        clearAuthenticationSession(context);
        context.setUser(user);

        return user;
    }

    private void unknownUserChallenge(AuthenticationFlowContext context, OrganizationModel organization, RealmModel realm, boolean domainMatch) {
        // 用户不存在且在组织范围内认证：展示身份优先登录页及
        // 组织的公开 IdP 供选择
        LoginFormsProvider form = context.form()
                .setAttributeMapper(attributes -> {
                    if (hasPublicBrokers(organization)) {
                        attributes.computeIfPresent("social",
                                (key, bean) -> new OrganizationAwareIdentityProviderBean((IdentityProviderBean) bean, true)
                        );
                        // 有组织公开 IdP 时隐藏自助注册链接，引导用户通过 broker 注册
                        attributes.computeIfPresent("realm",
                                (key, bean) -> new OrganizationAwareRealmBean(realm)
                        );
                    } else {
                        attributes.computeIfPresent("social",
                                (key, bean) -> new OrganizationAwareIdentityProviderBean((IdentityProviderBean) bean, false, true)
                        );
                    }

                    attributes.computeIfPresent("auth",
                            (key, bean) -> new OrganizationAwareAuthenticationContextBean((AuthenticationContextBean) bean, false)
                    );

                    return attributes;
                });

        if (domainMatch) {
            form.addError(new FormMessage("Your email domain matches an organization but you don't have an account yet."));
        }

        // 用户为空且启用 WebAuthn 时填充通行密钥表单数据
        if (webauthnAuth.isPasskeysEnabled()) {
            webauthnAuth.fillContextForm(context);
        }
        context.challenge(form.createLoginUsername());
    }

    private void initialChallenge(AuthenticationFlowContext context) {
        initialChallenge(context, null);
    }

    private void initialChallenge(AuthenticationFlowContext context, Function<LoginFormsProvider, Response> formCreator) {
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        UserModel user = context.getUser();

        if (user == null) {
            // 尚未选定用户时设置 WebAuthn 表单数据
            if (webauthnAuth.isPasskeysEnabled()) {
                webauthnAuth.fillContextForm(context);
            }

            context.challenge(createLoginForm(context, formCreator));
        } else if (isSSOAuthentication(authenticationSession)) {
            if (shouldUserSelectOrganization(context, user)) {
                return;
            }

            // 用户再认证且无待选组织时直接成功
            context.success();
        } else {
            // 用户再认证且无组织需处理时标记 attempted
            attempted(context, user.getUsername());
        }
    }

    private Response createLoginForm(AuthenticationFlowContext context) {
        return createLoginForm(context, null);
    }

    private Response createLoginForm(AuthenticationFlowContext context, Function<LoginFormsProvider, Response> formCreator) {
        // 默认挑战仅展示身份优先登录页及“尝试其他方式”，不显示 broker
        LoginFormsProvider form = context.form()
                .setAttributeMapper(attributes -> {
                    attributes.computeIfPresent("social",
                            (key, bean) -> new OrganizationAwareIdentityProviderBean((IdentityProviderBean) bean, false, true)
                    );
                    attributes.computeIfPresent("auth",
                            (key, bean) -> new OrganizationAwareAuthenticationContextBean((AuthenticationContextBean) bean, false)
                    );
                    return attributes;
                });

        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

        if (loginHint != null) {
            form.setFormData(new MultivaluedHashMap<>(Map.of(UserModel.USERNAME, loginHint)));
        } else {
            context.getAuthenticationSession().removeAuthNote(USER_SET_BEFORE_USERNAME_PASSWORD_AUTH);
            String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getSession());
            if (rememberMeUsername != null) {
                MultivaluedHashMap<String, String> formData = new MultivaluedHashMap<>();
                formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
                formData.add("rememberMe", "on");
                form.setFormData(formData);
            }
        }

        return formCreator == null ? form.createLoginUsername() : formCreator.apply(form);
    }

    private void attempted(AuthenticationFlowContext context) {
        attempted(context, null);
    }

    private void attempted(AuthenticationFlowContext context, String username) {
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();

        if (username != null) {
            authenticationSession.setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);
            authenticationSession.setAuthNote(AbstractUsernameFormAuthenticator.USERNAME_HIDDEN, Boolean.TRUE.toString());
        }

        context.attempted();
    }

    private boolean hasPublicBrokers(OrganizationModel organization) {
        return organization.getIdentityProviders().anyMatch(i -> Booleans.isFalse(i.isHideOnLogin()));
    }

    private OrganizationProvider getOrganizationProvider() {
        return session.getProvider(OrganizationProvider.class);
    }

    private boolean isRequiresMembership(AuthenticationFlowContext context) {
        return Boolean.parseBoolean(getConfig(context).getOrDefault(OrganizationAuthenticatorFactory.REQUIRES_USER_MEMBERSHIP, Boolean.FALSE.toString()));
    }

    private Map<String, String> getConfig(AuthenticationFlowContext context) {
        return Optional.ofNullable(context.getAuthenticatorConfig()).map(AuthenticatorConfigModel::getConfig).orElse(Map.of());
    }

    private void clearAuthenticationSession(AuthenticationFlowContext context) {
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        authenticationSession.removeAuthNote(OrganizationModel.ORGANIZATION_ATTRIBUTE);
    }

    private boolean isMembershipRequired(AuthenticationFlowContext context, OrganizationModel organization, UserModel user) {
        if (user == null || !isRequiresMembership(context)) {
            return false;
        }

        if (organization == null) {
            OrganizationScope scope = OrganizationScope.valueOfScope(session);

            if (OrganizationScope.SPECIFIC.equals(scope)) {
                organization = scope.resolveOrganizations(session).findAny().orElse(null);
            }
        }

        if (organization != null && organization.isMember(user)) {
            return false;
        }

        // 成员资格不满足时不展示“尝试其他方式”
        context.setAuthenticationSelections(List.of());

        LoginFormsProvider form = context.form();
        String errorMessage;
        String failureMessage;

        if (organization == null) {
            errorMessage = "notMemberOfAnyOrganization";
            failureMessage = "User " + user.getUsername() + " not a member of any organization";
            form.setError(errorMessage);
        } else {
            errorMessage = "notMemberOfOrganization";
            failureMessage = "User " + user.getUsername() + " not a member of organization " + organization.getAlias();
            form.setError(errorMessage, organization.getName());
        }

        context.failure(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR,
                form.createErrorPage(Response.Status.FORBIDDEN),
                failureMessage, errorMessage);

        return true;
    }
}
