package org.keycloak.testframework.database;

import org.keycloak.testframework.annotations.InjectTestDatabase;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;
import org.keycloak.testframework.injection.SupplierOrder;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;
import org.keycloak.testframework.server.KeycloakServerConfigInterceptor;

/**
 * 测试数据库 {@link Supplier} 抽象基类。
 * <p>
 * 负责根据 {@link InjectTestDatabase} 注解创建并启动 {@link TestDatabase} 实例，
 * 并在 Keycloak 服务器启动前注入数据库相关配置选项。
 */
public abstract class AbstractDatabaseSupplier implements Supplier<TestDatabase, InjectTestDatabase>, KeycloakServerConfigInterceptor<TestDatabase, InjectTestDatabase> {

    /** {@inheritDoc} 构建数据库配置、启动测试数据库并返回实例。 */
    @Override
    public TestDatabase getValue(InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        DatabaseConfigBuilder builder = DatabaseConfigBuilder
              .create()
              .preventReuse(instanceContext.getLifeCycle() != LifeCycle.GLOBAL);

        DatabaseConfig config = SupplierHelpers.getInstance(instanceContext.getAnnotation().config());
        builder = config.configure(builder);

        TestDatabase testDatabase = getTestDatabase();
        testDatabase.start(builder.build());
        return testDatabase;
    }

    /** {@inheritDoc} 仅当 {@code config} 注解值相同时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<TestDatabase, InjectTestDatabase> a, RequestedInstance<TestDatabase, InjectTestDatabase> b) {
        return a.getAnnotation().config().equals(b.getAnnotation().config());
    }

    /** {@inheritDoc} 默认生命周期为 {@link LifeCycle#GLOBAL}。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    /** 由子类提供具体的 {@link TestDatabase} 实现。 */
    abstract TestDatabase getTestDatabase();

    /** {@inheritDoc} 关闭并停止测试数据库。 */
    @Override
    public void close(InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        instanceContext.getValue().stop();
    }

    /** {@inheritDoc} 将测试数据库的 {@link TestDatabase#serverConfig()} 合并到服务器配置。 */
    @Override
    public KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        return serverConfig.options(instanceContext.getValue().serverConfig());
    }

    /** {@inheritDoc} 在 Keycloak 服务器启动前执行拦截。 */
    @Override
    public int order() {
        return SupplierOrder.BEFORE_KEYCLOAK_SERVER;
    }
}
