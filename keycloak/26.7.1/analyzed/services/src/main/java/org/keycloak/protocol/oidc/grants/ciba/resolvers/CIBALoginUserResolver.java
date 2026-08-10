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

import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * CIBA 登录用户解析器接口。
 * <p>将 login_hint、login_hint_token、id_token_hint 等提示解析为 {@link UserModel}，
 * 并在 Keycloak 用户与外部认证设备（AD）可识别的用户标识之间双向转换。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public interface CIBALoginUserResolver extends Provider {

    /**
     * 根据 login_hint 参数解析用户。
     * @param loginHint 登录提示（用户名或邮箱）
     * @return 对应的 {@link UserModel}，无法解析时返回 null
     */
    default UserModel getUserFromLoginHint(String loginHint) {
        return null;
    }

    /**
     * 根据 login_hint_token 参数解析用户。
     * @param loginHintToken 登录提示令牌
     * @return 对应的 {@link UserModel}，无法解析时返回 null
     */
    default UserModel getUserFromLoginHintToken(String loginHintToken) {
        return null;
    }

    /**
     * 根据 id_token_hint 参数解析用户。
     * @param idToken ID Token 提示
     * @return 对应的 {@link UserModel}，无法解析时返回 null
     */
    default UserModel getUserFromIdTokenHint(String idToken) {
        return null;
    }

    /**
     * 将 {@link UserModel} 转换为 AD 可识别的用户标识（默认用户名）。
     * @param user 用户模型
     * @return AD 用于识别用户的标识字符串
     */
    default String getInfoUsedByAuthentication(UserModel user) {
        return user.getUsername();
    }

    /**
     * 将 AD 返回的用户标识反向解析为 {@link UserModel}。
     * @param info AD 可识别的用户标识
     * @return 对应的用户模型
     */
    UserModel getUserFromInfoUsedByAuthentication(String info);

}
