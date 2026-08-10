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

package org.keycloak.testsuite.forms;

import java.util.List;

import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 测试套件用的“点击通过”认证器：展示条款页面，用户确认后继续流程。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClickThroughAuthenticator implements Authenticator, AuthenticatorFactory {
    /** 提供者唯一标识。 */
    public static final String PROVIDER_ID = "testsuite-dummy-click-through";

    /** 展示 terms.ftl 表单作为挑战页面。 */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Response challenge = context.form().createForm("terms.ftl");
        context.challenge(challenge);
    }

    /** {@inheritDoc} 本认证器不依赖已有用户。 */
    @Override
    public boolean requiresUser() {
        return false;
    }

    /** {@inheritDoc} 对所有用户均视为已配置。 */
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    /** {@inheritDoc} 无需设置必需操作。 */
    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    /** 处理表单提交；若用户取消则重新展示挑战页，否则标记成功。 */
    @Override
    public void action(AuthenticationFlowContext context) {
        if (context.getHttpRequest().getDecodedFormParameters().containsKey("cancel")) {
            authenticate(context);
            return;
        }

        context.success();
    }

   @Override
    public String getDisplayType() {
        return "Testsuite Dummy Click Thru";
    }

    @Override
    public String getReferenceCategory() {
        return null;
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
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Testsuite Dummy authenticator.  User needs to click through the page to continue.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return this;
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
}
