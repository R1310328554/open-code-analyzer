package org.keycloak.authorization.store.syncronization;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.OrganizationModel.OrganizationRemovedEvent;
import org.keycloak.provider.ProviderFactory;

/**
 * 组织删除事件同步器：从授权 FGAP 模式中移除对应资源对象。
 */
public class OrganizationSynchronizer implements Synchronizer<OrganizationRemovedEvent> {

    /** 组织移除时调用 {@link AdminPermissionsSchema} 清理授权资源。 */
    @Override
    public void synchronize(OrganizationRemovedEvent event, KeycloakSessionFactory factory) {
        ProviderFactory<AuthorizationProvider> providerFactory = factory.getProviderFactory(AuthorizationProvider.class);
        AuthorizationProvider authorizationProvider = providerFactory.create(event.getKeycloakSession());

        AdminPermissionsSchema.SCHEMA.removeResourceObject(authorizationProvider, event);
    }
}
