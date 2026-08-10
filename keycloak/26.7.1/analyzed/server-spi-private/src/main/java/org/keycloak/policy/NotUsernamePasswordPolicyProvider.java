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

package org.keycloak.policy;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * “非用户名”密码策略：禁止密码与用户名相同（大小写不敏感）。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NotUsernamePasswordPolicyProvider implements PasswordPolicyProvider {

    /** 违反策略时的 i18n 消息键。 */
    private static final String ERROR_MESSAGE = "invalidPasswordNotUsernameMessage";

    private KeycloakContext context;

    /** @param context Keycloak 上下文 */
    public NotUsernamePasswordPolicyProvider(KeycloakContext context) {
        this.context = context;
    }

    /** 比较密码与用户名（忽略大小写），相同则返回 {@link PolicyError}。 */
    @Override
    public PolicyError validate(String username, String password) {
        if (username == null) {
            return null;
        }
        return username.equalsIgnoreCase(password) ? new PolicyError(ERROR_MESSAGE) : null;
    }

    /** 使用用户模型中的用户名进行校验。 */
    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        return validate(user.getUsername(), password);
    }

    /** 本策略无需配置，始终返回 {@code null}。 */
    @Override
    public Object parseConfig(String value) {
        return null;
    }

    @Override
    public void close() {
    }

}
