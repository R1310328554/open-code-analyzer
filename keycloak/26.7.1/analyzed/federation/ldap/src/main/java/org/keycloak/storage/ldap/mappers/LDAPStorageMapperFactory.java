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
package org.keycloak.storage.ldap.mappers;

import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.component.SubComponentFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderModel;

/**
 * LDAP 存储映射器工厂接口：作为 {@link SubComponentFactory} 子组件，负责创建各类 LDAP 映射器实例。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface LDAPStorageMapperFactory<T extends LDAPStorageMapper> extends SubComponentFactory<T, LDAPStorageMapper> {
    /**
     * 每个 Keycloak 事务调用一次，创建映射器实例。
     *
     * @param session
     * @param model
     * @return
     */
    T create(KeycloakSession session, ComponentModel model);

    /**
     * 提供者名称，会在管理控制台中作为选项展示。
     *
     * @return
     */
    @Override
    String getId();

    /** {@inheritDoc} 默认无初始化逻辑。 */
    @Override
    default void init(Config.Scope config) {

    }

    /** {@inheritDoc} 默认无后置初始化。 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} 默认无资源需释放。 */
    @Override
    default void close() {

    }

    /** {@inheritDoc} 默认无帮助文本。 */
    @Override
    default String getHelpText() {
        return "";
    }

    /** {@inheritDoc} 默认无配置项。 */
    @Override
    default List<ProviderConfigProperty> getConfigProperties() {
        return Collections.EMPTY_LIST;
    }

    /** {@inheritDoc} 默认跳过配置校验。 */
    @Override
    default void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {

    }

    /** 父级 LDAP 用户存储提供者更新时的回调，子类可覆盖以同步配置。 */
    default void onParentUpdate(RealmModel realm, UserStorageProviderModel oldParent, UserStorageProviderModel newParent, ComponentModel mapperModel) {

    }

    /**
     * 创建 {@link UserStorageProviderModel} 时调用，可用于初始化额外配置。
     * 例如通过内省数据库或 LDAP 模式自动创建属性映射。
     *
     * @param session
     * @param realm
     * @param model
     */
    @Override
    default void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {

    }
}
