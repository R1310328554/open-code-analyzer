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

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * 密码策略 Provider SPI：验证密码是否符合 realm 策略并解析策略配置。
 *
 * @author <a href="mailto:roelof.naude@epiuse.com">Roelof Naude</a>
 */
public interface PasswordPolicyProvider extends Provider {

    /** 字符串类型策略配置。 */
    String STRING_CONFIG_TYPE = "String";
    /** 整数类型策略配置。 */
    String INT_CONFIG_TYPE = "int";

    /** 在 realm 上下文中验证用户密码。
     * @return 验证失败时返回 {@link PolicyError}，成功返回 null */
    PolicyError validate(RealmModel realm, UserModel user, String password);
    /** 无 realm 上下文时验证密码。
     * @return 验证失败时返回 {@link PolicyError}，成功返回 null */
    PolicyError validate(String user, String password);
    /** 解析策略配置字符串。
     * @param value 配置值
     * @return 解析后的配置对象 */
    Object parseConfig(String value);

    /** 解析整数配置，无效时抛出 {@link PasswordPolicyConfigException}。
     * @param value 配置值
     * @param defaultValue 默认值
     * @return 解析后的整数 */
    default Integer parseInteger(String value, Integer defaultValue) {
        try {
            return value != null ? Integer.valueOf(value) : defaultValue;
        } catch (NumberFormatException e) {
            throw new PasswordPolicyConfigException("Not a valid number");
        }
    }
}
