package org.keycloak.testframework.server;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * 内嵌模式 Keycloak 服务器的 {@link Supplier}，别名 {@code embedded}。
 * <p>
 * 在同 JVM 内启动 Quarkus Keycloak，启动超时可通过 MicroProfile 配置。
 */
public class EmbeddedKeycloakServerSupplier extends AbstractKeycloakServerSupplier {

    /** 启动超时（秒）。 */
    @ConfigProperty(name = "start.timeout", defaultValue = "120")
    long startTimeout;

    /** 本 Supplier 的日志记录器。 */
    private static final Logger LOGGER = Logger.getLogger(EmbeddedKeycloakServerSupplier.class);

    /** {@inheritDoc} 构造 {@link EmbeddedKeycloakServer}。 */
    @Override
    public KeycloakServer getServer() {
        return new EmbeddedKeycloakServer(startTimeout);
    }

    /** {@inheritDoc} 内嵌模式需要测试数据库。 */
    @Override
    public boolean requiresDatabase() {
        return true;
    }

    /** {@inheritDoc} @return 别名 {@code embedded} */
    @Override
    public String getAlias() {
        return "embedded";
    }

    /** {@inheritDoc} @return 本类 {@link Logger} */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
