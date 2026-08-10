/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.authentication;

import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 按钮式认证器：向用户展示 HTML 表单，点击提交按钮后继续认证流程。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class PushButtonAuthenticator implements Authenticator {

    /**
     * 生成包含提交按钮的 HTML 挑战页面，等待用户点击后继续。
     *
     * @param context 认证流程上下文
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        String accessCode = context.generateAccessCode();
        String actionUrl = context.getActionUrl(accessCode).toString();

        StringBuilder response = new StringBuilder("<html><head><title>PushTheButton</title></head><body>");

        UserModel user = context.getUser();
        if (user == null) {
            response.append("No authenticated user<br>");
        } else {
            response.append("Authenticated user: " + user.getUsername() + "<br>");
        }

        response.append("<form method='POST' action='" + actionUrl + "'>");
        response.append(" This is the Test Approver. Press login to continue.<br>");
        response.append(" <input type='submit' name='submit1' value='Submit' />");
        response.append("</form></body></html>");
        String html = response.toString();

        Response jaxrsResponse = Response
                .status(Response.Status.OK)
                .type("text/html")
                .entity(html)
                .build();

        context.challenge(jaxrsResponse);

//        Response challenge = context.form().createForm("login-approve.ftl");
//        context.challenge(challenge);
    }

    /** {@inheritDoc} 用户提交表单后直接标记认证成功。 */
    @Override
    public void action(AuthenticationFlowContext context) {
        context.success();
    }

    /** {@inheritDoc} 不依赖已登录用户。 */
    @Override
    public boolean requiresUser() {
        return false;
    }

    /** {@inheritDoc} 始终返回 {@code false}，表示无需额外配置。 */
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }


    @Override
    public void close() {

    }
}
