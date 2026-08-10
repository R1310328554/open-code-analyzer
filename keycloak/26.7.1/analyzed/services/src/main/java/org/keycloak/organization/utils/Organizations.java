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

package org.keycloak.organization.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.TokenVerifier;
import org.keycloak.authentication.actiontoken.inviteorg.InviteOrgActionToken;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.common.VerificationException;
import org.keycloak.crypto.CryptoUtils;
import org.keycloak.crypto.SignatureVerifierContext;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.Constants;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.GroupModel.Type;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelValidationException;
import org.keycloak.models.OrganizationDomainModel;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.protocol.mappers.oidc.OrganizationScope;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.Urls;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.EmailValidationUtil;

import static java.util.Optional.ofNullable;

import static org.keycloak.utils.StringUtil.isBlank;

/**
 * 组织特性工具类：提供组织启用检测、域名校验、上下文解析、Home IdP 解析等辅助方法。
 * <p>供认证流程、用户配置校验、管理 API 及协议映射器复用。</p>
 */
public class Organizations {

    /** 通配符域名前缀（如 {@code *.example.com}）。 */
    private static final String WILDCARD_PREFIX = "*.";
    private static final int MIN_DOMAIN_PARTS = 2;
    private static final int MAX_DOMAIN_PARTS = 10;

    /** 判断群组是否为组织类型且关联了组织模型。 */
    public static boolean isOrganizationGroup(GroupModel group) {
        return Type.ORGANIZATION.equals(group.getType()) && group.getOrganization() != null;
    }

    /** 判断当前上下文是否允许管理指定组织群组（含内部组织群组特例）。 */
    public static boolean canManageOrganizationGroup(KeycloakSession session, GroupModel group) {
        // 非组织群组或组织未启用时无需进一步校验
        if (!isOrganizationGroup(group) || !isEnabled(session)) {
            return true;
        }

        // 上下文已有组织时允许管理
        if (resolveOrganization(session) != null) {
            return true;
        }

        // 无组织上下文时，仅内部组织群组可管理
        return getProvider(session).getById(group.getName()) == null;
    }

    /** 解析受管成员用户应自动跳转的 Home 身份提供方（联邦身份来源）列表。 */
    public static List<IdentityProviderModel> resolveHomeBroker(KeycloakSession session, UserModel user) {
        if (!isEnabled(session)) {
            return List.of();
        }
        OrganizationProvider provider = getProvider(session);
        RealmModel realm = session.getContext().getRealm();
        List<OrganizationModel> organizations = Optional.ofNullable(user).stream().flatMap(provider::getByMember)
                .filter(OrganizationModel::isEnabled)
                .filter((org) -> org.isManaged(user))
                .toList();

        if (organizations.isEmpty()) {
            return List.of();
        }

        List<IdentityProviderModel> brokers = new ArrayList<>();

        for (OrganizationModel organization : organizations) {
            // 受管成员：尝试解析来源 IdP 以便自动重定向
            List<IdentityProviderModel> organizationBrokers = organization.getIdentityProviders().toList();
            session.users().getFederatedIdentitiesStream(realm, user)
                    .map(f -> {
                        IdentityProviderModel broker = session.identityProviders().getByAlias(f.getIdentityProvider());

                        if (!organizationBrokers.contains(broker)) {
                            return null;
                        }

                        FederatedIdentityModel identity = session.users().getFederatedIdentity(realm, user, broker.getAlias());

                        if (identity != null) {
                            return broker;
                        }

                        return null;
                    }).filter(Objects::nonNull)
                    .forEach(brokers::add);
        }

        return brokers;
    }

    /** 返回删除群组的消费者；组织群组删除前会临时设置组织上下文。 */
    public static Consumer<GroupModel> removeGroup(KeycloakSession session, RealmModel realm) {
        return group -> {
            if (!Type.ORGANIZATION.equals(group.getType())) {
                realm.removeGroup(group);
                return;
            }

            OrganizationModel current = resolveOrganization(session);

            try {
                OrganizationProvider provider = getProvider(session);

                session.getContext().setOrganization(provider.getById(group.getName()));

                realm.removeGroup(group);
            } finally {
                session.getContext().setOrganization(current);
            }
        };
    }

    /** 判断组织特性在 Profile 与 Provider 层面均已启用。 */
    public static boolean isEnabled(KeycloakSession session) {
        if (!Profile.isFeatureEnabled(Feature.ORGANIZATION)) {
            return false;
        }
        OrganizationProvider provider = getProvider(session);
        return provider != null && provider.isEnabled();
    }

    /** 判断组织 Provider 已启用且 Realm 中存在至少一个组织。 */
    public static boolean isEnabledAndOrganizationsPresent(OrganizationProvider orgProvider) {
        return orgProvider != null && orgProvider.isEnabled() && orgProvider.hasOrganizations();
    }

    /** 基于会话判断组织特性启用且存在组织。 */
    public static boolean isEnabledAndOrganizationsPresent(KeycloakSession session) {
        if (!Profile.isFeatureEnabled(Feature.ORGANIZATION)) {
            return false;
        }

        OrganizationProvider provider = getProvider(session);

        return isEnabledAndOrganizationsPresent(provider);
    }

    /** 校验组织已启用；未启用时按查询权限返回 404 或 403。 */
    public static void checkEnabled(OrganizationProvider provider, AdminPermissionEvaluator auth) {
        if (provider == null || !provider.isEnabled()) {
            throw auth.orgs().canQuery() ?
                    ErrorResponse.error("Organizations not enabled for this realm.", Response.Status.NOT_FOUND) :
                    new ForbiddenException();
        }
    }

    /** 从请求查询参数解析并验证组织邀请 Action Token。 */
    public static InviteOrgActionToken parseInvitationToken(KeycloakSession session, HttpRequest request) throws VerificationException {
        MultivaluedMap<String, String> queryParameters = request.getUri().getQueryParameters();
        String tokenFromQuery = queryParameters.getFirst(Constants.TOKEN);

        if (tokenFromQuery == null) {
            return null;
        }

        KeycloakContext context = session.getContext();
        RealmModel realm = session.getContext().getRealm();
        TokenVerifier<InviteOrgActionToken> verifier = TokenVerifier.create(tokenFromQuery, InviteOrgActionToken.class)
                .withChecks(TokenVerifier.IS_ACTIVE,
                        new TokenVerifier.RealmUrlCheck(Urls.realmIssuer(context.getUri().getBaseUri(), realm.getName())));

        SignatureVerifierContext verifierContext = CryptoUtils.getSignatureProvider(session, verifier.getHeader().getAlgorithm().name()).verifier(verifier.getHeader().getKeyId());
        verifier.verifierContext(verifierContext);

        return verifier.verify().getToken();
    }

    /** 计算域名分段数（以点分隔的部分个数）。 */
    public static int getDomainPartsSize(String domain) {
        if (isBlank(domain)) {
            return 0;
        }
        return Math.toIntExact(domain.chars().filter(c -> c == '.').count()) + 1;
    }

    /** 校验组织域名格式，支持 {@code *.example.com} 通配符规则。 */
    public static void validateDomain(String rawDomain) {
        if (rawDomain == null) {
            return;
        }

        String domain = rawDomain;

        if (rawDomain.contains(WILDCARD_PREFIX)) {
            if (rawDomain.length() == WILDCARD_PREFIX.length()) {
                throw new ModelValidationException("Wildcard domain must specify a base domain: " + rawDomain);
            }

            if (!rawDomain.startsWith(WILDCARD_PREFIX)) {
                throw new ModelValidationException("Wildcard domain must start with the wildcard");
            }

            domain = rawDomain.substring(2);

            if (domain.contains("*")) {
                throw new ModelValidationException("Multiple wildcards are not allowed: " + rawDomain);
            }

            int parts = getDomainPartsSize(domain);

            if (parts < MIN_DOMAIN_PARTS) {
                throw new ModelValidationException("Domain must have at least " + MIN_DOMAIN_PARTS + " parts (e.g. 'example.com'): " + domain);
            }

            if (parts > MAX_DOMAIN_PARTS) {
                throw new ModelValidationException("Domain has too many parts (max " + MAX_DOMAIN_PARTS + " allowed): " + domain);
            }
        }

        if (isBlank(domain) || !EmailValidationUtil.isValidEmail("user@" + domain)) {
            throw new ModelValidationException("Invalid domain format: " + rawDomain);
        }
    }


    /**
     * 返回给定域名与组织下最精确匹配的组织域模型。
     * <p>多个域同时匹配时（精确域、父级通配符等），分段数最多者胜出。</p>
     * @param domain 待匹配域名
     * @param organization 组织模型
     * @return 最精确匹配的组织域，无匹配时返回 {@code null}
     */
    public static OrganizationDomainModel getMatchingDomain(String domain, OrganizationModel organization) {
        if (domain == null || organization == null) {
            return null;
        }

        List<OrganizationDomainModel> domains = organization.getDomains().filter(model -> isSameDomain(domain, model))
                // 按域分段数升序排序，最精确匹配位于列表末尾
                .sorted(Comparator.comparingInt(o -> getDomainPartsSize(o.getName())))
                .toList();

        if (domains.isEmpty()) {
            return null;
        }

        return domains.get(domains.size() - 1);
    }

    /** 判断域名是否与组织域模型匹配（含通配符）。 */
    public static boolean isSameDomain(String domain, OrganizationDomainModel model) {
        return isSameDomain(domain, ofNullable(model).map(OrganizationDomainModel::getName).orElse(null));
    }

    /** 判断域名是否与期望域模式匹配（精确或 {@code *.base} 通配符）。 */
    public static boolean isSameDomain(String domain, String expectedDomain) {
        if (domain == null || expectedDomain == null) {
            return false;
        }

        String canonicalDomain = domain.toLowerCase();
        String pattern = expectedDomain.toLowerCase();

        if (canonicalDomain.equals(pattern)) {
            return true;
        }

        if (pattern.startsWith(WILDCARD_PREFIX)) {
            String baseDomain = pattern.substring(2);
            return canonicalDomain.equals(baseDomain) || canonicalDomain.endsWith("." + baseDomain);
        }

        return false;
    }

    /** 从邮箱地址提取 {@code @} 后的域名部分。 */
    public static String getEmailDomain(String email) {
        if (email == null) {
            return null;
        }

        int domainSeparator = email.indexOf('@');

        if (domainSeparator == -1) {
            return null;
        }

        return email.substring(domainSeparator + 1);
    }

    /** 从用户邮箱提取域名。 */
    public static String getEmailDomain(UserModel user) {
        if (user == null) {
            return null;
        }
        return getEmailDomain(user.getEmail());
    }

    /** 从当前会话上下文解析组织（无用户/域提示）。 */
    public static OrganizationModel resolveOrganization(KeycloakSession session) {
        return resolveOrganization(session, null, null);
    }

    /** 结合用户成员关系与认证会话解析当前组织。 */
    public static OrganizationModel resolveOrganization(KeycloakSession session, UserModel user) {
        return resolveOrganization(session, user, null);
    }

    /**
     * 解析当前上下文中的组织：优先会话上下文、认证会话 note、成员关系与邮箱域匹配。
     * @param session Keycloak 会话
     * @param user 可选用户
     * @param domain 可选邮箱域提示
     * @return 解析到的组织，或 {@code null}
     */
    public static OrganizationModel resolveOrganization(KeycloakSession session, UserModel user, String domain) {
        KeycloakContext context = session.getContext();
        RealmModel realm = context.getRealm();

        if (!realm.isOrganizationsEnabled()) {
            return null;
        }

        OrganizationModel current = context.getOrganization();

        if (current != null) {
            // 已从当前 Keycloak 会话上下文解析
            return current;
        }

        OrganizationProvider provider = getProvider(session);

        if (!provider.hasOrganizations()) {
            return null;
        }

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String emailDomain = ofNullable(domain).orElseGet(() -> getEmailDomain(user));

        if (authSession != null) {
            OrganizationScope scope = OrganizationScope.valueOfScope(session);
            List<OrganizationModel> organizations = ofNullable(authSession.getAuthNote(OrganizationModel.ORGANIZATION_ATTRIBUTE))
                    .map(provider::getById)
                    .map(List::of)
                    .orElseGet(() -> scope == null ? List.of() : scope.resolveOrganizations(user, session).toList());

            if (organizations.size() == 1) {
                OrganizationModel organization = organizations.get(0);

                if (user == null) {
                    return organization;
                }

                // 确认用户仍属于认证会话中的组织
                if (organization.isMember(user)) {
                    return organization;
                }

                return resolveByDomain(organizations, emailDomain);
            } else if (scope != null && user != null) {
                return resolveByDomain(organizations, emailDomain);
            }
        }

        List<OrganizationModel> organizations = ofNullable(user).stream()
                .flatMap(provider::getByMember)
                .filter(OrganizationModel::isEnabled)
                .toList();

        if (organizations.size() == 1) {
            // 唯一成员关系，直接返回该组织
            return organizations.get(0);
        }

        if (organizations.isEmpty()) {
            // 无成员关系时，按域名匹配任意组织
            return resolveByDomain(ofNullable(emailDomain)
                    .map(provider::getByDomainName)
                    .map(List::of)
                    .orElse(List.of()), emailDomain);
        }

        for (OrganizationModel organization : organizations) {
            if (organization.isManaged(user)) {
                return organization;
            }
        }

        return resolveByDomain(organizations, emailDomain);
    }

    /** @return 会话中的 {@link OrganizationProvider} */
    public static OrganizationProvider getProvider(KeycloakSession session) {
        return session.getProvider(OrganizationProvider.class);
    }

    /** 组织上下文中始终允许注册；否则遵循 Realm 注册策略。 */
    public static boolean isRegistrationAllowed(KeycloakSession session, RealmModel realm) {
        if (session.getContext().getOrganization() != null) return true;
        return realm.isRegistrationAllowed();
    }

    /** 判断用户是否为只读组织成员（受管且组织禁用，或组织全局禁用时的受管成员）。 */
    public static boolean isReadOnlyOrganizationMember(KeycloakSession session, UserModel delegate) {
        if (delegate == null) {
            return false;
        }

        if (!Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION)) {
            return false;
        }

        var organizationProvider = getProvider(session);

        if (!organizationProvider.hasOrganizations()) {
            return false;
        }

        // 系统级检查需关闭 FGAP 过滤，避免 getByMember 递归
        // getByMember -> applyAuthorizationFilters -> getPredicates -> getUser -> getUserById -> validateUser -> isReadOnlyOrganizationMember -> ...
        return AdminPermissionsSchema.runWithoutAuthorization(session, () ->
                organizationProvider.getByMember(delegate)
                        .anyMatch((org) -> (organizationProvider.isEnabled() && org.isManaged(delegate) && !org.isEnabled()) ||
                                (!organizationProvider.isEnabled() && org.isManaged(delegate))));
    }

    /** 在候选组织列表中按域名最精确匹配规则选出唯一组织。 */
    public static OrganizationModel resolveByDomain(List<OrganizationModel> organizations, String domain) {
        int bestParts = -1;
        OrganizationModel organization = null;

        for (OrganizationModel model : organizations) {
            OrganizationDomainModel bestMatch = getMatchingDomain(domain, model);

            if (bestMatch == null) {
                continue;
            }

            if (organizations.size() == 1) {
                // 仅一个组织时，任意域匹配即可
                return model;
            }

            int mostSpecificParts = getDomainPartsSize(bestMatch.getName());

            if (mostSpecificParts > bestParts) {
                bestParts = mostSpecificParts;
                organization = model;
            }
        }

        return organization;
    }
}
