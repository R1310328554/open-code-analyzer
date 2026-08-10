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
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.authentication.authenticators.util.AuthenticatorUtils;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;

import static org.keycloak.authentication.authenticators.util.AuthenticatorUtils.getDisabledByBruteForceEventError;

/**
 * Direct Grant 用户名校验认证器：从表单参数 username 查找用户，并处理服务账号、暴力破解锁定及禁用账户等边界情况。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ValidateUsername extends AbstractDirectGrantAuthenticator {

    /** Provider ID：direct-grant-validate-username。 */
    public static final String PROVIDER_ID = "direct-grant-validate-username";

    @Override
    /** 解析用户名、查找用户并校验启用状态与暴力破解限制。 */
    public void authenticate(AuthenticationFlowContext context) {
        String username = retrieveUsername(context);
        if (username == null) {
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", "Missing parameter: username");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        UserModel user = null;
        try {
            user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
        } catch (ModelDuplicateException mde) {
            ServicesLogger.LOGGER.modelDuplicateException(mde);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }


        if (user == null) {
            AuthenticatorUtils.dummyHash(context);
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_grant", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }

        if (user.getServiceAccountClientLink() != null) {
            AuthenticatorUtils.dummyHash(context);
            context.getEvent().detail(Details.REASON, "User is a service account");
            context.getEvent().error(Errors.INVALID_USER);
            Response challengeResponse = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_grant", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }

        String bruteForceError = getDisabledByBruteForceEventError(context, user);
        if (bruteForceError != null) {
            AuthenticatorUtils.dummyHash(context);
            context.getEvent().user(user);
            context.getEvent().error(bruteForceError);
            Response challengeResponse = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_grant", "Invalid user credentials");
            context.forceChallenge(challengeResponse);
            return;
        }

        if (!user.isEnabled()) {
            context.getEvent().user(user);
            context.getEvent().error(Errors.USER_DISABLED);
            String password = retrievePassword(context);
            String errorDescription = user.credentialManager().isValid(UserCredentialModel.password(password))
                    ? "Account disabled"
                    : "Invalid user credentials";
            Response challengeResponse = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_grant", errorDescription);
            context.forceChallenge(challengeResponse);
            return;
        }
        context.setUser(user);
        context.success();
    }

    @Override
    /** @return 本步骤负责识别用户，不要求前置用户 */
    public boolean requiresUser() {
        return false;
    }

    @Override
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
        return "Username Validation";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    /** 执行要求选项：仅 REQUIRED。 */
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    /** @return 帮助说明：校验 Direct Grant 请求中的 username 表单参数 */
    public String getHelpText() {
        return "Validates the username supplied as a 'username' form parameter in direct grant request";
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
 
    /** 从解码后的表单参数中提取 username 字段。 */
    protected String retrieveUsername(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
        return inputData.getFirst(AuthenticationManager.FORM_USERNAME);
    }

    /** 从解码后的表单参数中提取 password 字段（用于禁用账户时的凭证探测）。 */
    protected String retrievePassword(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
        return inputData.getFirst(CredentialRepresentation.PASSWORD);
    }
}
