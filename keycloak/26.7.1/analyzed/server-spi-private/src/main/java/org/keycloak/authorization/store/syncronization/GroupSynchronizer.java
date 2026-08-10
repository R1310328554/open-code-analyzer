package org.keycloak.authorization.store.syncronization;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.models.GroupModel.GroupRemovedEvent;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * 组删除事件同步器：从授权 FGAP 模式中移除对应资源对象。
 */
public class GroupSynchronizer implements Synchronizer<GroupRemovedEvent> {

    /** 组移除时调用 {@link AdminPermissionsSchema} 清理授权资源。 */
    @Override
    public void synchronize(GroupRemovedEvent event, KeycloakSessionFactory factory) {
        ProviderFactory<AuthorizationProvider> providerFactory = factory.getProviderFactory(AuthorizationProvider.class);
        AuthorizationProvider authorizationProvider = providerFactory.create(event.getKeycloakSession());

        AdminPermissionsSchema.SCHEMA.removeResourceObject(authorizationProvider, event);
    }
}
