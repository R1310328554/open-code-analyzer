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

package org.keycloak.quarkus.runtime;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.authentication.authenticators.browser.WebAuthnAuthenticatorMetadata;
import org.keycloak.authentication.authenticators.browser.WebAuthnMetadataService;
import org.keycloak.common.Profile;
import org.keycloak.common.Profile.Enablement;
import org.keycloak.common.Profile.Feature;
import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.crypto.CryptoProvider;
import org.keycloak.common.crypto.FipsMode;
import org.keycloak.config.DatabaseOptions;
import org.keycloak.config.HealthOptions;
import org.keycloak.config.HttpAccessLogOptions;
import org.keycloak.config.HttpOptions;
import org.keycloak.config.MetricsOptions;
import org.keycloak.config.OpenApiOptions;
import org.keycloak.config.TruststoreOptions;
import org.keycloak.marshalling.Marshalling;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;
import org.keycloak.quarkus.runtime.services.RejectNonNormalizedPathFilter;
import org.keycloak.quarkus.runtime.storage.database.liquibase.FastServiceLocator;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.theme.ClasspathThemeProviderFactory;
import org.keycloak.truststore.TruststoreBuilder;
import org.keycloak.userprofile.DeclarativeUserProfileProviderFactory;

import io.agroal.api.AgroalDataSource;
import io.quarkus.agroal.DataSource;
import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.hibernate.orm.runtime.integration.HibernateOrmIntegrationRuntimeInitListener;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import liquibase.Scope;
import liquibase.servicelocator.ServiceLocator;
import org.hibernate.cfg.AvailableSettings;
import org.infinispan.protostream.SerializationContextInitializer;

/**
 * Quarkus 构建时录制器：在运行阶段初始化 Keycloak 配置、Profile、信任库、会话工厂等。
 */
@Recorder
public class KeycloakRecorder {

    /** 绑定 Keycloak {@link Config} 到 MicroProfile 配置提供者。 */
    public void initConfig() {
        Config.init(new MicroProfileConfigProvider());
    }

    /** 若启用 HTTP 访问日志文件，则在数据目录下创建 log 子目录。 */
    public void createHttpAccessLogDirectory() {
        if (Configuration.isTrue(HttpAccessLogOptions.HTTP_ACCESS_LOG_FILE_ENABLED)) {
            Environment.getHomeDir().ifPresent(homeDir -> {
                File logDir = new File(homeDir, "data" + File.separator + "log");
                if (!logDir.exists() && !logDir.mkdirs() && !logDir.exists()) {
                    throw new RuntimeException("Failed to create HTTP Access log directory");
                }
            });
        }
    }

    /**
     * 初始化 Keycloak {@link Profile} 及特性开关。
     *
     * @param profileName Profile 名称
     * @param features 特性到布尔值的映射
     * @param enablements 特性启用级别映射
     */
    public void configureProfile(Profile.ProfileName profileName, Map<Profile.Feature, Boolean> features, Map<Feature, Enablement> enablements) {
        Profile.init(profileName, features, enablements);
    }

    /** 返回将请求重定向到指定路径的 Vert.x 处理器。 */
    public Handler<RoutingContext> getRedirectHandler(String redirectPath) {
        return routingContext -> routingContext.redirect(redirectPath);
    }

    /** 管理接口首页可链接的端点列表。 */
    private static final List<ManagementInterfaceItem> MANAGEMENT_INTERFACE_ENDPOINTS = List.of(
            new ManagementInterfaceItem("/health", "Health endpoint", () -> Configuration.isTrue(HealthOptions.HEALTH_ENABLED)),
            new ManagementInterfaceItem("/metrics", "Metrics endpoint", () -> Configuration.isTrue(MetricsOptions.METRICS_ENABLED)),
            new ManagementInterfaceItem("/openapi", "OpenAPI specification", () -> Configuration.isTrue(OpenApiOptions.OPENAPI_ENABLED)),
            new ManagementInterfaceItem("/openapi/ui", "OpenAPI UI specification (Swagger)", () -> Configuration.isTrue(OpenApiOptions.OPENAPI_UI_ENABLED))
    );

    /** 返回管理接口 HTML 索引页处理器。 */
    public Handler<RoutingContext> getManagementHandler() {
        String itemsHtml = "<ul>%s</ul>".formatted(MANAGEMENT_INTERFACE_ENDPOINTS.stream()
                .filter(f -> f.isEnabled.getAsBoolean())
                .map(ManagementInterfaceItem::getListItem)
                .collect(Collectors.joining("\n")));

        return routingContext -> routingContext.response().end("""
                <html>
                <h2>Keycloak Management Interface</h2>
                %s
                </html>
                """.formatted(itemsHtml));
    }

    /** 管理接口列表项：路径、描述与启用条件。 */
    private record ManagementInterfaceItem(String path, String description, BooleanSupplier isEnabled) {
        String getListItem() {
            return "<li><a href=\"%s\">%s</a> - %s</li>".formatted(path, path, description);
        }
    }

    /**
     * 若未接受非规范化路径，则返回拒绝过滤器；否则返回 null。
     */
    public Handler<RoutingContext> getRejectNonNormalizedPathFilter() {
        return !Configuration.isTrue(HttpOptions.HTTP_ACCEPT_NON_NORMALIZED_PATHS) ? new RejectNonNormalizedPathFilter() : null;
    }

    /** 聚合配置、Kubernetes CA 与 conf/truststores 目录，设置 JVM 信任库。 */
    public void configureTruststore() {
        List<String> truststores = new ArrayList<>();
        Configuration.getOptionalKcValue(TruststoreOptions.TRUSTSTORE_PATHS.getKey())
                .ifPresent(s -> Stream.of(s.split(",")).forEach(truststores::add));

        boolean includeKubernetesCa = Configuration.getOptionalKcValue(TruststoreOptions.TRUSTSTORE_KUBERNETES_CA_ENABLED.getKey())
                .map(Boolean::parseBoolean).orElse(true);
        if (includeKubernetesCa) {
            TruststoreBuilder.includeKubernetesTrustStorePaths(truststores);
        }

        Optional<String> dataDir = Environment.getDataDir();

        File truststoresDir = Environment.getHomePath().map(p -> p.resolve("conf").resolve("truststores").toFile()).orElse(null);

        if (truststoresDir != null && truststoresDir.exists() && Optional.ofNullable(truststoresDir.list()).map(a -> a.length).orElse(0) > 0) {
            truststores.add(truststoresDir.getAbsolutePath());
        } else if (truststores.size() == 0) {
            return; // 无自定义信任库，使用系统默认
        }

        TruststoreBuilder.setSystemTruststore(truststores.toArray(String[]::new), true, dataDir.orElseThrow());
    }

    /**
     * 向 FastServiceLocator 注入预解析的 Liquibase 服务映射。
     *
     * @param services Liquibase 服务类到实现类列表的映射
     */
    public void configureLiquibase(Map<String, List<String>> services) {
        ServiceLocator locator = Scope.getCurrentScope().getServiceLocator();
        if (locator instanceof FastServiceLocator) {
            ((FastServiceLocator) locator).initServices(services);
        }
    }

    /**
     * 创建 Quarkus 版 Keycloak 会话工厂 RuntimeValue。
     */
    public RuntimeValue<QuarkusKeycloakSessionFactory> createSessionFactory(
            Map<Spi, Map<Class<? extends Provider>, Map<String, Class<? extends ProviderFactory>>>> factories,
            Map<Class<? extends Provider>, String> defaultProviders,
            Map<String, ProviderFactory> preConfiguredProviders,
            List<ClasspathThemeProviderFactory.ThemesRepresentation> themes) {
        return new RuntimeValue<QuarkusKeycloakSessionFactory>(new QuarkusKeycloakSessionFactory(factories, defaultProviders, preConfiguredProviders, themes));
    }

    /** 设置声明式用户 Profile 的默认 UP 配置。 */
    public void setDefaultUserProfileConfiguration(UPConfig configuration) {
        DeclarativeUserProfileProviderFactory.setDefaultConfig(configuration);
    }

    /** 设置 WebAuthn 认证器默认元数据。 */
    public void setDefaultWebAuthnMetadata(Map<String, WebAuthnAuthenticatorMetadata> metadata) {
        WebAuthnMetadataService.setDefaultMetadata(metadata);
    }


    /**
     * 为命名 Hibernate 持久化单元创建数据源绑定监听器。
     *
     * @param name Quarkus 数据源名称
     */
    public HibernateOrmIntegrationRuntimeInitListener createUserDefinedUnitListener(String name) {
        return propertyCollector -> {
            try (InstanceHandle<AgroalDataSource> instance = Arc.container().instance(
                    AgroalDataSource.class, new DataSource() {
                        @Override public Class<? extends Annotation> annotationType() {
                            return DataSource.class;
                        }

                        @Override public String value() {
                            return name;
                        }
                    })) {
                propertyCollector.accept(AvailableSettings.DATASOURCE, instance.get());
            }
        };
    }

    /** 为默认持久化单元设置数据库 schema。 */
    public HibernateOrmIntegrationRuntimeInitListener createDefaultUnitListener() {
        return propertyCollector -> propertyCollector.accept(AvailableSettings.DEFAULT_SCHEMA, Configuration.getConfigValue(DatabaseOptions.DB_SCHEMA).getValue());
    }

    /**
     * 按 FIPS 模式加载并注册 CryptoProvider。
     *
     * @param fipsMode FIPS 运行模式
     */
    public void setCryptoProvider(FipsMode fipsMode) {
        String cryptoProvider = fipsMode.getProviderClassName();

        try {
            CryptoIntegration.setProvider(
                    (CryptoProvider) Thread.currentThread().getContextClassLoader().loadClass(cryptoProvider).getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException | NoClassDefFoundError cause) {
            if (fipsMode.isFipsEnabled()) {
                throw new RuntimeException("Failed to configure FIPS. Make sure you have added the Bouncy Castle FIPS dependencies to the 'providers' directory.");
            }
            throw new RuntimeException("Unexpected error when configuring the crypto provider: " + cryptoProvider, cause);
        } catch (Exception cause) {
            throw new RuntimeException("Unexpected error when configuring the crypto provider: " + cryptoProvider, cause);
        }
    }

    /** 注册 Infinispan ProtoStream 序列化 schema 初始化器列表。 */
    public void configureProtoStreamSchemas(List<SerializationContextInitializer> schemas) {
        Marshalling.setSchemas(schemas);
    }
}
