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

package org.keycloak.events.jpa;

import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.events.EventStoreProvider;
import org.keycloak.events.EventStoreProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.InvalidationHandler;
import org.keycloak.storage.datastore.PeriodicEventInvalidation;

/**
 * 基于 JPA 的 {@link EventStoreProviderFactory} 实现，提供用户事件与管理事件的持久化存储。
 * <p>
 * 工厂 ID 为 {@code jpa}；在周期性失效通知到达时，会触发过期管理事件的清理。
 * </p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JpaEventStoreProviderFactory implements EventStoreProviderFactory, InvalidationHandler {

    /** JPA 事件存储提供者的标识符。 */
    public static final String ID = "jpa";

    /** 为当前会话创建 {@link JpaEventStoreProvider}，绑定 JPA EntityManager。 */
    @Override
    public EventStoreProvider create(KeycloakSession session) {
        JpaConnectionProvider connection = session.getProvider(JpaConnectionProvider.class);
        return new JpaEventStoreProvider(session, connection.getEntityManager());
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return ID;
    }

    /**
     * 响应缓存失效：当 JPA 事件存储收到周期性失效信号时，清理已过期的管理事件。
     */
    @Override
    public void invalidate(KeycloakSession session, InvalidableObjectType type, Object... params) {
        if(type == PeriodicEventInvalidation.JPA_EVENT_STORE) {
            ((JpaEventStoreProvider) session.getProvider(EventStoreProvider.class)).clearExpiredAdminEvents();
        }
    }
}
