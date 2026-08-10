package org.keycloak.admin.ui.rest;

import jakarta.ws.rs.Path;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * Admin UI 扩展 REST 根资源，按路径委派认证管理、角色映射、会话等子端点。
 */
public final class AdminExtResource {
    /** 当前 Keycloak 会话。 */
    private KeycloakSession session;
    /** 当前操作的领域模型。 */
    private RealmModel realm;
    /** 细粒度管理权限评估器。 */
    private AdminPermissionEvaluator auth;
    /** 管理事件审计构建器。 */
    private AdminEventBuilder adminEvent;

    public AdminExtResource(KeycloakSession session, RealmModel realm, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.realm = realm;
        this.auth = auth;
        this.adminEvent = adminEvent;
    }

    /** 认证流与必需操作管理子资源。 */
    @Path("/authentication-management")
    public AuthenticationManagementResource authenticationManagement() {
        return new AuthenticationManagementResource(session, realm, auth);
    }

    /** 暴力破解锁定用户查询子资源。 */
    @Path("/brute-force-user")
    public BruteForceUsersResource bruteForceUsers() {
        return new BruteForceUsersResource(session, realm, auth);
    }

    /** 可分配角色列表子资源。 */
    @Path("/available-roles")
    public AvailableRoleMappingResource availableRoles() {
        return new AvailableRoleMappingResource(session, realm, auth);
    }

    /** 可用事件监听器提供者子资源。 */
    @Path("/available-event-listeners")
    public AvailableEventListenersResource availableEventListeners() {
        return new AvailableEventListenersResource(session, auth);
    }

    /** 有效客户端角色映射子资源。 */
    @Path("/effective-roles")
    public EffectiveRoleMappingResource effectiveRoles() {
        return new EffectiveRoleMappingResource(session, realm, auth);
    }

    /** 全部有效角色（领域+客户端）映射子资源。 */
    @Path("/effective-roles-all")
    public AllEffectiveRoleMappingResource allEffectiveRoles() {
        return new AllEffectiveRoleMappingResource(session, realm, auth);
    }

    /** 复合角色映射查询子资源。 */
    @Path("/role-mappings")
    public RoleCompositeResource roleMappings() {
        return new RoleCompositeResource(session, realm, auth);
    }

    /** 批量删除角色映射子资源。 */
    @Path("/role-mapping-delete")
    public RoleMappingDeleteResource roleMappingDelete() {
        return new RoleMappingDeleteResource(session, realm, auth, adminEvent);
    }

    /** 用户会话列表子资源。 */
    @Path("/sessions")
    public SessionsResource sessions() {
        return new SessionsResource(session, realm, auth);
    }

    /** 领域列表 UI 子资源。 */
    @Path("/realms")
    public UIRealmsResource realms() {
        return new UIRealmsResource(session, auth);
    }

    /** 当前领域 UI 详情子资源。 */
    @Path("/")
    public UIRealmResource realm() {
        return new UIRealmResource(session, auth, adminEvent);
    }
}
