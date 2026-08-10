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

package org.keycloak.models;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.provider.InvalidationHandler;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderEventManager;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * Keycloak 会话工厂：创建 {@link KeycloakSession} 并管理 SPI/Provider 工厂注册。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface KeycloakSessionFactory extends ProviderEventManager, InvalidationHandler {

    /** @return 新建 Keycloak 会话实例 */
    KeycloakSession create();

    /** @return 已注册 SPI 集合 */
    Set<Spi> getSpis();

    /** @param providerClass Provider 类型
     * @return 对应 SPI */
    Spi getSpi(Class<? extends Provider> providerClass);

    /** @param clazz Provider 类型
     * @return 默认 Provider 工厂 */
    <T extends Provider> ProviderFactory<T> getProviderFactory(Class<T> clazz);

    /** @param clazz Provider 类型
     * @param id 工厂 ID
     * @return 指定 ID 的 Provider 工厂 */
    <T extends Provider> ProviderFactory<T> getProviderFactory(Class<T> clazz, String id);

    /** 按 Realm 组件配置解析 Provider 工厂。
     * @param clazz Provider 类型
     * @param realmId Realm ID
     * @param componentId 组件 ID
     * @param modelGetter 组件模型获取函数
     * @return Provider 工厂 */
    <T extends Provider> ProviderFactory<T> getProviderFactory(Class<T> clazz, String realmId, String componentId, Function<KeycloakSessionFactory, ComponentModel> modelGetter);

    /**
     * 返回给定 Provider 类型的所有工厂流。
     * Returns stream of provider factories for the given provider.
     * @param clazz {@code Class<? extends Provider>}
     * @return {@code Stream<ProviderFactory>} Stream of provider factories. Never returns {@code null}.
     */
    Stream<ProviderFactory> getProviderFactoriesStream(Class<? extends Provider> clazz);
    
    /** @return 服务器启动时间戳（毫秒） */
    long getServerStartupTimestamp();

    /** 关闭工厂并释放资源。 */
    void close();
}
