package org.keycloak.testframework.database;

import org.keycloak.testframework.util.ContainerImages;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.tidb.TiDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 使用 {@link TiDBContainer} 启动 TiDB 镜像的测试数据库实现。
 * <p>
 * TiDB 官方镜像不支持动态修改库名/用户名/密码，因此覆写相关方法并在值不变时返回自身，
 * 否则抛出 {@link UnsupportedOperationException}。
 */
class TiDBTestDatabase extends AbstractContainerTestDatabase {

    private static final Logger LOGGER = Logger.getLogger(TiDBTestDatabase.class);

    /** 数据库供应器别名与 vendor 标识。 */
    public static final String NAME = "tidb";

    /**
     * {@inheritDoc}
     * <p>
     * 创建 TiDB 容器并暴露 4000 端口；镜像兼容 {@code pingcap/tidb} 标签。
     */
    @Override
    public JdbcDatabaseContainer<?> createContainer() {
        return new TiDBContainer(DockerImageName.parse(ContainerImages.getContainerImageName(NAME)).asCompatibleSubstituteFor("pingcap/tidb")){
            /** TiDB 镜像不支持修改库名；仅在与当前值相同时返回自身。 */
            @Override
            public TiDBContainer withDatabaseName(String databaseName) {
                if(StringUtils.equals(this.getDatabaseName(), databaseName)) {
                    return this;
                }
                throw new UnsupportedOperationException("The TiDB docker image does not currently support this");
            }

            /** TiDB 镜像不支持修改用户名；仅在与当前值相同时返回自身。 */
            @Override
            public TiDBContainer withUsername(String username) {
                if(StringUtils.equals(this.getUsername(), username)) {
                    return this;
                }
                throw new UnsupportedOperationException("The TiDB docker image does not currently support this");
            }

            /** TiDB 镜像不支持修改密码；仅在与当前值相同时返回自身。 */
            @Override
            public TiDBContainer withPassword(String password) {
                if(StringUtils.equals(this.getPassword(), password)) {
                    return this;
                }
                throw new UnsupportedOperationException("The TiDB docker image does not currently support this");
            }
        }.withExposedPorts(4000);
    }

    /** {@inheritDoc} 返回 {@code tidb}。 */
    @Override
    public String getDatabaseVendor() {
        return "tidb";
    }

    /** {@inheritDoc} 返回本类的 JBoss 日志记录器。 */
    @Override
    public Logger getLogger() {
        return LOGGER;
    }


    /** {@inheritDoc} TiDB 测试默认使用 {@code test} 库。 */
    @Override
    public String getDatabase() {
        return "test";
    }

    /** {@inheritDoc} TiDB 测试默认使用 {@code root} 用户。 */
    @Override
    public String getUsername() {
        return "root";
    }

    /** {@inheritDoc} TiDB 测试默认无密码。 */
    @Override
    public String getPassword() {
        return "";
    }
}
