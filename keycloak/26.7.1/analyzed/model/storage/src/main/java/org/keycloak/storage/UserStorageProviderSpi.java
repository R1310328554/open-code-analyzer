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

package org.keycloak.storage;

import java.util.Collections;
import java.util.List;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 用户存储 Provider 的 {@link Spi} 实现，注册 {@link UserStorageProvider} 及其工厂，
 * 并定义各用户存储实现共享的通用配置项（同步周期、缓存策略等）。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class UserStorageProviderSpi implements Spi {

    /** {@inheritDoc} 用户存储为可插拔扩展，非内部 SPI。 */
    @Override
    public boolean isInternal() {
        return false;
    }

    /** {@inheritDoc} SPI 名称为 {@code storage}。 */
    @Override
    public String getName() {
        return "storage";
    }

    /** {@inheritDoc} 关联的 Provider 类型为 {@link UserStorageProvider}。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return UserStorageProvider.class;
    }

    /** {@inheritDoc} 关联的工厂类型为 {@link UserStorageProviderFactory}。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return UserStorageProviderFactory.class;
    }

    /** 所有用户存储 Provider 共享的通用配置属性列表。 */
    private static final List<ProviderConfigProperty> commonConfig;

    static {
        List<ProviderConfigProperty> config = ProviderConfigurationBuilder.create()
                .property()
                .name("enabled").type(ProviderConfigProperty.BOOLEAN_TYPE).add()
                .property()
                .name("priority").type(ProviderConfigProperty.STRING_TYPE).add()
                .property()
                .name("fullSyncPeriod").type(ProviderConfigProperty.STRING_TYPE).add()
                .property()
                .name("changedSyncPeriod").type(ProviderConfigProperty.STRING_TYPE).add()
                .property()
                .name("lastSync").type(ProviderConfigProperty.STRING_TYPE).add()
                .property()
                .name("batchSizeForSync").type(ProviderConfigProperty.STRING_TYPE).add()
                .property()
                .name("importEnabled").type(ProviderConfigProperty.BOOLEAN_TYPE).add()
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
                .property()
                .name("removeInvalidUsersEnabled").type(ProviderConfigProperty.BOOLEAN_TYPE).add()
                .build();
        commonConfig = Collections.unmodifiableList(config);
    }

    /**
     * 返回用户存储 Provider 的通用配置属性（启用、优先级、同步周期、缓存策略等）。
     *
     * @return 不可变的通用配置属性列表
     */
    public static List<ProviderConfigProperty> commonConfig() {
        return commonConfig;

    }

}
