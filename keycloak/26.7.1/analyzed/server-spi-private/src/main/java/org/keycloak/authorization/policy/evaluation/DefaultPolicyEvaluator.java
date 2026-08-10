/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

package org.keycloak.authorization.policy.evaluation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.Decision;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.permission.ResourcePermission;
import org.keycloak.authorization.policy.provider.PolicyProvider;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.representations.idm.authorization.PolicyEnforcementMode;

/**
 * 默认 {@link PolicyEvaluator}：按资源、资源类型与作用域查找关联策略并委托 {@link PolicyProvider} 评估。
 * <p>支持 DISABLED/PERMISSIVE 强制模式及已标记 granted 的权限短路。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class DefaultPolicyEvaluator implements PolicyEvaluator {

    /** 主评估入口：查找并执行关联策略，或在宽松/禁用模式下直接授予。 */
    @Override
    public void evaluate(ResourcePermission permission, AuthorizationProvider authorizationProvider, EvaluationContext executionContext, Decision decision, Map<Policy, Map<Object, Decision.Effect>> decisionCache) {
        ResourceServer resourceServer = permission.getResourceServer();
        PolicyEnforcementMode enforcementMode = resourceServer.getPolicyEnforcementMode();

        if (PolicyEnforcementMode.DISABLED.equals(enforcementMode)) {
            grantAndComplete(permission, authorizationProvider, executionContext, decision);
            return;
        }
        
        // if marked as granted we just complete the evaluation
        if (permission.isGranted()) {
            grantAndComplete(permission, authorizationProvider, executionContext, decision);
            return;
        }

        AtomicBoolean verified = new AtomicBoolean();
        Consumer<Policy> policyConsumer = createPolicyEvaluator(permission, authorizationProvider, executionContext, decision, verified, decisionCache);
        Resource resource = permission.getResource();

        if (resource != null) {
            evaluateResourcePolicies(permission, authorizationProvider, policyConsumer);
            evaluateResourceTypePolicies(permission, authorizationProvider, policyConsumer);
        }

        evaluateScopePolicies(permission, authorizationProvider, policyConsumer);

        if (verified.get()) {
            decision.onComplete(permission);
            return;
        }

        if (PolicyEnforcementMode.PERMISSIVE.equals(enforcementMode)) {
            if (resource != null && resource.isOwnerManagedAccess()) {
                return;
            }
            grantAndComplete(permission, authorizationProvider, executionContext, decision);
        }
    }

    /** 评估直接绑定到资源实例的策略。 */
    protected void evaluateResourcePolicies(ResourcePermission permission, AuthorizationProvider authorization, Consumer<Policy> policyConsumer) {
        StoreFactory storeFactory = authorization.getStoreFactory();
        PolicyStore policyStore = storeFactory.getPolicyStore();
        ResourceServer resourceServer = permission.getResourceServer();
        Resource resource = permission.getResource();
        policyStore.findByResource(resourceServer, resource, policyConsumer);
    }

    /** 评估绑定到资源类型及同类型父资源的策略。 */
    protected void evaluateResourceTypePolicies(ResourcePermission permission, AuthorizationProvider authorization, Consumer<Policy> policyConsumer) {
        Resource resource = permission.getResource();

        if (resource.getType() != null) {
            StoreFactory storeFactory = authorization.getStoreFactory();
            PolicyStore policyStore = storeFactory.getPolicyStore();
            ResourceServer resourceServer = permission.getResourceServer();

            policyStore.findByResourceType(resourceServer, resource.getType(), policyConsumer);

            if (!resource.getOwner().equals(resourceServer.getClientId())) {
                ResourceStore resourceStore = storeFactory.getResourceStore();

                for (Resource typedResource : resourceStore.findByType(resourceServer, resource.getType())) {
                    policyStore.findByResource(resourceServer, typedResource, policyConsumer);
                }
            }
        }
    }

    /** 评估与请求作用域关联的策略。 */
    protected void evaluateScopePolicies(ResourcePermission permission, AuthorizationProvider authorization, Consumer<Policy> policyConsumer) {
        Collection<Scope> scopes = permission.getScopes();

        if (!scopes.isEmpty()) {
            PolicyStore policyStore = authorization.getStoreFactory().getPolicyStore();
            ResourceServer resourceServer = permission.getResourceServer();
            policyStore.findByScopes(resourceServer, null, new LinkedList<>(scopes), policyConsumer);
        }
    }

    private void grantAndComplete(ResourcePermission permission, AuthorizationProvider authorizationProvider,
            EvaluationContext executionContext, Decision decision) {
        DefaultEvaluation evaluation = new DefaultEvaluation(permission, executionContext, decision, authorizationProvider);

        evaluation.grant();

        decision.onComplete(permission);
    }

    /** 创建策略消费者：查找 {@link PolicyProvider} 并执行 {@link DefaultEvaluation}。 */
    protected Consumer<Policy> createPolicyEvaluator(ResourcePermission permission, AuthorizationProvider authorizationProvider, EvaluationContext executionContext, Decision decision, AtomicBoolean verified, Map<Policy, Map<Object, Decision.Effect>> decisionCache) {
        return parentPolicy -> {
            if (parentPolicy != null) {
                PolicyProvider policyProvider = authorizationProvider.getProvider(parentPolicy.getType());

                if (policyProvider == null) {
                    throw new RuntimeException("Unknown parentPolicy provider for type [" + parentPolicy.getType() + "].");
                }

                policyProvider.evaluate(new DefaultEvaluation(permission, executionContext, parentPolicy, decision, authorizationProvider, decisionCache));

                verified.compareAndSet(false, true);
            }
        };
    }
}
