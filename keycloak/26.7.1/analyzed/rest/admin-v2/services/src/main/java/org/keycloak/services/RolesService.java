package org.keycloak.services;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.RoleContainerResource;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * 为 {@link RoleContainerModel} 构建 {@link RoleContainerResource} 的辅助服务。
 */
public class RolesService {
    private final KeycloakSession session;
    private final RealmModel realm;
    private final AdminPermissionEvaluator permissions;
    private final AdminEventBuilder adminEventBuilder;

    /** 注入会话、领域、权限评估器与管理事件构建器。 */
    public RolesService(KeycloakSession session, RealmModel realm, AdminPermissionEvaluator permissions, AdminEventBuilder adminEventBuilder) {
        this.session = session;
        this.realm = realm;
        this.permissions = permissions;
        this.adminEventBuilder = adminEventBuilder;
    }

    /** 为指定角色容器创建并配置 {@link RoleContainerResource}。 */
    public RoleContainerResource resource(RoleContainerModel roleContainer) {
        var resource = new RoleContainerResource(session, session.getContext().getUri(), realm, permissions, adminEventBuilder);
        resource.setRoleContainer(roleContainer);
        return resource;
    }
}
