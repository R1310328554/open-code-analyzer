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
 * {@link RoleStorageProvider} 的组件工厂接口。
 * <p>基于 {@link ComponentModel} 创建角色存储提供者实例。</p>
 */
public interface RoleStorageProviderFactory<T extends RoleStorageProvider> extends ComponentFactory<T, RoleStorageProvider> {


    /**
     * 每个 Keycloak 事务调用一次，创建提供者实例。
     *
     * @param session
     * @param model
     * @return
     */
    @Override
    T create(KeycloakSession session, ComponentModel model);

    /**
     * 返回提供者标识名称。
     *
     * @return
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
     * 创建 RoleStorageProviderModel 时调用，用于初始化额外配置。
     *
     * @param session
     * @param realm
     * @param model
     */
    @Override
    default void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
    }

    /**
     * 所有 {@link RoleStorageProvider} 实现共用的配置属性。
     *
     * @return
     */
    @Override
    default
    List<ProviderConfigProperty> getCommonProviderConfigProperties() {
        return RoleStorageProviderSpi.commonConfig();
    }

    @Override
    default
    Map<String, Object> getTypeMetadata() {
        return new HashMap<>();
    }
}
