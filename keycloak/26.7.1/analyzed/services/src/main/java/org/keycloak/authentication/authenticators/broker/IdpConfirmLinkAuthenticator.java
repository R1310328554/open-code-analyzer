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

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.CommonClientSessionModel;

/**
 * 确认关联认证器：当检测到重复账户时展示确认页，用户可选择关联现有账户或更新 IdP 资料以避免冲突。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IdpConfirmLinkAuthenticator extends AbstractIdpAuthenticator {

    @Override
    /** 无重复则 attempted；否则展示 IdP 关联确认页。 */
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        String existingUserInfo = authSession.getAuthNote(EXISTING_USER_INFO);
        if (existingUserInfo == null) {
            ServicesLogger.LOGGER.noDuplicationDetected();
            context.attempted();
            return;
        }

        // 若资料审查步骤未成功执行，则隐藏「更新资料」按钮
        boolean hideReviewButton = authSession.getExecutionStatus().entrySet().stream()
                .filter(entry -> CommonClientSessionModel.ExecutionStatus.SUCCESS.equals(entry.getValue()))
                .map(entry -> context.getRealm().getAuthenticationExecutionById(entry.getKey()))
                .filter(exec -> IdpReviewProfileAuthenticatorFactory.PROVIDER_ID.equals(exec.getAuthenticator()))
                .findAny()
                .isEmpty();

        ExistingUserInfo duplicationInfo = ExistingUserInfo.deserialize(existingUserInfo);
        Response challenge = context.form()
                .setStatus(Response.Status.OK)
                .setAttribute(LoginFormsProvider.IDENTITY_PROVIDER_BROKER_CONTEXT, brokerContext)
                .setAttribute("hideReviewButton", hideReviewButton ? Boolean.TRUE : null)
                .setError(Messages.FEDERATED_IDENTITY_CONFIRM_LINK_MESSAGE, duplicationInfo.getDuplicateAttributeName(), duplicationInfo.getDuplicateAttributeValue())
                .createIdpLinkConfirmLinkPage();
        context.challenge(challenge);
    }

    @Override
    /** 处理 updateProfile（重置流程并强制更新资料）或 linkAccount（确认关联）。 */
    protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String action = formData.getFirst("submitAction");
        if (action != null && action.equals("updateProfile")) {
            context.resetFlow(() -> {
                AuthenticationSessionModel authSession = context.getAuthenticationSession();

                serializedCtx.saveToAuthenticationSession(authSession, BROKERED_CONTEXT_NOTE);
                authSession.setAuthNote(ENFORCE_UPDATE_PROFILE, "true");
            });
        } else if (action != null && action.equals("linkAccount")) {
            context.success();
        } else {
            throw new AuthenticationFlowException("Unknown action: " + action,
                    AuthenticationFlowError.INTERNAL_ERROR);
        }
    }

    @Override
    /** @return 本步骤不要求上下文中已有用户 */
    public boolean requiresUser() {
        return false;
    }

    @Override
    /** @return 始终未针对特定用户配置 */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }
}
