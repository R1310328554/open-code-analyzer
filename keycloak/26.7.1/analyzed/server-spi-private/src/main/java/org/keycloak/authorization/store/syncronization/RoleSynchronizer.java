package org.keycloak.authorization.store.syncronization;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RoleContainerModel.RoleRemovedEvent;
import org.keycloak.provider.ProviderFactory;

/**
 * 角色删除事件同步器：从授权 FGAP 模式中移除对应资源对象。
 */
public class RoleSynchronizer implements Synchronizer<RoleRemovedEvent> {

    /** 角色移除时调用 {@link AdminPermissionsSchema} 清理授权资源。 */
    @Override
    public void synchronize(RoleRemovedEvent event, KeycloakSessionFactory factory) {
        ProviderFactory<AuthorizationProvider> providerFactory = factory.getProviderFactory(AuthorizationProvider.class);
        AuthorizationProvider authorizationProvider = providerFactory.create(event.getKeycloakSession());

        AdminPermissionsSchema.SCHEMA.removeResourceObject(authorizationProvider, event);
    }
}
