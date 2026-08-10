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

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.PasswordPolicy;

/**
 * {@link AgePasswordPolicyProvider} 的工厂：注册“近期未使用（天数）”密码策略。
 *
 * @author <a href="mailto:dev.maciej.mierzwa@gmail.com">Maciej Mierzwa</a>
 */
public class AgePasswordPolicyProviderFactory implements PasswordPolicyProviderFactory {
    /** 默认禁止重复使用的历史窗口：30 天。 */
    public static final Integer DEFAULT_AGE_DAYS = 30;

    /** @return 策略 ID {@link PasswordPolicy#PASSWORD_AGE} */
    @Override
    public String getId() {
        return PasswordPolicy.PASSWORD_AGE;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayName() {
        return "Not Recently Used (In Days)";
    }

    @Override
    public String getConfigType() {
        return PasswordPolicyProvider.INT_CONFIG_TYPE;
    }

    @Override
    public String getDefaultConfigValue() {
        return String.valueOf(DEFAULT_AGE_DAYS);
    }

    @Override
    public boolean isMultiplSupported() {
        return false;
    }

    /** 创建 {@link AgePasswordPolicyProvider} 实例。 */
    @Override
    public PasswordPolicyProvider create(KeycloakSession session) {
        return new AgePasswordPolicyProvider(session);
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }
}
