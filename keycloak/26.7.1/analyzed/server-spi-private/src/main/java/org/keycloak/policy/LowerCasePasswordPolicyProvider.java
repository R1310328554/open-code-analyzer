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
 * 密码小写字母策略提供者：要求密码包含不少于配置数量的小写字符。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LowerCasePasswordPolicyProvider implements PasswordPolicyProvider {

    private static final String ERROR_MESSAGE = "invalidPasswordMinLowerCaseCharsMessage";

    private KeycloakContext context;

    /** @param context Keycloak 上下文，用于读取 realm 密码策略 */
    public LowerCasePasswordPolicyProvider(KeycloakContext context) {
        this.context = context;
    }

    /**
     * 统计密码中小写字符数量并与策略最小值比较。
     * @param username 用户名（本策略未使用）
     * @param password 待校验密码
     * @return 小写字母不足时返回 {@link PolicyError}，否则 {@code null}
     */
    @Override
    public PolicyError validate(String username, String password) {
        int min = context.getRealm().getPasswordPolicy().getPolicyConfig(LowerCasePasswordPolicyProviderFactory.ID);
        int count = 0;
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                count++;
            }
        }
        return count < min ? new PolicyError(ERROR_MESSAGE, min) : null;
    }

    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        return validate(user.getUsername(), password);
    }

    /** 将配置解析为最少小写字母数，无效时默认 1。 */
    @Override
    public Object parseConfig(String value) {
        return parseInteger(value, 1);
    }

    @Override
    public void close() {
    }

}
