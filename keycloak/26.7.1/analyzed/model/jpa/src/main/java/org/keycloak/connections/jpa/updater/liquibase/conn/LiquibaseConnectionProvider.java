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

package org.keycloak.connections.jpa.updater.liquibase.conn;

import java.sql.Connection;

import org.keycloak.provider.Provider;

import liquibase.exception.LiquibaseException;

/**
 * 为 Keycloak 提供 Liquibase 连接与实例的 SPI 提供者。
 * <p>封装标准 schema 迁移与自定义 changelog 两种场景的 {@link KeycloakLiquibase} 构建。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface LiquibaseConnectionProvider extends Provider {

    /** 基于主 changelog 创建 Liquibase 实例，用于常规 JPA schema 升级。 */
    KeycloakLiquibase getLiquibase(Connection connection, String defaultSchema) throws LiquibaseException;

    /** 为自定义 changelog 与独立变更表创建 Liquibase 实例（如实体扩展迁移）。 */
    KeycloakLiquibase getLiquibaseForCustomUpdate(Connection connection, String defaultSchema, String changelogLocation, ClassLoader classloader, String changelogTableName) throws LiquibaseException;

}
