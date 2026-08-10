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

package org.keycloak.authentication.authenticators.browser;

import java.util.Objects;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 用户名表单认证器，仅展示用户名输入页并校验用户身份；若上下文中已有用户且无可用联邦 IdP 则可跳过表单。
 */
public final class UsernameForm extends UsernamePasswordForm {

    /** 无会话参数的默认构造器。 */
    public UsernameForm() {
        super();
    }

    /** @param session 当前 Keycloak 会话 */
    public UsernameForm(KeycloakSession session) {
        super(session);
    }

    @Override
    /** 若用户已设置且无关联 IdP 可跳过，否则调用父类展示用户名表单。 */
    public void authenticate(AuthenticationFlowContext context) {
        if (context.getUser() != null) {
            // 重新认证时可跳过表单，除非用户有关联 IdP 可用于 IdP 登录
            if (!this.hasLinkedBrokers(context)) {
                context.success();
                return;
            }
        }
        super.authenticate(context);
    }

    @Override
    /** 仅校验并解析用户名（不校验密码）。 */
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return validateUser(context, formData);
    }

    @Override
    /** @return 用户名登录表单挑战响应 */
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();

        if (!formData.isEmpty()) forms.setFormData(formData);

        return forms.createLoginUsername();
    }

    @Override
    /** @return 仅含用户名字段的登录表单 */
    protected Response createLoginForm(LoginFormsProvider form) {
        return form.createLoginUsername();
    }

    @Override
    /** @return 用户名校验失败时的默认错误消息键 */
    protected String getDefaultChallengeMessage(AuthenticationFlowContext context) {
        if (context.getRealm().isLoginWithEmailAllowed())
            return Messages.INVALID_USERNAME_OR_EMAIL;
        return Messages.INVALID_USERNAME;
    }

    /**
     * 检查上下文用户（若已设置）是否关联了可用于认证的联邦 IdP；认证会话 brokered context 中已有的 IdP 会被过滤。
     * Checks if the context user, if it has been set, is currently linked to any IDPs they could use to authenticate.
     * If the auth session has an existing IDP in the brokered context, it is filtered out.
     *
     * @param context 认证流程上下文 {@link AuthenticationFlowContext}
     * @return 若用户有关联且可用的联邦 IdP 则 {@code true}，否则 {@code false}
     */
    /** 判断当前用户是否存在除 brokered context 外可用的联邦 IdP。 */
    private boolean hasLinkedBrokers(AuthenticationFlowContext context) {
        KeycloakSession session = context.getSession();
        UserModel user = context.getUser();
        if (user == null) {
            return false;
        }
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(authSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
        final IdentityProviderModel existingIdp = (serializedCtx == null) ? null : serializedCtx.deserialize(session, authSession).getIdpConfig();

        return session.users().getFederatedIdentitiesStream(session.getContext().getRealm(), user)
                .map(fedIdentity -> session.identityProviders().getByAlias(fedIdentity.getIdentityProvider()))
                .filter(Objects::nonNull)
                .anyMatch(idpModel -> existingIdp == null || !Objects.equals(existingIdp.getAlias(), idpModel.getAlias()));

    }
}
