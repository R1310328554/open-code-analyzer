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

import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link IdpDetectExistingBrokerUserAuthenticator} 的 SPI 工厂，注册 idp-detect-existing-broker-user 检测现有用户执行器。
 */
public class IdpDetectExistingBrokerUserAuthenticatorFactory implements AuthenticatorFactory {

    /** 提供者 ID：idp-detect-existing-broker-user。 */
    public static final String PROVIDER_ID = "idp-detect-existing-broker-user";
    private static final IdpDetectExistingBrokerUserAuthenticator SINGLETON = new IdpDetectExistingBrokerUserAuthenticator();

    @Override
    /** @return 单例 {@link IdpDetectExistingBrokerUserAuthenticator} */
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    /** @return 提供者 ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** @return 引用分类 detectExistingBrokerUser */
    public String getReferenceCategory() {
        return "detectExistingBrokerUser";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Detect existing broker user";
    }

    @Override
    /** @return 帮助说明：要求本地已存在匹配用户否则报错 */
    public String getHelpText() {
        return "Detect if there is an existing Keycloak account with same email like identity provider. If no, throw an error.";
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    /** @return 无配置项 */
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }
}
