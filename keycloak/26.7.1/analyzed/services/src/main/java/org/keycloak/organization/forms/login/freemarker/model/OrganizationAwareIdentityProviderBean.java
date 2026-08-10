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

package org.keycloak.organization.forms.login.freemarker.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.forms.login.freemarker.model.IdentityProviderBean;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.util.Booleans;

import static org.keycloak.models.IdentityProviderStorageProvider.FetchMode.ALL;
import static org.keycloak.models.IdentityProviderStorageProvider.FetchMode.ORG_ONLY;
import static org.keycloak.models.IdentityProviderStorageProvider.FetchMode.REALM_ONLY;

/**
 * 组织感知的身份提供者 FreeMarker Bean：按当前组织上下文过滤登录页展示的 IdP 列表。
 * <p>支持仅领域 IdP、仅组织公开 IdP 或混合模式，并处理“在其他组织已链接时仍显示”等配置。</p>
 */
public class OrganizationAwareIdentityProviderBean extends IdentityProviderBean {

    private final OrganizationModel organization;
    private final boolean onlyRealmBrokers;
    private final boolean onlyOrganizationBrokers;

    /** @param delegate 被包装的身份提供者 Bean */
    public OrganizationAwareIdentityProviderBean(IdentityProviderBean delegate) {
        this(delegate, false);
    }

    /**
     * @param delegate 被包装的身份提供者 Bean
     * @param onlyOrganizationBrokers 是否仅展示组织 IdP
     */
    public OrganizationAwareIdentityProviderBean(IdentityProviderBean delegate,  boolean onlyOrganizationBrokers) {
        this(delegate, onlyOrganizationBrokers, false);
    }

    /**
     * @param delegate 被包装的身份提供者 Bean
     * @param onlyOrganizationBrokers 是否仅展示组织 IdP
     * @param onlyRealmBrokers 是否仅展示领域级 IdP
     */
    public OrganizationAwareIdentityProviderBean(IdentityProviderBean delegate, boolean onlyOrganizationBrokers, boolean onlyRealmBrokers) {
        super(delegate.getSession(), delegate.getRealm(), delegate.getBaseURI(), delegate.getFlowContext());
        this.organization = Organizations.resolveOrganization(super.session);
        this.onlyRealmBrokers = onlyRealmBrokers;
        this.onlyOrganizationBrokers = onlyOrganizationBrokers;
    }

    @Override
    protected Set<String> getLinkedBrokerAliases(KeycloakSession session, RealmModel realm, AuthenticationFlowContext context) {
        Set<String> linkedBrokerAliases = super.getLinkedBrokerAliases(session, realm, context);

        if (linkedBrokerAliases == null || linkedBrokerAliases.isEmpty() || context == null || context.getUser() == null || onlyRealmBrokers) {
            return linkedBrokerAliases;
        }

        Set<String> allBrokerAliases = new HashSet<>(linkedBrokerAliases);

        allBrokerAliases.addAll(getOrgBrokersShownWhenLinkedElsewhere(context.getUser()));

        return allBrokerAliases;
    }

    @Override
    protected List<IdentityProvider> searchForIdentityProviders(String existingIDP) {
        if (onlyRealmBrokers) {
            // 仅返回领域级 IdP（未关联任何组织）
            return session.identityProviders().getForLogin(REALM_ONLY, null)
                    .filter(idp -> !Objects.equals(existingIDP, idp.getAlias()))
                    .map(idp -> createIdentityProvider(this.realm, this.baseURI, idp))
                    .sorted(IDP_COMPARATOR_INSTANCE).toList();
        }
        Predicate<IdentityProviderModel> defaultFilter = idp -> {
            if (idp.isEnabled() && !Objects.equals(existingIDP, idp.getAlias())) {
                if (organization == null) {
                    Map<String, String> config = idp.getConfig();
                    return !Boolean.parseBoolean(config.get(OrganizationModel.HIDE_IDP_ON_LOGIN_WHEN_ORGANIZATION_UNKNOWN));
                }

                return true;
            }

            return false;
        };
        if (onlyOrganizationBrokers) {
            // 已有组织上下文，直接获取该组织的公开且已启用 IdP
            if (this.organization != null) {
                return organization.getIdentityProviders()
                        .filter(idp -> idp.isEnabled() && Booleans.isFalse(idp.isLinkOnly()) && Booleans.isFalse(idp.isHideOnLogin()))
                        .filter(idp -> !Objects.equals(existingIDP, idp.getAlias()))
                        .map(idp -> createIdentityProvider(super.realm, super.baseURI, idp))
                        .sorted(IDP_COMPARATOR_INSTANCE).toList();
            }
            // 无特定组织时，获取任意组织关联的公开已启用 IdP
            return session.identityProviders().getForLogin(ORG_ONLY, null)
                    .filter(defaultFilter) // 再次校验 isEnabled，因 IdP 可能被包装
                    .map(idp -> createIdentityProvider(this.realm, this.baseURI, idp))
                    .sorted(IDP_COMPARATOR_INSTANCE).toList();
        }
        return session.identityProviders().getForLogin(ALL, this.organization != null ? this.organization.getId() : null)
                .filter(defaultFilter) // re-check isEnabled as idp might have been wrapped.
                .map(idp -> createIdentityProvider(this.realm, this.baseURI, idp))
                .sorted(IDP_COMPARATOR_INSTANCE).toList();
    }

    @Override
    protected Predicate<IdentityProviderModel> federatedProviderPredicate() {
        // 组合父类谓词与组织过滤条件
        return super.federatedProviderPredicate().and(idp -> {
            if (onlyRealmBrokers) {
                return idp.getOrganizationId() == null;
            } else if (onlyOrganizationBrokers) {
                return isPublicOrganizationBroker(idp);
            } else {
                return idp.getOrganizationId() == null || isPublicOrganizationBroker(idp);
            }
        });
    }

    private boolean isPublicOrganizationBroker(IdentityProviderModel idp) {

        if (idp.getOrganizationId() == null) {
            return false;
        }
        if (organization != null && !Objects.equals(organization.getId(),idp.getOrganizationId())) {
            return false;
        }
        return Booleans.isFalse(idp.isHideOnLogin());
    }

    private Set<String> getOrgBrokersShownWhenLinkedElsewhere(UserModel user) {
        OrganizationProvider provider = Organizations.getProvider(session);

        if (!Organizations.isEnabledAndOrganizationsPresent(provider)) {
            return Set.of();
        }

        return provider.getByMember(user)
                .filter(OrganizationModel::isEnabled)
                .flatMap(OrganizationModel::getIdentityProviders)
                .filter(this::isShownWhenLinkedElsewhere)
                .map(IdentityProviderModel::getAlias)
                .collect(Collectors.toSet());
    }

    private boolean isShownWhenLinkedElsewhere(IdentityProviderModel idp) {
        if (idp.getOrganizationId() == null) {
            return false;
        }

        if (!Boolean.parseBoolean(idp.getConfig().get(OrganizationModel.SHOW_IDP_ON_LOGIN_WHEN_LINKED_ELSEWHERE))) {
            return false;
        }

        if (organization != null && !Objects.equals(organization.getId(), idp.getOrganizationId())) {
            return false;
        }

        if (organization == null && Boolean.parseBoolean(idp.getConfig().get(OrganizationModel.HIDE_IDP_ON_LOGIN_WHEN_ORGANIZATION_UNKNOWN))) {
            return false;
        }

        return true;
    }
}
