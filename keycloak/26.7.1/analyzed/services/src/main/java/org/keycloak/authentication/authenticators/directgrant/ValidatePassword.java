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

package org.keycloak.authentication.authenticators.directgrant;

import java.util.LinkedList;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.managers.AuthenticationManager;

/**
 * Direct Grant 密码校验认证器：从表单参数 password 读取并验证用户密码。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ValidatePassword extends AbstractDirectGrantAuthenticator {

    /** Provider ID：direct-grant-validate-password。 */
    public static final String PROVIDER_ID = "direct-grant-validate-password";

    @Override
    /** 校验密码并在认证会话中标记 PASSWORD_VALIDATED。 */
    public void authenticate(AuthenticationFlowContext context) {
        String password = retrievePassword(context);
        boolean valid = context.getUser().credentialManager().isValid(UserCredentialModel.password(password));
        if (!valid) {
            context.getEvent().user(context.getUser());
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            Response challengeResponse = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_grant", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        context.getAuthenticationSession().setAuthNote(AuthenticationManager.PASSWORD_VALIDATED, "true");
        context.success(PasswordCredentialModel.TYPE);
    }

    @Override
    /** @return 校验密码需要前置步骤已识别用户 */
    public boolean requiresUser() {
        return true;
    }

    @Override
    /** @return 密码校验始终可用 */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }


    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Password";
    }

    @Override
    public String getReferenceCategory() {
        return PasswordCredentialModel.TYPE;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    /** @return 帮助说明：校验 Direct Grant 请求中的 password 表单参数 */
    public String getHelpText() {
        return "Validates the password supplied as a 'password' form parameter in direct grant request";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new LinkedList<>();
    }

    @Override
    /** @return Provider ID */
    public String getId() {
        return PROVIDER_ID;
    }

    /** 从解码后的表单参数中提取 password 字段。 */
    protected String retrievePassword(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
        return inputData.getFirst(CredentialRepresentation.PASSWORD);
    }
}
