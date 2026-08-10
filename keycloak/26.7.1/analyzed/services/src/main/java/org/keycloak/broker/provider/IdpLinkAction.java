/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.broker.provider;

import java.io.IOException;
import java.util.Collections;

import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.resources.IdentityBrokerService;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;

import static org.keycloak.services.resources.IdentityBrokerService.LINKING_IDENTITY_PROVIDER;

/**
 * 身份提供方链接必需操作：客户端发起的账户 IdP 关联流程。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IdpLinkAction implements RequiredActionProvider, RequiredActionFactory {

    /** 日志记录器。 */
    protected static final Logger logger = Logger.getLogger(IdpLinkAction.class);

    /** 必需操作 provider id。 */
    public static final String PROVIDER_ID = "idp_link";

    /** 认证会话 note：标记由本操作触发的客户端发起 IdP 关联。 */
    /** 认证会话 note 键：客户端发起 IdP 关联标记。 */
    public static final String KC_ACTION_LINKING_IDENTITY_PROVIDER = "kc_action_linking_identity_provider";

    /** 认证会话 note：IdP 关联状态与错误信息（error 时填充 idp_link_error）。 */
    /** 认证会话 note 键：IdP 关联结果状态。 */
    public static final String IDP_LINK_STATUS = "idp_link_status";
    /** 认证会话 note 键：关联失败时的序列化错误。 */
    public static final String IDP_LINK_ERROR = "idp_link_error";

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return this;
    }

    /** 支持客户端发起的 kc_action 流程。 */
    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }


    @Override
    public void evaluateTriggers(RequiredActionContext context) {

    }

    /** 展示 IdP 关联确认页；校验 manage-account 或 manage-account-links 权限。 */
    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();
        ClientModel client = authSession.getClient();
        EventBuilder event = context.getEvent().clone();
        event.event(EventType.FEDERATED_IDENTITY_LINK);

        String identityProviderAlias = authSession.getClientNote(Constants.KC_ACTION_PARAMETER);
        if (identityProviderAlias == null) {
            event.error(Errors.UNKNOWN_IDENTITY_PROVIDER);
            context.ignore();
            return;
        }
        event.detail(Details.IDENTITY_PROVIDER, identityProviderAlias);
        IdentityProviderModel identityProviderModel = session.identityProviders().getByAlias(identityProviderAlias);
        if (identityProviderModel == null) {
            event.error(Errors.UNKNOWN_IDENTITY_PROVIDER);
            context.ignore();
            return;
        }

        // 校验账户管理或账户链接管理角色
        ClientModel accountService = realm.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
        RoleModel manageAccountRole = accountService.getRole(AccountRoles.MANAGE_ACCOUNT);
        if (!user.hasRole(manageAccountRole) || !client.hasScope(manageAccountRole)) {
            RoleModel linkRole = accountService.getRole(AccountRoles.MANAGE_ACCOUNT_LINKS);
            if (!user.hasRole(linkRole) || !client.hasScope(linkRole)) {
                event.error(Errors.NOT_ALLOWED);
                context.ignore();
                return;
            }
        }

        String idpDisplayName = KeycloakModelUtils.getIdentityProviderDisplayName(session, identityProviderModel);
        Response challenge = context.form()
                .setAttribute("idpDisplayName", idpDisplayName)
                .createForm("link-idp-action.ftl");
        context.challenge(challenge);
    }

    /** 确认后重定向至 IdP 完成关联，或根据回调状态结束必需操作。 */
    @Override
    public void processAction(RequiredActionContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        ClientModel client = authSession.getClient();

        if (!Boolean.parseBoolean(authSession.getAuthNote(IdpLinkAction.KC_ACTION_LINKING_IDENTITY_PROVIDER))) {
            // 用户确认关联，重定向至 IdP 完成联邦登录
            String identityProviderAlias = authSession.getClientNote(Constants.KC_ACTION_PARAMETER);

            ClientSessionCode<AuthenticationSessionModel> clientSessionCode = new ClientSessionCode<>(session, realm, authSession);
            clientSessionCode.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
            String noteValue = authSession.getParentSession().getId() + client.getClientId() + identityProviderAlias;
            authSession.setAuthNote(LINKING_IDENTITY_PROVIDER, noteValue);
            authSession.setAuthNote(KC_ACTION_LINKING_IDENTITY_PROVIDER, "true");

            IdentityBrokerService brokerService = new IdentityBrokerService(session);
            Response response = brokerService.performClientInitiatedAccountLogin(identityProviderAlias, clientSessionCode);
            context.challenge(response);
        } else {
            // 用户已在 IdP 完成认证，处理关联结果
            EventBuilder event = context.getEvent();
            event.event(EventType.FEDERATED_IDENTITY_LINK);

            // 状态由 IdentityBrokerService 写入认证会话 note
            String statusNote = authSession.getAuthNote(IdpLinkAction.IDP_LINK_STATUS);
            if (statusNote == null) {
                removeAuthNotes(authSession);
                logger.warn("Not found IDP_LINK_STATUS even if redirect to IDP was already triggered");
                context.failure(Errors.INVALID_REQUEST);
                return;
            }
            RequiredActionContext.KcActionStatus status = RequiredActionContext.KcActionStatus.valueOf(statusNote);
            switch (status) {
                case SUCCESS:
                    context.success();
                    break;
                case CANCELLED:
                    context.cancel();
                    break;
                case ERROR:
                    String error = authSession.getAuthNote(IDP_LINK_ERROR);
                    errorPage(context, error);
                    break;
                default:
                    throw new IllegalStateException("Unknown status in the note idp_link_status: " + status);
            }
            removeAuthNotes(authSession);
        }
    }

    /** 清理 IdP 关联流程相关的认证会话 note。 */
    private void removeAuthNotes(AuthenticationSessionModel authSession) {
        authSession.removeAuthNote(IdpLinkAction.KC_ACTION_LINKING_IDENTITY_PROVIDER);
        authSession.removeAuthNote(IdpLinkAction.IDP_LINK_STATUS);
        authSession.removeAuthNote(IdpLinkAction.IDP_LINK_ERROR);
    }

    /** 反序列化错误并展示错误页。 */
    private void errorPage(RequiredActionContext context, String serializedError) {
        FormMessage formMessage;
        try {
            formMessage = JsonSerialization.readValue(serializedError, FormMessage.class);
        } catch (IOException ioe) {
            throw new RuntimeException("Unexpected error when deserialization of error: " + serializedError);
        }
        Response response = context.getSession().getProvider(LoginFormsProvider.class)
                .setAuthenticationSession(context.getAuthenticationSession())
                .setUser(context.getUser())
                .setErrors(Collections.singletonList(formMessage))
                .createErrorPage(Response.Status.BAD_REQUEST);
        context.getEvent().error(formMessage.getMessage());
        context.challenge(response);
    }

    /** @return 管理控制台显示文本 */
    @Override
    public String getDisplayText() {
        return "Linking Identity Provider";
    }

    @Override
    public void close() {

    }
}
