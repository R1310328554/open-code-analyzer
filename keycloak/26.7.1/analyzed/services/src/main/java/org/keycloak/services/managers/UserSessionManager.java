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
package org.keycloak.services.managers;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.common.util.Time;
import org.keycloak.device.DeviceActivityManager;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.services.ServicesLogger;

import org.jboss.logging.Logger;


/**
 * 用户会话管理器。
 * <p>管理在线/离线用户会话的创建、离线令牌生命周期及 offline_access 角色校验。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserSessionManager {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(UserSessionManager.class);

    /** Keycloak 会话 */
    private final KeycloakSession kcSession;

    /** @param session Keycloak 会话 */
    public UserSessionManager(KeycloakSession session) {
        this.kcSession = session;
    }

    /** 创建或更新离线用户会话及关联的离线客户端会话。
     * @param clientSession 在线客户端会话
     * @param userSession 在线用户会话
     */
    public void createOrUpdateOfflineSession(AuthenticatedClientSessionModel clientSession, UserSessionModel userSession) {
        UserModel user = userSession.getUser();

        // 若无离线用户会话则创建并持久化
        UserSessionModel offlineUserSession = kcSession.sessions().getOfflineUserSession(clientSession.getRealm(), userSession.getId());
        if (offlineUserSession == null) {
            offlineUserSession = createOfflineUserSession(user, userSession);
        } else {
            // 更新 lastSessionRefresh，无需额外持久化
            offlineUserSession.setLastSessionRefresh(Time.currentTime());
        }

        // 创建并持久化离线客户端会话
        AuthenticatedClientSessionModel offlineClientSession = offlineUserSession.getAuthenticatedClientSessionByClient(clientSession.getClient().getId());
        if (offlineClientSession == null) {
            offlineClientSession = createOfflineClientSession(user, clientSession, offlineUserSession);
            offlineClientSession.removeNote(AuthenticationProcessor.FIRST_OFFLINE_ACCESS);
        }
    }


    public UserSessionModel findOfflineUserSession(RealmModel realm, String userSessionId) {
        return kcSession.sessions().getOfflineUserSession(realm, userSessionId);
    }

    public Set<ClientModel> findClientsWithOfflineToken(RealmModel realm, UserModel user) {
        return kcSession.sessions().getOfflineUserSessionsStream(realm, user)
                .flatMap(userSession -> userSession.getAuthenticatedClientSessions().keySet().stream())
                .map(clientUUID -> realm.getClientById(clientUUID))
                .collect(Collectors.toSet());
    }

    @Deprecated
    public List<UserSessionModel> findOfflineSessions(RealmModel realm, UserModel user) {
        return this.findOfflineSessionsStream(realm, user).collect(Collectors.toList());
    }

    public Stream<UserSessionModel> findOfflineSessionsStream(RealmModel realm, UserModel user) {
        return kcSession.sessions().getOfflineUserSessionsStream(realm, user);
    }

    /** 撤销用户对指定客户端的全部离线令牌。
     * @param user 用户
     * @param client 客户端
     * @return 是否移除了至少一个离线客户端会话
     */
    public boolean revokeOfflineToken(UserModel user, ClientModel client) {
        RealmModel realm = client.getRealm();

        AtomicBoolean anyRemoved = new AtomicBoolean(false);
        kcSession.sessions().getOfflineUserSessionsStream(realm, user).collect(Collectors.toList())
                .forEach(userSession -> {
                    if (removeClientFromOfflineUserSession(realm, userSession, client, user)) {
                        anyRemoved.set(true);
                    }
                });

        return anyRemoved.get();
    }

    public boolean removeClientFromOfflineUserSession(RealmModel realm, UserSessionModel userSession, ClientModel client, UserModel user) {
        AuthenticatedClientSessionModel clientSession = userSession.getAuthenticatedClientSessionByClient(client.getId());
        if (clientSession != null) {
            if (logger.isTraceEnabled()) {
                logger.tracef("Removing existing offline token for user '%s' and client '%s' .",
                        user.getUsername(), client.getClientId());
            }

            clientSession.detachFromUserSession();
            checkOfflineUserSessionHasClientSessions(realm, user, userSession);
            return true;
        } else {
            return false;
        }
    }

    public void revokeOfflineUserSession(UserSessionModel userSession) {
        if (logger.isTraceEnabled()) {
            logger.tracef("Removing offline user session '%s' for user '%s' ", userSession.getId(), userSession.getLoginUsername());
        }
        kcSession.sessions().removeOfflineUserSession(userSession.getRealm(), userSession);
    }

    /** 校验客户端会话上下文是否包含 {@code offline_access} 角色（含复合角色）。
     * @param clientSessionCtx 客户端会话上下文
     * @return 是否允许离线令牌
     */
    public boolean isOfflineTokenAllowed(ClientSessionContext clientSessionCtx) {
        RoleModel offlineAccessRole = clientSessionCtx.getClientSession().getRealm().getRole(Constants.OFFLINE_ACCESS_ROLE);
        if (offlineAccessRole == null) {
            ServicesLogger.LOGGER.roleNotInRealm(Constants.OFFLINE_ACCESS_ROLE);
            return false;
        }

        // 检查是否授予 offline_access（含复合角色展开）
        return clientSessionCtx.getRolesStream().collect(Collectors.toSet()).contains(offlineAccessRole);
    }

    private UserSessionModel createOfflineUserSession(UserModel user, UserSessionModel userSession) {
        if (logger.isTraceEnabled()) {
            logger.tracef("Creating new offline user session. UserSessionID: '%s' , Username: '%s'", userSession.getId(), user.getUsername());
        }

        UserSessionModel offlineUserSession = kcSession.sessions().createOfflineUserSession(userSession);
        return offlineUserSession;
    }

    private AuthenticatedClientSessionModel createOfflineClientSession(UserModel user, AuthenticatedClientSessionModel clientSession, UserSessionModel offlineUserSession) {
        if (logger.isTraceEnabled()) {
            logger.tracef("Creating new offline token client session. ClientSessionId: '%s', UserSessionID: '%s' , Username: '%s', Client: '%s'" ,
                    clientSession.getId(), offlineUserSession.getId(), user.getUsername(), clientSession.getClient().getClientId());
        }

        return kcSession.sessions().createOfflineClientSession(clientSession, offlineUserSession);
    }

    // 若无任何离线客户端会话挂载则移除离线用户会话
    private void checkOfflineUserSessionHasClientSessions(RealmModel realm, UserModel user, UserSessionModel userSession) {
        // TODO：可优化以避免从缓存加载客户端会话
        if (! userSession.getAuthenticatedClientSessions().isEmpty()) {
            return;
        }

        if (logger.isTraceEnabled()) {
            logger.tracef("Removing offline userSession for user %s as it doesn't have any client sessions attached. UserSessionID: %s", user.getUsername(), userSession.getId());
        }
        kcSession.sessions().removeOfflineUserSession(realm, userSession);
    }

    /** 创建持久化在线用户会话并附加设备信息。
     * @param realm 领域
     * @param user 用户
     * @param loginUsername 登录用户名
     * @param ipAddress 客户端 IP
     * @param authMethod 认证方式
     * @param rememberMe 是否记住登录
     * @param brokerSessionId IdP 会话 ID
     * @param brokerUserId IdP 用户 ID
     * @return 新用户会话
     */
    public UserSessionModel createUserSession(RealmModel realm, UserModel user, String loginUsername, String ipAddress,
                                              String authMethod, boolean rememberMe, String brokerSessionId, String brokerUserId) {
        return createUserSession(null, realm, user, loginUsername, ipAddress, authMethod, rememberMe, brokerSessionId, brokerUserId, UserSessionModel.SessionPersistenceState.PERSISTENT);
    }

    public UserSessionModel createUserSession(String id, RealmModel realm, UserModel user, String loginUsername, String ipAddress,
                                              String authMethod, boolean rememberMe, String brokerSessionId, String brokerUserId,
                                              UserSessionModel.SessionPersistenceState persistenceState) {
        // 在存储中创建用户会话
        UserSessionModel userSession = kcSession.sessions().createUserSession(id, realm, user, loginUsername, ipAddress, authMethod, rememberMe, brokerSessionId, brokerUserId, persistenceState);

        // 将设备信息写入用户会话 note
        if (userSession != null) {
            DeviceActivityManager.attachDevice(userSession, kcSession);
        }

        return userSession;
    }
}
