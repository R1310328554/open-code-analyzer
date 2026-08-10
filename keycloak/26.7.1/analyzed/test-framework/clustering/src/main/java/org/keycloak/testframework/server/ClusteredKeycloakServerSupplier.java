package org.keycloak.testframework.server;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * 集群 Keycloak 服务器的 {@link KeycloakServer} Supplier，别名 {@code cluster}。
 * 通过 MicroProfile 配置控制节点数、镜像、启动超时与无状态模式。
 */
public class ClusteredKeycloakServerSupplier extends AbstractKeycloakServerSupplier {

    private static final Logger LOGGER = Logger.getLogger(ClusteredKeycloakServerSupplier.class);

    /** 启动超时（秒）。 */
    @ConfigProperty(name = "start.timeout", defaultValue = "300")
    long startTimeout;

    /** 集群容器数量。 */
    @ConfigProperty(name = "numContainer", defaultValue = "2")
    int numContainers = 2;

    /** 镜像列表（逗号分隔），默认快照镜像占位符。 */
    @ConfigProperty(name = "images", defaultValue = ClusteredKeycloakServer.SNAPSHOT_IMAGE)
    String images = ClusteredKeycloakServer.SNAPSHOT_IMAGE;

    /** 是否启用无状态特性。 */
    @ConfigProperty(name = "stateless", defaultValue = "false")
    boolean stateless;

    /** 构造 {@link ClusteredKeycloakServer} 实例。 */
    @Override
    public KeycloakServer getServer() {
        return new ClusteredKeycloakServer(numContainers, images, startTimeout, stateless);
    }

    /** 集群模式需要持久化数据库。 */
    @Override
    public boolean requiresDatabase() {
        return true;
    }

    /** @return Supplier 别名 {@code cluster} */
    @Override
    public String getAlias() {
        return "cluster";
    }

    /** @return 本 Supplier 使用的日志器 */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
