/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.remote.transaction;

import java.util.stream.Stream;

import org.keycloak.models.sessions.infinispan.changes.remote.remover.query.ClientSessionQueryConditionalRemover;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.BaseUpdater;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.Updater;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.UpdaterFactory;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.client.AuthenticatedClientSessionUpdater;
import org.keycloak.models.sessions.infinispan.entities.ClientSessionKey;
import org.keycloak.models.sessions.infinispan.entities.RemoteAuthenticatedClientSessionEntity;

/**
 * 已认证客户端会话变更日志事务的语法糖封装。
 * <p>
 * 等价于
 * {@code RemoteChangeLogTransaction<SessionKey, AuthenticatedClientSessionEntity, AuthenticatedClientSessionUpdater,
 * UserAndClientSessionConditionalRemover<AuthenticatedClientSessionEntity>>}
 */
public class ClientSessionChangeLogTransaction extends RemoteChangeLogTransaction<ClientSessionKey, RemoteAuthenticatedClientSessionEntity, AuthenticatedClientSessionUpdater, ClientSessionQueryConditionalRemover> {

    public ClientSessionChangeLogTransaction(UpdaterFactory<ClientSessionKey, RemoteAuthenticatedClientSessionEntity, AuthenticatedClientSessionUpdater> factory, SharedState<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> sharedState) {
        super(factory, sharedState, new ClientSessionQueryConditionalRemover());
    }

    /**
     * 包装 Query 投影结果（首参为实体，次参为版本；此处无版本信息）。
     */
    public void wrapFromProjection(RemoteAuthenticatedClientSessionEntity entity) {
        wrap(entity.createCacheKey(), entity, Updater.NO_VERSION);
    }

    /**
     * 删除指定用户会话下的全部客户端会话。
     */
    public void removeByUserSessionId(String userSessionId) {
        getConditionalRemover().removeByUserSessionId(userSessionId);
        // 同时将事务内已缓存的匹配实体标记为已删除
        getClientSessions()
                .filter(getConditionalRemover()::willRemove)
                .forEach(BaseUpdater::markDeleted);
    }

    /**
     * @return 当前事务中已缓存的全部 {@link AuthenticatedClientSessionUpdater} 流。
     */
    public Stream<AuthenticatedClientSessionUpdater> getClientSessions() {
        return getCachedEntities().values().stream();
    }
}
