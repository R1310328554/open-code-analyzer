package org.keycloak.testframework.database;

import org.keycloak.testframework.util.ContainerImages;

import org.jboss.logging.Logger;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * EnterpriseDB Testcontainers 测试数据库实现。
 * <p>
 * 使用自定义 {@link KeycloakEnterpriseDbContainer}，对 Keycloak 报告的数据库厂商为 {@code postgres}。
 */
public class EnterpriseDbTestDatabase extends AbstractContainerTestDatabase {
    private static final Logger LOGGER = Logger.getLogger(EnterpriseDbTestDatabase.class);

    /** 配置别名与 {@code containers.properties} 中的逻辑容器名。 */
    public static final String NAME = "edb";

    /** {@inheritDoc} 基于 {@link ContainerImages} 解析的镜像创建 EDB 容器。 */
    @Override
    public JdbcDatabaseContainer<?> createContainer() {
        return new KeycloakEnterpriseDbContainer(DockerImageName.parse(ContainerImages.getContainerImageName(NAME)));
    }

    /** {@inheritDoc} Keycloak 侧使用 PostgreSQL 驱动与方言。 */
    @Override
    public String getDatabaseVendor() {
        return "postgres";
    }

    /** {@inheritDoc} 返回本模块 JBoss 日志记录器。 */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
