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

package org.keycloak.quarkus.runtime.storage.database.jpa;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import jakarta.enterprise.inject.Instance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.SynchronizationType;

import org.keycloak.Config;
import org.keycloak.config.DatabaseOptions;
import org.keycloak.connections.jpa.DefaultJpaConnectionProvider;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.connections.jpa.JpaConnectionProviderFactory;
import org.keycloak.connections.jpa.support.EntityManagerProxy;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import io.quarkus.arc.Arc;
import io.quarkus.hibernate.orm.PersistenceUnit;
import liquibase.GlobalConfiguration;
import org.hibernate.internal.SessionFactoryImpl;
import org.jboss.logging.Logger;

/**
 * Quarkus JPA 连接提供方工厂抽象基类：从 CDI 获取 {@link EntityManagerFactory}，创建带 {@link EntityManagerProxy} 的 {@link JpaConnectionProvider}，并处理 schema/Liquibase 大小写。
 */
public abstract class AbstractJpaConnectionProviderFactory implements JpaConnectionProviderFactory {

    private final Logger logger = Logger.getLogger(getClass());

    protected Config.Scope config;
    protected EntityManagerFactory entityManagerFactory;

    /** 为会话创建默认 JPA 连接提供方（会话托管 EntityManager）。 */
    @Override
    public JpaConnectionProvider create(KeycloakSession session) {
        return new DefaultJpaConnectionProvider(createEntityManager(entityManagerFactory, session, true));
    }

    /** 从 Hibernate SessionFactory 获取底层 JDBC 连接。 */
    @Override
    public Connection getConnection() {
        SessionFactoryImpl entityManagerFactory = this.entityManagerFactory.unwrap(SessionFactoryImpl.class);

        try {
            return entityManagerFactory.getJdbcServices().getBootstrapJdbcConnectionAccess().obtainConnection();
        } catch (SQLException cause) {
            throw new RuntimeException("Failed to obtain JDBC connection", cause);
        }
    }

    /** 返回配置的 DB schema；含连字符时自动启用 Liquibase PRESERVE_SCHEMA_CASE。 */
    @Override
    public String getSchema() {
        String schema = Configuration.getConfigValue(DatabaseOptions.DB_SCHEMA).getValue();
        if (schema != null && schema.contains("-") && ! Boolean.parseBoolean(System.getProperty(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey()))) {
            System.setProperty(GlobalConfiguration.PRESERVE_SCHEMA_CASE.getKey(), "true");
            logger.warnf("The passed schema '%s' contains a dash. Setting liquibase config option PRESERVE_SCHEMA_CASE to true. See https://github.com/keycloak/keycloak/issues/20870 for more information.", schema);
        }
        return schema;
    }

    @Override
    public void init(Config.Scope config) {
        this.config = config;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        entityManagerFactory = getEntityManagerFactory();
    }

    @Override
    public void close() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    /** 子类提供具体持久化单元的 EntityManagerFactory。 */
    protected abstract EntityManagerFactory getEntityManagerFactory();

    /** 按 {@link PersistenceUnit} 名称从 Arc 容器解析 EntityManagerFactory。 */
    protected Optional<EntityManagerFactory> getEntityManagerFactory(String unitName) {
        Instance<EntityManagerFactory> instance = Arc.container().select(EntityManagerFactory.class, new PersistenceUnit() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return PersistenceUnit.class;
            }

            @Override
            public String value() {
                return unitName;
            }
        });

        if (instance.isResolvable()) {
            return Optional.of(instance.get());
        }

        return Optional.empty();
    }

    /** 创建同步 EntityManager 并包装为 Keycloak 会话代理。 */
    protected EntityManager createEntityManager(EntityManagerFactory emf, KeycloakSession session, boolean sessionManaged) {
        EntityManager entityManager = EntityManagerProxy.create(session, emf.createEntityManager(SynchronizationType.SYNCHRONIZED), sessionManaged);

        entityManager.setFlushMode(FlushModeType.AUTO);

        return entityManager;
    }
}
