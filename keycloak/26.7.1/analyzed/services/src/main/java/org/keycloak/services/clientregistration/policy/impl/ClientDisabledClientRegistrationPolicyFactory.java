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
import java.util.List;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientregistration.policy.AbstractClientRegistrationPolicyFactory;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;

/**
 * {@link ClientDisabledClientRegistrationPolicy} 的 Provider 工厂。
 * <p>启用后，新注册客户端默认禁用，需管理员在控制台手动启用。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientDisabledClientRegistrationPolicyFactory extends AbstractClientRegistrationPolicyFactory {

    /** 策略 Provider 标识符 */
    public static final String PROVIDER_ID = "client-disabled";

    /** {@inheritDoc} 创建客户端禁用策略实例 */
    @Override
    public ClientRegistrationPolicy create(KeycloakSession session, ComponentModel model) {
        return new ClientDisabledClientRegistrationPolicy();
    }

    /** {@inheritDoc} 返回策略说明文本 */
    @Override
    public String getHelpText() {
        return "When present, then newly registered client will be disabled and admin needs to manually enable them";
    }

    /** {@inheritDoc} 本策略无可配置属性 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

}
