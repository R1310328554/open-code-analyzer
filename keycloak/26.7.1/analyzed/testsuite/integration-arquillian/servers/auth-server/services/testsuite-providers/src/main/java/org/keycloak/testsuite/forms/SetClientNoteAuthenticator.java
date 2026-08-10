/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.testsuite.forms;

import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;

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
import org.keycloak.sessions.AuthenticationSessionModel;

import org.jboss.logging.Logger;

/**
 * 将 HTTP 请求中以 {@link #PREFIX} 开头的表单参数写入认证会话客户端备注的认证器。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SetClientNoteAuthenticator implements Authenticator, AuthenticatorFactory {

    protected static final Logger logger = Logger.getLogger(SetClientNoteAuthenticator.class);

    /** 提供者唯一标识。 */
    public static final String PROVIDER_ID = "set-client-note-authenticator";

    // 以此前缀开头的查询参数将保存为认证会话的客户端备注
    /** 表单参数名前缀；去掉前缀后的部分作为 client note 键。 */
    public static final String PREFIX = "note-";

    /** 扫描表单参数，将 note-* 键值对写入认证会话并标记成功。 */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> inputData = context.getHttpRequest().getDecodedFormParameters();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        inputData.keySet().stream()
                .filter(paramName -> paramName.startsWith(PREFIX))
                .forEach(paramName -> {
                    String key = paramName.substring(PREFIX.length());
                    String value = inputData.getFirst(paramName);
                    logger.infof("Set authentication session client note %s=%s", key, value);
                    authSession.setClientNote(key, value);
                });

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
        return "Set Client Note Authenticator";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    /** 本认证器仅支持 REQUIRED 执行要求。 */
    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED
    };

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
        return "Set client note of specified name with the specified value to the authenticationSession.";
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
