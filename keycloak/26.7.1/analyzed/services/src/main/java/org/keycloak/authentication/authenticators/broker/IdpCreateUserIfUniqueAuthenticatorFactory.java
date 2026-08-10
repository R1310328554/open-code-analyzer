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

import java.util.ArrayList;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link IdpCreateUserIfUniqueAuthenticator} 的 SPI 工厂，注册 idp-create-user-if-unique 唯一时创建用户执行器。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IdpCreateUserIfUniqueAuthenticatorFactory implements AuthenticatorFactory {

    /** 提供者 ID：idp-create-user-if-unique。 */
    public static final String PROVIDER_ID = "idp-create-user-if-unique";
    static IdpCreateUserIfUniqueAuthenticator SINGLETON = new IdpCreateUserIfUniqueAuthenticator();

    /** 配置项键：注册成功后是否要求用户更新密码。 */
    public static final String REQUIRE_PASSWORD_UPDATE_AFTER_REGISTRATION = "require.password.update.after.registration";

    @Override
    /** @return 单例 {@link IdpCreateUserIfUniqueAuthenticator} */
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
    /** @return 引用分类 createUserIfUnique */
    public String getReferenceCategory() {
        return "createUserIfUnique";
    }

    @Override
    /** @return 可配置注册后密码更新选项 */
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Create User If Unique";
    }

    @Override
    /** @return 帮助说明：检测重复邮箱/用户名并在唯一时创建用户 */
    public String getHelpText() {
        return "Detect if there is existing Keycloak account with same email like identity provider. If no, create new user";
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(REQUIRE_PASSWORD_UPDATE_AFTER_REGISTRATION);
        property.setLabel("Require Password Update After Registration");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setHelpText("If this option is true and new user is successfully imported from Identity Provider to Keycloak (there is no duplicated email or username detected in Keycloak DB), then this user is required to update his password");
        configProperties.add(property);
    }


    @Override
    /** @return 配置属性（注册后强制更新密码） */
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
