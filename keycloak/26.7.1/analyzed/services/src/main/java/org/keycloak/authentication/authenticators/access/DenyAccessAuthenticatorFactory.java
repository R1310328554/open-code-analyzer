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
 */

package org.keycloak.authentication.authenticators.access;

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
 * 拒绝访问认证器工厂，注册始终拒绝用户访问的认证器，适用于条件流程中满足前置条件后的阻断步骤。
 * @author <a href="mailto:mabartos@redhat.com">Martin Bartos</a>
 */
public class DenyAccessAuthenticatorFactory implements AuthenticatorFactory {
    private static final DenyAccessAuthenticator SINGLETON = new DenyAccessAuthenticator();
    /** 提供者 ID：deny-access-authenticator。 */
    public static final String PROVIDER_ID = "deny-access-authenticator";

    /** 配置项键：自定义拒绝访问错误消息。 */
    public static final String ERROR_MESSAGE = "denyErrorMessage";

    @Override
    /** @return 认证器提供者 ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** 返回单例 {@link DenyAccessAuthenticator} 实例。 */
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Deny access";
    }

    @Override
    /** @return 引用分类（本认证器无分类） */
    public String getReferenceCategory() {
        return null;
    }

    @Override
    /** @return 是否可配置错误消息 */
    public boolean isConfigurable() {
        return true;
    }

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    /** @return 允许的执行要求：REQUIRED 或 DISABLED */
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    /** @return 是否允许用户自助配置 */
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    /** @return 认证器帮助说明文本 */
    public String getHelpText() {
        return "Access will be always denied. Useful for example in the conditional flows to be used after satisfying the previous conditions";
    }

    @Override
    /** @return 可配置属性列表（错误消息） */
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty errorMessage = new ProviderConfigProperty();
        errorMessage.setType(ProviderConfigProperty.STRING_TYPE);
        errorMessage.setName(ERROR_MESSAGE);
        errorMessage.setLabel("Error message");
        errorMessage.setHelpText("Error message which will be shown to the user. " +
                "You can directly define particular message or property, which will be used for mapping the error message f.e `deny-access-role1`. " +
                "If the field is blank, default property 'access-denied' is used.");
        return Collections.singletonList(errorMessage);
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
}
