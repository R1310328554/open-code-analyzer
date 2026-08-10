package org.keycloak.scim.model.schema;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.scim.resource.spi.ScimResourceTypeProviderFactory;

/**
 * SCIM Schemas 端点的 SPI 工厂。
 */
public class SchemaResourceTypeProviderFactory implements ScimResourceTypeProviderFactory<SchemaResourceTypeProvider> {

    /** 工厂与端点 ID：{@code Schemas}。 */
    public static final String ID = "Schemas";

    @Override
    public SchemaResourceTypeProvider create(KeycloakSession session) {
        return new SchemaResourceTypeProvider(session);
    }

    @Override
    public void init(Scope config) {
        // 无需初始化
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // 无需后置初始化
    }

    @Override
    public void close() {
        // 无需要关闭的资源
    }

    @Override
    public String getId() {
        return ID;
    }
}
