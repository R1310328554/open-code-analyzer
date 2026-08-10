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

import java.util.LinkedList;
import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ConfigurationValidationHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientregistration.policy.AbstractClientRegistrationPolicyFactory;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;

/**
 * {@link MaxClientsClientRegistrationPolicy} 的 Provider 工厂。
 * <p>配置每个领域允许的最大客户端数量，超出时拒绝动态注册。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MaxClientsClientRegistrationPolicyFactory extends AbstractClientRegistrationPolicyFactory {

    /** 配置键：最大客户端数量 */
    public static final String MAX_CLIENTS = "max-clients";
    /** 最大客户端数配置属性定义 */
    public static final ProviderConfigProperty MAX_CLIENTS_PROPERTY = new ProviderConfigProperty();

    /** 未配置时的默认客户端上限 */
    public static final int DEFAULT_MAX_CLIENTS = 200;

    /** 静态初始化的配置属性列表 */
    private static List<ProviderConfigProperty> configProperties = new LinkedList<>();

    static {
        MAX_CLIENTS_PROPERTY.setName(MAX_CLIENTS);
        MAX_CLIENTS_PROPERTY.setLabel("max-clients.label");
        MAX_CLIENTS_PROPERTY.setHelpText("max-clients.tooltip");
        MAX_CLIENTS_PROPERTY.setType(ProviderConfigProperty.STRING_TYPE);
        MAX_CLIENTS_PROPERTY.setDefaultValue(String.valueOf(DEFAULT_MAX_CLIENTS));
        configProperties.add(MAX_CLIENTS_PROPERTY);
    }

    /** 策略 Provider 标识符 */
    public static final String PROVIDER_ID = "max-clients";

    /** {@inheritDoc} 创建客户端数量上限策略实例 */
    @Override
    public ClientRegistrationPolicy create(KeycloakSession session, ComponentModel model) {
        return new MaxClientsClientRegistrationPolicy(session, model);
    }

    /** {@inheritDoc} 返回策略说明文本 */
    @Override
    public String getHelpText() {
        return "When present, then it won't be allowed to register new client if count of existing clients in realm is same or bigger than configured limit";
    }

    /** {@inheritDoc} 返回最大客户端数配置属性 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 校验 max-clients 为合法整数 */
    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        ConfigurationValidationHelper.check(config)
                .checkInt(MAX_CLIENTS_PROPERTY, true);
    }
}
