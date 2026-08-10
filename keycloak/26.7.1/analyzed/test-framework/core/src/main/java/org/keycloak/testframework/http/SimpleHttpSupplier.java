package org.keycloak.testframework.http;

import java.util.List;

import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.testframework.annotations.InjectSimpleHttp;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;

import org.apache.http.client.HttpClient;

/**
 * 为 {@link InjectSimpleHttp} 注入 Keycloak {@link SimpleHttp} 客户端的供应器。
 * <p>
 * 基于已注入的 {@link HttpClient} 创建 {@link SimpleHttp} 实例。
 */
public class SimpleHttpSupplier implements Supplier<SimpleHttp, InjectSimpleHttp> {

    /** {@inheritDoc} 声明对 {@link HttpClient} 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<SimpleHttp, InjectSimpleHttp> instanceContext) {
        return DependenciesBuilder.create(HttpClient.class).build();
    }

    /** {@inheritDoc} 使用依赖 HttpClient 创建 SimpleHttp。 */
    @Override
    public SimpleHttp getValue(InstanceContext<SimpleHttp, InjectSimpleHttp> instanceContext) {
        HttpClient httpClient = instanceContext.getDependency(HttpClient.class);
        return SimpleHttp.create(httpClient);
    }

    /** {@inheritDoc} 所有 SimpleHttp 实例互相兼容。 */
    @Override
    public boolean compatible(InstanceContext<SimpleHttp, InjectSimpleHttp> a, RequestedInstance<SimpleHttp, InjectSimpleHttp> b) {
        return true;
    }

}
