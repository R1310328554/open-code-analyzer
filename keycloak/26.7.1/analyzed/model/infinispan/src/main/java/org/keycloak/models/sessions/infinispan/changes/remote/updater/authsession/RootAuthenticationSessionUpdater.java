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
package org.keycloak.models.sessions.infinispan.changes.remote.updater.authsession;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.sessions.infinispan.AuthenticationSessionAdapter;
import org.keycloak.models.sessions.infinispan.SessionEntityUpdater;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.BaseUpdater;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.Expiration;
import org.keycloak.models.sessions.infinispan.entities.AuthenticationSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.RootAuthenticationSessionEntity;
import org.keycloak.models.sessions.infinispan.util.SessionTimeouts;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

/**
 * 根认证会话的 {@link Updater}，同时实现 {@link RootAuthenticationSessionModel}。
 * <p>
 * 跟踪 {@link RootAuthenticationSessionEntity} 及其子 {@link AuthenticationSessionEntity} 的变更，
 * 提交时重放到远程 Infinispan 缓存。
 */
public class RootAuthenticationSessionUpdater extends BaseUpdater<String, RootAuthenticationSessionEntity> implements RootAuthenticationSessionModel {

    // 按时间戳选取最旧子会话以便淘汰
    private final static Comparator<Map.Entry<String, AuthenticationSessionEntity>> TIMESTAMP_COMPARATOR =
            Comparator.comparingInt(e -> e.getValue().getTimestamp());

    // 待提交的重放变更列表
    private final List<Consumer<RootAuthenticationSessionEntity>> changes;

    private RealmModel realm;
    private KeycloakSession session;


    private int authSessionsLimit;
    private RootAuthenticationSessionUpdater(String key, RootAuthenticationSessionEntity entity, long version, UpdaterState initialState) {
        super(key, entity, version, initialState);
        if (entity == null) {
            assert initialState == UpdaterState.DELETED;
            changes = List.of();
            return;
        }
        changes = new ArrayList<>(4);
    }

    public synchronized void initialize(KeycloakSession session, RealmModel realm, int authSessionsLimit) {
        this.session = session;
        this.realm = realm;
        this.authSessionsLimit = authSessionsLimit;
    }

    /**
     * @return {@code true} 表示已注入 session/realm 等上下文
     */
    public synchronized boolean isInitialized() {
        return session != null;
    }


    @Override
    protected boolean isUnchanged() {
        return changes.isEmpty();
    }

    /** 新建根认证会话 Updater（本事务创建，无版本号）。 */
    public static RootAuthenticationSessionUpdater create(String key, RootAuthenticationSessionEntity entity) {
        return new RootAuthenticationSessionUpdater(key, Objects.requireNonNull(entity), NO_VERSION, UpdaterState.CREATED);
    }

    /** 包装从缓存读取的根认证会话。 */
    public static RootAuthenticationSessionUpdater wrap(String key, RootAuthenticationSessionEntity value, long version) {
        return new RootAuthenticationSessionUpdater(key, Objects.requireNonNull(value), version, UpdaterState.READ);
    }

    /** 创建表示删除操作的 Updater（未读取即删除）。 */
    public static RootAuthenticationSessionUpdater delete(String key) {
        return new RootAuthenticationSessionUpdater(key, null, NO_VERSION, UpdaterState.DELETED);
    }

    @Override
    public Expiration computeExpiration() {
        return new Expiration(
                SessionTimeouts.getAuthSessionMaxIdleMS(realm, null, getValue()),
                SessionTimeouts.getAuthSessionLifespanMS(realm, null, getValue()));
    }

    @Override
    public RootAuthenticationSessionEntity apply(String ignored, RootAuthenticationSessionEntity cachedEntity) {
        assert !isDeleted();
        assert !isReadOnly();
        if (cachedEntity == null) {
            // 缓存条目已被其他节点移除
            return null;
        }
        changes.forEach(c -> c.accept(cachedEntity));
        // 无子会话时返回 null，触发缓存条目删除
        return cachedEntity.getAuthenticationSessions().isEmpty() ? null : cachedEntity;
    }


    @Override
    public String getId() {
        return getKey();
    }

    @Override
    public RealmModel getRealm() {
        return realm;
    }

    @Override
    public int getTimestamp() {
        return getValue().getTimestamp();
    }

    @Override
    public void setTimestamp(int timestamp) {
        addAndApplyChange(entity -> entity.setTimestamp(timestamp));
    }

    @Override
    public Map<String, AuthenticationSessionModel> getAuthenticationSessions() {
        Map<String, AuthenticationSessionModel> result = new HashMap<>();

        for (Map.Entry<String, AuthenticationSessionEntity> entry : getValue().getAuthenticationSessions().entrySet()) {
            String tabId = entry.getKey();
            result.put(tabId, new AuthenticationSessionAdapter(session, this, new AuthenticationSessionUpdater(this, tabId, entry.getValue()), tabId));
        }

        return result;
    }

    @Override
    public AuthenticationSessionModel getAuthenticationSession(ClientModel client, String tabId) {
        if (client == null || tabId == null) {
            return null;
        }

        AuthenticationSessionModel authSession = getAuthenticationSessions().get(tabId);
        if (authSession != null && client.equals(authSession.getClient())) {
            session.getContext().setAuthenticationSession(authSession);
            return authSession;
        } else {
            return null;
        }
    }

    @Override
    public AuthenticationSessionModel createAuthenticationSession(ClientModel client) {
        Objects.requireNonNull(client, "client");

        AuthenticationSessionEntity authSessionEntity = new AuthenticationSessionEntity();
        authSessionEntity.setClientUUID(client.getId());
        String newTabId = Base64Url.encode(SecretGenerator.getInstance().randomBytes(8));
        int timestamp = Time.currentTime();

        addAndApplyChange(entity -> {
            Map<String, AuthenticationSessionEntity> authenticationSessions = entity.getAuthenticationSessions();
            // 超过上限时淘汰时间戳最早的子会话
            if (authenticationSessions.size() >= authSessionsLimit && !authenticationSessions.containsKey(newTabId)) {
                authenticationSessions.entrySet().stream()
                        .min(TIMESTAMP_COMPARATOR)
                        .map(Map.Entry::getKey)
                        .ifPresent(authenticationSessions::remove);
            }
            authSessionEntity.setTimestamp(timestamp);
            authenticationSessions.put(newTabId, authSessionEntity);

            // 新增子会话时同步更新根会话时间戳
            entity.setTimestamp(timestamp);
        });

        AuthenticationSessionAdapter authSession = new AuthenticationSessionAdapter(session, this, new AuthenticationSessionUpdater(this, newTabId, authSessionEntity), newTabId);
        session.getContext().setAuthenticationSession(authSession);
        return authSession;
    }

    @Override
    public void removeAuthenticationSessionByTabId(String tabId) {
        if (getValue().getAuthenticationSessions().remove(tabId) != null) {
            if (getValue().getAuthenticationSessions().isEmpty()) {
                // 最后一个子会话移除后标记根会话删除
                markDeleted();
            } else {
                int currentTime = Time.currentTime();
                addAndApplyChange(entity -> {
                    entity.getAuthenticationSessions().remove(tabId);
                    entity.setTimestamp(currentTime);
                });
            }
        }
    }

    @Override
    public void restartSession(RealmModel realm) {
        addAndApplyChange(entity ->  {
            entity.getAuthenticationSessions().clear();
            entity.setTimestamp(Time.currentTime());
        });
    }

    /** 记录变更并立即应用到本地实体快照。 */
    private void addAndApplyChange(Consumer<RootAuthenticationSessionEntity> change) {
        changes.add(change);
        change.accept(getValue());
    }

    /** 子认证会话的变更代理，将修改回写到根实体 map。 */
    private record AuthenticationSessionUpdater(RootAuthenticationSessionUpdater updater, String tabId, AuthenticationSessionEntity authenticationSession) implements SessionEntityUpdater<AuthenticationSessionEntity> {

        @Override
        public AuthenticationSessionEntity getEntity() {
            return authenticationSession;
        }

        @Override
        public void onEntityUpdated() {
            updater.addAndApplyChange(entity-> {
                entity.getAuthenticationSessions().put(tabId, authenticationSession);
            });
        }

        @Override
        public void onEntityRemoved() {

        }
    }


}
