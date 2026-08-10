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
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * 密码哈希算法策略工厂：配置 realm 使用的 {@link PasswordHashProvider} 算法 ID。
 * <p>同时实现 {@link PasswordPolicyProviderFactory} 与 {@link PasswordPolicyProvider}，校验阶段不拦截密码，仅提供配置解析与默认值。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class HashAlgorithmPasswordPolicyProviderFactory implements PasswordPolicyProviderFactory, PasswordPolicyProvider {

    private KeycloakSession session;

    /** 默认哈希算法 ID，在 {@link #postInit} 中从 {@link PasswordHashProvider} 工厂解析。 */
    private String defaultHashAlgorithm;

    /** 绑定会话并返回自身作为策略提供者实例。 */
    @Override
    public PasswordPolicyProvider create(KeycloakSession session) {
        this.session = session;
        return this;
    }

    @Override
    public void init(Config.Scope config) {
    }

    /** 初始化默认哈希算法为已注册的 {@link PasswordHashProvider} 工厂 ID。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        defaultHashAlgorithm = factory.getProviderFactory(PasswordHashProvider.class).getId();
    }

    @Override
    public void close() {
    }

    /** @return 策略 ID {@link PasswordPolicy#HASH_ALGORITHM_ID} */
    @Override
    public String getId() {
        return PasswordPolicy.HASH_ALGORITHM_ID;
    }

    /** 哈希算法策略不参与密码内容校验。 */
    @Override
    public PolicyError validate(RealmModel realm, UserModel user, String password) {
        return null;
    }

    @Override
    public PolicyError validate(String user, String password) {
        return null;
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayName() {
        return "Hashing Algorithm";
    }

    @Override
    public String getConfigType() {
        return PasswordPolicyProvider.STRING_CONFIG_TYPE;
    }

    @Override
    public String getDefaultConfigValue() {
        return defaultHashAlgorithm;
    }

    @Override
    public boolean isMultiplSupported() {
        return false;
    }

    /**
     * 解析并校验哈希算法提供者 ID 是否存在。
     * @param value {@link PasswordHashProvider} 工厂 ID
     * @return 校验通过的算法 ID
     * @throws PasswordPolicyConfigException 未设置或找不到对应提供者时
     */
    @Override
    public Object parseConfig(String value) {
        if (value == null) {
            throw new PasswordPolicyConfigException("Password hashing provider id must be set");
        }
        PasswordHashProvider provider = session.getProvider(PasswordHashProvider.class, value);
        if (provider == null) {
            throw new PasswordPolicyConfigException("Password hashing provider not found");
        }
        return value;
    }

}
