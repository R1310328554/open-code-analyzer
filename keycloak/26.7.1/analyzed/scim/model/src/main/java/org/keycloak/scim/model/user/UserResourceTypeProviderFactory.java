package org.keycloak.scim.model.user;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.scim.resource.spi.ScimResourceTypeProviderFactory;

/**
 * SCIM Users 资源类型的 SPI 工厂，注册 ID 为 {@code Users}。
 */
public class UserResourceTypeProviderFactory implements ScimResourceTypeProviderFactory<UserResourceTypeProvider> {

    /** 为会话创建 {@link UserResourceTypeProvider} 实例。 */
    @Override
    public UserResourceTypeProvider create(KeycloakSession session) {
        return new UserResourceTypeProvider(session);
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

    /** 返回 SCIM 端点路径段 {@code Users}。 */
    @Override
    public String getId() {
        return "Users";
    }
}
