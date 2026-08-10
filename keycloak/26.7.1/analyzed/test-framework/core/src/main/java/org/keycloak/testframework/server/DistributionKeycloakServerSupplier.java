package org.keycloak.testframework.server;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * 发行包模式 Keycloak 服务器的 {@link Supplier}，别名 {@code distribution}。
 * <p>
 * 通过 MicroProfile 配置控制调试、进程复用与启动超时。
 */
public class DistributionKeycloakServerSupplier extends AbstractKeycloakServerSupplier {

    /** 本 Supplier 的日志记录器。 */
    private static final Logger LOGGER = Logger.getLogger(DistributionKeycloakServerSupplier.class);

    /** 启动超时（秒）。 */
    @ConfigProperty(name = "start.timeout", defaultValue = "120")
    long startTimeout;

    /** 是否以 DEBUG 模式启动 Keycloak。 */
    @ConfigProperty(name = "debug", defaultValue = "false")
    boolean debug = false;

    /** 是否复用已在运行的托管 Keycloak 进程。 */
    @ConfigProperty(name = "reuse", defaultValue = "false")
    boolean reuse;

    /** {@inheritDoc} 构造 {@link DistributionKeycloakServer}。 */
    @Override
    public KeycloakServer getServer() {
        return new DistributionKeycloakServer(debug, reuse, startTimeout);
    }

    /** {@inheritDoc} 发行包模式需要测试数据库。 */
    @Override
    public boolean requiresDatabase() {
        return true;
    }

    /** {@inheritDoc} @return 别名 {@code distribution} */
    @Override
    public String getAlias() {
        return "distribution";
    }

    /** {@inheritDoc} @return 本类 {@link Logger} */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
