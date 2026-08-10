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
 * 数字字符密码策略：要求密码至少包含指定数量的数字。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DigitsPasswordPolicyProvider implements PasswordPolicyProvider {

    private static final String ERROR_MESSAGE = "invalidPasswordMinDigitsMessage";

    private KeycloakContext context;

    /** @param context Keycloak 上下文 */
    public DigitsPasswordPolicyProvider(KeycloakContext context) {
        this.context = context;
    }

    /** 统计密码中数字字符数量并与策略最小值比较。 */
    @Override
    public PolicyError validate(String username, String password) {
        int min = context.getRealm().getPasswordPolicy().getPolicyConfig(DigitsPasswordPolicyProviderFactory.ID);
        int count = 0;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                count++;
            }
        }
        return count < min ? new PolicyError(ERROR_MESSAGE, min) : null;
    }

    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        return validate(user.getUsername(), password);
    }

    /** 解析最小数字个数配置，缺省为 1。 */
    @Override
    public Object parseConfig(String value) {
        return value != null ? Integer.valueOf(value) : Integer.valueOf(1);
    }

    @Override
    public void close() {
    }

}
