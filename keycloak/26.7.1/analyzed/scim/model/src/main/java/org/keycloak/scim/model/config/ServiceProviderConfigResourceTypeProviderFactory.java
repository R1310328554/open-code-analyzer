package org.keycloak.scim.model.config;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.scim.resource.spi.ScimResourceTypeProviderFactory;

/**
 * {@link ServiceProviderConfigResourceTypeProvider} 的工厂实现。
 * <p>在 Keycloak 启动时注册 ServiceProviderConfig 单例资源类型。</p>
 */
public class ServiceProviderConfigResourceTypeProviderFactory implements ScimResourceTypeProviderFactory<ServiceProviderConfigResourceTypeProvider> {

    @Override
    public ServiceProviderConfigResourceTypeProvider create(KeycloakSession session) {
        return new ServiceProviderConfigResourceTypeProvider(session);
    }

    @Override
    public void init(Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    /** 返回工厂标识符。 */
    @Override
    public String getId() {
        return "ServiceProviderConfig";
    }
}
