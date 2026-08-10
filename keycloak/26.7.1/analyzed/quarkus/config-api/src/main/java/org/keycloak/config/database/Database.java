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

package org.keycloak.config.database;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.keycloak.common.util.TriFunction;
import org.keycloak.config.DatabaseOptions;
import org.keycloak.config.DatabaseOptions.DatabaseTlsMode;
import org.keycloak.config.Option;

import io.quarkus.runtime.util.StringUtil;


/**
 * Keycloak 支持的数据库厂商元数据：驱动、方言、默认 JDBC URL 与 Liquibase 类型映射。
 */
import static java.util.Arrays.asList;


/**
 * Keycloak 支持的数据库厂商元数据：驱动、方言、默认 JDBC URL 与 Liquibase 类型映射。
 */
public final class Database {
    
    /** 数据库别名到 {@link Vendor} 的查找表。 */
    /** 数据库别名到 {@link Vendor} 的查找表。 */
    private static final Map<String, Vendor> DATABASES = new HashMap<>();

    static {
        for (Vendor vendor : Vendor.values()) {
            for (String alias : vendor.aliases) {
                DATABASES.put(alias, vendor);
            }
        }
    }

    /** 判断给定 Liquibase 数据库类型是否与当前 db-kind 兼容。 */
    /** 判断给定 Liquibase 数据库类型是否与当前 db-kind 兼容。 */
    public static boolean isLiquibaseDatabaseSupported(String databaseType, String dbKind) {
        for (Vendor vendor : DATABASES.values()) {
            if (vendor.liquibaseType.equals(databaseType) && vendor.isOfKind(dbKind)) {
                return true;
            }
        }

        return false;
    }

    /** 按厂商名或别名解析 {@link Vendor}。 */
    /** 按厂商名或别名解析 {@link Vendor}。 */
    public static Optional<Vendor> getVendor(String vendor) {
        return Arrays.stream(Vendor.values())
                .filter(v -> v.isOfKind(vendor) || asList(v.aliases).contains(vendor))
                .findAny();
    }

    /** 返回别名对应的 Quarkus db-kind 字符串。 */
    /** 返回别名对应的 Quarkus db-kind 字符串。 */
    public static Optional<String> getDatabaseKind(String alias) {
        return mapValue(alias, vendor -> vendor.databaseKind);
    }

    /**
     * The {@param namedProperty} represents name of the named datasource if we need to set the URL for additional datasource
 * 其中 {@param namedProperty} 表示命名数据源名称，用于为附加数据源生成 JDBC URL。

     */
    /** 按配置选项与别名生成默认 JDBC URL。 */
    /** 按配置选项与别名生成默认 JDBC URL。 */
    public static Optional<String> getDefaultUrl(Function<Option<?>, String> getter, String namedProperty, String alias) {
        return getVendor(alias).map(f -> f.defaultUrl.apply(getter, namedProperty, alias));
    }

    /** 返回 XA 或非 XA JDBC 驱动类名。 */
    /** 返回 XA 或非 XA JDBC 驱动类名。 */
    public static Optional<String> getDriver(String alias, boolean isXaEnabled) {
        return mapValue(alias, vendor -> isXaEnabled ? vendor.xaDriver : vendor.nonXaDriver);
    }

    /** 返回 Hibernate 方言类名。 */
    /** 返回 Hibernate 方言类名。 */
    public static Optional<String> getDialect(String alias) {
        return mapValue(alias, vendor -> vendor.dialect.apply(alias));
    }

    /** 解析别名后对 Vendor 应用映射函数。 */
    /** 解析别名后对 Vendor 应用映射函数。 */
    private static <T> Optional<T> mapValue(String alias, Function<Vendor, T> mapper) {
        return getVendor(alias).map(mapper);
    }

    /**
     * @return List of aliases of databases
 * @return 所有已注册数据库别名列表

     */
    public static List<String> getDatabaseAliases() {
        return DATABASES.keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /** 支持的数据库厂商枚举，含驱动、方言、默认 URL 与 Liquibase 类型。 */
    /** 支持的数据库厂商枚举，含驱动、方言、默认 URL 与 Liquibase 类型。 */
    public enum Vendor {
        H2("h2",
                "org.h2.jdbcx.JdbcDataSource",
                "org.h2.Driver",
                "org.hibernate.dialect.H2Dialect",
                new TriFunction<>() {
                    @Override
                    public String apply(Function<Option<?>, String> getter, String namedProperty, String alias) {
                        String url;
                        if ("dev-file".equalsIgnoreCase(alias)) {
                            var separator = escapeReplacements(File.separator);
                            url = new StringBuilder()
                                    .append("jdbc:h2:file:")
                                    .append("${kc.db-url-path:${kc.home.dir:%s}}".formatted(escapeReplacements(System.getProperty("user.home"))))
                                    .append(separator)
                                    .append("${kc.data.dir:data}")
                                    .append(separator)
                                    .append(getFolder(namedProperty))
                                    .append(separator)
                                    .append(getDbName(namedProperty))
                                    .toString();
                        } else {
                            url = "jdbc:h2:mem:%s".formatted(getDbName(namedProperty));
                        }
                        String urlProps = getProperty(DatabaseOptions.DB_URL_PROPERTIES, getter);
                        if (!urlProps.isEmpty()) {
                            url += urlProps;
                        }
                        return amendH2(url);
                    }

                    private String getFolder(String namedProperty) {
                        return StringUtil.isNullOrEmpty(namedProperty) ? "h2" : "h2-%s".formatted(namedProperty);
                    }

                    private String getDbName(String namedProperty) {
                        return StringUtil.isNullOrEmpty(namedProperty) ? "keycloakdb" : "keycloakdb-%s".formatted(namedProperty);
                    }

                    private String escapeReplacements(String snippet) {
                        if (File.separator.equals("\\")) {
                            // SmallRye 会替换 "${...}"，反斜杠不得转义该表达式；Windows 下将 \ 替换为 /
                            // As we nest multiple expressions, and each nested expression must re-escape the backslashes,
                            // the simplest way is to replace a backslash with a slash, as those are processed nicely on Windows.
                            return snippet.replace("\\", "/");
                        }
                        return snippet;
                    }

                },
                "liquibase.database.core.H2Database",
                "dev-mem", "dev-file"
        ),
        MYSQL("mysql",
                "com.mysql.cj.jdbc.MysqlXADataSource",
                "com.mysql.cj.jdbc.Driver",
                "org.hibernate.dialect.MySQLDialect",
                // default URL looks like this: "jdbc:mysql://${kc.db-url-host:localhost}:${kc.db-url-port:3306}/${kc.db-url-database:keycloak}${kc.db-url-properties:}"
                (getter, namedProperty, alias) -> "jdbc:mysql://%s:%s/%s%s".formatted(
                        getProperty(DatabaseOptions.DB_URL_HOST, getter, "localhost"),
                        getProperty(DatabaseOptions.DB_URL_PORT, getter, "3306"),
                        getProperty(DatabaseOptions.DB_URL_DATABASE, getter, "keycloak"),
                        getProperty(DatabaseOptions.DB_URL_PROPERTIES, getter)),
                "org.keycloak.connections.jpa.updater.liquibase.UpdatedMySqlDatabase"
        ),
        TIDB("tidb",
                "com.mysql.cj.jdbc.MysqlXADataSource",
                "com.mysql.cj.jdbc.Driver",
                "org.hibernate.community.dialect.TiDBDialect",
                // default URL looks like this: "jdbc:mysql://${kc.db-url-host:localhost}:${kc.db-url-port:3306}/${kc.db-url-database:keycloak}${kc.db-url-properties:}"
                (getter, namedProperty, alias) -> "jdbc:mysql://%s:%s/%s%s".formatted(
                        getProperty(DatabaseOptions.DB_URL_HOST, getter, "localhost"),
                        getProperty(DatabaseOptions.DB_URL_PORT, getter, "3306"),
                        getProperty(DatabaseOptions.DB_URL_DATABASE, getter, "keycloak"),
                        getProperty(DatabaseOptions.DB_URL_PROPERTIES, getter)),
                "org.keycloak.connections.jpa.updater.liquibase.UpdatedMySqlDatabase"
        ),
        MARIADB("mariadb",
                "org.mariadb.jdbc.MariaDbDataSource",
                "org.mariadb.jdbc.Driver",
                "org.hibernate.dialect.MariaDBDialect",
                // default URL looks like this: "jdbc:mariadb://${kc.db-url-host:localhost}:${kc.db-url-port:3306}/${kc.db-url-database:keycloak}${kc.db-url-properties:}"
                (getter, namedProperty, alias) -> "jdbc:mariadb://%s:%s/%s%s".formatted(
                        getProperty(DatabaseOptions.DB_URL_HOST, getter, "localhost"),
                        getProperty(DatabaseOptions.DB_URL_PORT, getter, "3306"),
                        getProperty(DatabaseOptions.DB_URL_DATABASE, getter, "keycloak"),
                        getProperty(DatabaseOptions.DB_URL_PROPERTIES, getter)),
                "org.keycloak.connections.jpa.updater.liquibase.UpdatedMariaDBDatabase"
        ),
        POSTGRES("postgresql",
                "org.postgresql.xa.PGXADataSource",
                "org.postgresql.Driver",
                "org.hibernate.dialect.PostgreSQLDialect",
                // default URL looks like this: "jdbc:postgresql://${kc.db-url-host:localhost}:${kc.db-url-port:5432}/${kc.db-url-database:keycloak}${kc.db-url-properties:}"
                (getter, namedProperty, alias) -> "jdbc:postgresql://%s:%s/%s%s".formatted(
                        getProperty(DatabaseOptions.DB_URL_HOST, getter, "localhost"),
                        getProperty(DatabaseOptions.DB_URL_PORT, getter, "5432"),
                        getProperty(DatabaseOptions.DB_URL_DATABASE, getter, "keycloak"),
                        getProperty(DatabaseOptions.DB_URL_PROPERTIES, getter)),
                "liquibase.database.core.PostgresDatabase",
                "postgres"
        ),
        MSSQL("mssql",
                "com.microsoft.sqlserver.jdbc.SQLServerXADataSource",
                "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                "org.hibernate.dialect.SQLServerDialect",
                // default URL looks like this: "jdbc:sqlserver://${kc.db-url-host:localhost}:${kc.db-url-port:1433};databaseName=${kc.db-url-database:keycloak}${kc.db-url-properties:}"
                (getter, namedProperty, alias) -> "jdbc:sqlserver://%s:%s;databaseName=%s%s".formatted(
                        getProperty(DatabaseOptions.DB_URL_HOST, getter, "localhost"),
                        getProperty(DatabaseOptions.DB_URL_PORT, getter, "1433"),
                        getProperty(DatabaseOptions.DB_URL_DATABASE, getter, "keycloak"),
                        getProperty(DatabaseOptions.DB_URL_PROPERTIES, getter)),
                "org.keycloak.quarkus.runtime.storage.database.liquibase.database.CustomMSSQLDatabase",
                "mssql"
        ),
        ORACLE("oracle",
                "oracle.jdbc.xa.client.OracleXADataSource",
                "oracle.jdbc.driver.OracleDriver",
                "org.hibernate.dialect.OracleDialect",
                // default URL looks like this: "jdbc:oracle:thin:@//${kc.db-url-host:localhost}:${kc.db-url-port:1521}/${kc.db-url-database:keycloak}${kc.db-url-properties:}"
                (getter, namedProperty, alias) -> "jdbc:oracle:thin:%s//%s:%s/%s%s".formatted(
                        DatabaseOptions.DatabaseTlsMode.fromCliValue(getProperty(DatabaseOptions.DB_TLS_MODE, getter,
                                DatabaseOptions.DatabaseTlsMode.DISABLED.toCliValue())) == DatabaseTlsMode.DISABLED ? "@" : "@tcps:",
                        getProperty(DatabaseOptions.DB_URL_HOST, getter, "localhost"),
                        getProperty(DatabaseOptions.DB_URL_PORT, getter, "1521"),
                        getProperty(DatabaseOptions.DB_URL_DATABASE, getter, "keycloak"),
                        getProperty(DatabaseOptions.DB_URL_PROPERTIES, getter)),
                "liquibase.database.core.OracleDatabase"
        );

        final String databaseKind;
        final String xaDriver;
        final String nonXaDriver;
        final Function<String, String> dialect;
        final TriFunction<Function<Option<?>, String>, String, String, String> defaultUrl;
        final String liquibaseType;
        final String[] aliases;

        Vendor(String databaseKind, String xaDriver, String nonXaDriver, String dialect, TriFunction<Function<Option<?>, String>, String, String, String> defaultUrl,
               String liquibaseType, String... aliases) {
            this(databaseKind, xaDriver, nonXaDriver, alias -> dialect, defaultUrl, liquibaseType, aliases);
        }

        Vendor(String databaseKind, String xaDriver, String nonXaDriver, Function<String, String> dialect, TriFunction<Function<Option<?>, String>, String, String, String> defaultUrl,
               String liquibaseType,
               String... aliases) {
            this.databaseKind = databaseKind;
            this.xaDriver = xaDriver;
            this.nonXaDriver = nonXaDriver;
            this.dialect = dialect;
            this.defaultUrl = defaultUrl;
            this.liquibaseType = liquibaseType;
            this.aliases = aliases.length == 0 ? new String[]{databaseKind} : aliases;
        }

        /** 判断是否与给定 Quarkus db-kind 匹配。 */
        /** 判断是否与给定 Quarkus db-kind 匹配。 */
        public boolean isOfKind(String dbKind) {
            return databaseKind.equals(dbKind);
        }

        private static String getProperty(Option<?> option, Function<Option<?>, String> getter) {
            return getProperty(option, getter, "");
        }

        private static String getProperty(Option<?> option, Function<Option<?>, String> getter, String defaultValue) {
            return Optional.ofNullable(getter.apply(option)).orElse(defaultValue);
        }

        /** @return Liquibase Database 实现类全限定名 */
        /** @return Liquibase Database 实现类全限定名 */
        public String getLiquibaseType() {
            return liquibaseType;
        }

        @Override
        public String toString() {
            return databaseKind.toLowerCase(Locale.ROOT);
        }
        
        /**
         * Starting with H2 version 2.x, marking "VALUE" as a non-keyword is necessary as some columns are named "VALUE" in the Keycloak schema.
 * 自 H2 2.x 起，须将 VALUE 标记为非关键字，因 Keycloak 模式中部分列名为 VALUE。

         * <p />
         * Alternatives considered and rejected:
         * <ul>
         * <li>customizing H2 Database dialect -&gt; wouldn't work for existing Liquibase scripts.</li>
         * <li>adding quotes to <code>@Column(name="VALUE")</code> annotations -&gt; would require testing for all DBs, wouldn't work for existing Liquibase scripts.</li>
         * </ul>
         * Downsides of this solution: Release notes needed to point out that any H2 JDBC URL parameter with <code>NON_KEYWORDS</code> needs to add the keyword <code>VALUE</code> manually.
         * @return JDBC URL with <code>NON_KEYWORDS=VALUE</code> appended if the URL doesn't contain <code>NON_KEYWORDS=</code> yet
         */
        private static String addH2NonKeywords(String jdbcUrl) {
            if (!jdbcUrl.contains("NON_KEYWORDS=")) {
                jdbcUrl = jdbcUrl + ";NON_KEYWORDS=VALUE";
            }
            return jdbcUrl;
        }
        
        /**
         * Required so that the H2 db instance is closed only when the Agroal connection pool is closed during
 * 确保 H2 实例仅在 Keycloak 关闭且 Agroal 连接池关闭后才关闭，

         * Keycloak shutdown. We cannot rely on the default H2 ShutdownHook as this can result in the DB being
         * closed before dependent resources, e.g. JDBC_PING2, are shutdown gracefully. This solution also
         * requires the Agroal min-pool connection size to be at least 1.
         */
        private static String addH2CloseOnExit(String jdbcUrl) {
            if (!jdbcUrl.contains("DB_CLOSE_ON_EXIT=")) {
                jdbcUrl = jdbcUrl + ";DB_CLOSE_ON_EXIT=FALSE";
            }
            if (!jdbcUrl.contains("DB_CLOSE_DELAY=")) {
                jdbcUrl = jdbcUrl + ";DB_CLOSE_DELAY=0";
            }
            return jdbcUrl;
        }

        private static String amendH2(String jdbcUrl) {
            return addH2CloseOnExit(addH2NonKeywords(jdbcUrl));
        }
    }
}
