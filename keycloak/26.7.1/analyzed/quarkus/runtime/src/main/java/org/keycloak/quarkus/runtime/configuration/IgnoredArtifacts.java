/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.quarkus.runtime.configuration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.common.Profile;
import org.keycloak.config.HealthOptions;
import org.keycloak.config.MetricsOptions;
import org.keycloak.config.OpenApiOptions;
import org.keycloak.config.TelemetryOptions;
import org.keycloak.config.database.Database;

import static java.util.Collections.emptySet;

import static org.keycloak.quarkus.runtime.Environment.getCurrentOrCreateFeatureProfile;

/**
 * 根据构建配置计算应从 Quarkus 类路径移除的 artifact 集合。
 * <p>
 * 在 {@link org.keycloak.quarkus.runtime.cli.command.Build} 阶段通过
 * {@code quarkus.class-loading.removed-artifacts} 排除未启用特性对应的依赖，
 * 以缩小优化镜像体积。
 */
public class IgnoredArtifacts {

    /** 汇总各特性模块的默认忽略 artifact 集合。 */
    public static Set<String> getDefaultIgnoredArtifacts() {
        return Stream.of(
                        fips(),
                        jdbcDrivers(),
                        health(),
                        metrics(),
                        otelMetrics(),
                        openApi(),
                        openApiSwagger(),
                        hibernateValidator()
                )
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    // FIPS 密码学实现
    /** FIPS 启用时需移除的非 FIPS BouncyCastle 与默认 crypto 模块。 */
    public static final Set<String> FIPS_ENABLED = Set.of(
            "org.bouncycastle:bcprov-jdk18on",
            "org.bouncycastle:bcpkix-jdk18on",
            "org.bouncycastle:bcutil-jdk18on",
            "org.keycloak:keycloak-crypto-default"
    );

    /** FIPS 禁用时需移除的 FIPS1402 与 FIPS BouncyCastle 模块。 */
    public static final Set<String> FIPS_DISABLED = Set.of(
            "org.keycloak:keycloak-crypto-fips1402",
            "org.bouncycastle:bc-fips",
            "org.bouncycastle:bctls-fips",
            "org.bouncycastle:bcpkix-fips",
            "org.bouncycastle:bcutil-fips"
    );

    /** 按当前 Profile 的 FIPS 特性开关返回应忽略的 crypto artifact。 */
    private static Set<String> fips() {
        final Profile profile = getCurrentOrCreateFeatureProfile();
        boolean isFipsEnabled = profile.getFeatures().get(Profile.Feature.FIPS);

        return isFipsEnabled ? FIPS_ENABLED : FIPS_DISABLED;
    }

    // JDBC 驱动
    /** H2 数据库相关 Quarkus 与驱动 artifact。 */
    public static final Set<String> JDBC_H2 = Set.of(
            "io.quarkus:quarkus-jdbc-h2",
            "io.quarkus:quarkus-jdbc-h2-deployment",
            "com.h2database:h2",
            "org.locationtech.jts:jts-core"
    );

    public static final Set<String> JDBC_POSTGRES = Set.of(
            "io.quarkus:quarkus-jdbc-postgresql",
            "io.quarkus:quarkus-jdbc-postgresql-deployment",
            "org.postgresql:postgresql"
    );

    public static final Set<String> JDBC_MARIADB = Set.of(
            "io.quarkus:quarkus-jdbc-mariadb",
            "io.quarkus:quarkus-jdbc-mariadb-deployment",
            "org.mariadb.jdbc:mariadb-java-client"
    );

    public static final Set<String> JDBC_MYSQL = Set.of(
            "io.quarkus:quarkus-jdbc-mysql",
            "io.quarkus:quarkus-jdbc-mysql-deployment",
            "com.mysql:mysql-connector-j"
    );

    public static final Set<String> JDBC_MSSQL = Set.of(
            "io.quarkus:quarkus-jdbc-mssql",
            "io.quarkus:quarkus-jdbc-mssql-deployment",
            "com.microsoft.sqlserver:mssql-jdbc"
    );

    public static final Set<String> JDBC_ORACLE = Set.of(
            "io.quarkus:quarkus-jdbc-oracle",
            "io.quarkus:quarkus-jdbc-oracle-deployment",
            "com.oracle.database.jdbc:ojdbc17",
            "com.oracle.database.nls:orai18n"
    );

    /** 全部 JDBC 驱动 artifact 的并集。 */
    public static final Set<String> JDBC_DRIVERS = Stream.of(
                    JDBC_H2,
                    JDBC_POSTGRES,
                    JDBC_MARIADB,
                    JDBC_MYSQL,
                    JDBC_MSSQL,
                    JDBC_ORACLE
            )
            .flatMap(Collection::stream)
            .collect(Collectors.toUnmodifiableSet());

    /** 移除当前未配置的数据源厂商对应的 JDBC 驱动 artifact。 */
    private static Set<String> jdbcDrivers() {
        final Set<Database.Vendor> vendorsOfAllDatasources = new HashSet<>();

        Configuration.getConfig().getPropertyNames().forEach(p -> {
            if (p.startsWith("quarkus.datasource.") && p.endsWith(".db-kind")) {
                Configuration.getOptionalValue(p)
                        .flatMap(Database::getVendor)
                        .ifPresent(vendorsOfAllDatasources::add);
            }
        });

        final Set<String> jdbcArtifacts = vendorsOfAllDatasources.stream()
                .map(vendor -> switch (vendor) {
                    case H2 -> JDBC_H2;
                    case MYSQL, TIDB -> JDBC_MYSQL;
                    case MARIADB -> JDBC_MARIADB;
                    case POSTGRES -> JDBC_POSTGRES;
                    case MSSQL -> JDBC_MSSQL;
                    case ORACLE -> JDBC_ORACLE;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        final Set<String> allJdbcDrivers = new HashSet<>(JDBC_DRIVERS);
        allJdbcDrivers.removeAll(jdbcArtifacts);
        return allJdbcDrivers;
    }

    // 健康检查
    public static final Set<String> HEALTH = Set.of(
            "io.quarkus:quarkus-smallrye-health",
            "io.quarkus:quarkus-smallrye-health-deployment"
    );

    /** 健康端点未启用时移除 SmallRye Health 相关 artifact。 */
    private static Set<String> health() {
        boolean isHealthEnabled = Configuration.isTrue(HealthOptions.HEALTH_ENABLED);
        return !isHealthEnabled ? HEALTH : emptySet();
    }

    // 指标
    public static Set<String> METRICS = Set.of(
            "io.quarkus:quarkus-micrometer",
            "io.quarkus:quarkus-micrometer-deployment",
            "io.quarkus:quarkus-micrometer-registry-prometheus",
            "io.quarkus:quarkus-micrometer-registry-prometheus-deployment"
    );

    /** Micrometer 指标未启用时移除相关 artifact。 */
    private static Set<String> metrics() {
        boolean isMetricsEnabled = Configuration.isTrue(MetricsOptions.METRICS_ENABLED);
        return !isMetricsEnabled ? METRICS : emptySet();
    }

    // OpenTelemetry 指标（Micrometer → OTel 桥接）
    public static Set<String> OTEL_METRICS = Set.of(
            "io.quarkus:quarkus-micrometer-opentelemetry",
            "io.quarkus:quarkus-micrometer-opentelemetry-deployment",
            "io.opentelemetry.instrumentation:opentelemetry-micrometer-1.5"
    );

    /** OTel 指标导出未启用时移除 Micrometer-OTel 桥接 artifact。 */
    private static Set<String> otelMetrics() {
        boolean isOtelMetricsEnabled = Configuration.isTrue(TelemetryOptions.TELEMETRY_METRICS_ENABLED);
        return !isOtelMetricsEnabled ? OTEL_METRICS : emptySet();
    }

    // OpenAPI
    public static Set<String> OPENAPI = Set.of(
            "io.quarkus:quarkus-smallrye-openapi",
            "io.quarkus:quarkus-smallrye-openapi-deployment",
            "io.smallrye:smallrye-open-api-core"
    );

    /** OpenAPI 未启用时移除 SmallRye OpenAPI 核心 artifact。 */
    private static Set<String> openApi() {
        boolean isEnabled = Configuration.isTrue(OpenApiOptions.OPENAPI_ENABLED);
        return !isEnabled ? OPENAPI : emptySet();
    }

    // OpenAPI UI（Swagger）
    public static Set<String> OPENAPI_SWAGGER = Set.of(
            "io.quarkus:quarkus-swagger-ui",
            "io.quarkus:quarkus-swagger-ui-deployment",
            "io.smallrye:smallrye-open-api-ui"
    );

    /** Swagger UI 未启用时移除 UI 相关 artifact。 */
    private static Set<String> openApiSwagger() {
        boolean isEnabled = Configuration.isTrue(OpenApiOptions.OPENAPI_UI_ENABLED);
        return !isEnabled ? OPENAPI_SWAGGER : emptySet();
    }

    // Hibernate Validator
    public static Set<String> HIBERNATE_VALIDATOR = Set.of(
            "io.quarkus:quarkus-hibernate-validator",
            "io.quarkus:quarkus-hibernate-validator-deployment",
            "io.quarkus:quarkus-hibernate-validator-spi",
            "org.hibernate.validator:hibernate-validator"
    );

    /** Client Admin API v2 未启用时移除 Hibernate Validator 相关 artifact。 */
    private static Set<String> hibernateValidator() {
        boolean isEnabled = Profile.isFeatureEnabled(Profile.Feature.CLIENT_ADMIN_API_V2);
        return !isEnabled ? HIBERNATE_VALIDATOR : emptySet();
    }
}
