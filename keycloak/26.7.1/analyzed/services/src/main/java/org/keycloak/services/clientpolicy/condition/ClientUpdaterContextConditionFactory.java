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

package org.keycloak.services.clientpolicy.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 客户端更新上下文条件工厂：注册 {@link ClientUpdaterContextCondition}。
 * <p>配置项 {@code update-client-source} 指定允许的创建/更新来源（Admin、匿名、各类访问令牌）。</p>
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientUpdaterContextConditionFactory extends AbstractClientPolicyConditionProviderFactory {

    /** SPI 提供者标识符：client-updater-context */
    public static final String PROVIDER_ID = "client-updater-context";

    /** 配置键：客户端更新来源（认证方式） */
    public static final String UPDATE_CLIENT_SOURCE = "update-client-source";

    /** 来源：已认证用户/服务账户的 Admin REST 请求 */
    public static final String BY_AUTHENTICATED_USER = "ByAuthenticatedUser";
    /** 来源：匿名动态客户端注册请求 */
    public static final String BY_ANONYMOUS = "ByAnonymous";
    /** 来源：使用初始访问令牌的 OIDC 客户端注册 */
    public static final String BY_INITIAL_ACCESS_TOKEN = "ByInitialAccessToken";
    /** 来源：使用注册访问令牌的 OIDC 客户端更新 */
    public static final String BY_REGISTRATION_ACCESS_TOKEN = "ByRegistrationAccessToken";

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        addCommonConfigProperties(configProperties);

        ProviderConfigProperty property;
        property = new ProviderConfigProperty(UPDATE_CLIENT_SOURCE, "Update Client Context", "Specifies the context how is client created or updated. " +
                "ByInitialAccessToken is usually OpenID Connect client registration with the initial access token. " +
                "ByRegistrationAccessToken is usually OpenID Connect client update request with the registration access token. " +
                "ByAuthenticatedUser is usually Admin REST request with the token on behalf of authenticated user or client (service account). ByAnonymous is usually anonymous OpenID Client registration request.",
                ProviderConfigProperty.MULTIVALUED_LIST_TYPE, BY_AUTHENTICATED_USER);
        List<String> updateProfileValues = Arrays.asList(BY_AUTHENTICATED_USER, BY_ANONYMOUS, BY_INITIAL_ACCESS_TOKEN, BY_REGISTRATION_ACCESS_TOKEN);
        property.setOptions(updateProfileValues);
        configProperties.add(property);
    }

    /** {@inheritDoc} 创建 {@link ClientUpdaterContextCondition} 实例 */
    @Override
    public ClientPolicyConditionProvider create(KeycloakSession session) {
        return new ClientUpdaterContextCondition(session);
    }

    /** {@inheritDoc} @return {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 按客户端创建/更新上下文判断是否应用策略 */
    @Override
    public String getHelpText() {
        return "The condition checks the context how is client created/updated to determine whether the policy is applied. For example it checks if client is created with admin REST API or OIDC dynamic client registration. And for the letter case if it is ANONYMOUS client registration or AUTHENTICATED client registration with Initial access token or Registration access token and so on.";
    }

    /** {@inheritDoc} 返回更新来源多选配置项 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
