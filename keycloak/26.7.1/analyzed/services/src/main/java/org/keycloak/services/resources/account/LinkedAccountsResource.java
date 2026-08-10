/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.services.resources.account;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.events.Details;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderQuery;
import org.keycloak.models.IdentityProviderShowInAccountConsole;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.representations.account.AccountLinkUriRepresentation;
import org.keycloak.representations.account.LinkedAccountRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.Auth;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.theme.Theme;
import org.keycloak.utils.BrokerUtil;
import org.keycloak.utils.StreamsUtil;

import org.jboss.logging.Logger;

import static org.keycloak.models.Constants.ACCOUNT_CONSOLE_CLIENT_ID;

/**
 * 关联/解除关联社交与联邦登录账户的 REST API。
 *
 * @author Stan Silvert
 */
public class LinkedAccountsResource {
    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(LinkedAccountsResource.class);

    private final KeycloakSession session;
    private final HttpRequest request;
    private final EventBuilder event;
    private final UserModel user;
    private final RealmModel realm;
    private final Auth auth;
    private final Set<String> socialIds;

    public LinkedAccountsResource(KeycloakSession session,
                                  HttpRequest request,
                                  Auth auth,
                                  EventBuilder event,
                                  UserModel user) {
        this.session = session;
        this.request = request;
        this.auth = auth;
        this.event = event;
        this.user = user;
        this.realm = session.getContext().getRealm();
        this.socialIds = session.getKeycloakSessionFactory().getProviderFactoriesStream(SocialIdentityProvider.class)
                .map(ProviderFactory::getId)
                .collect(Collectors.toSet());
    }

    /**
     * 返回用户已关联的 IdP，或可供关联的领域级 IdP 列表。
     * <p>{@code linked=true} 时返回已关联提供者（含组织绑定）；{@code linked=false} 时返回未关联且非组织绑定的可关联 IdP。</p>
     *
     * @param linked true 仅已关联，false 仅可关联
     * @param search 名称过滤：前缀 name*、包含 *name*、精确 "name"
     * @param firstResult 分页偏移
     * @param maxResults 最大条数
     * @return 按 guiOrder 排序的 {@link LinkedAccountRepresentation} 列表
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response linkedAccounts(
            @QueryParam("linked") Boolean linked,
            @QueryParam("search") String search,
            @QueryParam("first") Integer firstResult,
            @QueryParam("max") Integer maxResults
    ) {
        auth.requireOneOf(AccountRoles.MANAGE_ACCOUNT, AccountRoles.VIEW_PROFILE);

        // TODO：控制台与测试更新后移除此向后兼容分支
        if (linked == null) {
            List<LinkedAccountRepresentation> linkedAccounts = getLinkedAccounts(this.session, this.realm, this.user);
            return Cors.builder().auth().checkAllowedOrigins(auth.getToken()).add(Response.ok(linkedAccounts));
        }

        List<LinkedAccountRepresentation> linkedAccounts;
        if (linked) {
            // linked=true：从联邦身份获取已关联账户
			Set<IdentityProviderShowInAccountConsole> includedShowInAccountConsoleValues = Set.of(IdentityProviderShowInAccountConsole.ALWAYS, IdentityProviderShowInAccountConsole.WHEN_LINKED);
            linkedAccounts = StreamsUtil.paginatedStream(session.users().getFederatedIdentitiesStream(realm, user)
                    .map(fedIdentity -> this.toLinkedAccount(session.identityProviders().getByAlias(fedIdentity.getIdentityProvider()), fedIdentity.getUserName(), includedShowInAccountConsoleValues))
                    .filter(account -> account != null && this.matchesLinkedProvider(account, search))
                    .sorted(), firstResult, maxResults)
                    .toList();
        } else {
            if (Organizations.resolveHomeBroker(session, user).isEmpty()) {
                // linked=false：返回领域级、已启用且尚未关联的 IdP
                String fedAliasesToExclude = session.users().getFederatedIdentitiesStream(realm, user).map(FederatedIdentityModel::getIdentityProvider)
                        .collect(Collectors.joining(","));

                Map<String, String> searchOptions = Map.of(
                        IdentityProviderModel.ENABLED, "true",
                        IdentityProviderModel.ORGANIZATION_ID, "",
                        IdentityProviderModel.SEARCH, search == null ? "" : search,
                        IdentityProviderModel.ALIAS_NOT_IN, fedAliasesToExclude,
                        IdentityProviderModel.SHOW_IN_ACCOUNT_CONSOLE, IdentityProviderShowInAccountConsole.ALWAYS.name());

                linkedAccounts = session.identityProviders().getAllStream(IdentityProviderQuery.userAuthentication().with(searchOptions), firstResult, maxResults)
                        .map(idp -> this.toLinkedAccount(idp, null, null))
                        .toList();
            } else {
                linkedAccounts = List.of();
            }
        }
        return Cors.builder().auth().checkAllowedOrigins(auth.getToken()).add(Response.ok(linkedAccounts));
    }

    private LinkedAccountRepresentation toLinkedAccount(IdentityProviderModel provider, String fedIdentity, Set<IdentityProviderShowInAccountConsole> includedShowInAccountConsoleValues) {
        if (provider == null || !provider.isEnabled()) {
            return null;
        }
		if (includedShowInAccountConsoleValues != null && !includedShowInAccountConsoleValues.contains(provider.getShowInAccountConsole())) {
			return null;
		}
        LinkedAccountRepresentation rep = new LinkedAccountRepresentation();
        rep.setConnected(fedIdentity != null);
        rep.setSocial(socialIds.contains(provider.getProviderId()));
        rep.setProviderAlias(provider.getAlias());
        rep.setDisplayName(KeycloakModelUtils.getIdentityProviderDisplayName(session, provider));
        rep.setGuiOrder(provider.getConfig() != null ? provider.getConfig().get("guiOrder") : null);
        rep.setProviderName(provider.getAlias());
        rep.setLinkedUsername(fedIdentity);
        return rep;
    }

    private boolean matchesLinkedProvider(final LinkedAccountRepresentation linkedAccount, final String search) {
        if (search == null) {
            return true;
        }else if (search.startsWith("\"") && search.endsWith("\"")) {
            final String name = search.substring(1, search.length() - 1);
            return linkedAccount.getProviderAlias().equals(name) || linkedAccount.getDisplayName().equals(name);
        } else if (search.startsWith("*") && search.endsWith("*")) {
            final String name = search.substring(1, search.length() - 1);
            return linkedAccount.getProviderAlias().contains(name) || linkedAccount.getDisplayName().contains(name);
        } else if (search.endsWith("*")) {
            final String name = search.substring(0, search.length() - 1);
            return linkedAccount.getProviderAlias().startsWith(name) || linkedAccount.getDisplayName().startsWith(name);
        } else {
            return linkedAccount.getProviderAlias().startsWith(search) || linkedAccount.getDisplayName().startsWith(search);
        }
    }

    @Deprecated
    public List<LinkedAccountRepresentation> getLinkedAccounts(KeycloakSession session, RealmModel realm, UserModel user) {
        return session.identityProviders().getAllStream(IdentityProviderQuery.userAuthentication().with(IdentityProviderModel.ENABLED, "true"), null, null)
                .map(provider -> toLinkedAccountRepresentation(provider, session.users().getFederatedIdentitiesStream(realm, user)))
                .filter(Objects::nonNull)
                .sorted().toList();
    }

    @Deprecated
    private LinkedAccountRepresentation toLinkedAccountRepresentation(IdentityProviderModel provider, Stream<FederatedIdentityModel> identities) {
        String providerAlias = provider.getAlias();

        FederatedIdentityModel identity = getIdentity(identities, providerAlias);
        // if idp is not yet linked and is currently bound to an organization, it should not be available for linking.
        if (identity == null && provider.getOrganizationId() != null) return null;
		boolean hide = switch (provider.getShowInAccountConsole()) {
			case ALWAYS -> false;
			case WHEN_LINKED -> identity == null;
			case NEVER -> true;
		};
		if (hide) {
			return null;
		}

        String displayName = KeycloakModelUtils.getIdentityProviderDisplayName(session, provider);
        String guiOrder = provider.getConfig() != null ? provider.getConfig().get("guiOrder") : null;

        LinkedAccountRepresentation rep = new LinkedAccountRepresentation();
        rep.setConnected(identity != null);
        rep.setSocial(socialIds.contains(provider.getProviderId()));
        rep.setProviderAlias(providerAlias);
        rep.setDisplayName(displayName);
        rep.setGuiOrder(guiOrder);
        rep.setProviderName(provider.getAlias());
        if (identity != null) {
            rep.setLinkedUsername(identity.getUserName());
        }
        return rep;
    }

    @Deprecated
    private FederatedIdentityModel getIdentity(Stream<FederatedIdentityModel> identities, String providerAlias) {
        return identities.filter(model -> Objects.equals(model.getIdentityProvider(), providerAlias))
                .findFirst().orElse(null);
    }

    /**
     * 生成用于关联 IdP 的重定向 URL（已废弃）。
     *
     * @deprecated 建议使用 {@code idp_link} kc_action
     * @return 含关联 URI 的响应
     */
    @GET
    @Path("/{providerAlias}")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public Response buildLinkedAccountURI(@PathParam("providerAlias") String providerAlias,
                                     @QueryParam("redirectUri") String redirectUri) {
        auth.require(AccountRoles.MANAGE_ACCOUNT);
        logger.warnf("Using deprecated endpoint of Account REST service for linking user '%s' in the realm '%s' to identity provider '%s'. It is recommended to use application initiated actions (AIA) for linking identity provider with the user.",
                user.getUsername(),
                realm.getName(),
                providerAlias);
        if (redirectUri == null) {
            ErrorResponse.error(Messages.INVALID_REDIRECT_URI, Response.Status.BAD_REQUEST);
        }

        String errorMessage = checkCommonPreconditions(providerAlias);
        if (errorMessage != null) {
            throw ErrorResponse.error(errorMessage, Response.Status.BAD_REQUEST);
        }
        if (!Organizations.resolveHomeBroker(session, user).isEmpty()) {
            throw ErrorResponse.error(translateErrorMessage(Messages.FEDERATED_IDENTITY_BOUND_ORGANIZATION), Response.Status.BAD_REQUEST);
        }
        if (auth.getSession() == null) {
            throw ErrorResponse.error(Messages.SESSION_NOT_ACTIVE, Response.Status.BAD_REQUEST);
        }

        try {
            AccountLinkUriRepresentation rep = BrokerUtil.createClientInitiatedLinkURI(ACCOUNT_CONSOLE_CLIENT_ID, redirectUri, providerAlias, realm.getName(), auth.getSession().getId(), this.session.getContext().getUri().getBaseUri());

            return Cors.builder().auth().checkAllowedOrigins(auth.getToken()).add(Response.ok(rep));
        } catch (Exception spe) {
            spe.printStackTrace();
            throw ErrorResponse.error(Messages.FAILED_TO_PROCESS_RESPONSE, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/{providerAlias}")
    @Produces(MediaType.APPLICATION_JSON)
    /** 解除与指定 IdP 的联邦关联 */
    public Response removeLinkedAccount(@PathParam("providerAlias") String providerAlias) {
        auth.require(AccountRoles.MANAGE_ACCOUNT);

        String errorMessage = checkCommonPreconditions(providerAlias);
        if (errorMessage != null) {
            throw ErrorResponse.error(errorMessage, Response.Status.BAD_REQUEST);
        }

        FederatedIdentityModel link = session.users().getFederatedIdentity(realm, user, providerAlias);
        if (link == null) {
            throw ErrorResponse.error(translateErrorMessage(Messages.FEDERATED_IDENTITY_NOT_ACTIVE), Response.Status.BAD_REQUEST);
        }

        if (Profile.isFeatureEnabled(Feature.ORGANIZATION)) {
            if (Organizations.resolveHomeBroker(session, user).stream()
                    .map(IdentityProviderModel::getAlias)
                    .anyMatch(providerAlias::equals)) {
                throw ErrorResponse.error(translateErrorMessage(Messages.FEDERATED_IDENTITY_BOUND_ORGANIZATION), Response.Status.BAD_REQUEST);
            }
        }

        // 若无密码或其他 IdP，不允许移除最后一个社交提供者
        if (!(session.users().getFederatedIdentitiesStream(realm, user).count() > 1 || user.isFederated() || isPasswordSet())) {
            throw ErrorResponse.error(translateErrorMessage(Messages.FEDERATED_IDENTITY_REMOVING_LAST_PROVIDER), Response.Status.BAD_REQUEST);
        }

        session.users().removeFederatedIdentity(realm, user, providerAlias);

        logger.debugv("Social provider {0} removed successfully from user {1}", providerAlias, user.getUsername());

        event.event(EventType.REMOVE_FEDERATED_IDENTITY).client(auth.getClient()).user(auth.getUser())
                .detail(Details.USERNAME, auth.getUser().getUsername())
                .detail(Details.IDENTITY_PROVIDER, link.getIdentityProvider())
                .detail(Details.IDENTITY_PROVIDER_USERNAME, link.getUserName())
                .success();

        return Cors.builder().auth().checkAllowedOrigins(auth.getToken()).add(Response.noContent());
    }

    /** 校验 provider 别名、存在性及用户启用状态 */
    private String checkCommonPreconditions(String providerAlias) {
        auth.require(AccountRoles.MANAGE_ACCOUNT);

        if (Validation.isEmpty(providerAlias)) {
            return Messages.MISSING_IDENTITY_PROVIDER;
        }

        if (!isValidProvider(providerAlias)) {
            return Messages.IDENTITY_PROVIDER_NOT_FOUND;
        }

        if (!user.isEnabled()) {
            return Messages.ACCOUNT_DISABLED;
        }

        return null;
    }

    /** 从账户主题消息束本地化错误码 */
    private String translateErrorMessage(String errorCode, Object... params) {
        try {
            Locale locale = session.getContext().resolveLocale(user);
            String pattern = session.theme().getTheme(Theme.Type.ACCOUNT).getMessages(locale).getProperty(errorCode);
            return new MessageFormat(pattern, locale).format(params, new StringBuffer(), null).toString();
        } catch (IOException e) {
            return errorCode;
        }
    }

    /** 用户是否已配置密码凭证 */
    private boolean isPasswordSet() {
        return user.credentialManager().isConfiguredFor(PasswordCredentialModel.TYPE);
    }

    /** IdP 别名是否在领域中存在 */
    private boolean isValidProvider(String providerAlias) {
        return session.identityProviders().getByAlias(providerAlias) != null;
    }
}
