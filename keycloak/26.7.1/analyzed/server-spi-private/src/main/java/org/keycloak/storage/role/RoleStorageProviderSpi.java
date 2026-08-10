/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.storage.role;

import java.util.Collections;
import java.util.List;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 角色存储 SPI：注册 {@link RoleStorageProvider} 及工厂。
 * <p>内部 SPI，名称 {@code role-storage}；定义缓存与优先级等通用配置。</p>
 */
public class RoleStorageProviderSpi implements Spi {

    /** @return 始终为 {@code true}，表示内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code role-storage} */
    @Override
    public String getName() {
        return "role-storage";
    }

    /** @return 提供者接口 {@link RoleStorageProvider} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return RoleStorageProvider.class;
    }

    /** @return 工厂接口 {@link RoleStorageProviderFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return RoleStorageProviderFactory.class;
    }

    private static final List<ProviderConfigProperty> commonConfig;

    static {
        // 对应 CacheableStorageProviderModel 与 PrioritizedComponentModel 中定义的属性
        List<ProviderConfigProperty> config = ProviderConfigurationBuilder.create()
                .property()
                .name("enabled").type(ProviderConfigProperty.BOOLEAN_TYPE).add()
                .property()
                .name("priority").type(ProviderConfigProperty.STRING_TYPE).add()
                 .property()
                .name("cachePolicy").type(ProviderConfigProperty.STRING_TYPE).add()
                .property()
                .name("maxLifespan").type(ProviderConfigProperty.STRING_TYPE).add()
                .property()
                .name("evictionHour").type(ProviderConfigProperty.STRING_TYPE).add()
                .property()
                .name("evictionMinute").type(ProviderConfigProperty.STRING_TYPE).add()
                .property()
                .name("evictionDay").type(ProviderConfigProperty.STRING_TYPE).add()
                .property()
                .name("cacheInvalidBefore").type(ProviderConfigProperty.STRING_TYPE).add()
                .build();
        commonConfig = Collections.unmodifiableList(config);
    }

    /** @return 所有角色存储实现共用的不可变配置属性列表 */
    public static List<ProviderConfigProperty> commonConfig() {
        return commonConfig;
    }

}
