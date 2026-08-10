/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.services.resources.admin.fgap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.ForbiddenException;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Permission;

import org.jboss.logging.Logger;

/**
 * 角色细粒度管理权限 V1 实现。
 * <p>为每个角色创建授权资源与 map-role / map-role-client-scope / map-role-composite 权限，并在映射管理员角色时进行额外冲突检查。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
class RolePermissions implements RolePermissionEvaluator, RolePermissionManagement {
    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(RolePermissions.class);
    /** Keycloak 会话 */
    protected final KeycloakSession session;
    /** 当前领域 */
    protected final RealmModel realm;
    /** 授权 Provider */
    protected final AuthorizationProvider authz;
    /** 根权限管理器 */
    protected final MgmtPermissions root;
    /** 授权资源存储 */
    protected final ResourceStore resourceStore;
    /** 授权策略存储 */
    protected final PolicyStore policyStore;
    /** 角色授权资源名称前缀 */
    private static final String RESOURCE_NAME_PREFIX = "role.resource.";

    /** 构造角色权限管理器。 */
    public RolePermissions(KeycloakSession session, RealmModel realm, AuthorizationProvider authz, MgmtPermissions root) {
        this.session = session;
        this.realm = realm;
        this.authz = authz;
        this.root = root;
        if (authz != null) {
            resourceStore = authz.getStoreFactory().getResourceStore();
            policyStore = authz.getStoreFactory().getPolicyStore();
        } else {
            resourceStore = null;
            policyStore = null;
        }
    }

    @Override
    public boolean isPermissionsEnabled(RoleModel role) {
        return mapRolePermission(role) != null;
    }

    @Override
    public void setPermissionsEnabled(RoleModel role, boolean enable) {
       if (enable) {
           initialize(role);
       } else {
           disablePermissions(role);
       }
    }

    /** 删除角色的全部 FGAP 资源与策略。 */
    private void disablePermissions(RoleModel role) {
        ResourceServer server = resourceServer(role);
        if (server == null) return;

        Policy policy = mapRolePermission(role);
        if (policy != null) authz.getStoreFactory().getPolicyStore().delete(policy.getId());
        policy = mapClientScopePermission(role);
        if (policy != null) authz.getStoreFactory().getPolicyStore().delete(policy.getId());
        policy = mapCompositePermission(role);
        if (policy != null) authz.getStoreFactory().getPolicyStore().delete(policy.getId());

        Resource resource = authz.getStoreFactory().getResourceStore().findByName(server, getRoleResourceName(role));
        if (resource != null) authz.getStoreFactory().getResourceStore().delete(resource.getId());
    }

    @Override
    public Map<String, String> getPermissions(RoleModel role) {
        if (authz == null) return null;
        initialize(role);
        Map<String, String> scopes = new LinkedHashMap<>();
        scopes.put(RolePermissionManagement.MAP_ROLE_SCOPE, mapRolePermission(role).getId());
        scopes.put(RolePermissionManagement.MAP_ROLE_CLIENT_SCOPE_SCOPE, mapClientScopePermission(role).getId());
        scopes.put(RolePermissionManagement.MAP_ROLE_COMPOSITE_SCOPE, mapCompositePermission(role).getId());
        return scopes;
    }

    @Override
    public Policy mapRolePermission(RoleModel role) {
        ResourceServer server = resourceServer(role);
        if (server == null) return null;
        return  authz.getStoreFactory().getPolicyStore().findByName(server, getMapRolePermissionName(role));
    }

    @Override
    public Policy mapCompositePermission(RoleModel role) {
        ResourceServer server = resourceServer(role);
        if (server == null) return null;

        return  authz.getStoreFactory().getPolicyStore().findByName(server, getMapCompositePermissionName(role));
    }

    @Override
    public Policy mapClientScopePermission(RoleModel role) {
        ResourceServer server = resourceServer(role);
        if (server == null) return null;

        return  authz.getStoreFactory().getPolicyStore().findByName(server, getMapClientScopePermissionName(role));
    }

    @Override
    public Resource resource(RoleModel role) {
        ResourceServer server = resourceServer(role);
        if (server == null) return null;
        ResourceStore resourceStore = authz.getStoreFactory().getResourceStore();
        return  resourceStore.findByName(server, getRoleResourceName(role));
    }

    @Override
    public ResourceServer resourceServer(RoleModel role) {
        ClientModel client = getRoleClient(role);
        return root.resourceServer(client);
    }

    /** 映射管理员角色前检查：调用者须拥有同等或更高特权，防止权限提升。 */
    protected boolean checkAdminRoles(RoleModel role) {
        if (AdminRoles.ALL_ROLES.contains(role.getName())) {
            if (root.admin().hasRole(role)) return true;

            ClientModel adminClient = root.getRealmManagementClient();
            // 是否为被管理领域 realm-management 客户端的管理员角色？
            if (adminClient.equals(role.getContainer())) {
                // 检查管理员是否拥有对应的领域/客户端管理权限
                // 以便授权服务参与求值
                if (role.getName().equals(AdminRoles.MANAGE_CLIENTS)
                        || role.getName().equals(AdminRoles.CREATE_CLIENT)
                        ) {
                    if (!root.clients().canManage()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.VIEW_CLIENTS)) {
                    if (!root.clients().canView()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }

                } else if (role.getName().equals(AdminRoles.QUERY_REALMS)) {
                    return true;
                } else if (role.getName().equals(AdminRoles.QUERY_CLIENTS)) {
                    return true;
                } else if (role.getName().equals(AdminRoles.QUERY_USERS)) {
                    return true;
                } else if (role.getName().equals(AdminRoles.QUERY_GROUPS)) {
                    return true;
                } else if (role.getName().equals(AdminRoles.QUERY_ORGANIZATIONS)) {
                    return true;
                } else if (role.getName().equals(AdminRoles.MANAGE_AUTHORIZATION)) {
                    ResourceServer resourceServer = getResourceServer(role);
                    if (!root.realm().canManageAuthorization(resourceServer)) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.VIEW_AUTHORIZATION)) {
                    ResourceServer resourceServer = getResourceServer(role);
                    if (!root.realm().canViewAuthorization(resourceServer)) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.MANAGE_EVENTS)) {
                    if (!root.realm().canManageEvents()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.VIEW_EVENTS)) {
                    if (!root.realm().canViewEvents()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.MANAGE_USERS)) {
                    if (!root.users().canManage()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.VIEW_USERS)) {
                    if (!root.users().canView()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.MANAGE_IDENTITY_PROVIDERS)) {
                    if (!root.realm().canManageIdentityProviders()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.VIEW_IDENTITY_PROVIDERS)) {
                    if (!root.realm().canViewIdentityProviders()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.MANAGE_REALM)) {
                    if (!root.realm().canManageRealm()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.VIEW_REALM)) {
                    if (!root.realm().canViewRealm()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.MANAGE_ORGANIZATIONS)) {
                    if (!root.orgs().canManage()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.VIEW_ORGANIZATIONS)) {
                    if (!root.orgs().canView()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.IMPERSONATION)) {
                    if (!root.users().canImpersonate()) {
                        return adminConflictMessage(role);
                    } else {
                        return true;
                    }
                } else if (role.getName().equals(AdminRoles.REALM_ADMIN)) {
                    // 须拥有 master admin 角色，否则拒绝
                    if (root.adminsRealm() == null || !root.adminsRealm().getName().equals(Config.getAdminRealm())) {
                        return adminConflictMessage(role);
                    }

                    RealmModel masterRealm = root.adminsRealm();
                    RoleModel adminRole = masterRealm.getRole(AdminRoles.ADMIN);
                    if (root.admin().hasRole(adminRole)) {
                        return true;
                    } else {
                        return adminConflictMessage(role);
                    }
                 } else {
                    return adminConflictMessage(role);
                }

            } else {
                // 检查是否为 master 领域的管理员角色
                if (role.getContainer() instanceof RealmModel) {
                    RealmModel realm = (RealmModel)role.getContainer();
                    // master 领域角色须拒绝非 master admin 映射
                    // if realm name is master realm, than we know this is a admin role in master realm.
                    if (realm.getName().equals(Config.getAdminRealm())) {
                        return adminConflictMessage(role);
                    }
                } else {
                    ClientModel container = (ClientModel)role.getContainer();
                    // master 领域中 *-realm 客户端的管理员角色须拒绝
                    if (container.getRealm().getName().equals(Config.getAdminRealm())
                            && container.getClientId().endsWith("-realm")) {
                        return adminConflictMessage(role);
                    }
                }
                return true;
            }

        }
        return true;

    }

    /** 记录权限冲突调试信息并返回 false。 */
    private boolean adminConflictMessage(RoleModel role) {
        logger.debugf("Trying to assign admin privileges of role: %s but admin doesn't have same privilege", role.getName());
        return false;
    }

        /** 管理员是否允许将指定角色映射给用户。
     *
     * @param role 待映射角色
     * @return 是否允许
     */
    @Override
    public boolean canMapRole(RoleModel role) {
        if (root.hasOneAdminRole(AdminRoles.MANAGE_USERS)) return checkAdminRoles(role);
        if (!root.isAdminSameRealm()) {
            return false;
        }

        if (role.getContainer() instanceof ClientModel) {
            if (root.clients().canMapRoles((ClientModel)role.getContainer())) return true;
        }
        if (!isPermissionsEnabled(role)){
            return false;
        }

        ResourceServer resourceServer = resourceServer(role);
        if (resourceServer == null) return false;

        Policy policy = authz.getStoreFactory().getPolicyStore().findByName(resourceServer, getMapRolePermissionName(role));
        if (policy == null || policy.getAssociatedPolicies().isEmpty()) {
            return false;
        }

        Resource roleResource = resource(role);
        Scope mapRoleScope = mapRoleScope(resourceServer);
        if (root.evaluatePermission(roleResource, resourceServer, mapRoleScope)) {
            return checkAdminRoles(role);
        } else {
            return false;
        }
    }

    @Override
    public void requireMapRole(RoleModel role) {
        if (!canMapRole(role)) {
            throw new ForbiddenException();
        }

    }

    @Override
    public boolean canList(RoleContainerModel container) {
        if (canView(container)) {
            return true;
        } else if (container instanceof RealmModel) {
            return root.realm().canViewRealm() || root.hasOneAdminRole(AdminRoles.MANAGE_IDENTITY_PROVIDERS);
        } else {
            return root.clients().canView((ClientModel)container);
        }
    }

    @Override
    public void requireList(RoleContainerModel container) {
        if (!canList(container)) {
            throw new ForbiddenException();
        }

    }

    @Override
    public boolean canManage(RoleContainerModel container) {
        if (container instanceof RealmModel) {
            return root.realm().canManageRealm();
        } else {
            return root.clients().canConfigure((ClientModel)container);
        }
    }

    @Override
    public void requireManage(RoleContainerModel container) {
        if (!canManage(container)) {
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean canView(RoleContainerModel container) {
        if (container instanceof RealmModel) {
            return root.realm().canViewRealm();
        } else {
            return root.clients().canView((ClientModel)container);
        }
    }

    @Override
    public void requireView(RoleContainerModel container) {
        if (!canView(container)) {
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean canMapComposite(RoleModel role) {
        if (canManageDefault(role)) return checkAdminRoles(role);

        if (!root.isAdminSameRealm()) {
            return false;
        }
        if (role.getContainer() instanceof ClientModel) {
            if (root.clients().canMapCompositeRoles((ClientModel)role.getContainer())) return true;
        }
        if (!isPermissionsEnabled(role)){
            return false;
        }

        ResourceServer resourceServer = resourceServer(role);
        if (resourceServer == null) return false;

        Policy policy = authz.getStoreFactory().getPolicyStore().findByName(resourceServer, getMapCompositePermissionName(role));
        if (policy == null || policy.getAssociatedPolicies().isEmpty()) {
            return false;
        }

        Resource roleResource = resource(role);
        Scope scope = mapCompositeScope(resourceServer);
        if (root.evaluatePermission(roleResource, resourceServer, scope)) {
            return checkAdminRoles(role);
        } else {
            return false;
        }
    }

    @Override
    public void requireMapComposite(RoleModel role) {
        if (!canMapComposite(role)) {
            throw new ForbiddenException();
        }

    }


    @Override
    public boolean canMapClientScope(RoleModel role) {
        if (root.clients().canManageClientsDefault()) return true;
        if (!root.isAdminSameRealm()) {
            return false;
        }
        if (role.getContainer() instanceof ClientModel) {
            if (root.clients().canMapClientScopeRoles((ClientModel)role.getContainer())) return true;
        }
        if (!isPermissionsEnabled(role)){
            return false;
        }

        ResourceServer resourceServer = resourceServer(role);
        if (resourceServer == null) return false;

        Policy policy = authz.getStoreFactory().getPolicyStore().findByName(resourceServer, getMapClientScopePermissionName(role));
        if (policy == null || policy.getAssociatedPolicies().isEmpty()) {
            return false;
        }

        Resource roleResource = resource(role);
        Scope scope = mapClientScope(resourceServer);
        return root.evaluatePermission(roleResource, resourceServer, scope);
    }

    @Override
    public void requireMapClientScope(RoleModel role) {
        if (!canMapClientScope(role)) {
            throw new ForbiddenException();
        }
    }


    @Override
    public boolean canManage(RoleModel role) {
        if (role.getContainer() instanceof RealmModel) {
            return root.realm().canManageRealm() && !isRealmAdminRole(role);
        } else if (role.getContainer() instanceof ClientModel) {
            ClientModel client = (ClientModel)role.getContainer();
            return root.clients().canConfigure(client);
        }
        return false;
    }

    /** 是否拥有领域/客户端默认管理权限（不含 realm admin 角色限制）。 */
    public boolean canManageDefault(RoleModel role) {
        if (role.getContainer() instanceof RealmModel) {
            return root.realm().canManageRealmDefault();
        } else if (role.getContainer() instanceof ClientModel) {
            return root.clients().canManageClientsDefault();
        }
        return false;
    }

    @Override
    public void requireManage(RoleModel role) {
        if (!canManage(role)) {
            throw new ForbiddenException();
        }

    }

    @Override
    public boolean canView(RoleModel role) {
        if (role.getContainer() instanceof RealmModel) {
            return root.realm().canViewRealm();
        } else if (role.getContainer() instanceof ClientModel) {
            ClientModel client = (ClientModel)role.getContainer();
            return root.clients().canView(client);
        }
        return false;
    }

    @Override
    public void requireView(RoleModel role) {
        if (!canView(role)) {
            throw new ForbiddenException();
        }

    }

    /** 返回角色所属客户端，领域角色则返回 realm-management 客户端。 */
    private ClientModel getRoleClient(RoleModel role) {
        ClientModel client = null;
        if (role.getContainer() instanceof ClientModel) {
            client = (ClientModel)role.getContainer();
        } else {
            client = root.getRealmPermissionsClient();
        }
        return client;
    }

    @Override
    public Policy manageUsersPolicy(ResourceServer server) {
        RoleModel role = root.getRealmPermissionsClient().getRole(AdminRoles.MANAGE_USERS);
        return rolePolicy(server, role);
    }

    @Override
    public Policy viewUsersPolicy(ResourceServer server) {
        RoleModel role = root.getRealmPermissionsClient().getRole(AdminRoles.VIEW_USERS);
        return rolePolicy(server, role);
    }

    @Override
    public Policy rolePolicy(ResourceServer server, RoleModel role) {
        String policyName = Helper.getRolePolicyName(role);
        Policy policy = authz.getStoreFactory().getPolicyStore().findByName(server, policyName);
        if (policy != null) return policy;
        return Helper.createRolePolicy(authz, server, role, policyName);
    }

    @Override
    public Set<String> getRoleIdsByScope(String scope) {
        if (!root.isAdminSameRealm()) {
            return Collections.emptySet();
        }

        ResourceServer server = root.realmResourceServer();

        if (server == null) {
            return Collections.emptySet();
        }

        Set<String> granted = new HashSet<>();

        resourceStore.findByType(server, "Role", resource -> {
            if (hasPermission(resource, scope)) {
                granted.add(resource.getName().substring(RESOURCE_NAME_PREFIX.length()));
            }
        });

        return granted;
    }

    /** 检查当前用户对资源是否拥有指定 scope。 */
    private boolean hasPermission(Resource resource, String scope) {
        ResourceServer server = root.realmResourceServer();
        Collection<Permission> permissions = root.evaluatePermission(new ResourcePermission(resource, resource.getScopes(), server), server);
        for (Permission permission : permissions) {
            for (String s : permission.getScopes()) {
                if (scope.equals(s)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** 查找 map-role scope。 */
    private Scope mapRoleScope(ResourceServer server) {
        return authz.getStoreFactory().getScopeStore().findByName(server, MAP_ROLE_SCOPE);
    }

    /** 查找 map-role-client-scope scope。 */
    private Scope mapClientScope(ResourceServer server) {
        return authz.getStoreFactory().getScopeStore().findByName(server, MAP_ROLE_CLIENT_SCOPE_SCOPE);
    }

    /** 查找 map-role-composite scope。 */
    private Scope mapCompositeScope(ResourceServer server) {
        return authz.getStoreFactory().getScopeStore().findByName(server, MAP_ROLE_COMPOSITE_SCOPE);
    }


    /** 为角色创建授权资源与三个 map-* 权限策略。 */
    private void initialize(RoleModel role) {
        ResourceServer server = resourceServer(role);
        if (server == null) {
            ClientModel client = getRoleClient(role);
            server = root.findOrCreateResourceServer(client);
            if (server == null ) return;
        }
        Scope mapRoleScope = mapRoleScope(server);
        if (mapRoleScope == null) {
            mapRoleScope = authz.getStoreFactory().getScopeStore().create(server, MAP_ROLE_SCOPE);
        }
        Scope mapClientScope = mapClientScope(server);
        if (mapClientScope == null) {
            mapClientScope = authz.getStoreFactory().getScopeStore().create(server, MAP_ROLE_CLIENT_SCOPE_SCOPE);
        }
        Scope mapCompositeScope = mapCompositeScope(server);
        if (mapCompositeScope == null) {
            mapCompositeScope = authz.getStoreFactory().getScopeStore().create(server, MAP_ROLE_COMPOSITE_SCOPE);
        }

        String roleResourceName = getRoleResourceName(role);
        Resource resource = authz.getStoreFactory().getResourceStore().findByName(server, roleResourceName);
        if (resource == null) {
            resource = authz.getStoreFactory().getResourceStore().create(server, roleResourceName, server.getClientId());
            Set<Scope> scopeset = new HashSet<>();
            scopeset.add(mapClientScope);
            scopeset.add(mapCompositeScope);
            scopeset.add(mapRoleScope);
            resource.updateScopes(scopeset);
            resource.setType("Role");
        }
        Policy mapRolePermission = mapRolePermission(role);
        if (mapRolePermission == null) {
            mapRolePermission = Helper.addEmptyScopePermission(authz, server, getMapRolePermissionName(role), resource, mapRoleScope);
            mapRolePermission.setDecisionStrategy(DecisionStrategy.AFFIRMATIVE);
        }

        Policy mapClientScopePermission = mapClientScopePermission(role);
        if (mapClientScopePermission == null) {
            mapClientScopePermission = Helper.addEmptyScopePermission(authz, server, getMapClientScopePermissionName(role), resource, mapClientScope);
            mapClientScopePermission.setDecisionStrategy(DecisionStrategy.AFFIRMATIVE);
        }

        Policy mapCompositePermission = mapCompositePermission(role);
        if (mapCompositePermission == null) {
            mapCompositePermission = Helper.addEmptyScopePermission(authz, server, getMapCompositePermissionName(role), resource, mapCompositeScope);
            mapCompositePermission.setDecisionStrategy(DecisionStrategy.AFFIRMATIVE);
        }
    }

    /** 返回 map-role 权限策略名称。 */
    private String getMapRolePermissionName(RoleModel role) {
        return MAP_ROLE_SCOPE + ".permission." + role.getId();
    }

    /** 返回 map-role-client-scope 权限策略名称。 */
    private String getMapClientScopePermissionName(RoleModel role) {
        return MAP_ROLE_CLIENT_SCOPE_SCOPE + ".permission." + role.getId();
    }

    /** 返回 map-role-composite 权限策略名称。 */
    private String getMapCompositePermissionName(RoleModel role) {
        return MAP_ROLE_COMPOSITE_SCOPE + ".permission." + role.getId();
    }

    /** 返回角色授权资源名称。 */
    private String getRoleResourceName(RoleModel role) {
        return "role.resource." + role.getId();
    }

    /** 客户端角色返回其客户端 ResourceServer。 */
    private ResourceServer getResourceServer(RoleModel role) {
        ResourceServer resourceServer = null;
        if (role.isClientRole()) {
            RoleContainerModel container = role.getContainer();
            resourceServer = session.getProvider(AuthorizationProvider.class).getStoreFactory().getResourceServerStore().findById(container.getId());
        }
        return resourceServer;
    }
    /** 是否为领域级 admin/create-realm 角色。 */
    private boolean isRealmAdminRole(RoleModel role) {
        return role.getContainer() instanceof RealmModel && List.of(AdminRoles.ADMIN, AdminRoles.CREATE_REALM).contains(role.getName());
    }
}
