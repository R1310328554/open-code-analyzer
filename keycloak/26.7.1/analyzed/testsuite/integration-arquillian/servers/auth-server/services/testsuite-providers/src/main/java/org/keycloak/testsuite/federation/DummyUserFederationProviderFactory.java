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

package org.keycloak.testsuite.federation;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.user.ImportSynchronization;
import org.keycloak.storage.user.SynchronizationResult;

import org.jboss.logging.Logger;

/**
 * {@link DummyUserFederationProvider} 的工厂，支持用户导入同步并统计同步调用次数。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DummyUserFederationProviderFactory implements UserStorageProviderFactory<DummyUserFederationProvider>, ImportSynchronization {

    private static final Logger logger = Logger.getLogger(DummyUserFederationProviderFactory.class);
    /** 提供者名称标识。 */
    public static final String PROVIDER_NAME = "dummy";

    /** 全量同步调用计数器。 */
    private AtomicInteger fullSyncCounter = new AtomicInteger();
    /** 增量同步调用计数器。 */
    private AtomicInteger changedSyncCounter = new AtomicInteger();

    /** 跨会话共享的内存用户映射。 */
    private Map<String, UserModel> users = new HashMap<String, UserModel>();

    /** {@inheritDoc} 创建虚拟联邦提供者实例。 */
    @Override
    public DummyUserFederationProvider create(KeycloakSession session, ComponentModel model) {
        return new DummyUserFederationProvider(session, model, users);
    }

    /** {@inheritDoc} 返回可配置属性列表。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property().name("important.config")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add().build();
    }

    /** {@inheritDoc} 初始化工厂配置。 */
    @Override
    public void init(Config.Scope config) {

    }

    /** {@inheritDoc} 会话工厂就绪后的回调。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} 关闭工厂。 */
    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回工厂标识。 */
    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    /** {@inheritDoc} 执行全量用户同步并递增计数器。 */
    @Override
    public SynchronizationResult sync(KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        logger.info("syncAllUsers invoked");
        fullSyncCounter.incrementAndGet();
        return SynchronizationResult.empty();
    }

    /** {@inheritDoc} 执行增量用户同步并递增计数器。 */
    @Override
    public SynchronizationResult syncSince(Date lastSync, KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        logger.info("syncChangedUsers invoked");
        changedSyncCounter.incrementAndGet();
        return SynchronizationResult.empty();
    }

    /** 返回全量同步被调用的次数。 */
    public int getFullSyncCounter() {
        return fullSyncCounter.get();
    }

    /** 返回增量同步被调用的次数。 */
    public int getChangedSyncCounter() {
        return changedSyncCounter.get();
    }

}
