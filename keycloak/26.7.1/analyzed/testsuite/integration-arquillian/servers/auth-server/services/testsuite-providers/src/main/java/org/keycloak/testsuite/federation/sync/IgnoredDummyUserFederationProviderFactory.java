/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.federation.sync;

import java.util.Date;

import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.storage.user.SynchronizationResult;
import org.keycloak.testsuite.federation.DummyUserFederationProviderFactory;

/**
 * 测试用 {@link org.keycloak.storage.UserStorageProviderFactory}，同步方法始终返回 ignored。
 * 继承 {@link DummyUserFederationProviderFactory} 并覆盖同步行为。
 *
 * @author rmartinc
 */
public class IgnoredDummyUserFederationProviderFactory extends DummyUserFederationProviderFactory {

    /** 提供者标识符。 */
    public static final String IGNORED_PROVIDER_ID = "ignored-dummy";

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return IGNORED_PROVIDER_ID;
    }

    /** {@inheritDoc} 全量同步始终忽略。 */
    @Override
    public SynchronizationResult sync(KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        return SynchronizationResult.ignored();
    }

    /** {@inheritDoc} 增量同步始终忽略。 */
    @Override
    public SynchronizationResult syncSince(Date lastSync, KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model) {
        return SynchronizationResult.ignored();
    }
}
