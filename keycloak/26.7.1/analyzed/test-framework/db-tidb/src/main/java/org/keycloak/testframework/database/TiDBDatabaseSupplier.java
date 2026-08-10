package org.keycloak.testframework.database;

import org.keycloak.common.Profile;
import org.keycloak.testframework.annotations.InjectTestDatabase;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;

/**
 * 基于 Testcontainers 启动 TiDB 容器并启用 {@link Profile.Feature#DB_TIDB} 的测试数据库供应器。
 * <p>
 * 在注入数据库配置的同时打开 TiDB 特性开关，使 Keycloak 使用 TiDB 方言。
 */
public class TiDBDatabaseSupplier extends AbstractContainerDatabaseSupplier {

    /** {@inheritDoc} 返回 {@code tidb} 作为注入别名。 */
    @Override
    public String getAlias() {
        return "tidb";
    }

    /**
     * {@inheritDoc}
     * <p>
     * 在父类注入数据库配置后，启用 {@link Profile.Feature#DB_TIDB} 特性。
     *
     * @param serverConfig 服务器配置构建器
     * @param instanceContext 当前测试数据库实例上下文
     * @return 已启用 TiDB 特性的配置构建器
     */
    @Override
    public KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        KeycloakServerConfigBuilder builder = super.intercept(serverConfig, instanceContext);
        builder.features(Profile.Feature.DB_TIDB);
        return builder;
    }

    /** {@inheritDoc} 创建 {@link TiDBTestDatabase} 实例。 */
    @Override
    TestDatabase getTestDatabase() {
        return new TiDBTestDatabase();
    }

}
