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
import java.util.Objects;

import org.keycloak.util.EnumWithStableIndex;

import org.infinispan.protostream.annotations.Proto;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 用户会话模型：表示已登录用户的在线/离线会话状态与关联客户端会话。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface UserSessionModel {

    /**
     * 对应在线/离线用户会话的 note 键名。
     * Represents the corresponding online/offline user session.
     */
    String CORRESPONDING_SESSION_ID = "correspondingSessionId";

    /** @return 会话 ID */
    String getId();
    /** @return 所属 realm */
    RealmModel getRealm();

    /**
     * 若通过 broker 外部登录创建，可用于匹配后端通道登出请求的标识符。
     * If created via a broker external login, this is an identifier that can be
     * used to match external broker backchannel logout requests to a UserSession
     *
     * @return
     */
    String getBrokerSessionId();
    /** @return broker 用户 ID */
    String getBrokerUserId();

    /** @return 关联用户 */
    UserModel getUser();

    /** @return 登录用户名 */
    String getLoginUsername();

    /**
     * 注意：代理未提供有效地址时可能不是真实 IP。
     * Note: will not be an address when a proxy does not provide a valid one
     *
     * @return the ip address
     */
    String getIpAddress();

    /** @return 认证方法 */
    String getAuthMethod();

    /** @return 是否记住我 */
    boolean isRememberMe();

    /** @return 会话开始时间（秒） */
    int getStarted();

    /** @return 最后刷新时间（秒） */
    int getLastSessionRefresh();

    /**
     * 设置用户会话最后刷新时间戳；若小于等于当前值则忽略。
     * Set the last session refresh timestamp for the user session.
     * If the timestamp is smaller or equal than the current timestamp, the operation is ignored.
     */
    void setLastSessionRefresh(int seconds);

    /** @return 是否为离线会话 */
    boolean isOffline();

    /**
     * 返回客户端 UUID 到 {@link AuthenticatedClientSessionModel} 的映射（不可直接修改 Map）。
     * Returns map where key is ID of the client (its UUID) and value is ID respective {@link AuthenticatedClientSessionModel} object.
     * <p>
     * Any direct modification via the {@link Map} interface will throw an {@link UnsupportedOperationException}. To add a
     * new mapping, use a method like {@link UserSessionProvider#createClientSession(RealmModel, ClientModel, UserSessionModel)} or
     * equivalent. To remove a mapping, use {@link AuthenticatedClientSessionModel#detachFromUserSession()}.
     */
    Map<String, AuthenticatedClientSessionModel> getAuthenticatedClientSessions();
    /**
     * 返回指定客户端 UUID 的客户端会话。
     * Returns a client session for the given client UUID.
     * @return
     */
    default AuthenticatedClientSessionModel getAuthenticatedClientSessionByClient(String clientUUID) {
        return getAuthenticatedClientSessions().get(clientUUID);
    }

    /**
     * 移除 {@code removedClientUUIDS} 中列出的客户端会话。
     * Removes authenticated client sessions for all clients whose UUID is present in {@code removedClientUUIDS} parameter.
     * @param removedClientUUIDS
     */
    void removeAuthenticatedClientSessions(Collection<String> removedClientUUIDS);


    /** @param name note 键
     * @return note 值 */
    String getNote(String name);
    /** @param name note 键
     * @param value note 值 */
    void setNote(String name, String value);
    /** @param name note 键 */
    void removeNote(String name);
    /** @return 全部 session notes */
    Map<String, String> getNotes();

    /** @return 会话状态 */
    State getState();
    /** @param state 会话状态 */
    void setState(State state);

    // 完全重置用户会话状态，仅保留相同 ID
    // Will completely restart whole state of user session. It will just keep same ID.
    void restartSession(RealmModel realm, UserModel user, String loginUsername, String ipAddress, String authMethod, boolean rememberMe, String brokerSessionId, String brokerUserId);

    @ProtoTypeId(65536) // see org.keycloak.Marshalling
    @Proto
    /** 用户会话生命周期状态。 */
    enum State implements EnumWithStableIndex {
        /** 已登录 */ LOGGED_IN(0),
        /** 登出中 */ LOGGING_OUT(1),
        /** 已登出 */ LOGGED_OUT(2),
        /** 已登出（未确认） */ LOGGED_OUT_UNCONFIRMED(3);

        private final int stableIndex;
        private static final Map<Integer, State> BY_ID = EnumWithStableIndex.getReverseIndex(values());

        private State(int stableIndex) {
            Objects.requireNonNull(stableIndex);
            this.stableIndex = stableIndex;
        }

        @Override
        public int getStableIndex() {
            return stableIndex;
        }

        public static State valueOfInteger(Integer id) {
            return id == null ? null : BY_ID.get(id);
        }
    }

    /**
     * 返回会话持久化状态。
     * @return Persistence state of the session
     */
    default SessionPersistenceState getPersistenceState() {
        return SessionPersistenceState.PERSISTENT;
    }

    /**
     * 创建用户会话时使用的持久化标志。
     * Flag used when creating user session
     */
    enum SessionPersistenceState {

        /**
         * Session will be marked as persistent when created and it will be saved into the persistent storage (EG. infinispan cache).
         * This is the default behaviour
         */
        PERSISTENT,

        /**
         *  This userSession will be valid just for the single request. Hence there won't be real
         *  userSession created in the persistent store. Flag can be used for the protocols, which need just "dummy"
         *  userSession to be able to run protocolMappers SPI. Example is DockerProtocol or OAuth2 client credentials grant.
         */
        TRANSIENT;

        public static SessionPersistenceState fromString(String sessionPersistenceString) {
            return (sessionPersistenceString == null) ? PERSISTENT : Enum.valueOf(SessionPersistenceState.class, sessionPersistenceString);
        }
    }

}
