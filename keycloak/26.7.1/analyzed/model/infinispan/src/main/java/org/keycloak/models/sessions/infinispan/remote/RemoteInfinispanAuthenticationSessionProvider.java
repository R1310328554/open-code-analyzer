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

package org.keycloak.models.sessions.infinispan.remote;

import java.util.Map;
import java.util.Objects;

import org.keycloak.cluster.ClusterProvider;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.cache.infinispan.events.AuthenticationSessionAuthNoteUpdateEvent;
import org.keycloak.models.sessions.infinispan.InfinispanAuthenticationSessionProviderFactory;
import org.keycloak.models.sessions.infinispan.entities.RootAuthenticationSessionEntity;
import org.keycloak.models.sessions.infinispan.remote.transaction.AuthenticationSessionChangeLogTransaction;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.sessions.RootAuthenticationSessionModel;

/**
 * 基于远程 Infinispan 的 {@link AuthenticationSessionProvider} 实现。
 * <p>
 * 根认证会话的读写通过 {@link AuthenticationSessionChangeLogTransaction} 延迟提交至远程缓存；
 * 跨节点 auth note 更新则经 {@link ClusterProvider} 广播事件。
 */
public class RemoteInfinispanAuthenticationSessionProvider implements AuthenticationSessionProvider {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 认证会话变更日志事务，负责缓存 CRUD 与提交。 */
    private final AuthenticationSessionChangeLogTransaction transaction;
    /** 每个根认证会话允许的最大子认证会话数。 */
    private final int authSessionsLimit;

    public RemoteInfinispanAuthenticationSessionProvider(KeycloakSession session, int authSessionsLimit, AuthenticationSessionChangeLogTransaction transaction) {
        this.session = Objects.requireNonNull(session);
        this.authSessionsLimit = authSessionsLimit;
        this.transaction = Objects.requireNonNull(transaction);
    }

    @Override
    public void close() {

    }

    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm) {
        return createRootAuthenticationSession(realm, SecretGenerator.SECURE_ID_GENERATOR.get());
    }

    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm, String id) {
        RootAuthenticationSessionEntity entity = new RootAuthenticationSessionEntity(id);
        entity.setRealmId(realm.getId());
        entity.setTimestamp(Time.currentTime());
        var updater = transaction.create(id, entity);
        updater.initialize(session, realm, authSessionsLimit);
        return updater;
    }

    @Override
    public RootAuthenticationSessionModel getRootAuthenticationSession(RealmModel realm, String authenticationSessionId) {
        var updater = transaction.get(authenticationSessionId);
        if (updater == null || !Objects.equals(realm.getId(), updater.getValue().getRealmId())) {
            return null;
        }
        updater.initialize(session, realm, authSessionsLimit);
        return updater;
    }

    @Override
    public void removeRootAuthenticationSession(RealmModel realm, RootAuthenticationSessionModel authenticationSession) {
        if (!Objects.equals(realm.getId(), authenticationSession.getRealm().getId())) {
            throw new ModelException("Authentication session with id '" + authenticationSession.getId() + "' does not belong to realm '" + realm.getId() + "'");
        }
        transaction.remove(authenticationSession.getId());
    }

    @Override
    public void onRealmRemoved(RealmModel realm) {
        transaction.removeByRealmId(realm.getId());
    }

    @Override
    public void updateNonlocalSessionAuthNotes(AuthenticationSessionCompoundId compoundId, Map<String, String> authNotesFragment) {
        if (compoundId == null) {
            return;
        }

        // 向除本数据中心外的所有节点广播 auth note 片段更新
        session.getProvider(ClusterProvider.class).notify(
                InfinispanAuthenticationSessionProviderFactory.AUTHENTICATION_SESSION_EVENTS,
                AuthenticationSessionAuthNoteUpdateEvent.create(compoundId.getRootSessionId(), compoundId.getTabId(), authNotesFragment),
                true,
                ClusterProvider.DCNotify.ALL_BUT_LOCAL_DC
        );
    }
}
