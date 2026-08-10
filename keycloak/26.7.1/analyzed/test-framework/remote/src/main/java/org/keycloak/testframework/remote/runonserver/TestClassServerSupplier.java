package org.keycloak.testframework.remote.runonserver;

import java.util.List;

import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;

import com.sun.net.httpserver.HttpServer;

/**
 * 为 {@link InjectTestClassServer} 注解提供 {@link TestClassServer} 实例的供应器。
 * <p>
 * 在嵌入式 {@link HttpServer} 上注册测试类下载端点。
 */
public class TestClassServerSupplier implements Supplier<TestClassServer, InjectTestClassServer> {

    /** {@inheritDoc} 声明对 {@link HttpServer} 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<TestClassServer, InjectTestClassServer> instanceContext) {
        return DependenciesBuilder.create(HttpServer.class).build();
    }

    /** {@inheritDoc} 创建并注册测试类 HTTP 端点。 */
    @Override
    public TestClassServer getValue(InstanceContext<TestClassServer, InjectTestClassServer> instanceContext) {
        HttpServer httpServer = instanceContext.getDependency(HttpServer.class);
        return new TestClassServer(httpServer);
    }

    /** {@inheritDoc} 所有 {@link InjectTestClassServer} 实例均视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<TestClassServer, InjectTestClassServer> a, RequestedInstance<TestClassServer, InjectTestClassServer> b) {
        return true;
    }

    /** {@inheritDoc} 关闭测试类服务器并移除 HTTP 上下文。 */
    @Override
    public void close(InstanceContext<TestClassServer, InjectTestClassServer> instanceContext) {
        instanceContext.getValue().close();
    }
}
