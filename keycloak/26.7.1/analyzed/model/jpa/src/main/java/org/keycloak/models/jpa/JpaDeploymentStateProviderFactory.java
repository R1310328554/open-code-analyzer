/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.jpa;

import jakarta.persistence.EntityManager;

import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.DeploymentStateProvider;
import org.keycloak.models.DeploymentStateProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import static org.keycloak.models.jpa.JpaRealmProviderFactory.PROVIDER_ID;
import static org.keycloak.models.jpa.JpaRealmProviderFactory.PROVIDER_PRIORITY;

/**
 * JPA {@link DeploymentStateProvider} 工厂：读写 realm 部署/迁移状态（经 {@link JpaRealmProvider}）。
 * <p>
 * 用于跨节点协调 schema 版本、迁移进度等部署元数据。
 */
public class JpaDeploymentStateProviderFactory implements DeploymentStateProviderFactory {

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 与 ClientScope 工厂相同：单一 JpaRealmProvider 门面多种 Provider 接口。 */
    @Override
    public DeploymentStateProvider create(KeycloakSession session) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return new JpaRealmProvider(session, em, null, null);
    }

    @Override
    public void close() {
    }

    @Override
    public int order() {
        return PROVIDER_PRIORITY;
    }
}
