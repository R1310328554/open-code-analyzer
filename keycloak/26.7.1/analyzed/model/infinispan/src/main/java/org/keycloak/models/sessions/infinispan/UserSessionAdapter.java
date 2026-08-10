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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.keycloak.common.util.MultiSiteUtils;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.UserSessionProvider;
import org.keycloak.models.sessions.infinispan.changes.SessionsChangelogBasedTransaction;
import org.keycloak.models.sessions.infinispan.changes.Tasks;
import org.keycloak.models.sessions.infinispan.changes.UserSessionUpdateTask;
import org.keycloak.models.sessions.infinispan.entities.AuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.EmbeddedClientSessionKey;
import org.keycloak.models.sessions.infinispan.entities.UserSessionEntity;

import org.jboss.logging.Logger;

/**
 * {@link UserSessionModel} 的 Infinispan 适配器。
 * <p>
 * 持有 {@link UserSessionEntity} 本地副本，读写操作通过 {@link UserSessionUpdateTask}
 * 与用户/客户端会话变更事务合并；支持在线与离线会话及多站点 lastSessionRefresh 同步。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class UserSessionAdapter<T extends SessionRefreshStore & UserSessionProvider> implements UserSessionModel {

    private static final Logger logger = Logger.getLogger(UserSessionAdapter.class);

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;

    /** 用户会话 Provider（同时提供刷新存储）。 */
    private final T provider;

    /** 用户会话变更事务。 */
    private final SessionsChangelogBasedTransaction<String, UserSessionEntity> userSessionUpdateTx;

    /** 嵌入式客户端会话变更事务。 */
    private final SessionsChangelogBasedTransaction<EmbeddedClientSessionKey, AuthenticatedClientSessionEntity> clientSessionUpdateTx;

    /** 所属 realm。 */
    private final RealmModel realm;

    /** 会话关联用户。 */
    private final UserModel user;

    /** 事务内跟踪的用户会话实体。 */
    private final UserSessionEntity entity;

    /** 是否为离线会话。 */
    private final boolean offline;

    /** 持久化状态（如 TRANSIENT 会话不写缓存）。 */
    private SessionPersistenceState persistenceState;

    public UserSessionAdapter(KeycloakSession session, UserModel user, T provider,
                              SessionsChangelogBasedTransaction<String, UserSessionEntity> userSessionUpdateTx,
                              SessionsChangelogBasedTransaction<EmbeddedClientSessionKey, AuthenticatedClientSessionEntity> clientSessionUpdateTx,
                              RealmModel realm, UserSessionEntity entity, boolean offline) {
        this.session = session;
        this.user = user;
        this.provider = provider;
        this.userSessionUpdateTx = userSessionUpdateTx;
        this.clientSessionUpdateTx = clientSessionUpdateTx;
        this.realm = realm;
        this.entity = entity;
        this.offline = offline;
    }

    @Override
    public Map<String, AuthenticatedClientSessionModel> getAuthenticatedClientSessions() {
        var clientSessionEntities = entity.getClientSessions();
        Map<String, AuthenticatedClientSessionModel> result = new HashMap<>();

        List<String> removedClientUUIDS = new LinkedList<>();

        clientSessionEntities.forEach(clientUUID -> {
            // 检查客户端是否仍存在
            ClientModel client = realm.getClientById(clientUUID);
            if (client == null) {
                // 客户端已删除，稍后从映射中移除
                removedClientUUIDS.add(clientUUID);
                return;
            }
            var clientSession = provider.getClientSession(this, client, offline);
            if (clientSession == null) {
                // 客户端会话可能已过期或并发登录尚未写入，此时不宜移除映射
                // 否则 ConcurrentLoginTest.concurrentLoginSingleUser 会失败
                return;
            }
            result.put(clientUUID, clientSession);
        });

        removeAuthenticatedClientSessions(removedClientUUIDS);

        return Collections.unmodifiableMap(result);
    }

    @Override
    public AuthenticatedClientSessionModel getAuthenticatedClientSessionByClient(String clientUUID) {
        ClientModel client = realm.getClientById(clientUUID);

        if (client != null) {
            // 可能因过期或并发登录尚未完成而返回 null，不宜主动清理映射
            return provider.getClientSession(this, client, offline);
        }

        logger.debugf("Client not found. Removing from mappings. userSessionId=%s, clientId=%s, clientSessionId=%s, offline=%s",
                getId(), clientUUID, new EmbeddedClientSessionKey(getId(), clientUUID), offline);
        removeAuthenticatedClientSessions(Collections.singleton(clientUUID));
        return null;
    }

    @Override
    public void removeAuthenticatedClientSessions(Collection<String> removedClientUUIDS) {
        if (removedClientUUIDS == null || removedClientUUIDS.isEmpty()) {
            return;
        }
        logger.debugf("Removing client sessions. clients=%s, offline=%s", removedClientUUIDS, offline);

        // 勿在迭代 removedClientUUIDS 时直接删客户端会话，addTask 可能修改被迭代的集合导致 NPE
        List<String> clientSessionUuids = removedClientUUIDS.stream()
                .filter(entity.getClientSessions()::contains)
                .toList();

        // 更新用户会话上的客户端 UUID 集合
        UserSessionUpdateTask task = new UserSessionUpdateTask() {
            @Override
            public void runUpdate(UserSessionEntity entity) {
                removedClientUUIDS.forEach(entity.getClientSessions()::remove);
            }

            @Override
            public boolean isOffline() {
                return offline;
            }
        };
        update(task);

        clientSessionUuids.forEach(clientUUID -> this.clientSessionUpdateTx.addTask(new EmbeddedClientSessionKey(entity.getId(), clientUUID), Tasks.removeSync(offline)));
    }

    @Override
    public String getId() {
        return entity.getId();
    }

    @Override
    public RealmModel getRealm() {
        return realm;
    }

    @Override
    public String getBrokerSessionId() {
        return entity.getBrokerSessionId();
    }

    @Override
    public String getBrokerUserId() {
        return entity.getBrokerUserId();
    }

    @Override
    public UserModel getUser() {
        return this.user;
    }

    @Override
    public String getLoginUsername() {
        if (entity.getLoginUsername() == null) {
            // 导入离线令牌时 UserModel 可能不可用，回退到用户对象（KEYCLOAK-5350）
            return getUser().getUsername();
        } else {
            return entity.getLoginUsername();
        }
    }

    @Override
    public String getIpAddress() {
        return entity.getIpAddress();
    }

    @Override
    public String getAuthMethod() {
        return entity.getAuthMethod();
    }

    @Override
    public boolean isRememberMe() {
        return entity.isRememberMe();
    }

    @Override
    public int getStarted() {
        return entity.getStarted();
    }

    @Override
    public int getLastSessionRefresh() {
        return entity.getLastSessionRefresh();
    }

    @Override
    public void setLastSessionRefresh(int lastSessionRefresh) {
        if (lastSessionRefresh <= entity.getLastSessionRefresh()) {
            return;
        }

        if (!MultiSiteUtils.isPersistentSessionsEnabled() && offline) {
            // 其他数据中心已更新 DB，本地集群仅刷新 lastSessionRefresh 缓存
            provider.getPersisterLastSessionRefreshStore().putLastSessionRefresh(session, entity.getId(), realm.getId(), lastSessionRefresh);
        }

        UserSessionUpdateTask task = new UserSessionUpdateTask() {

            @Override
            public void runUpdate(UserSessionEntity entity) {
                if (entity.getLastSessionRefresh() >= lastSessionRefresh) {
                    return;
                }
                entity.setLastSessionRefresh(lastSessionRefresh);
            }

            @Override
            public boolean isOffline() {
                return offline;
            }

            @Override
            public String toString() {
                return "setLastSessionRefresh(" + lastSessionRefresh + ')';
            }
        };

        update(task);
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public String getNote(String name) {
        return entity.getNotes() != null ? entity.getNotes().get(name) : null;
    }

    @Override
    public void setNote(String name, String value) {
        UserSessionUpdateTask task = new UserSessionUpdateTask() {

            @Override
            public void runUpdate(UserSessionEntity entity) {
                if (value == null) {
                    if (entity.getNotes().containsKey(name)) {
                        removeNote(name);
                    }
                    return;
                }
                entity.getNotes().put(name, value);
            }

            @Override
            public boolean isOffline() {
                return offline;
            }
        };

        update(task);
    }

    @Override
    public void removeNote(String name) {
        UserSessionUpdateTask task = new UserSessionUpdateTask() {

            @Override
            public void runUpdate(UserSessionEntity entity) {
                entity.getNotes().remove(name);
            }

            @Override
            public boolean isOffline() {
                return offline;
            }
        };

        update(task);
    }

    @Override
    public Map<String, String> getNotes() {
        return entity.getNotes();
    }

    @Override
    public State getState() {
        return entity.getState();
    }

    @Override
    public void setState(State state) {
        UserSessionUpdateTask task = new UserSessionUpdateTask() {

            @Override
            public boolean isOffline() {
                return offline;
            }

            @Override
            public void runUpdate(UserSessionEntity entity) {
                entity.setState(state);
            }

        };

        update(task);
    }

    @Override
    public SessionPersistenceState getPersistenceState() {
        return persistenceState;
    }

    public void setPersistenceState(SessionPersistenceState persistenceState) {
        this.persistenceState = persistenceState;
    }

    @Override
    public void restartSession(RealmModel realm, UserModel user, String loginUsername, String ipAddress, String authMethod, boolean rememberMe, String brokerSessionId, String brokerUserId) {
        // 逐客户端发 DELETE 开销大；用户会话更新任务会清空 clientSessions 集合
        entity.getClientSessions().forEach(clientUUID -> this.clientSessionUpdateTx.addTask(new EmbeddedClientSessionKey(entity.getId(), clientUUID), Tasks.removeSync(offline)));
        UserSessionUpdateTask task = new UserSessionUpdateTask() {

            @Override
            public boolean isOffline() {
                return offline;
            }

            @Override
            public void runUpdate(UserSessionEntity entity) {
                UserSessionEntity.updateSessionEntity(entity, realm, user, loginUsername, ipAddress, authMethod, rememberMe, brokerSessionId, brokerUserId);

                entity.setState(null);
                entity.getNotes().clear();
                entity.getClientSessions().clear();
            }

        };

        userSessionUpdateTx.restartEntity(getId(), task);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserSessionModel that)) {
            return false;
        }

        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    // TODO: This should not be public
    /** 返回底层用户会话实体（供同包事务类使用）。 */
    public UserSessionEntity getEntity() {
        return entity;
    }

    /** 将用户会话更新任务加入变更事务。 */
    void update(UserSessionUpdateTask task) {
        userSessionUpdateTx.addTask(getId(), task);
    }

}
