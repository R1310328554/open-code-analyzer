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

package org.keycloak.services.clientregistration.policy;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 客户端注册策略 SPI 定义。
 * <p>将 {@link ClientRegistrationPolicy} 与 {@link ClientRegistrationPolicyFactory} 注册到 Keycloak 提供者框架。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientRegistrationPolicySpi implements Spi {

    /** {@inheritDoc} 内部 SPI，不对外暴露 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** {@inheritDoc} SPI 名称：{@code client-registration-policy} */
    @Override
    public String getName() {
        return "client-registration-policy";
    }

    /** {@inheritDoc} Provider 接口类 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ClientRegistrationPolicy.class;
    }

    /** {@inheritDoc} Provider 工厂接口类 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ClientRegistrationPolicyFactory.class;
    }
}
