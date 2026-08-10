/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.OAuth2Constants;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 授权类型条件工厂：注册 {@link GrantTypeCondition}。
 * <p>配置项 {@code grant_types} 列出允许触发策略的 OAuth2 授权类型；选项随 Profile 特性动态扩展。</p>
 * @author <a href="mailto:ggrazian@redhat.com">Giuseppe Graziano</a>
 */
public class GrantTypeConditionFactory extends AbstractClientPolicyConditionProviderFactory {

    /** SPI 提供者标识符：grant-type */
    public static final String PROVIDER_ID = "grant-type";
    /** 配置键：期望的 OAuth2 授权类型列表 */
    public static final String GRANT_TYPES = "grant_types";

    /** {@inheritDoc} 创建 {@link GrantTypeCondition} 实例 */
    @Override
    public GrantTypeCondition create(KeycloakSession session) {
        return new GrantTypeCondition(session);
    }

    /** {@inheritDoc} @return {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 当前 grant_type 是否在配置列表中 */
    @Override
    public String getHelpText() {
        return "The condition checks that the grant type used is one of those in the configured list.";
    }

    /** {@inheritDoc} 返回授权类型多选配置项（含特性门控的可选项） */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new ArrayList<>();
        addCommonConfigProperties(configProperties);

        ProviderConfigProperty property = new ProviderConfigProperty(GRANT_TYPES, "Grant Types",
                "The condition evaluates to true if the current grant type is one of those in the list",
                ProviderConfigProperty.MULTIVALUED_LIST_TYPE, null);

        List<String> DEFAULT_GRANT_TYPES_SUPPORTED = Stream.of(OAuth2Constants.AUTHORIZATION_CODE,  OAuth2Constants.IMPLICIT, OAuth2Constants.REFRESH_TOKEN, OAuth2Constants.PASSWORD, OAuth2Constants.CLIENT_CREDENTIALS).collect(Collectors.toList());
        if (Profile.isFeatureEnabled(Profile.Feature.TOKEN_EXCHANGE_STANDARD_V2)) {
            DEFAULT_GRANT_TYPES_SUPPORTED.add(OAuth2Constants.TOKEN_EXCHANGE_GRANT_TYPE);
        }
        if (Profile.isFeatureEnabled(Profile.Feature.DEVICE_FLOW)) {
            DEFAULT_GRANT_TYPES_SUPPORTED.add(OAuth2Constants.DEVICE_CODE_GRANT_TYPE);
        }
        if (Profile.isFeatureEnabled(Profile.Feature.JWT_AUTHORIZATION_GRANT)) {
            DEFAULT_GRANT_TYPES_SUPPORTED.add(OAuth2Constants.JWT_AUTHORIZATION_GRANT);
        }

        property.setOptions(DEFAULT_GRANT_TYPES_SUPPORTED);
        configProperties.add(property);

        return configProperties;
    }
}
