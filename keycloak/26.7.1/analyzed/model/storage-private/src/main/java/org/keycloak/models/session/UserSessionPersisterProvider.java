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

package org.keycloak.models.session;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.provider.Provider;

/**
 * 用户会话持久化 Provider：负责将在线/离线用户会话及其客户端会话写入持久存储，并在启动时恢复。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface UserSessionPersisterProvider extends Provider {

    /** 持久化用户会话（不包含其客户端会话）。 */
    void createUserSession(UserSessionModel userSession, boolean offline);

    /** 持久化客户端会话（假定对应的用户会话已持久化）。 */
    void createClientSession(AuthenticatedClientSessionModel clientSession, boolean offline);

    /** 登出（在线会话）或周期性过期清理时调用；同时移除所有关联的客户端会话。 */
    void removeUserSession(String userSessionId, boolean offline);

    /** 撤销时调用；若这是用户会话上最后一个客户端会话，则一并移除用户会话。 */
    void removeClientSession(String userSessionId, String clientUUID, boolean offline);

    /** 领域删除时的回调，清理该领域下的持久化会话数据。 */
    void onRealmRemoved(RealmModel realm);
    /** 客户端删除时的回调，清理该客户端相关的持久化会话数据。 */
    void onClientRemoved(RealmModel realm, ClientModel client);
    /** 用户删除时的回调，清理该用户相关的持久化会话数据。 */
    void onUserRemoved(RealmModel realm, UserModel user);

    /** 批量更新指定用户会话的 lastSessionRefresh 为给定值。 */
    void updateLastSessionRefreshes(RealmModel realm, int lastSessionRefresh, Collection<String> userSessionIds, boolean offline);

    /** 移除已过期的用户会话与客户端会话。 */
    void removeExpired(RealmModel realm);

    /**
     * 按 userSessionId 加载用户会话。
     *
     * @param userSessionId 用户会话 ID
     * @param offline 是否加载离线会话
     * @return 用户会话模型，不存在时返回 {@code null}
     */
    UserSessionModel loadUserSession(RealmModel realm, String userSessionId, boolean offline);

    /**
     * 加载指定 {@link UserModel} 在 {@link RealmModel} 中的用户会话流。
     *
     * @param realm 领域
     * @param user 用户
     * @param offline 是否加载离线会话
     * @param firstResult 起始偏移
     * @param maxResults 最大返回数量
     * @return 用户会话流
     */
    Stream<UserSessionModel> loadUserSessionsStream(RealmModel realm, UserModel user, boolean offline, Integer firstResult, Integer maxResults);

    /**
     * 返回指定 {@link UserModel} 的用户会话 ID 及其关联客户端 UUID 集合，不应用 {@code lastSessionRefresh} 空闲超时过滤。
     *
     * @param realm   The {@link RealmModel}.
     * @param user    The {@link UserModel} whose sessions are queried.
     * @param offline If {@code true}, returns offline sessions, otherwise online sessions.
     * @return A map of user-session id to the set of associated client UUIDs (the set may be empty).
     */
    default Map<String, Set<String>> findUserSessionsByUserId(RealmModel realm, UserModel user, boolean offline) {
        throw new IllegalStateException("not implemented");
    }

    /**
     * 加载指定 {@link ClientModel} 在 {@link RealmModel} 中的用户会话流。
     *
     * @param realm 领域
     * @param client 客户端
     * @param offline 是否加载离线会话
     * @param firstResult 起始偏移
     * @param maxResults 最大返回数量
     * @return 用户会话流
     */
    Stream<UserSessionModel> loadUserSessionsStream(RealmModel realm, ClientModel client, boolean offline, Integer firstResult, Integer maxResults);

    /** 按 Broker 会话 ID 加载用户会话（默认未实现）。 */
    default UserSessionModel loadUserSessionsStreamByBrokerSessionId(RealmModel realm, String brokerSessionId, boolean offline) {
        throw new IllegalStateException("not implemented");
    }

    /**
     * 启动时调用：加载用户会话，并为每个用户会话一并加载其客户端会话。
     *
     * @param firstResult {@code Integer} Index of the first desired user session. Ignored if negative or {@code null}.
     * @param maxResults {@code Integer} Maximum number of returned user sessions. Ignored if negative or {@code null}.
     * @param offline {@code boolean} Flag to include offline sessions.
     * @param lastUserSessionId {@code String} Id of the user session. It will return only user sessions with id's lexicographically greater than this.
     * it will compare the id in dictionary order and takes only those created later.
     * @return Stream of {@link UserSessionModel}. Never returns {@code null}.
     */
    Stream<UserSessionModel> loadUserSessionsStream(Integer firstResult, Integer maxResults, boolean offline,
                                                    String lastUserSessionId);

    /**
     * 按用户会话与客户端从数据库加载客户端会话。
     *
     * @param realm RealmModel Realm for the associated client session.
     * @param client ClientModel Client used for the creation of client session.
     * @param userSession UserSessionModel User session for the associated client session.
     * @param offline boolean Flag that indicates the client session should be online/offline.
     * @return Client session according the provided criteria or {@code null} if not found.
     */
    AuthenticatedClientSessionModel loadClientSession(RealmModel realm, ClientModel client, UserSessionModel userSession, boolean offline);

    /**
     * 返回所有领域的用户会话总数。
     *
     * @param offline 是否统计离线会话
     * @return 会话数量
     */
    int getUserSessionsCount(boolean offline);

    /**
     * 返回指定客户端的用户客户端会话数量。
     *
     * @param realm 领域
     * @param clientModel 客户端
     * @param offline 是否统计离线会话
     * @return 会话数量
     */
    int getUserSessionsCount(RealmModel realm, ClientModel clientModel, boolean offline);

    /**
     * 返回指定领域中按客户端 ID 聚合的用户会话数量映射。
     *
     * @param realm 领域
     * @param offline 是否统计离线会话
     * @return the count {@link Map} with clientId as key and session count as value
     */
    Map<String, Long> getUserSessionsCountsByClients(RealmModel realm, boolean offline);

    /**
     * 移除该领域的在线或离线用户会话。
     */
    default void removeUserSessions(RealmModel realm, boolean offline) {
        throw new IllegalArgumentException("not supported");
        // TODO: remove default implementation
    }

    /**
     * 移除指定领域的所有用户会话（在线与离线）。
     */
    default void removeUserSessions(RealmModel realm) {
        removeUserSessions(realm, true);
        removeUserSessions(realm, false);
    }

    /**
     * 流式返回属于该领域的所有会话。
     * <p>
     * 返回的 {@link UserSessionModel} 实例为只读；实体不受 JPA 跟踪，任何修改可能抛出 {@link UnsupportedOperationException}。
     *
     * @param realm   The {@link RealmModel} instance.
     * @param offline If {@code true}, it streams the offline sessions, otherwise the regular sessions.
     * @return A {@link Stream} for all the sessions in the realm.
     */
    Stream<UserSessionModel> readOnlyUserSessionStream(RealmModel realm, boolean offline);

    /**
     * 流式返回属于该领域且包含指定客户端会话的所有用户会话。
     * <p>
     * 返回的 {@link UserSessionModel} 实例为只读；实体不受 JPA 跟踪，任何修改可能抛出 {@link UnsupportedOperationException}。
     * <p>
     * {@code skip} 与 {@code maxResults} 控制流式返回的数量；负值表示忽略对应限制。若 {@code maxResults} 为 0，返回空流。
     *
     * @param realm      The {@link RealmModel} instance.
     * @param client     The {@link ClientModel} instance.
     * @param offline    If {@code true}, it streams the offline sessions, otherwise the regular sessions.
     * @param skip       The number of leading elements to skip.
     * @param maxResults The number of elements the stream should be limited to.
     * @return A {@link Stream} for all the sessions matching the parameters.
     */
    Stream<UserSessionModel> readOnlyUserSessionStream(RealmModel realm, ClientModel client, boolean offline, int skip, int maxResults);

    /**
     * 锁定即将更新的实体。
     *
     * @return 返回 {@code true} 表示无需锁定或已全部锁定，事务回滚的可能性较低
     */
    default boolean lockUserSession(RealmModel realm, String userSessionId, boolean offline, boolean isRemove) {
        return false;
    }

    /**
     * 锁定即将更新的客户端会话实体。
     *
     * @return 返回 {@code true} 表示无需锁定或已全部锁定，事务回滚的可能性较低
     */
    default boolean lockClientSession(RealmModel realm, String userSessionId, String clientId, boolean offline, boolean isRemove) {
        return false;
    }

}
