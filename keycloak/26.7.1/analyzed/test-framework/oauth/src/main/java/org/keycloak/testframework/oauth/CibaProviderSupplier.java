package org.keycloak.testframework.oauth;

import java.util.List;

import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.oauth.annotations.InjectCibaProvider;

import com.sun.net.httpserver.HttpServer;

/**
 * 为 {@link InjectCibaProvider} 注解提供 {@link CibaProvider} 实例的供应器。
 *
 * @author rmartinc
 */
public class CibaProviderSupplier implements Supplier<CibaProvider, InjectCibaProvider>{

    /** {@inheritDoc} 声明对 {@link HttpServer} 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<CibaProvider, InjectCibaProvider> instanceContext) {
        return DependenciesBuilder.create(HttpServer.class).build();
    }

    /** {@inheritDoc} 在嵌入式 HTTP 服务器上创建 CIBA 模拟端点。 */
    @Override
    public CibaProvider getValue(InstanceContext<CibaProvider, InjectCibaProvider> instanceContext) {
        HttpServer httpServer = instanceContext.getDependency(HttpServer.class);
        return new CibaProvider(httpServer);
    }

    /** {@inheritDoc} 仅当注解配置完全一致时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<CibaProvider, InjectCibaProvider> a, RequestedInstance<CibaProvider, InjectCibaProvider> b) {
        return a.getAnnotation().equals(b.getAnnotation());
    }

    /** {@inheritDoc} 关闭 CIBA 提供者并移除 HTTP 上下文。 */
    @Override
    public void close(InstanceContext<CibaProvider, InjectCibaProvider> instanceContext) {
        instanceContext.getValue().close();
    }
}
