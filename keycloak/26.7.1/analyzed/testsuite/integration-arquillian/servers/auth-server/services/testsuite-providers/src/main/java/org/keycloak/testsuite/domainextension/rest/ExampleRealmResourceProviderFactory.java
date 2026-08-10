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

package org.keycloak.testsuite.domainextension.rest;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * 域扩展示例 Realm 资源提供者工厂，注册 {@code example} 扩展端点。
 */
public class ExampleRealmResourceProviderFactory implements RealmResourceProviderFactory {

    /** 工厂唯一标识符。 */
    public static final String ID = "example";

    /** {@inheritDoc} 返回工厂标识。 */
    @Override
    public String getId() {
        return ID;
    }

    /** {@inheritDoc} 创建 Realm 资源提供者实例。 */
    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new ExampleRealmResourceProvider(session);
    }

    /** {@inheritDoc} 初始化工厂配置。 */
    @Override
    public void init(Scope config) {
    }

    /** {@inheritDoc} 会话工厂就绪后的回调。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

}
