/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2025 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization.fgap.evaluation;

import java.util.Map;
import java.util.function.Consumer;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.Decision;
import org.keycloak.authorization.Decision.Effect;
import org.keycloak.authorization.fgap.evaluation.partial.ResourceTypePolicyEvaluator;
import org.keycloak.authorization.fgap.evaluation.partial.UserResourceTypePolicyEvaluator;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.evaluation.DefaultPolicyEvaluator;
import org.keycloak.authorization.policy.evaluation.EvaluationContext;
import org.keycloak.authorization.policy.evaluation.PolicyEvaluator;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.authorization.store.StoreFactory;

import static org.keycloak.authorization.fgap.AdminPermissionsSchema.USERS_RESOURCE_TYPE;

/**
 * FGAP v2 专用 {@link PolicyEvaluator}：评估管理端细粒度权限，支持按资源类型扩展评估逻辑。
 * <p>通过 {@link FGAPDecision} 包装决策回调，并委托 {@link ResourceTypePolicyEvaluator} 处理 Users 等类型。</p>
 *
 * A {@link PolicyEvaluator} specific for evaluating permisions in the context of the {@link org.keycloak.common.Profile.Feature#ADMIN_FINE_GRAINED_AUTHZ_V2} feature.
 */
public final class FGAPPolicyEvaluator extends DefaultPolicyEvaluator {

    private final Map<String, ? extends ResourceTypePolicyEvaluator> resourceTypePolicyEvaluators = Map.of(USERS_RESOURCE_TYPE, new UserResourceTypePolicyEvaluator());

    /** 使用 {@link FGAPDecision} 包装后委托父类评估。 */
    @Override
    public void evaluate(ResourcePermission permission, AuthorizationProvider authorizationProvider, EvaluationContext executionContext, Decision decision, Map<Policy, Map<Object, Effect>> decisionCache) {
        super.evaluate(permission, authorizationProvider, executionContext, new FGAPDecision(decision), decisionCache);
    }

    /** 评估实例资源策略，并按资源类型调用扩展评估器。 */
    @Override
    protected void evaluateResourcePolicies(ResourcePermission permission, AuthorizationProvider authorization, Consumer<Policy> policyConsumer) {
        super.evaluateResourcePolicies(permission, authorization, policyConsumer);

        String resourceType = permission.getResourceType();

        if (resourceType == null) {
            return;
        }

        ResourceTypePolicyEvaluator resourceTypePolicyEvaluator = resourceTypePolicyEvaluators.get(resourceType);

        if (resourceTypePolicyEvaluator == null) {
            return;
        }

        resourceTypePolicyEvaluator.evaluate(permission, authorization, policyConsumer);
    }

    /** 对具体实例资源额外评估绑定到资源类型的策略。 */
    @Override
    protected void evaluateResourceTypePolicies(ResourcePermission permission, AuthorizationProvider authorization, Consumer<Policy> policyConsumer) {
        String resourceType = permission.getResourceType();
        Resource resource = permission.getResource();

        if (resourceType == null || resource.getName().equals(permission.getResourceType())) {
            return;
        }

        StoreFactory storeFactory = authorization.getStoreFactory();
        ResourceStore resourceStore = storeFactory.getResourceStore();
        PolicyStore policyStore = storeFactory.getPolicyStore();
        ResourceServer resourceServer = permission.getResourceServer();

        Resource resourceTypeResource = resourceStore.findByName(resourceServer, resourceType);
        policyStore.findByResource(resourceServer, resourceTypeResource, policyConsumer);
    }

    /** FGAP 场景下不按单个作用域单独评估权限。 */
    @Override
    protected void evaluateScopePolicies(ResourcePermission permission, AuthorizationProvider authorization, Consumer<Policy> policyConsumer) {
        // do not evaluate permissions for individual scopes
    }
}
