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

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

/**
 * 透传联邦用户存储提供者工厂，注册 {@code pass-through-federated} 测试组件。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PassThroughFederatedUserStorageProviderFactory implements UserStorageProviderFactory<PassThroughFederatedUserStorageProvider> {

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "pass-through-federated";

    /** {@inheritDoc} 创建透传联邦用户存储提供者。 */
    @Override
    public PassThroughFederatedUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new PassThroughFederatedUserStorageProvider(session, model);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
