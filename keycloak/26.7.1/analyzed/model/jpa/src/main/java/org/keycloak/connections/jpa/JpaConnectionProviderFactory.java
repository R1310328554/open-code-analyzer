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

package org.keycloak.connections.jpa;

import java.sql.Connection;

import org.keycloak.provider.ProviderFactory;

/**
 * {@link JpaConnectionProvider} 的工厂 SPI，额外提供 JDBC 连接与 schema 名称访问。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface JpaConnectionProviderFactory extends ProviderFactory<JpaConnectionProvider> {

    /** 获取原始 JDBC 连接；调用方负责关闭。 */
    // Caller is responsible for closing connection
    Connection getConnection();

    /** 返回配置的数据库 schema 名称，未配置时为 {@code null}。 */
    String getSchema();

}
