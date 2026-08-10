/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.deployment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Handler;

import jakarta.inject.Singleton;
import jakarta.persistence.Entity;
import jakarta.persistence.PersistenceUnitTransactionType;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorSpi;
import org.keycloak.authentication.authenticators.browser.DeployedScriptAuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.WebAuthnMetadataService;
import org.keycloak.authorization.policy.provider.PolicySpi;
import org.keycloak.authorization.policy.provider.js.DeployedScriptPolicyFactory;
import org.keycloak.common.Profile;
import org.keycloak.common.crypto.FipsMode;
import org.keycloak.common.util.MultiSiteUtils;
import org.keycloak.common.util.StreamUtil;
import org.keycloak.config.DatabaseOptions;
import org.keycloak.config.HealthOptions;
import org.keycloak.config.HttpOptions;
import org.keycloak.config.LoggingOptions;
import org.keycloak.config.ManagementOptions;
import org.keycloak.config.MetricsOptions;
import org.keycloak.config.SecurityOptions;
import org.keycloak.config.TracingOptions;
import org.keycloak.config.TransactionOptions;
import org.keycloak.config.database.Database;
import org.keycloak.connections.jpa.DefaultJpaConnectionProviderFactory;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.connections.jpa.JpaConnectionSpi;
import org.keycloak.connections.jpa.updater.liquibase.LiquibaseJpaUpdaterProviderFactory;
import org.keycloak.connections.jpa.updater.liquibase.conn.DefaultLiquibaseConnectionProvider;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.policy.DenylistPasswordPolicyProviderFactory;
import org.keycloak.protocol.ProtocolMapperSpi;
import org.keycloak.protocol.oidc.mappers.DeployedScriptOIDCProtocolMapper;
import org.keycloak.protocol.saml.mappers.DeployedScriptSAMLProtocolMapper;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.ProviderManager;
import org.keycloak.provider.Spi;
import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.KeycloakRecorder;
import org.keycloak.quarkus.runtime.cli.Picocli;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.KeycloakConfigSourceProvider;
import org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider;
import org.keycloak.quarkus.runtime.configuration.PersistedConfigSource;
import org.keycloak.quarkus.runtime.configuration.PropertyMappingInterceptor;
import org.keycloak.quarkus.runtime.configuration.mappers.DatabasePropertyMappers;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMappers;
import org.keycloak.quarkus.runtime.configuration.mappers.WildcardPropertyMapper;
import org.keycloak.quarkus.runtime.integration.QuarkusKeycloakSessionFactory;
import org.keycloak.quarkus.runtime.integration.resteasy.KeycloakHandlerChainCustomizer;
import org.keycloak.quarkus.runtime.integration.resteasy.KeycloakTracingCustomizer;
import org.keycloak.quarkus.runtime.logging.ClearMappedDiagnosticContextFilter;
import org.keycloak.quarkus.runtime.services.RejectSourceMapFilter;
import org.keycloak.quarkus.runtime.services.health.BootstrapReadyHealthCheck;
import org.keycloak.quarkus.runtime.services.health.KeycloakClusterReadyHealthCheck;
import org.keycloak.quarkus.runtime.services.health.KeycloakReadyHealthCheck;
import org.keycloak.quarkus.runtime.storage.database.jpa.NamedJpaConnectionProviderFactory;
import org.keycloak.quarkus.runtime.themes.FlatClasspathThemeResourceProviderFactory;
import org.keycloak.representations.provider.ScriptProviderDescriptor;
import org.keycloak.representations.provider.ScriptProviderMetadata;
import org.keycloak.services.DefaultKeycloakSessionFactory;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.resources.LoadBalancerResource;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.theme.ClasspathThemeProviderFactory;
import org.keycloak.theme.ClasspathThemeResourceProviderFactory;
import org.keycloak.theme.FolderThemeProviderFactory;
import org.keycloak.theme.JarThemeProviderFactory;
import org.keycloak.theme.ThemeResourceSpi;
import org.keycloak.transaction.JBossJtaTransactionManagerLookup;
import org.keycloak.userprofile.config.UPConfigUtils;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.StringUtil;
import org.keycloak.vault.FilesKeystoreVaultProviderFactory;
import org.keycloak.vault.FilesPlainTextVaultProviderFactory;

import io.quarkus.agroal.runtime.DataSourcesJdbcBuildTimeConfig;
import io.quarkus.agroal.runtime.TransactionIntegration;
import io.quarkus.agroal.runtime.health.DataSourceHealthCheck;
import io.quarkus.agroal.spi.JdbcDataSourceBuildItem;
import io.quarkus.agroal.spi.JdbcDriverBuildItem;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.BuildTimeConditionBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.bootstrap.logging.InitialConfigurator;
import io.quarkus.datasource.runtime.DataSourcesBuildTimeConfig;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Produce;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.HotDeploymentWatchedFileBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.StaticInitConfigBuilderBuildItem;
import io.quarkus.hibernate.orm.deployment.HibernateOrmConfig;
import io.quarkus.hibernate.orm.deployment.PersistenceXmlDescriptorBuildItem;
import io.quarkus.hibernate.orm.deployment.integration.HibernateOrmIntegrationRuntimeConfiguredBuildItem;
import io.quarkus.hibernate.orm.deployment.spi.AdditionalJpaModelBuildItem;
import io.quarkus.narayana.jta.runtime.TransactionManagerBuildTimeConfig;
import io.quarkus.narayana.jta.runtime.TransactionManagerBuildTimeConfig.UnsafeMultipleLastResourcesMode;
import io.quarkus.resteasy.reactive.server.spi.MethodScannerBuildItem;
import io.quarkus.resteasy.reactive.server.spi.PreExceptionMapperHandlerBuildItem;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigurationException;
import io.quarkus.vertx.http.deployment.FilterBuildItem;
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;
import io.quarkus.vertx.http.deployment.ManagementInterfaceFilterBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.runtime.security.SecurityHandlerPriorities;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.health.Readiness;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.JdbcSettings;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.boot.spi.PersistenceXmlParser;
import org.infinispan.protostream.SerializationContextInitializer;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTransformation;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.model.HandlerChainCustomizer;
import org.jboss.resteasy.reactive.server.processor.scanning.MethodScanner;

import static org.keycloak.config.DatabaseOptions.DB;
import static org.keycloak.connections.jpa.util.JpaUtils.loadSpecificNamedQueries;
import static org.keycloak.quarkus.runtime.Environment.getCurrentOrCreateFeatureProfile;
import static org.keycloak.quarkus.runtime.Providers.getProviderManager;
import static org.keycloak.quarkus.runtime.configuration.Configuration.getOptionalBooleanKcValue;
import static org.keycloak.quarkus.runtime.configuration.Configuration.getOptionalKcValue;
import static org.keycloak.quarkus.runtime.configuration.Configuration.getOptionalValue;
import static org.keycloak.quarkus.runtime.configuration.MicroProfileConfigProvider.NS_KEYCLOAK_PREFIX;
import static org.keycloak.quarkus.runtime.storage.database.jpa.QuarkusJpaConnectionProviderFactory.DEFAULT_PERSISTENCE_UNIT;
import static org.keycloak.quarkus.runtime.storage.database.jpa.QuarkusJpaConnectionProviderFactory.QUERY_PROPERTY_PREFIX;
import static org.keycloak.representations.provider.ScriptProviderDescriptor.AUTHENTICATORS;
import static org.keycloak.representations.provider.ScriptProviderDescriptor.MAPPERS;
import static org.keycloak.representations.provider.ScriptProviderDescriptor.POLICIES;
import static org.keycloak.representations.provider.ScriptProviderDescriptor.SAML_MAPPERS;
import static org.keycloak.theme.ClasspathThemeProviderFactory.KEYCLOAK_THEMES_JSON;

/**
 * Keycloak Quarkus 扩展核心构建处理器：配置、持久化、Provider、健康检查与 REST 集成。
 */
/**
 * Keycloak Quarkus 扩展核心构建处理器：配置、持久化、Provider、健康检查与 REST 集成。
 */
class KeycloakProcessor {

    private static final Logger logger = Logger.getLogger(KeycloakProcessor.class);

    /** JAR URL 中路径与条目分隔符。 */
    /** JAR URL 中路径与条目分隔符。 */
    private static final String JAR_FILE_SEPARATOR = "!/";
    /** 可部署脚本 Provider 类型到注册函数的映射。 */
    /** 可部署脚本 Provider 类型到注册函数的映射。 */
    private static final Map<String, Function<ScriptProviderMetadata, ProviderFactory>> DEPLOYEABLE_SCRIPT_PROVIDERS = new HashMap<>();
    /** 脚本 Provider 描述符在 classpath 中的路径。 */
    /** 脚本 Provider 描述符在 classpath 中的路径。 */
    private static final String KEYCLOAK_SCRIPTS_JSON_PATH = "META-INF/keycloak-scripts.json";

    /** 构建时跳过预加载的 ProviderFactory 类型。 */
    /** 构建时跳过预加载的 ProviderFactory 类型。 */
    private static final List<Class<? extends ProviderFactory>> IGNORED_PROVIDER_FACTORY = List.of(
            JBossJtaTransactionManagerLookup.class,
            DefaultJpaConnectionProviderFactory.class,
            DefaultLiquibaseConnectionProvider.class,
            FolderThemeProviderFactory.class,
            LiquibaseJpaUpdaterProviderFactory.class,
            FilesKeystoreVaultProviderFactory.class,
            FilesPlainTextVaultProviderFactory.class,
            DenylistPasswordPolicyProviderFactory.class,
            ClasspathThemeResourceProviderFactory.class,
            JarThemeProviderFactory.class);

    static {
        DEPLOYEABLE_SCRIPT_PROVIDERS.put(AUTHENTICATORS, KeycloakProcessor::registerScriptAuthenticator);
        DEPLOYEABLE_SCRIPT_PROVIDERS.put(POLICIES, KeycloakProcessor::registerScriptPolicy);
        DEPLOYEABLE_SCRIPT_PROVIDERS.put(MAPPERS, KeycloakProcessor::registerScriptMapper);
        DEPLOYEABLE_SCRIPT_PROVIDERS.put(SAML_MAPPERS, KeycloakProcessor::registerSAMLScriptMapper);
    }

    /** 注册脚本认证器 ProviderFactory。 */
    private static ProviderFactory registerScriptAuthenticator(ScriptProviderMetadata metadata) {
        return new DeployedScriptAuthenticatorFactory(metadata);
    }

    /** 注册脚本策略 ProviderFactory。 */
    private static ProviderFactory registerScriptPolicy(ScriptProviderMetadata metadata) {
        return new DeployedScriptPolicyFactory(metadata);
    }

    /** 注册 OIDC 脚本协议映射器 ProviderFactory。 */
    private static ProviderFactory registerScriptMapper(ScriptProviderMetadata metadata) {
        return new DeployedScriptOIDCProtocolMapper(metadata);
    }

    /** 注册 SAML 脚本协议映射器 ProviderFactory。 */
    private static ProviderFactory registerSAMLScriptMapper(ScriptProviderMetadata metadata) {
        return new DeployedScriptSAMLProtocolMapper(metadata);
    }

    /** 注册 Keycloak Quarkus 功能特性。 */
    @BuildStep
    FeatureBuildItem getFeature() {
        return new FeatureBuildItem("keycloak");
    }

    /**
     * Initialize configuration in runtime during the runtime initialization.
 * 在静态初始化阶段初始化 Keycloak 配置。

     */
    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    @Produce(ConfigBuildItem.class)
    /** 静态初始化 Keycloak 配置并产出 {@link ConfigBuildItem}。 */
    void initConfig(KeycloakRecorder recorder) {
        Config.init(new MicroProfileConfigProvider());
        recorder.initConfig();
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    @Consume(ConfigBuildItem.class)
    /** 创建 HTTP 访问日志目录。 */
    void createHttpAccessLogDirectory(KeycloakRecorder recorder) {
        recorder.createHttpAccessLogDirectory();
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    @Consume(ConfigBuildItem.class)
    @Produce(ProfileBuildItem.class)
    /** 解析并录制 Keycloak Profile 与特性开关。 */
    void configureProfile(KeycloakRecorder recorder) {
        Profile profile = getCurrentOrCreateFeatureProfile();
        Profile.getInstance().logUnsupportedFeatures();
        // 录制特性列表，避免运行时重复计算
        recorder.configureProfile(profile.getName(), profile.getFeatures(), profile.getEnablements());
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    @Consume(ConfigBuildItem.class)
    /** 当 HTTP 相对路径非根时，为 / 配置重定向。 */
    void configureRedirectForRootPath(BuildProducer<RouteBuildItem> routes,
                                      HttpRootPathBuildItem httpRootPathBuildItem,
                                      KeycloakRecorder recorder) {
        Configuration.getOptionalKcValue(HttpOptions.HTTP_RELATIVE_PATH)
                .filter(StringUtil::isNotBlank)
                .filter(f -> !f.equals("/"))
                .ifPresent(relativePath ->
                        routes.produce(httpRootPathBuildItem.routeBuilder()
                                .route("/")
                                .handler(recorder.getRedirectHandler(relativePath))
                                .build())
                );
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    @Consume(ConfigBuildItem.class)
    /** 注册拒绝非规范化路径的请求过滤器。 */
    void filterAllRequests(BuildProducer<FilterBuildItem> filters, KeycloakRecorder recorder) {
        var filter = recorder.getRejectNonNormalizedPathFilter();
        if (filter != null) {
            filters.produce(new FilterBuildItem(filter, SecurityHandlerPriorities.CORS + 1));
        }
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep(onlyIf = IsManagementEnabled.class)
    @Consume(ConfigBuildItem.class)
    /** 为管理接口注册路径规范化过滤器。 */
    void filterAllManagementRequests(BuildProducer<ManagementInterfaceFilterBuildItem> filters, KeycloakRecorder recorder) {
        var filter = recorder.getRejectNonNormalizedPathFilter();
        if (filter != null) {
            filters.produce(new ManagementInterfaceFilterBuildItem(filter, SecurityHandlerPriorities.CORS + 1));
        }
    }

    @BuildStep(onlyIfNot = IsKeycloakDevMode.class)
    /** 非开发模式下拒绝 source map 请求。 */
    void filterSourceMapRequests(BuildProducer<FilterBuildItem> filters) {
        filters.produce(new FilterBuildItem(new RejectSourceMapFilter(), SecurityHandlerPriorities.CORS + 1));
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep(onlyIf = IsManagementEnabled.class)
    @Consume(ConfigBuildItem.class)
    /** 配置管理接口根路径与处理器。 */
    void configureManagementInterface(BuildProducer<RouteBuildItem> routes,
                                      NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
                                      KeycloakRecorder recorder) {
        final var relativePath = Configuration.getOptionalKcValue(ManagementOptions.HTTP_MANAGEMENT_RELATIVE_PATH).orElse("/");

        if (StringUtil.isNotBlank(relativePath) && !relativePath.equals("/")) {
            // 将管理根路径 / 重定向到配置的相对路径
            routes.produce(nonApplicationRootPathBuildItem.routeBuilder()
                    .management()
                    .route("/")
                    .handler(recorder.getRedirectHandler(relativePath))
                    .build());
        }

        routes.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .management()
                .route(relativePath)
                .handler(recorder.getManagementHandler())
                .build());
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    @Consume(ConfigBuildItem.class)
    @Consume(CryptoProviderInitBuildItem.class) // ensures the Providers are loaded prior to handle the keystore #49359
    /** 配置信任库（须在 Crypto Provider 初始化之后）。 */
    void configureTruststore(KeycloakRecorder recorder) {
        recorder.configureTruststore();
    }

    /**
     * Check whether JDBC driver is present for the specified DB
 * 校验指定数据库的 JDBC 驱动是否存在于 classpath。

     *
     * @param ignore used for changing build items execution order with regards to AgroalProcessor
 * @param ignore 用于与 AgroalProcessor 协调构建项执行顺序

     */
    @BuildStep
    @Produce(CheckJdbcBuildStep.class)
    /** 校验 JDBC 驱动类是否可加载。 */
    void checkJdbcDriver(BuildProducer<JdbcDriverBuildItem> ignore) {
        final Optional<String> dbDriver = Configuration.getOptionalValue("quarkus.datasource.jdbc.driver");

        if (dbDriver.isPresent()) {
            try {
                // 仅验证类存在，不初始化 JDBC 驱动
                Class.forName(dbDriver.get(), false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                throwConfigError(String.format("Unable to find the JDBC driver (%s). You need to install it.", dbDriver.get()));
            }
        }
    }

    // 参考 AgroalProcessor：校验多数据源 XA 配置
    @BuildStep
    @Produce(CheckMultipleDatasourcesBuildStep.class)
    /** 校验多数据源场景下 LRCO 所需的 XA 事务配置。 */
    void checkMultipleDatasourcesUseXA(TransactionManagerBuildTimeConfig transactionManagerConfig, DataSourcesBuildTimeConfig dataSourcesConfig, DataSourcesJdbcBuildTimeConfig jdbcConfig) {
        Set<String> datasources = dataSourcesConfig.dataSources().keySet();
        if (datasources.size() > 1) {
            logger.infof("Multiple datasources are specified: %s", String.join(", ", datasources));
        }

        if (transactionManagerConfig.unsafeMultipleLastResources()
                .orElse(UnsafeMultipleLastResourcesMode.DEFAULT) != UnsafeMultipleLastResourcesMode.FAIL) {
            return;
        }

        List<String> nonXADatasources = datasources.stream()
                .filter(ds -> !Configuration.isKcPropertyTrue(TransactionOptions.getNamedTxXADatasource(ds)))
                .filter(ds -> {
                    var jdbc = jdbcConfig.dataSources().get(ds).jdbc();
                    return jdbc.enabled() && jdbc.transactions() != TransactionIntegration.XA;
                })
                .toList();

        if (nonXADatasources.size() > 1) {
            throwConfigError("Multiple datasources are configured but more than 1 (%s) is using non-XA transactions. ".formatted(String.join(", ", nonXADatasources)) +
                    "All the datasources except one must must be XA to be able to use Last Resource Commit Optimization (LRCO). " +
                    "Please update your configuration by setting --transaction-xa-enabled=true " +
                    "and/or --transaction-xa-enabled-<your-datasource-name>=true.");
        }
    }

    /** 清空延迟日志处理器并抛出 Quarkus 配置异常。 */
    private void throwConfigError(String msg) {
        // 忽略尚未初始化的日志处理器所积压的 TRACE/DEBUG 消息
        InitialConfigurator.DELAYED_HANDLER.setBuildTimeHandlers(new Handler[]{});
        throw new ConfigurationException(msg);
    }

    @BuildStep
    @Consume(ProfileBuildItem.class)
    @Produce(ValidatePersistenceUnitsBuildItem.class)
    /** 校验 TiDB 特性与命名数据源的 db-kind 配置。 */
    void checkPersistenceUnits(List<PersistenceXmlDescriptorBuildItem> descriptors) {
        if (Database.Vendor.TIDB.isOfKind(Configuration.getConfigValue(DB).getValue())) {
            if (!Profile.isFeatureEnabled(Profile.Feature.DB_TIDB)){
                throw new RuntimeException("The feature TiDB is not enabled");
            }
        }

        List<String> notSetPersistenceUnitsDBKinds = descriptors.stream()
                .map(PersistenceXmlDescriptorBuildItem::getDescriptor)
                .filter(descriptor -> !descriptor.getName().equals(DEFAULT_PERSISTENCE_UNIT)) // not default persistence unit
                .map(KeycloakProcessor::getDatasourceNameFromPersistenceXml)
                .filter(this::missingDbKind)
                .map(datasourceName -> PropertyMappers.getWildcardPropertyMapper(DatabaseOptions.DB_KIND).orElseThrow().getFrom(datasourceName)).toList();

        if (!notSetPersistenceUnitsDBKinds.isEmpty()) {
            throwConfigError("Detected additional named datasources without a DB kind set, please specify: %s".formatted(String.join(",", notSetPersistenceUnitsDBKinds)));
        }
    }

    /**
     * Try to find if DB kind is specified for the descriptor name.
 * 检查持久化单元对应的数据源是否已配置 db-kind。

     * <p>
     * Check it in order:
     * <ol>
     * <li> {@code db-kind-<descriptorName}
     * <li> {@code quarkus.datasource."<descriptorName>".db-kind}
     * <li> {@code quarkus.datasource.<descriptorName>.db-kind}
     * </ol>
     */
    /** 判断命名数据源是否缺少 db-kind 配置。 */
    private boolean missingDbKind(String datasourceName) {
        PropertyMappingInterceptor.disable();
        try {
            var from = DatabasePropertyMappers.getDatasourceOptionValue(DB, datasourceName);

            if (from.isPresent()) {
                return false; // user has directly specified
            }

            WildcardPropertyMapper<?> mapper = PropertyMappers.getWildcardPropertyMapper(DatabaseOptions.DB_KIND).orElseThrow();

            // quarkus properties
            boolean missing = Configuration.getOptionalValue(mapper.getTo(datasourceName))
                    .or(() -> Configuration.getOptionalValue(mapper.getTo(datasourceName).replaceAll("\"", "")))
                    .isEmpty();

            if (!missing) {
                logger.warnf(
                        "You have set DB kind for '%s' datasource via a Quarkus property. This approach is deprecated and you should use the Keycloak 'db-kind-%s' property.",
                        datasourceName, datasourceName);
            }
            return missing;
        } finally {
            PropertyMappingInterceptor.enable();
        }
    }

    /**
     * Get datasource name obtained from the persistence.xml file based on this order:
 * 按以下优先级从 persistence.xml 解析数据源名称：

     * <ol>
     *      <li> return {@link JdbcSettings#JAKARTA_JTA_DATASOURCE} if specified
     *      <li> return {@link AvailableSettings#DATASOURCE} property if specified
     *      <li> return persistence unit name
     * </ol>
     * Can be removed after removing support for persistence.xml files
     */
    /** 从 persistence.xml 描述符解析数据源名称。 */
    static String getDatasourceNameFromPersistenceXml(PersistenceUnitDescriptor descriptor) {
        if (descriptor == null) {
            throw new IllegalStateException("Descriptor cannot be null");
        }
        final BiConsumer<String, String> infoAboutUsedSourceForDsName = (source, name) -> logger.debugf(
                "Datasource name '%s' is obtained from the '%s' configuration property in persistence.xml file. " +
                        "Use '%s' name for datasource options like 'db-kind-%s'.", name, source, name, name);

        String persistenceUnitName = descriptor.getName();
        Properties properties = descriptor.getProperties();

        // 1. 优先使用 Jakarta JTA 数据源属性
        var jakartaProperty = properties.getProperty(JdbcSettings.JAKARTA_JTA_DATASOURCE);
        if (jakartaProperty != null) {
            infoAboutUsedSourceForDsName.accept(JdbcSettings.JAKARTA_JTA_DATASOURCE, jakartaProperty);
            return jakartaProperty;
        }

        // 2. 回退到已弃用的 Hibernate 数据源属性
        var deprecatedHibernateProperty = properties.getProperty(AvailableSettings.DATASOURCE);
        if (deprecatedHibernateProperty != null) {
            logger.warnf("Property '%s' is deprecated for some time and you should rather use '%s' property for datasource name in persistence.xml file.",
                    AvailableSettings.DATASOURCE, JdbcSettings.JAKARTA_JTA_DATASOURCE);
            infoAboutUsedSourceForDsName.accept(AvailableSettings.DATASOURCE, deprecatedHibernateProperty);
            return deprecatedHibernateProperty;
        }

        // 3. 最后使用持久化单元名称
        infoAboutUsedSourceForDsName.accept("Persistence unit name", persistenceUnitName);
        return persistenceUnitName;
    }

    /**
     * <p>Configures the persistence unit for Quarkus.
 * 为 Quarkus 配置持久化单元属性（方言、Schema、事务类型等）。

     *
     * <p>The {@code hibernate-orm} extension expects that the dialect is statically
     * set to the persistence unit if there is any from the classpath and we use this method to obtain the dialect from the configuration
     * file so that we can build the application with whatever dialect we want. In addition to the dialect, we should also be
     * allowed to set any additional defaults that we think that makes sense.
     *
     * @param config
 * @param config Hibernate ORM 构建时配置

     * @param descriptors
 * @param descriptors persistence.xml 解析结果列表

     */
    @BuildStep
    @Consume(ValidatePersistenceUnitsBuildItem.class)
    @Record(ExecutionTime.RUNTIME_INIT)
    /** 为默认与命名持久化单元设置 Hibernate 属性并注册运行时监听器。 */
    void configurePersistenceUnits(HibernateOrmConfig config,
            List<PersistenceXmlDescriptorBuildItem> descriptors,
            List<JdbcDataSourceBuildItem> jdbcDataSources,
            BuildProducer<AdditionalJpaModelBuildItem> additionalJpaModel,
            CombinedIndexBuildItem indexBuildItem,
            BuildProducer<HibernateOrmIntegrationRuntimeConfiguredBuildItem> runtimeConfigured,
            KeycloakRecorder recorder) {
        ParsedPersistenceXmlDescriptor defaultUnitDescriptor = null;
        List<String> userManagedEntities = new ArrayList<>();

        for (PersistenceXmlDescriptorBuildItem item : descriptors) {
            ParsedPersistenceXmlDescriptor descriptor = (ParsedPersistenceXmlDescriptor) item.getDescriptor();

            if (DEFAULT_PERSISTENCE_UNIT.equals(descriptor.getName())) {
                defaultUnitDescriptor = descriptor;
                configureDefaultPersistenceUnitProperties(defaultUnitDescriptor, config, getDefaultDataSource(jdbcDataSources));
                runtimeConfigured.produce(new HibernateOrmIntegrationRuntimeConfiguredBuildItem("keycloak", defaultUnitDescriptor.getName())
                        .setInitListener(recorder.createDefaultUnitListener()));
            } else {
                String datasourceName = getDatasourceNameFromPersistenceXml(descriptor);
                configurePersistenceUnitProperties(datasourceName, descriptor);
                // register a listener for customizing the unit configuration at runtime
                runtimeConfigured.produce(new HibernateOrmIntegrationRuntimeConfiguredBuildItem("keycloak", descriptor.getName())
                        .setInitListener(recorder.createUserDefinedUnitListener(datasourceName)));
                userManagedEntities.addAll(descriptor.getManagedClassNames());
            }
        }

        if (defaultUnitDescriptor == null) {
            throw new RuntimeException("No default persistence unit found.");
        }

        configureDefaultPersistenceUnitEntities(defaultUnitDescriptor, indexBuildItem, userManagedEntities);
    }

    @BuildStep
    @Consume(CheckJdbcBuildStep.class)
    @Consume(CheckMultipleDatasourcesBuildStep.class)
    /** 解析并产出默认 persistence.xml 描述符。 */
    void produceDefaultPersistenceUnit(BuildProducer<PersistenceXmlDescriptorBuildItem> producer) {
        PersistenceXmlParser parser = PersistenceXmlParser.create();
        PersistenceUnitDescriptor descriptor = parser.parse(Collections.singletonList(parser.getClassLoaderService().locateResource("default-persistence.xml")))
                .values()
                .stream()
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("Cannot find the file 'default-persistence.xml'"));

        producer.produce(new PersistenceXmlDescriptorBuildItem(descriptor));
    }

    /** 为命名持久化单元设置方言、Schema 与 JTA 事务类型。 */
    static void configurePersistenceUnitProperties(String datasourceName, ParsedPersistenceXmlDescriptor descriptor) {
        Properties unitProperties = descriptor.getProperties();
        var isResourceLocalSpecified = PersistenceUnitTransactionType.RESOURCE_LOCAL.equals(descriptor.getPersistenceUnitTransactionType()) ||
                Optional.ofNullable(unitProperties.getProperty(AvailableSettings.JAKARTA_TRANSACTION_TYPE))
                        .map(f -> f.equalsIgnoreCase(PersistenceUnitTransactionType.RESOURCE_LOCAL.name()))
                        .orElse(false);
        if (isResourceLocalSpecified) {
            throw new IllegalArgumentException("You need to use '%s' transaction type in your persistence.xml file."
                    .formatted(PersistenceUnitTransactionType.JTA.name()));
        }

        // 数据库方言
        DatabasePropertyMappers.getDatasourceOptionValue(DatabaseOptions.DB_DIALECT, datasourceName)
                .ifPresent(dialect -> unitProperties.setProperty(AvailableSettings.DIALECT, dialect));

        // 默认 Schema
        DatabasePropertyMappers.getDatasourceOptionValue(DatabaseOptions.DB_SCHEMA, datasourceName)
                .ifPresent(schema -> unitProperties.setProperty(AvailableSettings.DEFAULT_SCHEMA, schema));

        unitProperties.setProperty(AvailableSettings.JAKARTA_TRANSACTION_TYPE, PersistenceUnitTransactionType.JTA.name());
        descriptor.setTransactionType(PersistenceUnitTransactionType.JTA);

        // 设置 JTA 数据源名称
        unitProperties.setProperty(JdbcSettings.JAKARTA_JTA_DATASOURCE,datasourceName);
        unitProperties.setProperty(AvailableSettings.DATASOURCE, datasourceName); // for backward compatibility

        // JPQL 调试注释
        DatabasePropertyMappers.getDatasourceOptionValue(DatabaseOptions.DB_SQL_JPA_DEBUG, datasourceName)
                .ifPresent(f -> unitProperties.put(AvailableSettings.USE_SQL_COMMENTS, f));

        // 慢查询日志阈值
        DatabasePropertyMappers.getDatasourceOptionValue(DatabaseOptions.DB_SQL_LOG_SLOW_QUERIES, datasourceName)
                .ifPresent(threshold -> unitProperties.put(AvailableSettings.LOG_SLOW_QUERY, threshold));
    }

    /** 配置默认持久化单元的方言、命名查询与 JDBC 错误日志行为。 */
    private void configureDefaultPersistenceUnitProperties(ParsedPersistenceXmlDescriptor descriptor, HibernateOrmConfig config,
            JdbcDataSourceBuildItem defaultDataSource) {
        if (defaultDataSource == null || !defaultDataSource.isDefault()) {
            throw new RuntimeException("The server datasource must be the default datasource.");
        }

        Properties unitProperties = descriptor.getProperties();

        final Optional<String> dialect = getOptionalKcValue(DatabaseOptions.DB_DIALECT.getKey());
        dialect.ifPresent(d -> unitProperties.setProperty(AvailableSettings.DIALECT, d));

        final Optional<String> defaultSchema = getOptionalKcValue(DatabaseOptions.DB_SCHEMA.getKey());
        defaultSchema.ifPresent(ds -> unitProperties.setProperty(AvailableSettings.DEFAULT_SCHEMA, ds));

        unitProperties.setProperty(AvailableSettings.JAKARTA_TRANSACTION_TYPE, PersistenceUnitTransactionType.JTA.name());
        descriptor.setTransactionType(PersistenceUnitTransactionType.JTA);

        unitProperties.setProperty(AvailableSettings.QUERY_STARTUP_CHECKING, Boolean.FALSE.toString());

        String dbKind = defaultDataSource.getDbKind();

        for (Entry<Object, Object> query : loadSpecificNamedQueries(dbKind.toLowerCase()).entrySet()) {
            unitProperties.setProperty(QUERY_PROPERTY_PREFIX + query.getKey(), query.getValue().toString());
        }

        if (getOptionalBooleanKcValue(DatabaseOptions.DB_SQL_JPA_DEBUG.getKey()).orElse(false)) {
            unitProperties.put(AvailableSettings.USE_SQL_COMMENTS, "true");
        }

        // 禁止 SqlExceptionHelper 记录后抛出的 JDBC 错误日志（避免重复处理）
        // As those messages might later be caught and handled, this is an antipattern so we prevent logging them.
        unitProperties.put(JdbcSettings.LOG_JDBC_ERRORS, "false");

        getOptionalKcValue(DatabaseOptions.DB_SQL_LOG_SLOW_QUERIES.getKey())
                .ifPresent(v -> unitProperties.put(AvailableSettings.LOG_SLOW_QUERY, v));
    }

    /** 将 Jandex 索引中的实体类加入默认持久化单元。 */
    private void configureDefaultPersistenceUnitEntities(ParsedPersistenceXmlDescriptor descriptor, CombinedIndexBuildItem indexBuildItem,
            List<String> userManagedEntities) {
        IndexView index = indexBuildItem.getIndex();
        Collection<AnnotationInstance> annotations = index.getAnnotations(DotName.createSimple(Entity.class.getName()));

        for (AnnotationInstance annotation : annotations) {
            AnnotationTarget target = annotation.target();
            String targetName = target.asClass().name().toString();

            if (!userManagedEntities.contains(targetName)
                    && (!targetName.startsWith("org.keycloak") || targetName.startsWith("org.keycloak.testsuite"))) {
                descriptor.addClasses(targetName);
            }
        }
    }

    /**
     * <p>Load the built-in provider factories during build time so we don't spend time looking up them at runtime. By loading
 * 构建时预加载内置 ProviderFactory，避免运行时 SPI 扫描；用户自定义 Provider 仍在启动时加载。

 * 构建时预加载内置 ProviderFactory，避免运行时 SPI 扫描开销；用户自定义 Provider 仍在启动时加载。

     * providers at this stage we are also able to perform a more dynamic configuration based on the default providers.
     *
     * <p>User-defined providers are going to be loaded at startup</p>
     *
     * @param recorder
 * @param recorder Keycloak 运行时录制器

     */
    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    @Consume(ConfigBuildItem.class)
    @Consume(CryptoProviderInitBuildItem.class)
    @Produce(KeycloakSessionFactoryPreInitBuildItem.class)
    /** 预加载 Provider 并注册 {@link QuarkusKeycloakSessionFactory} 合成 Bean。 */
    SyntheticBeanBuildItem configureKeycloakSessionFactory(KeycloakRecorder recorder, List<PersistenceXmlDescriptorBuildItem> descriptors) {
        Map<Spi, Map<Class<? extends Provider>, Map<String, Class<? extends ProviderFactory>>>> factories = new HashMap<>();
        Map<Class<? extends Provider>, String> defaultProviders = new HashMap<>();
        Map<String, ProviderFactory> preConfiguredProviders = new HashMap<>();

        for (Entry<Spi, Map<Class<? extends Provider>, Map<String, ProviderFactory>>> entry : loadFactories(preConfiguredProviders)
                .entrySet()) {
            Spi spi = entry.getKey();

            checkProviders(spi, entry.getValue(), defaultProviders);

            for (Entry<Class<? extends Provider>, Map<String, ProviderFactory>> value : entry.getValue().entrySet()) {
                for (ProviderFactory factory : value.getValue().values()) {
                    factories.computeIfAbsent(spi,
                            key -> new HashMap<>())
                            .computeIfAbsent(spi.getProviderClass(), aClass -> new HashMap<>()).put(factory.getId(),factory.getClass());
                }
            }

            if (spi instanceof JpaConnectionSpi) {
                configureUserDefinedPersistenceUnits(descriptors, factories, preConfiguredProviders, spi);
            }

            if (spi instanceof ThemeResourceSpi) {
                configureThemeResourceProviders(factories, spi);
            }
        }

        recorder.setDefaultUserProfileConfiguration(UPConfigUtils.parseSystemDefaultConfig());

        return SyntheticBeanBuildItem.configure(QuarkusKeycloakSessionFactory.class).scope(Singleton.class)
                .unremovable()
                .runtimeValue(recorder.createSessionFactory(factories, defaultProviders, preConfiguredProviders,
                        loadThemesFromClassPath())).done();
    }

    /** 从 classpath 加载 META-INF/keycloak-themes.json。 */
    private List<ClasspathThemeProviderFactory.ThemesRepresentation> loadThemesFromClassPath() {
        try {
            List<ClasspathThemeProviderFactory.ThemesRepresentation> themes = new ArrayList<>();
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(KEYCLOAK_THEMES_JSON);

            while (resources.hasMoreElements()) {
                themes.add(JsonSerialization.readValue(resources.nextElement().openStream(), ClasspathThemeProviderFactory.ThemesRepresentation.class));
            }

            return themes;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load themes", e);
        }
    }

    /** 若存在扁平 classpath 主题资源则注册对应 Provider。 */
    private void configureThemeResourceProviders(Map<Spi, Map<Class<? extends Provider>, Map<String, Class<? extends ProviderFactory>>>> factories, Spi spi) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(FlatClasspathThemeResourceProviderFactory.THEME_RESOURCES);

            if (resources.hasMoreElements()) {
                // 扁平 classpath 模式下注册主题资源 Provider；无资源则不注册
                factories.computeIfAbsent(spi, key -> new HashMap<>()).computeIfAbsent(spi.getProviderClass(), aClass -> new HashMap<>()).put(FlatClasspathThemeResourceProviderFactory.ID, FlatClasspathThemeResourceProviderFactory.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to install default theme resource provider", e);
        }
    }

    /** 为每个命名持久化单元注册 {@link NamedJpaConnectionProviderFactory}。 */
    private void configureUserDefinedPersistenceUnits(List<PersistenceXmlDescriptorBuildItem> descriptors,
            Map<Spi, Map<Class<? extends Provider>, Map<String, Class<? extends ProviderFactory>>>> factories,
            Map<String, ProviderFactory> preConfiguredProviders, Spi spi) {
        descriptors.stream()
                .map(PersistenceXmlDescriptorBuildItem::getDescriptor)
                .map(PersistenceUnitDescriptor::getName)
                .filter(Predicate.not(DEFAULT_PERSISTENCE_UNIT::equals))
                .forEach((String unitName) -> {
                    NamedJpaConnectionProviderFactory factory = new NamedJpaConnectionProviderFactory();

                    factory.setUnitName(unitName);

                    factories.get(spi).get(JpaConnectionProvider.class).put(unitName, NamedJpaConnectionProviderFactory.class);
                    preConfiguredProviders.put(unitName, factory);
                });
    }

    /**
     * Register the custom {@link ConfigSource} implementations.
 * 注册 Keycloak 自定义 {@link ConfigSource} 实现。

     *
     * @param configSources
 * @param configSources 静态初始化 ConfigSource 构建项生产者

     */
    @BuildStep
    /** 注册 Keycloak 静态 ConfigSource 构建器。 */
    void configureConfigSources(BuildProducer<StaticInitConfigBuilderBuildItem> configSources) {
        configSources.produce(new StaticInitConfigBuilderBuildItem(KeycloakConfigSourceProvider.class.getName()));
    }

    /**
     * <p>Make the build time configuration available at runtime so that the server can run without having to specify some of
 * 将构建时配置持久化为资源，使运行时无需重复指定相同属性。

     * the properties again.
     */
    @BuildStep(onlyIf = IsReAugmentation.class)
    /** 重新增强时将构建时配置写入 persisted.properties。 */
    void persistBuildTimeProperties(BuildProducer<GeneratedResourceBuildItem> resources) {
        Properties properties = Picocli.getNonPersistedBuildTimeOptions();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            properties.store(outputStream, " Auto-generated, DO NOT change this file");
            resources.produce(new GeneratedResourceBuildItem(PersistedConfigSource.PERSISTED_PROPERTIES, outputStream.toByteArray()));
        } catch (Exception cause) {
            throw new RuntimeException("Failed to persist configuration", cause);
        }
    }

    @BuildStep
    @Consume(ProfileBuildItem.class)
    /** 解析 WebAuthn 认证器元数据（特性启用时）。 */
    void parseWebAuthnMetadata(BuildProducer<WebAuthnMetadataBuildItem> producer) {
        if (Profile.isFeatureEnabled(Profile.Feature.WEB_AUTHN)) {
            producer.produce(new WebAuthnMetadataBuildItem(WebAuthnMetadataService.parseMetadata()));
        }
    }

    @Record(ExecutionTime.STATIC_INIT)
    @Consume(ProfileBuildItem.class)
    @Consume(WebAuthnMetadataBuildItem.class)
    @BuildStep
    /** 将 WebAuthn 元数据录制到运行时。 */
    void configureWebAuthnMetadata(KeycloakRecorder recorder, WebAuthnMetadataBuildItem metadataBuildItem) {
        if (Profile.isFeatureEnabled(Profile.Feature.WEB_AUTHN)) {
            recorder.setDefaultWebAuthnMetadata(metadataBuildItem.getMetadata());
        }
    }

    /**
     * This will cause quarkus to include specified modules in the jandex index. For example keycloak-services is needed as it includes
 * 指示 Quarkus 将指定模块纳入 Jandex 索引（如 keycloak-services 含 JAX-RS 资源）。

     * most of the JAX-RS resources, which are required to register Resteasy builtin providers.
     * Similar reason is liquibase
     *
     * @param indexDependencyBuildItemBuildProducer
 * @param indexDependencyBuildItemBuildProducer 索引依赖构建项生产者

     */
    @BuildStep
    /** 将 Liquibase、keycloak-services 等模块加入 Jandex 索引。 */
    void index(BuildProducer<IndexDependencyBuildItem> indexDependencyBuildItemBuildProducer) {
        indexDependencyBuildItemBuildProducer.produce(new IndexDependencyBuildItem("org.liquibase", "liquibase-core"));
        indexDependencyBuildItemBuildProducer.produce(new IndexDependencyBuildItem("org.keycloak", "keycloak-services"));
        indexDependencyBuildItemBuildProducer.produce(new IndexDependencyBuildItem("com.fasterxml.jackson.jakarta.rs", "jackson-jakarta-rs-yaml-provider"));
    }

    @BuildStep
    @Consume(CheckJdbcBuildStep.class)
    /** 将 keycloak-model-jpa 加入 Jandex 索引。 */
    void indexJpaStore(BuildProducer<IndexDependencyBuildItem> indexDependencyBuildItemBuildProducer) {
        indexDependencyBuildItemBuildProducer.produce(new IndexDependencyBuildItem("org.keycloak", "keycloak-model-jpa"));
    }

    @BuildStep
    @Consume(ProfileBuildItem.class)
    /** 按健康/指标/集群配置禁用相应 Readiness Bean。 */
    void disableHealthCheckBean(BuildProducer<BuildTimeConditionBuildItem> removeBeans, CombinedIndexBuildItem index) {
        if (isHealthDisabled()) {
            disableReadyHealthCheck(removeBeans, index);
            disableClusterHealthCheck(removeBeans, index);
            disableBootstrapReadyHealthCheck(removeBeans, index);
            return;
        }
        if (isMetricsDisabled()) {
            // 指标禁用时移除依赖指标的 Ready 检查
            disableReadyHealthCheck(removeBeans, index);
        }
        if (InfinispanUtils.isRemoteInfinispan()) {
            // 远程 Infinispan 模式下无集群健康检查
            disableClusterHealthCheck(removeBeans, index);
        }
    }

    /** 禁用集群就绪健康检查 Bean。 */
    private static void disableClusterHealthCheck(BuildProducer<BuildTimeConditionBuildItem> removeBeans, CombinedIndexBuildItem index) {
        ClassInfo clusterHealth = index.getIndex().getClassByName(DotName.createSimple(KeycloakClusterReadyHealthCheck.class));
        removeBeans.produce(new BuildTimeConditionBuildItem(clusterHealth.asClass(), false));
    }

    /** 禁用 Keycloak 就绪健康检查 Bean。 */
    private static void disableReadyHealthCheck(BuildProducer<BuildTimeConditionBuildItem> removeBeans, CombinedIndexBuildItem index) {
        ClassInfo disabledBean = index.getIndex().getClassByName(DotName.createSimple(KeycloakReadyHealthCheck.class.getName()));
        removeBeans.produce(new BuildTimeConditionBuildItem(disabledBean.asClass(), false));
    }

    /** 禁用引导就绪健康检查 Bean。 */
    private static void disableBootstrapReadyHealthCheck(BuildProducer<BuildTimeConditionBuildItem> removeBeans, CombinedIndexBuildItem index) {
        ClassInfo disabledBean = index.getIndex().getClassByName(DotName.createSimple(BootstrapReadyHealthCheck.class.getName()));
        removeBeans.produce(new BuildTimeConditionBuildItem(disabledBean.asClass(), false));
    }

    @BuildStep
    /** MDC 未启用时移除 MDC 上下文清理过滤器。 */
    void disableMdcContextFilter(BuildProducer<BuildTimeConditionBuildItem> removeBeans, CombinedIndexBuildItem index) {
        if (!Configuration.isTrue(LoggingOptions.LOG_MDC_ENABLED)) {
            // 禁用 MDC 过滤器 Bean
            ClassInfo disabledBean = index.getIndex()
                    .getClassByName(DotName.createSimple(ClearMappedDiagnosticContextFilter.class.getName()));
            removeBeans.produce(new BuildTimeConditionBuildItem(disabledBean.asClass(), false));
        }
    }

    // 不能直接用 quarkus.datasource.health.enabled=false，否则 DataSourceHealthCheck 无法实例化；改为移除 @Readiness
    // it can't be instantiated via constructor as it now includes some field injection points. So we just make it a regular
    // bean without the @Readiness annotation so it won't be used as a health check on it's own.
    @BuildStep
    /** 移除 Quarkus 默认 DataSourceHealthCheck 的 @Readiness 注解。 */
    AnnotationsTransformerBuildItem disableDefaultDataSourceHealthCheck() {
        return new AnnotationsTransformerBuildItem(AnnotationTransformation.forClasses()
                .whenClass(c -> c.name().equals(DotName.createSimple(DataSourceHealthCheck.class)))
                .transform(t -> t.remove(
                        a -> a.name().equals(DotName.createSimple(Readiness.class)))));
    }

    @BuildStep
    @Consume(ProfileBuildItem.class)
    /** 按 Profile 禁用 Admin API/LB 资源并配置 RESTEasy 处理器链与追踪。 */
    void configureResteasy(CombinedIndexBuildItem index,
            BuildProducer<BuildTimeConditionBuildItem> buildTimeConditionBuildItemBuildProducer,
            BuildProducer<MethodScannerBuildItem> scanner,
           BuildProducer<PreExceptionMapperHandlerBuildItem> preExceptionMapperHandlerBuildItemBuildProducer) {
        if (!Profile.isFeatureEnabled(Profile.Feature.ADMIN_API)) {
            buildTimeConditionBuildItemBuildProducer.produce(new BuildTimeConditionBuildItem(index.getIndex().getClassByName(DotName.createSimple(
                    AdminRoot.class.getName())), false));
        }

        if (!MultiSiteUtils.isMultiSiteEnabled() && !Profile.isFeatureEnabled(Profile.Feature.STATELESS)) {
            buildTimeConditionBuildItemBuildProducer.produce(new BuildTimeConditionBuildItem(index.getIndex().getClassByName(DotName.createSimple(
                    LoadBalancerResource.class.getName())), false));
        }

        ArrayList<HandlerChainCustomizer> chainCustomizers = new ArrayList<>();

        chainCustomizers.add(new KeycloakHandlerChainCustomizer());

        if (Configuration.isTrue(TracingOptions.TRACING_ENABLED)) {
            chainCustomizers.add(new KeycloakTracingCustomizer());
            // 需要异常处理器以确保 Bean 方法抛错时 Span 正确关闭
            // otherwise the spans will not be closed.
            preExceptionMapperHandlerBuildItemBuildProducer
                    .produce(new PreExceptionMapperHandlerBuildItem(new KeycloakTracingCustomizer.EndHandler()));
        }

        scanner.produce(new MethodScannerBuildItem(new MethodScanner() {
            @Override
            public List<HandlerChainCustomizer> scan(MethodInfo method, ClassInfo actualEndpointClass,
                    Map<String, Object> methodContext) {
                return chainCustomizers;
            }
        }));
    }

    @Consume(ProfileBuildItem.class)
    @Produce(CryptoProviderInitBuildItem.class)
    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    /** 解析 FIPS 模式并初始化 Crypto Provider。 */
    void setCryptoProvider(KeycloakRecorder recorder) {
        FipsMode fipsMode = getOptionalValue(NS_KEYCLOAK_PREFIX + SecurityOptions.FIPS_MODE.getKey())
                .map(FipsMode::valueOfOption)
                .orElse(FipsMode.DISABLED);
        if (Profile.isFeatureEnabled(Profile.Feature.FIPS) && !fipsMode.isFipsEnabled()) {
            // FIPS 特性启用但未指定模式时默认 NON_STRICT
            fipsMode = FipsMode.NON_STRICT;
        } else if (fipsMode.isFipsEnabled() && !Profile.isFeatureEnabled(Profile.Feature.FIPS)) {
            throw new RuntimeException("FIPS mode cannot be enabled without enabling the FIPS feature --features=fips");
        }

        recorder.setCryptoProvider(fipsMode);
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    /** 开发模式下监视 keycloak.conf 热部署变更。 */
    void configureDevMode(BuildProducer<HotDeploymentWatchedFileBuildItem> hotFiles) {
        hotFiles.produce(new HotDeploymentWatchedFileBuildItem("META-INF/keycloak.conf"));
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    /** 加载并配置 Infinispan ProtoStream 序列化模式。 */
    void configureProtoStreamSchemas(KeycloakRecorder recorder) {
        var schemas = ServiceLoader.load(SerializationContextInitializer.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        recorder.configureProtoStreamSchemas(schemas);
    }

    /** 构建时加载 SPI ProviderFactory（含脚本 Provider）。 */
    private Map<Spi, Map<Class<? extends Provider>, Map<String, ProviderFactory>>> loadFactories(
            Map<String, ProviderFactory> preConfiguredProviders) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ProviderManager pm = getProviderManager(classLoader);
        Map<Spi, Map<Class<? extends Provider>, Map<String, ProviderFactory>>> factories = new HashMap<>();

        for (Spi spi : pm.loadSpis()) {
            Map<Class<? extends Provider>, Map<String, ProviderFactory>> providers = new HashMap<>();
            List<ProviderFactory> loadedFactories = new ArrayList<>();
            String provider = Config.getProvider(spi.getName());

            if (provider == null) {
                loadedFactories.addAll(pm.load(spi));
            } else {
                ProviderFactory factory = pm.load(spi, provider);

                if (factory != null) {
                    loadedFactories.add(factory);
                }
            }

            Map<String, ProviderFactory<?>> deployedScriptProviders = loadDeployedScriptProviders(classLoader, spi);

            loadedFactories.addAll(deployedScriptProviders.values());
            preConfiguredProviders.putAll(deployedScriptProviders);

            for (ProviderFactory<?> factory : loadedFactories) {
                if (IGNORED_PROVIDER_FACTORY.contains(factory.getClass())) {
                    continue;
                }

                Config.Scope scope = Config.scope(spi.getName(), factory.getId());

                if (isEnabled(factory, scope)) {
                    if (spi.isInternal() && !isInternal(factory)) {
                        ServicesLogger.LOGGER.spiMayChange(factory.getId(), factory.getClass().getName(), spi.getName());
                    }

                    providers.computeIfAbsent(spi.getProviderClass(), aClass -> new HashMap<>()).put(factory.getId(),
                            factory);
                } else {
                    logger.debugv("SPI {0} provider {1} disabled", spi.getName(), factory.getId());
                }
            }

            factories.put(spi, providers);
        }

        return factories;
    }

    /** 从 JAR 与 classpath 发现并加载可部署脚本 Provider。 */
    private Map<String, ProviderFactory<?>> loadDeployedScriptProviders(ClassLoader classLoader, Spi spi) {
        Map<String, ProviderFactory<?>> providers = new HashMap<>();

        if (supportsDeployeableScripts(spi)) {
            try {
                Enumeration<URL> descriptorsUrls = classLoader.getResources(KEYCLOAK_SCRIPTS_JSON_PATH);

                while (descriptorsUrls.hasMoreElements()) {
                    URL url = descriptorsUrls.nextElement();
                    List<ScriptProviderDescriptor> descriptors = getScriptProviderDescriptorsFromJarFile(url);

                    if (LaunchMode.current().isDevOrTest() || Environment.getHomeDir().isEmpty()) {
                        // 嵌入式运行时额外从 classpath 加载脚本 Provider
                        descriptors = new ArrayList<>(descriptors);
                        descriptors.addAll(getScriptProviderDescriptorsFromClassPath(url));
                    }

                    for (ScriptProviderDescriptor descriptor : descriptors) {
                        for (Entry<String, List<ScriptProviderMetadata>> entry : descriptor.getProviders().entrySet()) {
                            if (isScriptForSpi(spi, entry.getKey())) {
                                for (ScriptProviderMetadata metadata : entry.getValue()) {
                                    ProviderFactory<?> factory = DEPLOYEABLE_SCRIPT_PROVIDERS.get(entry.getKey()).apply(metadata);
                                    providers.put(metadata.getId(), factory);
                                }
                            }
                        }
                    }
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("Failed to discover script providers", e);
            }
        }

        return providers;
    }

    /** 从 classpath JSON 文件加载脚本 Provider 描述符。 */
    private List<ScriptProviderDescriptor> getScriptProviderDescriptorsFromClassPath(URL url) throws IOException {
        String file = url.getFile();

        if (!file.endsWith(".json")) {
            return List.of();
        }

        List<ScriptProviderDescriptor> descriptors = new ArrayList<>();

        try (InputStream is = url.openStream()) {
            ScriptProviderDescriptor descriptor = JsonSerialization.readValue(is, ScriptProviderDescriptor.class);

            configureScriptDescriptor(descriptor, fileName -> {
                // 描述符位于 META-INF/，脚本路径相对其上级目录解析
                Path basePath = Path.of(url.getPath()).getParent().getParent();

                String path = basePath.resolve(fileName).toString();
                if (!path.startsWith(url.getProtocol())) {
                    path = url.getProtocol() + ":" + path;
                }
                try {
                    return new URI(path).toURL().openStream();
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException("Failed to read script file from: " + fileName);
                }
            });
            descriptors.add(descriptor);
        }

        return descriptors;
    }

    /** 从 JAR 内 META-INF/keycloak-scripts.json 加载脚本描述符。 */
    private List<ScriptProviderDescriptor> getScriptProviderDescriptorsFromJarFile(URL url) throws IOException, URISyntaxException {
        String file = url.toURI().getSchemeSpecificPart();

        if (!file.contains(JAR_FILE_SEPARATOR)) {
            return List.of();
        }

        List<ScriptProviderDescriptor> descriptors = new ArrayList<>();

        try (JarFile jarFile = new JarFile(file.substring("file:".length(), file.indexOf(JAR_FILE_SEPARATOR)))) {
            JarEntry descriptorEntry = jarFile.getJarEntry(KEYCLOAK_SCRIPTS_JSON_PATH);

            if (descriptorEntry == null) {
                return descriptors;
            }

            try (InputStream is = jarFile.getInputStream(descriptorEntry)) {
                ScriptProviderDescriptor descriptor = JsonSerialization.readValue(is, ScriptProviderDescriptor.class);

                configureScriptDescriptor(descriptor, fileName -> {
                    try {
                        JarEntry scriptFile = jarFile.getJarEntry(fileName);
                        return jarFile.getInputStream(scriptFile);
                    } catch (IOException cause) {
                        throw new RuntimeException("Failed to read script file from file: " + fileName, cause);
                    }
                });

                descriptors.add(descriptor);
            }
        }

        return descriptors;
    }

    /** 读取脚本源码并填充元数据 ID 与显示名称。 */
    private static void configureScriptDescriptor(ScriptProviderDescriptor descriptor, Function<String, InputStream> jsFileLoader) throws IOException {
        for (List<ScriptProviderMetadata> metadatas : descriptor.getProviders().values()) {
            for (ScriptProviderMetadata metadata : metadatas) {
                String fileName = metadata.getFileName();

                if (fileName == null) {
                    throw new RuntimeException("You must provide the script file name");
                }

                try (InputStream in = jsFileLoader.apply(fileName)) {
                    metadata.setCode(StreamUtil.readString(in, StandardCharsets.UTF_8));
                }

                metadata.setId("script-" + fileName);

                String name = metadata.getName();

                if (name == null) {
                    name = fileName;
                }

                metadata.setName(name);
            }
        }
    }

    /** 判断脚本类型是否属于当前 SPI。 */
    private boolean isScriptForSpi(Spi spi, String type) {
        if (spi instanceof ProtocolMapperSpi && (MAPPERS.equals(type) || SAML_MAPPERS.equals(type))) {
            return true;
        } else if (spi instanceof PolicySpi && POLICIES.equals(type)) {
            return true;
        } else if (spi instanceof AuthenticatorSpi && AUTHENTICATORS.equals(type)) {
            return true;
        }
        return false;
    }

    /** 判断 SPI 是否支持可部署脚本 Provider。 */
    private boolean supportsDeployeableScripts(Spi spi) {
        return spi instanceof ProtocolMapperSpi || spi instanceof PolicySpi || spi instanceof AuthenticatorSpi;
    }

    /** 判断 ProviderFactory 在当前配置下是否启用。 */
    private boolean isEnabled(ProviderFactory factory, Config.Scope scope) {
        if (!scope.getBoolean("enabled", true)) {
            return false;
        }
        if (factory instanceof EnvironmentDependentProviderFactory environmentDependentProviderFactory) {
            return environmentDependentProviderFactory.isSupported(scope);
        }
        return true;
    }

    /** 判断是否为 Keycloak 内部 Provider（非 examples 包）。 */
    private boolean isInternal(ProviderFactory<?> factory) {
        String packageName = factory.getClass().getPackage().getName();
        return packageName.startsWith("org.keycloak") && !packageName.startsWith("org.keycloak.examples");
    }

    /** 校验 SPI 默认 Provider 配置是否有效。 */
    private void checkProviders(Spi spi,
                                Map<Class<? extends Provider>, Map<String, ProviderFactory>> factoriesMap,
                                Map<Class<? extends Provider>, String> defaultProviders) {
        String provider = Config.getProvider(spi.getName());
        if (provider != null) {
            Map<String, ProviderFactory> map = factoriesMap.get(spi.getProviderClass());
            if (map == null || map.get(provider) == null) {
                throw new RuntimeException("Failed to find provider " + provider + " for " + spi.getName());
            }
            defaultProviders.put(spi.getProviderClass(), provider);
        } else {
            Map<String, ProviderFactory> factories = factoriesMap.get(spi.getProviderClass());
            String defaultProvider = DefaultKeycloakSessionFactory.resolveDefaultProvider(factories, spi);
            if (defaultProvider != null) {
                defaultProviders.put(spi.getProviderClass(), defaultProvider);
            }
        }
    }

    /** @return 指标是否已禁用 */
    private static boolean isMetricsDisabled() {
        return !Configuration.isTrue(MetricsOptions.METRICS_ENABLED);
    }

    /** @return 健康检查是否已禁用 */
    private static boolean isHealthDisabled() {
        return !Configuration.isTrue(HealthOptions.HEALTH_ENABLED);
    }

    /** 从构建项列表中查找默认 JDBC 数据源。 */
    static JdbcDataSourceBuildItem getDefaultDataSource(List<JdbcDataSourceBuildItem> jdbcDataSources) {
        for (JdbcDataSourceBuildItem jdbcDataSource : jdbcDataSources) {
            if (jdbcDataSource.isDefault()) {
                return jdbcDataSource;
            }
        }

        throw new RuntimeException("No default datasource found. The server datasource must be the default datasource.");
    }
}
