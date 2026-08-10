package org.keycloak.testframework.remote;

import org.keycloak.testframework.annotations.InjectTestDatabase;
import org.keycloak.testframework.database.TestDatabase;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierOrder;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;
import org.keycloak.testframework.server.KeycloakServerConfigInterceptor;

/**
 * 供应 {@link RemoteProviders} 并在 Keycloak 服务器配置中声明 remote-providers 依赖。
 * <p>
 * 作为 {@link KeycloakServerConfigInterceptor}，在启动测试服务器前注入 Maven 依赖，
 * 使 {@link RunOnServerRealmResourceProvider} 等测试 SPI 可用。
 */
public class RemoteProvidersSupplier implements Supplier<RemoteProviders, InjectRemoteProviders>, KeycloakServerConfigInterceptor<TestDatabase, InjectTestDatabase> {

    /** {@inheritDoc} 返回新的 {@link RemoteProviders} 标记实例。 */
    @Override
    public RemoteProviders getValue(InstanceContext<RemoteProviders, InjectRemoteProviders> instanceContext) {
        return new RemoteProviders();
    }

    /** {@inheritDoc} 默认 {@link LifeCycle#GLOBAL}。 */
    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    /** {@inheritDoc} 任意两个 {@link RemoteProviders} 请求均兼容。 */
    @Override
    public boolean compatible(InstanceContext<RemoteProviders, InjectRemoteProviders> a, RequestedInstance<RemoteProviders, InjectRemoteProviders> b) {
        return true;
    }

    /** {@inheritDoc} 在 Keycloak 服务器启动前执行配置拦截。 */
    @Override
    public int order() {
        return SupplierOrder.BEFORE_KEYCLOAK_SERVER;
    }

    /** {@inheritDoc} 添加 {@code keycloak-test-framework-remote-providers} 模块依赖。 */
    @Override
    public KeycloakServerConfigBuilder intercept(KeycloakServerConfigBuilder serverConfig, InstanceContext<TestDatabase, InjectTestDatabase> instanceContext) {
        return serverConfig.dependency("org.keycloak.testframework", "keycloak-test-framework-remote-providers");
    }
}
