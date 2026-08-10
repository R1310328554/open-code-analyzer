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

package org.keycloak.models.sessions.infinispan.changes;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.session.UserSessionPersisterProvider;
import org.keycloak.models.sessions.infinispan.UserSessionAdapter;
import org.keycloak.models.sessions.infinispan.entities.AuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.EmbeddedClientSessionKey;
import org.keycloak.models.sessions.infinispan.util.SessionTimeouts;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CLIENT_SESSION_CACHE_NAME;

/**
 * 持久化客户端会话的变更日志事务。
 * <p>
 * 缓存未命中时从 {@link UserSessionPersisterProvider} 加载并导入；
 * 与用户会话事务协作维护 clientSessions 映射，并处理认证会话悲观锁场景。
 */
public class ClientSessionPersistentChangelogBasedTransaction extends PersistentSessionsChangelogBasedTransaction<EmbeddedClientSessionKey, AuthenticatedClientSessionEntity> {

    private static final Logger LOG = Logger.getLogger(ClientSessionPersistentChangelogBasedTransaction.class);
    /** 关联的用户会话持久化事务。 */
    private final UserSessionPersistentChangelogBasedTransaction userSessionTx;
    /** 认证会话是否使用悲观锁（影响 ADD_IF_ABSENT 的 DB 锁策略）。 */
    private final boolean pessimisticLockingAuthenticationSession;

    public ClientSessionPersistentChangelogBasedTransaction(KeycloakSession session,
                                                            CacheHolder<EmbeddedClientSessionKey, AuthenticatedClientSessionEntity> cacheHolder,
                                                            CacheHolder<EmbeddedClientSessionKey, AuthenticatedClientSessionEntity> offlineCacheHolder,
                                                            UserSessionPersistentChangelogBasedTransaction userSessionTx,
                                                            boolean pessimisticLockingAuthenticationSession) {
        super(session, CLIENT_SESSION_CACHE_NAME, cacheHolder, offlineCacheHolder);
        this.userSessionTx = userSessionTx;
        this.pessimisticLockingAuthenticationSession = pessimisticLockingAuthenticationSession;
    }

    /** 批量更新客户端会话实体上的 userSessionId。 */
    public void setUserSessionId(Collection<EmbeddedClientSessionKey> keys, String userSessionId, boolean offline) {
        keys.stream().map(getUpdates(offline)::get)
                .filter(Objects::nonNull)
                .map(SessionUpdatesList::getEntityWrapper)
                .map(SessionEntityWrapper::getEntity)
                .filter(Objects::nonNull)
                .forEach(authenticatedClientSessionEntity -> authenticatedClientSessionEntity.setUserSessionId(userSessionId));
    }

    /**
     * 按 realm/client/userSession 获取客户端会话；优先事务内副本，其次缓存，最后持久层。
     */
    public SessionEntityWrapper<AuthenticatedClientSessionEntity> get(RealmModel realm, ClientModel client, UserSessionModel userSession, EmbeddedClientSessionKey key, boolean offline) {
        if (key == null) {
            key = new EmbeddedClientSessionKey(userSession.getId(), client.getId());
        }
        SessionUpdatesList<AuthenticatedClientSessionEntity> myUpdates = getUpdates(offline).get(key);
        if (myUpdates == null) {
            SessionEntityWrapper<AuthenticatedClientSessionEntity> wrappedEntity = null;
            Cache<EmbeddedClientSessionKey, SessionEntityWrapper<AuthenticatedClientSessionEntity>> cache = getCache(offline);
            if (cache != null) {
                wrappedEntity = cache.get(key);
            }

            if (wrappedEntity == null) {
                LOG.tracef("Client-session not found in cache, loading from persister. userSessionId=%s, clientSessionId=%s, clientId=%s, offline=%s",
                        userSession.getId(), key, client.getId(), offline);
                wrappedEntity = getSessionEntityFromPersister(realm, client, userSession, key, offline);
            } else {
                LOG.tracef("Client-session found in cache. userSessionId=%s, clientSessionId=%s, clientId=%s, offline=%s",
                        userSession.getId(), key, client.getId(), offline);
            }

            if (wrappedEntity == null) {
                LOG.debugf("Client-session not found in persister. userSessionId=%s, clientSessionId=%s, clientId=%s, offline=%s",
                        userSession.getId(), key, client.getId(), offline);
                return null;
            }

            // 缓存不含 offline 标志，导入时补全
            wrappedEntity.getEntity().setOffline(offline);
            wrappedEntity.getEntity().setUserSessionId(userSession.getId());

            RealmModel realmFromSession = kcSession.realms().getRealm(wrappedEntity.getEntity().getRealmId());
            if (!realmFromSession.getId().equals(realm.getId())) {
                LOG.warnf("Realm mismatch for session %s. Expected realm %s, but found realm %s", wrappedEntity.getEntity(), realm.getId(), realmFromSession.getId());
                return null;
            }

            myUpdates = new SessionUpdatesList<>(realm, wrappedEntity);
            getUpdates(offline).put(key, myUpdates);

            return wrappedEntity;
        } else {

            // 已调度删除的实体不返回
            boolean scheduledForRemove = myUpdates.getUpdateTasks().stream()
                    .map(SessionUpdateTask::getOperation)
                    .anyMatch(SessionUpdateTask.CacheOperation.REMOVE::equals);

            if (scheduledForRemove) {
                LOG.debugf("Client-session scheduled for removal in transaction. userSessionId=%s, clientSessionId=%s, clientId=%s, offline=%s",
                        userSession.getId(), key, client.getId(), offline);
            }

            return scheduledForRemove ? null : myUpdates.getEntityWrapper();
        }
    }

    @Override
    protected boolean lockDatabaseEntity(RealmModel realm, EmbeddedClientSessionKey clientSessionKey, boolean offline, SessionUpdateTask.CacheOperation operation) {
        if (operation == SessionUpdateTask.CacheOperation.ADD_IF_ABSENT) {
            // 同键并发插入可能冲突；认证会话已悲观锁定时可安全插入
            // 参见 UserSessionConcurrencyTest#testConcurrentNotesChange
            return pessimisticLockingAuthenticationSession;
        } else {
            return kcSession.getProvider(UserSessionPersisterProvider.class).lockClientSession(realm, clientSessionKey.userSessionId(), clientSessionKey.clientId(), offline, operation == SessionUpdateTask.CacheOperation.REMOVE);
        }
    }

    /** 从持久层加载客户端会话实体。 */
    private SessionEntityWrapper<AuthenticatedClientSessionEntity> getSessionEntityFromPersister(RealmModel realm, ClientModel client, UserSessionModel userSession, EmbeddedClientSessionKey clientSessionId, boolean offline) {
        UserSessionPersisterProvider persister = kcSession.getProvider(UserSessionPersisterProvider.class);
        AuthenticatedClientSessionModel clientSession = persister.loadClientSession(realm, client, userSession, offline);

        if (clientSession == null) {
            LOG.debugf("Client-session not loaded from persister. userSessionId=%s, clientSessionId=%s, clientId=%s, offline=%s",
                    userSession.getId(), clientSessionId, client.getId(), offline);
            return null;
        }

        SessionEntityWrapper<AuthenticatedClientSessionEntity> authenticatedClientSessionEntitySessionEntityWrapper = importClientSession(realm, client, userSession, clientSession, clientSessionId);
        if (authenticatedClientSessionEntitySessionEntityWrapper == null) {
            LOG.debugf("Client-session not imported from persister. userSessionId=%s, clientSessionId=%s, clientId=%s, offline=%s",
                    userSession.getId(), clientSessionId, client.getId(), offline);
        }

        return authenticatedClientSessionEntitySessionEntityWrapper;
    }

    /** 根据持久化模型创建 {@link AuthenticatedClientSessionEntity} 实例。 */
    public static AuthenticatedClientSessionEntity createAuthenticatedClientSessionInstance(String userSessionId, String userId, AuthenticatedClientSessionModel clientSession,
                                                                                      String realmId, String clientId, boolean offline) {

        AuthenticatedClientSessionEntity entity = new AuthenticatedClientSessionEntity();
        entity.setRealmId(realmId);

        entity.setAction(clientSession.getAction());
        entity.setAuthMethod(clientSession.getProtocol());

        entity.setNotes(clientSession.getNotes() == null ? new ConcurrentHashMap<>() : clientSession.getNotes());
        entity.setClientId(clientId);
        entity.setRedirectUri(clientSession.getRedirectUri());
        entity.setTimestamp(clientSession.getTimestamp());
        entity.setOffline(offline);
        entity.setUserSessionId(userSessionId);
        entity.setUserId(userId);

        return entity;
    }

    private SessionEntityWrapper<AuthenticatedClientSessionEntity> importClientSession(RealmModel realm, ClientModel client, UserSessionModel userSession, AuthenticatedClientSessionModel persistentClientSession, EmbeddedClientSessionKey clientSessionId) {
        AuthenticatedClientSessionEntity entity = createAuthenticatedClientSessionInstance(userSession.getId(), userSession.getUser().getId(), persistentClientSession,
                realm.getId(), client.getId(), userSession.isOffline());
        boolean offline = userSession.isOffline();

        entity.setUserSessionId(userSession.getId());

        if (offline) {
            // 离线会话 timestamp 与用户会话 lastSessionRefresh 对齐（历史优化，持久化迁移后可移除）
            entity.setTimestamp(userSession.getLastSessionRefresh());
        }

        long lifespan = getLifespanMsLoader(offline).apply(realm, client, entity);
        long maxIdle = getMaxIdleMsLoader(offline).apply(realm, client, entity);

        if (lifespan == SessionTimeouts.ENTRY_EXPIRED_FLAG || maxIdle == SessionTimeouts.ENTRY_EXPIRED_FLAG) {
            LOG.debugf("Client-session has expired, not importing it. userSessionId=%s, clientSessionId=%s, clientId=%s, offline=%s",
                    userSession.getId(), clientSessionId, client.getId(), offline);
            return null;
        }
        
        SessionEntityWrapper<AuthenticatedClientSessionEntity> wrapper = new SessionEntityWrapper<>(entity);

        SessionEntityWrapper<AuthenticatedClientSessionEntity> imported = importSession(realm, clientSessionId, wrapper, offline, lifespan, maxIdle);

        if (imported != null) {
            LOG.debugf("Client-session already imported by another transaction. userSessionId=%s, clientSessionId=%s, clientId=%s, offline=%s",
                    userSession.getId(), clientSessionId, client.getId(), offline);
            imported.getEntity().setUserSessionId(userSession.getId());
            return imported;
        }

        // TODO: 导入客户端会话时用户会话映射是否仍需下列逻辑？
        if (! (userSession instanceof UserSessionAdapter<?> sessionToImportInto)) {
            throw new IllegalStateException("UserSessionModel must be instance of UserSessionAdapter");
        }

        if (sessionToImportInto.getEntity().getClientSessions().add(client.getId())) {
            userSessionTx.registerClientSession(sessionToImportInto.getId(), client.getId(), offline);
        }

        return wrapper;
    }

}
