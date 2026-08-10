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
 * 测试套件用的错误事件触发认证器：向事件日志写入伪造用户与错误消息后立即成功。
 */
public class ErrorEventAuthenticator implements Authenticator, AuthenticatorFactory {
    /** 提供者唯一标识。 */
    public static final String PROVIDER_ID = "test-suite-fire-error-event";
    /** 写入事件日志的错误消息。 */
    public static final String ERROR_MESSAGE = "fire-error-event";
    /** 写入事件的伪造用户 ID。 */
    public static final String FAKE_USERID = "fake-userid";

    /** 触发错误事件并标记认证成功。 */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        context.getEvent().user(FAKE_USERID);
        context.getEvent().error(ERROR_MESSAGE);

        context.success();
    }

    /** {@inheritDoc} 不依赖已有用户。 */
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

    /** {@inheritDoc} 无表单动作处理。 */
    @Override
    public void action(AuthenticationFlowContext context) {
    }

   @Override
    public String getDisplayType() {
        return "Fire Error Event";
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
        return "Testsuite Error event firer authenticator.";
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
