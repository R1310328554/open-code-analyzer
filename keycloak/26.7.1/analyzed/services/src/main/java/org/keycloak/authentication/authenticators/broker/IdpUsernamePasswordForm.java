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

package org.keycloak.authentication.authenticators.broker;

import java.util.Optional;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordForm;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import org.jboss.logging.Logger;

/**
 * Same like classic username+password form, but for use in IdP linking.
 *
 * User identity is optionally established by the preceding idp-create-user-if-unique execution.
 * In this case username field will be pre-filled (but still changeable).
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IdpUsernamePasswordForm extends UsernamePasswordForm {

    private final static Logger log = Logger.getLogger(IdpUsernamePasswordForm.class);

    /** @param session 当前 Keycloak 会话 */
    public IdpUsernamePasswordForm(KeycloakSession session) {
        super(session);
    }

    @Override
    /** 渲染用户名/密码登录表单（含 IdP 重新认证提示）。 */
    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return setupForm(context, formData, getExistingUser(context))
                .setStatus(Response.Status.OK)
                .createLoginUsernamePassword();
    }

    @Override
    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        LoginFormsProvider form = setupForm(context, new MultivaluedHashMap<>(), getExistingUser(context))
                .setExecution(context.getExecution().getId());
        if (error != null) {
            if (field != null) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }
        return createLoginForm(form);
    }

    @Override
    /** 若已有用户则写入上下文，并校验用户名与密码。 */
    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        Optional<UserModel> existingUser = getExistingUser(context);
        existingUser.ifPresent(context::setUser);

        return validateUserAndPassword(context, formData);
    }

    /** 配置登录表单：预填用户名、禁用注册、展示 IdP 重新认证信息及嵌套 broker 提示。 */
    protected LoginFormsProvider setupForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData, Optional<UserModel> existingUser) {
        SerializedBrokeredIdentityContext serializedCtx = SerializedBrokeredIdentityContext.readFromAuthenticationSession(context.getAuthenticationSession(), AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
        if (serializedCtx == null) {
            throw new AuthenticationFlowException("Not found serialized context in clientSession", AuthenticationFlowError.IDENTITY_PROVIDER_ERROR);
        }

        IdentityProviderModel idpModel = context.getSession().identityProviders().getByAlias(serializedCtx.getIdentityProviderId());

        existingUser.ifPresent(u -> formData.putSingle(AuthenticationManager.FORM_USERNAME, u.getUsername()));

        if (isConditionalPasskeysEnabled(existingUser.orElse(null))) {
            webauthnAuth.fillContextForm(context);
        }

        LoginFormsProvider form = context.form()
                .setFormData(formData)
                .setAttribute(LoginFormsProvider.REGISTRATION_DISABLED, true)
                .setInfo(Messages.FEDERATED_IDENTITY_CONFIRM_REAUTHENTICATE_MESSAGE, Validation.isBlank(idpModel.getDisplayName()) ? idpModel.getAlias() : idpModel.getDisplayName());

        SerializedBrokeredIdentityContext serializedCtx0 = SerializedBrokeredIdentityContext.readFromAuthenticationSession(context.getAuthenticationSession(), AbstractIdpAuthenticator.NESTED_FIRST_BROKER_CONTEXT);
        if (serializedCtx0 != null) {
            BrokeredIdentityContext ctx0 = serializedCtx0.deserialize(context.getSession(), context.getAuthenticationSession());
            form.setError(Messages.NESTED_FIRST_BROKER_FLOW_MESSAGE, ctx0.getIdpConfig().getAlias(), ctx0.getUsername());
            context.getAuthenticationSession().setAuthNote(AbstractIdpAuthenticator.NESTED_FIRST_BROKER_CONTEXT, null);
        }

        return form;
    }

    /** 从认证会话读取已关联的本地用户；不存在时返回 empty。 */
    private Optional<UserModel> getExistingUser(AuthenticationFlowContext context) {
        try {
            return Optional.of(AbstractIdpAuthenticator.getExistingUser(context.getSession(), context.getRealm(), context.getAuthenticationSession()));
        } catch (AuthenticationFlowException ex) {
            log.debug("No existing user in authSession", ex);
            return Optional.empty();
        }
    }
}
