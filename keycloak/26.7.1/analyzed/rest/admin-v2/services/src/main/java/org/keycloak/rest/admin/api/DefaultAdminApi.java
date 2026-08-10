package org.keycloak.rest.admin.api;

import jakarta.ws.rs.NotFoundException;

import org.keycloak.admin.api.AdminApi;
import org.keycloak.admin.api.client.ClientsApi;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.rest.admin.api.client.DefaultClientsApi;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.fgap.AdminPermissions;

/**
 * {@link org.keycloak.admin.api.AdminApi} 默认实现：认证 realm 管理员请求并组装 v2 子资源 API。
 */
public class DefaultAdminApi implements AdminApi {
    private final KeycloakSession session;
    private final RealmModel realm;
    private final AdminPermissionEvaluator permissions;

    /** 认证管理员、解析 realm 并初始化权限评估器。 */
    public DefaultAdminApi(KeycloakSession session, String realmName) {
        this.session = session;
        var authInfo = AdminRoot.authenticateRealmAdminRequest(session);
        RealmModel realm = session.realms().getRealmByName(realmName);
        if (realm == null) throw new NotFoundException("Realm not found.");
        session.getContext().setRealm(realm);
        this.realm = realm;
        this.permissions = AdminPermissions.evaluator(session, realm, authInfo);
    }

    /** {@inheritDoc} 返回客户端 Admin API v2 实现。 */
    @Override
    public ClientsApi clientsV2() {
        return new DefaultClientsApi(session, realm, permissions);
    }
}
