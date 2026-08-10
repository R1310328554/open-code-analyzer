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

package org.keycloak.services.clientregistration.policy.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.OAuth2Constants;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientregistration.policy.AbstractClientRegistrationPolicyFactory;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;

/**
 * {@link ClientScopesClientRegistrationPolicy} 的 Provider 工厂。
 * <p>配置允许的客户端范围白名单，以及是否自动包含领域默认范围。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientScopesClientRegistrationPolicyFactory extends AbstractClientRegistrationPolicyFactory {

    /** 缓存的配置属性列表 */
    private List<ProviderConfigProperty> configProperties;

    // 保留旧 Provider ID 以兼容历史配置
    /** 策略 Provider 标识符（历史名称 allowed-client-templates） */
    public static final String PROVIDER_ID = "allowed-client-templates";

    /** 配置键：允许的客户端范围名称列表 */
    public static final String ALLOWED_CLIENT_SCOPES = "allowed-client-scopes";

    /** 配置键：是否允许使用领域默认/可选范围 */
    public static final String ALLOW_DEFAULT_SCOPES = "allow-default-scopes";

    /** {@inheritDoc} 创建客户端范围白名单策略实例 */
    @Override
    public ClientRegistrationPolicy create(KeycloakSession session, ComponentModel model) {
        return new ClientScopesClientRegistrationPolicy(session, model);
    }

    /** {@inheritDoc} 返回策略说明文本 */
    @Override
    public String getHelpText() {
        return "When present, it allows to specify whitelist of client scopes, which will be allowed in representation of registered (or updated) client";
    }

    /** {@inheritDoc} 返回可配置属性（允许范围列表、是否包含默认范围） */
    @Override
    public List<ProviderConfigProperty> getConfigProperties(KeycloakSession session) {
        List<ProviderConfigProperty> configProps = new LinkedList<>();

        ProviderConfigProperty property;
        property = new ProviderConfigProperty();
        property.setName(ALLOWED_CLIENT_SCOPES);
        property.setLabel("allowed-client-scopes.label");
        property.setHelpText("allowed-client-scopes.tooltip");
        property.setType(ProviderConfigProperty.MULTIVALUED_LIST_TYPE);

        if (session != null) {
            property.setOptions(getClientScopes(session));
        }
        configProps.add(property);

        property = new ProviderConfigProperty();
        property.setName(ALLOW_DEFAULT_SCOPES);
        property.setLabel("allow-default-scopes.label");
        property.setHelpText("allow-default-scopes.tooltip");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setDefaultValue(true);
        configProps.add(property);

        configProperties = configProps;
        return configProperties;
    }

    /** 获取当前领域中所有客户端范围名称（确保包含 openid）。
     * @param session Keycloak 会话
     * @return 范围名称列表
     */
    private List<String> getClientScopes(KeycloakSession session) {
        RealmModel realm = session.getContext().getRealm();
        if (realm == null) {
            return Collections.emptyList();
        } else {
            List<String> scopes = realm.getClientScopesStream().map(ClientScopeModel::getName).collect(Collectors.toList());
            // 若列表中无 openid，则补充
            if (!scopes.contains(OAuth2Constants.SCOPE_OPENID)) {
                scopes.add(OAuth2Constants.SCOPE_OPENID);
            }
            return scopes;
        }
    }

    /** {@inheritDoc} 无会话上下文时委托 {@link #getConfigProperties(KeycloakSession)} */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return getConfigProperties(null);
    }

    /** {@inheritDoc} 校验配置中的范围均存在于当前领域 */
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        List<String> allowedScopesConfig = config.getConfig().getOrDefault(ClientScopesClientRegistrationPolicyFactory.ALLOWED_CLIENT_SCOPES, Collections.emptyList());
        if (!getClientScopes(session).containsAll(allowedScopesConfig)) {
            throw new ComponentValidationException("Client scopes not allowed: " + allowedScopesConfig);
        }
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
