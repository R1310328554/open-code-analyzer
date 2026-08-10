package org.keycloak.testframework.infinispan;

import org.keycloak.testframework.annotations.InjectInfinispanServer;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierOrder;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;
import org.keycloak.testframework.server.KeycloakServerConfigInterceptor;

import org.jboss.logging.Logger;

/**
 * 为 {@link InjectInfinispanServer} 注入外部 Infinispan 服务器并写入 Keycloak 缓存配置的供应器。
 * <p>
 * 在 Keycloak 启动前启动 Testcontainers 管理的 Infinispan 实例，
 * 并将其 {@link InfinispanServer#serverConfig()} 合并到服务器配置。
 */
public class InfinispanExternalServerSupplier implements Supplier<InfinispanServer, InjectInfinispanServer>, KeycloakServerConfigInterceptor<InfinispanServer, InjectInfinispanServer> {

    private static final Logger LOGGER = Logger.getLogger(InfinispanExternalServerSupplier.class);

    /** {@inheritDoc} 创建、启动 Infinispan 容器并记录启动耗时。 */
    @Override
    public InfinispanServer getValue(InstanceContext<InfinispanServer, InjectInfinispanServer> instanceContext) {
        InfinispanServer server = InfinispanExternalServer.create();
        getLogger().info("Starting Infinispan Server");

        long start = System.currentTimeMillis();

        server.start();

        getLogger().infov("Infinispan server started in {0} ms", System.currentTimeMillis() - start);
        return server;
    }

    /** {@inheritDoc} 停止 Infinispan 容器。 */
    @Override
    public void close(InstanceContext<InfinispanServer, InjectInfinispanServer> instanceContext) {
        instanceContext.getValue().stop();
    }

    /** {@inheritDoc} 所有 Infinispan 服务器实例互相兼容。 */
    @Override
    public boolean compatible(InstanceContext<InfinispanServer, InjectInfinispanServer> a, RequestedInstance<InfinispanServer, InjectInfinispanServer> b) {
        return true;
    }

    /** {@inheritDoc} 在 Keycloak 服务器启动前执行配置。 */
    @Override
    public int order() {
        return SupplierOrder.BEFORE_KEYCLOAK_SERVER;
    }

    /** {@inheritDoc} 将 Infinispan 远程缓存选项写入 Keycloak 配置。 */
    @Override
    public KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder config, InstanceContext<InfinispanServer, InjectInfinispanServer> instanceContext) {
        InfinispanServer ispnServer = instanceContext.getValue();

        return config.options(ispnServer.serverConfig());
    }

    /** 返回本供应器使用的 JBoss 日志记录器。 */
    public Logger getLogger() {
        return LOGGER;
    }
}
