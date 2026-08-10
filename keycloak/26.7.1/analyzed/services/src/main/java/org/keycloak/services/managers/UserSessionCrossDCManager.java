/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.UserSessionProvider;
import org.keycloak.services.util.UserSessionUtil;

/**
 * 跨数据中心用户会话管理器（已废弃）。
 * <p>原用于从远程缓存拉取用户会话；请改用各方法 JavaDoc 中的替代 API。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 *
 * @deprecated 将移除且无替代类，请参阅各方法的替代方案。
 */
@Deprecated(since = "26", forRemoval = true)
public class UserSessionCrossDCManager {

    /** Keycloak 会话 */
    private final KeycloakSession kcSession;

    /** @param session Keycloak 会话 */
    public UserSessionCrossDCManager(KeycloakSession session) {
        this.kcSession = session;
    }


    // 若用户会话已挂载指定客户端的 authenticatedClientSession 则直接返回，否则从 remoteCache 拉取（已废弃逻辑）

    /**
     * 获取挂载指定客户端会话的用户会话（含 offline 标志）。
     * @deprecated Keycloak 27+ 将移除，请使用
     * {@link UserSessionProvider#getUserSessionIfClientExists(RealmModel, String, boolean, String)}
     */
    @Deprecated(since = "26", forRemoval = true)
    public UserSessionModel getUserSessionWithClient(RealmModel realm, String id, boolean offline, String clientUUID) {
        return kcSession.sessions().getUserSessionIfClientExists(realm, id, offline, clientUUID);
    }

    /**
     * 获取含模拟客户端会话的用户会话。
     * @deprecated Keycloak 27+ 将移除，请使用
     * {@link UserSessionUtil#getUserSessionWithImpersonatorClient(KeycloakSession, RealmModel, String, boolean, String)}
     */
    @Deprecated(since = "26", forRemoval = true)
    public UserSessionModel getUserSessionWithImpersonatorClient(RealmModel realm, String id, boolean offline, String clientUUID) {
        return UserSessionUtil.getUserSessionWithImpersonatorClient(kcSession, realm, id, offline, clientUUID);
    }

    // 在线用户会话重载（无 offline 参数）；TODO：OAuth code JWT 迁移后可能移除
    /**
     * 获取挂载指定客户端会话的在线用户会话（默认非 offline）。
     * @deprecated Keycloak 27+ 将移除，请使用
     * {@link UserSessionProvider#getUserSessionIfClientExists(RealmModel, String, boolean, String)}
     */
    @Deprecated(since = "26", forRemoval = true)
    public UserSessionModel getUserSessionWithClient(RealmModel realm, String id, String clientUUID) {
        return getUserSessionWithClient(realm, id, false, clientUUID);
    }

    // 检查用户会话在远程缓存是否仍存在（跨 DC 登出通知延迟场景）
    /**
     * 从认证 Cookie 获取远程仍存在的用户会话。
     * @deprecated Keycloak 27+ 将移除，请使用
     * {@link AuthenticationSessionManager#getUserSessionFromAuthenticationCookie(RealmModel)}
     */
    @Deprecated(since = "26", forRemoval = true)
    public UserSessionModel getUserSessionIfExistsRemotely(AuthenticationSessionManager asm, RealmModel realm) {
        return asm.getUserSessionFromAuthenticationCookie(realm);
    }
}
