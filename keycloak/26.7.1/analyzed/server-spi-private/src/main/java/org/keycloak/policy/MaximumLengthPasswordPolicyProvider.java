/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 * 密码最大长度策略提供者：要求密码字符数不超过 realm 配置的上限。
 *
 * @author rmartinc
 */
public class MaximumLengthPasswordPolicyProvider implements PasswordPolicyProvider {

    private static final String ERROR_MESSAGE = "invalidPasswordMaxLengthMessage";

    private final KeycloakContext context;

    /** @param context Keycloak 上下文，用于读取 realm 密码策略 */
    public MaximumLengthPasswordPolicyProvider(KeycloakContext context) {
        this.context = context;
    }

    /**
     * 校验密码长度是否超过最大长度限制。
     * @param username 用户名（本策略未使用）
     * @param password 待校验密码
     * @return 超长时返回 {@link PolicyError}，否则 {@code null}
     */
    @Override
    public PolicyError validate(String username, String password) {
        int max = context.getRealm().getPasswordPolicy().getPolicyConfig(MaximumLengthPasswordPolicyProviderFactory.ID);
        return password.length() > max ? new PolicyError(ERROR_MESSAGE, max) : null;
    }

    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        return validate(user.getUsername(), password);
    }

    /** 将配置解析为最大长度整数，无效时使用工厂默认值。 */
    @Override
    public Object parseConfig(String value) {
        return parseInteger(value, MaximumLengthPasswordPolicyProviderFactory.DEFAULT_MAX_LENGTH);
    }

    @Override
    public void close() {
    }
}
