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
 */
package org.keycloak.protocol.oidc.grants.ciba.resolvers;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * 默认 CIBA 登录用户解析器。
 * <p>通过用户名或邮箱查找用户，并将用户名作为 AD 认证标识。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class DefaultCIBALoginUserResolver implements CIBALoginUserResolver {

    /** Keycloak 会话 */
    private KeycloakSession session;

    /** @param session Keycloak 会话 */
    public DefaultCIBALoginUserResolver(KeycloakSession session) {
        this.session = session;
    }

    /** 按用户名或邮箱解析 login_hint @param loginHint 登录提示 @return 用户模型 */
    @Override
    public UserModel getUserFromLoginHint(String loginHint) {
        return KeycloakModelUtils.findUserByNameOrEmail(session, session.getContext().getRealm(), loginHint);
    }

    /** @param user 用户模型 @return 用户名（供 AD 识别） */
    @Override
    public String getInfoUsedByAuthentication(UserModel user) {
        return user.getUsername();
    }

    /** 按 AD 返回的用户标识（用户名/邮箱）查找用户 @param info 用户标识 @return 用户模型 */
    @Override
    public UserModel getUserFromInfoUsedByAuthentication(String info) {
        return KeycloakModelUtils.findUserByNameOrEmail(session, session.getContext().getRealm(), info);
    }

    @Override
    public void close() {
    }

}
