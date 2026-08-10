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

package org.keycloak.policy;

import org.keycloak.models.KeycloakContext;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 密码不得等于邮箱策略提供者：禁止将用户当前邮箱（忽略大小写）作为密码。
 * <p>A {@link PasswordPolicyProvider} which does not allow to use the current email as password.</p>
 *
 * @author <a href="mailto:thomas.darimont@googlemail.com">Thomas Darimont</a>
 */
public class NotEmailPasswordPolicyProvider implements PasswordPolicyProvider {

    private static final String ERROR_MESSAGE = "invalidPasswordNotEmailMessage";
    private static final PolicyError POLICY_ERROR = new PolicyError(ERROR_MESSAGE);

    private KeycloakContext context;

    /** @param context Keycloak 上下文 */
    public NotEmailPasswordPolicyProvider(KeycloakContext context) {
        this.context = context;
    }

    /**
     * 校验密码是否与邮箱相同（大小写不敏感）。
     * @param email 用户邮箱
     * @param password 待校验密码
     * @return 与邮箱相同时返回 {@link PolicyError}，否则 {@code null}
     */
    @Override
    public PolicyError validate(String email, String password) {
        if (email == null) {
            return null;
        }
        return email.equalsIgnoreCase(password) ? POLICY_ERROR : null;
    }

    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        return validate(user.getEmail(), password);
    }

    @Override
    public Object parseConfig(String value) {
        return null;
    }

    @Override
    public void close() {
        // 无资源需释放
    }

}
