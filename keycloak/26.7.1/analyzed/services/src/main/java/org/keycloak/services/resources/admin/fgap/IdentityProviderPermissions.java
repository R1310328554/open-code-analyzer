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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.common.ClientModelIdentity;
import org.keycloak.authorization.common.DefaultEvaluationContext;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.models.ClientModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import org.jboss.logging.Logger;

import static org.keycloak.services.resources.admin.fgap.AdminPermissionManagement.TOKEN_EXCHANGE;

/**
 * 身份提供者（IdP）细粒度管理权限实现。
 * <p>为每个 IdP 创建 token-exchange 授权资源与权限策略，控制客户端是否可向该 IdP 发起令牌交换。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
class IdentityProviderPermissions implements  IdentityProviderPermissionManagement {
    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(IdentityProviderPermissions.class);
    /** Keycloak 会话 */
    protected final KeycloakSession session;
    /** 当前领域 */
    protected final RealmModel realm;
    /** 授权 Provider */
    protected final AuthorizationProvider authz;
    /** 根权限管理器 */
    protected final MgmtPermissions root;

    /** 构造 IdP 权限管理器。 */
    public IdentityProviderPermissions(KeycloakSession session, RealmModel realm, AuthorizationProvider authz, MgmtPermissions root) {
        this.session = session;
        this.realm = realm;
        this.authz = authz;
        this.root = root;
    }

    /** 返回 IdP 对应的授权资源名称。 */
    private String getResourceName(IdentityProviderModel idp) {
        return "idp.resource." + idp.getInternalId();
    }

    /** 返回 token-exchange 权限策略名称。 */
    private String getExchangeToPermissionName(IdentityProviderModel idp) {
        return TOKEN_EXCHANGE + ".permission.idp." + idp.getInternalId();
    }

    /** 为 IdP 初始化授权资源与 token-exchange 权限。 */
    private void initialize(IdentityProviderModel idp) {
        ResourceServer server = root.initializeRealmResourceServer();
        if (server == null) return;
        Scope exchangeToScope = root.initializeScope(TOKEN_EXCHANGE, server);

        String resourceName = getResourceName(idp);
        Resource resource = authz.getStoreFactory().getResourceStore().findByName(server, resourceName);
        if (resource == null) {
            resource = authz.getStoreFactory().getResourceStore().create(server, resourceName, server.getClientId());
            resource.setType("IdentityProvider");
            Set<Scope> scopeset = new HashSet<>();
            scopeset.add(exchangeToScope);
            resource.updateScopes(scopeset);
        }
        String exchangeToPermissionName = getExchangeToPermissionName(idp);
        Policy exchangeToPermission = authz.getStoreFactory().getPolicyStore().findByName(server, exchangeToPermissionName);
        if (exchangeToPermission == null) {
            Helper.addEmptyScopePermission(authz, server, exchangeToPermissionName, resource, exchangeToScope);
        }
    }

    /** 按名称删除授权策略。 */
    private void deletePolicy(String name, ResourceServer server) {
        Policy policy = authz.getStoreFactory().getPolicyStore().findByName(server, name);
        if (policy != null) {
            authz.getStoreFactory().getPolicyStore().delete(policy.getId());
        }

    }

    /** 删除 IdP 的全部细粒度权限资源与策略。 */
    private void deletePermissions(IdentityProviderModel idp) {
        ResourceServer server = root.initializeRealmResourceServer();
        if (server == null) return;
        deletePolicy(getExchangeToPermissionName(idp), server);
        Resource resource = authz.getStoreFactory().getResourceStore().findByName(server, getResourceName(idp));;
        if (resource != null) authz.getStoreFactory().getResourceStore().delete(resource.getId());
    }

    @Override
    public boolean isPermissionsEnabled(IdentityProviderModel idp) {
        ResourceServer server = root.initializeRealmResourceServer();
        if (server == null) return false;

        return authz.getStoreFactory().getResourceStore().findByName(server, getResourceName(idp)) != null;
    }

    @Override
    public void setPermissionsEnabled(IdentityProviderModel idp, boolean enable) {
        if (enable) {
            initialize(idp);
        } else {
            deletePermissions(idp);
        }
    }



    /** 查找 token-exchange scope。 */
    private Scope exchangeToScope(ResourceServer server) {
        return authz.getStoreFactory().getScopeStore().findByName(server, TOKEN_EXCHANGE);
    }

    @Override
    public Resource resource(IdentityProviderModel idp) {
        ResourceServer server = root.initializeRealmResourceServer();
        if (server == null) return null;
        Resource resource =  authz.getStoreFactory().getResourceStore().findByName(server, getResourceName(idp));
        if (resource == null) return null;
        return resource;
    }


    @Override
    public Map<String, String> getPermissions(IdentityProviderModel idp) {
        if (authz==null) return null;
        initialize(idp);
        Map<String, String> scopes = new LinkedHashMap<>();
        scopes.put(TOKEN_EXCHANGE, exchangeToPermission(idp).getId());
        return scopes;
    }

    @Override
    public boolean canExchangeTo(ClientModel authorizedClient, IdentityProviderModel to) {
        ResourceServer server = root.initializeRealmResourceServer();
        if (server == null) {
            logger.debug("No resource server set up for target idp");
            return false;
        }

        Resource resource =  authz.getStoreFactory().getResourceStore().findByName(server, getResourceName(to));
        if (resource == null) {
            logger.debug("No resource object set up for target idp");
            return false;
        }

        Policy policy = authz.getStoreFactory().getPolicyStore().findByName(server, getExchangeToPermissionName(to));
        if (policy == null) {
            logger.debug("No permission object set up for target idp");
            return false;
        }

        Set<Policy> associatedPolicies = policy.getAssociatedPolicies();
        // 权限未关联任何策略时采用默认拒绝行为
        if (associatedPolicies == null || associatedPolicies.isEmpty()) {
            logger.debug("No policies set up for permission on target idp");
            return false;
        }

        Scope scope = exchangeToScope(server);
        if (scope == null) {
            logger.debug(TOKEN_EXCHANGE + " not initialized");
            return false;
        }
        ClientModelIdentity identity = new ClientModelIdentity(session, authorizedClient);
        EvaluationContext context = new DefaultEvaluationContext(identity, session) {
            @Override
            public Map<String, Collection<String>> getBaseAttributes() {
                Map<String, Collection<String>> attributes = super.getBaseAttributes();
                attributes.put("kc.client.id", Arrays.asList(authorizedClient.getClientId()));
                return attributes;
            }

        };
        return root.evaluatePermission(resource, server, context, scope);
    }

    @Override
    public Policy exchangeToPermission(IdentityProviderModel idp) {
        ResourceServer server = root.initializeRealmResourceServer();
        if (server == null) return null;
        return authz.getStoreFactory().getPolicyStore().findByName(server, getExchangeToPermissionName(idp));
    }

}
