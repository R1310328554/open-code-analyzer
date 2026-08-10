/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.organization.authentication.authenticators.broker;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Feature;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * IdP 组织成员入驻认证器的 {@link AuthenticatorFactory}，工厂 ID 为 {@code idp-add-organization-member}。
 * <p>创建 {@link IdpAddOrganizationMemberAuthenticator}，在联邦用户首次登录时将用户加入组织；仅在 {@link Feature#ORGANIZATION} 特性启用时可用。</p>
 */
public class IdpOrganizationAuthenticatorFactory implements AuthenticatorFactory, EnvironmentDependentProviderFactory {

    /** 工厂标识 {@code idp-add-organization-member}。 */
    public static final String ID = "idp-add-organization-member";

    @Override
    /** 创建 {@link IdpAddOrganizationMemberAuthenticator} 实例。 */
    public Authenticator create(KeycloakSession session) {
        return new IdpAddOrganizationMemberAuthenticator();
    }

    @Override
    /** SPI 初始化（当前无操作）。 */
    public void init(Config.Scope config) {

    }

    @Override
    /** 会话工厂后置初始化（当前无操作）。 */
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    /** 关闭工厂（当前无操作）。 */
    public void close() {

    }

    @Override
    /** @return 工厂 ID {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @return 参考类别 organization */
    public String getReferenceCategory() {
        return "organization";
    }

    @Override
    /** @return 本认证器不可配置 */
    public boolean isConfigurable() {
        return false;
    }

    @Override
    /** @return 认证执行要求选项 */
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Organization Member Onboard";
    }

    @Override
    /** @return 管理控制台帮助文本 */
    public String getHelpText() {
        return "Adds a federated user as a member of an organization";
    }

    @Override
    /** @return 允许用户自行配置 */
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    /** @return 配置属性列表（当前为空） */
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    /** @return 组织特性启用时可用 */
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Feature.ORGANIZATION);
    }
}
