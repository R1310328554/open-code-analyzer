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

package org.keycloak.policy;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 密码策略管理器 SPI：注册 {@link PasswordPolicyManagerProvider} 及其工厂。
 * <p>内部 SPI，名称 {@code password-policy-manager}。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PasswordPolicyManagerSpi implements Spi {

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code password-policy-manager} */
    @Override
    public String getName() {
        return "password-policy-manager";
    }

    /** @return 提供者接口 {@link PasswordPolicyManagerProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return PasswordPolicyManagerProvider.class;
    }

    /** @return 工厂接口 {@link PasswordPolicyManagerProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return PasswordPolicyManagerProviderFactory.class;
    }

}
