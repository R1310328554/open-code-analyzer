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
import org.keycloak.storage.user.ImportSynchronization;

/**
 * 用户存储 Provider 工厂接口：创建 Provider 实例并暴露通用配置项。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserStorageProviderFactory<T extends UserStorageProvider> extends ComponentFactory<T, UserStorageProvider> {


    /**
     * 每个 Keycloak 事务调用一次，创建 Provider 实例。
     *
     * @param session 当前会话
     * @param model 组件配置模型
     * @return Provider 实例
     */
    T create(KeycloakSession session, ComponentModel model);

    /**
     * Provider 在管理控制台中的显示名称（亦作工厂 ID）。
     *
     * @return 工厂标识
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
        return Collections.emptyList();
    }

    @Override
    default void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {

    }

    /**
     * {@link UserStorageProviderModel} 创建时调用；可用于自动探测数据库/LDAP 模式并生成映射配置。
     *
     * @param session 当前会话
     * @param realm 所属 realm
     * @param model 新创建的组件模型
     */
    @Override
    default void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {

    }

    /**
     * 所有 UserStorageProvider 实现共用的配置属性列表。
     *
     * @return 通用配置项
     */
    @Override
    default
    List<ProviderConfigProperty> getCommonProviderConfigProperties() {
        return UserStorageProviderSpi.commonConfig();
    }

    @Override
    default
    Map<String, Object> getTypeMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        if (this instanceof ImportSynchronization) {
            // 标记该工厂支持用户导入同步
            metadata.put("synchronizable", true);
        }
        return metadata;
    }
}
