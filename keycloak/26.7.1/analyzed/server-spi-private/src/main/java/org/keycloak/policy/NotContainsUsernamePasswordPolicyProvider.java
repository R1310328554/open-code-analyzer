/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.policy;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 密码不得包含用户名策略提供者：禁止密码（忽略大小写）包含用户名字符串。
 * <p>实现 {@link PasswordPolicyProvider}，无需额外配置参数。</p>
 */
public class NotContainsUsernamePasswordPolicyProvider implements PasswordPolicyProvider {

    private static final String ERROR_MESSAGE = "invalidPasswordNotContainsUsernameMessage";

    private KeycloakContext context;

    /** @param context Keycloak 上下文 */
    public NotContainsUsernamePasswordPolicyProvider(KeycloakContext context) {
        this.context = context;
    }

    /**
     * 校验密码是否包含用户名（大小写不敏感）。
     * @param username 用户名
     * @param password 待校验密码
     * @return 包含用户名时返回 {@link PolicyError}，否则 {@code null}
     */
    @Override
    public PolicyError validate(String username, String password) {
        if (username == null) {
            return null;
        }
        return password.toLowerCase().contains(username.toLowerCase()) ? new PolicyError(ERROR_MESSAGE) : null;
    }

    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        return validate(user.getUsername(), password);
    }

    /** 本策略无配置项，始终返回 {@code null}。 */
    @Override
    public Object parseConfig(String value) {
        return null;
    }

    @Override
    public void close() {
    }

}
