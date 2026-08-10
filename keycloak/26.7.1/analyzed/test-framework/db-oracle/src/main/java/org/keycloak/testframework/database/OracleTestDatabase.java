package org.keycloak.testframework.database;

import org.keycloak.testframework.util.ContainerImages;

import org.jboss.logging.Logger;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 使用 {@link OracleContainer} 启动 Oracle Free 镜像的测试数据库实现。
 * <p>
 * 镜像名从 {@link org.keycloak.testframework.util.ContainerImages} 读取，
 * 并兼容 {@code gvenzl/oracle-free} 标签。
 */
class OracleTestDatabase extends AbstractContainerTestDatabase {

    private static final Logger LOGGER = Logger.getLogger(OracleTestDatabase.class);

    /** 数据库供应器别名与 vendor 标识。 */
    public static final String NAME = "oracle";

    /** {@inheritDoc} 创建并配置 Oracle Testcontainers 容器。 */
    @Override
    public JdbcDatabaseContainer<?> createContainer() {
        return new OracleContainer(DockerImageName.parse(ContainerImages.getContainerImageName(NAME)).asCompatibleSubstituteFor("gvenzl/oracle-free"));
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
