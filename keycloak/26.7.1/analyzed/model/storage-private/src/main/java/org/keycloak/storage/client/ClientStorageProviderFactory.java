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
 * 客户端存储 Provider 工厂接口：每个 Keycloak 事务内创建 {@link ClientStorageProvider} 实例。
 * <p>
 * 同时作为 {@link ComponentFactory} 管理组件生命周期与配置校验。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ClientStorageProviderFactory<T extends ClientStorageProvider> extends ComponentFactory<T, ClientStorageProvider> {


    /**
     * 每个 Keycloak 事务调用一次，创建 Provider 实例。
     *
     * @param session 当前会话
     * @param model 组件配置模型
     * @return Provider 实例
     */
    T create(KeycloakSession session, ComponentModel model);

    /**
     * Provider 名称，在管理控制台中作为选项展示。
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
     * 创建 {@link ClientStorageProviderModel} 时调用，可用于初始化额外配置。
     *
     * @param session 当前会话
     * @param realm 目标领域
     * @param model 新创建的组件模型
     */
    @Override
    default void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {

    }

    /**
     * 所有 {@link UserStorageProvider} 实现共用的配置属性（缓存策略、优先级等）。
     *
     * @return 通用配置属性列表
     */
    @Override
    default
    List<ProviderConfigProperty> getCommonProviderConfigProperties() {
        return ClientStorageProviderSpi.commonConfig();
    }

    @Override
    default
    Map<String, Object> getTypeMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        return metadata;
    }
}
