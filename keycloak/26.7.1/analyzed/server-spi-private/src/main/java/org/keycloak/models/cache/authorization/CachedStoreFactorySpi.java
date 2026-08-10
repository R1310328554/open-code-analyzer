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

package org.keycloak.models.cache.authorization;

import org.keycloak.common.Profile;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 授权缓存 SPI，注册 {@link CachedStoreFactoryProvider} 提供者类型。
 * <p>仅在 {@link org.keycloak.common.Profile.Feature#AUTHORIZATION} 特性启用时可用。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class CachedStoreFactorySpi implements Spi {
    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** SPI 名称：{@code authorizationCache}。 */
    @Override
    public String getName() {
        return "authorizationCache";
    }

    /** 授权缓存存储工厂提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return CachedStoreFactoryProvider.class;
    }

    /** 授权缓存工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return CachedStoreProviderFactory.class;
    }

    /** 是否启用：取决于 AUTHORIZATION 特性开关。 */
    @Override
    public boolean isEnabled() {
        return Profile.isFeatureEnabled(Profile.Feature.AUTHORIZATION);
    }
}
