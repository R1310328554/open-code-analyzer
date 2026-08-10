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

package org.keycloak.models.sessions.infinispan.entities;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.common.util.Time;
import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.OfflineUserSessionModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoReserved;
import org.infinispan.protostream.annotations.ProtoTypeId;
import org.jboss.logging.Logger;

/**
 * 用户会话 Infinispan 实体，保存登录用户、客户端会话 ID 集合及刷新时间等。
 * <p>
 * 跨数据中心合并时通过 {@link #mergeRemoteEntityWithLocalEntity} 取较大的 {@code lastSessionRefresh}。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@ProtoTypeId(Marshalling.USER_SESSION_ENTITY)
@ProtoReserved(
        value = {11},
        names = {"authenticatedClientSessions"}
)
public class UserSessionEntity extends SessionEntity {

    public static final Logger logger = Logger.getLogger(UserSessionEntity.class);

    /** 元数据键：远程缓存上的 lastSessionRefresh，用于判断是否需要写回远程 DC。 */
    public static final String LAST_SESSION_REFRESH_REMOTE = "lsrr";

    /** 会话唯一 ID。 */
    private final String id;

    /** 关联用户 ID。 */
    private String user;

    /** 身份代理（broker）侧会话 ID。 */
    private String brokerSessionId;
    /** 身份代理侧用户 ID。 */
    private String brokerUserId;

    /** 登录时使用的用户名。 */
    private String loginUsername;

    /** 客户端 IP 地址。 */
    private String ipAddress;

    /** 认证方式（如 pwd、otp 等）。 */
    private String authMethod;

    /** 是否勾选“记住我”。 */
    private boolean rememberMe;

    /** 会话创建时间戳（秒）。 */
    private int started;

    /** 上次会话刷新时间戳（秒）。 */
    private int lastSessionRefresh;

    /** 会话状态（如 LOGGED_IN、LOGGING_OUT 等）。 */
    private UserSessionModel.State state;

    /** 已认证的客户端会话 ID 集合。 */
    private final Set<String> clientSessions = ConcurrentHashMap.newKeySet();

    /** 会话级 notes 键值对。 */
    private Map<String, String> notes = new ConcurrentHashMap<>();

    public UserSessionEntity(String id) {
        this.id = id;
    }

    /** Protostream 反序列化工厂方法。 */
    @ProtoFactory
    static UserSessionEntity protoFactory(String realmId, String id, String user, String loginUsername, String ipAddress, String authMethod, boolean rememberMe, int started, int lastSessionRefresh, Map<String, String> notes, UserSessionModel.State state, String brokerSessionId, String brokerUserId, Set<String> clientSessions) {
        var entity = new UserSessionEntity(id);
        entity.setRealmId(realmId);
        entity.setUser(user);
        entity.setLoginUsername(loginUsername);
        entity.setIpAddress(ipAddress);
        entity.setAuthMethod(authMethod);
        entity.setRememberMe(rememberMe);
        entity.setStarted(started);
        entity.setLastSessionRefresh(lastSessionRefresh);
        entity.setBrokerSessionId(brokerSessionId);
        entity.setBrokerUserId(brokerUserId);
        entity.setState(state);
        entity.setNotes(notes);
        entity.getClientSessions().addAll(clientSessions);
        return entity;
    }

    @ProtoField(2)
    public String getId() {
        return id;
    }

    @ProtoField(3)
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @ProtoField(4)
    public String getLoginUsername() {
        return loginUsername;
    }

    public void setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    @ProtoField(5)
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @ProtoField(6)
    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    @ProtoField(7)
    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @ProtoField(8)
    public int getStarted() {
        return started;
    }

    public void setStarted(int started) {
        this.started = started;
    }

    @ProtoField(9)
    public int getLastSessionRefresh() {
        return lastSessionRefresh;
    }

    public void setLastSessionRefresh(int lastSessionRefresh) {
        this.lastSessionRefresh = lastSessionRefresh;
    }

    @ProtoField(value = 10, mapImplementation = ConcurrentHashMap.class)
    public Map<String, String> getNotes() {
        return notes;
    }

    public void setNotes(Map<String, String> notes) {
        this.notes = notes;
    }

    @ProtoField(value = 12)
    public UserSessionModel.State getState() {
        return state;
    }

    public void setState(UserSessionModel.State state) {
        this.state = state;
    }

    @ProtoField(13)
    public String getBrokerSessionId() {
        return brokerSessionId;
    }

    public void setBrokerSessionId(String brokerSessionId) {
        this.brokerSessionId = brokerSessionId;
    }

    @ProtoField(14)
    public String getBrokerUserId() {
        return brokerUserId;
    }

    public void setBrokerUserId(String brokerUserId) {
        this.brokerUserId = brokerUserId;
    }

    @ProtoField(value = 15, collectionImplementation = HashSet.class)
    public Set<String> getClientSessions() {
        return clientSessions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof UserSessionEntity that &&
                Objects.equals(id, that.id);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("UserSessionEntity [id=%s, realm=%s, lastSessionRefresh=%d, clients=%s]", getId(), getRealmId(), getLastSessionRefresh(),
          clientSessions);
    }

    /** 将远程实体与本地副本合并，保留较大的 lastSessionRefresh 并记录远程刷新值。 */
    @Override
    public SessionEntityWrapper mergeRemoteEntityWithLocalEntity(SessionEntityWrapper localEntityWrapper) {
        int lsrRemote = getLastSessionRefresh();

        SessionEntityWrapper entityWrapper;
        if (localEntityWrapper == null) {
            entityWrapper = new SessionEntityWrapper<>(this);
        } else {
            UserSessionEntity localUserSession = (UserSessionEntity) localEntityWrapper.getEntity();

            // 本地 lastSessionRefresh 应始终取较大值
            if (lsrRemote < localUserSession.getLastSessionRefresh()) {
                setLastSessionRefresh(localUserSession.getLastSessionRefresh());
            }

            entityWrapper = new SessionEntityWrapper<>(localEntityWrapper.getLocalMetadata(), this);
        }

        entityWrapper.putLocalMetadataNoteInt(LAST_SESSION_REFRESH_REMOTE, lsrRemote);

        logger.debugf("Updating session entity '%s'. lastSessionRefresh=%d, lastSessionRefreshRemote=%d", getId(), getLastSessionRefresh(), lsrRemote);

        return entityWrapper;
    }

    /** 根据 realm/用户上下文创建新用户会话实体并初始化时间戳。 */
    public static UserSessionEntity create(String id, RealmModel realm, UserModel user, String loginUsername, String ipAddress, String authMethod, boolean rememberMe, String brokerSessionId, String brokerUserId) {
        UserSessionEntity entity = new UserSessionEntity(id);
        updateSessionEntity(entity, realm, user, loginUsername, ipAddress, authMethod, rememberMe, brokerSessionId, brokerUserId);
        return entity;
    }

    /** 用 realm/用户字段更新已有实体并重置 started/lastSessionRefresh。 */
    public static void updateSessionEntity(UserSessionEntity entity, RealmModel realm, UserModel user, String loginUsername, String ipAddress, String authMethod, boolean rememberMe, String brokerSessionId, String brokerUserId) {
        entity.setRealmId(realm.getId());
        entity.setUser(user.getId());
        entity.setLoginUsername(loginUsername);
        entity.setIpAddress(ipAddress);
        entity.setAuthMethod(authMethod);
        entity.setRememberMe(rememberMe);
        entity.setBrokerSessionId(brokerSessionId);
        entity.setBrokerUserId(brokerUserId);

        int currentTime = Time.currentTime();

        entity.setStarted(currentTime);
        entity.setLastSessionRefresh(currentTime);
    }

    /** 从 {@link UserSessionModel} 快照构建实体；离线会话导入时跳过 loginUsername（KEYCLOAK-5350）。 */
    public static UserSessionEntity createFromModel(UserSessionModel userSession) {
        UserSessionEntity entity = new UserSessionEntity(userSession.getId());
        entity.setRealmId(userSession.getRealm().getId());

        entity.setAuthMethod(userSession.getAuthMethod());
        entity.setBrokerSessionId(userSession.getBrokerSessionId());
        entity.setBrokerUserId(userSession.getBrokerUserId());
        entity.setIpAddress(userSession.getIpAddress());
        entity.setNotes(userSession.getNotes() == null ? new ConcurrentHashMap<>() : userSession.getNotes());
        entity.setRememberMe(userSession.isRememberMe());
        entity.setState(userSession.getState());
        if (userSession instanceof OfflineUserSessionModel offline) {
            // 离线 token 导入时 UserModel 可能不可用，直接使用 userId（KEYCLOAK-5350）
            entity.setUser(offline.getUserId());
            // 有意跳过 setLoginUsername
        } else {
            entity.setLoginUsername(userSession.getLoginUsername());
            entity.setUser(userSession.getUser().getId());
        }

        entity.setStarted(userSession.getStarted());
        entity.setLastSessionRefresh(userSession.getLastSessionRefresh());

        return entity;
    }
}
