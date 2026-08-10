package org.keycloak.protocol.oidc.endpoints;

import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * 授权端点附加校验 {@link AuthorizationEndpointCheckProvider} 的工厂接口。
 * <p>实现 {@link EnvironmentDependentProviderFactory}，可按运行环境条件加载扩展校验逻辑。</p>
 */
public interface AuthorizationEndpointCheckProviderFactory extends ProviderFactory<AuthorizationEndpointCheckProvider>, EnvironmentDependentProviderFactory {
}
