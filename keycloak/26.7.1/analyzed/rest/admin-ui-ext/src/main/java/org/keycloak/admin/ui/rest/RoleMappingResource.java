package org.keycloak.admin.ui.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * Admin UI 角色映射 REST 资源的抽象基类，持有会话、领域与权限评估器。
 */
public abstract class RoleMappingResource {
    /** Keycloak 会话。 */
    protected final KeycloakSession session;
    /** 目标领域。 */
    protected final RealmModel realm;
    /** 管理权限评估器。 */
    protected final AdminPermissionEvaluator auth;

    public RoleMappingResource(KeycloakSession session, RealmModel realm, AdminPermissionEvaluator auth) {
        this.session = session;
        this.realm = realm;
        this.auth = auth;
    }
}
