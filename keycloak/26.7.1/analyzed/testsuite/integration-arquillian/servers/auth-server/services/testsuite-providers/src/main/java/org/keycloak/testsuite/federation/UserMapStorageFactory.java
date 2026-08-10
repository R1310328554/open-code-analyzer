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
package org.keycloak.testsuite.federation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderFactory;

/**
 * {@link UserMapStorage} 工厂，在测试间共享内存用户与组映射。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserMapStorageFactory implements UserStorageProviderFactory<UserMapStorage> {


    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "user-password-map-arq";

    /** 静态配置属性列表。 */
    protected static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty attr = new ProviderConfigProperty("attr", "attr",
                "This is some attribute",
                ProviderConfigProperty.STRING_TYPE, null);
        configProperties.add(attr);
    }

    /** 跨实例共享的用户名-密码映射。 */
    private final Map<String, String> userPasswords = new ConcurrentHashMap<>();
    /** 跨实例共享的用户-组映射。 */
    private final ConcurrentMap<String, Set<String>> userGroups = new ConcurrentHashMap<>();

    /** {@inheritDoc} */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public UserMapStorage create(KeycloakSession session, ComponentModel model) {
        return new UserMapStorage(session, model, userPasswords, userGroups);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
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

    /** 清空共享用户与组映射（测试重置用）。 */
    public void clear() {
        userPasswords.clear();
        userGroups.clear();
    }
}
