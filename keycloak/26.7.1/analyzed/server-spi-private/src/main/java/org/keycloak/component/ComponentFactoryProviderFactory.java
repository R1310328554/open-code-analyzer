/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.component;

import java.util.function.Function;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.InvalidationHandler;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;

/**
 * 组件工厂 SPI 的 {@link ProviderFactory}，负责按领域与组件解析具体 {@link ProviderFactory}。
 * <p>本工厂与会话无关，{@link #create(KeycloakSession)} 不支持按会话实例化。</p>
 *
 * @author hmlnarik
 */
public interface ComponentFactoryProviderFactory extends ProviderFactory<ComponentFactoryProvider>, InvalidationHandler {

    /**
     * 获取指定类型、领域与组件对应的提供者工厂。
     *
     * @param clazz 提供者类型
     * @param realmId 领域 ID
     * @param componentId 组件 ID
     * @param model 从会话工厂解析 {@link ComponentModel} 的函数
     * @return 已按组件配置定制的工厂实例
     */
    <T extends Provider> ProviderFactory<T> getProviderFactory(Class<T> clazz, String realmId, String componentId, Function<KeycloakSessionFactory, ComponentModel> model);

    @Override
    default ComponentFactoryProvider create(KeycloakSession session) {
        throw new UnsupportedOperationException("ComponentFactoryProvider is session-independent, hence not instantiable per session.");
    }

}
