package org.keycloak.testframework.oauth;

import java.util.List;

import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.oauth.annotations.InjectTestApp;

import com.sun.net.httpserver.HttpServer;

/**
 * 为 {@link InjectTestApp} 注解提供 {@link TestApp} 实例的供应器。
 * <p>
 * 在嵌入式 {@link HttpServer} 上注册模拟 OAuth 客户端回调端点。
 */
public class TestAppSupplier implements Supplier<TestApp, InjectTestApp> {

    /** {@inheritDoc} 声明对 {@link HttpServer} 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<TestApp, InjectTestApp> instanceContext) {
        return DependenciesBuilder.create(HttpServer.class).build();
    }

    /** {@inheritDoc} 创建并注册模拟 OAuth 测试应用。 */
    @Override
    public TestApp getValue(InstanceContext<TestApp, InjectTestApp> instanceContext) {
        HttpServer httpServer = instanceContext.getDependency(HttpServer.class);
        return new TestApp(httpServer);
    }

    /** {@inheritDoc} 所有 {@link InjectTestApp} 实例均视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<TestApp, InjectTestApp> a, RequestedInstance<TestApp, InjectTestApp> b) {
        return true;
    }

    /** {@inheritDoc} 关闭测试应用并移除 HTTP 上下文。 */
    @Override
    public void close(InstanceContext<TestApp, InjectTestApp> instanceContext) {
        instanceContext.getValue().close();
    }

}
