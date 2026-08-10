/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authentication.authenticators.conditional;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowCallbackFactory;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.Profile;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * 条件 LoA 认证器工厂：注册 {@link ConditionalLoaAuthenticator}，需启用 {@link Profile.Feature#STEP_UP_AUTHENTICATION} 特性。
 * <p>每次 create 返回新实例（非单例），因需持有 {@link KeycloakSession}。</p>
 */
public class ConditionalLoaAuthenticatorFactory implements ConditionalAuthenticatorFactory, AuthenticationFlowCallbackFactory, EnvironmentDependentProviderFactory {

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "conditional-level-of-authentication";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = new AuthenticationExecutionModel.Requirement[]{
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    private static final List<ProviderConfigProperty> CONFIG = ProviderConfigurationBuilder.create()
            .property()
            .name(ConditionalLoaAuthenticator.LEVEL)
            .label(ConditionalLoaAuthenticator.LEVEL)
            .helpText(ConditionalLoaAuthenticator.LEVEL + ".tooltip")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .property()
            .name(ConditionalLoaAuthenticator.MAX_AGE)
            .label(ConditionalLoaAuthenticator.MAX_AGE)
            .helpText(ConditionalLoaAuthenticator.MAX_AGE + ".tooltip")
            .type(ProviderConfigProperty.STRING_TYPE)
            .defaultValue(ConditionalLoaAuthenticator.DEFAULT_MAX_AGE) // 10 hours
            .add()
            .build();

    /** @return 绑定 session 的新 {@link ConditionalLoaAuthenticator} 实例 */
    @Override
    public Authenticator create(KeycloakSession session) {
        return new ConditionalLoaAuthenticator(session);
    }

    @Override
    public void init(Config.Scope config) { }

    @Override
    public void postInit(KeycloakSessionFactory factory) { }

    @Override
    public void close() { }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Condition - Level of Authentication";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    /** @return 条件说明：请求 LoA 未满足时执行子流程并在成功后更新 LoA */
    @Override
    public String getHelpText() {
        return "Flow is executed only if the configured LOA or a higher one has been requested but not yet satisfied. After the flow is successfully finished, the LOA in the session will be updated to value prescribed by this condition.";
    }

    /** @return LoA 级别与 max-age 配置项 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG;
    }

    /** @return 不使用单例，实例由 {@link #create} 创建 */
    @Override
    public ConditionalAuthenticator getSingleton() {
        // 无单例，由 create() 创建实例
        return null;
    }

    /** @return 是否启用 STEP_UP_AUTHENTICATION 特性 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.STEP_UP_AUTHENTICATION);
    }
}
