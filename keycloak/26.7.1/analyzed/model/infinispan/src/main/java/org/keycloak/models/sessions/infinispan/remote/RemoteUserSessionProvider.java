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

import java.lang.invoke.MethodHandles;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.cluster.ClusterProvider;
import org.keycloak.common.Profile;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.UserSessionProvider;
import org.keycloak.models.light.LightweightUserAdapter;
import org.keycloak.models.session.UserSessionPersisterProvider;
import org.keycloak.models.sessions.infinispan.ImmutableSession;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.BaseUpdater;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.client.AuthenticatedClientSessionUpdater;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.user.AuthenticatedClientSessionMapping;
import org.keycloak.models.sessions.infinispan.changes.remote.updater.user.UserSessionUpdater;
import org.keycloak.models.sessions.infinispan.entities.ClientSessionKey;
import org.keycloak.models.sessions.infinispan.entities.RemoteAuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.RemoteUserSessionEntity;
import org.keycloak.models.sessions.infinispan.query.ClientSessionQueries;
import org.keycloak.models.sessions.infinispan.query.QueryHelper;
import org.keycloak.models.sessions.infinispan.query.UserSessionQueries;
import org.keycloak.models.sessions.infinispan.remote.transaction.ClientSessionChangeLogTransaction;
import org.keycloak.models.sessions.infinispan.remote.transaction.UserSessionChangeLogTransaction;
import org.keycloak.models.sessions.infinispan.remote.transaction.UserSessionTransaction;
import org.keycloak.models.sessions.infinispan.util.SessionExpirationPredicates;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.utils.StreamsUtil;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.util.concurrent.CompletionStages;
import org.jboss.logging.Logger;

import static org.keycloak.models.Constants.SESSION_NOTE_LIGHTWEIGHT_USER;

/**
 * 仅使用 {@link RemoteCache} 存储的 {@link UserSessionProvider} 实现。
 * <p>
 * 在线/离线用户会话与客户端会话经 {@link UserSessionTransaction} 统一管理；
 * 查询走 Ickle/Remote Query，批量迁移自 {@link UserSessionPersisterProvider}。
 */
public class RemoteUserSessionProvider implements UserSessionProvider {

    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass());
    /** 并发远程 get 请求上限。 */
    private static final int MAX_CONCURRENT_REQUESTS = 16;

    private final KeycloakSession session;
    /** 聚合四套会话变更日志事务。 */
    private final UserSessionTransaction transaction;
    /** 流式查询每批拉取条数。 */
    private final int batchSize;

    public RemoteUserSessionProvider(KeycloakSession session, UserSessionTransaction transaction, int batchSize) {
        this.session = session;
        this.transaction = transaction;
        this.batchSize = batchSize;
    }

    @Override
    public AuthenticatedClientSessionModel createClientSession(RealmModel realm, ClientModel client, UserSessionModel userSession) {
        var clientTx = getClientSessionTransaction(false);
        var key = new ClientSessionKey(userSession.getId(), client.getId());
        var entity = RemoteAuthenticatedClientSessionEntity.create(key, realm.getId(), userSession);
        var model = clientTx.create(key, entity);
        if (!model.isInitialized()) {
            model.initialize(userSession, client, clientTx);
        }
        return model;
    }

    @Override
    public AuthenticatedClientSessionModel getClientSession(UserSessionModel userSession, ClientModel client, boolean offline) {
        var clientTx = getClientSessionTransaction(offline);
        var updater = clientTx.get(new ClientSessionKey(userSession.getId(), client.getId()));
        if (updater == null) {
            return null;
        }
        if (!updater.isInitialized()) {
            updater.initialize(userSession, client, clientTx);
        }
        return updater;
    }

    @Override
    public UserSessionModel createUserSession(String id, RealmModel realm, UserModel user, String loginUsername, String ipAddress, String authMethod, boolean rememberMe, String brokerSessionId, String brokerUserId, UserSessionModel.SessionPersistenceState persistenceState) {
        if (id == null) {
            id = SecretGenerator.SECURE_ID_GENERATOR.get();
        }

        var entity = RemoteUserSessionEntity.create(id, realm, user, loginUsername, ipAddress, authMethod, rememberMe, brokerSessionId, brokerUserId);
        var updater = getUserSessionTransaction(false).create(id, entity);
        return initUserSessionUpdater(updater, persistenceState, realm, user, false);
    }

    @Override
    public UserSessionModel getUserSession(RealmModel realm, String id) {
        return getUserSession(realm, id, false);
    }

    @Override
    public Stream<UserSessionModel> getUserSessionsStream(RealmModel realm, UserModel user) {
        return StreamsUtil.closing(streamUserSessionByUserId(realm, user, false));
    }

    @Override
    public Stream<UserSessionModel> getUserSessionsStream(RealmModel realm, ClientModel client) {
        return StreamsUtil.closing(streamUserSessionByClientId(realm, client.getId(), false, null, null));
    }

    @Override
    public Stream<UserSessionModel> getUserSessionsStream(RealmModel realm, ClientModel client, Integer firstResult, Integer maxResults) {
        return StreamsUtil.closing(streamUserSessionByClientId(realm, client.getId(), false, firstResult, maxResults));
    }

    @Override
    public Stream<UserSessionModel> getUserSessionByBrokerUserIdStream(RealmModel realm, String brokerUserId) {
        return StreamsUtil.closing(streamUserSessionByBrokerUserId(realm, brokerUserId, false));
    }

    @Override
    public UserSessionModel getUserSessionByBrokerSessionId(RealmModel realm, String brokerSessionId) {
        var userTx = getUserSessionTransaction(false);
        var query = UserSessionQueries.searchByBrokerSessionId(userTx.getCache(), realm.getId(), brokerSessionId);
        return QueryHelper.fetchSingle(query, userTx::wrapFromProjection)
                .map(session -> initUserSessionFromQuery(session, realm, null, false))
                .orElse(null);
    }

    @Override
    public UserSessionModel getUserSessionWithPredicate(RealmModel realm, String id, boolean offline, Predicate<UserSessionModel> predicate) {
        var updater = getUserSession(realm, id, offline);
        return updater != null && predicate.test(updater) ? updater : null;
    }

    @Override
    public long getActiveUserSessions(RealmModel realm, ClientModel client) {
        return computeUserSessionCount(realm, client, false);
    }

    @Override
    public Map<String, Long> getActiveClientSessionStats(RealmModel realm, boolean offline) {
        var query = ClientSessionQueries.activeClientCount(getClientSessionTransaction(offline).getCache(), realm.getId());
        return QueryHelper.streamAll(query, batchSize, QueryHelper.PROJECTION_TO_STRING_LONG_ENTRY)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void removeUserSession(RealmModel realm, UserSessionModel userSession) {
        internalRemoveUserSession(userSession, false);
    }

    @Override
    public void removeUserSessions(RealmModel realm, UserModel user) {
        transaction.removeAllSessionByUserId(realm.getId(), user.getId());
    }

    @Override
    public void removeUserSessions(RealmModel realm) {
        transaction.removeAllSessionsByRealmId(realm.getId());
    }

    /** realm 删除时清除远程缓存并通知持久化层。 */
    @Override
    public void onRealmRemoved(RealmModel realm) {
        transaction.removeAllSessionsByRealmId(realm.getId());
        var database = session.getProvider(UserSessionPersisterProvider.class);
        if (database != null) {
            database.onRealmRemoved(realm);
        }
    }

    @Override
    public void onClientRemoved(RealmModel realm, ClientModel client) {
        var database = session.getProvider(UserSessionPersisterProvider.class);
        if (database != null) {
            database.onClientRemoved(realm, client);
        }
    }

    @Override
    public UserSessionModel createOfflineUserSession(UserSessionModel userSession) {
        var entity = RemoteUserSessionEntity.createFromModel(userSession);
        var updater = getUserSessionTransaction(true).create(userSession.getId(), entity);
        return initUserSessionUpdater(updater, userSession.getPersistenceState(), userSession.getRealm(), userSession.getUser(), true);
    }

    @Override
    public UserSessionModel getOfflineUserSession(RealmModel realm, String userSessionId) {
        return getUserSession(realm, userSessionId, true);
    }

    @Override
    public void removeOfflineUserSession(RealmModel realm, UserSessionModel userSession) {
        internalRemoveUserSession(userSession, true);
    }

    @Override
    public AuthenticatedClientSessionModel createOfflineClientSession(AuthenticatedClientSessionModel clientSession, UserSessionModel offlineUserSession) {
        var clientTx = getClientSessionTransaction(true);
        var key = new ClientSessionKey(offlineUserSession.getId(), clientSession.getClient().getId());
        var entity = RemoteAuthenticatedClientSessionEntity.createFromModel(key, clientSession);
        var model = clientTx.create(key, entity);
        if (!model.isInitialized()) {
            model.initialize(offlineUserSession, clientSession.getClient(), clientTx);
        }
        return model;
    }

    @Override
    public Stream<UserSessionModel> getOfflineUserSessionsStream(RealmModel realm, UserModel user) {
        return StreamsUtil.closing(streamUserSessionByUserId(realm, user, true));
    }

    @Override
    public Stream<UserSessionModel> getOfflineUserSessionByBrokerUserIdStream(RealmModel realm, String brokerUserId) {
        return StreamsUtil.closing(streamUserSessionByBrokerUserId(realm, brokerUserId, true));
    }

    @Override
    public long getOfflineSessionsCount(RealmModel realm, ClientModel client) {
        return computeUserSessionCount(realm, client, true);
    }

    @Override
    public Stream<UserSessionModel> getOfflineUserSessionsStream(RealmModel realm, ClientModel client, Integer firstResult, Integer maxResults) {
        return StreamsUtil.closing(streamUserSessionByClientId(realm, client.getId(), true, firstResult, maxResults));
    }

    @Override
    public int getStartupTime(RealmModel realm) {
        return session.getProvider(ClusterProvider.class).getClusterStartupTime();
    }

    @Override
    public KeycloakSession getKeycloakSession() {
        return session;
    }

    @Override
    public void close() {

    }

    /** 25.0.0 模型升级：将数据库中的在线/离线会话批量迁入远程缓存。 */
    @Override
    public void migrate(String modelVersion) {
        if ("25.0.0".equals(modelVersion)) {
            migrateUserSessions(true);
            migrateUserSessions(false);
        }

    }

    @Override
    public Stream<UserSessionModel> readOnlyStreamOfflineUserSessions(RealmModel realm) {
        return readOnlyStream(realm, true);
    }

    @Override
    public Stream<UserSessionModel> readOnlyStreamUserSessions(RealmModel realm) {
        return readOnlyStream(realm, false);
    }

    @Override
    public Stream<UserSessionModel> readOnlyStreamOfflineUserSessions(RealmModel realm, ClientModel client, int skip, int maxResults) {
        return readOnlyStream(realm, client, true, skip, maxResults);
    }

    @Override
    public Stream<UserSessionModel> readOnlyStreamUserSessions(RealmModel realm, ClientModel client, int skip, int maxResults) {
        return readOnlyStream(realm, client, false, skip, maxResults);
    }

    /** 只读遍历 realm 下全部会话（效率较低，逐条复制为不可变视图）。 */
    private Stream<UserSessionModel> readOnlyStream(RealmModel realm, boolean offline) {
        var expiration = new SessionExpirationPredicates(realm, offline, Time.currentTime());
        var query = UserSessionQueries.searchByRealm(getUserSessionTransaction(offline).getCache(), realm.getId());
        var clientSessionCache = getClientSessionTransaction(offline).getCache();
        // 效率不高：先查全部用户会话再逐条组装客户端会话。
        return Flowable.fromStream(QueryHelper.streamAll(query, batchSize, Function.identity()))
                .buffer(128)
                .blockingStream()
                .flatMap(us -> ImmutableSession.copyOf(session, us, expiration, clientSessionCache, batchSize)).filter(Objects::nonNull);
    }

    private Stream<UserSessionModel> readOnlyStream(RealmModel realm, ClientModel client, boolean offline, int skip, int maxResults) {
        if (maxResults == 0) {
            return Stream.empty();
        }
        var expiration = new SessionExpirationPredicates(realm, offline, Time.currentTime());
        var clientSessionCache = getClientSessionTransaction(offline).getCache();
        var userSessionIdQuery = ClientSessionQueries.fetchUserSessionIdForClientId(clientSessionCache, realm.getId(), client.getId());
        if (skip > 0) {
            userSessionIdQuery.startOffset(skip);
        }
        if (maxResults > 0) {
            userSessionIdQuery.maxResults(maxResults);
        }
        var userSessionCache = getUserSessionTransaction(offline).getCache();
        return Flowable.fromIterable(QueryHelper.toCollection(userSessionIdQuery, QueryHelper.SINGLE_PROJECTION_TO_STRING))
                .buffer(128)
                .flatMapMaybe(sessionId -> Maybe.fromCompletionStage(userSessionCache.getAllAsync(Set.copyOf(sessionId))), false, MAX_CONCURRENT_REQUESTS)
                .map(Map::values)
                .blockingStream()
                .flatMap(entity -> ImmutableSession.copyOf(session, entity, expiration, clientSessionCache, batchSize));
    }

    private void migrateUserSessions(boolean offline) {
        log.info("Migrate user sessions from database to the remote cache");

        List<String> userSessionIds = Collections.synchronizedList(new ArrayList<>(batchSize));
        List<Map.Entry<String, String>> clientSessionIds = Collections.synchronizedList(new ArrayList<>(batchSize));
        boolean hasSessions;
        do {
            hasSessions = migrateUserSessionBatch(session.getKeycloakSessionFactory(), offline, userSessionIds, clientSessionIds);
        } while (hasSessions);

        log.info("All sessions migrated.");
    }

    /** 从数据库分批迁移会话到远程缓存，成功后从 DB 删除。 */
    private boolean migrateUserSessionBatch(KeycloakSessionFactory factory, boolean offline, List<String> userSessionBuffer, List<Map.Entry<String, String>> clientSessionBuffer) {
        var userSessionCache = getUserSessionTransaction(offline).getCache();
        var clientSessionCache = getClientSessionTransaction(offline).getCache();

        log.infof("Migrating %s user(s) session(s) from database.", batchSize);

        return KeycloakModelUtils.runJobInTransactionWithResult(factory, kcSession -> {
            var database = kcSession.getProvider(UserSessionPersisterProvider.class);
            var stage = CompletionStages.aggregateCompletionStage();
            database.loadUserSessionsStream(-1, batchSize, offline, "")
                    .forEach(userSessionModel -> {
                        var userSessionEntity = RemoteUserSessionEntity.createFromModel(userSessionModel);
                        stage.dependsOn(userSessionCache.putIfAbsentAsync(userSessionModel.getId(), userSessionEntity));
                        userSessionBuffer.add(userSessionModel.getId());
                        for (var clientSessionModel : userSessionModel.getAuthenticatedClientSessions().values()) {
                            var clientSessionKey = new ClientSessionKey(userSessionModel.getId(), clientSessionModel.getClient().getId());
                            clientSessionBuffer.add(Map.entry(clientSessionKey.userSessionId(), clientSessionKey.clientId()));
                            var clientSessionEntity = RemoteAuthenticatedClientSessionEntity.createFromModel(clientSessionKey, clientSessionModel);
                            stage.dependsOn(clientSessionCache.putIfAbsentAsync(clientSessionKey, clientSessionEntity));
                        }
                    });
            CompletionStages.join(stage.freeze());

            if (userSessionBuffer.isEmpty() && clientSessionBuffer.isEmpty()) {
                return false;
            }

            log.infof("%s user(s) session(s) stored in the remote cache. Removing them from database.", userSessionBuffer.size());

            userSessionBuffer.forEach(s -> database.removeUserSession(s, offline));
            userSessionBuffer.clear();

            clientSessionBuffer.forEach(e -> database.removeClientSession(e.getKey(), e.getValue(), offline));
            clientSessionBuffer.clear();

            return true;
        });
    }

    private UserSessionUpdater getUserSession(RealmModel realm, String id, boolean offline) {
        if (id == null) {
            return null;
        }
        var updater = getUserSessionTransaction(offline).get(id);
        if (updater == null || !updater.getValue().getRealmId().equals(realm.getId())) {
            return null;
        }
        if (updater.isInitialized()) {
            return updater;
        }
        UserModel user = session.users().getUserById(realm, updater.getValue().getUserId());
        return initUserSessionUpdater(updater, UserSessionModel.SessionPersistenceState.PERSISTENT, realm, user, offline);
    }

    private void internalRemoveUserSession(UserSessionModel userSession, boolean offline) {
        transaction.removeUserSessionById(userSession.getId(), offline);
    }

    private UserSessionChangeLogTransaction getUserSessionTransaction(boolean offline) {
        return transaction.getUserSessions(offline);
    }

    private ClientSessionChangeLogTransaction getClientSessionTransaction(boolean offline) {
        return transaction.getClientSessions(offline);
    }

    private UserSessionUpdater initUserSessionFromQuery(UserSessionUpdater updater, RealmModel realm, UserModel user, boolean offline) {
        assert updater != null;
        assert realm != null;
        if (updater.isInvalid()) {
            return null;
        }
        if (updater.isInitialized()) {
            return updater;
        }
        if (user == null) {
            user = session.users().getUserById(realm, updater.getValue().getUserId());
        }
        return initUserSessionUpdater(updater, UserSessionModel.SessionPersistenceState.PERSISTENT, realm, user, offline);
    }

    /** 加载并初始化用户会话；用户不存在时视为孤儿会话并删除。 */
    private UserSessionUpdater initUserSessionUpdater(UserSessionUpdater updater, UserSessionModel.SessionPersistenceState persistenceState, RealmModel realm, UserModel user, boolean offline) {
        if (user instanceof LightweightUserAdapter) {
            updater.initialize(persistenceState, realm, user, new ClientSessionMapping(updater));
            return checkExpiration(updater);
        }
        // 逻辑自 org.keycloak.models.sessions.infinispan.InfinispanUserSessionProvider 复制
        if (Profile.isFeatureEnabled(Profile.Feature.TRANSIENT_USERS) && updater.getNotes().containsKey(SESSION_NOTE_LIGHTWEIGHT_USER)) {
            LightweightUserAdapter lua = LightweightUserAdapter.fromString(session, realm, updater.getNotes().get(SESSION_NOTE_LIGHTWEIGHT_USER));
            updater.initialize(persistenceState, realm, lua, new ClientSessionMapping(updater));
            lua.setUpdateHandler(lua1 -> {
                if (lua == lua1) {  // 避免冲突：仅最新轻量用户模型可写回 note
                    updater.setNote(SESSION_NOTE_LIGHTWEIGHT_USER, lua1.serialize());
                }
            });
            return checkExpiration(updater);
        }

        if (user == null) {
            // 用户已不存在，从缓存移除孤儿会话
            internalRemoveUserSession(updater, offline);
            return null;
        }
        updater.initialize(persistenceState, realm, user, new ClientSessionMapping(updater));
        return checkExpiration(updater);
    }

    private AuthenticatedClientSessionModel initClientSessionUpdater(AuthenticatedClientSessionUpdater updater, UserSessionUpdater userSession) {
        if (updater == null || updater.isInvalid()) {
            return null;
        }
        var client = userSession.getRealm().getClientById(updater.getKey().clientId());
        if (client == null) {
            updater.markDeleted();
            return null;
        }
        if (updater.isInitialized()) {
            return updater;
        }
        updater.initialize(userSession, client, getClientSessionTransaction(userSession.isOffline()));
        return checkExpiration(updater);
    }

    private long computeUserSessionCount(RealmModel realm, ClientModel client, boolean offline) {
        var query = ClientSessionQueries.countClientSessions(getClientSessionTransaction(offline).getCache(), realm.getId(), client.getId());
        return QueryHelper.fetchSingle(query, QueryHelper.SINGLE_PROJECTION_TO_LONG).orElse(0L);
    }

    private Stream<UserSessionModel> streamUserSessionByUserId(RealmModel realm, UserModel user, boolean offline) {
        var userTx = getUserSessionTransaction(offline);
        var query = UserSessionQueries.searchByUserId(userTx.getCache(), realm.getId(), user.getId());
        return QueryHelper.streamAll(query, batchSize, userTx::wrapFromProjection)
                .map(session -> initUserSessionFromQuery(session, realm, user, offline))
                .filter(Objects::nonNull)
                .map(UserSessionModel.class::cast);
    }

    private Stream<UserSessionModel> streamUserSessionByBrokerUserId(RealmModel realm, String brokerUserId, boolean offline) {
        var userTx = getUserSessionTransaction(offline);
        var query = UserSessionQueries.searchByBrokerUserId(userTx.getCache(), realm.getId(), brokerUserId);
        return QueryHelper.streamAll(query, batchSize, userTx::wrapFromProjection)
                .map(session -> initUserSessionFromQuery(session, realm, null, offline))
                .filter(Objects::nonNull)
                .map(UserSessionModel.class::cast);
    }

    private Stream<UserSessionModel> streamUserSessionByClientId(RealmModel realm, String clientId, boolean offline, Integer offset, Integer maxResults) {
        var userSessionIdQuery = ClientSessionQueries.fetchUserSessionIdForClientId(getClientSessionTransaction(offline).getCache(), realm.getId(), clientId);
        if (offset != null && offset > -1) {
            userSessionIdQuery.startOffset(offset);
        }
        userSessionIdQuery.maxResults(maxResults == null || maxResults == -1 ? Integer.MAX_VALUE : maxResults);
        var userSessionTx = getUserSessionTransaction(offline);
        return Flowable.fromIterable(QueryHelper.toCollection(userSessionIdQuery, QueryHelper.SINGLE_PROJECTION_TO_STRING))
                .flatMapMaybe(userSessionTx::maybeGet, false, MAX_CONCURRENT_REQUESTS)
                .blockingStream(batchSize)
                .map(session -> initUserSessionFromQuery(session, realm, null, offline))
                .filter(Objects::nonNull)
                .map(UserSessionModel.class::cast);
    }

    /** 过期则标记并丢弃，否则返回 updater。 */
    private static <K, V, T extends BaseUpdater<K, V>> T checkExpiration(T updater) {
        var expiration = updater.computeExpiration();
        if (expiration.isExpired()) {
            updater.markExpired();
            return null;
        }
        return updater;
    }

    /**
     * 用户会话下的客户端会话映射：懒加载远程客户端会话并缓存在当前事务中。
     */
    private class ClientSessionMapping extends AbstractMap<String, AuthenticatedClientSessionModel> implements Consumer<RemoteAuthenticatedClientSessionEntity>, AuthenticatedClientSessionMapping {

        private final UserSessionUpdater userSession;
        /** 尚未从远程缓存批量拉取客户端会话时为 true。 */
        private boolean coldCache = true;

        ClientSessionMapping(UserSessionUpdater userSession) {
            this.userSession = userSession;
        }

        @Override
        public AuthenticatedClientSessionModel get(Object key) {
            var updater = getTransaction().get(keyForClientId(key));
            return initClientSessionUpdater(updater, userSession);
        }

        @Override
        public AuthenticatedClientSessionModel remove(Object key) {
            getTransaction().remove(keyForClientId(key));
            return null;
        }

        @Override
        public boolean containsKey(Object key) {
            return get(key) != null;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Set<Entry<String, AuthenticatedClientSessionModel>> entrySet() {
            if (coldCache) {
                fetchAndCacheClientSessions();
                coldCache = false;
            }
            // 优先遍历事务内已缓存的客户端会话。
            return getTransaction().getClientSessions()
                    .filter(this::isFromUserSession)
                    .map(this::initialize)
                    .filter(Objects::nonNull)
                    .map(RemoteUserSessionProvider::toMapEntry)
                    .collect(Collectors.toSet());
        }

        private ClientSessionKey keyForClientId(String clientId) {
            return new ClientSessionKey(getUserSessionId(), clientId);
        }

        private ClientSessionKey keyForClientId(Object clientId) {
            return keyForClientId(String.valueOf(clientId));
        }

        private ClientSessionKey keyForClientId(Object[] projection) {
            assert projection.length == 1;
            return keyForClientId(String.valueOf(projection[0]));
        }

        private void fetchAndCacheClientSessions() {
            var query = ClientSessionQueries.fetchClientSessions(getTransaction().getCache(), getUserSessionId());
            QueryHelper.streamAll(query, batchSize, Function.identity()).forEach(this);
        }

        @Override
        public void accept(RemoteAuthenticatedClientSessionEntity entity) {
            getTransaction().wrapFromProjection(entity);
        }

        private ClientSessionChangeLogTransaction getTransaction() {
            return getClientSessionTransaction(userSession.isOffline());
        }

        private String getUserSessionId() {
            return userSession.getKey();
        }

        private boolean isFromUserSession(AuthenticatedClientSessionUpdater updater) {
            return Objects.equals(getUserSessionId(), updater.getValue().getUserSessionId());
        }

        private AuthenticatedClientSessionModel initialize(AuthenticatedClientSessionUpdater updater) {
            return initClientSessionUpdater(updater, userSession);
        }

        /** 用户会话重启时删除其下全部客户端会话（冷缓存则先查 ID 再删）。 */
        @Override
        public void onUserSessionRestart() {
            if (coldCache) {
                // 事务内未缓存全部会话：先查客户端 ID 再逐条标记删除。
                var query = ClientSessionQueries.fetchClientSessionsIds(getTransaction().getCache(), getUserSessionId());
                QueryHelper.streamAll(query, batchSize, this::keyForClientId)
                        .forEach(getTransaction()::remove);
                coldCache = false;
                return;
            }
            getTransaction().getClientSessions()
                    .filter(this::isFromUserSession)
                    .forEach(BaseUpdater::markDeleted);
        }
    }

    private static Map.Entry<String, AuthenticatedClientSessionModel> toMapEntry(AuthenticatedClientSessionModel model) {
        return Map.entry(model.getClient().getId(), model);
    }
}
