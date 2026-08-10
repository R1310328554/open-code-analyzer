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

package org.keycloak.models.sessions.infinispan;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.keycloak.cluster.ClusterProvider;
import org.keycloak.common.util.Time;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.cache.infinispan.events.AuthenticationSessionAuthNoteUpdateEvent;
import org.keycloak.models.sessions.infinispan.changes.InfinispanChangelogBasedTransaction;
import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;
import org.keycloak.models.sessions.infinispan.changes.SessionUpdateTask;
import org.keycloak.models.sessions.infinispan.changes.Tasks;
import org.keycloak.models.sessions.infinispan.entities.RootAuthenticationSessionEntity;
import org.keycloak.models.sessions.infinispan.events.RealmRemovedSessionEvent;
import org.keycloak.models.sessions.infinispan.events.SessionEventsSenderTransaction;
import org.keycloak.models.sessions.infinispan.stream.SessionWrapperPredicate;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import org.infinispan.Cache;
import org.infinispan.commons.util.concurrent.CompletionStages;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.persistence.manager.PersistenceManager;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.AUTHENTICATION_SESSIONS_CACHE_NAME;

/**
 * 基于 Infinispan 的认证会话 {@link AuthenticationSessionProvider} 实现。
 * <p>
 * 通过变更日志事务管理根认证会话实体，并在集群间广播领域删除与认证备注更新事件。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InfinispanAuthenticationSessionProvider implements AuthenticationSessionProvider {

    /** 当前 Keycloak 会话上下文。 */
    private final KeycloakSession session;
    /** 每个根认证会话允许的最大并发认证会话数。 */
    private final int authSessionsLimit;
    /** 根认证会话的 Infinispan 变更日志事务。 */
    protected final InfinispanChangelogBasedTransaction<String, RootAuthenticationSessionEntity> sessionTx;
    /** 用于向集群其他节点发送会话相关事件的异步发送器。 */
    protected final SessionEventsSenderTransaction clusterEventsSenderTx;

    public InfinispanAuthenticationSessionProvider(KeycloakSession session,
                                                   InfinispanChangelogBasedTransaction<String, RootAuthenticationSessionEntity> sessionTx, int authSessionsLimit) {
        this.session = session;
        this.authSessionsLimit = authSessionsLimit;
        this.sessionTx = sessionTx;
        this.clusterEventsSenderTx = new SessionEventsSenderTransaction(session);
        session.getTransactionManager().enlistAfterCompletion(clusterEventsSenderTx);
    }


    /** 在指定领域中创建根认证会话（自动生成 ID）。 */
    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm) {
        return createRootAuthenticationSession(realm, sessionTx.generateKey());
    }


    /** 在指定领域中以给定 ID 创建根认证会话。 */
    @Override
    public RootAuthenticationSessionModel createRootAuthenticationSession(RealmModel realm, String id) {
        RootAuthenticationSessionEntity entity = new RootAuthenticationSessionEntity(id);
        entity.setRealmId(realm.getId());
        entity.setTimestamp(Time.currentTime());

        SessionUpdateTask<RootAuthenticationSessionEntity> createAuthSessionTask = Tasks.addIfAbsentSync();
        sessionTx.addTask(entity.getId(), createAuthSessionTask, entity, UserSessionModel.SessionPersistenceState.PERSISTENT);
        return wrap(realm, entity);
    }


    private RootAuthenticationSessionAdapter wrap(RealmModel realm, RootAuthenticationSessionEntity entity) {
        return entity == null ? null : new RootAuthenticationSessionAdapter(session, this, realm, entity, authSessionsLimit);
    }


    private RootAuthenticationSessionEntity getRootAuthenticationSessionEntity(String authSessionId) {
        SessionEntityWrapper<RootAuthenticationSessionEntity> entityWrapper = sessionTx.get(authSessionId);
        return entityWrapper==null ? null : entityWrapper.getEntity();
    }

    /** 领域删除时广播事件，由各节点清理本地认证会话缓存。 */
    @Override
    public void onRealmRemoved(RealmModel realm) {
        // 向所有数据中心发送消息，远程缓存会在各节点触发认证会话删除监听
        clusterEventsSenderTx.addEvent(
                RealmRemovedSessionEvent.createEvent(RealmRemovedSessionEvent.class, InfinispanAuthenticationSessionProviderFactory.REALM_REMOVED_AUTHSESSION_EVENT, session, realm.getId())
        );
    }

    protected void onRealmRemovedEvent(String realmId) {
        Cache<String, SessionEntityWrapper<RootAuthenticationSessionEntity>> cache = sessionTx.getCache();
        Iterator<Map.Entry<String, SessionEntityWrapper<RootAuthenticationSessionEntity>>> itr = CacheDecorators.localCache(cache)
                .entrySet()
                .stream()
                .filter(SessionWrapperPredicate.create(realmId))
                .iterator();

        while (itr.hasNext()) {
            CacheDecorators.localCache(cache)
                    .remove(itr.next().getKey());
        }
    }

    /** 将认证备注片段同步到非本地数据中心上的认证会话。 */
    @Override
    public void updateNonlocalSessionAuthNotes(AuthenticationSessionCompoundId compoundId, Map<String, String> authNotesFragment) {
        if (compoundId == null) {
            return;
        }

        ClusterProvider cluster = session.getProvider(ClusterProvider.class);
        cluster.notify(
          InfinispanAuthenticationSessionProviderFactory.AUTHENTICATION_SESSION_EVENTS,
          AuthenticationSessionAuthNoteUpdateEvent.create(compoundId.getRootSessionId(), compoundId.getTabId(), authNotesFragment),
          true,
          ClusterProvider.DCNotify.ALL_BUT_LOCAL_DC
        );
    }


    /** 按 ID 获取根认证会话，领域不匹配时返回 null。 */
    @Override
    public RootAuthenticationSessionModel getRootAuthenticationSession(RealmModel realm, String authenticationSessionId) {
        RootAuthenticationSessionEntity entity = getRootAuthenticationSessionEntity(authenticationSessionId);
        if (entity != null && !Objects.equals(realm.getId(), entity.getRealmId())) {
            return null;
        }
        return wrap(realm, entity);
    }


    /** 删除根认证会话，会话不属于当前领域时抛出 {@link ModelException}。 */
    @Override
    public void removeRootAuthenticationSession(RealmModel realm, RootAuthenticationSessionModel authenticationSession) {
        if (!Objects.equals(realm.getId(), authenticationSession.getRealm().getId())) {
            throw new ModelException("Authentication session with id '" + authenticationSession.getId() + "' does not belong to realm '" + realm.getId() + "'");
        }
        SessionUpdateTask<RootAuthenticationSessionEntity> removeTask = Tasks.removeSync();
        sessionTx.addTask(authenticationSession.getId(), removeTask);
    }

    @Override
    public void close() {

    }

    public Cache<String, SessionEntityWrapper<RootAuthenticationSessionEntity>> getCache() {
        return sessionTx.getCache();
    }

    public InfinispanChangelogBasedTransaction<String, RootAuthenticationSessionEntity> getRootAuthSessionTransaction() {
        return sessionTx;
    }

    /** 模型版本迁移：26.1.0 时清空认证会话持久化存储。 */
    @Override
    public void migrate(String modelVersion) {
        if ("26.1.0".equals(modelVersion)) {
            InfinispanConnectionProvider infinispanConnectionProvider = session.getProvider(InfinispanConnectionProvider.class);
            Cache<String, RootAuthenticationSessionEntity> authSessionsCache = infinispanConnectionProvider.getCache(AUTHENTICATION_SESSIONS_CACHE_NAME);
            CompletionStages.join(ComponentRegistry.componentOf(authSessionsCache, PersistenceManager.class).clearAllStores(PersistenceManager.AccessMode.BOTH));
        }
    }
}
