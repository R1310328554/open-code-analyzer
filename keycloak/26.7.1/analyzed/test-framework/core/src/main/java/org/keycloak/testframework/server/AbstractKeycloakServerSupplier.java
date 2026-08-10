package org.keycloak.testframework.server;

import java.util.List;

import org.keycloak.common.util.KeystoreUtil;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.config.Config;
import org.keycloak.testframework.database.TestDatabase;
import org.keycloak.testframework.https.ManagedCertificates;
import org.keycloak.testframework.infinispan.InfinispanServer;
import org.keycloak.testframework.injection.AbstractInterceptorHelper;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.Registry;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;
import org.keycloak.testframework.injection.SupplierOrder;

import org.jboss.logging.Logger;

/**
 * Keycloak 测试服务器的抽象 {@link Supplier} 基类。
 * <p>
 * 负责解析 {@link KeycloakServerConfig}、装配 TLS/MTLS 与数据库/Infinispan 依赖，
 * 并调用具体 {@link KeycloakServer} 实现启动与停止服务器。
 */
public abstract class AbstractKeycloakServerSupplier implements Supplier<KeycloakServer, KeycloakIntegrationTest> {

    /** {@inheritDoc} 按需依赖证书、测试数据库与外部 Infinispan。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<KeycloakServer, KeycloakIntegrationTest> instanceContext) {
        KeycloakServerConfigBuilder command = getKeycloakServerConfigBuilder(instanceContext.getAnnotation());

        DependenciesBuilder builder = DependenciesBuilder.create(ManagedCertificates.class);
        if (requiresDatabase()) {
            builder.add(TestDatabase.class);
        }

        if (command.isExternalInfinispanEnabled()) {
            builder.add(InfinispanServer.class);
        }

        return builder.build();
    }

    /** {@inheritDoc} 构建启动命令、应用 TLS 配置并启动 {@link KeycloakServer}。 */
    @Override
    public KeycloakServer getValue(InstanceContext<KeycloakServer, KeycloakIntegrationTest> instanceContext) {

        KeycloakServerConfigBuilder command = getKeycloakServerConfigBuilder(instanceContext.getAnnotation());

        // 数据库启动及 Keycloak 连接配置
        if (requiresDatabase()) {
            instanceContext.getDependency(TestDatabase.class);
        }

        // 外部 Infinispan 启动及 Keycloak 连接配置
        if (command.isExternalInfinispanEnabled()) {
            instanceContext.getDependency(InfinispanServer.class);
        }

        ServerConfigInterceptorHelper interceptor = new ServerConfigInterceptorHelper(instanceContext.getRegistry());
        command = interceptor.intercept(command, instanceContext);

        ManagedCertificates managedCert = instanceContext.getDependency(ManagedCertificates.class);

        if (managedCert.isTlsEnabled()) {
            command.option("https-key-store-file", managedCert.getServerKeyStorePath());
            command.option("https-key-store-password", managedCert.getServerKeyStorePassword());
            command.option("https-key-store-type", managedCert.getKeystoreFormat().name());
        }

        if (managedCert.isMTlsEnabled()) {
            command.option("https-client-auth", "request");
            if (KeystoreUtil.TruststoreFormat.PEM.name().equalsIgnoreCase(KeystoreUtil.getTruststoreType(
                    null, managedCert.getServerTrustStorePath(), managedCert.getKeystoreFormat().name()))) {
                // PEM 格式使用 truststore-paths 选项
                command.option("truststore-paths", managedCert.getServerTrustStorePath());
            } else {
                // 其他格式使用 https-trust-store-file 选项
                command.option("https-trust-store-file", managedCert.getServerTrustStorePath());
                command.option("https-trust-store-password", managedCert.getServerTrustStorePassword());
                command.option("https-trust-store-type", managedCert.getKeystoreFormat().name());
            }
        }

        command.log().fromConfig(Config.getConfig());

        getLogger().info("Starting Keycloak test server");
        if (getLogger().isDebugEnabled()) {
            getLogger().debugv("Startup command and options: \n\t{0}", String.join("\n\t", command.toArgs()));
        }

        long start = System.currentTimeMillis();

        KeycloakServer server = getServer();
        server.start(command, managedCert.isTlsEnabled());

        getLogger().infov("Keycloak test server started in {0} ms", System.currentTimeMillis() - start);

        return server;
    }

    /** 从注解与全局 {@link Config} 组装 dev 模式启动命令构建器。 */
    private static KeycloakServerConfigBuilder getKeycloakServerConfigBuilder(KeycloakIntegrationTest annotation) {
        KeycloakServerConfig serverConfig = SupplierHelpers.getInstance(annotation.config());
        KeycloakServerConfigBuilder command = KeycloakServerConfigBuilder.startDev()
                .bootstrapAdminClient(Config.getAdminClientId(), Config.getAdminClientSecret())
                .bootstrapAdminUser(Config.getAdminUsername(), Config.getAdminPassword());

        command.log().handlers(KeycloakServerConfigBuilder.LogHandlers.CONSOLE);

        String supplierConfig = Config.getSupplierConfig(KeycloakServer.class);
        if (supplierConfig != null) {
            KeycloakServerConfig serverConfigOverride = SupplierHelpers.getInstance(supplierConfig);
            serverConfigOverride.configure(command);
        }

        command = serverConfig.configure(command);
        return command;
    }

    /** {@inheritDoc} 服务器默认使用 {@link LifeCycle#GLOBAL} 生命周期。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    /** {@inheritDoc} 仅当 {@code config} 注解值相同时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<KeycloakServer, KeycloakIntegrationTest> a, RequestedInstance<KeycloakServer, KeycloakIntegrationTest> b) {
        return a.getAnnotation().config().equals(b.getAnnotation().config());
    }

    /** {@inheritDoc} 停止 Keycloak 测试服务器。 */
    @Override
    public void close(InstanceContext<KeycloakServer, KeycloakIntegrationTest> instanceContext) {
        instanceContext.getValue().stop();
    }

    /** @return 具体 {@link KeycloakServer} 实现实例 */
    public abstract KeycloakServer getServer();

    /** @return 启动前是否必须部署 {@link TestDatabase} */
    public abstract boolean requiresDatabase();

    /** @return 本 Supplier 使用的 JBoss {@link Logger} */
    public abstract Logger getLogger();

    /** {@inheritDoc} 使用 {@link SupplierOrder#KEYCLOAK_SERVER} 顺序。 */
    @Override
    public int order() {
        return SupplierOrder.KEYCLOAK_SERVER;
    }

    /** 聚合并调用所有 {@link KeycloakServerConfigInterceptor} 实现。 */
    private static class ServerConfigInterceptorHelper extends AbstractInterceptorHelper<KeycloakServerConfigInterceptor, KeycloakServerConfigBuilder> {

        /** @param registry 测试框架注册表 */
        private ServerConfigInterceptorHelper(Registry registry) {
            super(registry, KeycloakServerConfigInterceptor.class);
        }

        /** {@inheritDoc} 若供应器实现拦截器接口则调用其 {@code intercept}。 */
        @Override
        public KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder value, Supplier<?, ?> supplier, InstanceContext<?, ?> existingInstance) {
            if (supplier instanceof KeycloakServerConfigInterceptor keycloakServerConfigInterceptor) {
                value = keycloakServerConfigInterceptor.intercept(value, existingInstance);
            }
            return value;
        }
    }

}
