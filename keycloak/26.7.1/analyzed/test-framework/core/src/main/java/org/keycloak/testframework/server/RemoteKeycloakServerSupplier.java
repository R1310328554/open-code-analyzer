package org.keycloak.testframework.server;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * 提供 {@link RemoteKeycloakServer} 的 {@link AbstractKeycloakServerSupplier} 实现。
 * <p>
 * 别名 {@code remote}，不依赖嵌入式数据库。
 */
public class RemoteKeycloakServerSupplier extends AbstractKeycloakServerSupplier {

    /** 远程服务器就绪等待超时（秒），可通过 {@code start.timeout} 配置。 */
    @ConfigProperty(name = "start.timeout", defaultValue = "120")
    long startTimeout;

    private static final Logger LOGGER = Logger.getLogger(RemoteKeycloakServerSupplier.class);

    /** {@inheritDoc} */
    @Override
    public KeycloakServer getServer() {
        return new RemoteKeycloakServer(startTimeout);
    }

    /** {@inheritDoc} — 远程模式假定外部 Keycloak 已配置数据库。 */
    @Override
    public boolean requiresDatabase() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getAlias() {
        return "remote";
    }

    /** {@inheritDoc} */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
