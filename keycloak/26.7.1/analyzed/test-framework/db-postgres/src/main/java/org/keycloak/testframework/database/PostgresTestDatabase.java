package org.keycloak.testframework.database;

import org.keycloak.testframework.util.ContainerImages;

import org.jboss.logging.Logger;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 使用 {@link PostgreSQLContainer} 启动 PostgreSQL 镜像的测试数据库实现。
 * <p>
 * 镜像名从 {@link org.keycloak.testframework.util.ContainerImages} 读取。
 */
public class PostgresTestDatabase extends AbstractContainerTestDatabase {

    private static final Logger LOGGER = Logger.getLogger(PostgresTestDatabase.class);

    /** 数据库供应器别名与 vendor 标识。 */
    public static final String NAME = "postgres";

    /** 包级可见构造器，由 {@link PostgresDatabaseSupplier} 实例化。 */
    PostgresTestDatabase() {}

    /** {@inheritDoc} 创建并配置 PostgreSQL Testcontainers 容器。 */
    @Override
    public JdbcDatabaseContainer<?> createContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse(ContainerImages.getContainerImageName(NAME)).asCompatibleSubstituteFor(NAME));
    }

    /** {@inheritDoc} 返回 {@link #NAME}。 */
    @Override
    public String getDatabaseVendor() {
        return NAME;
    }

    /** {@inheritDoc} 返回本类的 JBoss 日志记录器。 */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
