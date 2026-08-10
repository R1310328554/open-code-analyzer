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

import java.util.Set;

import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.ClientConnection;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * 暴力破解防护提供者：记录登录失败、临时/永久锁定与成功登录。
 * <p>配合 Realm 暴力破解策略限制异常登录尝试。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface BruteForceProtector extends Provider {
    /** 用户因永久锁定被禁用时使用的属性键。 */
    String DISABLED_BY_PERMANENT_LOCKOUT = "permanentLockout";

    /** 记录一次失败登录尝试。 */
    void failedLogin(RealmModel realm, UserModel user, ClientConnection clientConnection, UriInfo uriInfo, Set<String> authenticationCategory);

    /** 记录一次成功登录（可重置失败计数）。 */
    void successfulLogin(RealmModel realm, UserModel user, ClientConnection clientConnection, UriInfo uriInfo, Set<String> authenticationCategories);

    /** @return 用户是否处于临时锁定状态 */
    boolean isTemporarilyDisabled(KeycloakSession session, RealmModel realm, UserModel user);

    /** @return 用户是否已被永久锁定 */
    boolean isPermanentlyLockedOut(KeycloakSession session, RealmModel realm, UserModel user);

    /**
     * 清除永久锁定痕迹（不会自动启用用户账户）。
     * Clears any remaining traces of the permanent lockout. Does not enable the user as such!
     * @param session
     * @param realm
     * @param user
     */
    void cleanUpPermanentLockout(KeycloakSession session, RealmModel realm, UserModel user);
}
