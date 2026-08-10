package org.keycloak.scim.model.resourcetype;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.scim.resource.spi.ScimResourceTypeProviderFactory;

/**
 * SCIM ResourceTypes 端点的 SPI 工厂，ID 为 {@code ResourceTypes}。
 */
public class ResourceTypeProviderFactory implements ScimResourceTypeProviderFactory<ResourceTypeProvider> {

    /** 创建 {@link ResourceTypeProvider} 实例。 */
    @Override
    public ResourceTypeProvider create(KeycloakSession session) {
        return new ResourceTypeProvider(session);
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

    /** 返回端点 ID {@code ResourceTypes}。 */
    @Override
    public String getId() {
        return "ResourceTypes";
    }
}
