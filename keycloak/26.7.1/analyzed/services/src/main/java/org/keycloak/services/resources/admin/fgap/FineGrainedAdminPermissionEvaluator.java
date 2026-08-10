/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.ResourceWrapper;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.DecisionPermissionCollector;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelIllegalStateException;
import org.keycloak.representations.idm.authorization.Permission;

/**
 * FGAP v2 细粒度管理权限评估器。
 * <p>基于 {@link AdminPermissionsSchema} 资源类型与作用域，对模型实例或类型级资源执行策略决策。</p>
 */
class FineGrainedAdminPermissionEvaluator {
    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 根权限上下文 */
    private final MgmtPermissions root;
    /** 资源存储 */
    private final ResourceStore resourceStore;
    /** 策略存储 */
    private final PolicyStore policyStore;

    /** 构造 FGAP v2 评估器 */
    FineGrainedAdminPermissionEvaluator(KeycloakSession session, MgmtPermissions root, ResourceStore resourceStore, PolicyStore policyStore) {
        this.session = session;
        this.root = root;
        this.resourceStore = resourceStore;
        this.policyStore = policyStore;
    }

    boolean hasPermission(ModelRecord model, EvaluationContext context, String scope) {
        return hasPermission(model.getId(), model.getResourceType(), context, scope);
    }

    /**
     * 检查模型在指定 {@code scope} 下是否授权；未评估该作用域时返回 {@code defaultValue}。
     * @param model 模型记录
     * @param context 评估上下文
     * @param scope 作用域名称
     * @param defaultValue 未评估时的默认值
     * @return 是否授权
     */
    boolean hasPermission(ModelRecord model, EvaluationContext context, String scope, Supplier<Boolean> defaultValue) {
        return hasPermission(model.getId(), model.getResourceType(), context, scope, defaultValue);
    }

    boolean hasPermission(String modelId, String resourceType, EvaluationContext context, String scope) {
        return hasPermission(modelId, resourceType, context, scope, null);
    }

    /**
     * 按模型 ID 与资源类型检查 {@code scopeName} 权限。
     * @param modelId 模型 ID（null 表示类型级资源）
     * @param resourceType 资源类型
     * @param context 评估上下文
     * @param scopeName 作用域名称
     * @param defaultValue 未评估时的默认值
     * @return 是否授权
     */
    boolean hasPermission(String modelId, String resourceType, EvaluationContext context, String scopeName, Supplier<Boolean> defaultValue) {
        if (!root.isAdminSameRealm()) {
            return false;
        }
        if (!AdminPermissionsSchema.SCHEMA.isAdminPermissionsEnabled(root.realm)) {
            return false;
        }

        ResourceServer server = root.realmResourceServer();

        if (server == null) {
            return false;
        }

        Resource resourceTypeResource = AdminPermissionsSchema.SCHEMA.getResourceTypeResource(session, server, resourceType);
        Resource resource = modelId == null ? resourceTypeResource : resourceStore.findByName(server, modelId);

        Scope scope = resourceTypeResource.getScopes()
                .stream()
                .filter(s -> s.getName().equals(scopeName)).findAny()
                .orElseThrow(() -> new ModelIllegalStateException("Scope '%s' is not defined for resource type '%s'".formatted(scopeName, resourceType)));

        if (modelId != null && resource == null) {
            resource = new ResourceWrapper(modelId, modelId, Set.of(scope), server);
        }

        DecisionPermissionCollector decision = (context == null) ?
                root.getDecision(new ResourcePermission(resourceType, resource, resource.getScopes(), server), server) :
                root.getDecision(new ResourcePermission(resourceType, resource, resource.getScopes(), server), server, context);
        Collection<Permission> permissions = decision.results();

        for (Permission permission : permissions) {
            if (permission.getResourceId().equals(resource.getId())) {
                if (permission.getScopes().contains(scopeName)) {
                    return true;
                }
            }
        }

        if (defaultValue != null) {
            if (!decision.isEvaluated(scopeName)) {
                return defaultValue.get();
            }
        }

        return false;
    }

    /** 返回对 {@code resourceType} 上 {@code scope} 有权限的资源 ID 集合 */
    Set<String> getIdsByScope(String resourceType, String scope) {
        if (!root.isAdminSameRealm()) {
            return Collections.emptySet();
        }

        ResourceServer server = root.realmResourceServer();

        if (server == null) {
            return Collections.emptySet();
        }

        return policyStore.findByResourceType(server, resourceType).stream()
                .flatMap((Function<Policy, Stream<Resource>>) policy -> policy.getResources().stream())
                .filter(resource -> hasPermission(resource.getName(), resourceType, null, scope))
                .map(Resource::getName)
                .collect(Collectors.toSet());
    }
}
