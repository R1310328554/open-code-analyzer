/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.operator.crds.v2beta1.deployment.spec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.kubernetes.api.model.SecretKeySelector;
import io.sundr.builder.annotations.Buildable;

/**
 * Keycloak 外部数据库连接与连接池配置规范。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class DatabaseSpec {

    /** 数据库厂商标识（如 postgres、mysql）。 */
    @JsonPropertyDescription("The database vendor.")
    private String vendor;

    /** 数据库用户名 Secret 引用。 */
    @JsonPropertyDescription("The reference to a secret holding the username of the database user.")
    private SecretKeySelector usernameSecret;

    /** 数据库密码 Secret 引用。 */
    @JsonPropertyDescription("The reference to a secret holding the password of the database user.")
    private SecretKeySelector passwordSecret;

    /** 默认 JDBC URL 中的数据库名；若设置了 {@link #url} 则忽略。 */
    @JsonPropertyDescription("Sets the database name of the default JDBC URL of the chosen vendor. If the `url` option is set, this option is ignored.")
    private String database;

    /** 默认 JDBC URL 中的主机名；若设置了 {@link #url} 则忽略。 */
    @JsonPropertyDescription("Sets the hostname of the default JDBC URL of the chosen vendor. If the `url` option is set, this option is ignored.")
    private String host;

    /** 默认 JDBC URL 中的端口；若设置了 {@link #url} 则忽略。 */
    @JsonPropertyDescription("Sets the port of the default JDBC URL of the chosen vendor. If the `url` option is set, this option is ignored.")
    private Integer port;

    /** 数据库 schema 名称。 */
    @JsonPropertyDescription("The database schema to be used.")
    private String schema;

    /** 完整 JDBC URL；未设置时按 vendor 生成默认 URL。 */
    @JsonPropertyDescription("The full database JDBC URL. If not provided, a default URL is set based on the selected database vendor. " +
            "For instance, if using 'postgres', the default JDBC URL would be 'jdbc:postgresql://localhost/keycloak'. ")
    private String url;

    /** 连接池初始大小。 */
    @JsonPropertyDescription("The initial size of the connection pool.")
    private Integer poolInitialSize;

    /** 连接池最小空闲连接数。 */
    @JsonPropertyDescription("The minimal size of the connection pool.")
    private Integer poolMinSize;

    /** 连接池最大连接数。 */
    @JsonPropertyDescription("The maximum size of the connection pool.")
    private Integer poolMaxSize;

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public SecretKeySelector getUsernameSecret() {
        return usernameSecret;
    }

    public void setUsernameSecret(SecretKeySelector usernameSecret) {
        this.usernameSecret = usernameSecret;
    }

    public SecretKeySelector getPasswordSecret() {
        return passwordSecret;
    }

    public void setPasswordSecret(SecretKeySelector passwordSecret) {
        this.passwordSecret = passwordSecret;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPoolInitialSize() {
        return poolInitialSize;
    }

    public void setPoolInitialSize(Integer poolInitialSize) {
        this.poolInitialSize = poolInitialSize;
    }

    public Integer getPoolMinSize() {
        return poolMinSize;
    }

    public void setPoolMinSize(Integer poolMinSize) {
        this.poolMinSize = poolMinSize;
    }

    public Integer getPoolMaxSize() {
        return poolMaxSize;
    }

    public void setPoolMaxSize(Integer poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }
}
