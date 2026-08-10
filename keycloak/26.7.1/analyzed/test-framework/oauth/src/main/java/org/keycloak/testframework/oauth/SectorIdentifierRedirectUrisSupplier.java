package org.keycloak.testframework.oauth;

import java.util.List;

import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.oauth.annotations.InjectSectorIdentifierRedirectUrisProvider;

import com.sun.net.httpserver.HttpServer;

/**
 * 为 {@link InjectSectorIdentifierRedirectUrisProvider} 提供 {@link SectorIdentifierRedirectUrisProvider} 的供应器。
 *
 * @author rmartinc
 */
public class SectorIdentifierRedirectUrisSupplier implements Supplier<SectorIdentifierRedirectUrisProvider, InjectSectorIdentifierRedirectUrisProvider> {

    /** {@inheritDoc} 声明对 {@link HttpServer} 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<SectorIdentifierRedirectUrisProvider, InjectSectorIdentifierRedirectUrisProvider> instanceContext) {
        return DependenciesBuilder.create(HttpServer.class).build();
    }

    /** {@inheritDoc} 根据注解中的 URI 列表创建 sector identifier 端点。 */
    @Override
    public SectorIdentifierRedirectUrisProvider getValue(InstanceContext<SectorIdentifierRedirectUrisProvider, InjectSectorIdentifierRedirectUrisProvider> instanceContext) {
        HttpServer httpServer = instanceContext.getDependency(HttpServer.class);
        String[] uris = instanceContext.getAnnotation().value();
        return new SectorIdentifierRedirectUrisProvider(httpServer, uris);
    }

    /** {@inheritDoc} 仅当注解配置完全一致时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<SectorIdentifierRedirectUrisProvider, InjectSectorIdentifierRedirectUrisProvider> a, RequestedInstance<SectorIdentifierRedirectUrisProvider, InjectSectorIdentifierRedirectUrisProvider> b) {
        return a.getAnnotation().equals(b.getAnnotation());
    }

    /** {@inheritDoc} 关闭 provider 并移除 HTTP 上下文。 */
    @Override
    public void close(InstanceContext<SectorIdentifierRedirectUrisProvider, InjectSectorIdentifierRedirectUrisProvider> instanceContext) {
        instanceContext.getValue().close();
    }
}
