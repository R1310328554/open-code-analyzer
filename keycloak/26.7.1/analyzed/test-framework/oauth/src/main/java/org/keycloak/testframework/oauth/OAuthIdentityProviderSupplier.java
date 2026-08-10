package org.keycloak.testframework.oauth;

import java.util.List;

import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.injection.SupplierHelpers;
import org.keycloak.testframework.oauth.annotations.InjectOAuthIdentityProvider;

import com.sun.net.httpserver.HttpServer;

/**
 * 为 {@link InjectOAuthIdentityProvider} 注解提供 {@link OAuthIdentityProvider} 实例的供应器。
 * <p>
 * 依赖嵌入式 {@link HttpServer}，并根据注解中的 {@link OAuthIdentityProviderConfig} 构建模拟 IdP。
 */
public class OAuthIdentityProviderSupplier implements Supplier<OAuthIdentityProvider, InjectOAuthIdentityProvider> {

    /** {@inheritDoc} 声明对 {@link HttpServer} 的依赖。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<OAuthIdentityProvider, InjectOAuthIdentityProvider> instanceContext) {
        return DependenciesBuilder.create(HttpServer.class).build();
    }

    /** {@inheritDoc} 创建并注册模拟 OAuth 身份提供者。 */
    @Override
    public OAuthIdentityProvider getValue(InstanceContext<OAuthIdentityProvider, InjectOAuthIdentityProvider> instanceContext) {
        HttpServer httpServer = instanceContext.getDependency(HttpServer.class);
        OAuthIdentityProviderConfig config = SupplierHelpers.getInstance(instanceContext.getAnnotation().config());
        OAuthIdentityProviderConfigBuilder configBuilder = new OAuthIdentityProviderConfigBuilder();
        OAuthIdentityProviderConfigBuilder.OAuthIdentityProviderConfiguration configuration = config.configure(configBuilder).build();

        return new OAuthIdentityProvider(httpServer, configuration);
    }

    /** {@inheritDoc} 关闭身份提供者并移除 HTTP 上下文。 */
    @Override
    public void close(InstanceContext<OAuthIdentityProvider, InjectOAuthIdentityProvider> instanceContext) {
        instanceContext.getValue().close();
    }

    /** {@inheritDoc} 仅当注解配置完全一致时视为兼容。 */
    @Override
    public boolean compatible(InstanceContext<OAuthIdentityProvider, InjectOAuthIdentityProvider> a, RequestedInstance<OAuthIdentityProvider, InjectOAuthIdentityProvider> b) {
        return a.getAnnotation().equals(b.getAnnotation());
    }

}
