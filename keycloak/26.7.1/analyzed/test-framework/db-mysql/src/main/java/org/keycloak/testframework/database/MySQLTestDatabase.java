package org.keycloak.testframework.database;

import org.keycloak.testframework.util.ContainerImages;

import org.jboss.logging.Logger;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * MySQL Testcontainers 测试数据库实现。
 * <p>
 * JDBC URL 追加 {@code allowPublicKeyRetrieval=true} 以支持容器内默认认证插件。
 */
class MySQLTestDatabase extends AbstractContainerTestDatabase {

    private static final Logger LOGGER = Logger.getLogger(MySQLTestDatabase.class);

    /** 配置别名与容器镜像逻辑名。 */
    public static final String NAME = "mysql";

    /** {@inheritDoc} 创建兼容 MySQL 镜像的 Testcontainers 容器。 */
    @Override
    public JdbcDatabaseContainer<?> createContainer() {
        return new MySQLContainer<>(DockerImageName.parse(ContainerImages.getContainerImageName(NAME)).asCompatibleSubstituteFor(NAME));
    }

    @Override
    public String getDatabaseVendor() {
        return NAME;
    }

    /** {@inheritDoc} 在父类 URL 后追加公钥检索参数。 */
    @Override
    public String getJdbcUrl(boolean internal) {
        return super.getJdbcUrl(internal) + "?allowPublicKeyRetrieval=true";
    }

    /** {@inheritDoc} 返回本模块 JBoss 日志记录器。 */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
