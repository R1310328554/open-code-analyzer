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

package org.keycloak.storage.clientscope;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.Config;
import org.keycloak.component.ComponentFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 客户端作用域存储 Provider 工厂接口：按 realm 组件模型创建 {@link ClientScopeStorageProvider} 实例。
 * <p>
 * 继承 {@link ComponentFactory}，在管理控制台中作为可配置组件注册，并暴露 SPI 通用缓存/优先级属性。
 */
public interface ClientScopeStorageProviderFactory<T extends ClientScopeStorageProvider> extends ComponentFactory<T, ClientScopeStorageProvider> {


    /**
     * 每个 Keycloak 事务调用一次，创建 Provider 实例。
     *
     * @param session 当前 {@link KeycloakSession}
     * @param model 组件配置模型
     * @return 新建的 Provider 实例
     */
    @Override
    T create(KeycloakSession session, ComponentModel model);

    /**
     * Provider 唯一标识，亦作为管理控制台中的选项名称。
     *
     * @return Provider ID
     */
    @Override
    String getId();

    @Override
    default void init(Config.Scope config) {
    }

    @Override
    default void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    default void close() {
    }

    @Override
    default String getHelpText() {
        return "";
    }

    @Override
    default List<ProviderConfigProperty> getConfigProperties() {
        return Collections.EMPTY_LIST;
    }

    @Override
    default void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
    }

    /**
     * 创建 {@link ClientScopeStorageProviderFactory} 组件时回调，可用于初始化额外配置。
     *
     * @param session 当前会话
     * @param realm 所属 realm
     * @param model 组件模型
     */
    @Override
    default void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
    }

    /**
     * 所有 {@link ClientScopeStorageProvider} 实现共享的通用配置属性（启用、优先级、缓存策略等）。
     *
     * @return 通用 {@link ProviderConfigProperty} 列表
     */
    @Override
    default List<ProviderConfigProperty> getCommonProviderConfigProperties() {
        return ClientScopeStorageProviderSpi.commonConfig();
    }

    @Override
    default
    Map<String, Object> getTypeMetadata() {
        return new HashMap<>();
    }
}
