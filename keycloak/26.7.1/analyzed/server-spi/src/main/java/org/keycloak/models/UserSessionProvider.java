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

package org.keycloak.models;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.keycloak.provider.Provider;

/**
 * 用户会话 Provider：管理在线/离线用户会话与客户端会话的创建、查询与清理。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserSessionProvider extends Provider {

    /**
     * 返回当前 Keycloak 会话（已弃用）。
     * Returns currently used Keycloak session.
     * @return {@link KeycloakSession}
     * @deprecated for removal.
     */
    // 未使用，待移除
    // It is not used anywhere. Remove it?
    @Deprecated(since = "26.4", forRemoval = true)
    KeycloakSession getKeycloakSession();

    /** 创建已认证客户端会话。 */
    AuthenticatedClientSessionModel createClientSession(RealmModel realm, ClientModel client, UserSessionModel userSession);

    /**
     * @deprecated Use {@link #getClientSession(UserSessionModel, ClientModel, boolean)} instead.
     */
    @Deprecated(since = "26.4", forRemoval = true)
    default AuthenticatedClientSessionModel getClientSession(UserSessionModel userSession, ClientModel client, UUID clientSessionId, boolean offline) {
        return getClientSession(userSession, client, offline);
    }

    /**
     * @deprecated Use {@link #getClientSession(UserSessionModel, ClientModel, boolean)} instead.
     */
    @Deprecated(since = "26.4", forRemoval = true)
    default AuthenticatedClientSessionModel getClientSession(UserSessionModel userSession, ClientModel client, String clientSessionId, boolean offline) {
        return getClientSession(userSession, client, offline);
    }

    /**
     * 获取用户会话与客户端对应的已认证客户端会话。
     * Gets the authenticated client session for a given user session and client.
     *
     * @param userSession The user's session model.
     * @param client      The client model.
     * @param offline     If {@code true}, retrieves the offline session; otherwise, retrieves the online session.
     * @return The authenticated client session, or {@code null} if it doesn't exist.
     */
    AuthenticatedClientSessionModel getClientSession(UserSessionModel userSession, ClientModel client, boolean offline);

    /**
     * @deprecated Use {@link #createUserSession(String, RealmModel, UserModel, String, String, String, boolean, String, String, UserSessionModel.SessionPersistenceState)} instead.
     */
    @Deprecated(forRemoval = true)
    default UserSessionModel createUserSession(RealmModel realm, UserModel user, String loginUsername, String ipAddress, String authMethod, boolean rememberMe, String brokerSessionId, String brokerUserId) {
        return createUserSession(null, realm, user, loginUsername, ipAddress, authMethod, rememberMe, brokerSessionId,
                brokerUserId, UserSessionModel.SessionPersistenceState.PERSISTENT);
    }

    /**
     * 使用给定参数创建新用户会话。
     * Creates a new user session with the given parameters.
     *
     * @param id identifier. Is generated if {@code null}
     * @param realm the realm
     * @param user user associated with the created user session
     * @return Model of the created user session
     */
    UserSessionModel createUserSession(String id, RealmModel realm, UserModel user, String loginUsername, String ipAddress,
                                       String authMethod, boolean rememberMe, String brokerSessionId, String brokerUserId, UserSessionModel.SessionPersistenceState persistenceState);

    /** 按 ID 获取用户会话。 */
    UserSessionModel getUserSession(RealmModel realm, String id);

    /**
     * 获取指定用户的在线会话流。
     * Obtains the online user sessions associated with the specified user.
     *
     * @param realm a reference to the realm.
     * @param user the user whose sessions are being searched.
     * @return a non-null {@link Stream} of online user sessions.
     */
    Stream<UserSessionModel> getUserSessionsStream(RealmModel realm, UserModel user);

    /**
     * 获取指定客户端的在线会话流（已弃用）。
     * Obtains the online user sessions associated with the specified client.
     *
     * @param realm  a reference to the realm.
     * @param client the client whose user sessions are being searched.
     * @return a non-null {@link Stream} of online user sessions.
     * @see #readOnlyStreamUserSessions(RealmModel, ClientModel, int, int)
     * @deprecated use {@link #readOnlyStreamUserSessions(RealmModel, ClientModel, int, int)} instead.
     */
    @Deprecated(since = "26.6", forRemoval = true)
    default Stream<UserSessionModel> getUserSessionsStream(RealmModel realm, ClientModel client) {
        return Stream.empty();
    }

    /**
     * 分页获取指定客户端的在线会话流（已弃用）。
     * Obtains the online user sessions associated with the specified client, starting from the {@code firstResult} and
     * containing at most {@code maxResults}.
     *
     * @param realm       a reference to the realm.
     * @param client      the client whose user sessions are being searched.
     * @param firstResult first result to return. Ignored if negative or {@code null}.
     * @param maxResults  maximum number of results to return. Ignored if negative or {@code null}.
     * @return a non-null {@link Stream} of online user sessions.
     * @see #readOnlyStreamUserSessions(RealmModel, ClientModel, int, int)
     * @deprecated use {@link #readOnlyStreamUserSessions(RealmModel, ClientModel, int, int)} instead.
     */
    @Deprecated(since = "26.6", forRemoval = true)
    default Stream<UserSessionModel> getUserSessionsStream(RealmModel realm, ClientModel client, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    /**
     * 按 broker 用户 ID 获取在线会话流。
     * Obtains the online user sessions associated with the user that matches the specified {@code brokerUserId}.
     *
     * @param realm a reference to the realm.
     * @param brokerUserId the id of the broker user whose sessions are being searched.
     * @return a non-null {@link Stream} of online user sessions.
     */
    Stream<UserSessionModel> getUserSessionByBrokerUserIdStream(RealmModel realm, String brokerUserId);

    /** 按 broker 会话 ID 获取用户会话。 */
    UserSessionModel getUserSessionByBrokerSessionId(RealmModel realm, String brokerSessionId);

    /**
     * 仅当谓词通过时返回指定 ID 的用户会话。
     * Return userSession of specified ID as long as the predicate passes. Otherwise, returns {@code null}.
     * If predicate doesn't pass, implementation can do some best-effort actions to try to have predicate passing (e.g. download userSession from other DC)
     */
    UserSessionModel getUserSessionWithPredicate(RealmModel realm, String id, boolean offline, Predicate<UserSessionModel> predicate);

    /** 返回客户端活跃在线会话数。 */
    long getActiveUserSessions(RealmModel realm, ClientModel client);

    /**
     * 返回客户端会话统计摘要，键为 client.getId()。
     * Returns a summary of client sessions key is client.getId()
     */
    Map<String, Long> getActiveClientSessionStats(RealmModel realm, boolean offline);

    /** 同时移除关联的 ClientLoginSessionModels */
    /** 移除单个用户会话。 */
    void removeUserSession(RealmModel realm, UserSessionModel session);
    /** 移除用户的全部会话。 */
    void removeUserSessions(RealmModel realm, UserModel user);

    /**
     * 移除所有 realm 的过期会话（已弃用）。
     * Remove expired user sessions and client sessions in all the realms
     *
     * @deprecated to be removed without replacement. The providers are responsible for purging the expired entries
     * themselves.
     */
    @Deprecated(since = "26.5", forRemoval = true)
    default void removeAllExpired() {
    }

    /**
     * 从本 Provider 移除指定 realm 的过期用户会话（已弃用）。
     * Removes expired user sessions owned by this realm from this provider. If this `UserSessionProvider` uses
     * `UserSessionPersister`, the removal of the expired {@link UserSessionModel user sessions} is also propagated to
     * relevant `UserSessionPersister`.
     *
     * @param realm {@link RealmModel} Realm where all the expired user sessions to be removed from.
     * @deprecated to be removed without replacement. The providers are responsible for purging the expired entries
     * themselves.
     */
    @Deprecated(since = "26.5", forRemoval = true)
    default void removeExpired(RealmModel realm) {
    }

    /**
     * 移除指定 realm 的全部用户会话（含离线）。
     * Removes all user sessions (regular and offline) from the specified realm.
     *
     * @param realm the realm whose sessions are to be removed.
     */
    void removeUserSessions(RealmModel realm);

    /**
     * realm 删除时回调，应清理关联会话。
     * Callback method invoked when a realm is removed. Implementations should clear any sessions associated with the removed
     * realm.
     *
     * @param realm a reference to the realm being removed.
     */
    void onRealmRemoved(RealmModel realm);

    /**
     * 客户端删除时回调，应清理关联会话。
     * Callback method invoked when a client is removed. Implementations should clear any sessions associated with the
     * removed client.
     *
     * @param realm a reference to the realm.
     * @param client a reference to the client being removed.
     */
    void onClientRemoved(RealmModel realm, ClientModel client);

    /** 新创建的离线用户会话不包含已附加的 AuthenticatedClientSessions */
    /** 基于在线会话创建离线用户会话。 */
    UserSessionModel createOfflineUserSession(UserSessionModel userSession);
    /** 获取离线用户会话。 */
    UserSessionModel getOfflineUserSession(RealmModel realm, String userSessionId);

    /** 同时移除关联的 clientSessions */
    /** 移除离线用户会话。 */
    void removeOfflineUserSession(RealmModel realm, UserSessionModel userSession);

    /** 新创建的离线客户端会话将自动附加到 offlineUserSession */
    AuthenticatedClientSessionModel createOfflineClientSession(AuthenticatedClientSessionModel clientSession, UserSessionModel offlineUserSession);

    /**
     * 获取指定用户的离线会话流。
     * Obtains the offline user sessions associated with the specified user.
     *
     * @param realm a reference to the realm.
     * @param user the user whose offline sessions are being searched.
     * @return a non-null {@link Stream} of offline user sessions.
     */
    Stream<UserSessionModel> getOfflineUserSessionsStream(RealmModel realm, UserModel user);

    /**
     * 按 broker 用户 ID 获取离线会话流。
     * Obtains the offline user sessions associated with the user that matches the specified {@code brokerUserId}.
     *
     * @param realm a reference to the realm.
     * @param brokerUserId the id of the broker user whose sessions are being searched.
     * @return a non-null {@link Stream} of offline user sessions.
     */
    Stream<UserSessionModel> getOfflineUserSessionByBrokerUserIdStream(RealmModel realm, String brokerUserId);

    /** 返回客户端离线会话数。 */
    long getOfflineSessionsCount(RealmModel realm, ClientModel client);

    /**
     * 分页获取指定客户端的离线会话流（已弃用）。
     * Obtains the offline user sessions associated with the specified client, starting from the {@code firstResult} and
     * containing at most {@code maxResults}.
     *
     * @param realm       a reference to the realm.
     * @param client      the client whose user sessions are being searched.
     * @param firstResult first result to return. Ignored if negative or {@code null}.
     * @param maxResults  maximum number of results to return. Ignored if negative or {@code null}.
     * @return a non-null {@link Stream} of offline user sessions.
     * @see #readOnlyStreamOfflineUserSessions(RealmModel, ClientModel, int, int)
     * @deprecated use {@link #readOnlyStreamOfflineUserSessions(RealmModel, ClientModel, int, int)} instead.
     */
    @Deprecated(since = "26.6", forRemoval = true)
    default Stream<UserSessionModel> getOfflineUserSessionsStream(RealmModel realm, ClientModel client, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    /** 持久化层预加载时触发，同时导入 authenticatedClientSessions（已弃用）。
     *
     * @deprecated Deprecated as offline session preloading was removed in KC25. This method will be removed in KC27.
     */
    @Deprecated(since = "26.4", forRemoval = true)
    default void importUserSessions(Collection<UserSessionModel> persistentUserSessions, boolean offline) {}

    /** 关闭 Provider。 */
    void close();

    /** 返回 Provider 启动时间。 */
    int getStartupTime(RealmModel realm);

    default void migrate(String modelVersion) {
    }

    /**
     * 若用户会话存在且包含指定客户端会话则返回该 {@link UserSessionModel}。
     * Returns the {@link UserSessionModel} if the user session with ID {@code userSessionId} exist, and it has an
     * {@link AuthenticatedClientSessionModel} from a {@link ClientModel} with ID {@code clientUUID}.
     * <p>
     * If the {@link AuthenticatedClientSessionModel} from the client or the {@link UserSessionModel} does not exist,
     * this method returns {@code null}.
     *
     * @param realm         The {@link RealmModel} where the session belongs to.
     * @param userSessionId The ID of the {@link UserSessionModel}.
     * @param offline       If {@code true}, it fetches an offline session and, if {@code false}, an online session.
     * @param clientUUID    The {@link ClientModel#getId()}.
     * @return The {@link UserSessionModel} if it has a session from the {@code clientUUID}.
     */
    default UserSessionModel getUserSessionIfClientExists(RealmModel realm, String userSessionId, boolean offline, String clientUUID) {
        return getUserSessionWithPredicate(realm, userSessionId, offline, userSession -> userSession.getAuthenticatedClientSessionByClient(clientUUID) != null);
    }

    /**
     * 流式返回 realm 内全部常规会话（只读）。
     * Stream all the regular sessions in the realm.
     * <p>
     * The returned {@link UserSessionModel} instances are immutable. More precisely, the entity is not tracked by the transaction and any
     * modification may throw an {@link UnsupportedOperationException}.
     *
     * @param realm The {@link RealmModel} instance.
     * @return A {@link Stream} for all the sessions in the realm.
     */
    default Stream<UserSessionModel> readOnlyStreamUserSessions(RealmModel realm) {
        return getActiveClientSessionStats(realm, false)
                .keySet()
                .stream()
                .map(realm::getClientById)
                .flatMap((client) -> readOnlyStreamUserSessions(realm, client, -1, -1));
    }

    /**
     * 流式返回 realm 内全部离线会话（只读）。
     * Stream all the offline sessions in the realm.
     * <p>
     * The returned {@link UserSessionModel} instances are immutable. More precisely, the entity is not tracked by the transaction and any
     * modification may throw an {@link UnsupportedOperationException}.
     *
     * @param realm The {@link RealmModel} instance.
     * @return A {@link Stream} for all the sessions in the realm.
     */
    default Stream<UserSessionModel> readOnlyStreamOfflineUserSessions(RealmModel realm) {
        return getActiveClientSessionStats(realm, true)
                .keySet()
                .stream()
                .map(realm::getClientById)
                .flatMap((client) -> readOnlyStreamOfflineUserSessions(realm, client, -1, -1));
    }

    /**
     * 流式返回 realm 内属于指定客户端的常规会话（只读）。
     * Stream all the regular sessions belonging to the realm and having a client session from the client.
     * <p>
     * The returned {@link UserSessionModel} instances are immutable. More precisely, the entity is not tracked by the
     * transaction and any modification may throw an {@link UnsupportedOperationException}.
     * <p>
     * The {@code skip} and {@code maxResults} parameters control how many sessions should be streamed. A negative value
     * for either parameter is ignored (no skip/limit applied). If {@code maxResults} is zero, an empty stream is
     * returned.
     *
     * @param realm      The {@link RealmModel} instance.
     * @param client     The {@link ClientModel} instance.
     * @param skip       The number of leading elements to skip.
     * @param maxResults The number of elements the stream should be limited to.
     * @return A {@link Stream} for all the sessions matching the parameters.
     */
    default Stream<UserSessionModel> readOnlyStreamUserSessions(RealmModel realm, ClientModel client, int skip, int maxResults) {
        return getUserSessionsStream(realm, client, skip, maxResults);
    }

    /**
     * 流式返回 realm 内属于指定客户端的离线会话（只读）。
     * Stream all the offline sessions belonging to the realm and having a client session from the client.
     * <p>
     * The returned {@link UserSessionModel} instances are immutable. More precisely, the entity is not tracked by the
     * transaction and any modification may throw an {@link UnsupportedOperationException}.
     * <p>
     * The {@code skip} and {@code maxResults} parameters control how many sessions should be streamed. A negative value
     * for either parameter is ignored (no skip/limit applied). If {@code maxResults} is zero, an empty stream is
     * returned.
     *
     * @param realm      The {@link RealmModel} instance.
     * @param client     The {@link ClientModel} instance.
     * @param skip       The number of leading elements to skip.
     * @param maxResults The number of elements the stream should be limited to.
     * @return A {@link Stream} for all the sessions matching the parameters.
     */
    default Stream<UserSessionModel> readOnlyStreamOfflineUserSessions(RealmModel realm, ClientModel client, int skip, int maxResults) {
        return getOfflineUserSessionsStream(realm, client, skip, maxResults);
    }
}
