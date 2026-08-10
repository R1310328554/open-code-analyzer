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

package org.keycloak.connections.jpa.updater;

import java.io.File;
import java.sql.Connection;

import org.keycloak.provider.Provider;

/**
 * JPA 数据库 schema 升级 Provider：负责校验、应用变更集或将 SQL 脚本导出到文件。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface JpaUpdaterProvider extends Provider {

    /**
     * 数据库 schema 与 changelog 的同步状态。
     */
    enum Status {
        /**
         * 数据库有效且已应用全部变更集。
         */
        VALID,
        /**
         * 数据库不存在或尚未初始化任何 changelog。
         */
        EMPTY,
        /**
         * 存在未应用的变更集，需要升级。
         */
        OUTDATED
    }

    /**
     * 对 Keycloak 数据库执行 schema 升级（应用所有待执行的 Liquibase changeset）。
     * @param connection DB connection
     * @param defaultSchema DB connection
     */
    void update(Connection connection, String defaultSchema);

    /**
     * 检查数据库是否已与最新 changeset 同步。
     * @param connection DB connection
     * @param defaultSchema DB schema to use
     * @return
     */
    Status validate(Connection connection, String defaultSchema);

    /**
     * 将待执行的 SQL 升级脚本导出到指定文件（不直接修改数据库）。
     * @param connection DB connection
     * @param defaultSchema DB schema to use
     * @param file File to write to
     */
    void export(Connection connection, String defaultSchema, File file);

}
