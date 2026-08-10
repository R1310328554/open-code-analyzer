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

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * {@link LengthPasswordPolicyProvider} 的工厂：注册“最小长度”密码策略。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LengthPasswordPolicyProviderFactory implements PasswordPolicyProviderFactory {

    /** 策略 ID：{@code length}。 */
    public static final String ID = "length";

    @Override
    public String getId() {
        return ID;
    }

    /** 创建 {@link LengthPasswordPolicyProvider} 实例。 */
    @Override
    public PasswordPolicyProvider create(KeycloakSession session) {
        return new LengthPasswordPolicyProvider(session.getContext());
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayName() {
        return "Minimum Length";
    }

    @Override
    public String getConfigType() {
        return PasswordPolicyProvider.INT_CONFIG_TYPE;
    }

    @Override
    public String getDefaultConfigValue() {
        return "8";
    }

    @Override
    public boolean isMultiplSupported() {
        return false;
    }

    @Override
    public void close() {
    }

}
