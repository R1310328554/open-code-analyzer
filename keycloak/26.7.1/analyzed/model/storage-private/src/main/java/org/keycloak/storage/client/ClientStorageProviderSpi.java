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

package org.keycloak.storage.client;

import java.util.Collections;
import java.util.List;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 客户端存储 Provider SPI：注册 {@link ClientStorageProvider} 及其工厂，并定义通用配置项。
 * <p>
 * 通用配置包括 enabled、priority、cachePolicy 及缓存失效相关属性。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientStorageProviderSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    /** SPI 名称：{@code client-storage}。 */
    @Override
    public String getName() {
        return "client-storage";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return ClientStorageProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ClientStorageProviderFactory.class;
    }

    private static final List<ProviderConfigProperty> commonConfig;

    static {
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

    /** 返回所有 ClientStorageProvider 实现共用的配置属性列表。 */
    public static List<ProviderConfigProperty> commonConfig() {
        return commonConfig;

    }

}
