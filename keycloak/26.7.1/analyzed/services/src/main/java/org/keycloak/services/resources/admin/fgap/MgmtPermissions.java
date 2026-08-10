/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.ForbiddenException;

import org.keycloak.Config;
import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.AuthorizationProviderFactory;
import org.keycloak.authorization.common.DefaultEvaluationContext;
import org.keycloak.authorization.common.KeycloakIdentity;
import org.keycloak.authorization.identity.Identity;
import org.keycloak.authorization.identity.UserModelIdentity;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.DecisionPermissionCollector;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.common.Profile;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CacheRealmProvider;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.authorization.Permission;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;

/**
 * 细粒度管理权限（FGAP）核心实现。
 * <p>聚合 users/groups/clients/roles/realm/idp/org 等子评估器，管理 realm-management 授权 ResourceServer，并提供权限求值与 admin 角色检查。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
class MgmtPermissions implements AdminPermissionEvaluator, AdminPermissionManagement, RealmsPermissionEvaluator {
    /** 被管理的领域 */
    protected RealmModel realm;
    /** Keycloak 会话 */
    protected KeycloakSession session;
    /** 授权服务 Provider（FGAP 特性启用时） */
    protected AuthorizationProvider authz;
    /** 管理员 REST 认证上下文 */
    protected AdminAuth auth;
    /** 授权求值用的身份标识 */
    protected Identity identity;
    /** 当前管理员用户 */
    protected UserModel admin;
    /** 管理员所属领域 */
    protected RealmModel adminsRealm;
    /** 领域级授权 ResourceServer 缓存 */
    protected ResourceServer realmResourceServer;
    /** 用户权限子评估器（懒加载） */
    protected UserPermissions users;
    /** 组权限子评估器（懒加载） */
    protected GroupPermissions groups;
    /** 领域权限子评估器（懒加载） */
    protected RealmPermissions realmPermissions;
    /** 客户端权限子评估器（懒加载） */
    protected ClientPermissions clientPermissions;
    /** IdP 权限子评估器（懒加载） */
    protected IdentityProviderPermissions idpPermissions;
    /** 角色权限子评估器（懒加载） */
    protected RolePermissions rolePermissions;
    /** 组织权限子评估器（懒加载） */
    protected OrganizationPermissions orgPermissions;


    /** 构造权限管理器（无认证，仅初始化 authz）。 */
    MgmtPermissions(KeycloakSession session, RealmModel realm) {
        this.session = session;
        this.realm = realm;
        KeycloakSessionFactory keycloakSessionFactory = session.getKeycloakSessionFactory();
        if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ) || Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ_V2)) {
            AuthorizationProviderFactory factory = (AuthorizationProviderFactory) keycloakSessionFactory.getProviderFactory(AuthorizationProvider.class);
            this.authz = factory.create(session, realm);
        }
    }

    /** 构造权限管理器并绑定 AdminAuth（跨领域访问需 master 或同领域）。 */
    MgmtPermissions(KeycloakSession session, RealmModel realm, AdminAuth auth) {
        this(session, realm);
        this.auth = auth;
        this.admin = auth.getUser();
        this.adminsRealm = auth.getRealm();
        if (!auth.getRealm().equals(realm)
                && !RealmManager.isAdministrationRealm(auth.getRealm())) {
            throw new ForbiddenException();
        }
        initIdentity(session, auth);
    }
    /** 构造权限管理器（领域由后续操作确定）。 */
    MgmtPermissions(KeycloakSession session, AdminAuth auth) {
        this.session = session;
        this.auth = auth;
        this.admin = auth.getUser();
        this.adminsRealm = auth.getRealm();
        initIdentity(session, auth);
    }

    /** 从 AccessToken 解析轻量令牌角色并构建 {@link KeycloakIdentity}。 */
    private void initIdentity(KeycloakSession session, AdminAuth auth) {
        AccessToken accessToken = auth.getToken();
        AuthenticationManager.resolveLightweightAccessTokenRoles(session, accessToken, adminsRealm);
        this.identity = new KeycloakIdentity(accessToken, session, adminsRealm);
    }

    /** 构造权限管理器（指定管理员，无目标领域）。 */
    MgmtPermissions(KeycloakSession session, RealmModel adminsRealm, UserModel admin) {
        this.session = session;
        this.admin = admin;
        this.adminsRealm = adminsRealm;
        this.identity = new UserModelIdentity(adminsRealm, admin);
    }

    /** 构造权限管理器（指定目标领域与管理员用户）。 */
    MgmtPermissions(KeycloakSession session, RealmModel realm, RealmModel adminsRealm, UserModel admin) {
        this(session, realm);
        this.admin = admin;
        this.adminsRealm = adminsRealm;
        this.identity = new UserModelIdentity(realm, admin);
    }

    @Override
    /** 返回存储 FGAP 策略的 realm-management 或 master-realm 客户端。 */
    @Override
    public ClientModel getRealmPermissionsClient() {
        if (realm.getName().equals(Config.getAdminRealm())) {
            return realm.getClientByClientId(Config.getAdminRealm() + "-realm");
        } else {
            return realm.getClientByClientId(Constants.REALM_MANAGEMENT_CLIENT_ID);
        }
    }

    @Override
    /** 返回授权 Provider。 */
    @Override
    public AuthorizationProvider authz() {
        return authz;
    }

    @Override
    /** 要求至少拥有一个 realm admin 角色。 */
    @Override
    public void requireAnyAdminRole() {
        if (!hasAnyAdminRole()) {
            throw new ForbiddenException();
        }
    }

    @Override
    /** 要求为服务器或领域管理员。 */
    @Override
    public void requireRealmAdmin() {
        if (isRealmAdmin()) {
            return;
        }
        throw new ForbiddenException();
    }

    /** 当前领域是否拥有任一 admin 角色。 */
    public boolean hasAnyAdminRole() {
        return hasOneAdminRole(AdminRoles.ALL_REALM_ROLES);
    }

    /** 指定领域是否拥有任一 admin 角色。 */
    public boolean hasAnyAdminRole(RealmModel realm) {
        return hasOneAdminRole(realm, AdminRoles.ALL_REALM_ROLES);
    }

    @Override
    /** 当前领域是否拥有给定 admin 角色之一。 */
    @Override
    public boolean hasOneAdminRole(String... adminRoles) {
        return hasOneAdminRole(realm, adminRoles);
    }

    /** 检查管理员在指定领域 realm-management 客户端上是否拥有给定角色。 */
    public boolean hasOneAdminRole(RealmModel realm, String... adminRoles) {
        String clientId;
        RealmManager realmManager = new RealmManager(session);
        boolean masterAdminRealm = false;
        if (RealmManager.isAdministrationRealm(adminsRealm)) {
            clientId = realm.getMasterAdminClient().getClientId();
            masterAdminRealm = true;
        } else if (adminsRealm.equals(realm)) {
            clientId = realm.getClientByClientId(realmManager.getRealmAdminClientId(realm)).getClientId();
        } else {
            return false;
        }
        boolean result = identity.hasOneClientRole(clientId, adminRoles);
        if (!result && masterAdminRealm && !adminsRealm.equals(realm)
                && AbstractOIDCProtocolMapper.getShouldUseLightweightToken(session)
                && hasNewAdminRoles(realm, clientId, adminRoles)) {
            return true;
        }
        return result;
    }

    /** 轻量令牌场景下检查 master admin 复合角色是否包含目标客户端角色。 */
    private boolean hasNewAdminRoles(RealmModel realm, String clientId, String... adminRoles) {
        RealmModel masterRealm = getMasterRealm();
        UserModel admin = admin();
        RoleModel masterAdminRole = masterRealm.getRole(AdminRoles.ADMIN);
        if (!admin.hasRole(masterAdminRole)) {
            return false;
        }
        CacheRealmProvider cache = session.getProvider(CacheRealmProvider.class);
        if (cache == null || !cache.refreshMasterAdminRole(masterAdminRole, clientId)) {
            return false;
        }
        Set<String> roleNames = Set.of(adminRoles);
        return masterAdminRole.getCompositesStream().anyMatch(r -> (r.isClientRole()
                && r.getContainerId().equals(clientId) && roleNames.contains(r.getName())));
    }

    /** 管理员与被管理领域是否同一领域（或无 auth 上下文）。 */
    public boolean isAdminSameRealm() {
        return auth == null || realm.getId().equals(auth.getRealm().getId());
    }

    @Override
    /** 返回 AdminAuth。 */
    @Override
    public AdminAuth adminAuth() {
        return auth;
    }

    /** 返回授权求值身份。 */
    public Identity identity() {
        return identity;
    }

    /** 返回管理员用户模型。 */
    public UserModel admin() {
        return admin;
    }

    /** 返回管理员所属领域。 */
    public RealmModel adminsRealm() {
        return adminsRealm;
    }


    @Override
    /** 懒加载并返回角色权限评估器。 */
    @Override
    public RolePermissions roles() {
        if (rolePermissions!=null) return rolePermissions;
        rolePermissions = new RolePermissions(session, realm, authz, this);
        return rolePermissions;
    }

    @Override
    /** 懒加载并返回用户权限评估器。 */
    @Override
    public UserPermissions users() {
        if (users != null) return users;
        users = new UserPermissions(session, authz, this);
        return users;
    }

    @Override
    /** 懒加载并返回领域权限评估器。 */
    @Override
    public RealmPermissions realm() {
        if (realmPermissions != null) return realmPermissions;
        realmPermissions = new RealmPermissions(this);
        return realmPermissions;
    }

    @Override
    /** 懒加载并返回客户端权限评估器。 */
    @Override
    public ClientPermissions clients() {
        if (clientPermissions != null) return clientPermissions;
        clientPermissions = new ClientPermissions(session, realm, authz, this);
        return clientPermissions;
    }

    @Override
    /** 懒加载并返回 IdP 权限评估器。 */
    @Override
    public IdentityProviderPermissions idps() {
        if (idpPermissions != null) return idpPermissions;
        idpPermissions = new IdentityProviderPermissions(session, realm, authz, this);
        return idpPermissions;
    }

    @Override
    /** 懒加载并返回组织权限评估器。 */
    @Override
    public OrganizationPermissions orgs() {
        if (orgPermissions != null) return orgPermissions;
        orgPermissions = new OrganizationPermissions(session, authz, this);
        return orgPermissions;
    }

    @Override
    /** 懒加载并返回组权限评估器。 */
    @Override
    public GroupPermissions groups() {
        if (groups != null) return groups;
        groups = new GroupPermissions(authz, this);
        return groups;
    }

    /** 查找或创建领域 ResourceServer（V1 兼容入口）。 */
    public ResourceServer findOrCreateResourceServer(ClientModel client) {
         return initializeRealmResourceServer();
    }

    /** 返回领域 ResourceServer。 */
    public ResourceServer resourceServer(ClientModel client) {
        return realmResourceServer();
    }

    @Override
    /** 返回已缓存的领域 ResourceServer（不创建）。 */
    @Override
    public ResourceServer realmResourceServer() {
        if (authz == null) return null;
        if (realmResourceServer != null) return realmResourceServer;
        ClientModel client = getRealmPermissionsClient();
        if (client == null) return null;
        realmResourceServer = authz.getStoreFactory().getResourceServerStore().findByClient(client);
        return realmResourceServer;

    }

    /** 查找或创建领域 ResourceServer。 */
    public ResourceServer initializeRealmResourceServer() {
        if (authz == null) return null;
        if (realmResourceServer != null) return realmResourceServer;
        ClientModel client = getRealmPermissionsClient();
        if (client == null) return null;
        realmResourceServer = authz.getStoreFactory().getResourceServerStore().findByClient(client);
        if (realmResourceServer == null) {
            realmResourceServer = authz.getStoreFactory().getResourceServerStore().create(client);
        }
        return realmResourceServer;
    }

    /** 缓存的 manage scope */
    protected Scope manageScope;
    /** 缓存的 view scope */
    protected Scope viewScope;

    /** 初始化领域默认 manage/view scope。 */
    public void initializeRealmDefaultScopes() {
        ResourceServer server = initializeRealmResourceServer();
        if (server == null) return;
        manageScope = initializeRealmScope(MgmtPermissions.MANAGE_SCOPE);
        viewScope = initializeRealmScope(MgmtPermissions.VIEW_SCOPE);
    }

    /** 在领域 ResourceServer 上查找或创建指定 scope。 */
    public Scope initializeRealmScope(String name) {
        ResourceServer server = initializeRealmResourceServer();
        if (server == null) return null;
        Scope scope  = authz.getStoreFactory().getScopeStore().findByName(server, name);
        if (scope == null) {
            scope = authz.getStoreFactory().getScopeStore().create(server, name);
        }
        return scope;
    }

    /** 在指定 ResourceServer 上查找或创建 scope。 */
    public Scope initializeScope(String name, ResourceServer server) {
        if (authz == null) return null;
        Scope scope  = authz.getStoreFactory().getScopeStore().findByName(server, name);
        if (scope == null) {
            scope = authz.getStoreFactory().getScopeStore().create(server, name);
        }
        return scope;
    }



    /** 返回领域 manage scope（懒加载）。 */
    public Scope realmManageScope() {
        if (manageScope != null) return manageScope;
        manageScope = realmScope(MgmtPermissions.MANAGE_SCOPE);
        return manageScope;
    }


    /** 返回领域 view scope（懒加载）。 */
    public Scope realmViewScope() {
        if (viewScope != null) return viewScope;
        viewScope = realmScope(MgmtPermissions.VIEW_SCOPE);
        return viewScope;
    }

    /** 按名称查找领域 scope。 */
    public Scope realmScope(String scope) {
        ResourceServer server = realmResourceServer();
        if (server == null) return null;
        return authz.getStoreFactory().getScopeStore().findByName(server, scope);
    }

    /** 使用当前 identity 评估资源权限。 */
    public boolean evaluatePermission(Resource resource, ResourceServer resourceServer, Scope... scope) {
        Identity identity = identity();
        if (identity == null) {
            throw new RuntimeException("Identity of admin is not set for permission query");
        }
        return evaluatePermission(resource, resourceServer, identity, scope);
    }

    /** 评估单个 ResourcePermission 并返回 granted 权限集合。 */
    public Collection<Permission> evaluatePermission(ResourcePermission permission, ResourceServer resourceServer) {
        return evaluatePermission(permission, resourceServer, new DefaultEvaluationContext(identity, session));
    }

    /** 获取权限决策收集器（默认 EvaluationContext）。 */
    public DecisionPermissionCollector getDecision(ResourcePermission permission, ResourceServer resourceServer) {
        return evaluatePermission(List.of(permission), resourceServer, new DefaultEvaluationContext(identity, session));
    }

    /** 使用指定上下文评估权限。 */
    public Collection<Permission> evaluatePermission(ResourcePermission permission, ResourceServer resourceServer, EvaluationContext context) {
        return evaluatePermission(Arrays.asList(permission), resourceServer, context).results();
    }

    /** 使用指定上下文获取决策收集器。 */
    public DecisionPermissionCollector getDecision(ResourcePermission permission, ResourceServer resourceServer, EvaluationContext context) {
        return evaluatePermission(Arrays.asList(permission), resourceServer, context);
    }

    /** 使用指定 Identity 评估资源权限。 */
    public boolean evaluatePermission(Resource resource, ResourceServer resourceServer, Identity identity, Scope... scope) {
        EvaluationContext context = new DefaultEvaluationContext(identity, session);
        return evaluatePermission(resource, resourceServer, context, scope);
    }

    /** 使用 EvaluationContext 评估资源是否至少有一个 scope 被授权。 */
    public boolean evaluatePermission(Resource resource, ResourceServer resourceServer, EvaluationContext context, Scope... scope) {
        return !evaluatePermission(Arrays.asList(new ResourcePermission(resource, Arrays.asList(scope), resourceServer)), resourceServer, context).results().isEmpty();
    }

    /** 批量评估权限并在目标领域上下文中执行。 */
    public DecisionPermissionCollector evaluatePermission(List<ResourcePermission> permissions, ResourceServer resourceServer, EvaluationContext context) {
        RealmModel oldRealm = session.getContext().getRealm();
        try {
            session.getContext().setRealm(realm);
            return authz.evaluators().from(permissions, resourceServer, context).getDecision(resourceServer, null, DecisionPermissionCollector.class);
        } finally {
            session.getContext().setRealm(oldRealm);
        }
    }

    @Override
    /** 是否可查看指定领域。 */
    @Override
    public boolean canView(RealmModel realm) {
        return hasOneAdminRole(realm, AdminRoles.VIEW_REALM, AdminRoles.MANAGE_REALM);
    }

    @Override
    /** 是否为指定领域的管理员。 */
    @Override
    public boolean isAdmin(RealmModel realm) {
        return hasAnyAdminRole(realm);
    }

    @Override
    /** 当前用户是否为管理员（master 或所在领域）。 */
    @Override
    public boolean isAdmin() {
        if (RealmManager.isAdministrationRealm(adminsRealm)) {
            if (identity.hasRealmRole(AdminRoles.ADMIN) || identity.hasRealmRole(AdminRoles.CREATE_REALM)) {
                return true;
            }
            return session.realms().getRealmsStream().anyMatch(this::isAdmin);
        } else {
            return isAdmin(adminsRealm);
        }
    }

    @Override
    /** 是否可创建新领域。 */
    @Override
    public boolean canCreateRealm() {
        if (!RealmManager.isAdministrationRealm(auth.getRealm())) {
           return false;
        }
        return identity.hasRealmRole(AdminRoles.CREATE_REALM);
    }

    @Override
    /** 要求 create-realm 权限。 */
    @Override
    public void requireCreateRealm() {
        if (!canCreateRealm()) {
            throw new ForbiddenException();
        }
    }

    @Override
    /** 是否为 master admin 或 realm-admin 角色持有者。 */
    @Override
    public boolean isRealmAdmin() {
        RealmModel masterRealm = getMasterRealm();
        UserModel admin = admin();
        RoleModel masterAdminRole = masterRealm.getRole(AdminRoles.ADMIN);

        if (admin.hasRole(masterAdminRole)) {
            // 服务器级 admin
            return true;
        }

        ClientModel realmManagementClient = getRealmManagementClient();

        if (realmManagementClient != null && !realmManagementClient.getRealm().equals(masterRealm)) {
            RoleModel realmAdminRole = realmManagementClient.getRole(AdminRoles.REALM_ADMIN);

            if (realmAdminRole != null && admin.hasRole(realmAdminRole)) {
                // 领域 admin
                return true;
            }
        }

        return false;
    }

    /** 返回 master 领域模型。 */
    RealmModel getMasterRealm() {
        return adminsRealm().getName().equals(Config.getAdminRealm()) ?
                adminsRealm():
                session.realms().getRealmByName(Config.getAdminRealm());
    }

    /** 返回当前领域的 realm-management 客户端。 */
    ClientModel getRealmManagementClient() {
        if (realm.getName().equals(Config.getAdminRealm())) {
            return realm.getClientByClientId(Config.getAdminRealm() + "-realm");
        } else {
            return realm.getClientByClientId(Constants.REALM_MANAGEMENT_CLIENT_ID);
        }
    }
}
