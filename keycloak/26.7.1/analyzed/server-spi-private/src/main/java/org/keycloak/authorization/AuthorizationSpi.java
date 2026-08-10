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

package org.keycloak.authorization;

import org.keycloak.common.Profile;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 授权服务 SPI 描述符，注册 {@link AuthorizationProvider} 与 {@link AuthorizationProviderFactory}。
 * <p>内部 SPI，名称 {@code authorization}；仅在启用 {@link Profile.Feature#AUTHORIZATION} 时可用。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class AuthorizationSpi implements Spi {
    /** 内部 SPI，不对外暴露。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** 返回 SPI 名称 {@code authorization}。 */
    @Override
    public String getName() {
        return "authorization";
    }

    /** 提供者接口类型 {@link AuthorizationProvider}。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return AuthorizationProvider.class;
    }

    /** 工厂接口类型 {@link AuthorizationProviderFactory}。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AuthorizationProviderFactory.class;
    }

    /** 是否启用授权特性。 */
    @Override
    public boolean isEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.AUTHORIZATION);
    }
}
