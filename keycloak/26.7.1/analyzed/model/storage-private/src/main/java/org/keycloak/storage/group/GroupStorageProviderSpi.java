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

package org.keycloak.storage.group;

import java.util.Collections;
import java.util.List;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 组存储 SPI 定义：注册 {@link GroupStorageProvider} 及其工厂到 Keycloak Provider 体系。
 */
public class GroupStorageProviderSpi implements Spi {

    /** 内部 SPI，不对外暴露给第三方扩展列表。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** SPI 名称，对应组件类型标识 {@code group-storage}。 */
    @Override
    public String getName() {
        return "group-storage";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return GroupStorageProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return GroupStorageProviderFactory.class;
    }

    private static final List<ProviderConfigProperty> commonConfig;

    static {
        // 与 CacheableStorageProviderModel、PrioritizedComponentModel 中定义的属性对应
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

    /** 返回所有组存储 Provider 共享的配置属性模板。 */
    public static List<ProviderConfigProperty> commonConfig() {
        return commonConfig;
    }

}
