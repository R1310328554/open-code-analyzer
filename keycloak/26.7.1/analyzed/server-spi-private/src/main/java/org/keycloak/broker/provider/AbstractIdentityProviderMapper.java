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

package org.keycloak.broker.provider;

import java.util.Set;

import org.keycloak.broker.provider.mappersync.ConfigSyncEventListener;
import org.keycloak.cache.AlternativeLookupProvider;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

import org.jboss.logging.Logger;

/**
 * 身份联邦映射器抽象基类，封装 {@link IdentityProviderMapper} 生命周期与配置同步监听注册。
 * <p>每个 {@link KeycloakSessionFactory} 仅注册一次 {@link ConfigSyncEventListener}，并在联邦身份预处理/导入/更新流程中提供空默认实现。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractIdentityProviderMapper implements IdentityProviderMapper {

    private static final Logger LOG = Logger.getLogger(AbstractIdentityProviderMapper.class);

    private static volatile KeycloakSessionFactory keycloakSessionFactory;

    @Override
    public void close() {

    }

    @Override
    public IdentityProviderMapper create(KeycloakSession session) {
        return null;
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        registerConfigSyncEventListenerOnce(factory);
    }

    /** 为会话工厂单次注册配置同步事件监听器（集成测试需按工厂隔离）。 */
    private void registerConfigSyncEventListenerOnce(KeycloakSessionFactory factory) {
        /*
         * Make sure that the config sync listener is registered only once for a session factory. It would also be
         * possible to register it only once per VM, but that does not work fine in integration tests.
         */
        if (keycloakSessionFactory != factory) {
            synchronized (AbstractIdentityProviderMapper.class) {
                if (keycloakSessionFactory != factory) {
                    keycloakSessionFactory = factory;

                    LOG.debugf("Registering %s", ConfigSyncEventListener.class);
                    factory.register(new ConfigSyncEventListener());
                }
            }
        }
    }

    @Override
    public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

    }

    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

    }

    @Override
    public void updateBrokeredUserLegacy(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        updateBrokeredUser(session, realm, user, mapperModel, context);
    }

    /** 依赖 {@link AlternativeLookupProvider} 以支持缓存查找。 */
    @Override
    public Set<Class<? extends Provider>> dependsOn() {
        return Set.of(AlternativeLookupProvider.class); //for caching
    }
}
