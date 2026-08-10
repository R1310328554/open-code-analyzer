package org.keycloak.testframework.database;

import org.keycloak.testframework.util.ContainerImages;

import org.jboss.logging.Logger;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * MariaDB Testcontainers 测试数据库实现。
 * <p>
 * 使用官方 {@link MariaDBContainer}，镜像名由 {@link ContainerImages} 解析。
 */
class MariaDBTestDatabase extends AbstractContainerTestDatabase {

    private static final Logger LOGGER = Logger.getLogger(MariaDBTestDatabase.class);

    /** 配置别名与容器镜像逻辑名。 */
    public static final String NAME = "mariadb";

    /** {@inheritDoc} 创建兼容 MariaDB 镜像的 Testcontainers 容器。 */
    @Override
    public JdbcDatabaseContainer<?> createContainer() {
        return new MariaDBContainer(DockerImageName.parse(ContainerImages.getContainerImageName(NAME)).asCompatibleSubstituteFor(NAME));
    }

    /** {@inheritDoc} 返回 {@link #NAME} 作为 Keycloak 数据库厂商标识。 */
    @Override
    public String getDatabaseVendor() {
        return NAME;
    }

    /** {@inheritDoc} 返回本模块 JBoss 日志记录器。 */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
