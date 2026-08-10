/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization.policy.provider;

import org.keycloak.common.Profile;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 策略 SPI 描述符：注册 {@link PolicyProvider} 与 {@link PolicyProviderFactory}。
 * <p>内部 SPI，名称 {@code policy}；仅在启用 {@link Profile.Feature#AUTHORIZATION} 时可用。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PolicySpi implements Spi {
    /** 内部 SPI，不对外暴露。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** 返回 SPI 名称 {@code policy}。 */
    @Override
    public String getName() {
        return "policy";
    }

    /** 提供者接口类型 {@link PolicyProvider}。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return PolicyProvider.class;
    }

    /** 工厂接口类型 {@link PolicyProviderFactory}。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return PolicyProviderFactory.class;
    }

    /** 是否启用授权特性。 */
    @Override
    public boolean isEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.AUTHORIZATION);
    }
}
