
package org.keycloak.testframework.oauth;

import java.util.List;

import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;
import org.keycloak.testframework.oauth.annotations.InjectCimdProvider;

import com.sun.net.httpserver.HttpServer;

/**
 * 为 {@link InjectCimdProvider} 注解提供 {@link CimdProvider} 实例的供应器。
 *
 * @author rmartinc
 */
public class CimdProviderSupplier implements Supplier<CimdProvider, InjectCimdProvider> {

    /** {@inheritDoc} 声明对 {@link HttpServer} 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<CimdProvider, InjectCimdProvider> instanceContext) {
        return DependenciesBuilder.create(HttpServer.class).build();
    }

    /** {@inheritDoc} 根据注解中的 {@link OIDCClientRepresentationBuilder} 创建 CIMD 端点。 */
    @Override
    public CimdProvider getValue(InstanceContext<CimdProvider, InjectCimdProvider> instanceContext) {
        HttpServer httpServer = instanceContext.getDependency(HttpServer.class);
        OIDCClientRepresentationBuilder clientBuilder = SupplierHelpers.getInstance(instanceContext.getAnnotation().config());
        return new CimdProvider(httpServer, clientBuilder.build());
    }

    /** {@inheritDoc} 关闭 CIMD 提供者并移除 HTTP 上下文。 */
    @Override
    public void close(InstanceContext<CimdProvider, InjectCimdProvider> instanceContext) {
        instanceContext.getValue().close();
    }

    /** {@inheritDoc} 仅当注解配置完全一致时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<CimdProvider, InjectCimdProvider> a, RequestedInstance<CimdProvider, InjectCimdProvider> b) {
        return a.getAnnotation().equals(b.getAnnotation());
    }
}
