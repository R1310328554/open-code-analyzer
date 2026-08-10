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

package org.keycloak.models.sessions.infinispan.changes.remote.updater.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.BaseUpdater;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.Expiration;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.Updater;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.UpdaterFactory;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.helper.MapUpdater;
import org.keycloak.models.sessions.infinispan.entities.ClientSessionKey;
import org.keycloak.models.sessions.infinispan.entities.RemoteAuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.remote.transaction.ClientSessionChangeLogTransaction;
import org.keycloak.models.sessions.infinispan.util.SessionTimeouts;

import org.infinispan.client.hotrod.RemoteCache;

/**
 * 跟踪 {@link AuthenticatedClientSessionModel} 变更的 {@link Updater} 实现。
 * <p>
 * 同时实现在线/离线客户端会话模型，提交时将 notes 与字段变更重放到远程缓存实体。
 */
public class AuthenticatedClientSessionUpdater extends BaseUpdater<ClientSessionKey, RemoteAuthenticatedClientSessionEntity> implements AuthenticatedClientSessionModel {

    private static final Factory ONLINE = new Factory(false);
    private static final Factory OFFLINE = new Factory(true);

    // notes 字段的可重放 map 包装器
    private final MapUpdater<String, String> notesUpdater;
    private final List<Consumer<RemoteAuthenticatedClientSessionEntity>> changes;
    private final boolean offline;
    private UserSessionModel userSession;
    private ClientModel client;
    private ClientSessionChangeLogTransaction clientTransaction;

    private AuthenticatedClientSessionUpdater(ClientSessionKey cacheKey, RemoteAuthenticatedClientSessionEntity cacheValue, long version, boolean offline, UpdaterState initialState) {
        super(cacheKey, cacheValue, version, initialState);
        this.offline = offline;
        if (cacheValue == null) {
            assert initialState == UpdaterState.DELETED; // cannot be undone
            notesUpdater = null;
            changes = List.of();
            return;
        }
        initNotes(cacheValue);
        notesUpdater = new MapUpdater<>(cacheValue.getNotes());
        changes = new ArrayList<>(4);
    }

    /**
     * @return 在线客户端会话的 {@link UpdaterFactory}
     */
    public static UpdaterFactory<ClientSessionKey, RemoteAuthenticatedClientSessionEntity, AuthenticatedClientSessionUpdater> onlineFactory() {
        return ONLINE;
    }

    /**
     * @return 离线客户端会话的 {@link UpdaterFactory}
     */
    public static UpdaterFactory<ClientSessionKey, RemoteAuthenticatedClientSessionEntity, AuthenticatedClientSessionUpdater> offlineFactory() {
        return OFFLINE;
    }

    @Override
    public RemoteAuthenticatedClientSessionEntity apply(ClientSessionKey uuid, RemoteAuthenticatedClientSessionEntity entity) {
        initNotes(entity);
        notesUpdater.applyChanges(entity.getNotes());
        changes.forEach(change -> change.accept(entity));
        if (isCreated()) {
            // ID 生成非随机；RefreshTokenTest 中可能出现 KC 已过期但外部 Infinispan 未过期的情况
            // 生产环境若发生类似情况，需合并 timestamp 与 started 取较大值
            entity.setTimestamp(Math.max(entity.getTimestamp(), getTimestamp()));
            entity.setStarted(Math.max(entity.getStarted(), getStarted()));
        }
        return entity;
    }

    @Override
    public Expiration computeExpiration() {
        long maxIdle = SessionTimeouts.getClientSessionMaxIdleMs(userSession.getRealm(), client, offline, isUserSessionRememberMe(), getTimestamp());
        long lifespan = SessionTimeouts.getClientSessionLifespanMs(userSession.getRealm(), client, offline, isUserSessionRememberMe(), getStarted(), getUserSessionStarted());
        return new Expiration(SessionTimeouts.IMMORTAL_FLAG, SessionTimeouts.calculateEffectiveSessionLifespan(maxIdle, lifespan));
    }

    @Override
    public String getId() {
        return getValue().createId();
    }

    @Override
    public int getStarted() {
        return getValue().getStarted();
    }

    @Override
    public int getUserSessionStarted() {
        checkInitialized();
        return userSession.getStarted();
    }

    @Override
    public boolean isUserSessionRememberMe() {
        checkInitialized();
        return userSession.isRememberMe();
    }

    @Override
    public int getTimestamp() {
        return getValue().getTimestamp();
    }

    @Override
    public void setTimestamp(int timestamp) {
        addAndApplyChange(entity -> entity.setTimestamp(Math.max(timestamp, entity.getTimestamp())));
    }

    @Override
    public void detachFromUserSession() {
        clientTransaction.remove(getKey());
    }

    @Override
    public UserSessionModel getUserSession() {
        return userSession;
    }

    @Override
    public String getNote(String name) {
        return notesUpdater.get(name);
    }

    @Override
    public void setNote(String name, String value) {
        notesUpdater.put(name, value);
    }

    @Override
    public void removeNote(String name) {
        notesUpdater.remove(name);
    }

    @Override
    public Map<String, String> getNotes() {
        return notesUpdater;
    }

    @Override
    public String getRedirectUri() {
        return getValue().getRedirectUri();
    }

    @Override
    public void setRedirectUri(String uri) {
        addAndApplyChange(entity -> entity.setRedirectUri(uri));
    }

    @Override
    public RealmModel getRealm() {
        return userSession.getRealm();
    }

    @Override
    public ClientModel getClient() {
        return client;
    }

    @Override
    public String getAction() {
        return getValue().getAction();
    }

    @Override
    public void setAction(String action) {
        addAndApplyChange(entity -> entity.setAction(action));
    }

    @Override
    public String getProtocol() {
        return getValue().getProtocol();
    }

    @Override
    public void setProtocol(String method) {
        addAndApplyChange(entity -> entity.setProtocol(method));
    }

    @Override
    public void restartClientSession() {
        changes.clear();
        resetState();
        addAndApplyChange(RemoteAuthenticatedClientSessionEntity::restart);
    }

    @Override
    public boolean isTransient() {
        return !isDeleted() && userSession.getPersistenceState() == UserSessionModel.SessionPersistenceState.TRANSIENT;
    }

    @Override
    protected boolean isUnchanged() {
        return changes.isEmpty() && notesUpdater.isUnchanged();
    }

    /**
     * 注入用户会话、客户端及变更日志事务等上下文引用。
     *
     * @param userSession       关联的 {@link UserSessionModel}
     * @param client            关联的 {@link ClientModel}
     * @param clientTransaction 用于将变更写入 {@link RemoteCache} 的 {@link ClientSessionChangeLogTransaction}
     */
    public synchronized void initialize(UserSessionModel userSession, ClientModel client, ClientSessionChangeLogTransaction clientTransaction) {
        this.userSession = Objects.requireNonNull(userSession);
        this.client = Objects.requireNonNull(client);
        this.clientTransaction = Objects.requireNonNull(clientTransaction);
    }

    /**
     * @return {@code true} 表示已注入 userSession/client 等上下文
     */
    public synchronized boolean isInitialized() {
        return userSession != null;
    }

    /** 记录变更并立即应用到本地实体快照。 */
    private void addAndApplyChange(Consumer<RemoteAuthenticatedClientSessionEntity> change) {
        changes.add(change);
        change.accept(getValue());
    }

    private void checkInitialized() {
        if (!isInitialized()) {
            throw new IllegalStateException(getClass().getSimpleName() + " not initialized yet!");
        }
    }

    /** 确保 notes map 非 null，便于 MapUpdater 跟踪变更。 */
    private static void initNotes(RemoteAuthenticatedClientSessionEntity entity) {
        var notes = entity.getNotes();
        if (notes == null) {
            entity.setNotes(new HashMap<>());
        }
    }

    /** 按 online/offline 区分创建、包装或删除 Updater 的内部工厂。 */
    private record Factory(
            boolean offline) implements UpdaterFactory<ClientSessionKey, RemoteAuthenticatedClientSessionEntity, AuthenticatedClientSessionUpdater> {

        @Override
        public AuthenticatedClientSessionUpdater create(ClientSessionKey key, RemoteAuthenticatedClientSessionEntity entity) {
            return new AuthenticatedClientSessionUpdater(key, Objects.requireNonNull(entity), NO_VERSION, offline, UpdaterState.CREATED);
        }

        @Override
        public AuthenticatedClientSessionUpdater wrapFromCache(ClientSessionKey key, RemoteAuthenticatedClientSessionEntity value, long version) {
            return new AuthenticatedClientSessionUpdater(key, Objects.requireNonNull(value), version, offline, UpdaterState.READ);
        }

        @Override
        public AuthenticatedClientSessionUpdater deleted(ClientSessionKey key) {
            return new AuthenticatedClientSessionUpdater(key, null, NO_VERSION, offline, UpdaterState.DELETED);
        }
    }

}
