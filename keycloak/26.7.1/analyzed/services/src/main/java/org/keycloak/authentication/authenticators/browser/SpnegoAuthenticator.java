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

import java.net.URI;
import java.util.Map;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.constants.KerberosConstants;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.CredentialValidationOutput;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;

import org.jboss.logging.Logger;

/**
 * SPNEGO/Kerberos 认证器，通过 HTTP Authorization 头中的 Negotiate 令牌发起 SPNEGO 协议协商，支持多轮 Kerberos 握手及可选执行时的 401 挑战重定向。
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SpnegoAuthenticator extends AbstractUsernameFormAuthenticator implements Authenticator{
    private static final Logger logger = Logger.getLogger(SpnegoAuthenticator.class);

    @Override
    /** @return 本步骤不要求上下文中已有用户 */
    public boolean requiresUser() {
        return false;
    }

    @Override
    /** 表单提交时标记为 attempted（SPNEGO 通常由浏览器自动发起）。 */
    public void action(AuthenticationFlowContext context) {
        context.attempted();
        return;
    }

    @Override
    /** 解析 Authorization 头中的 SPNEGO 令牌并委派用户存储校验 Kerberos 凭证。 */
    public void authenticate(AuthenticationFlowContext context) {
        HttpRequest request = context.getHttpRequest();
        String authHeader = request.getHttpHeaders().getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            if (context.getAuthenticationSession().getAuthNote(AuthenticationProcessor.FORKED_FROM) != null) {
                // 若流程由重置凭证分叉而来，跳过 SPNEGO 认证
                context.attempted();
                return;
            }
            Response challenge = challengeNegotiation(context, null);
            context.forceChallenge(challenge);
            return;
        }

        String[] tokens = authHeader.split(" ");
        if (tokens.length == 0) { // assume not supported
            logger.debug("Invalid length of tokens: " + tokens.length);
            context.attempted();
            return;
        }
        if (!KerberosConstants.NEGOTIATE.equalsIgnoreCase(tokens[0])) {
            logger.debug("Unknown scheme " + tokens[0]);
            context.attempted();
            return;
        }
        if (tokens.length != 2) {
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        String spnegoToken = tokens[1];
        UserCredentialModel spnegoCredential = UserCredentialModel.kerberos(spnegoToken);

        CredentialValidationOutput output = context.getSession().users().getUserByCredential(context.getRealm(), spnegoCredential);

        if (output == null) {
            logger.warn("Received kerberos token, but there is no user storage provider that handles kerberos credentials.");
            context.attempted();
            return;
        }
        if (output.getAuthStatus() == CredentialValidationOutput.Status.AUTHENTICATED) {
            context.setUser(output.getAuthenticatedUser());
            if (output.getState() != null && !output.getState().isEmpty()) {
                for (Map.Entry<String, String> entry : output.getState().entrySet()) {
                    context.getAuthenticationSession().setUserSessionNote(entry.getKey(), entry.getValue());
                }
            }
            context.success(UserCredentialModel.KERBEROS);
        } else if (output.getAuthStatus() == CredentialValidationOutput.Status.CONTINUE) {
            String spnegoResponseToken = (String) output.getState().get(KerberosConstants.RESPONSE_TOKEN);
            Response challenge =  challengeNegotiation(context, spnegoResponseToken);
            context.challenge(challenge);
        } else {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
        }
    }

    /** 发送 WWW-Authenticate: Negotiate 挑战；REQUIRED 执行时返回错误页，否则返回可选重定向页。 */
    private Response challengeNegotiation(AuthenticationFlowContext context, final String negotiateToken) {
        String negotiateHeader = negotiateToken == null ? KerberosConstants.NEGOTIATE : KerberosConstants.NEGOTIATE + " " + negotiateToken;

        if (logger.isTraceEnabled()) {
            logger.trace("Sending back " + HttpHeaders.WWW_AUTHENTICATE + ": " + negotiateHeader);
        }
        if (context.getExecution().isRequired()) {
            return context.getSession().getProvider(LoginFormsProvider.class)
                    .setAuthenticationSession(context.getAuthenticationSession())
                    .setResponseHeader(HttpHeaders.WWW_AUTHENTICATE, negotiateHeader)
                    .setError(Messages.KERBEROS_NOT_ENABLED).createErrorPage(Response.Status.UNAUTHORIZED);
        } else {
            return optionalChallengeRedirect(context, negotiateHeader);
        }
    }

    // 仅用于测试：Selenium 执行返回 HTML 挑战中的 JavaScript 重定向
    // redirecting.  Our old Selenium tests expect that the current URL will be the original openid redirect.
    /** 测试标志：是否跳过挑战页中的 JavaScript 自动提交。 */
    public static boolean bypassChallengeJavascript = false;

    /**
     * 返回 401 挑战页，Kerberos 不可用时通过 HTML 表单引导用户改用其他登录方式
     * @param context
     * @param negotiateHeader
     * @return
     */
    /** 构建带 Negotiate 头的 401 HTML 挑战页，含 JavaScript 自动提交表单。 */
    protected Response optionalChallengeRedirect(AuthenticationFlowContext context, String negotiateHeader) {
        String accessCode = context.generateAccessCode();
        URI action = context.getActionUrl(accessCode);

        StringBuilder builder = new StringBuilder();

        builder.append("<HTML>");
        builder.append("<HEAD>");

        builder.append("<TITLE>Kerberos Unsupported</TITLE>");
        builder.append("</HEAD>");
        if (bypassChallengeJavascript) {
            builder.append("<BODY>");

        } else {
            builder.append("<BODY Onload=\"document.forms[0].submit()\">");
        }
        builder.append("<FORM METHOD=\"POST\" ACTION=\"" + action.toString() + "\">");
        builder.append("<NOSCRIPT>");
        builder.append("<P>JavaScript is disabled. We strongly recommend to enable it. You were unable to login via Kerberos.  Click the button below to login via an alternative method .</P>");
        builder.append("<INPUT name=\"continue\" TYPE=\"SUBMIT\" VALUE=\"CONTINUE\" />");
        builder.append("</NOSCRIPT>");

        builder.append("</FORM></BODY></HTML>");
        return Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, negotiateHeader)
                .type(MediaType.TEXT_HTML_TYPE)
                .entity(builder.toString()).build();
    }


    @Override
    /** @return 始终已配置（对所有用户适用） */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {

    }
}
