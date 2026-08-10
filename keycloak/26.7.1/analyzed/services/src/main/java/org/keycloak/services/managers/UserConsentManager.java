/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.services.managers;

import java.util.stream.Stream;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserConsentModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.light.LightweightUserAdapter;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.LoginProtocolFactory;

import static org.keycloak.models.light.LightweightUserAdapter.isLightweightUser;

/**
 * 用户同意（Consent）管理工具。
 * <p>统一处理持久化用户与 {@link LightweightUserAdapter} 轻量用户的 consent CRUD 及撤销逻辑。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserConsentManager {

    /**
     * 撤销用户对指定客户端的同意，并尝试撤销离线令牌。
     *
     * @param session Keycloak 会话
     * @param client 客户端
     * @param user 用户
     * @return 是否撤销了 consent 或 offline token
     */
    public static boolean revokeConsentToClient(KeycloakSession session, ClientModel client, UserModel user) {
        RealmModel realm = session.getContext().getRealm();
        boolean revokedConsent = revokeConsentForClient(session, realm, user, client.getId());
        boolean revokedOfflineToken = new UserSessionManager(session).revokeOfflineToken(user, client);

        if (revokedConsent) {
            // 对该用户与该客户端执行 backchannel 登出
            AuthenticationManager.backchannelLogoutUserFromClient(session, realm, user, client, session.getContext().getUri(), session.getContext().getRequestHeaders());

            // 通知各登录协议工厂 consent 已撤销
            session.getKeycloakSessionFactory().getProviderFactoriesStream(LoginProtocol.class)
                    .map(LoginProtocolFactory.class::cast)
                    .forEach(loginProtocolFactory -> loginProtocolFactory.onConsentRevoked(session, client, user));
        }

        return revokedConsent || revokedOfflineToken;
    }

    /**
     * 为用户添加 consent 记录。
     *
     * @param session Keycloak 会话
     * @param realm 领域
     * @param user 用户，不可为 {@code null}
     * @param consent 同意详情
     * @throws ModelException 用户不存在
     */
    public static void addConsent(KeycloakSession session, RealmModel realm, UserModel user, UserConsentModel consent) {
        if (isLightweightUser(user)) {
            LightweightUserAdapter lua = (LightweightUserAdapter) user;
            lua.addConsent(consent);
        } else {
            session.users().addConsent(realm, user.getId(), consent);
        }
    }

    /**
     * 获取用户对指定客户端的内部 ID 所授予的 consent。
     *
     * @param session Keycloak 会话
     * @param realm 领域
     * @param user 用户，不可为 {@code null}
     * @param clientInternalId 客户端内部 ID
     * @return 用户 consent，不存在时返回 {@code null}
     * @throws ModelException 存在多条匹配 consent
     */
    public static UserConsentModel getConsentByClient(KeycloakSession session, RealmModel realm, UserModel user, String clientInternalId) {
        if (isLightweightUser(user)) {
            LightweightUserAdapter lua = (LightweightUserAdapter) user;
            return lua.getConsentByClient(clientInternalId);
        } else {
            return session.users().getConsentByClient(realm, user.getId(), clientInternalId);
        }
    }

    /**
     * 获取用户全部 consent 流。
     *
     * @param session Keycloak 会话
     * @param realm 领域
     * @param user 用户，不可为 {@code null}
     * @return 非 null 的 consent {@link Stream}
     */
    public static Stream<UserConsentModel> getConsentsStream(KeycloakSession session, RealmModel realm, UserModel user) {
        if (isLightweightUser(user)) {
            LightweightUserAdapter lua = (LightweightUserAdapter) user;
            return lua.getConsentsStream();
        } else {
            return session.users().getConsentsStream(realm, user.getId());
        }
    }

    /**
     * 更新已存储的用户 consent（含 client scope）。
     *
     * @param session Keycloak 会话
     * @param realm 领域
     * @param user 用户，不可为 {@code null}
     * @param consent 新的 consent 详情
     * @throws ModelException 该用户无对应 consent
     */
    public static void updateConsent(KeycloakSession session, RealmModel realm, UserModel user, UserConsentModel consent) {
        if (isLightweightUser(user)) {
            LightweightUserAdapter lua = (LightweightUserAdapter) user;
            lua.updateConsent(consent);
        } else {
            session.users().updateConsent(realm, user.getId(), consent);
        }
    }

    /**
     * 按客户端内部 ID 移除用户 consent。
     *
     * @param session Keycloak 会话
     * @param realm 领域
     * @param user 用户，不可为 {@code null}
     * @param clientInternalId 客户端内部 ID
     * @return 是否成功移除
     *
     * TODO: 可改为返回 Boolean 以支持异步存储的「未知」结果
     */
    public static boolean revokeConsentForClient(KeycloakSession session, RealmModel realm, UserModel user, String clientInternalId) {
        if (isLightweightUser(user)) {
            LightweightUserAdapter lua = (LightweightUserAdapter) user;
            return lua.revokeConsentForClient(clientInternalId);
        } else {
            return session.users().revokeConsentForClient(realm, user.getId(), clientInternalId);
        }
    }

}
