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

package org.keycloak.models.jpa.session;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.session.UserSessionPersisterProvider;
import org.keycloak.models.session.UserSessionPersisterProviderFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

/**
 * JPA 用户会话 Persister Provider 工厂。
 * <p>
 * SPI ID 为 {@value #ID}；可配置 {@value #EXPIRATION_BATCH_CONFIG} 控制过期清理批大小。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JpaUserSessionPersisterProviderFactory implements UserSessionPersisterProviderFactory, ServerInfoAwareProviderFactory {

    /** SPI provider ID。 */
    public static final String ID = "jpa";

    /** 过期清理批大小配置键。 */
    private static final String EXPIRATION_BATCH_CONFIG = "expirationBatch";
    // 默认 512。各数据库 IN 子句参数上限：Oracle 1000（ORA-01795）、SQL Server ~2100、PostgreSQL/MySQL 通常更高
    public static final int DEFAULT_EXPIRATION_BATCH = 512;

    /** 当前配置的过期清理批大小。 */
    private int expirationBatch = DEFAULT_EXPIRATION_BATCH;

    @Override
    public UserSessionPersisterProvider create(KeycloakSession session) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return new JpaUserSessionPersisterProvider(session, em, expirationBatch);
    }

    @Override
    public void init(Config.Scope config) {
        // 不设上限：Hibernate 方言会自动拆分超大 IN 列表（如 Oracle 1000 项限制）
        expirationBatch = Math.max(1, config.getInt(EXPIRATION_BATCH_CONFIG, DEFAULT_EXPIRATION_BATCH));
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public Set<Class<? extends Provider>> dependsOn() {
        return Set.of(JpaConnectionProvider.class);
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        var builder = ProviderConfigurationBuilder.create();
        builder.property()
                .name(EXPIRATION_BATCH_CONFIG)
                .helpText("Sets the size of the expiration batch, i.e., the number of expired sessions to remove per delete statement.")
                .label("size")
                .type(ProviderConfigProperty.INTEGER_TYPE)
                .defaultValue(DEFAULT_EXPIRATION_BATCH)
                .add();
        return builder.build();
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        return Map.of(EXPIRATION_BATCH_CONFIG, Integer.toString(expirationBatch));
    }
}
