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

package org.keycloak.models.utils;

import org.keycloak.OAuth2Constants;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.LoginProtocolFactory;

/**
 * 领域默认客户端范围初始化工具类。
 * <p>委托各 {@link org.keycloak.protocol.LoginProtocolFactory} 创建协议默认 scope，并提供 {@code offline_access} 内置范围。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultClientScopes {

    /**
     * 为领域创建各登录协议的默认客户端范围。
     *
     * @param session
     * @param realm
     * @param addScopesToExistingClients true when creating new realm. False when migrating from previous version
     */
    public static void createDefaultClientScopes(KeycloakSession session, RealmModel realm, boolean addScopesToExistingClients) {
        session.getKeycloakSessionFactory().getProviderFactoriesStream(LoginProtocol.class)
                .map(LoginProtocolFactory.class::cast)
                .forEach(lpf -> lpf.createDefaultClientScopes(realm, addScopesToExistingClients));
    }


    // 假定 newRealm 与 offlineRole 非空且 offline_access 范围尚不存在；由调用方保证前提条件
    /** 创建 {@code offline_access} 可选客户端范围并映射 offline 角色。 */
    public static void createOfflineAccessClientScope(RealmModel newRealm, RoleModel offlineRole) {
        ClientScopeModel offlineAccessScope = newRealm.addClientScope(OAuth2Constants.OFFLINE_ACCESS);
        offlineAccessScope.setDescription("OpenID Connect built-in scope: offline_access");
        offlineAccessScope.setDisplayOnConsentScreen(true);
        offlineAccessScope.setConsentScreenText(Constants.OFFLINE_ACCESS_SCOPE_CONSENT_TEXT);
        offlineAccessScope.setProtocol("openid-connect");
        offlineAccessScope.addScopeMapping(offlineRole);

        // 可选 scope，需通过 scope 参数显式请求
        newRealm.addDefaultClientScope(offlineAccessScope, false);
    }
}
