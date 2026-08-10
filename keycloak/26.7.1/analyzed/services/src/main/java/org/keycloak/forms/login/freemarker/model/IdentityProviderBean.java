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
package org.keycloak.forms.login.freemarker.model;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.common.Profile;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderStorageProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrderedModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.Urls;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;

/**
 * 身份提供者（IdP）FreeMarker Bean：构建登录页可用的联邦身份源列表。
 * <p>根据认证流上下文、已关联联邦身份与 Realm 配置，筛选并包装 {@link IdentityProviderModel}
 * 为模板可读的 {@link IdentityProvider} 视图对象。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class IdentityProviderBean {

    /** IdP 列表按 guiOrder 排序的比较器实例。 */
    public static OrderedModel.OrderedModelComparator<IdentityProvider> IDP_COMPARATOR_INSTANCE = new OrderedModel.OrderedModelComparator<>();
    private static final String ICON_THEME_PREFIX = "kcLogoIdP-";
    private static final String IDP_THEME_CONFIG_PREFIX = "kcTheme-";

    protected AuthenticationFlowContext context;
    protected List<IdentityProvider> providers;
    protected KeycloakSession session;
    protected RealmModel realm;
    protected URI baseURI;

    /** @param session Keycloak 会话 @param realm 当前 Realm @param baseURI 服务基础 URI @param context 认证流上下文（可为 null） */
    public IdentityProviderBean(KeycloakSession session, RealmModel realm, URI baseURI, AuthenticationFlowContext context) {
        this.session = session;
        this.realm = realm;
        this.baseURI = baseURI;
        this.context = context;
    }

    /** @return 登录页应展示的身份提供者列表（懒加载并缓存） */
    public List<IdentityProvider> getProviders() {
        if (this.providers == null) {
            String existingIDP = this.getExistingIDP(session, context);
            Set<String> federatedIdentities = this.getLinkedBrokerAliases(session, realm, context);
            if (federatedIdentities != null) {
                this.providers = getFederatedIdentityProviders(federatedIdentities, existingIDP);
            } else {
                this.providers = searchForIdentityProviders(existingIDP);
            }
        }
        return this.providers;
    }

    /** @return Keycloak 会话 */
    public KeycloakSession getSession() {
        return this.session;
    }

    /** @return 当前 Realm */
    public RealmModel getRealm() {
        return this.realm;
    }

    /** @return 服务基础 URI */
    public URI getBaseURI() {
        return this.baseURI;
    }

    /** @return 认证流上下文 */
    public AuthenticationFlowContext getFlowContext() {
        return this.context;
    }

    /**
     * 根据 {@link IdentityProviderModel} 构建 {@link IdentityProvider} 实例。
     *
     * @param realm Realm 引用
     * @param baseURI 基础 URI
     * @param identityProvider 待转换的身份提供者模型
     * @return 构造完成的 {@link IdentityProvider}
     */
    protected IdentityProvider createIdentityProvider(RealmModel realm, URI baseURI, IdentityProviderModel identityProvider) {
        String loginUrl = Urls.identityProviderAuthnRequest(baseURI, identityProvider.getAlias(), realm.getName()).toString();
        String displayName = KeycloakModelUtils.getIdentityProviderDisplayName(session, identityProvider);
        Map<String, String> themeConfig = identityProvider.getConfig().entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(IDP_THEME_CONFIG_PREFIX))
            .collect(Collectors.toMap(
                entry -> entry.getKey().substring(IDP_THEME_CONFIG_PREFIX.length()),
                Map.Entry::getValue
                 ));
        return new IdentityProvider(identityProvider.getAlias(),
                displayName, identityProvider.getProviderId(), loginUrl,
                identityProvider.getConfig().get("guiOrder"), getLoginIconClasses(identityProvider), themeConfig);
    }

    // 从当前登录主题 properties 读取 kcLogoIdP-{alias} 图标类，
    // 或回退到 IdentityProviderModel.getDisplayIconClasses（第三方 IdP 如 Sign-In-With-Apple）
    // 例如 kcLogoIdP-github = fa fa-github
    private String getLoginIconClasses(IdentityProviderModel identityProvider) {
        try {
            Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
            Optional<String> classesFromTheme = Optional.ofNullable(getLogoIconClass(identityProvider, theme.getProperties()));
            Optional<String> classesFromModel = Optional.ofNullable(identityProvider.getDisplayIconClasses());
            return classesFromTheme.orElse(classesFromModel.orElse(""));
        } catch (IOException e) {
            // 主题加载失败时忽略，返回空图标类
        }
        return "";
    }

    private String getLogoIconClass(IdentityProviderModel identityProvider, Properties themeProperties) throws IOException {
        String iconClass = themeProperties.getProperty(ICON_THEME_PREFIX + identityProvider.getAlias());

        if (iconClass == null) {
            return themeProperties.getProperty(ICON_THEME_PREFIX + identityProvider.getProviderId());
        }

        return iconClass;
    }

    /**
     * 检测用户是否正在关联新的 IdP 到账户。
     * <p>此时 currentUser 为 {@code null} 且流路径为 {@code FIRST_BROKER_LOGIN_PATH}，
     * 需取出用于登录的 IdP 别名并从可选列表中排除（GHI #14173）。</p>
     *
     * @param session {@link KeycloakSession} 引用
     * @param context {@link AuthenticationFlowContext} 引用
     * @return 关联新 IdP 前用于登录的 IdP 别名（若有）
     */
    protected String getExistingIDP(KeycloakSession session, AuthenticationFlowContext context) {

        String existingIDPAlias = null;
        if (context != null) {
            AuthenticationSessionModel authSession = context.getAuthenticationSession();
            String currentFlowPath = authSession.getAuthNote(AuthenticationProcessor.CURRENT_FLOW_PATH);
            UserModel currentUser = context.getUser();

            if (currentUser == null && Objects.equals(LoginActionsService.FIRST_BROKER_LOGIN_PATH, currentFlowPath)) {
                SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(authSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
                final IdentityProviderModel existingIdp = (serializedCtx == null) ? null : serializedCtx.deserialize(session, authSession).getIdpConfig();
                if (existingIdp != null) {
                    existingIDPAlias = existingIdp.getAlias();
                }
            }
        }
        return existingIDPAlias;
    }

    /**
     * 返回与用户联邦身份已关联的 IdP 别名集合。
     * <p>非空时登录页仅展示已关联 IdP；返回 {@code null} 表示展示全部公开启用的 IdP；
     * 空集合表示不展示任何 IdP。</p>
     *
     * @param session {@link KeycloakSession} 引用
     * @param realm Realm 引用
     * @param context {@link AuthenticationFlowContext} 引用
     * @return 可用于登录的 IdP 别名集合
     */
    protected Set<String> getLinkedBrokerAliases(KeycloakSession session, RealmModel realm, AuthenticationFlowContext context) {
        Set<String> result = null;
        if (context != null) {
            UserModel currentUser = context.getUser();
            if (currentUser != null) {
                Set<String> federatedIdentities = session.users().getFederatedIdentitiesStream(session.getContext().getRealm(), currentUser)
                        .map(FederatedIdentityModel::getIdentityProvider)
                        .collect(Collectors.toSet());

                if (!federatedIdentities.isEmpty() || organizationsDisabled(realm)) {
                    // 启用组织功能时不应返回空集合，以便仍展示组织级 IdP
                    result = new HashSet<>(federatedIdentities);
                }
            }
        }
        return result;
    }

    /**
     * 从已关联联邦 IdP 别名集合构建 {@link IdentityProvider} 列表。
     * <p>仅包含已启用、非仅链接且未在登录页隐藏的 IdP；{@code existingIDP} 会被排除。</p>
     *
     * @param federatedProviders 待考虑的联邦 IdP 别名集合
     * @param existingIDP 需从结果中排除的 IdP 别名
     * @return 构造完成的 {@link IdentityProvider} 列表
     */
    protected List<IdentityProvider> getFederatedIdentityProviders(Set<String> federatedProviders, String existingIDP) {
        return federatedProviders.stream()
                .filter(alias -> !Objects.equals(existingIDP, alias))
                .map(alias -> session.identityProviders().getByAlias(alias))
                .filter(federatedProviderPredicate())
                .map(idp -> createIdentityProvider(this.realm, this.baseURI, idp))
                .sorted(IDP_COMPARATOR_INSTANCE).toList();
    }

    /**
     * 返回在转换为 {@link IdentityProvider} 前过滤联邦 IdP 的谓词。
     * <p>子类可覆盖以进一步收窄返回的 IdP 集合。</p>
     *
     * @return 转换前使用的 {@link Predicate}
     */
    protected Predicate<IdentityProviderModel> federatedProviderPredicate() {
        return IdentityProviderStorageProvider.LoginFilter.getLoginPredicate();
    }

    /**
     * 构建登录页可用的 {@link IdentityProvider} 列表。
     * <p>从 {@link IdentityProviderStorageProvider} 获取已启用、非仅链接且未隐藏的 IdP。</p>
     *
     * @param existingIDP 需从结果中排除的 IdP 别名
     * @return 构造完成的 {@link IdentityProvider} 列表
     */
    protected List<IdentityProvider> searchForIdentityProviders(String existingIDP) {
        return session.identityProviders().getForLogin(IdentityProviderStorageProvider.FetchMode.REALM_ONLY, null)
                .filter(idp -> !Objects.equals(existingIDP, idp.getAlias()))
                .map(idp -> createIdentityProvider(this.realm, this.baseURI, idp))
                .sorted(IDP_COMPARATOR_INSTANCE).toList();
    }

    private static boolean organizationsDisabled(RealmModel realm) {
        return !Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION) || !realm.isOrganizationsEnabled();
    }

    /** 供 FreeMarker 模板使用的身份提供者视图对象。 */
    public static class IdentityProvider implements OrderedModel {

        private final String alias;
        /** 提供者类型标识（如 facebook、google 等）。 */
        private final String providerId; // providerType
        private final String loginUrl;
        private final String guiOrder;
        private final String displayName;
        private final String iconClasses;
        private final Map<String, String> themeConfig;

        public IdentityProvider(String alias, String displayName, String providerId, String loginUrl, String guiOrder) {
            this(alias, displayName, providerId, loginUrl, guiOrder, "", null);
        }

        public IdentityProvider(String alias, String displayName, String providerId, String loginUrl, String guiOrder, String iconClasses, Map<String, String> themeConfig) {
            this.alias = alias;
            this.displayName = displayName;
            this.providerId = providerId;
            this.loginUrl = loginUrl;
            this.guiOrder = guiOrder;
            this.iconClasses = iconClasses;
            this.themeConfig = themeConfig;
        }

        /** @return IdP 别名 */
        public String getAlias() {
            return alias;
        }

        /** @return IdP 认证请求 URL */
        public String getLoginUrl() {
            return loginUrl;
        }

        /** @return 提供者类型 ID */
        public String getProviderId() {
            return providerId;
        }

        @Override
        public String getGuiOrder() {
            return guiOrder;
        }

        /** @return 登录按钮显示名称 */
        public String getDisplayName() {
            return displayName;
        }

        /** @return 登录按钮图标 CSS 类 */
        public String getIconClasses() {
            return iconClasses;
        }

        /** @return 主题相关额外配置（kcTheme- 前缀项） */
        public Map<String, String> getThemeConfig() {
            return themeConfig;
        }
    }


}
